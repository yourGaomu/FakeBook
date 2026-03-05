package com.zhangzc.blog.blogai.AiService;

import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.zhangzc.blog.blogai.Pojo.domain.TLlmModel;
import com.zhangzc.blog.blogai.Pojo.domain.TSystemMessage;
import com.zhangzc.blog.blogai.Service.TLlmModelService;
import com.zhangzc.blog.blogai.Service.TSystemMessageService;
import com.zhangzc.blog.blogai.Tools.AliWebSearchTool;
import com.zhangzc.blog.blogai.Tools.SqlTool;
import com.zhangzc.blog.blogai.Tools.TaiServiceTool;
import com.zhangzc.blog.blogai.Tools.WebSearchTool;

import dev.langchain4j.community.model.dashscope.QwenChatRequestParameters;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicChatService {

    private final TLlmModelService tLlmModelService;
    private final TSystemMessageService tSystemMessageService;
    private final ChatMemoryProvider chatMemoryProvider;
    private final TaiServiceTool taiServiceTool;
    private final WebSearchTool webSearchTool;
    private final SqlTool sqlTool;
    private final AliWebSearchTool aliWebSearchTool;

    private final Map<String,ChatStreamService> service4Online = new  ConcurrentHashMap<>();
    private final Map<String,ChatStreamService> service4NotOnline = new  ConcurrentHashMap<>();

    private final Map<Long, ChatStreamService> serviceCache = new ConcurrentHashMap<>();
    private final Map<String, ChatStreamService> serviceCacheBySystemMessage = new ConcurrentHashMap<>();


    @PostConstruct
    public void creatOpenAiService() {
        try {
            List<TLlmModel> list = tLlmModelService.lambdaQuery()
                    .eq(TLlmModel::getIsEnable, 1)
                    .like(TLlmModel::getModelCode, "text")
                    .list();
            if (!list.isEmpty()) {
                list.forEach(modelConfig -> {
                    try {
                        // 校验必要参数
                        if (modelConfig.getApiKey() == null || modelConfig.getApiKey().trim().isEmpty()) {
                            log.error("Skip building ChatStreamService for model: {} ({}) due to missing API Key", modelConfig.getModelName(), modelConfig.getModelCode());
                            return;
                        }
                        //构建OpenAiModel
                        // 假设所有支持的模型都兼容 OpenAI 协议 (如 Qwen, DeepSeek 等)
                        StreamingChatModel streamingChatModel;

                        if ("aliyun".equals(modelConfig.getProvider())) {
                            //判断是否能主动联网
                                streamingChatModel = QwenStreamingChatModel.builder()
                                        .apiKey(modelConfig.getApiKey())
                                        .modelName(modelConfig.getModelCode())
                                        .baseUrl(modelConfig.getBaseUrl())
                                        .enableSearch(true)
                                        .build();

                        } else {
                            OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                                    .apiKey(modelConfig.getApiKey())
                                    .modelName(modelConfig.getModelCode())
                                    .logRequests(false)
                                    .logResponses(false);
                            // 只有当 BaseUrl 不为空时才设置，否则使用默认的 OpenAI URL
                            if (modelConfig.getBaseUrl() != null && !modelConfig.getBaseUrl().trim().isEmpty()) {
                                builder.baseUrl(modelConfig.getBaseUrl());
                            }
                            streamingChatModel = builder.build();
                        }
                        // 使用 AiServices 动态构建接口代理
                        String systemMessage = loadSystemMessage(modelConfig);
                        AiServices<ChatStreamService> serviceBuilder = AiServices.builder(ChatStreamService.class)
                                .streamingChatModel(streamingChatModel)
                                .chatMemoryProvider(chatMemoryProvider)
                                .systemMessageProvider(memoryId -> systemMessage);

                        // 根据是否原生支持联网决定挂载哪些工具
                        if (modelConfig.getIsNet() != null && modelConfig.getIsNet() == 1) {
                            // 原生支持联网：只挂载 TaiServiceTool (RAG)，不挂载 WebSearchTool
                            serviceBuilder.tools(sqlTool, taiServiceTool);
                        } else {
//                            // 原生不支持联网：挂载 TaiServiceTool 和 WebSearchTool
//                            if (modelConfig.getProvider() != null && modelConfig.getProvider().equals("aliyun"))
//                                serviceBuilder.tools(taiServiceTool);
                            if (modelConfig.getProvider() != null && !modelConfig.getProvider().equals("aliyun"))
                                serviceBuilder.tools(sqlTool, taiServiceTool, webSearchTool);
                        }

                        ChatStreamService service = serviceBuilder.build();
                        serviceCache.put(modelConfig.getId(), service);
                    } catch (Throwable e) {
                        log.error("Error building ChatStreamService for model: " + modelConfig.getModelName(), e);
                    }
                });
            }
        } catch (Throwable e) {
            log.error("Fatal error during DynamicChatService initialization", e);
            // 抛出异常以便 Spring 能够捕获并停止启动，或者选择吞掉异常让应用继续运行（取决于业务要求）
            // 这里我们记录日志并重新抛出，因为没有 LLM 服务可能意味着核心功能不可用
            throw new RuntimeException("Failed to initialize DynamicChatService", e);
        }
    }

    /**
     * 根据 modelId 获取对应的 ChatStreamService
     *
     * @param modelId 模型ID，如果为 null 则使用默认启用的模型
     * @return ChatStreamService 实例
     */
    public ChatStreamService getService(Long modelId) {
        // 如果 modelId 为空，尝试查找默认模型（这里简化逻辑：找第一个启用的模型）
        if (modelId == null) {
            TLlmModel defaultModel = tLlmModelService.lambdaQuery()
                    .eq(TLlmModel::getIsEnable, 1)
                    .last("LIMIT 1")
                    .one();
            if (defaultModel != null) {
                modelId = defaultModel.getId();
            } else {
                throw new RuntimeException("No available LLM model configuration found.");
            }
        }
        // 检查缓存
        if (serviceCache.containsKey(modelId)) {
            return serviceCache.get(modelId);
        }

        // 构建新实例
        return buildAndCacheService(modelId);
    }

    public ChatStreamService getService(Long modelId, String systemMessageFileName) {
        if (systemMessageFileName == null || systemMessageFileName.isBlank()) {
            return getService(modelId);
        }

        if (modelId == null) {
            TLlmModel defaultModel = tLlmModelService.lambdaQuery()
                    .eq(TLlmModel::getIsEnable, 1)
                    .last("LIMIT 1")
                    .one();
            if (defaultModel != null) {
                modelId = defaultModel.getId();
            } else {
                throw new RuntimeException("No available LLM model configuration found.");
            }
        }

        String cacheKey = buildCacheKey(modelId, systemMessageFileName);
        if (serviceCacheBySystemMessage.containsKey(cacheKey)) {
            return serviceCacheBySystemMessage.get(cacheKey);
        }

        return buildAndCacheService(modelId, systemMessageFileName, cacheKey);
    }

    private synchronized ChatStreamService buildAndCacheService(Long modelId, String systemMessageFileName, String cacheKey) {
        if (serviceCacheBySystemMessage.containsKey(cacheKey)) {
            return serviceCacheBySystemMessage.get(cacheKey);
        }

        TLlmModel modelConfig = tLlmModelService.getById(modelId);
        if (modelConfig == null) {
            throw new RuntimeException("LLM model configuration not found for id: " + modelId);
        }

        if (modelConfig.getIsEnable() != 1) {
            throw new RuntimeException("LLM model is disabled: " + modelConfig.getModelName());
        }

        StreamingChatModel streamingChatModel;

        if ("aliyun".equals(modelConfig.getProvider())) {
            streamingChatModel = QwenStreamingChatModel.builder()
                    .apiKey(modelConfig.getApiKey())
                    .modelName(modelConfig.getModelCode())
                    .enableSearch(modelConfig.getIsNet() != null && modelConfig.getIsNet() == 1)
                    .build();
        } else {
            streamingChatModel = OpenAiStreamingChatModel.builder()
                    .apiKey(modelConfig.getApiKey())
                    .modelName(modelConfig.getModelCode())
                    .baseUrl(modelConfig.getBaseUrl())
                    .logRequests(false)
                    .logResponses(false)
                    .build();
        }

        String systemMessage = loadSystemMessage(modelConfig, systemMessageFileName);


        AiServices<ChatStreamService> serviceBuilder = AiServices.builder(ChatStreamService.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> systemMessage);
        //.toolProvider(toolProvider)

        if (modelConfig.getIsNet() != null && modelConfig.getIsNet() == 1) {
            serviceBuilder.tools(sqlTool, taiServiceTool);
        } else {
            serviceBuilder.tools(sqlTool, taiServiceTool, webSearchTool);
        }

        ChatStreamService service = serviceBuilder.build();
        serviceCacheBySystemMessage.put(cacheKey, service);
        return service;
    }

    private synchronized ChatStreamService buildAndCacheService(Long modelId) {
        // 双重检查
        if (serviceCache.containsKey(modelId)) {
            return serviceCache.get(modelId);
        }

        TLlmModel modelConfig = tLlmModelService.getById(modelId);
        if (modelConfig == null) {
            throw new RuntimeException("LLM model configuration not found for id: " + modelId);
        }

        if (modelConfig.getIsEnable() != 1) {
            throw new RuntimeException("LLM model is disabled: " + modelConfig.getModelName());
        }

        log.info("Building ChatStreamService for model: {} ({})", modelConfig.getModelName(), modelConfig.getModelCode());

        // 构建 StreamingChatLanguageModel
        // 假设所有支持的模型都兼容 OpenAI 协议 (如 Qwen, DeepSeek 等)
        StreamingChatModel streamingChatModel;

        if ("aliyun".equals(modelConfig.getProvider())) {
            streamingChatModel = QwenStreamingChatModel.builder()
                    .apiKey(modelConfig.getApiKey())
                    .modelName(modelConfig.getModelCode())
                    .enableSearch(modelConfig.getIsNet() != null && modelConfig.getIsNet() == 1)
                    .build();
        } else {
            streamingChatModel = OpenAiStreamingChatModel.builder()
                    .apiKey(modelConfig.getApiKey())
                    .modelName(modelConfig.getModelCode())
                    .baseUrl(modelConfig.getBaseUrl())
                    .logRequests(false)
                    .logResponses(false)
                    .build();
        }

        // 使用 AiServices 动态构建接口代理
        String systemMessage = loadSystemMessage(modelConfig);

//        ToolProvider toolProvider = McpToolProvider.builder()
//                .mcpClients(List.of(aliyunMcpClient))
//                .build();

        AiServices<ChatStreamService> serviceBuilder = AiServices.builder(ChatStreamService.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> systemMessage);
        //.toolProvider(toolProvider)

        if (modelConfig.getIsNet() != null && modelConfig.getIsNet() == 1) {
            serviceBuilder.tools(sqlTool, taiServiceTool);
        } else {
            serviceBuilder.tools(sqlTool, taiServiceTool, webSearchTool);
        }

        ChatStreamService service = serviceBuilder.build();
        serviceCache.put(modelId, service);
        return service;
    }

    private String loadSystemMessage(TLlmModel modelConfig) {
        try {
            ClassPathResource resource = new ClassPathResource("systemMessage.txt");
            if (resource.exists()) {
                return new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Failed to load systemMessage.txt from classpath, falling back to database config.", e);
        }

        if (modelConfig != null && modelConfig.getSystemMessageId() != null) {
            TSystemMessage sysMsg = tSystemMessageService.getById(modelConfig.getSystemMessageId());
            if (sysMsg != null && sysMsg.getContent() != null && !sysMsg.getContent().isEmpty()) {
                return sysMsg.getContent();
            }
        }

        return "You are a helpful AI assistant.";
    }

    private String loadSystemMessage(TLlmModel modelConfig, String systemMessageFileName) {
        if (systemMessageFileName != null && !systemMessageFileName.isBlank()) {
            String fileName = normalizeSystemMessageFileName(systemMessageFileName);
            try {
                ClassPathResource resource = new ClassPathResource(fileName);
                if (resource.exists()) {
                    return new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                log.warn("Failed to load {} from classpath, falling back to default.", fileName, e);
            }
        }

        return loadSystemMessage(modelConfig);
    }

    private String normalizeSystemMessageFileName(String systemMessageFileName) {
        String fileName = systemMessageFileName.trim();
        return fileName.endsWith(".txt") ? fileName : fileName + ".txt";
    }

    private String buildCacheKey(Long modelId, String systemMessageFileName) {
        return modelId + ":" + normalizeSystemMessageFileName(systemMessageFileName);
    }

    /**
     * 清除缓存（当配置更新时调用）
     *
     * @param modelId 模型ID
     */
    public void refreshCache(Long modelId) {
        serviceCache.remove(modelId);
        String prefix = modelId + ":";
        serviceCacheBySystemMessage.keySet().removeIf(key -> key.startsWith(prefix));
    }

    /**
     * 清除所有缓存
     */
    public void clearCache() {
        serviceCache.clear();
        serviceCacheBySystemMessage.clear();
    }
}
