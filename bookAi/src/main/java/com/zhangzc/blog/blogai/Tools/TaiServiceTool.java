package com.zhangzc.blog.blogai.Tools;

import com.zhangzc.blog.blogai.Pojo.domain.TLlmModel;
import com.zhangzc.blog.blogai.Retriever.MultiQueryRetriever;
import com.zhangzc.blog.blogai.Service.TLlmModelService;
import com.zhangzc.blog.blogai.Tools.ContentRetriever.MilvusContentRetriever;
import com.zhangzc.blog.blogai.Tools.ContentRetriever.WebContentRetriever;
import com.zhangzc.booksearchapi.Api.SearchNoteFeginApi;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaiServiceTool {

    private final SearchNoteFeginApi searchNoteFeginApi;
    private final TLlmModelService tLlmModelService;
    private final MilvusContentRetriever milvusContentRetriever;
    private final WebContentRetriever webContentRetriever;
    private MultiQueryRetriever multiQueryRetriever;

    @PostConstruct
    public void init() {
        try {
            // 1. 获取一个可用的文本模型用于生成查询
            TLlmModel selectedModel = tLlmModelService.lambdaQuery()
                    .eq(TLlmModel::getIsEnable, 1)
                    .like(TLlmModel::getModelType, "text").last("LIMIT 1")
                    .one();


            // 优先选择 Qwen 或其他强大的模型

            ChatModel chatModel;
            if ("aliyun".equalsIgnoreCase(selectedModel.getProvider())) {
                chatModel = QwenChatModel.builder()
                        .apiKey(selectedModel.getApiKey())
                        .modelName(selectedModel.getModelCode())
                        //.baseUrl(selectedModel.getBaseUrl())
                        //阿里云的百炼可以不需要baseUrl自动填写
                        //.baseUrl(selectedModel.getBaseUrl()) // Qwen SDK uses base URL if needed
                        .build();

            } else {
                chatModel = OpenAiChatModel.builder()
                        .apiKey(selectedModel.getApiKey())
                        .baseUrl(selectedModel.getBaseUrl())
                        .modelName(selectedModel.getModelCode())
                        .timeout(Duration.ofSeconds(60))
                        .build();
            }

            // 2. 定义基础检索器
            // 3. 构建 MultiQueryRetriever
            this.multiQueryRetriever = MultiQueryRetriever.builder()
                    .chatModel(chatModel)
                    .webContentRetriever(webContentRetriever)
                    .milvusContentRetriever(milvusContentRetriever)
                    .queryCount(3)
                    .build();

            log.info("MultiQueryRetriever initialized with model: {}", selectedModel.getModelName());
        } catch (Exception e) {
            log.error("Failed to initialize MultiQueryRetriever", e);
        }
    }

    @Tool("这是内部知识库，根据用户想要查找的相关学术文献进行查找,其中这个函数的返回值，将会忽略掉向量数据，只会得到具体的对象数据")
    public String findArt(@P("用户输入的相关的关键词") String keyWord) {

        if (multiQueryRetriever == null) {
            log.warn("MultiQueryRetriever not initialized, falling back to empty list.");
            return Collections.emptyList().toString();
        }
        try {
            // 使用 MultiQueryRetriever 进行检索
            List<Content> contents = multiQueryRetriever.retrieve(Query.from(keyWord));

            log.info("MultiQueryRetriever found {} results", contents.size());

            return contents.toString();

        } catch (Exception e) {
            log.error("Error in findArt tool", e);
        }
        return Collections.emptyList().toString();
    }
}

