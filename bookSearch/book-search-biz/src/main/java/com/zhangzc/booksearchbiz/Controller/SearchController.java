package com.zhangzc.booksearchbiz.Controller;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.booksearchbiz.Pojo.Entity.PopularSearch;
import com.zhangzc.booksearchbiz.Pojo.Vo.PopularSearchReqVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchHistoryAddVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchHistoryReqVO;
import com.zhangzc.booksearchbiz.Service.PopularSearchService;
import com.zhangzc.booksearchbiz.Service.SearchHistoryService;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchHistoryService searchHistoryService;
    private final PopularSearchService popularSearchService;

    @PostMapping("/search/hot/list")
    @ApiOperationLog(description = "获取热门搜索列表")
    public R<List<PopularSearch>> getHotList(@RequestBody PopularSearchReqVO req) {
        if (req == null) req = new PopularSearchReqVO();
        List<PopularSearch> list = popularSearchService.getHotList(req.getPageNo(), req.getPageSize());
        return R.success(list);
    }

    @PostMapping("/search/history/clear")
    @ApiOperationLog(description = "清空搜索历史")
    public R<Boolean> clearHistory() {
        Long userId = LoginUserContextHolder.getUserId();
        if (userId != null) {
            searchHistoryService.clearHistory(userId);
        }
        return R.success(true);
    }

    @PostMapping("/search/history/delete")
    @ApiOperationLog(description = "删除单条搜索历史")
    public R<Boolean> deleteHistory(@RequestBody SearchHistoryAddVO req) {
        if (req == null || req.getKeyword() == null) {
            return R.fail("Keyword cannot be null");
        }
        Long userId = LoginUserContextHolder.getUserId();
        if (userId != null) {
            searchHistoryService.deleteHistory(userId, req.getKeyword());
        }
        return R.success(true);
    }

    @PostMapping("/search/history/list")
    @ApiOperationLog(description = "获取用户搜索历史")
    public R<List<String>> getHistory(@RequestBody SearchHistoryReqVO req) {
        if (req == null) return R.fail("Request body is empty");
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            return R.fail("UserId is required");
        }
        
        List<String> list = searchHistoryService.getHistory(userId);
        
        // 内存分页
        int pageNo = req.getPageNo() != null ? req.getPageNo() : 1;
        int pageSize = req.getPageSize() != null ? req.getPageSize() : 20;
        
        int start = (pageNo - 1) * pageSize;
        if (start >= list.size()) {
            return R.success(Collections.emptyList());
        }
        int end = Math.min(start + pageSize, list.size());
        return R.success(list.subList(start, end));
    }

    @PostMapping("/search/history/add")
    @ApiOperationLog(description = "添加搜索历史")
    public R<Boolean> addHistory(@RequestBody SearchHistoryAddVO req) {
        if (req == null || req.getKeyword() == null) {
            return R.fail("Keyword cannot be null");
        }
        Long userId = LoginUserContextHolder.getUserId();
        if (userId != null) {
            searchHistoryService.addHistory(userId, req.getKeyword());
        }
        
        popularSearchService.addSearch(req.getKeyword());
        return R.success(true);
    }
}
