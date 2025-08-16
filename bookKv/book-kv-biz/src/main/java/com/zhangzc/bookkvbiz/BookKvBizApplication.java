package com.zhangzc.bookkvbiz;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScans(value = {
        @MapperScan("com.zhangzc.bookkvbiz.Mapper")
})

public class BookKvBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookKvBizApplication.class, args);
    }

}
