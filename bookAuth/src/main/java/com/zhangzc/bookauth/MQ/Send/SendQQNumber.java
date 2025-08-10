package com.zhangzc.bookauth.MQ.Send;

import com.zhangzc.bookauth.MQ.Service.RabbitMQService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendQQNumber {

    private final RabbitMQService rabbitMQService;
    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "book.sendCode", declare = "true"),//不会持久化
            exchange = @Exchange(name = "book.send", type = ExchangeTypes.TOPIC, declare = "true")//topic的交换机,也不会持久化
    ))
    public void sendCode(String to, String title, String code) {
        threadPoolTaskExecutor.execute(() -> {
            rabbitMQService.sendCode(to, title, code);
        });
    }
}
