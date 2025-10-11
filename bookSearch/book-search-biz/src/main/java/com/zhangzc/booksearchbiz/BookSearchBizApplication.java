package com.zhangzc.booksearchbiz;

import org.dromara.easyes.spring.annotation.EsMapperScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EsMapperScan("com.zhangzc.booksearchbiz.Mapper.Es")
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("com.zhangzc.booksearchbiz.Mapper.Mp")
public class BookSearchBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookSearchBizApplication.class, args);
    }

}
