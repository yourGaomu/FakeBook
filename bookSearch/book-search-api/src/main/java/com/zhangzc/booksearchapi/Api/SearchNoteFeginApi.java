package com.zhangzc.booksearchapi.Api;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.booksearchapi.Const.ApiConstants;
import com.zhangzc.booksearchapi.Pojo.Dto.Req.SearchNoteReqVO;
import com.zhangzc.booksearchapi.Pojo.Dto.Resp.SearchNoteRspVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface SearchNoteFeginApi {

    String prefix = "/search";

    @PostMapping(prefix+"/search/notes")
    PageResponse<SearchNoteRspVO> searchNotes(@RequestBody SearchNoteReqVO searchNoteReqVO);

    @PostMapping(prefix+"/search/note")
    PageResponse<SearchNoteRspVO> searchNote(@RequestBody SearchNoteReqVO searchNoteReqVO);
}
