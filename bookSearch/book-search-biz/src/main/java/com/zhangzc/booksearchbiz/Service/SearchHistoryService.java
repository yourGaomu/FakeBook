package com.zhangzc.booksearchbiz.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhangzc.booksearchbiz.Pojo.Entity.SearchHistory;

import java.util.List;

public interface SearchHistoryService extends IService<SearchHistory> {
    void addHistory(Long userId, String keyword);
    List<String> getHistory(Long userId);
    void clearHistory(Long userId);
    void deleteHistory(Long userId, String keyword);
}
