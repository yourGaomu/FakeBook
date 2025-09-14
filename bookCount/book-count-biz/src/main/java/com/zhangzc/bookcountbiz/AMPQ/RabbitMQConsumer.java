package com.zhangzc.bookcountbiz.AMPQ;


import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.reflect.TypeToken;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcountbiz.AMPQ.BufferConsumer.CountConsumer;
import com.zhangzc.bookcountbiz.AMPQ.BufferConsumer.CountNoteConsumer;
import com.zhangzc.bookcountbiz.Const.MQConstants;
import com.zhangzc.bookcountbiz.Const.RedisKeyConstants;
import com.zhangzc.bookcountbiz.Enum.ResponseCodeEnum;
import com.zhangzc.bookcountbiz.Pojo.Domain.TNoteCollection;
import com.zhangzc.bookcountbiz.Pojo.Domain.TNoteCount;
import com.zhangzc.bookcountbiz.Pojo.Domain.TUserCount;
import com.zhangzc.bookcountbiz.Pojo.Dto.CollectUnCollectNoteMqDTO;
import com.zhangzc.bookcountbiz.Pojo.Dto.CountLikeUnlikeNoteMqDTO;
import com.zhangzc.bookcountbiz.Pojo.Vo.CountFollowUnfollowMqDTO;
import com.zhangzc.bookcountbiz.Service.TNoteContentService;
import com.zhangzc.bookcountbiz.Service.TNoteCountService;
import com.zhangzc.bookcountbiz.Service.TUserCountService;
import com.zhangzc.bookcountbiz.Utills.RabbitMqUtil;
import com.zhangzc.bookcountbiz.Utills.RedisUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import com.google.common.util.concurrent.RateLimiter;

import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {


    @PostConstruct
    public void init() {
        bufferTrigger = BufferTrigger.<String>batchBlocking()
                .bufferSize(50000) // 缓存队列的最大容量
                .batchSize(1000)   // 一批次最多聚合 1000 条
                .linger(Duration.ofSeconds(1)) // 多久聚合一次
                .setConsumerEx(countConsumer::consumeCountMessage)
                .build();

        bufferTrigger2 = BufferTrigger.<String>batchBlocking()
                .bufferSize(50000) // 缓存队列的最大容量
                .batchSize(1000)   // 一批次最多聚合 1000 条
                .linger(Duration.ofSeconds(1)) // 多久聚合一次
                .setConsumerEx(countNoteConsumer::consumeCountMessage)
                .build();
    }

    // 每秒创建 5000 个令牌
    private RateLimiter rateLimiter = RateLimiter.create(5000);
    private BufferTrigger<String> bufferTrigger;
    private BufferTrigger<String> bufferTrigger2;
    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RedisUtil redisUtil;
    private final RabbitMqUtil rabbitMqUtil;
    private final CountConsumer countConsumer;
    private final CountNoteConsumer countNoteConsumer;
    private final TUserCountService tUserCountService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TNoteCountService tNoteCountService;


    @RabbitListener(queues = "count.collectionDBqueue")
    public void consumeCollectionMessage(String message) {
        try {
            //开始序列化
            List<CollectUnCollectNoteMqDTO> tNoteCollections = JsonUtils.parseList(message, new TypeReference<List<CollectUnCollectNoteMqDTO>>() {
            });// 泛型类型引用)
            //开始计数
            //先按照笔记id分组
            Map<Long, List<CollectUnCollectNoteMqDTO>> collect = tNoteCollections.stream().collect(Collectors.groupingBy(CollectUnCollectNoteMqDTO::getNoteId));
            collect.forEach((k, v) -> {
                //开始计数
                int count = v.stream().filter(sign -> sign.getType() == 1).collect(Collectors.toList()).size();
                int uncount = v.stream().filter(sign -> sign.getType() == 0).collect(Collectors.toList()).size();
                int total = count - uncount;
                //开始入库
                //先加载进入zset
                String redisKey = RedisKeyConstants.buildCountNoteKey(k);
                // 判断 Redis 中 Hash 是否存在
                boolean isExisted = redisUtil.hasKey(redisKey);
                if (isExisted) {
                    // 对目标用户 Hash 中的收藏总数字段进行计数操作
                    redisTemplate.opsForHash().increment(redisKey, RedisKeyConstants.FIELD_COLLECT_TOTAL, total);
                }

                tNoteCountService.lambdaUpdate()
                        .eq(TNoteCount::getNoteId, k)
                        .setIncrBy(TNoteCount::getCollectTotal, total).update();
            });

        } catch (Exception e) {
            log.error("==> 转换计数数据失败，message:{}", message, e);
        }
    }

    @RabbitListener(queues = "count.NoteDBqueue")
    @Transactional(rollbackFor = Exception.class)
    public void consumeCountMessageToNoteDB(String message) {
        try {
            log.info("开始处理消息");
            if (message == null){
                log.info("消息为空不会处理");
            }
            bufferTrigger2.enqueue(message);
        } catch (Exception e) {
            log.error("==> 转换计数数据失败，message:{}", message, e);
        }
    }


    @RabbitListener(queues = "count.FansDBqueue")
    @Transactional(rollbackFor = Exception.class)
    public void consumeCountMessageToFansDB(String message) {
        try {
            //先获取令牌
            rateLimiter.acquire();
            //先做数据的转换
            Map<Long, Integer> countMap = new Gson().fromJson(
                    message,
                    new TypeToken<Map<Long, Integer>>() {
                    }.getType()
            );
            //开始入库
            countMap.forEach((targetUserId, totalChange) -> {
                tUserCountService.lambdaUpdate()
                        .eq(TUserCount::getUserId, targetUserId)
                        .setIncrBy(TUserCount::getFansTotal, totalChange)
                        .update();
            });
        } catch (Exception e) {
            log.error("==> 转换计数数据失败，message:{}", message, e);
        }
    }

    @RabbitListener(queues = "count.queue")
    @SneakyThrows
    public void consumeCountMeg(String message) {
        log.info("===> 接受到计数服务的消息：{}", message);
        CountFollowUnfollowMqDTO dto = JsonUtils.parseObject(message, CountFollowUnfollowMqDTO.class);
        if (dto == null) {
            throw new BizException(ResponseCodeEnum.OBJECT_CONVERT_ERROR);
        }
        //使用compertableFuture完成并且添加超时
        CompletableFuture.runAsync(() -> bufferTrigger.enqueue(JsonUtils.toJsonString(dto)), threadPoolTaskExecutor)
                .orTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    // 处理超时异常
                    log.error("异步任务提交或超时失败，userId={}", dto.getUserId(), ex);
                    return null;
                });
    }


    @RabbitListener(bindings = {  // 注意：多个绑定关系用 {} 包裹，逗号分隔
            // 第一个队列绑定
            @QueueBinding(
                    value = @Queue(
                            name = "count.queue",   // 队列1名称
                            durable = "true"        // 持久化
                    ),
                    exchange = @Exchange(
                            name = "count.exchange",         // 同一个延迟交换机
                            type = ExchangeTypes.TOPIC,      // 延迟交换机类型
                            durable = "true"
                    ),
                    key = MQConstants.TAG_COUNT  // 相同的路由键
            )
    })
    public void consumeCountMessage(String message) {
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "count.collectionDBqueue", declare = "true"),
            exchange = @Exchange(name = "collection.exchange", type = ExchangeTypes.TOPIC, declare = "true"
            )
            , key = MQConstants.TAG_COUNT_COLLECT_UNCOLLECT
    ))
    public void consumeCountMessage4(String message) {
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "count.NoteDBqueue", declare = "true"),
            exchange = @Exchange(name = "count.exchange", type = ExchangeTypes.TOPIC, declare = "true"
            )
            , key = MQConstants.TAG_COUNT_NOTE_DB
    ))
    public void consumeCountMessage3(String message) {
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "count.FansDBqueue", declare = "true"),
            exchange = @Exchange(name = "count.exchange", type = ExchangeTypes.TOPIC, declare = "true"
            )
            , key = MQConstants.TAG_COUNT_DB
    ))
    public void consumeCountMessage2(String message) {
    }
}
