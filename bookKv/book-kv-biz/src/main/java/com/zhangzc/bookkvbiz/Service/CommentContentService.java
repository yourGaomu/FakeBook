package com.zhangzc.bookkvbiz.Service;

import com.zhangzc.bookkvbiz.Pojo.Domain.CommentContent;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.UUID;

/**
* @author 吃饭
* @description 针对表【comment_content(存储笔记的评论内容)】的数据库操作Service
* @createDate 2025-10-10 19:30:42
*/
public interface CommentContentService extends IService<CommentContent> {

    List<CommentContent> findByPrimaryKeyNoteIdAndPrimaryKeyYearMonthInAndPrimaryKeyContentIdIn(Long noteId, List<String> yearMonths, List<String> contentIds);
}
