package com.zhangzc.bookcountbiz.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AMQPConfig {


   /* @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        // 设置 Confirm 回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("✅ 消息已确认: {}", correlationData);
            } else {
                log.error("❌ 消息未确认: {}, 原因: {}", correlationData, cause);
                // 可记录日志、重发、告警
            }
        });

        template.setMandatory(true); // 必须设置为 true 才能触发 return
        template.setReturnsCallback(returned -> {
            String exchange = returned.getExchange();
            String routingKey = returned.getRoutingKey();
            String msg = new String(returned.getMessage().getBody());
            log.warn("⚠️ 消息未路由: exchange={}, routingKey={}, body={}", exchange, routingKey, msg);
            // 可记录日志、存入数据库、告警
        });


        return template;
    }
*/
}
