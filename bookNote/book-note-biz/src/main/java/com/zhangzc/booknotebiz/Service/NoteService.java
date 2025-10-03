package com.zhangzc.booknotebiz.Service;


import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.booknotebiz.Pojo.Vo.*;
import org.springframework.cloud.client.loadbalancer.Response;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface NoteService {

    R publishNote(PublishNoteReqVO publishNoteReqVO);

    R<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);

    R updateNote(UpdateNoteReqVO updateNoteReqVO);

    R deleteNote(DeleteNoteReqVO deleteNoteReqVO);

    R visibleOnlyMe(UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO);

    R topNote(TopNoteReqVO topNoteReqVO);

    R likeNote(LikeNoteReqVO likeNoteReqVO);

    R unlikeNote(UnlikeNoteReqVO unlikeNoteReqVO);

    R collectNote(CollectNoteReqVO collectNoteReqVO);

    R unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO);

    R<List<FindChannelListRspVO>>findChannelList();

    R<List<FindTopicListRspVO>> findTopicList();

    R<FindPublishedNoteListRspVO> findPublishedNoteList(FindPublishedNoteListReqVO findPublishedNoteListReqVO) throws BizException, ExecutionException, InterruptedException;

    PageResponse<List<NotePageInfo>> findProfileNoteList(FindProfileNoteListReqVO findProfileNoteListReqVO) throws BizException;

    R<FindNoteIsLikedAndCollectedRspVO> isLikedAndCollectedData(FindNoteIsLikedAndCollectedReqVO findNoteIsLikedAndCollectedReqVO) throws BizException;

    PageResponse<List<NoteVO>> showNotesInfoOnDiscoverPage(ChannelPageRequest channelPageRequest);
}
