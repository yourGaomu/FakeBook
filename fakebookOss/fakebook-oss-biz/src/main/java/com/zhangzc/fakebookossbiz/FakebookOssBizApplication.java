package com.zhangzc.fakebookossbiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FakebookOssBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(FakebookOssBizApplication.class, args);
    }

}
