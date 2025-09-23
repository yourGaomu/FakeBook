package com.zhangzc.bookuserbiz.AMPQ;

import com.zhangzc.bookuserbiz.Const.MQConstants;
import com.zhangzc.bookuserbiz.Const.RedisKeyConstants;
import com.zhangzc.bookuserbiz.Utils.RedisUtil;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RabbitMQConsumer {
    private final RedisUtil redisUtil;


    @RabbitListener(queues = "DelayUpdateUserInfo.queue")
    public void consumeDelayUpdateUserInfoMessage(String message) {
        //延时双删除,保持信息一致性
        if (message == null) {
            return;
        }
        Long userId = Long.valueOf(message);
        redisUtil.del(RedisKeyConstants.buildUserInfoKey(userId));
        redisUtil.del(RedisKeyConstants.buildUserProfileKey(userId));
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    // 1. 配置延迟队列（自动创建，持久化）
                    value = @Queue(
                            name = "DelayUpdateUserInfo.queue",  // 队列名，与之前报错的队列一致
                            durable = "true",                   // 持久化：重启 RabbitMQ 队列不丢失
                            autoDelete = "false",               // 不自动删除：消费者断开后队列保留
                            exclusive = "false"                 // 非排他：多个消费者可监听同一个队列
                    ),
                    // 2. 配置延迟交换机（核心：添加 delayed = true 和 x-delayed-type 参数）
                    exchange = @Exchange(
                            name = "delay.exchange",            // 交换机名
                            type = ExchangeTypes.TOPIC,         // 交换机类型（与 x-delayed-type 一致）
                            durable = "true",                   // 交换机持久化
                            delayed = "true",                   // 关键：标识这是延迟交换机（依赖插件）
                            arguments = {                       // 关键：指定延迟消息的底层交换机类型
                                    @Argument(name = "x-delayed-type", value = "topic")
                            }
                    ),
                    // 3. 配置路由键（与生产者发送消息时的路由键一致）
                    key = MQConstants.TOPIC_DELAY_USER_INFO_UPDATE  // 如："user.info.delay.update"
            )
    })
    public void consumeCountMessage(String message) {
    }
}
