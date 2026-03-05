package com.zhangzc.kafkaspringbootstart.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public interface StoreKafkaRecord {
    void storeKafkaRecordOnSuccess(ProducerRecord<String, String> producerRecord, RecordMetadata recordMetadata);
    void storeKafkaRecordOnFail(ProducerRecord<String, String> producerRecord, Exception exception);
}
