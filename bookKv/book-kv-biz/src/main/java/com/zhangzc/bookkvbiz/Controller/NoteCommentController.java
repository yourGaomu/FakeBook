package com.zhangzc.bookkvbiz.Controller;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.BatchFindCommentContentReqDTO;
import com.zhangzc.bookkvbiz.Service.CommentService;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kv")
@RequiredArgsConstructor
public class NoteCommentController {

    private final CommentService commentContentService;

    @PostMapping(value = "/comment/content/batchFind")
    @ApiOperationLog(description = "批量查询评论内容")
    public R<?> batchFindCommentContent(@Validated @RequestBody BatchFindCommentContentReqDTO batchFindCommentContentReqDTO) {
        return commentContentService.batchFindCommentContent(batchFindCommentContentReqDTO);
    }

}
