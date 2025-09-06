package com.zhangzc.bookcountbiz;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhangzc.bookcountbiz.Mapper")
public class BookCountBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookCountBizApplication.class, args);
    }

}
