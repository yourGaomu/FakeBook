package com.zhangzc.bookrelationbiz.Utils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqUtil {
    private final RabbitTemplate rabbitTemplate;



    /**
     * 发送延迟消息到注解声明的延迟交换机
     */
    public void sendDelayMessage(String exchange, String routingKey, @NonNull String message, long delayTime) {

        delayTime = delayTime * 1000;//变成秒

        Message delayMessage = MessageBuilder
                .withBody(message.getBytes())
                .setHeader("x-delay", delayTime)  // 延迟时间（毫秒）
                .build();

        // 发送到注解中声明的延迟交换机和路由键
        rabbitTemplate.send(
                exchange,  // 交换机名称（与@Exchange的name一致）
                routingKey,  // 路由键（与@QueueBinding的key一致）
                delayMessage
        );

        System.out.println("发送延迟消息：" + message + "，延迟时间：" + delayTime + "ms");
    }

    /*
    * 发送消息
    * @param exchange 交换机
    * @param routekey 路由
    * @param message 消息
    * */
    public void send(String exchange,String routekey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routekey, message);
    }

    /*
    * 发送消息
    * @param routingKey 路由键
    * */
    public void send(String routingKey, Object message) {
        rabbitTemplate.convertAndSend(routingKey, message);
    }



    /*
    * 发送消息
    * @param exchange 交换机
    * @param routingKey 路由键
    * @param message 消息
    * @param correlationId 相关id
    * */
    public void send(String exchange, String routingKey, Object message, String correlationId) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, message1 -> {
            message1.getMessageProperties().setCorrelationId(correlationId);
            return message1;
        });
    }
}
