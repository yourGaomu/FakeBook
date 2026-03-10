package com.zhangzc.booksearchbiz.Mapper.Mp;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangzc.booksearchbiz.Pojo.Entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {
    @Select("SELECT * FROM search_history WHERE user_id = #{userId} AND keyword = #{keyword} LIMIT 1")
    SearchHistory selectUnfiltered(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Update("UPDATE search_history SET is_delete = 0, update_time = #{updateTime} WHERE id = #{id}")
    void restoreHistory(@Param("id") Long id, @Param("updateTime") LocalDateTime updateTime);
}
