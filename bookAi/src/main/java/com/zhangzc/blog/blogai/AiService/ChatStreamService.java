package com.zhangzc.blog.blogai.AiService;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;


@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT
)
public interface ChatStreamService {
    Flux<String> chat(@MemoryId String userId, @UserMessage String userMessage);
}




