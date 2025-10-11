package com.zhangzc.bookcommentbiz.Mapper;

import com.zhangzc.bookcommentbiz.Pojo.Domain.TComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author 吃饭
* @description 针对表【t_comment(评论表)】的数据库操作Mapper
* @createDate 2025-10-10 19:49:53
* @Entity com.zhangzc.bookcommentbiz.Pojo.Domain.TComment
*/
public interface TCommentMapper extends BaseMapper<TComment> {

    void updateChindCommentTotal(@Param("parentCommentMap") Map<Long, Long> parentCommentMap);

    void updateFirstReplyCommentId(@Param("replyIdMap") Map<Long, Long> replyIdMap);
}




