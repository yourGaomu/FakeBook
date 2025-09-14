package com.zhangzc.booknotebiz.AMQP;

import com.github.phantomthief.collection.BufferTrigger;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.booknotebiz.Const.MQConstants;
import com.zhangzc.booknotebiz.Pojo.Domain.TNoteCollection;
import com.zhangzc.booknotebiz.Pojo.Domain.TNoteLike;
import com.zhangzc.booknotebiz.Pojo.Dto.CollectUnCollectNoteMqDTO;
import com.zhangzc.booknotebiz.Pojo.Dto.CountLikeUnlikeNoteMqDTO;
import com.zhangzc.booknotebiz.Pojo.Dto.LikeUnlikeNoteMqDTO;
import com.zhangzc.booknotebiz.Service.TNoteCollectionService;
import com.zhangzc.booknotebiz.Service.TNoteLikeService;
import com.zhangzc.booknotebiz.Utils.RabbitMqUtil;
import com.zhangzc.booknotebiz.Utils.RedisUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {
    private final RabbitMqUtil rabbitMqUtil;
    private final TNoteCollectionService tNoteCollectService;

    private final TNoteLikeService tNoteLikeService;

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeLikeAndUnlikeMessage)
            .build();

    private BufferTrigger<String> bufferTrigger2 = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeCollectAndUncollectMessage)
            .build();

    private void consumeCollectAndUncollectMessage(List<String> strings) {
        //开始序列化
        log.info("当前有{}条消息", strings.size());
        if (strings == null) {
            return;
        }
        List<CollectUnCollectNoteMqDTO> list = strings.stream()
                .map(string -> JsonUtils.parseObject(string, CollectUnCollectNoteMqDTO.class)).toList();
        //按照分组，按照标签来
        Map<Integer, List<CollectUnCollectNoteMqDTO>> collect = list.stream()
                .collect(Collectors.groupingBy(CollectUnCollectNoteMqDTO::getType));

        collect.forEach((k, v) -> {
            if (k == 1) {
                //收藏
                HandleCollectNoteMessage(v);
                //添加用户收藏
                handleUserCollectNoteMessage(v);
            } else {
                //取消收藏
                HandleUncollectNoteMessage(v);
                //添加用户取消收藏
                handleUserUncollectNoteMessage(v);
            }
        });

    }

    private void handleUserUncollectNoteMessage(List<CollectUnCollectNoteMqDTO> v) {
        //按照用户id分组
        Map<Long, List<CollectUnCollectNoteMqDTO>> collect = v.stream()
                .collect(Collectors.groupingBy(CollectUnCollectNoteMqDTO::getUserId));
        collect.forEach((k, value) -> {
            //发送用户取消收藏消息
            // todo rabbitMqUtil.send("user.exchange", MQConstants.TAG_USER_UNCOLLECT_NOTE, JsonUtils.toJsonString(v));
        });
    }

    private void handleUserCollectNoteMessage(List<CollectUnCollectNoteMqDTO> v) {
        //按照用户id分组
        Map<Long, List<CollectUnCollectNoteMqDTO>> collect = v.stream()
                .collect(Collectors.groupingBy(CollectUnCollectNoteMqDTO::getUserId));



        collect.forEach((k, value) -> {
            //发送用户收藏消息
            rabbitMqUtil.send("user.exchange", MQConstants.TAG_USER_COLLECT_NOTE, JsonUtils.toJsonString(v));
        });
    }

    private void HandleUncollectNoteMessage(List<CollectUnCollectNoteMqDTO> v) {
        List<TNoteCollection> list = v.stream().map(sign -> {
            //构建实体对象
            TNoteCollection tNoteCollection = new TNoteCollection();
            tNoteCollection.setUserId(sign.getUserId());
            tNoteCollection.setNoteId(sign.getNoteId());
            tNoteCollection.setStatus(0);
            tNoteCollection.setCreateTime(TimeUtil.getDateTime(sign.getCreateTime()));
            return tNoteCollection;
        }).toList();
        tNoteCollectService.saveOrUpdateTnoteCollection(list);
        CompletableFuture.runAsync(() -> {
            rabbitMqUtil.send("count.exchange", MQConstants.TAG_COUNT_COLLECT_UNCOLLECT, JsonUtils.toJsonString(v));
        });
    }

    private void HandleCollectNoteMessage(List<CollectUnCollectNoteMqDTO> v) {
        List<TNoteCollection> list = v.stream().map(sign -> {
            //构建实体对象
            TNoteCollection tNoteCollection = new TNoteCollection();
            tNoteCollection.setUserId(sign.getUserId());
            tNoteCollection.setNoteId(sign.getNoteId());
            tNoteCollection.setStatus(1);
            tNoteCollection.setCreateTime(TimeUtil.getDateTime(sign.getCreateTime()));
            return tNoteCollection;
        }).toList();
        tNoteCollectService.saveOrUpdateTnoteCollection(list);
        CompletableFuture.runAsync(() -> {
            rabbitMqUtil.send("count.exchange", MQConstants.TAG_COUNT_COLLECT_UNCOLLECT, JsonUtils.toJsonString(v));
        });
    }


    private void consumeLikeAndUnlikeMessage(List<String> strings) {
        log.info("开始处理点赞和取消点赞的消息一共{}", strings.size());
        //点赞入库转换对象
        List<LikeUnlikeNoteMqDTO> list = strings.stream()
                .map(s -> JsonUtils.parseObject(s, LikeUnlikeNoteMqDTO.class)).toList();
        //点赞计数入库对象
        //按照点赞和取消点赞分开
        Map<Integer, List<LikeUnlikeNoteMqDTO>> collect = list.stream().collect(Collectors.groupingBy(LikeUnlikeNoteMqDTO::getType));
        List<LikeUnlikeNoteMqDTO> likeNoteMqDTOS = collect.get(1);//点赞
        List<LikeUnlikeNoteMqDTO> UnlikeNoteMqDTOS = collect.get(0);//取消点赞
        HandleLikeNoteMessage(likeNoteMqDTOS);
        HandleUnlikeNoteMessage(UnlikeNoteMqDTOS);
        log.info("处理点赞和取消点赞的消息结束");
    }

    private void HandleUnlikeNoteMessage(List<LikeUnlikeNoteMqDTO> unlikeNoteMqDTOS) {
        //转换
        if (unlikeNoteMqDTOS == null || unlikeNoteMqDTOS.isEmpty()) {
            return;
        }
        List<TNoteLike> list = unlikeNoteMqDTOS.stream().map(one -> {
            TNoteLike tNoteLike = new TNoteLike();
            tNoteLike.setUserId(one.getUserId());
            tNoteLike.setNoteId(one.getNoteId());
            tNoteLike.setCreateTime(TimeUtil.getDateTime(one.getCreateTime()));
            tNoteLike.setStatus(0);
            return tNoteLike;
        }).toList();
        //如果队列为空
        if (list.isEmpty()) {
            return;
        }
        tNoteLikeService.batchUpsert(list);
        threadPoolTaskExecutor.execute(() -> {
            //发送计数服务
            unlikeNoteMqDTOS.forEach(sign -> {
                CountLikeUnlikeNoteMqDTO countLikeUnlikeNoteMqDTO = new CountLikeUnlikeNoteMqDTO();
                countLikeUnlikeNoteMqDTO.setUserId(sign.getUserId());
                countLikeUnlikeNoteMqDTO.setNoteId(sign.getNoteId());
                countLikeUnlikeNoteMqDTO.setType(0);
                countLikeUnlikeNoteMqDTO.setCreateTime(sign.getCreateTime());
                rabbitMqUtil.send("count.exchange", MQConstants.TAG_COUNT_NOTE_DB, JsonUtils.toJsonString(countLikeUnlikeNoteMqDTO));
            });
        });
    }

    private void HandleLikeNoteMessage(List<LikeUnlikeNoteMqDTO> likeNoteMqDTOS) {
        if (likeNoteMqDTOS == null || likeNoteMqDTOS.isEmpty()) {
            return;
        }
        List<TNoteLike> list = likeNoteMqDTOS.stream().map(one -> {
            TNoteLike tNoteLike = new TNoteLike();
            tNoteLike.setUserId(one.getUserId());
            tNoteLike.setNoteId(one.getNoteId());
            tNoteLike.setStatus(1);
            tNoteLike.setCreateTime(TimeUtil.getDateTime(one.getCreateTime()));
            return tNoteLike;
        }).toList();
        //如果队列为空
        if (list.isEmpty()) {
            return;
        }
        tNoteLikeService.batchUpsert(list);
        threadPoolTaskExecutor.execute(() -> {
            //发送计数服务
            likeNoteMqDTOS.forEach(sign -> {
                CountLikeUnlikeNoteMqDTO countLikeUnlikeNoteMqDTO = new CountLikeUnlikeNoteMqDTO();
                countLikeUnlikeNoteMqDTO.setUserId(sign.getUserId());
                countLikeUnlikeNoteMqDTO.setNoteId(sign.getNoteId());
                countLikeUnlikeNoteMqDTO.setType(1);
                countLikeUnlikeNoteMqDTO.setCreateTime(sign.getCreateTime());
                rabbitMqUtil.send("count.exchange", MQConstants.TAG_COUNT_NOTE_DB, JsonUtils.toJsonString(countLikeUnlikeNoteMqDTO));
            });
        });

    }

    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RedisUtil redisUtil;


    @RabbitListener(queues = "likeOrUnlike.queue")
    public void consumeLikeOrUnlikeMessage(String message) {
        log.info("来了一条");
        bufferTrigger.enqueue(message);
    }

    @RabbitListener(queues = "collectOrUncollect.queue")
    public void consumeCollectOrUncollectMessage(String message) {
        bufferTrigger2.enqueue(message);
    }

    @RabbitListener(queues = "delay.queue")
    public void consumeDelayMessageQueue(String message) {
        threadPoolTaskExecutor.submit(() -> {
            try {
                redisUtil.del(message);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("==> 删除笔记Redis缓存失败，noteId:{}", message, e);
            }
        });
    }

//----------------------------------一下的是队列和交换机的声明-----------------------------------------------

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "likeOrUnlike.queue"),
            exchange = @Exchange(name = "likeOrUnlike.exchange", type = ExchangeTypes.TOPIC),
            key = MQConstants.TOPIC_LIKE_OR_UNLIKE
    ))
    public void likeOrUnlikeMessage(String message) {
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "collectOrUncollect.queue"),
            exchange = @Exchange(name = "collectOrUncollect.exchange", type = ExchangeTypes.TOPIC),
            key = MQConstants.TOPIC_COLLECT_OR_UN_COLLECT
    ))
    public void collectOrUncollectedMessage(String message) {
    }


    /**
     * 使用@QueueBinding直接声明延迟交换机、队列及绑定关系
     * 无需单独的配置类，注解会自动创建相关组件
     */
    @RabbitListener(bindings = {  // 注意：多个绑定关系用 {} 包裹，逗号分隔
            // 第一个队列绑定
            @QueueBinding(
                    value = @Queue(
                            name = "delay.queue",   // 队列1名称
                            durable = "true"        // 持久化
                    ),
                    exchange = @Exchange(
                            name = "delay.exchange",         // 同一个延迟交换机
                            type = "x-delayed-message",      // 延迟交换机类型
                            durable = "true",
                            arguments = @Argument(name = "x-delayed-type", value = ExchangeTypes.TOPIC)  // 底层路由类型
                    ),
                    key = MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE  // 相同的路由键
            ),
            // 第二个队列绑定
            @QueueBinding(
                    value = @Queue(
                            name = "delay.queue2",  // 队列2名称
                            durable = "true"        // 持久化
                    ),
                    exchange = @Exchange(
                            name = "delay.exchange",         // 同一个延迟交换机
                            type = "x-delayed-message",      // 延迟交换机类型
                            durable = "true",
                            arguments = @Argument(name = "x-delayed-type", value = ExchangeTypes.TOPIC)
                    ),
                    key = MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE2  // 相同的路由键
            )
    })
    public void consumeDelayMessage(String message) {
    }


}
