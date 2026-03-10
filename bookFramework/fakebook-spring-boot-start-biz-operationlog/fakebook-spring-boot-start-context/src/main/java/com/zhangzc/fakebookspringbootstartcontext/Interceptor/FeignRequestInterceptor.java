package com.zhangzc.fakebookspringbootstartcontext.Interceptor;


import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId != null) {
            requestTemplate.header("userId", String.valueOf(userId));
            log.info("########## feign 请求设置请求头 userId: {}", userId);
        }
    }
}
