package com.zhangzc.bookcommentbiz.Service.impl;

import cn.hutool.core.date.DateTime;
import com.google.common.base.Preconditions;
import com.zhangzc.bookcommentbiz.Const.RabbitConstants;
import com.zhangzc.bookcommentbiz.Pojo.Dto.PublishCommentMqDTO;
import com.zhangzc.bookcommentbiz.Pojo.Vo.PublishCommentReqVO;
import com.zhangzc.bookcommentbiz.Rpc.DistributedIdGeneratorRpcService;
import com.zhangzc.bookcommentbiz.Service.CommentService;
import com.zhangzc.bookcommentbiz.Utils.RabbitMqUtil;
import com.zhangzc.bookcommentbiz.Utils.SendMqRetryHelper;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final SendMqRetryHelper sendMqRetryHelper;
    private final DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    private final RabbitMqUtil rabbitMqUtil;

    @Override
    public R<?> publishComment(PublishCommentReqVO publishCommentReqVO) {
        // 评论正文
        String content = publishCommentReqVO.getContent();
        // 附近图片
        String imageUrl = publishCommentReqVO.getImageUrl();

        //发布者的id
        Long userId = LoginUserContextHolder.getUserId();

        //调用分布式生成评论id
        String commentId = distributedIdGeneratorRpcService.getCommentId();

        // 评论内容和图片不能同时为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl),
                "评论正文和图片不能同时为空");

        PublishCommentMqDTO publishCommentMqDTO = new PublishCommentMqDTO(
        );
        publishCommentMqDTO.setCommentId(Long.valueOf(commentId));
        publishCommentMqDTO.setNoteId(publishCommentReqVO.getNoteId());
        publishCommentMqDTO.setContent(publishCommentReqVO.getContent());
        publishCommentMqDTO.setImageUrl(publishCommentReqVO.getImageUrl());
        publishCommentMqDTO.setCreateTime(TimeUtil.getLocalDateTime(DateTime.now()));
        publishCommentMqDTO.setCreatorId(userId == null ? 103L : userId);
        publishCommentMqDTO.setReplyCommentId(publishCommentReqVO.getReplyCommentId());
        //发送消息
        CompletableFuture.runAsync(() -> {
            rabbitMqUtil.send("comment.exchange", RabbitConstants.TOPIC_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));
        });

        return R.success();
    }
}
