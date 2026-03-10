package com.zhangzc.emailspringbootstart.consume;


import com.zhangzc.emailspringbootstart.consts.Email4KafkaEnum;
import com.zhangzc.emailspringbootstart.corn.MailHelper;
import com.zhangzc.emailspringbootstart.vo.MailVo;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.kafkaspringbootstart.annotation.AutoInserByRedis;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Objects;

@RequiredArgsConstructor
public class Kafka4SmsConsume {
    private final String topic4UserNotification = Email4KafkaEnum.KAFKA_EMAIL_USER_ACTION.getTopic();
    private final MailHelper mailHelper;

    @KafkaListener(topics = "email.user.action")
    @AutoInserByRedis(
            strategy = AutoInserByRedis.DuplicateStrategy.SKIP, // 重复消费跳过
            enableAlert = true,                                   // 启用告警
            redisKeyPrefix = "kafka:offset"                      // Redis key前缀
    )
    public void consumeUserNotification(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        Object value = record.value();
        //开始序列化
        try {
            MailVo mailVo = null;
            if (value instanceof String) {
                mailVo = JsonUtils.parseObject((String) value, MailVo.class);
            } else if (value instanceof MailVo) {
                mailVo = (MailVo) value;
            } else {
                mailVo = JsonUtils.parseObject(value.toString(), MailVo.class);
            }
            if (!Objects.isNull(mailVo)) {
                mailHelper.send(mailVo);
            }
            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
