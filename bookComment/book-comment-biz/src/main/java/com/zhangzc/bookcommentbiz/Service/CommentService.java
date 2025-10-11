package com.zhangzc.bookcommentbiz.Service;

import com.zhangzc.bookcommentbiz.Pojo.Vo.*;
import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;

public interface CommentService {

    R<?> publishComment(PublishCommentReqVO publishCommentReqVO);

    PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO);

    PageResponse<FindChildCommentItemRspVO> findChildCommentPageList(FindChildCommentPageListReqVO findChildCommentPageListReqVO);
}
