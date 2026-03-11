package com.zhangzc.blog.blogai.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.blog.blogai.Pojo.domain.TArticleContent;
import com.zhangzc.blog.blogai.Service.TArticleContentService;
import com.zhangzc.blog.blogai.Mapper.TArticleContentMapper;
import org.springframework.stereotype.Service;

/**
* @author 吃饭
* @description 针对表【t_article_content(文章内容表)】的数据库操作Service实现
* @createDate 2025-10-17 16:40:17
*/
@Service
public class TArticleContentServiceImpl extends ServiceImpl<TArticleContentMapper, TArticleContent>
    implements TArticleContentService{

    public String getAticleContent(String articleId){
        return this.baseMapper.getAticleContent(articleId);
    }


}




