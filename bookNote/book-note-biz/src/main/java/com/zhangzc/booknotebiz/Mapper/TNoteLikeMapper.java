package com.zhangzc.booknotebiz.Mapper;

import com.zhangzc.booknotebiz.Pojo.Domain.TNoteLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author 吃饭
* @description 针对表【t_note_like(笔记点赞表)】的数据库操作Mapper
* @createDate 2025-09-09 13:38:11
* @Entity com.zhangzc.booknotebiz.Pojo.Domain.TNoteLike
*/
public interface TNoteLikeMapper extends BaseMapper<TNoteLike> {

    void batchInsertOrUpdate(List<TNoteLike> list);
}




