package com.zhangzc.blog.blogai.Mapper;

import com.zhangzc.blog.blogai.Pojo.domain.TArticleContent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 吃饭
* @description 针对表【t_article_content(文章内容表)】的数据库操作Mapper
* @createDate 2025-10-17 16:40:17
* @Entity com.zhangzc.blog.blogai.Pojo.domain.TArticleContent
*/
public interface TArticleContentMapper extends BaseMapper<TArticleContent> {

    String getAticleContent(String articleId);
}




