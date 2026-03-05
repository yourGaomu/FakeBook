package com.zhangzc.kafkaspringbootstart.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka配置属性类
 * <p>配置前缀：zhang.kafka</p>
 * <p>用于从配置文件中读取Kafka相关配置信息</p>
 *
 * @author zhangzc
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "zhang.kafka")
public class KafkaProperty {

    /**
    * 选择策略,默认存入mysql
    * */
    private Boolean enable_mysql = false;
    private Boolean enable_mongo = false;

    /**
     * Kafka服务器地址列表，多个用逗号分隔
     */
    private String bootstrapServers = "";

    /**
     * 生产者配置
     */
    private Producer producer = new Producer();

    /**
     * 消费者配置
     */
    private Consumer consumer = new Consumer();

    /**
     * Kafka生产者配置内部类
     * <p>包含生产者相关的所有配置参数</p>
     */
    @Setter
    @Getter
    public static class Producer {
        /**
         * 消息确认机制：0-不等待确认，1-等待leader确认，all-等待所有副本确认
         */
        private String acks = "all";

        /**
         * 事物id前缀
         */
        private String transaction_id_prefix = "";

        /**
         * 发送失败重试次数
         */
        private Integer retries = 3;

        /**
         * 批量发送大小（字节）
         */
        private Integer batchSize = 16384;

        /**
         * 缓冲区大小（字节）
         */
        private Integer bufferMemory = 33554432;

        /**
         * key序列化器
         */
        private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";

        /**
         * value序列化器
         */
        private String valueSerializer = "org.apache.kafka.common.serialization.StringSerializer";

        /**
         * 压缩类型：none, gzip, snappy, lz4, zstd
         */
        private String compressionType = "none";

        /**
         * 批量发送延迟时间（毫秒）
         */
        private Integer lingerMs = 1;

    }

    /**
     * Kafka消费者配置内部类
     * <p>包含消费者相关的所有配置参数</p>
     */
    @Setter
    @Getter
    public static class Consumer {
        /**
         * 消费者组ID
         */
        private String groupId = "default-group";

        /**
         * 应答模式：manual-手动确认，batch-批量确认，record-逐条确认
        * */
        private String ack_mode = "manual";

        /**
         * 是否自动提交偏移量
         */
        private Boolean enableAutoCommit = false;

        /**
         * 自动提交间隔（毫秒）
         */
        private Integer autoCommitIntervalMs = 1000;

        /**
         * 会话超时时间（毫秒）
         */
        private Integer sessionTimeoutMs = 30000;

        /**
         * 单次拉取最大记录数
         */
        private Integer maxPollRecords = 500;

        /**
         * key反序列化器
         */
        private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";

        /**
         * value反序列化器
         */
        private String valueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";

        /**
         * 偏移量重置策略：earliest-从最早的消息开始，latest-从最新的消息开始
         */
        private String autoOffsetReset = "latest";

        /**
         * 心跳间隔（毫秒）
         */
        private Integer heartbeatIntervalMs = 3000;

    }

}
