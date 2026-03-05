package com.zhangzc.blog.blogai.Pojo.dto;

import dev.langchain4j.data.message.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryDto implements Serializable {
    private String sessionId;
    // 使用 JSON 字符串传输消息列表，避免序列化问题
    private String messagesJson;
}
