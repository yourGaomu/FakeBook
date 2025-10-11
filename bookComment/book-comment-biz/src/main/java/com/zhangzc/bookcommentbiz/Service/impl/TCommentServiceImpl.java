package com.zhangzc.bookcommentbiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.bookcommentbiz.Pojo.Domain.TComment;
import com.zhangzc.bookcommentbiz.Service.TCommentService;
import com.zhangzc.bookcommentbiz.Mapper.TCommentMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
* @author 吃饭
* @description 针对表【t_comment(评论表)】的数据库操作Service实现
* @createDate 2025-10-10 19:49:53
*/
@Service
public class TCommentServiceImpl extends ServiceImpl<TCommentMapper, TComment>
    implements TCommentService{

    @Override
    public void updateChindCommentTotal(Map<Long, Long> parentCommentMap) {
        this.baseMapper.updateChindCommentTotal(parentCommentMap);
    }

    @Override
    public void updateFirstReplyCommentId(Map<Long, Long> replyIdMap) {
        this.baseMapper.updateFirstReplyCommentId(replyIdMap);
    }
}




