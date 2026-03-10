-- 明确参数对应关系（和 Java 传递的 ARGV 顺序一致）
-- KEYS[1] = 顶级 Hash key（如 VerificationCode）
-- ARGV[1] = 字段名（phone，如 15881363498）
-- ARGV[2] = 字段值（templateCode，如 100001）—— 注意：这里你实际想存的是验证码，后续可以改
-- ARGV[3] = 过期秒数（字符串类型，如 "300"）

local hashKey = KEYS[1]
-- 严格按 ARGV 索引取值，不猜顺序
local field = ARGV[1]
local value = ARGV[2]
-- 关键：直接取 ARGV[3]，加双重兜底（即使转数字失败也不会 nil）
local expireSec = tonumber(ARGV[3])
-- 兜底1：若 ARGV[3] 转数字失败，默认 300 秒
if expireSec == nil then
    expireSec = 300
end
-- 兜底2：防止 expireSec 是非法数字（虽然概率极低）
if type(expireSec) ~= "number" then
    expireSec = 300
end

-- 计算过期时间戳（毫秒级），这里用更安全的写法（避免浮点数问题）
local currentTime = redis.call('time')
local currentTimestamp = currentTime[1] * 1000 + math.floor(currentTime[2] / 1000)
local expireTimestamp = currentTimestamp + expireSec * 1000

-- 原子存入 Hash（字段值 + 过期时间戳）
redis.call('hset', hashKey, field, value, field .. '_expire', expireTimestamp)

-- 返回详细信息，方便调试
return {
    status = "success",
    field = field,
    value = value,
    expireSec = expireSec,
    expireTimestamp = expireTimestamp
}