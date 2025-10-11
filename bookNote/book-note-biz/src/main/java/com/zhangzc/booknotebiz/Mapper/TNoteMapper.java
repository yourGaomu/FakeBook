package com.zhangzc.booknotebiz.Mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangzc.booknotebiz.Pojo.Domain.TNote;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 吃饭
* @description 针对表【t_note(笔记表)】的数据库操作Mapper
* @createDate 2025-08-17 14:56:56
* @Entity com.zhangzc.booknotebiz.domain.TNote
*/
public interface TNoteMapper extends BaseMapper<TNote> {

    List<TNote> selectPublishedNoteListByUserIdAndCursor(@Param("userId") Long userId, @Param("cursor") Long cursor);
}




