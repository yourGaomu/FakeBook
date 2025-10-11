package com.zhangzc.bookcommentbiz.Service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Preconditions;
import com.zhangzc.bookcommentbiz.Const.RabbitConstants;
import com.zhangzc.bookcommentbiz.Pojo.Domain.TComment;
import com.zhangzc.bookcommentbiz.Pojo.Dto.PublishCommentMqDTO;
import com.zhangzc.bookcommentbiz.Pojo.Vo.*;
import com.zhangzc.bookcommentbiz.Rpc.DistributedIdGeneratorRpcService;
import com.zhangzc.bookcommentbiz.Rpc.KeyValueRpcService;
import com.zhangzc.bookcommentbiz.Rpc.UserRpcService;
import com.zhangzc.bookcommentbiz.Service.CommentService;
import com.zhangzc.bookcommentbiz.Service.TCommentService;
import com.zhangzc.bookcommentbiz.Utils.RabbitMqUtil;
import com.zhangzc.bookcommentbiz.Utils.SendMqRetryHelper;
import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindCommentContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindCommentContentRspDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final SendMqRetryHelper sendMqRetryHelper;
    private final DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    private final RabbitMqUtil rabbitMqUtil;
    private final TCommentService tCommentService;
    private final KeyValueRpcService keyValueRpcService;
    private final UserRpcService userRpcService;

    @Override
    public R<?> publishComment(PublishCommentReqVO publishCommentReqVO) {
        // 评论正文
        String content = publishCommentReqVO.getContent();
        // 附近图片
        String imageUrl = publishCommentReqVO.getImageUrl();

        //发布者的id
        Long userId = 103L;

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

    @Override
    public PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO) {

        //获取笔记id
        Long noteId = findCommentPageListReqVO.getNoteId();
        if (noteId == null) {
            return PageResponse.success(null, 0L, 0L);
        }
        //构建返回内容体
        List<FindCommentItemRspVO> result = new ArrayList<>();
        //获取页码
        Integer page = findCommentPageListReqVO.getPageNo();
        if (page < 1) {
            page = 1;
        }
        // 每页展示一级评论数
        long pageSize = 10;
        //查询一级评论
        IPage<TComment> page1 = new Page<>(page, pageSize);
        LambdaQueryWrapper<TComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TComment::getNoteId, noteId)
                .eq(TComment::getLevel, 1)
                .orderByDesc(TComment::getCreateTime);
        IPage<TComment> page2 = tCommentService.page(page1, queryWrapper);
        List<TComment> records = page2.getRecords();
        if (records.isEmpty()) {
            return PageResponse.success(null, 0L, 0L);
        }
        //获取一级评论的ID
        List<Long> commentIds = records.stream().map(TComment::getId).toList();
        //获取最早二级评论的回复的ID
        List<Long> firstReplyCommentIds = records.stream().map(TComment::getFirstReplyCommentId).toList();
        //获取用户ID
        List<Long> userIds = records.stream().map(TComment::getUserId).toList();
        //获取评论的内容
        List<FindCommentContentReqDTO> findCommentContentReqDTOList = records.stream().map(tComment -> {
            FindCommentContentReqDTO findCommentContentReqDTO = new FindCommentContentReqDTO();
            findCommentContentReqDTO.setYearMonth(TimeUtil.formatToYearMonth(tComment.getCreateTime()));
            findCommentContentReqDTO.setContentId(tComment.getContentUuid());
            return findCommentContentReqDTO;
        }).toList();
        Map<String, FindCommentContentRspDTO> collect1 = keyValueRpcService.batchFindCommentContent(noteId, findCommentContentReqDTOList).stream()
                .collect(Collectors.toMap(FindCommentContentRspDTO::getContentId, p -> p));
        //查询对应的用户信息
        Map<Long, FindUserByIdRspDTO> collect = userRpcService.findByIds(userIds)
                .stream().collect(Collectors.toMap(FindUserByIdRspDTO::getId, p -> p));
        //查询二级评论
        Map<Long, TComment> secondCommentMap = tCommentService.lambdaQuery().in(TComment::getId, firstReplyCommentIds).list()
                .stream().collect(Collectors.toMap(TComment::getId, p -> p));

        //开始优先构建一级评论赋值
        records.forEach(tComment -> {
            FindCommentItemRspVO findCommentItemRspVO = new FindCommentItemRspVO();
            findCommentItemRspVO.setCommentId(tComment.getId());
            findCommentItemRspVO.setUserId(tComment.getUserId());
            findCommentItemRspVO.setAvatar(collect.get(tComment.getUserId()).getAvatar());
            findCommentItemRspVO.setNickname(collect.get(tComment.getUserId()).getNickName());
            findCommentItemRspVO.setContent(collect1.get(tComment.getContentUuid()).getContent());
            findCommentItemRspVO.setImageUrl(tComment.getImageUrl());
            findCommentItemRspVO.setCreateTime(tComment.getCreateTime().toString());
            findCommentItemRspVO.setLikeTotal(tComment.getLikeTotal());
            findCommentItemRspVO.setChildCommentTotal(tComment.getChildCommentTotal());
            findCommentItemRspVO.setFirstReplyComment(secondCommentMap.isEmpty()
                    ? null
                    : getFirstReplyComment(secondCommentMap.get(tComment.getFirstReplyCommentId()), collect1, collect));
            result.add(findCommentItemRspVO);
        });
        return PageResponse.success(result, page2.getCurrent(), page2.getTotal());
    }

    @Override
    public PageResponse<FindChildCommentItemRspVO> findChildCommentPageList(FindChildCommentPageListReqVO findChildCommentPageListReqVO) {
        //获取一级评论id
        Long commentId = findChildCommentPageListReqVO.getParentCommentId();
        if (commentId == null) {
            return PageResponse.success(null, 0L, 0L);
        }
        //获取页码
        Integer page = findChildCommentPageListReqVO.getPageNo();
        if (page < 1) {
            page = 1;
        }
        // 每页展示的二级评论数 (小红书 APP 中是一次查询 6 条)
        long pageSize = 6;
        IPage<TComment> page1 = new Page<>(page, pageSize);
        LambdaQueryWrapper<TComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TComment::getParentId, commentId)
                .eq(TComment::getLevel, 2)
                .orderByDesc(TComment::getCreateTime);
        IPage<TComment> page2 = tCommentService.page(page1, queryWrapper);
        List<TComment> records = page2.getRecords();
        if (records.isEmpty()) {
            return PageResponse.success(null, 0L, 0L);
        }
        //获取用户ID
        List<Long> userIds = records.stream().map(TComment::getUserId).toList();
        //获取评论的内容
        List<FindCommentContentReqDTO> findCommentContentReqDTOList = records.stream().map(tComment -> {
            FindCommentContentReqDTO findCommentContentReqDTO = new FindCommentContentReqDTO();
            findCommentContentReqDTO.setYearMonth(TimeUtil.formatToYearMonth(tComment.getCreateTime()));
            findCommentContentReqDTO.setContentId(tComment.getContentUuid());
            return findCommentContentReqDTO;
        }).toList();
        Map<String, FindCommentContentRspDTO> collect1 = keyValueRpcService.batchFindCommentContent(records.get(0).getNoteId(), findCommentContentReqDTOList).stream()
                .collect(Collectors.toMap(FindCommentContentRspDTO::getContentId, p -> p));
        //查询对应的用户信息
        Map<Long, FindUserByIdRspDTO> collect = userRpcService.findByIds(userIds)
                .stream().collect(Collectors.toMap(FindUserByIdRspDTO::getId, p -> p));
        //开始构建返回体
        List<FindChildCommentItemRspVO> result = new ArrayList<>();
        records.forEach(tComment -> {
            FindChildCommentItemRspVO findChildCommentItemRspVO = new FindChildCommentItemRspVO();
            findChildCommentItemRspVO.setCommentId(tComment.getId());
            findChildCommentItemRspVO.setUserId(tComment.getUserId());
            findChildCommentItemRspVO.setAvatar(collect.get(tComment.getUserId()).getAvatar());
            findChildCommentItemRspVO.setNickname(collect.get(tComment.getUserId()).getNickName());
            findChildCommentItemRspVO.setContent(collect1.get(tComment.getContentUuid()).getContent());
            findChildCommentItemRspVO.setImageUrl(tComment.getImageUrl());
            findChildCommentItemRspVO.setCreateTime(tComment.getCreateTime().toString());
            findChildCommentItemRspVO.setLikeTotal(tComment.getLikeTotal());
            findChildCommentItemRspVO.setReplyUserName(collect.get(tComment.getReplyUserId()).getNickName());
            findChildCommentItemRspVO.setReplyUserId(tComment.getReplyUserId());
            result.add(findChildCommentItemRspVO);
        });
        return PageResponse.success(result, page2.getCurrent(), page2.getTotal());

    }

    private FindCommentItemRspVO getFirstReplyComment(TComment tComment, Map<String, FindCommentContentRspDTO> collect1, Map<Long, FindUserByIdRspDTO> collect) {
        if (tComment == null) {
            return null;
        }
        FindCommentItemRspVO findCommentItemRspVO = new FindCommentItemRspVO();
        findCommentItemRspVO.setCommentId(tComment.getId());
        findCommentItemRspVO.setUserId(tComment.getUserId());
        findCommentItemRspVO.setAvatar(collect.get(tComment.getUserId()).getAvatar());
        findCommentItemRspVO.setNickname(collect.get(tComment.getUserId()).getNickName());
        findCommentItemRspVO.setContent(collect1.get(tComment.getContentUuid()).getContent());
        findCommentItemRspVO.setImageUrl(tComment.getImageUrl());
        findCommentItemRspVO.setCreateTime(tComment.getCreateTime().toString());
        findCommentItemRspVO.setLikeTotal(tComment.getLikeTotal());
        return findCommentItemRspVO;
    }

}
