package com.zhangzc.booknotebiz.Controller;


import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.booknotebiz.Pojo.Vo.*;
import com.zhangzc.booknotebiz.Service.NoteService;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/note")
@Slf4j
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @PostMapping("/discover/note/list")
    public PageResponse<List<NoteVO>> showNotesInfoOnDiscoverPage(ChannelPageRequest channelPageRequest) {
        return noteService.showNotesInfoOnDiscoverPage(channelPageRequest);
    }

    @PostMapping(value = "/profile/note/list")
    @ApiOperationLog(description = "用户主页 - 已发布笔记列表")
    public  PageResponse<List<NotePageInfo>> findProfileNoteList(@RequestBody FindProfileNoteListReqVO findProfileNoteListReqVO) throws BizException, ExecutionException, InterruptedException {
        return noteService.findProfileNoteList(findProfileNoteListReqVO);
    }


    @PostMapping(value = "/published/list")
    @ApiOperationLog(description = "用户主页 - 已发布笔记列表")
    public R<FindPublishedNoteListRspVO> findPublishedNoteList(@RequestBody FindPublishedNoteListReqVO findPublishedNoteListReqVO) throws BizException, ExecutionException, InterruptedException {
        return noteService.findPublishedNoteList(findPublishedNoteListReqVO);
    }

    @PostMapping("/topic/list")
    @ApiOperationLog(description = "查询话题列表")
    public R<List<FindTopicListRspVO>> findTopicList() {
        return noteService.findTopicList();
    }

    @PostMapping("/channel/list")
    @ApiOperationLog(description = "查询频道列表")
    public R<List<FindChannelListRspVO>> findChannelList() {
        return noteService.findChannelList();
    }

    @PostMapping(value = "/publish")
    @ApiOperationLog(description = "笔记发布")
    public R publishNote(@RequestBody PublishNoteReqVO publishNoteReqVO) {
        return noteService.publishNote(publishNoteReqVO);
    }

    @PostMapping(value = "/detail")
    @ApiOperationLog(description = "笔记详情")
    public R<FindNoteDetailRspVO> findNoteDetail(@RequestBody FindNoteDetailReqVO findNoteDetailReqVO) {
        return noteService.findNoteDetail(findNoteDetailReqVO);
    }

    @PostMapping(value = "/update")
    @ApiOperationLog(description = "笔记修改")
    public R updateNote(@RequestBody UpdateNoteReqVO updateNoteReqVO) {
        return noteService.updateNote(updateNoteReqVO);
    }

    @PostMapping(value = "/delete")
    @ApiOperationLog(description = "删除笔记")
    public R deleteNote(@RequestBody DeleteNoteReqVO deleteNoteReqVO) {
        return noteService.deleteNote(deleteNoteReqVO);
    }

    @PostMapping(value = "/visible/onlyme")
    @ApiOperationLog(description = "笔记仅对自己可见")
    public R visibleOnlyMe(@RequestBody UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO) {
        return noteService.visibleOnlyMe(updateNoteVisibleOnlyMeReqVO);
    }

    @PostMapping(value = "/top")
    @ApiOperationLog(description = "置顶/取消置顶笔记")
    public R topNote( @RequestBody TopNoteReqVO topNoteReqVO) {
        return noteService.topNote(topNoteReqVO);
    }

    @PostMapping(value = "/like")
    @ApiOperationLog(description = "点赞笔记")
    public R likeNote(@RequestBody LikeNoteReqVO likeNoteReqVO) {
        return noteService.likeNote(likeNoteReqVO);
    }


    @PostMapping(value = "/unlike")
    @ApiOperationLog(description = "取消点赞笔记")
    public R unlikeNote(@RequestBody UnlikeNoteReqVO unlikeNoteReqVO) {
        return noteService.unlikeNote(unlikeNoteReqVO);
    }

    @PostMapping(value = "/collect")
    @ApiOperationLog(description = "收藏笔记")
    public R collectNote(@RequestBody CollectNoteReqVO collectNoteReqVO) {
        return noteService.collectNote(collectNoteReqVO);
    }

    @PostMapping(value = "/uncollect")
    @ApiOperationLog(description = "取消收藏笔记")
    public R unCollectNote(@RequestBody UnCollectNoteReqVO unCollectNoteReqVO) {
        return noteService.unCollectNote(unCollectNoteReqVO);
    }

    @PostMapping(value = "/isLikedAndCollectedData")
    @ApiOperationLog(description = "获取当前用户是否点赞、收藏数据")
    public R<FindNoteIsLikedAndCollectedRspVO> isLikedAndCollectedData(@RequestBody FindNoteIsLikedAndCollectedReqVO findNoteIsLikedAndCollectedReqVO) throws BizException {
        return noteService.isLikedAndCollectedData(findNoteIsLikedAndCollectedReqVO);
    }

}
