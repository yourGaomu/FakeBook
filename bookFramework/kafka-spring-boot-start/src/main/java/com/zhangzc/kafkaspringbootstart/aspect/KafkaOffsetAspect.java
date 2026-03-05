package com.zhangzc.kafkaspringbootstart.aspect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.kafkaspringbootstart.annotation.AutoInserByRedis;
import com.zhangzc.redisspringbootstart.utills.LuaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Kafka消费偏移量管理切面
 * <p>拦截标注了@AutoInserByRedis的Kafka监听器方法，自动管理消费偏移量</p>
 * 
 * <p>处理流程：</p>
 * <ol>
 *     <li>提取Kafka消息的topic、partition、offset信息</li>
 *     <li>调用Lua脚本检查Redis中的偏移量</li>
 *     <li>根据检查结果决定是否执行业务逻辑</li>
 *     <li>重复消费按策略处理（跳过或幂等）</li>
 * </ol>
 *
 * @author zhangzc
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOffsetAspect {

    private final LuaUtil luaUtil;

    /**
     * 环绕通知：拦截@AutoInserByRedis注解的方法
     */
    @Around("@annotation(autoInserByRedis)")
    public Object around(ProceedingJoinPoint joinPoint, AutoInserByRedis autoInserByRedis) throws Throwable {
        // 1. 获取方法参数
        Object[] args = joinPoint.getArgs();
        ConsumerRecord<?, ?> record = null;
        Acknowledgment ack = null;
        // 2. 查找ConsumerRecord和Acknowledgment参数
        for (Object arg : args) {
            if (arg instanceof ConsumerRecord) {
                record = (ConsumerRecord<?, ?>) arg;
            } else if (arg instanceof Acknowledgment) {
                ack = (Acknowledgment) arg;
            }
        }
        // 3. 参数校验
        if (record == null) {
            log.error("❌ @AutoInserByRedis注解的方法必须包含ConsumerRecord参数");
            return null;
        }
        if (ack == null) {
            log.warn("⚠️ 未找到Acknowledgment参数，无法自动ack");
        }
        // 4. 提取Kafka消息信息
        String topic = record.topic();
        int partition = record.partition();
        long offset = record.offset();
        
        // 5. 构造Redis Key
        String redisKey = autoInserByRedis.redisKeyPrefix() + ":" + topic;
        String partitionField = "partition-" + partition;
        
        log.info("📥 接收Kafka消息 | Topic: {} | Partition: {} | Offset: {} | RedisKey: {}", 
                topic, partition, offset, redisKey);
        try {
            // 6. 调用Lua脚本检查偏移量
            Object result = luaUtil.execute(
                    "check_and_update_offset",
                    redisKey,
                    Arrays.asList(partitionField, offset)
            );
            // 7. 解析Lua脚本返回结果
            OffsetCheckResult checkResult = parseResult(result);
            
            log.info("🔍 偏移量检查结果 | Status: {} | Message: {}", 
                    checkResult.status, checkResult.message);
            
            // 8. 根据检查结果处理
            switch (checkResult.status) {
                case 1: // first 或 allow - 允许消费
                    log.info("✅ 允许消费 | Type: {}", checkResult.message);
                    return joinPoint.proceed();
                    
                case 0: // duplicate - 重复消费
                    return handleDuplicate(autoInserByRedis, ack, topic, partition, offset);
                    
                case -1: // rollback 或 invalid - 异常情况
                    return handleError(autoInserByRedis, ack, topic, partition, offset, checkResult.message);
                    
                default:
                    log.error("❌ 未知的检查结果状态: {}", checkResult.status);
                    return null;
            }
            
        } catch (Exception e) {
            log.error("发生错误，原因如下：{}", e.getMessage());

            log.error("❌ 偏移量检查失败 | Topic: {} | Partition: {} | Offset: {} | Error: {}",
                    topic, partition, offset, e.getMessage(), e);
            
            // 异常情况：根据配置决定是否继续消费
            if (autoInserByRedis.strategy() == AutoInserByRedis.DuplicateStrategy.IDEMPOTENT) {
                log.warn("⚠️ 降级为幂等处理，继续消费");
                return joinPoint.proceed();
            } else {
                log.warn("⚠️ 跳过异常消息");
                if (ack != null) {
                    ack.acknowledge();
                }
                return null;
            }
        }
    }
    
    /**
     * 处理重复消费
     */
    private Object handleDuplicate(AutoInserByRedis annotation, Acknowledgment ack, 
                                   String topic, int partition, long offset) {
        
        if (annotation.enableAlert()) {
            log.warn("⚠️ 检测到重复消费 | Topic: {} | Partition: {} | Offset: {} | Strategy: {}", 
                    topic, partition, offset, annotation.strategy());
        } else {
            log.info("ℹ️ 重复消费（已跳过） | Topic: {} | Partition: {} | Offset: {}", 
                    topic, partition, offset);
        }
        
        // 根据策略处理
        if (annotation.strategy() == AutoInserByRedis.DuplicateStrategy.SKIP) {
            // 跳过：直接ack
            if (ack != null) {
                ack.acknowledge();
                log.info("✅ 重复消息已ack（跳过处理）");
            }
            return null;
        } else {
            // 幂等处理：记录日志但不执行业务（因为Lua脚本未更新offset）
            log.warn("⚠️ 幂等模式下重复消息被Redis拒绝，建议业务层自行去重");
            if (ack != null) {
                ack.acknowledge();
            }
            return null;
        }
    }
    
    /**
     * 处理异常情况（offset回滚或无效）
     */
    private Object handleError(AutoInserByRedis annotation, Acknowledgment ack,
                              String topic, int partition, long offset, String errorType) {
        
        if (annotation.enableAlert()) {
            log.error("🚨 检测到异常消费 | Type: {} | Topic: {} | Partition: {} | Offset: {}", 
                    errorType, topic, partition, offset);
            log.error("🚨 可能原因：1. Redis数据被手动修改  2. Kafka回溯  3. 分区Rebalance异常");
        }
        
        // 异常情况：跳过并ack
        if (ack != null) {
            ack.acknowledge();
            log.info("✅ 异常消息已ack（跳过处理）");
        }
        
        return null;
    }
    
    /**
     * 解析Lua脚本返回结果
     * <p>Lua返回格式: {1, "allow"} 或 {-1, "invalid_offset"}</p>
     * <p>Spring解析后为: List [status, message]</p>
     */
    private OffsetCheckResult parseResult(Object result) {
        if (result == null) {
            return new OffsetCheckResult(-1, "null_result");
        }
        try {
            List<String> strings = JsonUtils.parseList(JsonUtils.toJsonString(result), new TypeReference<List<String>>() {
            });
            OffsetCheckResult offsetCheckResult = new OffsetCheckResult(Integer.parseInt(strings.get(0)),strings.get(1));
            return offsetCheckResult;
        } catch (Exception e) {
            log.error("❌ 解析Lua返回值失败 | 返回值: {} | 类型: {} | 错误: {}", 
                    result, result.getClass().getName(), e.getMessage(), e);
            return new OffsetCheckResult(-1, "parse_error");
        }
    }
    
    /**
     * 偏移量检查结果
     */
    private static class OffsetCheckResult {
        int status;      // 1-允许, 0-重复, -1-错误
        String message;  // first/allow/duplicate/rollback/invalid_offset
        
        OffsetCheckResult(int status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
