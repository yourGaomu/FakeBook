package com.zhangzc.bookcountapi.Api;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountapi.Const.ApiConstants;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdRspDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdsReqDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.Req.FindUserCountsByIdReqDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.Resp.FindUserCountsByIdRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface CountFeginApi {

    String PREFIX = "/count";

    @PostMapping(value = PREFIX + "/user/data")
    R<FindUserCountsByIdRspDTO> findUserCountData(@RequestBody FindUserCountsByIdReqDTO findUserCountsByIdReqDTO) throws BizException;

    @PostMapping(value = PREFIX + "/notes/data")
    R<List<FindNoteCountsByIdRspDTO>> findNotesCountData(@RequestBody FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO);

}
