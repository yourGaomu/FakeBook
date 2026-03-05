package com.zhangzc.kafkaspringbootstart.annotation;

import java.lang.annotation.*;

/**
 * Kafka消费自动偏移量管理注解
 * <p>使用Redis存储和检查消费偏移量，防止重复消费</p>
 * 
 * <p>工作原理：</p>
 * <ul>
 *     <li>消费前检查：对比Kafka当前offset与Redis存储的offset</li>
 *     <li>重复检测：如果当前offset <= Redis offset，判定为重复消费</li>
 *     <li>自动更新：消费成功后，自动更新Redis中的offset</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @Component
 * @Slf4j
 * public class MyConsumer {
 *     
 *     @KafkaListener(topics = "test-topic")
 *     @AutoInserByRedis
 *     public void onMessage(ConsumerRecord<String, Object> record, Acknowledgment ack) {
 *         // 业务逻辑
 *     }
 * }
 * }
 * </pre>
 *
 * @author zhangzc
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoInserByRedis {
    
    /**
     * 重复消费处理策略
     * @return 处理策略
     */
    DuplicateStrategy strategy() default DuplicateStrategy.SKIP;
    
    /**
     * 是否启用告警日志
     * @return true-启用告警，false-仅记录info日志
     */
    boolean enableAlert() default true;
    
    /**
     * Redis key前缀，默认为 kafka:offset
     * @return Redis key前缀
     */
    String redisKeyPrefix() default "kafka:offset";
    
    /**
     * 重复消费处理策略枚举
     */
    enum DuplicateStrategy {
        /**
         * 跳过重复消息（直接ack，不执行业务逻辑）
         */
        SKIP,
        
        /**
         * 幂等处理（执行业务逻辑，但记录为重复消费）
         */
        IDEMPOTENT
    }
}
