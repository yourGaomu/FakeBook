package com.zhangzc.bookmarketbiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zhangzc"})
public class BookMarketBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookMarketBizApplication.class, args);
    }

}
