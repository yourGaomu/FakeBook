package com.zhangzc.booksearchbiz.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.booksearchbiz.Mapper.Mp.PopularSearchMapper;
import com.zhangzc.booksearchbiz.Pojo.Entity.PopularSearch;
import com.zhangzc.booksearchbiz.Service.PopularSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class PopularSearchServiceImpl extends ServiceImpl<PopularSearchMapper, PopularSearch> implements PopularSearchService {

    @Override
    public void addSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;

        CompletableFuture.runAsync(() -> {
            try {
                // 查找是否已存在
                LambdaQueryWrapper<PopularSearch> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PopularSearch::getKeyword, keyword);
                PopularSearch popular = this.getOne(wrapper);
                
                if (popular == null) {
                    popular = PopularSearch.builder()
                            .keyword(keyword)
                            .searchCount(1L)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    // 计算初始热度
                    popular.setHeatScore(calcHeat(popular.getSearchCount(), popular.getCreateTime()));
                    
                    try {
                        this.save(popular);
                    } catch (Exception e) {
                        // 并发冲突，重新获取
                         popular = this.getOne(wrapper);
                         if (popular != null) {
                             updatePopular(popular);
                         }
                    }
                } else {
                    updatePopular(popular);
                }
            } catch (Exception e) {
                log.error("Update popular search error", e);
            }
        });
    }
    
    private void updatePopular(PopularSearch popular) {
        popular.setSearchCount(popular.getSearchCount() + 1);
        popular.setUpdateTime(LocalDateTime.now());
        popular.setHeatScore(calcHeat(popular.getSearchCount(), popular.getCreateTime()));
        this.updateById(popular);
    }
    
    /**
     * 热度计算算法
     * 采用简单的重力衰减模型 (类似 Hacker News)
     * Score = P / (T + 2)^G
     * 这里 P = searchCount, T = hours since creation, G = 1.5
     */
    private Double calcHeat(Long count, LocalDateTime createTime) {
        if (createTime == null) createTime = LocalDateTime.now();
        long hours = java.time.Duration.between(createTime, LocalDateTime.now()).toHours();
        // 避免除以0或负数，最小 hours 为 0
        if (hours < 0) hours = 0;
        return (double) count / Math.pow(hours + 2, 1.5);
    }

    @Override
    public List<PopularSearch> getHotList(int pageNo, int pageSize) {
        Page<PopularSearch> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<PopularSearch> wrapper = new LambdaQueryWrapper<>();
        // 按热度降序
        wrapper.orderByDesc(PopularSearch::getHeatScore);
        
        this.page(page, wrapper);
        return page.getRecords();
    }
}
