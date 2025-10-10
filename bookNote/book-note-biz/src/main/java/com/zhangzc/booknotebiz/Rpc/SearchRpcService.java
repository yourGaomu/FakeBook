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

    public List<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO) {
        PageResponse<SearchNoteRspVO> searchNoteRspVOPageResponse = searchNoteFeginApi.searchNote(searchNoteReqVO);
        List<SearchNoteRspVO> data = searchNoteRspVOPageResponse.getData();
        return data;
    }
}
