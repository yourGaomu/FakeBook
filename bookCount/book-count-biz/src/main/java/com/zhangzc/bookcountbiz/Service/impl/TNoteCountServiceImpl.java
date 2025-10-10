package com.zhangzc.bookcountbiz.Service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.bookcountbiz.Pojo.Domain.TNoteCount;
import com.zhangzc.bookcountbiz.Pojo.Dto.CountPublishCommentMqDTO;
import com.zhangzc.bookcountbiz.Service.TNoteCountService;
import com.zhangzc.bookcountbiz.Mapper.TNoteCountMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author 吃饭
* @description 针对表【t_note_count(笔记计数表)】的数据库操作Service实现
* @createDate 2025-09-06 17:55:18
*/
@Service
public class TNoteCountServiceImpl extends ServiceImpl<TNoteCountMapper, TNoteCount>
    implements TNoteCountService{


    /**
     * 对 like_total 进行累加（正数加，负数减）
     * @param noteId 笔记 ID
     * @param delta  变化量（正数加，负数减）
     * @return 是否更新成功
     */
    public boolean incrementLikeTotal(Long noteId, Long delta) {
        LambdaUpdateWrapper<TNoteCount> updateWrapper = new LambdaUpdateWrapper<>();
        // 条件：匹配 note_id
        updateWrapper.eq(TNoteCount::getNoteId, noteId);
        // 核心：使用 setSql 执行数据库字段自增/自减
        // like_total = like_total + #{delta}
        updateWrapper.setSql("like_total = like_total + " + delta);
        // 执行更新
        return this.update(updateWrapper);
    }

    // collect_total、comment_total 同理，只需替换字段名即可
    public boolean incrementCollectTotal(Long noteId, Long delta) {
        LambdaUpdateWrapper<TNoteCount> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TNoteCount::getNoteId, noteId);
        updateWrapper.setSql("collect_total = collect_total + " + delta);
        return this.update(updateWrapper);
    }

    public boolean incrementCommentTotal(Long noteId, Long delta) {
        LambdaUpdateWrapper<TNoteCount> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TNoteCount::getNoteId, noteId);
        updateWrapper.setSql("comment_total = comment_total + " + delta);
        return this.update(updateWrapper);
    }

    /**
     * 一次性对三个字段进行加减操作（正数加，负数减）
     * @param noteId 笔记ID
     * @param likeDelta like_total的变化量（正数加，负数减）
     * @param collectDelta collect_total的变化量
     * @param commentDelta comment_total的变化量
     * @return 是否操作成功
     */
    public boolean incrementThreeFields(Long noteId, Long likeDelta, Long collectDelta, Long commentDelta) {
        // 构建更新条件：根据noteId匹配记录
        LambdaUpdateWrapper<TNoteCount> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TNoteCount::getNoteId, noteId);

        // 批量设置三个字段的增减逻辑（数据库层面直接计算，保证原子性）
        updateWrapper.setSql("like_total = like_total + " + likeDelta)  // like_total增减
                .setSql("collect_total = collect_total + " + collectDelta)  // collect_total增减
                .setSql("comment_total = comment_total + " + commentDelta);  // comment_total增减

        // 执行更新操作
        return this.update(updateWrapper);
    }

    @Override
    public void incrementLikeTotal(Long noteId, int total) {
        this.baseMapper.incrementLikeTotal(noteId,total);
    }

    @Override
    public void insertOrUpdateCommentTotalByNoteId(Map<Long, Long> countPublishCommentMqDTOS) {
        if (countPublishCommentMqDTOS == null || countPublishCommentMqDTOS.isEmpty()) {
            return;
        }
        this.baseMapper.insertOrUpdateCommentTotalByNoteId(countPublishCommentMqDTOS);
    }

}




