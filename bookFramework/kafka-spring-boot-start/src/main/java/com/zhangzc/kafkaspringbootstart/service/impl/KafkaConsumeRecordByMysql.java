package com.zhangzc.kafkaspringbootstart.service.impl;

import com.zhangzc.kafkaspringbootstart.core.pojo.entity.KafkaRecord;
import com.zhangzc.kafkaspringbootstart.core.service.KafkaRecordService;
import com.zhangzc.kafkaspringbootstart.service.StoreKafkaRecord;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Arrays;
import java.util.Date;

@RequiredArgsConstructor
public class KafkaConsumeRecordByMysql implements StoreKafkaRecord {
    private final KafkaRecordService kafkaRecordService;

    @Override
    public void storeKafkaRecordOnSuccess(ProducerRecord<String, String> producerRecord, RecordMetadata recordMetadata) {
        //记录入数据库
        KafkaRecord kafkaRecord = new KafkaRecord();
        kafkaRecord.setMessageId(Arrays.toString(producerRecord.headers().lastHeader("messageId").value()));
        kafkaRecord.setTopic(producerRecord.topic());
        kafkaRecord.setPartition(recordMetadata.partition());
        kafkaRecord.setOffset(recordMetadata.offset());
        kafkaRecord.setContent(producerRecord.value());
        kafkaRecord.setType("SEND");
        kafkaRecord.setStatus("SUCCESS");
        kafkaRecord.setCreateTime(new Date());
        kafkaRecordService.save(kafkaRecord);
    }

    @Override
    public void storeKafkaRecordOnFail(ProducerRecord<String, String> producerRecord, Exception exception) {
        //记录入数据库
        KafkaRecord kafkaRecord = new KafkaRecord();
        kafkaRecord.setMessageId(Arrays.toString(producerRecord.headers().lastHeader("messageId").value()));
        kafkaRecord.setTopic(producerRecord.topic());
        kafkaRecord.setPartition(-1);
        kafkaRecord.setOffset(-1L);
        kafkaRecord.setContent("消息发送失败：" + producerRecord.value() + ";" + "异常消息是：" + exception.getMessage());
        kafkaRecord.setType("SEND");
        kafkaRecord.setStatus("FAIL");
        kafkaRecord.setCreateTime(new Date());
        kafkaRecordService.save(kafkaRecord);
    }
}
