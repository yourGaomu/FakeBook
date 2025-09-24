package com.zhangzc.bookrelationbiz;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.zhangzc.bookrelationbiz.Mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zhangzc"})
public class BookRelationBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookRelationBizApplication.class, args);
    }

}