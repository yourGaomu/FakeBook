package com.zhangzc.booknotebiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.booknotebiz.Pojo.Domain.TNoteCollection;
import com.zhangzc.booknotebiz.Service.TNoteCollectionService;
import com.zhangzc.booknotebiz.Mapper.TNoteCollectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 吃饭
 * @description 针对表【t_note_collection(笔记收藏表)】的数据库操作Service实现
 * @createDate 2025-09-11 11:24:27
 */
@Service
public class TNoteCollectionServiceImpl extends ServiceImpl<TNoteCollectionMapper, TNoteCollection>
        implements TNoteCollectionService {

    private Long size = 1000L;

    @Override
    public void saveOrUpdateTnoteCollection(List<TNoteCollection> list) {
        // 分批次保存（优化版）
        if (CollectionUtils.isEmpty(list)) {
            return; // 空列表直接返回，避免无效计算
        }
        // 计算总批次（修复整数除法问题）
        int total = list.size();
        // 计算总批次（修复整数除法问题）
        int totalBatches = Math.toIntExact((total + size - 1) / size);

        for (int i = 0; i < totalBatches; i++) {
            int start = Math.toIntExact(i * size);
            int end = Math.toIntExact(Math.min((i + 1) * size, total));
            // 转换为新的ArrayList，避免subList的视图特性问题
            List<TNoteCollection> subList = new ArrayList<>(list.subList(start, end));
            this.baseMapper.saveOrUpdateTnoteCollection(subList);
        }
    }
}




