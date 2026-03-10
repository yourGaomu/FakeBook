package com.zhangzc.blog.blogai.Store;

import com.zhangzc.blog.blogai.Pojo.Vo.SessionListVo;
import com.zhangzc.blog.blogai.Pojo.domain.MongoChatMessage;
import com.zhangzc.blog.blogai.Pojo.dto.ChatHistoryDto;


import com.zhangzc.kafkaspringbootstart.utills.KafkaUtills;
import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class HybridChatMemoryStore implements ChatMemoryStore {

    private final RedisUtil redisUtil;
    private final KafkaUtills kafkaUtills;
    private final MongoChatMemoryStore mongoStore;

    private static final String REDIS_PREFIX = "chat:history:";
    private static final long REDIS_EXPIRE = 60 * 60 * 24; // 1 day
    private static final String KAFKA_TOPIC = "blog-ai-chat-history";

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = REDIS_PREFIX + memoryId;
        
        // 1. Try Redis
        Object cached = redisUtil.get(key);
        if (cached != null) {
            try {
                String json = (String) cached;
                return ChatMessageDeserializer.messagesFromJson(json);
            } catch (Exception e) {
                log.warn("Failed to deserialize chat history from Redis", e);
            }
        }

        // 2. Fallback to Mongo
        List<ChatMessage> messages = mongoStore.getMessages(memoryId);

        // 3. Update Redis
        if (messages != null && !messages.isEmpty()) {
            String json = ChatMessageSerializer.messagesToJson(messages);
            redisUtil.set(key, json, REDIS_EXPIRE);
        }
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // 1. Update Redis (Write-Through)
        String key = REDIS_PREFIX + memoryId;
        String json = ChatMessageSerializer.messagesToJson(messages);
        redisUtil.set(key, json, REDIS_EXPIRE);

        // 2. Send to Kafka for async persistence
        ChatHistoryDto dto = new ChatHistoryDto(String.valueOf(memoryId), json);
        // 使用 KafkaUtills 发送消息
        kafkaUtills.sendMessage(KAFKA_TOPIC, dto);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = REDIS_PREFIX + memoryId;
        redisUtil.del(key);
        // 直接删除 Mongo 数据，或者也通过 Kafka 发送删除事件？
        // 为了简单起见和保证删除的一致性，这里直接调用 Mongo 删除
        mongoStore.deleteMessages(memoryId);
    }

    public void createSession(String sessionId, Long userId, Long modelId, Long promptId, String title) {
        mongoStore.createSession(sessionId, userId, modelId, promptId, title);
    }

    public List<SessionListVo> getSessions(Long userId) {
        return mongoStore.getSessionList(userId);
    }

    public List<MongoChatMessage> getHistoryDetails(String sessionId) {
        return mongoStore.getHistoryDetails(sessionId);
    }
}
