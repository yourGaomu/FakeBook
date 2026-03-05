package com.zhangzc.kafkaspringbootstart.core.mongo;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "kafka_record")
@Data
public class MogoKafkaRecord {

    @Id
    private String id; // MongoDB默认主键

    private String messageId;

    private String topic;

    private Integer partition;

    private Long offset;

    private String content;

    private String type; // SEND/CONSUME

    private String status; // SUCCESS/FAIL

    private Date createTime;

}
