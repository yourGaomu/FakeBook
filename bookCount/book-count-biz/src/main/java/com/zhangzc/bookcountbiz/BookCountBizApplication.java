package com.zhangzc.bookcountbiz;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.zhangzc.bookcountbiz.Mapper")
@EnableDiscoveryClient
@EnableFeignClients("com.zhangzc")
public class BookCountBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookCountBizApplication.class, args);
    }

}
