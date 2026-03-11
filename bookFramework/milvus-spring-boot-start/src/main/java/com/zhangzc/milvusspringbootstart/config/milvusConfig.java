package com.zhangzc.milvusspringbootstart.config;


import com.zhangzc.milvusspringbootstart.config.property.MilvusProperty;
import org.dromara.milvus.plus.config.MilvusPropertiesConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(MilvusProperty.class)
public class milvusConfig {
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "zhang.milvus", name = "enable", havingValue = "true")
    public MilvusPropertiesConfiguration milvusPropertiesConfiguration(MilvusProperty milvusProperty) {

        MilvusPropertiesConfiguration milvusPropertiesConfiguration = new MilvusPropertiesConfiguration();

        milvusPropertiesConfiguration.setBanner(false);
        //是否开启
        milvusPropertiesConfiguration.setEnable(milvusProperty.getEnable());
        //数据库的名称
        milvusPropertiesConfiguration.setDbName(milvusProperty.getDbName());
        //实体类所在的包
        milvusPropertiesConfiguration.setPackages(milvusProperty.getPackages());
        //日志等级
        //是否开启日志
        milvusPropertiesConfiguration.setOpenLog(milvusProperty.getOpenLog() != null && milvusProperty.getOpenLog());
        if (milvusProperty.getOpenLog()) {
            milvusPropertiesConfiguration.setLogLevel(milvusProperty.getLogLevel());
        }
        //数据库密码
        milvusPropertiesConfiguration.setPassword(milvusProperty.getPassword());
        //用户名
        milvusPropertiesConfiguration.setUsername(milvusProperty.getUsername());
        //token
        //milvusPropertiesConfiguration.setToken(milvusProperty.getToken());
        //
        milvusPropertiesConfiguration.setUri(milvusProperty.getUri());

        return milvusPropertiesConfiguration;

    }


}
