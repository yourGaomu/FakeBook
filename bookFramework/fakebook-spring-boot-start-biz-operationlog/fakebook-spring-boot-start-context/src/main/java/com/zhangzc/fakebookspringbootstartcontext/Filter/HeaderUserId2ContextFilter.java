package com.zhangzc.fakebookspringbootstartcontext.Filter;




import com.zhangzc.bookcommon.Const.GlobalConstants;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@Slf4j
public class HeaderUserId2ContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 从请求头中获取用户 ID
        String userId = request.getHeader(GlobalConstants.USER_ID);

        // 判断请求头中是否存在用户 ID
        if (StringUtils.isBlank(userId)) {
            // 若为空，则直接放行
            chain.doFilter(request, response);
            return;
        }

        // 将用户 ID 保存到上下文中
        LoginUserContextHolder.setUserId(userId);

        log.info("## HeaderUserId2ContextFilter, 用户 ID: {}", userId);

        // 将请求和响应传递给过滤链中的下一个过滤器。
        try {
            chain.doFilter(request, response);
        } finally {
            // 一定要删除 ThreadLocal ，防止内存泄露
            LoginUserContextHolder.remove();
            log.info("===== 删除 ThreadLocal， userId: {}", userId);
        }
    }
}

