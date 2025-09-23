package com.zhangzc.booksearchbiz;

import org.dromara.easyes.spring.annotation.EsMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EsMapperScan("com.zhangzc.booksearchbiz.Mapper")
public class BookSearchBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookSearchBizApplication.class, args);
    }

}
