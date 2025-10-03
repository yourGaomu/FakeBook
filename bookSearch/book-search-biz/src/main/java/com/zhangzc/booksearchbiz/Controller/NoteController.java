package com.zhangzc.booksearchbiz.Controller;


import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteReqVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteRspVO;
import com.zhangzc.booksearchbiz.Service.NoteService;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class NoteController {

    private  final NoteService noteService;

    @PostMapping("/search/note")
    @ApiOperationLog(description = "搜索笔记")
    public PageResponse<SearchNoteRspVO> searchNote(@RequestBody SearchNoteReqVO searchNoteReqVO) {
        return noteService.searchNote(searchNoteReqVO);
    }

}
