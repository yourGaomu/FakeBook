package com.zhangzc.bookcommentbiz.AMPQ;


import com.github.phantomthief.collection.BufferTrigger;
import com.zhangzc.bookcommentbiz.AMPQ.BufferConsumer.CommentConsume;
import com.zhangzc.bookcommentbiz.Const.RabbitConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {
    private final CommentConsume commentConsume;

    @PostConstruct
    public void init() {
        bufferTrigger = BufferTrigger.<String>batchBlocking()
                .bufferSize(50000) // 缓存队列的最大容量
                .batchSize(1000)   // 一批次最多聚合 1000 条
                .linger(Duration.ofSeconds(1)) // 多久聚合一次
                .setConsumerEx(commentConsume::consumeCommentMessage)
                .build();
    }

    private BufferTrigger<String> bufferTrigger;

    @RabbitListener(queues = "comment.queue")
    public void consumeCommentMessageQueue(String message) {
        bufferTrigger.enqueue(message);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "comment.queue"),
            exchange = @Exchange(name = "comment.exchange", type = ExchangeTypes.TOPIC),
            key = RabbitConstants.TOPIC_COMMENT
    ))
    public void consumeCommentMessage(String message) {
    }


}
