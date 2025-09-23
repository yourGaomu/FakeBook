package com.zhangzc.bookcountbiz.Rpc;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.booknoteapi.Api.NoteFeignApi;
import com.zhangzc.booknoteapi.Pojo.Dto.Req.FindNoteDetailReqVO;
import com.zhangzc.booknoteapi.Pojo.Dto.Resp.FindNoteDetailRspVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NoteRpcService {
    private final NoteFeignApi noteFeignApi;

    public R<FindNoteDetailRspVO> findNoteDetail(Long noteId) {
        return noteFeignApi.findNoteDetail(FindNoteDetailReqVO.builder().id(noteId).build());
    }


}
