package com.zhangzc.bookcommentbiz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@SpringBootApplication
@MapperScan("com.zhangzc.bookcommentbiz.Mapper")
@EnableDiscoveryClient
@EnableFeignClients("com.zhangzc")
public class BookCommentBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookCommentBizApplication.class, args);
    }

}
