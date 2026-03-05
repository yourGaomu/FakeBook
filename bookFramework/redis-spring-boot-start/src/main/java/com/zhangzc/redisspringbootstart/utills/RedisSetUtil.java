package com.zhangzc.redisspringbootstart.utills;


import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis Set结构通用工具类
 * 封装Set的常用操作，支持泛型（元素类型需为Redis支持的序列化类型）
 */
@RequiredArgsConstructor
public class RedisSetUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Long DefaultExpireTime = 3600L;

    /**
     * 向Set中添加单个元素（默认过期时间1小时）
     * @param key 集合键
     * @param value 元素值
     * @return true：添加成功（元素不存在），false：添加失败（元素已存在）
     */
    public <T> boolean add(String key, T value) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Long result = setOps.add(key, value);
        // 添加成功后设置过期时间
        if (result != null && result > 0) {
            redisTemplate.expire(key, DefaultExpireTime, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * 向Set中批量添加元素（默认过期时间1小时）
     * @param key 集合键
     * @param values 元素集合
     * @return 成功添加的元素数量
     */
    public <T> long addAll(String key, Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Long result = setOps.add(key, values.toArray());
        // 添加成功后设置过期时间
        if (result != null && result > 0) {
            redisTemplate.expire(key, DefaultExpireTime, TimeUnit.SECONDS);
        }
        return result != null ? result : 0;
    }

    /**
     * 从Set中删除单个元素
     * @param key 集合键
     * @param value 元素值
     * @return true：删除成功（元素存在），false：删除失败（元素不存在）
     */
    public <T> boolean remove(String key, T value) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        return setOps.remove(key, value) > 0;
    }

    /**
     * 从Set中批量删除元素
     * @param key 集合键
     * @param values 元素集合
     * @return 成功删除的元素数量
     */
    public <T> long removeAll(String key, Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        return setOps.remove(key, values.toArray());
    }

    /**
     * 判断元素是否存在于Set中
     * @param key 集合键
     * @param value 元素值
     * @return true：存在，false：不存在
     */
    public <T> boolean isMember(String key, T value) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        return setOps.isMember(key, value);
    }

    /**
     * 获取Set中的所有元素
     * @param key 集合键
     * @return 元素集合（泛型）
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> members(String key) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> rawSet = setOps.members(key);
        if (rawSet == null || rawSet.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> resultSet = new HashSet<>();
        for (Object obj : rawSet) {
            resultSet.add((T) obj);
        }
        return resultSet;
    }

    /**
     * 获取Set的大小（元素数量）
     * @param key 集合键
     * @return 集合大小，若键不存在则返回0
     */
    public long size(String key) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        return setOps.size(key);
    }

    /**
     * 随机获取Set中的一个元素
     * @param key 集合键
     * @return 随机元素，若集合为空则返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T randomMember(String key) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        return (T) setOps.randomMember(key);
    }

    /**
     * 随机获取Set中的多个元素（可重复）
     * @param key 集合键
     * @param count 获取数量
     * @return 随机元素列表，若集合为空则返回空列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> randomMembers(String key, long count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        List<Object> rawList = setOps.randomMembers(key, count);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> resultList = new ArrayList<>();
        for (Object obj : rawList) {
            resultList.add((T) obj);
        }
        return resultList;
    }

    /**
     * 随机获取Set中的多个元素（不重复）
     * @param key 集合键
     * @param count 获取数量
     * @return 随机元素集合，若集合为空则返回空集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> distinctRandomMembers(String key, long count) {
        if (count <= 0) {
            return Collections.emptySet();
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> rawSet = setOps.distinctRandomMembers(key, count);
        if (rawSet == null || rawSet.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> resultSet = new HashSet<>();
        for (Object obj : rawSet) {
            resultSet.add((T) obj);
        }
        return resultSet;
    }

    /**
     * 移除并返回Set中的一个随机元素
     * @param key 集合键
     * @return 被移除的随机元素，若集合为空则返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T pop(String key) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        return (T) setOps.pop(key);
    }

    /**
     * 移除并返回Set中的多个随机元素
     * @param key 集合键
     * @param count 移除数量
     * @return 被移除的随机元素列表，若集合为空则返回空列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> pop(String key, long count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        List<Object> rawList = setOps.pop(key, count);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> resultList = new ArrayList<>();
        for (Object obj : rawList) {
            resultList.add((T) obj);
        }
        return resultList;
    }

    /**
     * 计算多个Set的交集，并将结果存储到新的Set中（默认过期时间1小时）
     * @param destKey 结果集合键
     * @param keys 待计算交集的集合键列表
     * @return 交集的元素数量
     */
    public long intersectAndStore(String destKey, Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Long result = setOps.intersectAndStore(keys, destKey);
        // 存储成功后设置过期时间
        if (result != null && result > 0) {
            redisTemplate.expire(destKey, DefaultExpireTime, TimeUnit.SECONDS);
        }
        return result != null ? result : 0;
    }

    /**
     * 计算两个Set的交集，并将结果存储到新的Set中（默认过期时间1小时）
     * @param destKey 结果集合键
     * @param key1 第一个集合键
     * @param key2 第二个集合键
     * @return 交集的元素数量
     */
    public long intersectAndStore(String destKey, String key1, String key2) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Long result = setOps.intersectAndStore(key1, key2, destKey);
        // 存储成功后设置过期时间
        if (result != null && result > 0) {
            redisTemplate.expire(destKey, DefaultExpireTime, TimeUnit.SECONDS);
        }
        return result != null ? result : 0;
    }

    /**
     * 计算多个Set的交集
     * @param keys 待计算交集的集合键列表
     * @return 交集元素集合，若无交集则返回空集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> intersect(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> rawSet = setOps.intersect(keys);
        if (rawSet == null || rawSet.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> resultSet = new HashSet<>();
        for (Object obj : rawSet) {
            resultSet.add((T) obj);
        }
        return resultSet;
    }

    /**
     * 计算两个Set的交集
     * @param key1 第一个集合键
     * @param key2 第二个集合键
     * @return 交集元素集合，若无交集则返回空集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> intersect(String key1, String key2) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> rawSet = setOps.intersect(key1, key2);
        if (rawSet == null || rawSet.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> resultSet = new HashSet<>();
        for (Object obj : rawSet) {
            resultSet.add((T) obj);
        }
        return resultSet;
    }

    /**
     * 计算多个Set的并集，并将结果存储到新的Set中（默认过期时间1小时）
     * @param destKey 结果集合键
     * @param keys 待计算并集的集合键列表
     * @return 并集的元素数量
     */
    public long unionAndStore(String destKey, Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Long result = setOps.unionAndStore(keys, destKey);
        // 存储成功后设置过期时间
        if (result != null && result > 0) {
            redisTemplate.expire(destKey, DefaultExpireTime, TimeUnit.SECONDS);
        }
        return result != null ? result : 0;
    }

    /**
     * 计算两个Set的并集，并将结果存储到新的Set中（默认过期时间1小时）
     * @param destKey 结果集合键
     * @param key1 第一个集合键
     * @param key2 第二个集合键
     * @return 并集的元素数量
     */
    public long unionAndStore(String destKey, String key1, String key2) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Long result = setOps.unionAndStore(key1, key2, destKey);
        // 存储成功后设置过期时间
        if (result != null && result > 0) {
            redisTemplate.expire(destKey, DefaultExpireTime, TimeUnit.SECONDS);
        }
        return result != null ? result : 0;
    }

    /**
     * 计算多个Set的并集
     * @param keys 待计算并集的集合键列表
     * @return 并集元素集合，若所有集合为空则返回空集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> union(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> rawSet = setOps.union(keys);
        if (rawSet == null || rawSet.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> resultSet = new HashSet<>();
        for (Object obj : rawSet) {
            resultSet.add((T) obj);
        }
        return resultSet;
    }

    /**
     * 计算两个Set的并集
     * @param key1 第一个集合键
     * @param key2 第二个集合键
     * @return 并集元素集合，若两个集合都为空则返回空集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> union(String key1, String key2) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> rawSet = setOps.union(key1, key2);
        if (rawSet == null || rawSet.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> resultSet = new HashSet<>();
        for (Object obj : rawSet) {
            resultSet.add((T) obj);
        }
        return resultSet;
    }

    /**
     * 计算多个Set的差集，并将结果存储到新的Set中（默认过期时间1小时）
     * @param destKey 结果集合键
     * @param keys 待计算差集的集合键列表（第一个键为基准集合，其余为被减去的集合）
     * @return 差集的元素数量
     */
    public long differenceAndStore(String destKey, Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Long result = setOps.differenceAndStore(keys, destKey);
        // 存储成功后设置过期时间
        if (result != null && result > 0) {
            redisTemplate.expire(destKey, DefaultExpireTime, TimeUnit.SECONDS);
        }
        return result != null ? result : 0;
    }

    /**
     * 计算两个Set的差集（key1 - key2），并将结果存储到新的Set中（默认过期时间1小时）
     * @param destKey 结果集合键
     * @param key1 基准集合键
     * @param key2 被减去的集合键
     * @return 差集的元素数量
     */
    public long differenceAndStore(String destKey, String key1, String key2) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Long result = setOps.differenceAndStore(key1, key2, destKey);
        // 存储成功后设置过期时间
        if (result != null && result > 0) {
            redisTemplate.expire(destKey, DefaultExpireTime, TimeUnit.SECONDS);
        }
        return result != null ? result : 0;
    }

    /**
     * 计算多个Set的差集（第一个键为基准集合，其余为被减去的集合）
     * @param keys 待计算差集的集合键列表
     * @return 差集元素集合，若无差集则返回空集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> difference(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> rawSet = setOps.difference(keys);
        if (rawSet == null || rawSet.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> resultSet = new HashSet<>();
        for (Object obj : rawSet) {
            resultSet.add((T) obj);
        }
        return resultSet;
    }

    /**
     * 计算两个Set的差集（key1 - key2）
     * @param key1 基准集合键
     * @param key2 被减去的集合键
     * @return 差集元素集合，若无差集则返回空集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> difference(String key1, String key2) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> rawSet = setOps.difference(key1, key2);
        if (rawSet == null || rawSet.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> resultSet = new HashSet<>();
        for (Object obj : rawSet) {
            resultSet.add((T) obj);
        }
        return resultSet;
    }

    /**
     * 为Set设置过期时间
     * @param key 集合键
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     * @return true：设置成功，false：设置失败（键不存在或Redis错误）
     */
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        if (timeout <= 0) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, timeUnit));
    }

    /**
     * 获取Set的过期时间
     * @param key 集合键
     * @param timeUnit 时间单位
     * @return 过期时间（-1：永久有效，-2：键不存在）
     */
    public long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 删除Set集合
     * @param key 集合键
     * @return true：删除成功，false：删除失败（键不存在或Redis错误）
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 批量删除Set集合
     * @param keys 集合键列表
     * @return 成功删除的集合数量
     */
    public long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * 检查Set集合是否存在
     * @param key 集合键
     * @return true：存在（且为Set类型），false：不存在或类型不匹配
     */
    public boolean exists(String key) {
        // RedisTemplate的hasKey仅判断键是否存在，不判断类型；若需严格判断类型，可通过type命令扩展
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}