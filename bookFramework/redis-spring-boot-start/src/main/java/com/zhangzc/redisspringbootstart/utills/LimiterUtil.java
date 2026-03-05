package com.zhangzc.redisspringbootstart.utills;


import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;
import java.util.UUID;

public class LimiterUtil {

    private RedisTemplate<String, Object> redisTemplate;

    public LimiterUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 孤单窗口限流算法
     *
     * @return true 限流  false 放行
     */
    public boolean fixedWindow(String key, int count) {
        long countCache = redisTemplate.opsForValue().increment(key);
        return countCache > count;
    }

    /**
     * intervalTime 时间内 最多只能访问5次
     * @param key          缓存key
     * @param currentTime  当前时间  new Date().getTime();
     * @param intervalTime 有效期
     * @param count        限流次数
     * @return true 限流 false 放行
     */
    public boolean slidingWindow(String key, Long currentTime, Long intervalTime, int count) {
        //发送次数+1
        redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), currentTime);
        // intervalTime是限流的时间
        int countCache = Objects.requireNonNull(redisTemplate.opsForZSet().rangeByScore(key, currentTime - intervalTime, currentTime)).size();
        return countCache > count;
    }
}