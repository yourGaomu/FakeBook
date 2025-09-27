package com.zhangzc.booksearchbiz.Mapper.Mp;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 查询
 */
public interface SelectMapper {

    /**
     * 查询笔记文档所需的全字段数据
     * @param noteId
     * @return
     */
    List<Map<String, Object>> selectEsNoteIndexData(@Param("noteId") long noteId);

    List<Map<String, Object>> selectEsUserIndexData(@Param("userId") long userId);
}
