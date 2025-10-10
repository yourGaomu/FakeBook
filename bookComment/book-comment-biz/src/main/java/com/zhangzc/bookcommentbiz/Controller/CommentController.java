package com.zhangzc.bookcommentbiz.Controller;


import com.zhangzc.bookcommentbiz.Pojo.Vo.PublishCommentReqVO;
import com.zhangzc.bookcommentbiz.Service.CommentService;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping("/publish")
    @ApiOperationLog(description = "发布评论")
    public R<?> publishComment(@Validated @RequestBody PublishCommentReqVO publishCommentReqVO) {
        return commentService.publishComment(publishCommentReqVO);
    }




}

