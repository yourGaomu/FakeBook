package com.zhangzc.bookkvbiz.Service;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.BatchFindCommentContentReqDTO;
import org.springframework.cloud.client.loadbalancer.Response;


public interface CommentService {


    /**
     * 批量查询评论内容
     * @param batchFindCommentContentReqDTO
     * @return
     */
    R<?> batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO);

}

