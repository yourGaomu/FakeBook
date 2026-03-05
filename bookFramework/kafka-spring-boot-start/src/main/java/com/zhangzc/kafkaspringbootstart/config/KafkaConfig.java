package com.zhangzc.kafkaspringbootstart.config;


import com.zhangzc.kafkaspringbootstart.core.service.KafkaRecordService;
import com.zhangzc.kafkaspringbootstart.listener.ProduceListenerImpl;
import com.zhangzc.kafkaspringbootstart.service.StoreKafkaRecord;
import com.zhangzc.kafkaspringbootstart.service.impl.KafkaConsumeRecordByMongo;
import com.zhangzc.kafkaspringbootstart.service.impl.KafkaConsumeRecordByMysql;
import com.zhangzc.kafkaspringbootstart.utills.KafkaUtills;
import com.zhangzc.mongodbspringbootstart.utills.MongoUtil;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.transaction.support.TransactionTemplate;
import org.mybatis.spring.annotation.MapperScan;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Kafka配置类
 * <p>配置Kafka生产者和消费者的Bean</p>
 * <p>所有Bean仅在配置了zhang.kafka.bootstrap-servers时才会加载</p>
 *
 * @author zhangzc
 */
@Configuration
@EnableConfigurationProperties(KafkaProperty.class)
public class KafkaConfig {

    @Bean
    public MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory mongoDbFactory) {
        return new MongoTransactionManager(mongoDbFactory);
    }

    // 2. 可选：手动显式声明TransactionTemplate Bean（推荐，更直观）
    // 若不手动声明，Spring会通过事务管理器自动创建，此步为兜底/显式控制
    @Bean
    public TransactionTemplate transactionTemplate(MongoTransactionManager mongoTransactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        // 可自定义事务属性（如超时时间、隔离级别，按需配置）
        // transactionTemplate.setTimeout(3000); // 事务超时3秒
        transactionTemplate.setTransactionManager(mongoTransactionManager);
        return transactionTemplate;
    }

    /**
     * 生产者工厂配置
     * <p>创建Kafka生产者工厂，用于生成生产者实例</p>
     * <p>包含以下配置：</p>
     * <ul>
     *     <li>bootstrap.servers - Kafka服务器地址</li>
     *     <li>key.serializer - Key序列化器</li>
     *     <li>value.serializer - Value序列化器</li>
     *     <li>acks - 消息确认机制</li>
     *     <li>retries - 重试次数</li>
     *     <li>batch.size - 批量发送大小</li>
     *     <li>buffer.memory - 缓冲区大小</li>
     *     <li>compression.type - 压缩类型</li>
     *     <li>linger.ms - 批量发送延迟</li>
     * </ul>
     *
     * @param kafkaProperty Kafka配置属性
     * @return ProducerFactory实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhang.kafka", name = "bootstrap-servers")
    public ProducerFactory<String, String> producerFactory(KafkaProperty kafkaProperty) {

        System.out.println("开始初始化Kafka");
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperty.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProperty.getProducer().getKeySerializer());
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProperty.getProducer().getValueSerializer());
        configProps.put(ProducerConfig.ACKS_CONFIG, kafkaProperty.getProducer().getAcks());
        configProps.put(ProducerConfig.RETRIES_CONFIG, kafkaProperty.getProducer().getRetries());
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaProperty.getProducer().getBatchSize());
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, kafkaProperty.getProducer().getBufferMemory());
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProperty.getProducer().getCompressionType());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, kafkaProperty.getProducer().getLingerMs());
        /**
         * 有事物配置
         * */
        if (!Objects.equals(kafkaProperty.getProducer().getTransaction_id_prefix(), "")) {
            configProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, kafkaProperty.getProducer().getTransaction_id_prefix() + "-" + System.currentTimeMillis());
        }
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * 反向实列化数据存储对象
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhang.kafka", name = "enable_mongo", havingValue = "true")
    public StoreKafkaRecord mongoStoreKafkaRecord(MongoUtil mongoUtil, TransactionTemplate transactionTemplate) {
        return new KafkaConsumeRecordByMongo(mongoUtil);
    }

    @Bean
    @ConditionalOnProperty(prefix = "zhang.kafka", name = "enable_mysql", havingValue = "true")
    public StoreKafkaRecord mysqlStoreKafkaRecord(KafkaRecordService kafkaRecordService) {
        return new KafkaConsumeRecordByMysql(kafkaRecordService);
    }

    @ConditionalOnMissingBean(StoreKafkaRecord.class)
    public void throwS(){
        throw new RuntimeException("你没有选择一个策略去保存数据");
    }

    /**
     * KafkaTemplate配置
     * <p>用于发送消息到Kafka</p>
     * <p>使用示例：</p>
     * <pre>
     * {@code
     * @Autowired
     * private KafkaTemplate<String, String> kafkaTemplate;
     *
     * public void sendMessage(String topic, String message) {
     *     kafkaTemplate.send(topic, message);
     * }
     * }
     * </pre>
     *
     * @param producerFactory 生产者工厂
     * @param storeKafkaRecord 记录存储策略
     * @return KafkaTemplate实例
     */



    @Bean
    @ConditionalOnProperty(prefix = "zhang.kafka", name = "bootstrap-servers")
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory, StoreKafkaRecord storeKafkaRecord) {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        // 设置生产者监听器，用于监听消息发送成功和失败事件
        kafkaTemplate.setProducerListener(new ProduceListenerImpl(storeKafkaRecord));
        return kafkaTemplate;
    }

    /**
     * 消费者工厂配置
     * <p>创建Kafka消费者工厂，用于生成消费者实例</p>
     * <p>包含以下配置：</p>
     * <ul>
     *     <li>bootstrap.servers - Kafka服务器地址</li>
     *     <li>group.id - 消费者组ID</li>
     *     <li>key.deserializer - Key反序列化器</li>
     *     <li>value.deserializer - Value反序列化器</li>
     *     <li>enable.auto.commit - 自动提交开关</li>
     *     <li>auto.commit.interval.ms - 自动提交间隔</li>
     *     <li>session.timeout.ms - 会话超时时间</li>
     *     <li>max.poll.records - 单次拉取最大记录数</li>
     *     <li>auto.offset.reset - 偏移量重置策略</li>
     *     <li>heartbeat.interval.ms - 心跳间隔</li>
     * </ul>
     *
     * @param kafkaProperty Kafka配置属性
     * @return ConsumerFactory实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhang.kafka", name = "bootstrap-servers")
    public ConsumerFactory<String, String> consumerFactory(KafkaProperty kafkaProperty) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperty.getBootstrapServers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperty.getConsumer().getGroupId());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaProperty.getConsumer().getKeyDeserializer());
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaProperty.getConsumer().getValueDeserializer());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperty.getConsumer().getEnableAutoCommit());
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, kafkaProperty.getConsumer().getAutoCommitIntervalMs());
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaProperty.getConsumer().getSessionTimeoutMs());
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperty.getConsumer().getMaxPollRecords());
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperty.getConsumer().getAutoOffsetReset());
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaProperty.getConsumer().getHeartbeatIntervalMs());
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * 消费者监听器容器工厂配置
     * <p>用于创建@KafkaListener注解的消息监听器</p>
     * <p>配置说明：</p>
     * <ul>
     *     <li>并发数：3，表示同时启动3个消费者线程</li>
     *     <li>批量消费：false，表示单条消费模式</li>
     *     <li>应答模式：MANUAL，手动应答模式</li>
     * </ul>
     * <p>使用示例：</p>
     * <pre>
     * {@code
     * @KafkaListener(topics = "my-topic", groupId = "my-group")
     * public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
     *     System.out.println("Received: " + record.value());
     *     // 手动确认消费
     *     ack.acknowledge();
     * }
     * }
     * </pre>
     *
     * @param consumerFactory 消费者工厂
     * @return ConcurrentKafkaListenerContainerFactory实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhang.kafka", name = "bootstrap-servers")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaProperty kafkaProperty) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        // 设置并发数
        factory.setConcurrency(3);
        // 设置批量消费
        factory.setBatchListener(false);
        // 设置手动应答模式
        if (kafkaProperty.getConsumer().getAck_mode().equals("manual")) {
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        }
        return factory;
    }

    @Bean
    @ConditionalOnProperty(prefix = "zhang.kafka", name = "bootstrap-servers")
    public KafkaUtills kafkaUtills(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaUtills(kafkaTemplate);
    }

    @Bean
    @ConditionalOnClass(name = "javax.sql.DataSource")
    @ConditionalOnBean(type = "javax.sql.DataSource")
    public KafkaRecordMybatisAutoConfig kafkaRecordMybatisAutoConfig() {
        return new KafkaRecordMybatisAutoConfig();
    }

    @Configuration
    @MapperScan("com.zhangzc.kafkaspringbootstart.mapper")
    @ComponentScan(basePackages = "com.zhangzc.kafkaspringbootstart.core.service.impl")
    static class KafkaRecordMybatisAutoConfig {
    }
}

