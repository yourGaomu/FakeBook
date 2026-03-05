package com.zhangzc.redisspringbootstart.utills;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor // 自动生成带参构造函数
@Slf4j
public class RedisUtil {


    private final Long DefaultExpireTime = 3600L;
    private RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public boolean delHash(String key, List<String> items) {
        try {
            redisTemplate.opsForHash().delete(key, items.toArray());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delHash(String key) {
        try {
            redisTemplate.delete(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delHash(String key, String item) {
        try {
            redisTemplate.opsForHash().delete(key, item);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断Redis中是否存在指定Hash键，以及该Hash表中是否包含item中的所有字段
     *
     * @param key  Hash键名
     * @param item 包含要检查的字段的Map（仅需关注key，value无意义）
     * @return true：键存在且Hash表包含item中的所有字段；false：键不存在或缺少至少一个字段
     */
    public boolean hasHashKey(String key, Map<String, Object> item) {
        // 1. 先判断Hash键是否存在
        if (!redisTemplate.hasKey(key)) {
            return false;
        }

        // 2. 若item为null，仅需键存在即可（根据业务调整，此处假设item为null时返回键存在性）
        if (item == null || item.isEmpty()) {
            return true;
        }

        // 3. 检查item中的所有字段是否都存在于Hash表中
        return item.keySet().stream()
                .allMatch(field -> redisTemplate.opsForHash().hasKey(key, field));
    }


    public boolean setHash(String key, Map<String, Object> item, Long time) {
        // 参数校验：避免空Map写入
        if (item == null || item.isEmpty()) {
            log.warn("Cannot set empty hash item for key: {}", key);
            return false;
        }

        redisTemplate.opsForHash().putAll(key, item);
        if (time > 0)
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
        else
            redisTemplate.expire(key, DefaultExpireTime, TimeUnit.SECONDS);
        return true;
    }


    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public Object del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                return redisTemplate.delete(key[0]);
            } else {
                return redisTemplate.delete(Arrays.asList(key));
            }
        }
        return null;
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, DefaultExpireTime, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================================Map=================================

    /**
     * 获取Redis Hash结构中指定key下的多个item（字段）对应的值
     *
     * @param key   键 不能为null
     * @param items 字段集合 不能为null且不能为空
     * @return 对应字段的值列表（顺序与items一致，不存在的字段对应值为null）
     */
    public List<Object> hmget(String key, Collection<?> items) {
        // 参数校验
        if (key == null) {
            throw new IllegalArgumentException("key不能为null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items集合不能为null且不能为空");
        }
        // 调用multiGet方法获取多个字段值
        return redisTemplate.opsForHash().multiGet(key, (Collection<Object>) items);
    }


    /**
     *
     * @param key 键 不能为null
     *
     *
     */
    public Object hmget(String key, Object item) {
        // 参数校验
        if (key == null) {
            throw new IllegalArgumentException("key不能为null");
        }
        if (item == null) {
            throw new IllegalArgumentException("items集合不能为null且不能为空");
        }
        return redisTemplate.opsForHash().get(key, item);
    }


    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     */
// 内部通用实现
    public <V> boolean hmsetGeneric(String key, Map<String, V> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            expire(key, DefaultExpireTime);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 在RedisUtil中新增重载方法
    public boolean hset(String key, Map<String, String> map) {
        // 利用HashMap的构造器自动转换类型（String是Object的子类）
        return hmset(key, new HashMap<String, Object>(map));
    }

    // 原方法保留
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            expire(key, DefaultExpireTime);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            expire(key, DefaultExpireTime);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true成功 false失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个Set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            expire(key, DefaultExpireTime);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            expire(key, DefaultExpireTime);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ===============================ZSet (Sorted Set)=================================

    /**
     * 向ZSet中添加元素（默认过期时间1小时）
     *
     * @param key   键
     * @param value 元素值
     * @param score 分数（用于排序）
     * @return true成功 false失败
     */
    public boolean zAdd(String key, Object value, double score) {
        try {
            Boolean result = redisTemplate.opsForZSet().add(key, value, score);
            expire(key, DefaultExpireTime);
            return result != null && result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向ZSet中批量添加元素（默认过期时间1小时）
     *
     * @param key    键
     * @param values Map<元素, 分数>
     * @return 成功添加的元素数量
     */
    public long zAdd(String key, Map<Object, Double> values) {
        try {
            if (values == null || values.isEmpty()) {
                return 0;
            }
            // 转换为Spring需要的TypedTuple集合
            Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();
            values.forEach((v, s) -> tuples.add(
                    org.springframework.data.redis.core.ZSetOperations.TypedTuple.of(v, s)
            ));
            Long result = redisTemplate.opsForZSet().add(key, tuples);
            expire(key, DefaultExpireTime);
            return result != null ? result : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取ZSet的大小
     *
     * @param key 键
     * @return 元素数量
     */
    public long zCard(String key) {
        try {
            Long size = redisTemplate.opsForZSet().zCard(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 批量获取ZSet中指定多个元素的score值
     *
     * @param key    键
     * @param values 要获取的元素列表
     * @return 元素与score的映射（不存在的元素不会出现在Map中）
     */
    public Map<String, Double> batchZScore(String key, List<String> values) {
        Map<String, Double> scoreMap = new HashMap<>();
        if (values == null || values.isEmpty()) {
            return scoreMap;
        }

        try {
            for (String value : values) {
                Double score = zScore(key, value); // 调用你已有的zScore方法
                scoreMap.put(value, score);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scoreMap;
    }


    /**
     * 获取ZSet中指定范围的元素（按score升序）
     *
     * @param key   键
     * @param start 开始位置（0表示第一个）
     * @param end   结束位置（-1表示最后一个）
     * @return 元素集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    /**
     * 获取ZSet中指定范围的元素（按score降序）
     *
     * @param key   键
     * @param start 开始位置（0表示第一个）
     * @param end   结束位置（-1表示最后一个）
     * @return 元素集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRange(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    /**
     * 获取ZSet中指定范围的元素和score（按score升序）
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素和score的集合
     */
    public Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    /**
     * 获取ZSet中指定范围的元素和score（按score降序）
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 元素和score的集合
     */
    public Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> zReverseRangeWithScores(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    /**
     * 根据score范围获取ZSet中的元素
     *
     * @param key 键
     * @param min 最小score
     * @param max 最大score
     * @return 元素集合
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        try {
            return redisTemplate.opsForZSet().rangeByScore(key, min, max);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    /**
     * 获取元素的score
     *
     * @param key   键
     * @param value 元素值
     * @return score，如果不存在返回null
     */
    public Double zScore(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取元素的排名（按score升序，最小的是0）
     *
     * @param key   键
     * @param value 元素值
     * @return 排名，如果不存在返回null
     */
    public Long zRank(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().rank(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取元素的排名（按score降序，最大的是0）
     *
     * @param key   键
     * @param value 元素值
     * @return 排名，如果不存在返回null
     */
    public Long zReverseRank(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().reverseRank(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从zSet中移除元素
     *
     * @param key    键
     * @param values 元素值（可多个）
     * @return 成功移除的元素数量
     */
    public long zRemove(String key, Object... values) {
        try {
            Long result = redisTemplate.opsForZSet().remove(key, values);
            return result != null ? result : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除指定排名范围的元素
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 成功移除的元素数量
     */
    public long zRemoveRange(String key, long start, long end) {
        try {
            Long result = redisTemplate.opsForZSet().removeRange(key, start, end);
            return result != null ? result : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除指定score范围的元素
     *
     * @param key 键
     * @param min 最小score
     * @param max 最大score
     * @return 成功移除的元素数量
     */
    public long zRemoveRangeByScore(String key, double min, double max) {
        try {
            Long result = redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
            return result != null ? result : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 增加元素的score
     *
     * @param key   键
     * @param value 元素值
     * @param delta 增加的score
     * @return 增加后的score
     */
    public Double zIncrementScore(String key, Object value, double delta) {
        try {
            return redisTemplate.opsForZSet().incrementScore(key, value, delta);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 统计指定score范围内的元素数量
     *
     * @param key 键
     * @param min 最小score
     * @param max 最大score
     * @return 元素数量
     */
    public long zCount(String key, double min, double max) {
        try {
            Long count = redisTemplate.opsForZSet().count(key, min, max);
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}