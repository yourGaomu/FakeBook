--[[
Kafka消费偏移量检查与更新脚本（修复版）

核心修改：
1. 返回值改为标准JSON字符串，兼容JSON解析器
2. 把'invalid_offset'改为'"invalid_offset"'（带引号的JSON字符串）
3. 统一返回格式为JSON，避免解析异常
--]]
local topic = KEYS[1]                    -- Hash Key: kafka:offset:test-topic
local partition = ARGV[1]                -- Hash Field: partition-0
local currentOffset = tonumber(ARGV[2])  -- 当前Kafka偏移量

-- 参数校验：返回标准JSON字符串（带引号的invalid_offset）
if currentOffset == nil then
    -- 关键修改：返回JSON格式字符串，'invalid_offset'加引号
    return '[-1, "invalid_offset"]'
end

-- 获取Redis中存储的偏移量
local redisOffset = redis.call('HGET', topic, partition)

-- 场景1：首次消费该分区
if redisOffset == false then
    redis.call('HSET', topic, partition, currentOffset)
    return '[1, "first"]'
end

-- 转换为数字进行对比
redisOffset = tonumber(redisOffset)

-- 场景2：正常消费（offset递增）
if currentOffset > redisOffset then
    redis.call('HSET', topic, partition, currentOffset)
    return '[1, "allow"]'
end

-- 场景3：重复消费（offset相等）
if currentOffset == redisOffset then
    return '[0, "duplicate"]'
end

-- 场景4：异常回溯（offset倒退）
return '[-1, "rollback"]'