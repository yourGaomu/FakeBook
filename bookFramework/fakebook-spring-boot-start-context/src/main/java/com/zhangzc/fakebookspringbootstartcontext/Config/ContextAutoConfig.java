package com.zhangzc.fakebookspringbootstartcontext.Config;

import com.zhangzc.fakebookspringbootstartcontext.Filter.HeaderUserId2ContextFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContextAutoConfig {

    @Bean
    public FilterRegistrationBean<HeaderUserId2ContextFilter> filterFilterRegistrationBean() {
        HeaderUserId2ContextFilter filter = new HeaderUserId2ContextFilter();

        return new FilterRegistrationBean<>(filter);
    }

}
