package com.zhangzc.blog.blogai.Tools;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaiServiceTool {

//    @DubboReference(check = false, providedBy = "share-thing-search-impl-rpc")
//    private EsRpc esRpc;
//
//    @Tool("这是内部知识库，根据用户想要查找的相关学术文献进行查找,其中这个函数的返回值，将会忽略掉向量数据，只会得到具体的对象数据")
//    public List<MilvusArticle> findArt(@P("用户输入的相关的关键词") String keyWord,
//                                       @P("用户想要查找的文献的索引,比如“title_vct，content_vct,summary_vct，这三个其中一个") String index) {
//        log.info("Tool findArt called with keyword: {}, index: {}", keyWord, index);
//        try {
//            // 使用混合检索 (Hybrid Search)
//            List<EsArticleDto> rpcResults = esRpc.hybridSearch(keyWord);
//
//            if (rpcResults != null && !rpcResults.isEmpty()) {
//                log.info("Tool findArt found {} results via RPC", rpcResults.size());
//                // 将DTO转换为本地对象
//                return rpcResults.stream().map(dto -> {
//                    MilvusArticle article = new MilvusArticle();
//                    // 只复制必要的文本字段，忽略向量数据
//                    BeanUtils.copyProperties(dto, article);
//                    article.setContent(dto.getContentHtmlPlain());
//                    return article;
//                }).collect(Collectors.toList());
//            }
//        } catch (Exception e) {
//            log.error("Error in findArt tool via RPC", e);
//        }
//        return Collections.emptyList();
//    }
}

