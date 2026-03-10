package com.zhangzc.smsspringbootstart.config;

import com.zhangzc.redisspringbootstart.utills.LuaUtil;
import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import com.zhangzc.smsspringbootstart.service.impl.AliSmsSender;
import com.zhangzc.redisspringbootstart.utills.LimiterUtil;
import com.zhangzc.smsspringbootstart.utills.SmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Slf4j
public class SmsConfig {

    @Bean

    public AliyunAccessKeyProperties aliyunAccessKeyProperties() {
        return new AliyunAccessKeyProperties();
    }

    @Bean
    public AliSmsSender aliSmsSender(com.aliyun.dypnsapi20170525.Client client, RedisUtil redisUtil, LuaUtil luaUtil) {
        return new AliSmsSender(client,redisUtil,luaUtil);
    }

    @Bean
    public com.aliyun.dypnsapi20170525.Client smsClient(AliyunAccessKeyProperties aliyunAccessKeyProperties) {
        try {
            com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client();

            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                    .setCredential(credential);

            // Endpoint 请参考 https://api.aliyun.com/product/Dypnsapi
            config.endpoint = "dypnsapi.aliyuncs.com";

            config.accessKeyId = aliyunAccessKeyProperties.getAccessKeyId(); // 必填
            config.accessKeySecret = aliyunAccessKeyProperties.getAccessKeySecret(); // 必填

            return new com.aliyun.dypnsapi20170525.Client(config);
        } catch (Exception e) {
            log.error("初始化阿里云短信发送客户端错误: ", e);
            return null;
        }
    }

    @Bean
    public SmsUtil smsUtil(AliSmsSender aliSmsSender) {
        return new SmsUtil(aliSmsSender);
    }



}
