package com.zhangzc.leaf.server.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zhangzc.leaf.server.exception.InitException;
import com.zhangzc.leaf.server.properties.LeafProperties;
import com.zhangzc.leaf.server.service.SegmentService;
import com.zhangzc.leaf.server.service.SnowflakeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@EnableConfigurationProperties(LeafProperties.class)
public class LeafAutoConfig {

    // ========== Segment 模式 ==========

    @Bean(name = "leafDataSource")
    @ConditionalOnProperty(prefix = "zhangzc.leaf", name = "segment-enable", havingValue = "true")
    @ConditionalOnMissingBean(SegmentService.class)
//    // 给自定义的 druidDataSource 添加 @Primary 注解，标记为优先选择的 Bean
//    @Primary
    public DataSource druidDataSource(LeafProperties leafProperties) throws SQLException {
        System.out.println("==> leaf 配置类初始化: " + leafProperties);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(leafProperties.getJdbcUrl());
        dataSource.setUsername(leafProperties.getJdbcUsername());
        dataSource.setPassword(leafProperties.getJdbcPassword());
        dataSource.init();
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(prefix = "zhangzc.leaf", name = "segment-enable", havingValue = "true")
    @ConditionalOnMissingBean(SegmentService.class)
    public SegmentService segmentService(@Qualifier("leafDataSource") DataSource dataSource, LeafProperties leafProperties)
            throws SQLException, InitException {
        System.out.println("==> leaf 配置类初始化: " + leafProperties);
        return new SegmentService((DruidDataSource) dataSource, leafProperties);
    }

    // ========== Snowflake 模式 ==========

    @Bean
    @ConditionalOnProperty(prefix = "zhangzc.leaf", name = "snowflake-enable", havingValue = "true") // 注意属性名匹配
    @ConditionalOnMissingBean(SnowflakeService.class)
    public SnowflakeService snowflakeService(LeafProperties leafProperties) throws InitException {
        System.out.println("==> leaf 配置类初始化: " + leafProperties);
        return new SnowflakeService(leafProperties);
    }
}
