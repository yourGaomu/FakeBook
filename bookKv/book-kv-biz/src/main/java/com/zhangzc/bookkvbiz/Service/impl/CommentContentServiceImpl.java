package com.zhangzc.bookkvbiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.bookkvbiz.Pojo.Domain.CommentContent;
import com.zhangzc.bookkvbiz.Service.CommentContentService;
import com.zhangzc.bookkvbiz.Mapper.CommentContentMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
* @author 吃饭
* @description 针对表【comment_content(存储笔记的评论内容)】的数据库操作Service实现
* @createDate 2025-10-10 19:30:42
*/
@Service
public class CommentContentServiceImpl extends ServiceImpl<CommentContentMapper, CommentContent>
    implements CommentContentService{

    @Override
    public List<CommentContent> findByPrimaryKeyNoteIdAndPrimaryKeyYearMonthInAndPrimaryKeyContentIdIn(Long noteId, List<String> yearMonths, List<String> contentIds) {
        return baseMapper.batchFindCommentContent(noteId, yearMonths, contentIds);
    }

}




