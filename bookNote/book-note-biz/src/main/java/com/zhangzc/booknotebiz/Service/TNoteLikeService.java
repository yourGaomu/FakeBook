package com.zhangzc.booknotebiz.Service;

import com.zhangzc.booknotebiz.Pojo.Domain.TNoteLike;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author 吃饭
 * @description 针对表【t_note_like(笔记点赞表)】的数据库操作Service
 * @createDate 2025-09-09 13:38:11
 */
public interface TNoteLikeService extends IService<TNoteLike> {
    boolean batchUpsert(List<TNoteLike> list);

}
