package com.zhangzc.bookcountbiz.AMPQ;


import com.zhangzc.bookcountbiz.Const.MQConstants;
import com.zhangzc.bookcountbiz.Utills.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RedisUtil redisUtil;



    @RabbitListener(queues = "delay.queue")
    public void consumeDelayMessageQueue(String message) {
        System.out.println("接受到了消息2");
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
            value = @Queue(name = "fanout.queue1"),
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
