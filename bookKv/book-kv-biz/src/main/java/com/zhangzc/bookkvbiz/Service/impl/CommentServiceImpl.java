package com.zhangzc.bookkvbiz.Service.impl;


import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.BatchFindCommentContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindCommentContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindCommentContentRspDTO;
import com.zhangzc.bookkvbiz.Pojo.Domain.CommentContent;
import com.zhangzc.bookkvbiz.Service.CommentContentService;
import com.zhangzc.bookkvbiz.Service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentContentService commentContentService;


    /**
     * 批量查询评论内容
     *
     * @param batchFindCommentContentReqDTO
     * @return
     */
    @Override
    public R batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO) {
        // 归属的笔记 ID
        Long noteId = batchFindCommentContentReqDTO.getNoteId();

        // 查询评论的发布年月、内容 UUID
        List<FindCommentContentReqDTO> commentContentKeys = batchFindCommentContentReqDTO.getCommentContentKeys();

        // 过滤出年月
        List<String> yearMonths = commentContentKeys.stream()
                .map(FindCommentContentReqDTO::getYearMonth)
                .distinct() // 去重
                .collect(Collectors.toList());

        // 过滤出评论内容ID
        List<String> contentIds = commentContentKeys.stream()
                .map(FindCommentContentReqDTO::getContentId)
                .distinct() // 去重
                .collect(Collectors.toList());

        // 批量查询
        List<CommentContent> commentContentDOS = commentContentService
                .findByPrimaryKeyNoteIdAndPrimaryKeyYearMonthInAndPrimaryKeyContentIdIn(noteId, yearMonths, contentIds);

        // DO 转 DTO
        List<FindCommentContentRspDTO> findCommentContentRspDTOS = Lists.newArrayList();
        if (CollUtil.isNotEmpty(commentContentDOS)) {
            findCommentContentRspDTOS = commentContentDOS.stream()
                    .map(commentContentDO -> FindCommentContentRspDTO.builder()
                            .contentId(String.valueOf(commentContentDO.getContentId()))
                            .content(commentContentDO.getContent())
                            .build())
                    .toList();
        }

        return R.success(findCommentContentRspDTOS);
    }
}

