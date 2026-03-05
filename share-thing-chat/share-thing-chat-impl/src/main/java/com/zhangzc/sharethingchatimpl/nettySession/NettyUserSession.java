package com.zhangzc.sharethingchatimpl.nettySession;

import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NettyUserSession {

    @Resource
    private RedisUtil redisUtil;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${netty.server.port:9000}")
    private String nettyPort;

    // Redis Key 前缀
    private static final String REDIS_USER_SESSION_PREFIX = "chat:user:session:";
    private static final String REDIS_CHANNEL_PREFIX = "chat:channel:";
    
    // ChannelId -> UserId 的反向映射过期时间 (秒)，略长于心跳时间
    private static final long CHANNEL_EXPIRE_TIME = 3600; 

    // 本地连接缓存：必须保留，否则无法通过 ID 拿到 Channel 发消息
    // Key: ChannelId, Value: Channel
    private static final Map<String, Channel> localChannelMap = new ConcurrentHashMap<>();

    // 获取本机IP
    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /**
    * 放入数据 (上线)
    * */
    public void addSession(String userId, Channel channel) {
        String channelId = channel.id().asLongText();
        // 用户期望存储过期时间戳而不是 ServerInfo
        long expireAt = System.currentTimeMillis() + CHANNEL_EXPIRE_TIME * 1000;
        String expireAtStr = String.valueOf(expireAt);

        // 1. 本地存一份引用
        localChannelMap.put(channelId, channel);

        // 2. Redis 存 UserId -> ChannelId 的映射 (Hash)
        // Key: chat:user:session:{userId}, Field: {channelId}, Value: {过期时间戳}
        redisUtil.hset(REDIS_USER_SESSION_PREFIX + userId, channelId, expireAtStr);

        // 3. Redis 存 ChannelId -> UserId 的映射 (String) 用于反查和过期控制
        // Key: chat:channel:{channelId}, Value: {userId}
        redisUtil.set(REDIS_CHANNEL_PREFIX + channelId, userId);
        redisUtil.expire(REDIS_CHANNEL_PREFIX + channelId, CHANNEL_EXPIRE_TIME);
        
        log.info("用户上线: userId={}, channelId={}, expireAt={}", userId, channelId, expireAtStr);
    }

    /**
     * 移除 Session (下线)
     */
    public void removeSession(Channel channel) {
        String channelId = channel.id().asLongText();
        
        // 1. 先查 UserId
        String userId = (String) redisUtil.get(REDIS_CHANNEL_PREFIX + channelId);
        
        // 2. 清理 Redis
        if (userId != null) {
            redisUtil.hdel(REDIS_USER_SESSION_PREFIX + userId, channelId);
        }
        redisUtil.del(REDIS_CHANNEL_PREFIX + channelId);
        
        // 3. 清理本地
        localChannelMap.remove(channelId);
        
        log.info("用户下线: userId={}, channelId={}", userId, channelId);
    }

    /**
     * 根据channelId获取userId
    * */
    public String getUserIdByChannelId(String channelId) {
        return (String) redisUtil.get(REDIS_CHANNEL_PREFIX + channelId);
    }

    /**
     * 判断chanelid是否存在对应的userId
    * */
    public boolean hasUserIdByChannelId(String channelId) {
        return redisUtil.hasKey(REDIS_CHANNEL_PREFIX + channelId);
    }

    /**
     * 根据userId返回对应的本地 Channel 列表
     * 注意：这里只返回【本机】的连接。如果用户连在其他机器，这里是拿不到 Channel 对象的。
     * 对于分布式场景，由于 Value 改成了过期时间，无法直接判断 Server IP，
     * 但我们可以通过 localChannelMap 是否包含该 ID 来判断是否为本机连接。
    * */
    public List<Channel> getLocalChannelsByUserId(String userId) {
        // 从 Redis 获取该用户所有的 ChannelId -> ExpirationTimestamp 映射
        Map<Object, Object> entries = redisUtil.hmget(REDIS_USER_SESSION_PREFIX + userId);
        List<Channel> channels = new ArrayList<>();
        if (entries != null) {
            long now = System.currentTimeMillis();
            
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                String channelId = (String) entry.getKey();
                String expireAtStr = (String) entry.getValue();

                // 检查是否过期 (虽然有 TTL，但做个双重检查也不错)
                try {
                    long expireAt = Long.parseLong(expireAtStr);
                    if (now > expireAt) {
                        continue; 
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式错误
                }
                
                // 只有当本地 Map 确实有这个 Channel 时，才返回
                // 这意味着该连接就在当前这台机器上
                Channel channel = localChannelMap.get(channelId);
                if (channel != null && channel.isActive()) {
                     channels.add(channel);
                }
            }
        }
        return channels;
    }
    /**
     * 获取用户所有在线端的分布情况
     * @return Map<ChannelId, ServerIp>
     */
    public Map<Object, Object> getUserSessions(String userId) {
        return redisUtil.hmget(REDIS_USER_SESSION_PREFIX + userId);
    }

    /**
     * 判断user是否有在线 Session
    * */
    public boolean hasUserId(String userId) {
        return redisUtil.hasKey(REDIS_USER_SESSION_PREFIX + userId);
    }
}
