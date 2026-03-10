-- 脚本参数：KEYS[1] = 顶级 Hash key（如 key1），ARGV[1] = 字段名（如 k1）
local hashKey = KEYS[1]
local field = ARGV[1]
local expireField = field .. '_expire'

-- 1. 获取过期时间戳
local expireTimestampStr = redis.call('hget', hashKey, expireField)
if not expireTimestampStr then
    return nil  -- 无过期字段，视为不存在
end

-- 2. 解析过期时间戳为数字
local expireTimestamp = tonumber(expireTimestampStr)
if not expireTimestamp then
    -- 数据损坏，清理并返回 nil
    redis.call('hdel', hashKey, field, expireField)
    return nil
end

-- 3. 获取当前时间戳（毫秒，整数）
local t = redis.call('time')
local currentTimestamp = t[1] * 1000 + math.floor(t[2] / 1000)

-- 4. 检查是否过期
if currentTimestamp > expireTimestamp then
    redis.call('hdel', hashKey, field, expireField)
    return nil
end

-- 5. 未过期，返回字段值
return redis.call('hget', hashKey, field)