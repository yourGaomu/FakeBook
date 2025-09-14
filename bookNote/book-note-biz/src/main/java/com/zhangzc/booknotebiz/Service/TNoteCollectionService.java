package com.zhangzc.booknotebiz.Service;

import com.zhangzc.booknotebiz.Pojo.Domain.TNoteCollection;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 吃饭
* @description 针对表【t_note_collection(笔记收藏表)】的数据库操作Service
* @createDate 2025-09-11 11:24:27
*/
public interface TNoteCollectionService extends IService<TNoteCollection> {

    void saveOrUpdateTnoteCollection(List<TNoteCollection> list);
}
