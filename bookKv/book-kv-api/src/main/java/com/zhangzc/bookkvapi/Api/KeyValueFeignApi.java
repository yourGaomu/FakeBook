package com.zhangzc.bookkvapi.Api;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookkvapi.Const.ApiConstants;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.AddNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.DeleteNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.FindNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindNoteContentRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface KeyValueFeignApi {

    String PREFIX = "/kv";

    @PostMapping(value = PREFIX + "/note/content/add")
    R addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);

    @PostMapping(value = PREFIX + "/note/content/find")
    R<FindNoteContentRspDTO> findNoteContent(@Validated @RequestBody FindNoteContentReqDTO findNoteContentReqDTO);

    @PostMapping(value = "/note/content/delete")
     R deleteNoteContent(@RequestBody DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}

