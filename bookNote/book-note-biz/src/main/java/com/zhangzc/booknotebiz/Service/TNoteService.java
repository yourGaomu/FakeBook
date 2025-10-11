package com.zhangzc.booknotebiz.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhangzc.booknotebiz.Pojo.Domain.TNote;

import java.util.List;


/**
* @author 吃饭
* @description 针对表【t_note(笔记表)】的数据库操作Service
* @createDate 2025-08-17 14:56:56
*/
public interface TNoteService extends IService<TNote> {

    List<TNote> selectPublishedNoteListByUserIdAndCursor(Long userId, Long cursor);
}
