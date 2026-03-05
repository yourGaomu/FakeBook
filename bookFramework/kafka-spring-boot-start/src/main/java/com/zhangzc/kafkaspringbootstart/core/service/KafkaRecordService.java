package com.zhangzc.kafkaspringbootstart.core.service;

import com.zhangzc.kafkaspringbootstart.core.pojo.entity.KafkaRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 吃饭
* @description 针对表【kafka_record(Kafka发送/消费记录存储表)】的数据库操作Service
* @createDate 2026-01-08 15:42:47
*/
public interface KafkaRecordService extends IService<KafkaRecord> {

}
