package com.zhangzc.fakebookspringbootstartbizoperationlog.Config;


import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AOP.ApiOperationLogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ApiOperationLogAutoConfiguration {

    @Bean
    public ApiOperationLogAspect apiOperationLogAspect() {
        return new ApiOperationLogAspect();
    }
}
