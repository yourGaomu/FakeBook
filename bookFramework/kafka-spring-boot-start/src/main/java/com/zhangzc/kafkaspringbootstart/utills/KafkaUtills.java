package com.zhangzc.kafkaspringbootstart.utills;


import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka工具类
 * <p>提供常用的Kafka消息发送功能</p>
 * <p>包含普通消息、事务消息、分区消息的发送</p>
 *
 * @author zhangzc
 */
@RequiredArgsConstructor
@Slf4j
public class KafkaUtills {
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送普通消息（异步）
     * <p>消息会被自动序列化为JSON格式</p>
     *
     * @param topic   主题名称
     * @param message 消息对象
     * @return 是否成功提交发送请求（注意：不代表消息已成功到达Kafka）
     */
    public boolean sendMessage(String topic, Object message) {
        try {
            kafkaTemplate.send(topic, Object2String(message));
            return true;
        } catch (Exception e) {
            log.error("发送消息失败 | Topic: {} | Message: {}", topic, message, e);
            return false;
        }
    }


    /**
    * 发送带有key的消息
    * */
    public boolean sendSimpleMapMessage(String topic, Map<String, Object> message) {
        return sendTransactionMessage(topic, message);
    }


    /**
     * 发送普通消息（异步）- 带Key
     * <p>相同Key的消息会被发送到同一个分区</p>
     *
     * @param topic   主题名称
     * @param key     消息key（用于分区策略）
     * @param message 消息对象
     * @return 是否成功提交发送请求
     */
    public boolean sendMessage(String topic, String key, Object message) {
        try {
            kafkaTemplate.send(topic, key, Object2String(message));
            return true;
        } catch (Exception e) {
            log.error("发送消息失败 | Topic: {} | Key: {} | Message: {}", topic, key, message, e);
            return false;
        }
    }

    /**
     * 发送消息（同步）
     * <p>等待消息发送结果，适用于对可靠性要求高的场景</p>
     *
     * @param topic   主题名称
     * @param message 消息对象
     * @return 是否发送成功
     */
    public boolean sendMessageSync(String topic, Object message) {
        return sendMessageSync(topic, message, 5000L);
    }

    /**
     * 发送消息（同步）- 自定义超时时间
     *
     * @param topic     主题名称
     * @param message   消息对象
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否发送成功
     */
    public boolean sendMessageSync(String topic, Object message, long timeoutMs) {
        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, Object2String(message));
            SendResult<String, String> result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            log.info("同步发送成功 | Topic: {} | Partition: {} | Offset: {}",
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("发送消息被中断 | Topic: {} | Message: {}", topic, message, e);
            return false;
        } catch (ExecutionException | TimeoutException e) {
            log.error("同步发送失败 | Topic: {} | Message: {}", topic, message, e);
            return false;
        }
    }

    /**
     * 发送事务消息
     * <p>在事务中发送消息，如果执行过程中抛出异常，消息不会被发送</p>
     * <p>注意：需要在配置中启用Kafka事务支持</p>
     *
     * @param topic   主题名称
     * @param message 消息对象
     * @return 是否发送成功
     */
    public boolean sendTransactionMessage(String topic, Object message) {
        try {
            Boolean result = kafkaTemplate.executeInTransaction(new KafkaOperations.OperationsCallback<String, String, Boolean>() {
                /**
                 * 声明事务：如果此方法抛出异常，消息不会发出去
                 */
                @Override
                public Boolean doInOperations(KafkaOperations<String, String> operations) {
                    try {
                        operations.send(topic, Object2String(message));
                        log.info("事务消息发送成功 | Topic: {}", topic);
                        return true;
                    } catch (Exception e) {
                        log.error("事务消息发送失败 | Topic: {}", topic, e);
                        throw new RuntimeException("发送消息失败，事务将回滚", e);
                    }
                }
            });
            return result != null && result;
        } catch (Exception e) {
            log.error("事务执行失败 | Topic: {} | Message: {}", topic, message, e);
            return false;
        }
    }

    public boolean sendTransactionMessage(String topic, Map<String, Object> message) {
        try {
            message.forEach((k, v) -> {
                kafkaTemplate.executeInTransaction(new KafkaOperations.OperationsCallback<String, String, Boolean>() {
                    @Override
                    public Boolean doInOperations(KafkaOperations<String, String> kafkaOperations) {
                        try {
                            kafkaOperations.send(topic, k, Object2String(v));
                            return true;
                        } catch (Exception e) {
                            throw new RuntimeException("发送消息失败，事务将回滚", e);
                        }
                    }
                });
            });
            return true;
        } catch (Exception e) {
            log.error("事务执行失败 | Topic: {} | Message: {}", topic, message, e);
            return false;
        }
    }

    /**
     * 发送指定分区的消息
     * <p>将消息发送到指定的分区</p>
     *
     * @param topic     主题名称
     * @param partition 分区编号（从0开始）
     * @param message   消息对象
     * @return 是否成功提交发送请求
     */
    public boolean sendPartitionMessage(String topic, int partition, Object message) {
        return sendPartitionMessage(topic, partition, null, message);
    }

    /**
     * 发送指定分区的消息 - 带Key
     *
     * @param topic     主题名称
     * @param partition 分区编号（从0开始）
     * @param key       消息key
     * @param message   消息对象
     * @return 是否成功提交发送请求
     */
    public boolean sendPartitionMessage(String topic, int partition, String key, Object message) {
        try {
            kafkaTemplate.send(topic, partition, key, Object2String(message));
            log.info("发送分区消息 | Topic: {} | Partition: {} | Key: {}", topic, partition, key);
            return true;
        } catch (Exception e) {
            log.error("发送分区消息失败 | Topic: {} | Partition: {} | Message: {}", topic, partition, message, e);
            return false;
        }
    }

    /**
     * 批量发送消息（异步）
     * <p>适用于需要批量发送大量消息的场景</p>
     *
     * @param topic    主题名称
     * @param messages 消息列表
     * @return 成功提交的消息数量
     */
    public int sendBatchMessages(String topic, java.util.List<?> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (Object message : messages) {
            if (sendMessage(topic, message)) {
                successCount++;
            }
        }
        log.info("批量发送完成 | Topic: {} | 总数: {} | 成功: {}", topic, messages.size(), successCount);
        return successCount;
    }

    /**
     * 对象转JSON字符串
     *
     * @param object 对象
     * @return JSON字符串
     */
    private String Object2String(Object object) {
        return JsonUtils.toJsonString(object);
    }
}
