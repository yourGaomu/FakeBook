package com.zhangzc.fakebookspringbootstartcontext.Config;

import com.zhangzc.fakebookspringbootstartcontext.Interceptor.FeignRequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;


@AutoConfiguration
public class FeignIntercetorConfig {

    @Bean
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

}
