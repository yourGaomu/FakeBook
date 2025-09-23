package com.zhangzc.bookcountbiz.Service.impl;

import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Maps;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountapi.Pojo.Dto.Req.FindUserCountsByIdReqDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.Resp.FindUserCountsByIdRspDTO;
import com.zhangzc.bookcountbiz.Const.RedisKeyConstants;
import com.zhangzc.bookcountbiz.Enum.ResponseCodeEnum;
import com.zhangzc.bookcountbiz.Pojo.Domain.TUserCount;
import com.zhangzc.bookcountbiz.Rpc.UserRpcService;
import com.zhangzc.bookcountbiz.Service.TUserCountService;
import com.zhangzc.bookcountbiz.Service.UserCountService;
import com.zhangzc.bookcountbiz.Utills.RedisUtil;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserCountServiceImpl implements UserCountService {

    private final RedisUtil redisUtil;
    private final TUserCountService tUserCountService;
    private final UserRpcService userRpcService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public R<FindUserCountsByIdRspDTO> findUserCountData(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO) throws BizException {
        // 如果用户id为空
        if (findUserCountsByIdReqDTO.getUserId() == null) {
            throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);
        }

        Long userId = findUserCountsByIdReqDTO.getUserId();
        String userKey = RedisKeyConstants.buildCountUserKey(userId);
        FindUserCountsByIdRspDTO findUserCountsByIdRspDTO = new FindUserCountsByIdRspDTO();
        findUserCountsByIdRspDTO.setUserId(userId);

        // 先查Redis
        if (redisUtil.hasKey(userKey)) {
            List<Object> fields = List.of(
                    RedisKeyConstants.FIELD_COLLECT_TOTAL,
                    RedisKeyConstants.FIELD_FANS_TOTAL,
                    RedisKeyConstants.FIELD_NOTE_TOTAL,
                    RedisKeyConstants.FIELD_FOLLOWING_TOTAL,
                    RedisKeyConstants.FIELD_LIKE_TOTAL
            );

            List<Object> result = redisTemplate.opsForHash().multiGet(userKey, fields);

            findUserCountsByIdRspDTO.setCollectTotal(parseLongOrZero(result.get(0)));
            findUserCountsByIdRspDTO.setFansTotal(parseLongOrZero(result.get(1)));
            findUserCountsByIdRspDTO.setNoteTotal(parseLongOrZero(result.get(2)));
            findUserCountsByIdRspDTO.setFollowingTotal(parseLongOrZero(result.get(3)));
            findUserCountsByIdRspDTO.setLikeTotal(parseLongOrZero(result.get(4)));

            return R.success(findUserCountsByIdRspDTO);
        }

        // Redis不存在则查数据库
        TUserCount userCount = tUserCountService.lambdaQuery()
                .eq(TUserCount::getUserId, userId)
                .one();

        // 处理数据库查询结果可能为null的情况
        if (Objects.isNull(userCount)) {
            // 如果数据库也没有记录，初始化默认值
            findUserCountsByIdRspDTO.setFansTotal(0L);
            findUserCountsByIdRspDTO.setFollowingTotal(0L);
            findUserCountsByIdRspDTO.setNoteTotal(0L);
            findUserCountsByIdRspDTO.setLikeTotal(0L);
            findUserCountsByIdRspDTO.setCollectTotal(0L);
        } else {
            findUserCountsByIdRspDTO.setFansTotal(userCount.getFansTotal());
            findUserCountsByIdRspDTO.setFollowingTotal(userCount.getFollowingTotal());
            findUserCountsByIdRspDTO.setNoteTotal(userCount.getNoteTotal());
            findUserCountsByIdRspDTO.setLikeTotal(userCount.getLikeTotal());
            findUserCountsByIdRspDTO.setCollectTotal(userCount.getCollectTotal());
        }

        // 同步到Redis（只需要传递userCountDO，不需要传递DTO的值）
        syncHashCount2Redis(userKey, userCount);

        return R.success(findUserCountsByIdRspDTO);
    }

    // 辅助方法：将对象转换为Long，null则返回0
    private Long parseLongOrZero(Object value) {
        if (value == null) {
            return 0L;
        }
        // 处理可能的数字类型（Redis返回的可能是Long或String等类型）
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 将该用户的 Hash 计数同步到 Redis 中
     *
     * @param userCountHashKey
     * @param userCountDO
     * @return
     */
    private void syncHashCount2Redis(String userCountHashKey, TUserCount userCountDO) {
        if (Objects.nonNull(userCountDO)) {
            CompletableFuture.runAsync(() -> {
                // 存放计数，直接从userCountDO获取值，不存在则设为0
                Map<String, Long> userCountMap = Maps.newHashMap();
                userCountMap.put(RedisKeyConstants.FIELD_COLLECT_TOTAL,
                        Objects.requireNonNullElse(userCountDO.getCollectTotal(), 0L));
                userCountMap.put(RedisKeyConstants.FIELD_FANS_TOTAL,
                        Objects.requireNonNullElse(userCountDO.getFansTotal(), 0L));
                userCountMap.put(RedisKeyConstants.FIELD_NOTE_TOTAL,
                        Objects.requireNonNullElse(userCountDO.getNoteTotal(), 0L));
                userCountMap.put(RedisKeyConstants.FIELD_FOLLOWING_TOTAL,
                        Objects.requireNonNullElse(userCountDO.getFollowingTotal(), 0L));
                userCountMap.put(RedisKeyConstants.FIELD_LIKE_TOTAL,
                        Objects.requireNonNullElse(userCountDO.getLikeTotal(), 0L));

                redisTemplate.executePipelined(new SessionCallback<Object>() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        // 批量添加 Hash 的计数 Field
                        operations.opsForHash().putAll(userCountHashKey, userCountMap);
                        // 设置随机过期时间（2小时以内）
                        long expireTime = 60 * 60 + RandomUtil.randomInt(60 * 60);
                        operations.expire(userCountHashKey, expireTime, TimeUnit.SECONDS);
                        return null;
                    }
                });
            });
        }
    }


}
