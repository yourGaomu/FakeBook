package com.zhangzc.blog.blogai;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;


@MapperScan("com.zhangzc.blog.blogai.Mapper")
@SpringBootApplication
@EnableAspectJAutoProxy // 开启 AOP 代理
@EnableDubbo
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zhangzc.booksearchapi.Api"})
public class BlogAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogAiApplication.class, args);
    }
}