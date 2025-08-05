package com.zhangzc.bookauth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.zhangzc.bookauth.Mapper")
public class BookAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookAuthApplication.class, args);
    }

}
