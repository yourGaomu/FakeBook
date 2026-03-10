package com.zhangzc.bookmarketbiz.Service.impl;

import cn.hutool.core.collection.CollUtil;
import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookmarketbiz.Domain.MarketComment;
import com.zhangzc.bookmarketbiz.Repository.MarketCommentRepository;
import com.zhangzc.bookmarketbiz.Rpc.User4Rpc;
import com.zhangzc.bookmarketbiz.Service.MarketCommentService;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.kafkaspringbootstart.utills.KafkaUtills;
import com.zhangzc.leaf.server.service.SegmentService;
import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketCommentServiceImpl implements MarketCommentService {

    private static final String COMMENT_CACHE_PREFIX = "product:comment:";
    private static final String COMMENT_TOPIC = "market-comment-topic";
    private final MarketCommentRepository marketCommentRepository;
    private final RedisUtil redisUtil;
    private final KafkaUtills kafkaUtills;
    private final SegmentService segmentService;
    private final User4Rpc user4Rpc;

    @Override
    public String addComment(MarketComment comment) {
        // 1. 获取当前用户信息
        Long userId = LoginUserContextHolder.getUserId();
        FindUserByIdRspDTO userInfoByUserID = user4Rpc.getUserInfoByUserID(userId);
        if (userInfoByUserID == null) {
            throw new RuntimeException("用户信息有误，请重新尝试");
        }

        // 2. 补全评论信息
        String commentId = String.valueOf(segmentService.getId("market_comment_id").getId());
        comment.setId(commentId);
        comment.setUserId(userId);
        comment.setUserNickname(userInfoByUserID.getNickName());
        comment.setUserAvatar(userInfoByUserID.getAvatar());
        comment.setCreateTime(new Date());

        // 3. 保存到 MongoDB (Source of Truth)
        marketCommentRepository.save(comment);

        // 4. 发送 Kafka 消息，异步更新缓存
        kafkaUtills.sendMessage(COMMENT_TOPIC, comment);

        return comment.getId();
    }

    @Override
    public PageResponse<MarketComment> listComments(String itemId, int page, int size) {
        String cacheKey = COMMENT_CACHE_PREFIX + itemId;
        long start = (long) (page - 1) * size;
        long end = start + size - 1;

        // 1. 尝试从 Redis 获取
        if (redisUtil.hasKey(cacheKey)) {
            Set<Object> cachedObjects = redisUtil.zReverseRange(cacheKey, start, end);
            if (CollUtil.isNotEmpty(cachedObjects)) {
                List<MarketComment> comments = cachedObjects.stream()
                        .map(obj -> {
                            MarketComment comment = null;
                            try {
                                if (obj instanceof MarketComment) {
                                    comment = (MarketComment) obj;
                                } else if (obj instanceof String jsonString) {
                                    comment = JsonUtils.parseObject(jsonString, MarketComment.class);
                                } else {
                                    comment = JsonUtils.parseObject(JsonUtils.toJsonString(obj), MarketComment.class);
                                }

                            } catch (Exception e) {
                                throw  new RuntimeException(e);
                            }
                            return comment;
                        })
                        .collect(Collectors.toList());
                // 获取总数
                long total = redisUtil.zCard(cacheKey);
                return PageResponse.success(comments, page, total, size);
            }
        }

        // 2. Redis 未命中或为空（可能是过期或首次访问），查询 MongoDB
        // 策略：查询 DB 所有评论（假设数量可控，或者限制最近 1000 条），写入 Redis
        List<MarketComment> allComments = marketCommentRepository.findByItemId(itemId, Sort.by(Sort.Direction.DESC, "createTime"));

        if (CollUtil.isNotEmpty(allComments)) {

            CompletableFuture.runAsync(() -> {
                try {
                    Map<Object, Double> zSetData = new HashMap<>();
                    for (MarketComment c : allComments) {
                        zSetData.put(c, (double) c.getCreateTime().getTime());
                    }
                    redisUtil.zAdd(cacheKey, zSetData);
                    redisUtil.expire(cacheKey, 3600); // 1小时过期
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // 手动分页
            int total = allComments.size();
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, total);

            List<MarketComment> pageList;
            if (fromIndex >= total) {
                pageList = Collections.emptyList();
            } else {
                pageList = allComments.subList(fromIndex, toIndex);
            }
            return PageResponse.success(pageList, page, total, size);
        }
        // 空结果
        return PageResponse.success(new ArrayList<>(), page, 0L, size);
    }
}
