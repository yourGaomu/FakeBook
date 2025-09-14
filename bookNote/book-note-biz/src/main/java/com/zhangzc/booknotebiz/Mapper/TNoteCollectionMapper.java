package com.zhangzc.booknotebiz.Mapper;

import com.zhangzc.booknotebiz.Pojo.Domain.TNoteCollection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author 吃饭
* @description 针对表【t_note_collection(笔记收藏表)】的数据库操作Mapper
* @createDate 2025-09-11 11:24:27
* @Entity com.zhangzc.booknotebiz.Pojo.Domain.TNoteCollection
*/
public interface TNoteCollectionMapper extends BaseMapper<TNoteCollection> {

    void saveOrUpdateTnoteCollection(List<TNoteCollection> list);
}




