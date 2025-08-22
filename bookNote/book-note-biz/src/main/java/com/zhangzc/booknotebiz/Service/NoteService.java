package com.zhangzc.booknotebiz.Service;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.booknotebiz.Pojo.Vo.*;

public interface NoteService {

    R publishNote(PublishNoteReqVO publishNoteReqVO);

    R<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);

    R updateNote(UpdateNoteReqVO updateNoteReqVO);

    R deleteNote(DeleteNoteReqVO deleteNoteReqVO);

    R visibleOnlyMe(UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO);

    R topNote(TopNoteReqVO topNoteReqVO);
}
