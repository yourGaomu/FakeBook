package com.zhangzc.bookkvbiz.Service;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.AddNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.DeleteNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.FindNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindNoteContentRspDTO;

import java.util.List;

public interface NoteContentService {
    R addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

    R<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);

    R deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO);

    R<List<FindNoteContentRspDTO>> findNoteContents(List<FindNoteContentReqDTO> addNoteContentReqDTO);
}
