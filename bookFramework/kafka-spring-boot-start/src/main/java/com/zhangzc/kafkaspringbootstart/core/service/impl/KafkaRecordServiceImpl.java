package com.zhangzc.kafkaspringbootstart.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.kafkaspringbootstart.core.pojo.entity.KafkaRecord;
import com.zhangzc.kafkaspringbootstart.core.service.KafkaRecordService;
import com.zhangzc.kafkaspringbootstart.mapper.KafkaRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author 吃饭
* @description 针对表【kafka_record(Kafka发送/消费记录存储表)】的数据库操作Service实现
* @createDate 2026-01-08 15:42:47
*/
@Service
public class KafkaRecordServiceImpl extends ServiceImpl<KafkaRecordMapper, KafkaRecord>
    implements KafkaRecordService{

}




