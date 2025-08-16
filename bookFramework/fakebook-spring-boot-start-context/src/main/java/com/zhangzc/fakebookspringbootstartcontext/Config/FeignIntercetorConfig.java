package com.zhangzc.fakebookspringbootstartcontext.Config;

import com.zhangzc.fakebookspringbootstartcontext.Interceptor.FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignIntercetorConfig {

    @Bean
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

}
