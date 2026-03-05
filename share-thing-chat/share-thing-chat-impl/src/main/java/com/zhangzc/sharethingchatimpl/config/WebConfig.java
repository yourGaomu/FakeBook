package com.zhangzc.sharethingchatimpl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 用于配置跨域等信息
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有路径的跨域请求
        registry.addMapping("/**")
                // 允许所有来源（使用 allowedOriginPatterns 代替 allowedOrigins 以支持 allowCredentials）
                .allowedOriginPatterns("*")
                // 允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许携带凭证（Cookies等）
                .allowCredentials(true)
                // 允许的请求头
                .allowedHeaders("*")
                // 预检请求的缓存时间（秒）
                .maxAge(3600);
    }
}
