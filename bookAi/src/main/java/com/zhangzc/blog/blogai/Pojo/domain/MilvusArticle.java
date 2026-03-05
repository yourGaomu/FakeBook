package com.zhangzc.blog.blogai.Pojo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilvusArticle {
    private String id;
    private String title;
    private String content;
    private String summary;

    private List<Float> title_vct;
    private List<Float> content_vct;
    private List<Float> summary_vct;
}
