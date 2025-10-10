package com.zhangzc.bookcountbiz.Mapper;

import com.zhangzc.bookcountbiz.Pojo.Domain.TNoteCount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangzc.bookcountbiz.Pojo.Dto.CountPublishCommentMqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author 吃饭
* @description 针对表【t_note_count(笔记计数表)】的数据库操作Mapper
* @createDate 2025-09-06 17:55:18
* @Entity com.zhangzc.bookcountbiz.Domain.TNoteCount
*/
public interface TNoteCountMapper extends BaseMapper<TNoteCount> {
    void incrementLikeTotal(@Param("noteId") Long noteId, @Param("count") int count);

    void insertOrUpdateCommentTotalByNoteId(@Param("countPublishCommentMqDTOS") Map<Long, Long> countPublishCommentMqDTOS);
}




