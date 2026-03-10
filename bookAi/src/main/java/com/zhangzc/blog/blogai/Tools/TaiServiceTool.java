package com.zhangzc.blog.blogai.Tools;

import com.zhangzc.blog.blogai.Pojo.domain.MilvusArticle;
import com.zhangzc.blog.blogai.Pojo.domain.TLlmModel;
import com.zhangzc.blog.blogai.Retriever.MultiQueryRetriever;
import com.zhangzc.blog.blogai.Service.TLlmModelService;
import com.zhangzc.booksearchapi.Api.SearchNoteFeginApi;
import com.zhangzc.booksearchapi.Pojo.Dto.Resp.SearchNoteRspVO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaiServiceTool {

    private final SearchNoteFeginApi searchNoteFeginApi;
    private final TLlmModelService tLlmModelService;
    private MultiQueryRetriever multiQueryRetriever;

    @PostConstruct
    public void init() {
        try {
            // 1. 获取一个可用的文本模型用于生成查询
            List<TLlmModel> models = tLlmModelService.lambdaQuery()
                    .eq(TLlmModel::getIsEnable, 1)
                    .like(TLlmModel::getModelCode, "text")
                    .list();

            if (models.isEmpty()) {
                log.warn("No enabled text model found for MultiQueryRetriever initialization.");
                return;
            }

            // 优先选择 Qwen 或其他强大的模型
            TLlmModel selectedModel = models.stream()
                    .filter(m -> m.getModelCode().toLowerCase().contains("qwen"))
                    .findFirst()
                    .orElse(models.get(0));

            ChatModel chatModel;
            if ("aliyun".equalsIgnoreCase(selectedModel.getProvider())) {
                chatModel = QwenChatModel.builder()
                        .apiKey(selectedModel.getApiKey())
                        .modelName(selectedModel.getModelCode())
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
            //2.1构建web检索器
            ContentRetriever webContentRetriever = builWebContentRetriever();
            //2.2构建milvus检索器
            ContentRetriever milvusContentRetriever = builMilvusContentRetriever();
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

    private ContentRetriever builMilvusContentRetriever() {
        //



    }

    private ContentRetriever builWebContentRetriever() {
        //这里复用之前的聊天模型




    }

    private Content convertToContent(SearchNoteRspVO note) {
        Metadata metadata = new Metadata();
        if (note.getNoteId() != null) metadata.put("id", String.valueOf(note.getNoteId()));
        if (note.getTitle() != null) metadata.put("title", note.getTitle());
        if (note.getCover() != null) metadata.put("cover", note.getCover());

        // 简单组合内容，如果有更多字段可以添加
        String text = "Title: " + note.getTitle() + "\n";
        if (note.getHighlightTitle() != null) {
            text += "Highlight: " + note.getHighlightTitle() + "\n";
        }

        return Content.from(text);
    }

    @Tool("这是内部知识库，根据用户想要查找的相关学术文献进行查找,其中这个函数的返回值，将会忽略掉向量数据，只会得到具体的对象数据")
    public List<MilvusArticle> findArt(@P("用户输入的相关的关键词") String keyWord,
                                       @P("用户想要查找的文献的索引,比如“title_vct，content_vct,summary_vct，这三个其中一个") String index) {
        log.info("Tool findArt called with keyword: {}, index: {}", keyWord, index);

        if (multiQueryRetriever == null) {
            log.warn("MultiQueryRetriever not initialized, falling back to empty list.");

            return Collections.emptyList();
        }

        try {
            // 使用 MultiQueryRetriever 进行检索
            List<Content> contents = multiQueryRetriever.retrieve(Query.from(keyWord));

            log.info("MultiQueryRetriever found {} results", contents.size());

            return contents.stream().map(content -> {
                MilvusArticle article = new MilvusArticle();
                Metadata metadata = content.textSegment().metadata();

                article.setId(metadata.getString("id"));
                article.setTitle(metadata.getString("title"));
                article.setCover(metadata.getString("cover"));
                article.setContent(content.textSegment().text());
                // article.setSummary(...); // 如果有摘要

                return article;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in findArt tool", e);
        }
        return Collections.emptyList();
    }
}

