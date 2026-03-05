package com.zhangzc.kafkaspringbootstart.service.impl;

import com.zhangzc.kafkaspringbootstart.core.mongo.MogoKafkaRecord;
import com.zhangzc.kafkaspringbootstart.service.StoreKafkaRecord;
import com.zhangzc.mongodbspringbootstart.utills.MongoUtil;
import com.zhangzc.bookcommon.Utils.TimeUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class KafkaConsumeRecordByMongo implements StoreKafkaRecord {
    private final MongoUtil mongoUtil;

    @Override
    public void storeKafkaRecordOnSuccess(ProducerRecord<String, String> producerRecord, RecordMetadata recordMetadata) {
        try {
            MogoKafkaRecord mongoKafkaRecord = new MogoKafkaRecord();
            Optional.ofNullable(producerRecord).ifPresent(record -> {
                Headers headers = record.headers();
                if (headers != null && headers.lastHeader("messageId") != null) {
                    mongoKafkaRecord.setMessageId(Arrays.toString(headers.lastHeader("messageId").value()));
                }
            });
            mongoKafkaRecord.setContent(producerRecord.value());
            mongoKafkaRecord.setOffset(recordMetadata.offset());
            mongoKafkaRecord.setPartition(recordMetadata.partition());
            mongoKafkaRecord.setTopic(producerRecord.topic());
            mongoKafkaRecord.setType("SEND");
            mongoKafkaRecord.setStatus("SUCCESS");
            mongoKafkaRecord.setCreateTime(TimeUtil.getDateTime(LocalDateTime.now()));
            mongoUtil.insert(mongoKafkaRecord, "kafka_record");
        } catch (Exception e) {
            log.error("存储Kafka发送记录到MongoDB失败", e);
            //数据入库有问题
        }
    }

    @Override
    public void storeKafkaRecordOnFail(ProducerRecord<String, String> producerRecord, Exception exception) {
        try {
            MogoKafkaRecord mongoKafkaRecord = new MogoKafkaRecord();
            Optional.ofNullable(producerRecord).ifPresent(record -> {
                Headers headers = record.headers();
                if (headers != null && headers.lastHeader("messageId") != null) {
                    mongoKafkaRecord.setMessageId(Arrays.toString(headers.lastHeader("messageId").value()));
                }
            });
            mongoKafkaRecord.setContent("消息发送失败：" + producerRecord.value() + ";" + "异常消息是：" + exception.getMessage());
            mongoKafkaRecord.setOffset(-1L);
            mongoKafkaRecord.setPartition(-1);
            mongoKafkaRecord.setTopic(producerRecord.topic());
            mongoKafkaRecord.setType("SEND");
            mongoKafkaRecord.setStatus("FAIL");
            mongoKafkaRecord.setCreateTime(TimeUtil.getDateTime(LocalDateTime.now()));
            mongoUtil.insert(mongoKafkaRecord, "kafka_record");
        } catch (Exception e) {
            //进行回滚操作
        }
    }
}


