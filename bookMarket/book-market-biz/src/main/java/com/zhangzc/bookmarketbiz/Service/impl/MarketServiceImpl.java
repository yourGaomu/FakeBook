package com.zhangzc.bookmarketbiz.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookmarketapi.Dto.ItemPublishDto;
import com.zhangzc.bookmarketapi.Dto.ItemQueryDto;
import com.zhangzc.bookmarketapi.Dto.OrderCreateDto;
import com.zhangzc.bookmarketapi.Vo.MarketItemVo;
import com.zhangzc.bookmarketapi.Vo.OrderVo;
import com.zhangzc.bookmarketbiz.Domain.MarketFavorite;
import com.zhangzc.bookmarketbiz.Domain.MarketItem;
import com.zhangzc.bookmarketbiz.Domain.MarketOrder;
import com.zhangzc.bookmarketbiz.Repository.MarketFavoriteRepository;
import com.zhangzc.bookmarketbiz.Repository.MarketItemRepository;
import com.zhangzc.bookmarketbiz.Repository.MarketOrderRepository;
import com.zhangzc.bookmarketbiz.Service.MarketService;
import com.zhangzc.bookuserapi.Api.UserFeignApi;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUserByIdReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.leaf.server.service.SegmentService;
import com.zhangzc.redisspringbootstart.redisConst.RedisZHashConst;
import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 二手交易市场核心业务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    private final MarketItemRepository itemRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketFavoriteRepository favoriteRepository;
    private final UserFeignApi userFeignApi;
    private final MongoTemplate mongoTemplate;
    private final SegmentService segmentService;
    private final RedisUtil redisUtil;

    /**
     * 获取商品列表
     * 支持分页、分类筛选、关键词搜索
     * 默认按创建时间倒序排列
     *
     * @param queryDto 查询条件
     * @return 分页结果
     */
    @Override
    public PageResponse<MarketItemVo> listItems(ItemQueryDto queryDto) {
        int page = Math.max(1, queryDto.getPage());
        int pageSize = Math.max(1, queryDto.getPageSize());
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<MarketItem> items;
        long total;
        // 如果有关键词，使用全文搜索
        if (StrUtil.isNotBlank(queryDto.getKeyword())) {
            Criteria criteria = new Criteria().orOperator(
                    Criteria.where("title").regex(queryDto.getKeyword(), "i"),
                    Criteria.where("description").regex(queryDto.getKeyword(), "i")
            );
            if (StrUtil.isNotBlank(queryDto.getCategory()) && !"all".equals(queryDto.getCategory())) {
                criteria.and("category").is(queryDto.getCategory());
            }
            criteria.and("status").is("active");
            Query query = new Query(criteria);

            items = mongoTemplate.find(query, MarketItem.class);
            total = mongoTemplate.count(query, MarketItem.class);
        } else {
            // 普通查询
            Query query = new Query().with(pageRequest);
            if (StrUtil.isNotBlank(queryDto.getCategory()) && !"all".equals(queryDto.getCategory())) {
                query.addCriteria(Criteria.where("category").is(queryDto.getCategory()));
            }
            query.addCriteria(Criteria.where("status").is("active"));
            items = mongoTemplate.find(query, MarketItem.class);

            // 计算总数
            Query countQuery = new Query();
            if (StrUtil.isNotBlank(queryDto.getCategory()) && !"all".equals(queryDto.getCategory())) {
                countQuery.addCriteria(Criteria.where("category").is(queryDto.getCategory()));
            }
            countQuery.addCriteria(Criteria.where("status").is("active"));
            total = mongoTemplate.count(countQuery, MarketItem.class);
        }

        List<MarketItemVo> vos = items.stream().map(this::convertToVo).collect(Collectors.toList());
        return PageResponse.success(vos, page, total, pageSize);
    }

    /**
     * 获取商品详情
     * 同时增加浏览量
     *
     * @param id 商品 ID
     * @return 商品详情 VO
     */
    @Override
    public MarketItemVo getItemDetail(String id) throws BizException {
        MarketItem item = itemRepository.findById(id).orElseThrow(() -> new BizException("Item not found"));
        // 增加浏览量
        //获取用户是否对这个商品点击了我需要
        Boolean status = getUserNeedStatusById(LoginUserContextHolder.getUserId().toString(), id);
        item.setViews(item.getViews() == null ? 1 : item.getViews() + 1);
        //异步保存数据
        CompletableFuture.runAsync(() -> {
            itemRepository.save(item);
        });
        return convertToVo(item, status);
    }

    private Boolean getUserNeedStatusById(String userId, String itemId) {
        //够着Redis
        String key = RedisZHashConst.getMarketItemLikes(itemId);
        if (redisUtil.hasKey(key)) {
            return redisUtil.hHasKey(key, userId);
        }

        Query query = new Query(Criteria.where("itemId").is(itemId));
        List<MarketFavorite> favorites = mongoTemplate.find(query, MarketFavorite.class);

        if (CollUtil.isNotEmpty(favorites)) {
            Map<String, Object> map = new HashMap<>();
            for (MarketFavorite fav : favorites) {
                map.put(String.valueOf(fav.getUserId()), "1");
            }
            redisUtil.hmset(key, map, 3600 * 24);
            return map.containsKey(userId);
        }
        return false;
    }

    /**
     * 发布商品
     * 需要远程调用 User 服务获取用户信息，冗余存储到商品文档中
     *
     * @param publishDto 发布信息
     * @return 商品 ID
     */
    @Override
    public String publishItem(ItemPublishDto publishDto) throws BizException {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            throw new BizException("User not logged in");
        }

        // 远程获取用户信息
        FindUserByIdReqDTO req = new FindUserByIdReqDTO();
        req.setId(userId);
        R<FindUserByIdRspDTO> userResp = userFeignApi.findById(req);
        if (!userResp.isSuccess() || userResp.getData() == null) {
            throw new BizException("Failed to fetch user info");
        }
        FindUserByIdRspDTO user = userResp.getData();

        // 构建商品对象
        MarketItem item = new MarketItem();
        BeanUtil.copyProperties(publishDto, item);

        // 生成商品ID (使用 Segment)
        String itemId = String.valueOf(segmentService.getId("market_item").getId());
        item.setId(itemId);

        // 填充卖家信息 (冗余)
        MarketItem.Seller seller = new MarketItem.Seller();
        seller.setId(String.valueOf(userId));
        seller.setNickname(user.getNickName());
        seller.setAvatar(user.getAvatar());
        seller.setCreditScore(100); // 默认信用分
        item.setSeller(seller);

        // 初始化状态
        item.setStatus("active");
        item.setViews(0);
        item.setLikes(0);
        item.setCreatedAt(new Date());
        item.setUpdatedAt(new Date());

        itemRepository.save(item);
        return item.getId();
    }

    /**
     * 创建订单
     * 校验商品状态，生成订单快照
     *
     * @param orderDto 订单信息
     * @return 订单 VO
     */
    @Override
    public OrderVo createOrder(OrderCreateDto orderDto) throws BizException {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            throw new BizException("User not logged in");
        }

        // 校验商品
        MarketItem item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(() -> new BizException("Item not found"));

        if (!"active".equals(item.getStatus())) {
            throw new BizException("Item is not available");
        }

        // 不能购买自己的商品
        if (item.getSeller().getId().equals(userId)) {
            throw new BizException("Cannot buy your own item");
        }

        // 创建订单
        MarketOrder order = new MarketOrder();
        order.setBuyerId(userId);
        order.setSellerId(Long.valueOf(item.getSeller().getId()));
        order.setTotalAmount(item.getPrice());
        order.setStatus("pending_payment");
        order.setRemark(orderDto.getRemark());
        order.setCreatedAt(new Date());

        // 生成商品快照
        MarketOrder.ItemSnapshot snapshot = new MarketOrder.ItemSnapshot();
        snapshot.setId(item.getId());
        snapshot.setTitle(item.getTitle());
        snapshot.setPrice(item.getPrice());
        if (CollUtil.isNotEmpty(item.getImages())) {
            snapshot.setImage(item.getImages().get(0));
        }
        order.setItem(snapshot);

        orderRepository.save(order);

        return convertToOrderVo(order);
    }

    /**
     * 点赞/取消点赞
     * 维护商品点赞数和用户收藏记录
     *
     * @param id 商品 ID
     * @return 包含最新点赞数和是否已点赞的状态
     */
    @Override
    public Object likeItem(String id) throws BizException {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            throw new BizException("User not logged in");
        }

        Optional<MarketFavorite> favorite = favoriteRepository.findByUserIdAndItemId(userId, id);
        boolean isLiked;
        MarketItem item = itemRepository.findById(id).orElse(null);

        if (favorite.isPresent()) {
            // 取消点赞
            favoriteRepository.delete(favorite.get());
            isLiked = false;
            if (item != null) {
                item.setLikes(Math.max(0, (item.getLikes() == null ? 0 : item.getLikes()) - 1));
                itemRepository.save(item);
            }
        } else {
            // 点赞
            MarketFavorite newFavorite = new MarketFavorite();
            newFavorite.setUserId(userId);
            newFavorite.setItemId(id);
            newFavorite.setCreatedAt(new Date());
            favoriteRepository.save(newFavorite);
            isLiked = true;
            if (item != null) {
                item.setLikes((item.getLikes() == null ? 0 : item.getLikes()) + 1);
                itemRepository.save(item);
            }
        }

        // Update Redis cache for user like status
        String key = RedisZHashConst.getMarketItemLikes(id);
        if (redisUtil.hasKey(key)) {
            if (isLiked) {
                redisUtil.hset(key, String.valueOf(userId), "1");
            } else {
                redisUtil.hdel(key, String.valueOf(userId));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likes", item != null ? item.getLikes() : 0);
        return result;
    }

    // VO 转换辅助方法
    private MarketItemVo convertToVo(MarketItem item, Boolean isLike) {
        MarketItemVo vo = new MarketItemVo();
        BeanUtil.copyProperties(item, vo);
        if (item.getSeller() != null) {
            MarketItemVo.SellerVo sellerVo = new MarketItemVo.SellerVo();
            BeanUtil.copyProperties(item.getSeller(), sellerVo);
            vo.setSeller(sellerVo);
            vo.setIsLike(isLike);
        }
        return vo;
    }

    // VO 转换辅助方法
    private MarketItemVo convertToVo(MarketItem item) {
        MarketItemVo vo = new MarketItemVo();
        BeanUtil.copyProperties(item, vo);
        if (item.getSeller() != null) {
            MarketItemVo.SellerVo sellerVo = new MarketItemVo.SellerVo();
            BeanUtil.copyProperties(item.getSeller(), sellerVo);
            vo.setSeller(sellerVo);
        }
        return vo;
    }


    private OrderVo convertToOrderVo(MarketOrder order) {
        OrderVo vo = new OrderVo();
        BeanUtil.copyProperties(order, vo);
        if (order.getItem() != null) {
            OrderVo.ItemSnapshotVo itemVo = new OrderVo.ItemSnapshotVo();
            BeanUtil.copyProperties(order.getItem(), itemVo);
            vo.setItem(itemVo);
        }
        return vo;
    }
}
