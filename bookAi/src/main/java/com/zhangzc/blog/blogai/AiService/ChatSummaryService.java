package com.zhangzc.blog.blogai.AiService;

import com.zhangzc.blog.blogai.Pojo.domain.TLlmModel;
import com.zhangzc.blog.blogai.Service.TLlmModelService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSummaryService {

    private final TLlmModelService tLlmModelService;

    /**
     * 生成摘要
     * @param oldSummary 旧摘要
     * @param newMessages 新增的消息列表
     * @param modelId 使用的模型ID（如果为null，则自动选择一个可用模型）
     * @return 新摘要
     */
    public String summarize(String oldSummary, List<ChatMessage> newMessages, Long modelId) {
        try {
            // 1. 获取模型配置
            TLlmModel modelConfig = getModelConfig(modelId);
            if (modelConfig == null) {
                log.warn("No available model for summarization");
                return oldSummary;
            }

            // 2. 构建 ChatLanguageModel (Blocking)
            OpenAiChatModel chatModel = buildChatModel(modelConfig);

            // 3. 构建 Prompt
            String messagesText = newMessages.stream()
                    .map(msg -> msg.type() + ": " + getMessageText(msg))
                    .collect(Collectors.joining("\n"));

            String prompt = String.format(
                    "You are a helpful assistant summarizing a conversation.\n" +
                    "Existing summary: %s\n\n" +
                    "New conversation lines:\n%s\n\n" +
                    "Update the summary to include the new information, keeping it concise and informative. " +
                    "Focus on user preferences, key decisions, and important context. " +
                    "Do not include trivial greetings. If the existing summary is empty, create a new one.",
                    oldSummary != null ? oldSummary : "None",
                    messagesText
            );

            // 4. 调用 LLM
            return chatModel.chat(prompt);

        } catch (Exception e) {
            log.error("Failed to generate summary", e);
            return oldSummary; // 失败时返回旧摘要，避免数据丢失
        }
    }

    private String getMessageText(ChatMessage msg) {
        if (msg instanceof UserMessage) return ((UserMessage) msg).singleText();
        if (msg instanceof dev.langchain4j.data.message.AiMessage) return ((dev.langchain4j.data.message.AiMessage) msg).text();
        if (msg instanceof SystemMessage) return ((SystemMessage) msg).text();
        return "";
    }

    private TLlmModel getModelConfig(Long modelId) {
        if (modelId != null) {
            return tLlmModelService.getById(modelId);
        }
        // 默认获取第一个启用的模型
        return tLlmModelService.lambdaQuery()
                .eq(TLlmModel::getIsEnable, 1)
                .last("LIMIT 1")
                .one();
    }

    private OpenAiChatModel buildChatModel(TLlmModel config) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelCode())
                .timeout(Duration.ofSeconds(60))
                .logRequests(false)
                .logResponses(false);

        if (config.getBaseUrl() != null && !config.getBaseUrl().isBlank()) {
            builder.baseUrl(config.getBaseUrl());
        }
        return builder.build();
    }
}
