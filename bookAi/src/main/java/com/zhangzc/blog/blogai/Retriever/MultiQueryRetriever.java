package com.zhangzc.blog.blogai.Retriever;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Builder
public class MultiQueryRetriever implements ContentRetriever {

    private static final String DEFAULT_PROMPT_TEMPLATE =
            "You are an AI language model assistant. Your task is to generate %d different versions of the given user question to retrieve relevant documents from a vector database. " +
                    "By generating multiple perspectives on the user question, your goal is to help the user overcome some of the limitations of the distance-based similarity search. " +
                    "Provide these alternative questions separated by newlines. Original question: %s";
    private final ChatModel chatModel;
    private final ContentRetriever webContentRetriever;
    private final ContentRetriever milvusContentRetriever;
    @Builder.Default
    private final int queryCount = 3;
    // 实际执行检索的检索器
    private Map<ContentRetriever, String> retrieverMap;
    private QueryRouter queryRouter;

    @Override
    public List<Content> retrieve(Query query) {
        String originalQuery = query.text();

        // 1. 生成多重查询
        List<String> queries = generateQueries(originalQuery);
        queries.add(originalQuery); // 别忘了保留原始查询

        log.info("Generated queries: {}", queries);

        // 2. 并行执行检索
        retrieverMap.put(webContentRetriever, "它是一个基于互联网搜索的查询，具有实时性的数据可以根据它来检索");
        retrieverMap.put(milvusContentRetriever, "它是一个基于嵌入数据库的查询，关于知识库的内容可以根据它来检索");

        queryRouter = new LanguageModelQueryRouter(chatModel, retrieverMap);

        List<CompletableFuture<Collection<ContentRetriever>>> futures = queries.stream()
                .map(q -> CompletableFuture.supplyAsync(() -> queryRouter.route(Query.from(q))))
                .toList();

        // 3. 等待结果并去重合并
        Set<String> uniqueContent = new HashSet<>();
        List<Content> mergedResults = new ArrayList<>();

        for (CompletableFuture<Collection<ContentRetriever>> future : futures) {
            try {
                Collection<ContentRetriever> contents = future.join();
                if (contents != null) {
                    List<Content> retrieve = contents.iterator().next().retrieve(query);
                    mergedResults.addAll(retrieve);
                }
            } catch (Exception e) {
                log.error("Error during retrieval for query", e);
            }
        }

        return mergedResults;
    }

    private List<String> generateQueries(String originalQuery) {
        try {
            String prompt = String.format(DEFAULT_PROMPT_TEMPLATE, queryCount, originalQuery);
            String response = chatModel.chat(prompt);

            // 简单按行分割
            return Arrays.stream(response.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to generate queries", e);
            return new ArrayList<>();
        }
    }
}
