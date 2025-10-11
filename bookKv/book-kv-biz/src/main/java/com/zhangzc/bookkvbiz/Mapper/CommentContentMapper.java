package com.zhangzc.bookkvbiz.Mapper;

import com.zhangzc.bookkvbiz.Pojo.Domain.CommentContent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
* @author 吃饭
* @description 针对表【comment_content(存储笔记的评论内容)】的数据库操作Mapper
* @createDate 2025-10-10 19:30:42
* @Entity com.zhangzc.bookkvbiz.Pojo.Domain.CommentContent
*/
public interface CommentContentMapper extends BaseMapper<CommentContent> {

    // 修正后：方法名与 XML 中 id 一致
    List<CommentContent> batchFindCommentContent(
            @Param("noteId") Long noteId,
            @Param("yearMonths") List<String> yearMonths,
            @Param("contentIds") List<String> contentIds
    );

}




