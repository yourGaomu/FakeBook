package com.zhangzc.bookgateway.Filter;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import com.zhangzc.bookgateway.Constants.RedisKeyConstants;
import com.zhangzc.bookgateway.Utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(-90)
public class AddUserId2HeaderFilter implements GlobalFilter {

    private final RedisUtil redisUtil;

    /**
     * 请求头中，用户 ID 的键
     */
    private static final String HEADER_USER_ID = "userId";
    private final String TOKEN_HEADER_KEY = "Authorization";
    /**
     * Token 前缀
     */
    private static final String TOKEN_HEADER_VALUE_PREFIX = "Bearer ";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("==================> TokenConvertFilter");

        /*// 用户 ID
        Long userId = null;
        try {
            // 先 set 上下文，再调用 Sa-Token 同步 API，并在 finally 里清除上下文
            SaReactorSyncHolder.setContext(exchange);
            StpUtil.checkLogin();
            List<String> tokenList = exchange.getRequest().getHeaders().get("Authorization");
            if (CollUtil.isNotEmpty(tokenList)) {
                String token = tokenList.get(0);
                StpUtil.setTokenValue(token);
            }
            // 获取当前登录用户的 ID
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 若没有登录，则直接放行
            return chain.filter(exchange);
        } finally {
            SaReactorSyncHolder.clearContext();
        }*/

        // 从请求头中获取 Token 数据
        List<String> tokenList = exchange.getRequest().getHeaders().get(TOKEN_HEADER_KEY);

        if (CollUtil.isEmpty(tokenList)) {
            // 若请求头中未携带 Token，则直接放行
            return chain.filter(exchange);
        }

        // 获取 Token 值
        String tokenValue = tokenList.get(0);
        // 将 Token 前缀去除
        String token = tokenValue.replace(TOKEN_HEADER_VALUE_PREFIX, "");

        // 构建 Redis Key
        String tokenRedisKey = RedisKeyConstants.SA_TOKEN_TOKEN_KEY_PREFIX + token;
        // 查询 Redis, 获取用户 ID
        Integer userId = (Integer) redisUtil.get(tokenRedisKey);

        if (Objects.isNull(userId)) {
            // 若没有登录，则直接放行
            return chain.filter(exchange);
        }

        log.info("## 当前登录的用户 ID: {}", userId);

        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(HEADER_USER_ID, String.valueOf(userId))) // 将用户 ID 设置到请求头中
                .build();
        return chain.filter(newExchange);
    }
}