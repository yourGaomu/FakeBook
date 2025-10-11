package com.zhangzc.booknotebiz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.zhangzc.booknotebiz.Mapper")
@EnableFeignClients(basePackages = {"com.zhangzc"})
@EnableCaching
@ComponentScan(basePackages = {"com.zhangzc"})
public class BookNoteBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookNoteBizApplication.class, args);
    }

}
