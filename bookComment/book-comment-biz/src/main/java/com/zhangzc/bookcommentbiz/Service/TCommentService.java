package com.zhangzc.bookcommentbiz.Service;

import com.zhangzc.bookcommentbiz.Pojo.Domain.TComment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author 吃饭
* @description 针对表【t_comment(评论表)】的数据库操作Service
* @createDate 2025-10-10 19:49:53
*/
public interface TCommentService extends IService<TComment> {

    void updateChindCommentTotal(Map<Long, Long> parentCommentMap);

    void updateFirstReplyCommentId(Map<Long, Long> replyIdMap);
}
