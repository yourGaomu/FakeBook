package com.zhangzc.blog.blogai.Service;

import com.zhangzc.blog.blogai.Pojo.domain.TLlmModel;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;


import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class VisionService {

    private final TLlmModelService tLlmModelService;

    // 缓存构建好的 ChatLanguageModel 实例
    private final Map<Long, OpenAiChatModel> modelCache = new ConcurrentHashMap<>();

    /**
     * 根据 modelId 获取对应的视觉模型
     *
     * @param modelId 模型ID，如果为 null 则尝试查找默认视觉模型
     * @return ChatLanguageModel 实例
     */
    private OpenAiChatModel getVisionModel(Long modelId) {
        // 如果 modelId 为空，尝试查找默认视觉模型 (优先查找包含 'vl' 或 'vision' 的模型)
        if (modelId == null) {
            TLlmModel defaultModel = tLlmModelService.lambdaQuery()
                    .eq(TLlmModel::getIsEnable, 1)
                    .and(w -> w.like(TLlmModel::getModelCode, "vl")
                            .or()
                            .like(TLlmModel::getModelCode, "vision"))
                    .last("LIMIT 1")
                    .one();
            
            if (defaultModel == null) {
                // 如果找不到特定的视觉模型，回退到任意启用的模型 (可能不支持视觉，但总比抛异常好，让调用端处理错误)
                defaultModel = tLlmModelService.lambdaQuery()
                        .eq(TLlmModel::getIsEnable, 1)
                        .eq(TLlmModel::getId,2L)
                        .one();
            }

            if (defaultModel != null) {
                modelId = defaultModel.getId();
            } else {
                throw new RuntimeException("No available LLM model configuration found for vision task.");
            }
        }

        // 检查缓存
        if (modelCache.containsKey(modelId)) {
            return modelCache.get(modelId);
        }

        // 构建新实例
        return buildAndCacheModel(modelId);
    }

    private synchronized OpenAiChatModel buildAndCacheModel(Long modelId) {
        // 双重检查
        if (modelCache.containsKey(modelId)) {
            return modelCache.get(modelId);
        }

        TLlmModel modelConfig = tLlmModelService.getById(modelId);
        if (modelConfig == null) {
            throw new RuntimeException("LLM model configuration not found for id: " + modelId);
        }

        if (modelConfig.getIsEnable() != 1) {
            throw new RuntimeException("LLM model is disabled: " + modelConfig.getModelName());
        }

        log.info("Building Vision ChatLanguageModel for: {} ({})", modelConfig.getModelName(), modelConfig.getModelCode());

        // 初始化专门用于视觉任务的模型
        // 注意：DashScope 的 VL 模型兼容 OpenAI 格式
        OpenAiChatModel visionModel = OpenAiChatModel.builder()
                .apiKey(modelConfig.getApiKey())
                .baseUrl(modelConfig.getBaseUrl())
                .modelName(modelConfig.getModelCode()) 
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        modelCache.put(modelId, visionModel);
        return visionModel;
    }

    /**
     * 描述图片内容
     *
     * @param visionModelId 视觉模型的 ID (可为 null，使用默认策略)
     * @param imageUrl      图片的 URL
     * @param userQuestion  用户的提问（如果为空，则使用默认的描述提示词）
     * @return 图片的文本描述或针对问题的回答
     */
    public String describeImage(Long visionModelId, String imageUrl, String userQuestion) {
        log.info("Requesting image description for: {} using modelId: {}", imageUrl, visionModelId);
        try {
            OpenAiChatModel visionModel = getVisionModel(visionModelId);

            String promptText;
            if (userQuestion != null && !userQuestion.trim().isEmpty()) {
                // 如果用户有具体问题，让视觉模型针对问题进行回答
                promptText = "请根据这张图片回答用户的问题：" + userQuestion + "\n\n请详细描述与问题相关的图片细节，以便后续对话使用。";
            } else {
                // 如果用户没有具体问题，则进行通用描述
                promptText = "请详细描述这张图片的内容，包括主要物体、场景、文字（如果有）以及任何显著的细节。";
            }

            // 构建包含图片的用户消息
            UserMessage userMessage = UserMessage.from(
                    TextContent.from(promptText),
                    ImageContent.from(imageUrl)
            );

            ChatResponse chatResponse = visionModel.chat(userMessage);
            String description = chatResponse.aiMessage().text();
            log.info("Image description received: {}", description);
            return description;
        } catch (Exception e) {
            log.error("Failed to describe image", e);
            return "图片识别失败: " + e.getMessage();
        }
    }
    
    /**
     * 描述图片内容 (兼容旧代码，使用默认提示词)
     *
     * @param visionModelId 视觉模型的 ID
     * @param imageUrl      图片的 URL
     * @return 图片的文本描述
     */
    public String describeImage(Long visionModelId, String imageUrl) {
        return describeImage(visionModelId, imageUrl, null);
    }
}
