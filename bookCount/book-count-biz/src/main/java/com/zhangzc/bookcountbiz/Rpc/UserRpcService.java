package com.zhangzc.bookcountbiz.Rpc;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookuserapi.Api.UserFeignApi;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUserByIdReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRpcService {
    private final UserFeignApi userFeignApi;

    public FindUserByIdRspDTO findById(Long userId) {
        R<FindUserByIdRspDTO> response = userFeignApi.findById(FindUserByIdReqDTO.builder()
                .id(userId)
                .build());
        if (!response.isSuccess()) {
            return null;
        }
        return response.getData();
    }
}
