package com.zhangzc.blog.blogai.Tools.ContentRetriever;

import com.zhangzc.blog.blogai.Mapper.MilvusMapper.FakeBookNotesMilvusMapper;
import com.zhangzc.blog.blogai.Pojo.milvus.FakeBookNotes;
import com.zhangzc.milvusspringbootstart.rerank.service.RerankModelService;
import com.zhangzc.milvusspringbootstart.utills.EmbeddingUtil;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Component

public class MilvusContentRetriever implements ContentRetriever {

    private final RerankModelService rerankModelService;
    private final FakeBookNotesMilvusMapper fakeBookNotesMilvusMapper;
    private final EmbeddingUtil embeddingUtil;

    @Override
    public List<Content> retrieve(Query query) {
        //获取内容
        String text = query.text();
        //先把问题向量化
        List<Float> embed = embeddingUtil.embed(text);
        //查询知识库
        //这个是根据内容查询
        CompletableFuture<MilvusResp<List<MilvusResult<FakeBookNotes>>>> milvusRespCompletableFuture = CompletableFuture.supplyAsync(() -> fakeBookNotesMilvusMapper.queryWrapper()
                .vector(FakeBookNotes::getContent_vct, embed)
                .gt(FakeBookNotes::getVisible, 0)
                .topK(10)
                .query());

        CompletableFuture<MilvusResp<List<MilvusResult<FakeBookNotes>>>> milvusRespCompletableFuture2 = CompletableFuture.supplyAsync(() -> fakeBookNotesMilvusMapper.queryWrapper()
                .vector(FakeBookNotes::getTitle_vct, embed)
                .gt(FakeBookNotes::getVisible, 0)
                .topK(10)
                .query());
        //获取数据
        try {
            MilvusResp<List<MilvusResult<FakeBookNotes>>> listMilvusResp = milvusRespCompletableFuture.get();
            MilvusResp<List<MilvusResult<FakeBookNotes>>> listMilvusResp1 = milvusRespCompletableFuture2.get();
            //再去把数据拿去重排序

            List<MilvusResult<FakeBookNotes>> data = listMilvusResp.getData() != null ? new ArrayList<>(listMilvusResp.getData()) : new ArrayList<>();
            List<MilvusResult<FakeBookNotes>> data1 = listMilvusResp1.getData() != null ? listMilvusResp1.getData() : List.of();

            data.addAll(data1);
            if (data.isEmpty()) {
                return List.of();
            }
            //进行重排序
            List<TextSegment> segments = data.stream().map(item -> {
                FakeBookNotes entity = item.getEntity();
                String result = "文章的内容是：" + entity.getContent() + "文章的标题是：" + entity.getTitle();
                return TextSegment.textSegment(result);
            }).toList();

            //获取具体的重排序打分
            Response<List<Double>> listResponse = rerankModelService.getScoringModel(4L)
                    .scoreAll(segments, query.text());
            //我们需要返回得分前10个数据
            List<Double> content = listResponse.content();

            List<AbstractMap.SimpleEntry<MilvusResult<FakeBookNotes>, Double>> scoredData = new ArrayList<>();
            for (int i = 0; i < content.size(); i++) {
                scoredData.add(new AbstractMap.SimpleEntry<>(data.get(i), content.get(i)));
            }

            scoredData.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

            List<Content> results = new ArrayList<>();
            Set<String> seenIds = new HashSet<>();

            for (AbstractMap.SimpleEntry<MilvusResult<FakeBookNotes>, Double> entry : scoredData) {
                if (results.size() >= 10) {
                    break;
                }
                FakeBookNotes entity = entry.getKey().getEntity();
                if (entity != null && seenIds.add(entity.getId())) {
                    String result = "文章的内容是：" + entity.getContent() + "文章的标题是：" + entity.getTitle();
                    Metadata metadata = new Metadata();
                    metadata.put("id", entity.getId());
                    metadata.put("title", entity.getTitle());
                    results.add(Content.from(TextSegment.from(result, metadata)));
                }
            }

            return results;

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
