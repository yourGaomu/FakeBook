package com.zhangzc.booknotebiz.AMQP;

import com.github.phantomthief.collection.BufferTrigger;
import com.zhangzc.booknotebiz.Const.MQConstants;
import com.zhangzc.booknotebiz.Pojo.Domain.TNoteLike;
import com.zhangzc.booknotebiz.Pojo.Dto.LikeUnlikeNoteMqDTO;
import com.zhangzc.booknotebiz.Service.MQService;
import com.zhangzc.booknotebiz.Service.TNoteLikeService;
import com.zhangzc.booknotebiz.Utils.RedisUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final TNoteLikeService tNoteLikeService;

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeLikeAndUnlikeMessage)
            .build();

    private void consumeLikeAndUnlikeMessage(List<String> strings) {
        log.info("开始处理点赞和取消点赞的消息一共{}", strings.size());
        threadPoolTaskExecutor.execute(() -> {
            //转换对象
            List<LikeUnlikeNoteMqDTO> list = strings.stream()
                    .map(s -> JsonUtils.parseObject(s, LikeUnlikeNoteMqDTO.class)).toList();
            //按照点赞和取消点赞分开
            Map<Integer, List<LikeUnlikeNoteMqDTO>> collect = list.stream().collect(Collectors.groupingBy(LikeUnlikeNoteMqDTO::getType));
            List<LikeUnlikeNoteMqDTO> likeNoteMqDTOS = collect.get(1);//点赞
            List<LikeUnlikeNoteMqDTO> UnlikeNoteMqDTOS = collect.get(0);//取消点赞
            HandleLikeNoteMessage(likeNoteMqDTOS);
            HandleUnlikeNoteMessage(UnlikeNoteMqDTOS);
        });

    }

    private void HandleUnlikeNoteMessage(List<LikeUnlikeNoteMqDTO> unlikeNoteMqDTOS) {
        //转换
        List<TNoteLike> list = unlikeNoteMqDTOS.stream().map(one -> {
            TNoteLike tNoteLike = new TNoteLike();
            tNoteLike.setUserId(one.getUserId());
            tNoteLike.setNoteId(one.getNoteId());
            tNoteLike.setStatus(0);
            return tNoteLike;
        }).toList();
        tNoteLikeService.batchUpsert(list);

    }

    private void HandleLikeNoteMessage(List<LikeUnlikeNoteMqDTO> likeNoteMqDTOS) {
        List<TNoteLike> list = likeNoteMqDTOS.stream().map(one -> {
            TNoteLike tNoteLike = new TNoteLike();
            tNoteLike.setUserId(one.getUserId());
            tNoteLike.setNoteId(one.getNoteId());
            tNoteLike.setStatus(1);
            return tNoteLike;
        }).toList();
        tNoteLikeService.batchUpsert(list);
    }

    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RedisUtil redisUtil;

    @RabbitListener(queues = "likeOrUnlike.queue")
    public void consumeLikeOrUnlikeMessage(String message) {
        bufferTrigger.enqueue(message);
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
            value = @Queue(name = "fanout.queue1", durable = "true"),
            exchange = @Exchange(name = "fanout.exchange", type = ExchangeTypes.FANOUT),
            key = ""  // 路由键无效，可省略或设为空
    ))
    public void receiveMessage(String message) {

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
