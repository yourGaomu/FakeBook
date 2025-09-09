package com.zhangzc.booknotebiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.booknotebiz.Pojo.Domain.TNoteLike;

import com.zhangzc.booknotebiz.Mapper.TNoteLikeMapper;
import com.zhangzc.booknotebiz.Service.TNoteLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
* @author 吃饭
* @description 针对表【t_note_like(笔记点赞表)】的数据库操作Service实现
* @createDate 2025-09-09 13:38:11
*/
@Service
@Slf4j
public class TNoteLikeServiceImpl extends ServiceImpl<TNoteLikeMapper, TNoteLike>
    implements TNoteLikeService {

    // 每批处理的数量（可根据数据库性能调整，建议500以内）
    private static final int BATCH_SIZE = 300;

    @Override
    public boolean batchUpsert(List<TNoteLike> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true; // 空列表直接返回成功
        }

        // 计算总批次
        int totalSize = list.size();
        int totalBatches = (totalSize + BATCH_SIZE - 1) / BATCH_SIZE; // 向上取整

        // 分批次处理
        for (int i = 0; i < totalBatches; i++) {
            // 计算当前批次的起始和结束索引
            int startIndex = i * BATCH_SIZE;
            int endIndex = Math.min((i + 1) * BATCH_SIZE, totalSize);

            // 截取当前批次的子列表
            List<TNoteLike> batchList = new ArrayList<>(list.subList(startIndex, endIndex));

            // 执行当前批次的Upsert
            baseMapper.batchInsertOrUpdate(batchList);

            // 可选：打印批次处理日志
            log.info("批次 {} 处理完成，处理数量：{}，总进度：{}/{}",
                    i + 1, batchList.size(), endIndex, totalSize);
        }
        return true;
    }
}




