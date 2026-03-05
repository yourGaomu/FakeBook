package com.zhangzc.mongodbspringbootstart.config;

import com.zhangzc.mongodbspringbootstart.utills.MongoUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * MongoDB 配置类
 * 功能：配置 MongoTemplate 核心组件、自定义工具类 MongoUtil，优化 MongoDB 序列化行为
 */
@Configuration
public class MongodbConfig { // 类名遵循 PascalCase 规范，首字母大写

    /**
     * 配置 MongoTemplate（MongoDB 核心操作模板）
     * 优化点：移除默认的 _class 字段（避免存储冗余的类名信息）
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory, MongoConverter mongoConverter) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDatabaseFactory, mongoConverter);
        // 可选：设置默认读写超时时间（根据业务需求调整，默认无超时）
        // mongoTemplate.setReadTimeout(Duration.ofSeconds(5));
        // mongoTemplate.setWriteTimeout(Duration.ofSeconds(5));
        return mongoTemplate;
    }

    /**
     * 自定义 MongoConverter，移除 _class 字段
     * Spring Data MongoDB 默认会在文档中添加 _class 字段存储实体类全路径，多数场景下无需保留
     */
    @Bean
    public MongoConverter mongoConverter(MongoDatabaseFactory factory, MongoMappingContext context) {
        MappingMongoConverter converter = new MappingMongoConverter(factory, context);
        // 禁用 _class 字段的自动添加
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }

    /**
     * 配置自定义 MongoDB 工具类（注入 MongoTemplate 供业务层复用）
     * 依赖：MongoTemplate 由 Spring 自动注入（上方已配置）
     */
    @Bean
    public MongoUtil mongoUtil(MongoTemplate mongoTemplate) {
        return new MongoUtil(mongoTemplate);
    }

//    /**
//     * 配置 MongoDB 事务管理器
//     * 开启事务支持（注意：MongoDB 4.0+ 且需配置为副本集模式才支持事务）
//     */
//    @Bean
//    public MongoTransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
//        return new MongoTransactionManager(mongoDatabaseFactory);
//    }
//
//    @Bean
//    public TransactionTemplate transactionTemplate(MongoTransactionManager transactionManager) {
//        return new TransactionTemplate(transactionManager);
//    }
}