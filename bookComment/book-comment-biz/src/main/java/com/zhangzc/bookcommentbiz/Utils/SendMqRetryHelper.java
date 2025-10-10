package com.zhangzc.bookcommentbiz.Utils;


import com.zhangzc.bookcommentbiz.Pojo.Dto.PublishCommentMqDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendMqRetryHelper {
    private final RabbitMqUtil rabbitMqUtil;

    @Retryable(
            retryFor = { Exception.class },  // 需要重试的异常类型
            maxAttempts = 3,                 // 最大重试次数
            backoff = @Backoff(delay = 1000, multiplier = 2)  // 初始延迟时间 1000ms，每次重试间隔加倍
    )
    public void send(String exchange, String routingKey, Object message) {
        log.info("==> 开始异步发送 MQ, routingKey: {}, publishCommentMqDTO: {}", routingKey, message);

        rabbitMqUtil.send(exchange, routingKey, message);
    }


    /**
     * 兜底方案: 将发送失败的 MQ 写入数据库，之后，通过定时任务扫表，将发送失败的 MQ 再次发送，最终发送成功后，将该记录物理删除
     */
    @Recover
    public void asyncSendMessageFallback(Exception e, String topic, PublishCommentMqDTO publishCommentMqDTO) {
        log.error("==> 多次发送失败, 进入兜底方案, Topic: {}, publishCommentMqDTO: {}", topic, publishCommentMqDTO);

        // TODO:
    }
}

