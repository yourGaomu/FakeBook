package com.zhangzc.booksearchbiz.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.booksearchbiz.Mapper.Mp.SearchHistoryMapper;
import com.zhangzc.booksearchbiz.Pojo.Entity.SearchHistory;
import com.zhangzc.booksearchbiz.Service.SearchHistoryService;
import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements SearchHistoryService {

    private static final String HISTORY_KEY_PREFIX = "search:history:";
    private static final int HISTORY_LIMIT = 50;
    private final RedisUtil redisUtil;

    @Override
    public void addHistory(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        // 异步处理
        CompletableFuture.runAsync(() -> {
            try {
                String key = HISTORY_KEY_PREFIX + userId;
                double score = System.currentTimeMillis();

                if (redisUtil.hasKey(key)) {
                    // 1. 更新 Redis ZSet
                    redisUtil.zAdd(key, keyword, score);

                    // 2. 保持 Redis 中只有最近 50 条
                    // zCard 获取总数
                    long size = redisUtil.zCard(key);
                    if (size > HISTORY_LIMIT) {
                        // 移除分数最低的 (最旧的)
                        // 移除从 0 到 (size - 50 - 1) 的元素
                        redisUtil.zRemoveRange(key, 0, size - HISTORY_LIMIT - 1);
                    }
                }

                // 3. 更新 MySQL
                // 使用自定义Mapper方法，忽略逻辑删除标记进行查询
                SearchHistory exist = baseMapper.selectUnfiltered(userId, keyword);
                
                if (exist != null) {
                    // 如果存在（无论是否已删除），恢复并更新时间
                    baseMapper.restoreHistory(exist.getId(), LocalDateTime.now());
                } else {
                    // 不存在则新增
                    SearchHistory history = SearchHistory.builder()
                            .userId(userId)
                            .keyword(keyword)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .isDelete(false)
                            .build();
                    try {
                        this.save(history);
                    } catch (Exception e) {
                        // 并发处理：再次尝试查询并恢复
                         SearchHistory existRetry = baseMapper.selectUnfiltered(userId, keyword);
                         if(existRetry != null){
                             baseMapper.restoreHistory(existRetry.getId(), LocalDateTime.now());
                         }
                    }
                }

            } catch (Exception e) {
                log.error("Add search history error", e);
            }
        });
    }

    @Override
    public List<String> getHistory(Long userId) {
        if (userId == null) return new ArrayList<>();

        String key = HISTORY_KEY_PREFIX + userId;

        // 从 Redis 读取 (倒序，分数大的在前面)
        // 0 到 49
        Set<Object> historySet = redisUtil.zReverseRange(key, 0, HISTORY_LIMIT - 1);

        if (historySet != null && !historySet.isEmpty()) {
            return historySet.stream().map(Object::toString).collect(Collectors.toList());
        }

        // 如果 Redis 为空 (可能过期或从未加载)，从 MySQL 加载前 50 条
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getUpdateTime)
                .last("LIMIT " + HISTORY_LIMIT);

        List<SearchHistory> list = this.list(wrapper);
        List<String> keywords = list.stream().map(SearchHistory::getKeyword).collect(Collectors.toList());

        // 回写 Redis (可选，为了下次快)
        if (!keywords.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                for (SearchHistory h : list) {
                    // 这里的 score 用 updateTime 的毫秒数
                    long score = java.sql.Timestamp.valueOf(h.getUpdateTime()).getTime();
                    redisUtil.zAdd(key, h.getKeyword(), score);
                }
                redisUtil.expire(key, 3600 * 24 * 7); // 7天过期
            });
        }

        return keywords;
    }

    @Override
    public void clearHistory(Long userId) {
        if (userId == null) return;
        String key = HISTORY_KEY_PREFIX + userId;
        redisUtil.del(key);

        this.lambdaUpdate()
                .eq(SearchHistory::getUserId, userId)
                .set(SearchHistory::getIsDelete, 1)
                .update();

    }

    @Override
    public void deleteHistory(Long userId, String keyword) {
        if (userId == null || keyword == null) return;

        // 1. 删除 Redis 中的记录
        String key = HISTORY_KEY_PREFIX + userId;
        redisUtil.zRemove(key, keyword);

        // 2. 删除 MySQL 中的记录
        this.lambdaUpdate()
                .eq(SearchHistory::getUserId, userId)
                .set(SearchHistory::getIsDelete, 1)
                .eq(SearchHistory::getKeyword, keyword)
                .update();
    }
}
