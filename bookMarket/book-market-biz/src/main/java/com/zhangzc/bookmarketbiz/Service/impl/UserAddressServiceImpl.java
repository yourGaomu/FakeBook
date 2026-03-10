package com.zhangzc.bookmarketbiz.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookmarketbiz.Const.UserAddressConst;
import com.zhangzc.bookmarketbiz.Domain.UserAddress;
import com.zhangzc.bookmarketbiz.Dto.UserAddressDto;
import com.zhangzc.bookmarketbiz.Repository.UserAddressRepository;
import com.zhangzc.bookmarketbiz.Service.UserAddressService;
import com.zhangzc.bookmarketbiz.Vo.UserAddressVo;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.leaf.server.service.SegmentService;
import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository addressRepository;
    private final MongoTemplate mongoTemplate;
    private final RedisUtil redisUtil;
    private final SegmentService segmentService;

    @Override
    public List<UserAddressVo> listAddresses() throws BizException {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            throw new BizException("用户未登录");
        }

        String key = UserAddressConst.getUserAddressKey(userId);

        // 1. 尝试从 Redis Hash 中获取
        if (redisUtil.hasKey(key)) {
            Map<Object, Object> addressMap = redisUtil.hmget(key);
            if (CollUtil.isNotEmpty(addressMap)) {
                return addressMap.values().stream()
                        .map(obj -> JSONUtil.toBean(obj.toString(), UserAddressVo.class))
                        .sorted(Comparator.comparing(UserAddressVo::getIsDefault, Comparator.reverseOrder()) // 默认地址排前面
                                .thenComparing(UserAddressVo::getCreatedAt, Comparator.reverseOrder()))
                        .collect(Collectors.toList());
            }
        }

        // 2. Redis 未命中，查询 MongoDB
        List<UserAddress> addresses = addressRepository.findByUserId(userId);
        List<UserAddressVo> vos = addresses.stream()
                .map(this::convertToVo)
                .sorted(Comparator.comparing(UserAddressVo::getIsDefault, Comparator.reverseOrder())
                        .thenComparing(UserAddressVo::getCreatedAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // 3. 写入 Redis Hash (ZHash 结构)

        CompletableFuture.runAsync(()->{
            if (CollUtil.isNotEmpty(vos)) {
                Map<String, Object> map = new HashMap<>();
                for (UserAddressVo vo : vos) {
                    map.put(vo.getId(), JSONUtil.toJsonStr(vo));
                }
                redisUtil.hmset(key, map, 3600 * 24); // 缓存 24 小时
            }
        });

        return vos;
    }

    @Override
    public String addAddress(UserAddressDto dto) throws BizException {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            throw new BizException("用户未登录");
        }

        // 处理默认地址逻辑
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefaultAddress(userId);
        }

        UserAddress address = new UserAddress();
        BeanUtil.copyProperties(dto, address);
        
        // 生成 ID
        try {
             // 尝试获取 ID，如果 leaf 服务不可用，降级使用 UUID
            String id = String.valueOf(segmentService.getId("user_address").getId());
            address.setId(id);
        } catch (Exception e) {
            log.warn("Leaf service error, using UUID instead", e);
            address.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        
        address.setUserId(userId);
        address.setCreatedAt(new Date());
        address.setUpdatedAt(new Date());
        
        // 如果是该用户第一个地址，强制设为默认
        long count = mongoTemplate.count(new Query(Criteria.where("userId").is(userId)), UserAddress.class);
        if (count == 0) {
            address.setIsDefault(true);
        }

        addressRepository.save(address);

        // 删除缓存，下次读取时重建
        redisUtil.del(UserAddressConst.getUserAddressKey(userId));

        return address.getId();
    }

    @Override
    public void updateAddress(UserAddressDto dto) throws BizException {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            throw new BizException("用户未登录");
        }

        UserAddress address = addressRepository.findById(dto.getId())
                .orElseThrow(() -> new BizException("地址不存在"));

        if (!address.getUserId().equals(userId)) {
            throw new BizException("无权修改此地址");
        }

        // 处理默认地址逻辑
        if (Boolean.TRUE.equals(dto.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefaultAddress(userId);
        }

        BeanUtil.copyProperties(dto, address, "id", "userId", "createdAt");
        address.setUpdatedAt(new Date());

        // 删除缓存
        CompletableFuture.runAsync(()->{
            addressRepository.save(address);

            redisUtil.del(UserAddressConst.getUserAddressKey(userId));
        });

    }

    @Override
    public void deleteAddress(String id) throws BizException {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            throw new BizException("用户未登录");
        }

        UserAddress address = addressRepository.findById(id)
                .orElseThrow(() -> new BizException("地址不存在"));

        if (!address.getUserId().equals(userId)) {
            throw new BizException("无权删除此地址");
        }
        CompletableFuture.runAsync(()->{
            addressRepository.delete(address);
            // 删除缓存
            redisUtil.del(UserAddressConst.getUserAddressKey(userId));
        });
    }

    /**
     * 将该用户的所有地址设为非默认
     */
    private void clearDefaultAddress(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId).and("isDefault").is(true));
        Update update = new Update().set("isDefault", false).set("updatedAt", new Date());
        mongoTemplate.updateMulti(query, update, UserAddress.class);
    }

    private UserAddressVo convertToVo(UserAddress address) {
        UserAddressVo vo = new UserAddressVo();
        BeanUtil.copyProperties(address, vo);
        return vo;
    }
}
