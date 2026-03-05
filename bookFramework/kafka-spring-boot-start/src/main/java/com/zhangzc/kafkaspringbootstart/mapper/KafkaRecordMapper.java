package com.zhangzc.kafkaspringbootstart.mapper;

import com.zhangzc.kafkaspringbootstart.core.pojo.entity.KafkaRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 吃饭
* @description 针对表【kafka_record(Kafka发送/消费记录存储表)】的数据库操作Mapper
* @createDate 2026-01-08 15:42:47
* @Entity com.zhangzc.kafkaspringbootstart.pojo.entity.KafkaRecord
*/
public interface KafkaRecordMapper extends BaseMapper<KafkaRecord> {

}




