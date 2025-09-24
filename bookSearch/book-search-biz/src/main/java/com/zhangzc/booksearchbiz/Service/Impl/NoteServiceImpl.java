package com.zhangzc.booksearchbiz.Service.Impl;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.booksearchbiz.Mapper.SearchNoteMapper;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteReqVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteRspVO;
import com.zhangzc.booksearchbiz.Service.NoteService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final SearchNoteMapper searchNoteMapper;

    @Override
    public PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO) {
        //关键词显示
        String keyword = searchNoteReqVO.getKeyword();
        //标记多少页
        Integer pageNo = searchNoteReqVO.getPageNo();
        //进行判空处理
        if (StringUtils.isEmpty(keyword)) {
            return PageResponse.success(null, 1, 0);
        }
        //进行


    }
}
