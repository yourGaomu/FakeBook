package com.zhangzc.sharethingchatimpl;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication()
@Slf4j
@EnableDubbo
@EnableTransactionManagement
public class ShareThingChatImplApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ShareThingChatImplApplication.class, args);
    }

    @Override
    //回调函数
    public void run(String... args) throws Exception {
        log.info("开始初始化这个netty");
    }

}
