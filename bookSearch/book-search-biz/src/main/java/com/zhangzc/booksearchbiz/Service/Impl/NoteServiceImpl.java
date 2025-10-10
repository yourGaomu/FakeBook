package com.zhangzc.booksearchbiz.Service.Impl;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.booksearchbiz.Mapper.Es.SearchNoteMapper;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteReqVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteRspVO;
import com.zhangzc.booksearchbiz.Service.NoteService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dromara.easyes.core.biz.EsPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final SearchNoteMapper searchNoteMapper;

    @Override
    public PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO) {
        //关键词
        String keyword = searchNoteReqVO.getKeyword();
        //标记多少页
        Integer pageNo = searchNoteReqVO.getPageNo();
        //进行判空处理
        if (StringUtils.isEmpty(keyword)) {
            return PageResponse.success(null, 1, 0);
        }
        if (pageNo < 1) {
            pageNo = 1;
        }

        //进行条件编写
        LambdaEsQueryWrapper<SearchNoteRspVO> wrapper = new LambdaEsQueryWrapper<>();
        Integer type = searchNoteReqVO.getType();
        //进行类型判断
        if (type == null) {
            wrapper.in(SearchNoteRspVO::getType, 0, 1);
        } else {
            wrapper.eq(SearchNoteRspVO::getType, type);
        }
        //进行时间的区间判断
        if (searchNoteReqVO.getPublishTimeRange() != null) {
            switch (searchNoteReqVO.getPublishTimeRange()) {
                case 0: {
                    //一天
                    wrapper.gt(SearchNoteRspVO::getUpdateTime, TimeUtil.getLocalDateTimeDownOneDay());
                    wrapper.lt(SearchNoteRspVO::getUpdateTime, TimeUtil.getLocalDateTime());
                    break;
                }
                case 1: {
                    //一周
                    wrapper.gt(SearchNoteRspVO::getUpdateTime, TimeUtil.getLocalDateTimeDownOneWeek());
                    break;
                }
                case 2: {
                    //半年
                    wrapper.gt(SearchNoteRspVO::getUpdateTime, TimeUtil.getLocalDateTimeDownHalfYear());
                    break;
                }
            }
        }

        wrapper.match(SearchNoteRspVO::getTitle, keyword, 2.0F)
                .or()
                .match(SearchNoteRspVO::getTopicName, keyword, 1.0F)
                .gt(SearchNoteRspVO::getLikeTotal, -1, 0.5F)
                .gt(SearchNoteRspVO::getCollectTotal, -1, 0.2F)
                .gt(SearchNoteRspVO::getCommentTotal, -1, 0.3F);

        Integer sort = searchNoteReqVO.getSort();
        //排序方法
        if (sort != null) {
            switch (sort) {
                case 1: {
                    wrapper.orderByDesc(SearchNoteRspVO::getLikeTotal);
                    break;
                }
                case 2: {
                    wrapper.orderByDesc(SearchNoteRspVO::getCommentTotal);
                    break;
                }
                case 3: {
                    wrapper.orderByDesc(SearchNoteRspVO::getCollectTotal);
                    break;
                }
            }
        } else {
            wrapper.sortByScore();
        }

        //进行查询
        EsPageInfo<SearchNoteRspVO> searchNoteRspVOEsPageInfo = searchNoteMapper.pageQuery(wrapper, pageNo, 10);
        Long total = searchNoteRspVOEsPageInfo.getTotal();
        if (total == 0) {
            return PageResponse.success(null, 1, 0);
        }
        List<SearchNoteRspVO> list = searchNoteRspVOEsPageInfo.getList();
        return PageResponse.success(list, pageNo, total);
    }

    @Override
    public PageResponse<SearchNoteRspVO> searchNotes(SearchNoteReqVO searchNoteReqVO) {
        //这是首页排序
        Integer pageNo = searchNoteReqVO.getPageNo();
        LambdaEsQueryWrapper<SearchNoteRspVO> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.gt(SearchNoteRspVO::getLikeTotal, -1, 0.5F)
                .gt(SearchNoteRspVO::getCollectTotal, -1, 0.2F)
                .gt(SearchNoteRspVO::getCommentTotal, -1, 0.3F)
                //按照评分来排序
                .sortByScore();
        EsPageInfo<SearchNoteRspVO> searchNoteRspVOEsPageInfo = searchNoteMapper.pageQuery(wrapper, pageNo, 10);
        Long total = searchNoteRspVOEsPageInfo.getTotal();
        if (total == 0) {
            return PageResponse.success(null, 1, 0);
        }
        List<SearchNoteRspVO> list = searchNoteRspVOEsPageInfo.getList();
        return PageResponse.success(list, pageNo, total);
    }
}
