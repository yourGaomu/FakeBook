package com.zhangzc.blog.blogai.Config;


import com.zhangzc.blog.blogai.Store.HybridChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatConfig {

    private final HybridChatMemoryStore hybridChatMemoryStore;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryid -> MessageWindowChatMemory.builder()
                .maxMessages(30)
                .id(memoryid)
                .chatMemoryStore(hybridChatMemoryStore)
                .build();
    }
}
