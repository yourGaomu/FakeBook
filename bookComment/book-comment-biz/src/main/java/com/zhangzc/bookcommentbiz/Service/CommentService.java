package com.zhangzc.bookcommentbiz.Service;

import com.zhangzc.bookcommentbiz.Pojo.Vo.PublishCommentReqVO;
import com.zhangzc.bookcommon.Utils.R;

public interface CommentService {

    R<?> publishComment(PublishCommentReqVO publishCommentReqVO);
}
