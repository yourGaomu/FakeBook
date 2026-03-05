package com.zhangzc.blog.blogai.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.blog.blogai.Pojo.domain.TArticle;
import com.zhangzc.blog.blogai.Service.TArticleService;
import com.zhangzc.blog.blogai.Mapper.TArticleMapper;
import org.springframework.stereotype.Service;

/**
* @author 吃饭
* @description 针对表【t_article(文章表)】的数据库操作Service实现
* @createDate 2025-10-17 16:40:17
*/
@Service
public class TArticleServiceImpl extends ServiceImpl<TArticleMapper, TArticle>
    implements TArticleService{

}




