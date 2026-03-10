package com.zhangzc.booksearchbiz.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhangzc.booksearchbiz.Pojo.Entity.PopularSearch;

import java.util.List;

public interface PopularSearchService extends IService<PopularSearch> {
    void addSearch(String keyword);
    List<PopularSearch> getHotList(int pageNo, int pageSize);
}
