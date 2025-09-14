package com.zhangzc.booknotebiz.Utils;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisHashExample {

    // 注入 RedisTemplate（Spring 自动配置）

    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 向 Hash 中添加一个键值对
     * @param hashKey Hash 表的 key（相当于一个集合的名称）
     * @param key 字段 key
     * @param value 字段值
     */
    public void put(String hashKey, String key, Object value) {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        hashOps.put(hashKey, key, value);
    }

    /**
     * 向 Hash 中批量添加键值对
     * @param hashKey Hash 表的 key
     * @param map 包含多个键值对的 Map
     */
    public void putAll(String hashKey, Map<String, Object> map) {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        hashOps.putAll(hashKey, map);
    }

    /**
     * 向 Hash 中批量添加键值对：
     * 1. 若 Hash 不存在，则创建并设置过期时间
     * 2. 若 Hash 已存在，则直接添加（覆盖已有字段）
     * @param hashKey Hash 表的 key
     * @param map 批量键值对
     * @param expireSeconds 过期时间（秒）
     */
    public void putAllWithExpire(String hashKey, Map<String, Object> map, long expireSeconds) {
        // 1. 先判断 Hash 是否存在（核心修正：使用 redisTemplate.hasKey 判断整个 key）
        boolean hashExists = redisTemplate.hasKey(hashKey);

        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

        // 2. 无论是否存在，先执行批量添加（不存在则创建 Hash，存在则追加/覆盖）
        hashOps.putAll(hashKey, map);

        // 3. 仅当 Hash 不存在时，设置过期时间（避免重复设置覆盖原有过期时间）
        if (!hashExists) {
            redisTemplate.expire(hashKey, expireSeconds, TimeUnit.SECONDS);
        }
    }




    /**
     * 获取 Hash 中某个字段的值
     * @param hashKey Hash 表的 key
     * @param key 字段 key
     * @return 字段值
     */
    public Object get(String hashKey, String key) {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        return hashOps.get(hashKey, key);
    }

    /**
     * 获取 Hash 中所有键值对
     * @param hashKey Hash 表的 key
     * @return 包含所有字段和值的 Map
     */
    public Map<String, Object> getAll(String hashKey) {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        return hashOps.entries(hashKey);
    }

    /**
     * 获取 Hash 中所有字段名
     * @param hashKey Hash 表的 key
     * @return 字段名集合
     */
    public Set<String> getKeys(String hashKey) {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        return hashOps.keys(hashKey);
    }

    /**
     * 删除 Hash 中的某个字段
     * @param hashKey Hash 表的 key
     * @param keys 要删除的字段（可传多个）
     * @return 成功删除的数量
     */
    public Long delete(String hashKey, String... keys) {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        return hashOps.delete(hashKey, keys);
    }

    /**
     * 判断 Hash 中是否存在某个字段
     * @param hashKey Hash 表的 key
     * @param key 字段 key
     * @return true 存在，false 不存在
     */
    public boolean hasKey(String hashKey, String key) {
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        return hashOps.hasKey(hashKey, key);
    }

    public void set(String key, long expireSeconds) {
       redisTemplate.opsForHash().put(key, expireSeconds, System.currentTimeMillis());
    }
}
