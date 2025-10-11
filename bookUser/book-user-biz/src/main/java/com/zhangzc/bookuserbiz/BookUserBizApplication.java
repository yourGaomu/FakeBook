package com.zhangzc.bookuserbiz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zhangzc"})
@MapperScan("com.zhangzc.bookuserbiz.Mapper")
public class BookUserBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookUserBizApplication.class, args);
    }

}
