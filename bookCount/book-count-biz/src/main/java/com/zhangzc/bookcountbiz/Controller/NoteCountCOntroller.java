package com.zhangzc.bookcountbiz.Controller;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdRspDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdsReqDTO;
import com.zhangzc.bookcountbiz.Service.NoteCountService;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/count")
@RestController
@Slf4j
@RequiredArgsConstructor
public class NoteCountCOntroller {
    private final NoteCountService noteCountService;

    @PostMapping(value = "/notes/data")
    @ApiOperationLog(description = "批量获取笔记计数数据")
    public R<List<FindNoteCountsByIdRspDTO>> findNotesCountData(@RequestBody FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO) {
        return noteCountService.findNotesCountData(findNoteCountsByIdsReqDTO);
    }

}
