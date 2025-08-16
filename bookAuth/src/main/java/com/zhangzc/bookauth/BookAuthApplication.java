package com.zhangzc.bookauth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.zhangzc.bookauth.Mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zhangzc"})
public class BookAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookAuthApplication.class, args);
    }

}
