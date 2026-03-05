package com.zhangzc.blog.blogai.Pojo.domain;

import dev.langchain4j.data.message.ChatMessageType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "t_chat_message")
public class MongoChatMessage {

    @Id
    private String id;

    @Indexed
    @Field("session_id")
    private String sessionId;

    @Field("role")
    private ChatMessageType type;

    @Field("content")
    private String content;

    @Field("token_count")
    private Integer tokenCount;

    @Field("created_at")
    private LocalDateTime createdAt;
}
