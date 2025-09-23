package com.zhangzc.booknoteapi.Api;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.booknoteapi.Const.ApiConstants;
import com.zhangzc.booknoteapi.Pojo.Dto.Req.FindNoteDetailReqVO;
import com.zhangzc.booknoteapi.Pojo.Dto.Resp.FindNoteDetailRspVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface NoteFeignApi {
    String SERVICE_NAME = "/note";


    @PostMapping(value = SERVICE_NAME + "/detail")
    R<FindNoteDetailRspVO> findNoteDetail(@RequestBody FindNoteDetailReqVO findNoteDetailReqVO);

}
