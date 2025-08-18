package com.zhangzc.booknotebiz.Controller;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.booknotebiz.Pojo.Vo.FindNoteDetailReqVO;
import com.zhangzc.booknotebiz.Pojo.Vo.FindNoteDetailRspVO;
import com.zhangzc.booknotebiz.Pojo.Vo.PublishNoteReqVO;
import com.zhangzc.booknotebiz.Pojo.Vo.UpdateNoteReqVO;
import com.zhangzc.booknotebiz.Service.NoteService;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/note")
@Slf4j
public class NoteController {

    @Resource
    private NoteService noteService;

    @PostMapping(value = "/publish")
    @ApiOperationLog(description = "笔记发布")
    public R publishNote( @RequestBody PublishNoteReqVO publishNoteReqVO) {
        return noteService.publishNote(publishNoteReqVO);
    }

    @PostMapping(value = "/detail")
    @ApiOperationLog(description = "笔记详情")
    public R<FindNoteDetailRspVO> findNoteDetail(@RequestBody FindNoteDetailReqVO findNoteDetailReqVO) {
        return noteService.findNoteDetail(findNoteDetailReqVO);
    }

    @PostMapping(value = "/update")
    @ApiOperationLog(description = "笔记修改")
    public R updateNote( @RequestBody UpdateNoteReqVO updateNoteReqVO) {
        return noteService.updateNote(updateNoteReqVO);
    }

}
