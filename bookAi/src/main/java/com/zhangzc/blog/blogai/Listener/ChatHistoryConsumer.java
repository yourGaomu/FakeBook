package com.zhangzc.blog.blogai.Listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zhangzc.blog.blogai.Pojo.dto.ChatHistoryDto;
import com.zhangzc.blog.blogai.Store.MongoChatMemoryStore;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.kafkaspringbootstart.annotation.AutoInserByRedis;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatHistoryConsumer {

    private final MongoChatMemoryStore mongoChatMemoryStore;

    @KafkaListener(topics = "blog-ai-chat-history")
    @AutoInserByRedis(
            strategy = AutoInserByRedis.DuplicateStrategy.SKIP, // 重复消息跳过
            enableAlert = true,                                   // 启用告警
            redisKeyPrefix = "kafka:offset:chat"                  // Redis key前缀
    )
    public void consumeChatHistory(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        Object value = record.value();
        if (value == null) {
            ack.acknowledge();
            return;
        }

        try {
            ChatHistoryDto chatHistoryDto = null;
            // 处理可能的双重序列化问题
            if (value instanceof String jsonStr) {
                // 尝试解析为 ChatHistoryDto
                try {
                    chatHistoryDto = JsonUtils.parseObject(jsonStr, ChatHistoryDto.class);
                } catch (Exception e) {
                    log.warn("Direct JSON parsing failed: {}", jsonStr, e);
                }
            } else if (value instanceof ChatHistoryDto) {
                 chatHistoryDto = (ChatHistoryDto) value;
            } else {
                // 如果是LinkedHashMap等其他类型（Spring Kafka默认JsonDeserializer可能转换成Map）
                try {
                     String json = JsonUtils.toJsonString(value);
                     chatHistoryDto = JsonUtils.parseObject(json, ChatHistoryDto.class);
                } catch (Exception e) {
                    log.warn("Failed to convert value to ChatHistoryDto: {}", value.getClass(), e);
                }
            }

            if (chatHistoryDto != null) {
                String sessionId = chatHistoryDto.getSessionId();
                String messagesJson = chatHistoryDto.getMessagesJson();
                
                if (sessionId != null && messagesJson != null) {
                    List<ChatMessage> messages = ChatMessageDeserializer.messagesFromJson(messagesJson);
                    // 保存到 MongoDB
                    // 使用 MongoChatMemoryStore 的 updateMessages 方法
                    mongoChatMemoryStore.updateMessages(sessionId, messages);
                    log.info("Successfully saved chat history for session: {}", sessionId);
                }
            } else {
                log.warn("ChatHistoryDto is null, skipping message");
            }

        } catch (Exception e) {
            log.error("Failed to consume chat history message", e);
        } finally {
            ack.acknowledge();
        }
    }
}
