package com.zhangzc.bookcountbiz.Service;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdRspDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdsReqDTO;

import java.util.List;

public interface NoteCountService {
    R<List<FindNoteCountsByIdRspDTO>> findNotesCountData(FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO);
}
