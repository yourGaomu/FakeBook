package com.zhangzc.booknotebiz.Service;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.booknotebiz.Pojo.Vo.FindNoteDetailReqVO;
import com.zhangzc.booknotebiz.Pojo.Vo.FindNoteDetailRspVO;
import com.zhangzc.booknotebiz.Pojo.Vo.PublishNoteReqVO;
import com.zhangzc.booknotebiz.Pojo.Vo.UpdateNoteReqVO;

public interface NoteService {

    R publishNote(PublishNoteReqVO publishNoteReqVO);

    R<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);

    R updateNote(UpdateNoteReqVO updateNoteReqVO);
}
