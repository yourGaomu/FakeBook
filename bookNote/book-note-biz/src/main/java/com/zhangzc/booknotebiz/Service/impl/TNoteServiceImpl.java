package com.zhangzc.booknotebiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.booknotebiz.Mapper.TNoteMapper;
import com.zhangzc.booknotebiz.Pojo.Domain.TNote;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 吃饭
 * @description 针对表【t_note(笔记表)】的数据库操作Service实现
 * @createDate 2025-08-17 14:56:56
 */
@Service
public class TNoteServiceImpl extends ServiceImpl<TNoteMapper, TNote>
        implements com.zhangzc.booknotebiz.Service.TNoteService {

    @Override
    public List<TNote> selectPublishedNoteListByUserIdAndCursor(Long userId, Long cursor) {
        return this.baseMapper.selectPublishedNoteListByUserIdAndCursor(userId, cursor);
    }
}




