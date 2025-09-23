package com.zhangzc.bookuserbiz.rpc;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountapi.Api.CountFeginApi;
import com.zhangzc.bookcountapi.Pojo.Dto.Req.FindUserCountsByIdReqDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.Resp.FindUserCountsByIdRspDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountRpcService {
    private final CountFeginApi countFeginApi;

    public FindUserCountsByIdRspDTO findUserCountsByIdRspDTO(Long userID) throws BizException {
        if (userID == null) {
            return null;
        }
        R<FindUserCountsByIdRspDTO> userCountData = countFeginApi.findUserCountData(FindUserCountsByIdReqDTO.builder().userId(userID).build());
        FindUserCountsByIdRspDTO data = userCountData.getData();
        return data;

    }
}
