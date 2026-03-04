package com.zhangzc.booknotebiz.Rpc;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.booksearchapi.Api.SearchNoteFeginApi;
import com.zhangzc.booksearchapi.Pojo.Dto.Req.SearchNoteReqVO;
import com.zhangzc.booksearchapi.Pojo.Dto.Resp.SearchNoteRspVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchRpcService {
    private final SearchNoteFeginApi searchNoteFeginApi;

    public PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO) {
        PageResponse<SearchNoteRspVO> searchNoteRspVOPageResponse = searchNoteFeginApi.searchNote(searchNoteReqVO);
        if (searchNoteRspVOPageResponse == null || searchNoteRspVOPageResponse.getData() == null) {
            return PageResponse.success(List.of(), 1, 0);
        }
        return searchNoteRspVOPageResponse;
    }

    public PageResponse<SearchNoteRspVO> searchNotes(SearchNoteReqVO searchNoteReqVO) {
        PageResponse<SearchNoteRspVO> searchNoteRspVOPageResponse = searchNoteFeginApi.searchNotes(searchNoteReqVO);
        if (searchNoteRspVOPageResponse == null || searchNoteRspVOPageResponse.getData() == null) {
            return PageResponse.success(List.of(), 1, 0);
        }
        return searchNoteRspVOPageResponse;
    }

    public Boolean syncNote(SearchNoteRspVO searchNoteRspVO) {
        return searchNoteFeginApi.syncNote(searchNoteRspVO);
    }
}
