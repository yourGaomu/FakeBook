package com.zhangzc.bookcommentbiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.bookcommentbiz.Mapper.CommentContentMapper;
import com.zhangzc.bookcommentbiz.Service.CommentContentService;
import com.zhangzc.bookcommentbiz.Pojo.Domain.CommentContent;
import org.springframework.stereotype.Service;

/**
* @author 吃饭
* @description 针对表【comment_content(存储笔记的评论内容)】的数据库操作Service实现
* @createDate 2025-10-04 11:43:33
*/
@Service
public class CommentContentServiceImpl extends ServiceImpl<CommentContentMapper, CommentContent>
    implements CommentContentService {

}




