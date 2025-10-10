package com.zhangzc.bookcountbiz.Service;

import com.zhangzc.bookcountbiz.Pojo.Domain.TNoteCount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhangzc.bookcountbiz.Pojo.Dto.CountPublishCommentMqDTO;

import java.util.List;
import java.util.Map;

/**
* @author 吃饭
* @description 针对表【t_note_count(笔记计数表)】的数据库操作Service
* @createDate 2025-09-06 17:55:18
*/
public interface TNoteCountService extends IService<TNoteCount> {
    void incrementLikeTotal(Long noteId, int total);

    void insertOrUpdateCommentTotalByNoteId(Map<Long, Long> countPublishCommentMqDTOS);
}
