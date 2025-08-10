package com.zhangzc.bookauth;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.zhangzc.bookauth.Mapper")
@EnableDiscoveryClient
@Slf4j
public class BookAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookAuthApplication.class, args);
    }

}
