package com.zhangzc.booksearchbiz.Service;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteReqVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteRspVO;

public interface NoteService {

    PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO);

    PageResponse<SearchNoteRspVO> searchNotes(SearchNoteReqVO searchNoteReqVO);
}
