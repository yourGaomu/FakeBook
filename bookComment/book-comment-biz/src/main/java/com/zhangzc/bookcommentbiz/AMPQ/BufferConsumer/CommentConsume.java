package com.zhangzc.bookcommentbiz.AMPQ.BufferConsumer;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zhangzc.bookcommentbiz.Const.RabbitConstants;
import com.zhangzc.bookcommentbiz.Enum.CommentLevelEnum;
import com.zhangzc.bookcommentbiz.Pojo.Domain.CommentContent;
import com.zhangzc.bookcommentbiz.Pojo.Domain.TComment;
import com.zhangzc.bookcommentbiz.Pojo.Dto.CountPublishCommentMqDTO;
import com.zhangzc.bookcommentbiz.Pojo.Dto.PublishCommentMqDTO;
import com.zhangzc.bookcommentbiz.Service.CommentContentService;
import com.zhangzc.bookcommentbiz.Service.TCommentService;
import com.zhangzc.bookcommentbiz.Utils.SendMqRetryHelper;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentConsume {

    private final CommentContentService commentContentService;
    private final TCommentService tCommentService;
    private final SendMqRetryHelper sendMqRetryHelper;


    @Transactional(rollbackFor = Exception.class)
    public void consumeCommentMessage(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            log.info("消息为空");
            return;
        }
        // 过滤空字符串和纯空白字符串（推荐）
        List<String> list1 = strings.stream()
                .filter(s -> {
                    if (s == null || s.isEmpty()) {
                        log.info("消息为空");
                        return false;
                    }
                    return true;
                }) // isBlank() 会把空白字符串也视为"空"
                .toList();
        if (list1.isEmpty()) {
            return;
        }
        //开始转换
        List<PublishCommentMqDTO> publishCommentMqDTOS = JsonUtils.parseList(list1.toString(), new TypeReference<List<PublishCommentMqDTO>>() {
        });
        //存入评论内容数据库
        List<CommentContent> commentContents = publishCommentMqDTOS.stream().map(publishCommentMqDTO -> {
            CommentContent commentContent = new CommentContent();
            commentContent.setNoteId(publishCommentMqDTO.getNoteId());
            commentContent.setYearMonth(publishCommentMqDTO.getCreateTime().getYear() + "-" + publishCommentMqDTO.getCreateTime().getMonthValue());
            commentContent.setContentId(publishCommentMqDTO.getCommentId().toString());
            commentContent.setContent(publishCommentMqDTO.getContent());
            return commentContent;
        }).toList();
        boolean b = commentContentService.saveBatch(commentContents);

        if (b) {
            //评论保存成功，则应该更新笔记评论数量
            CompletableFuture.runAsync(() -> {
                //发送异步消息
                List<CountPublishCommentMqDTO> list = publishCommentMqDTOS.stream().map(publishCommentMqDTO -> CountPublishCommentMqDTO.builder()
                                .noteId(publishCommentMqDTO.getNoteId())
                                .commentId(publishCommentMqDTO.getCommentId())
                                .level(publishCommentMqDTO.getReplyCommentId() == null || publishCommentMqDTO.getReplyCommentId() == 0 ? CommentLevelEnum.ONE.getCode() : CommentLevelEnum.TWO.getCode())
                                .parentId(publishCommentMqDTO.getReplyCommentId())
                                .build())
                        .toList();
                sendMqRetryHelper.send("count.exchange", RabbitConstants.TOPIC_COUNT_NOTE_COMMENT, JsonUtils.toJsonString(list));
            });
        }

        //获取被回复的评论id
        List<Long> replyIds = publishCommentMqDTOS.stream().map(PublishCommentMqDTO::getReplyCommentId).toList();
        //获取被回复的评论
        Map<Long, TComment> collect = tCommentService.lambdaQuery().in(TComment::getId, replyIds).list()
                .stream().collect(Collectors.toMap(TComment::getId, p -> p));

        //存入评论数据库里面
        List<TComment> list = publishCommentMqDTOS.stream().map(publishCommentMqDTO -> {
            //是否是一级评论
            boolean isFirstReply = publishCommentMqDTO.getReplyCommentId() == null || publishCommentMqDTO.getReplyCommentId() == 0;
            TComment tComment = new TComment();
            tComment.setNoteId(publishCommentMqDTO.getNoteId());
            tComment.setUserId(publishCommentMqDTO.getCreatorId());
            tComment.setContentUuid(publishCommentMqDTO.getCommentId().toString());
            tComment.setIsContentEmpty(publishCommentMqDTO.getContent().isEmpty());
            tComment.setImageUrl(publishCommentMqDTO.getImageUrl());
            tComment.setLevel(isFirstReply ? 1 : 2);
            tComment.setReplyTotal(0L);
            tComment.setLikeTotal(0L);
            //设置父评论ID
            if (isFirstReply) {
                //为一级评论
                tComment.setParentId(publishCommentMqDTO.getCommentId());
            } else {
                //为二级评论
                tComment.setParentId(collect.get(publishCommentMqDTO.getReplyCommentId()).getParentId() == null
                        ? publishCommentMqDTO.getReplyCommentId()
                        : collect.get(publishCommentMqDTO.getReplyCommentId()).getParentId());
            }
            tComment.setReplyCommentId(publishCommentMqDTO.getReplyCommentId());
            tComment.setReplyUserId(collect.get(publishCommentMqDTO.getReplyCommentId()).getUserId());
            tComment.setIsTop(0);
            tComment.setCreateTime(TimeUtil.getDateTime(publishCommentMqDTO.getCreateTime()));
            tComment.setUpdateTime(TimeUtil.getDateTime(publishCommentMqDTO.getCreateTime()));
            tComment.setChildCommentTotal(0L);
            tComment.setHeat(BigDecimal.ZERO);
            return tComment;
        }).toList();
        //优先保存评论
        tCommentService.saveBatch(list);
        //用于处理设置二级评论数量
        CompletableFuture.runAsync(() -> {
            //查询出二级评论
            Map<Long, Long> parentCommentMap = new HashMap<>();
            list.stream().filter(tComment -> tComment.getLevel() == 2).collect(Collectors.groupingBy(TComment::getParentId)).forEach((k, v) -> {
                parentCommentMap.put(k, (long) v.size());
            });
            if (!parentCommentMap.isEmpty()) {
                tCommentService.updateChindCommentTotal(parentCommentMap);
            }
        });
        //用于设置一级评论的回复数量
        CompletableFuture.runAsync(() -> {
            try {
                // 2. 统计：每个一级评论（parentId是UUID）的新增回复数
                Map<String, Long> replyCountMap = new HashMap<>();
                for (TComment tComment : list) {
                    // 空防护：避免 parentId 为 null
                    String parentId = String.valueOf(tComment.getParentId());
                    if (parentId == null) {
                        log.warn("异步更新回复数：TComment.id={} 的 parentId 为 null，跳过", tComment.getId());
                        continue;
                    }

                    // 可靠判断：是否为一级评论的回复（parentId是UUID格式，而非纯数字的笔记ID）
                    // 正则判断UUID（支持带-的36位和不带-的32位）
                    boolean isCommentParentId = parentId.matches("^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$")
                            || parentId.matches("^[0-9a-fA-F]{32}$");
                    if (isCommentParentId) {
                        // 统计：每个一级评论的新增回复数（key=parentId，value=新增数量）
                        replyCountMap.put(parentId, replyCountMap.getOrDefault(parentId, 0L) + 1);
                    }
                }

                // 3. 无需要更新的评论，直接返回
                if (replyCountMap.isEmpty()) {
                    log.info("异步更新回复数：无新增回复，无需更新");
                    return;
                }
                // 4. 批量更新：用 CASE WHEN 精准累加（避免每条加1的错误）
                LambdaUpdateWrapper<TComment> updateWrapper = new LambdaUpdateWrapper<>();
                // 构建 CASE WHEN 逻辑：每个一级评论ID累加对应的新增回复数
                StringBuilder caseSql = new StringBuilder("reply_total = CASE id ");
                for (Map.Entry<String, Long> entry : replyCountMap.entrySet()) {
                    String commentId = entry.getKey(); // 一级评论ID（UUID）
                    Long addCount = entry.getValue();  // 新增回复数
                    caseSql.append("WHEN '").append(commentId).append("' THEN reply_total + ").append(addCount).append(" ");
                }
                caseSql.append("ELSE reply_total END"); // 其他评论不更新

                // 设置更新SQL和条件（只更新有新增回复的一级评论）
                updateWrapper.setSql(caseSql.toString())
                        .in(TComment::getId, replyCountMap.keySet());
                // 执行更新
                boolean updateSuccess = tCommentService.update(updateWrapper);
            } catch (Exception e) {
                log.error("异步更新回复数失败", e);
            }
        });


    }
}
