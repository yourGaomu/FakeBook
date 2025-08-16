package com.zhangzc.bookkvbiz.Controller;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.AddNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.DeleteNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.FindNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindNoteContentRspDTO;
import com.zhangzc.bookkvbiz.Service.NoteContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kv")
@RequiredArgsConstructor
@Slf4j
public class NoteContentController {


    private final NoteContentService noteContentService;

    @PostMapping(value = "/note/content/add")
    public R addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO) {
        return noteContentService.addNoteContent(addNoteContentReqDTO);
    }

    @PostMapping(value = "/note/content/find")
    public R<FindNoteContentRspDTO> findNoteContent(@Validated @RequestBody FindNoteContentReqDTO findNoteContentReqDTO) {
        return noteContentService.findNoteContent(findNoteContentReqDTO);
    }

    @PostMapping(value = "/note/content/delete")
    public R deleteNoteContent(@RequestBody DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        return noteContentService.deleteNoteContent(deleteNoteContentReqDTO);
    }
}

