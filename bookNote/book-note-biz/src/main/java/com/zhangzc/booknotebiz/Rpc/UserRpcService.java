package com.zhangzc.booknotebiz.Rpc;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookuserapi.Api.UserFeignApi;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUserByIdReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    public FindUserByIdRspDTO findById(Long userId) {
        FindUserByIdReqDTO findUserByIdReqDTO = new FindUserByIdReqDTO();
        findUserByIdReqDTO.setId(userId);

        R<FindUserByIdRspDTO> response = userFeignApi.findById(findUserByIdReqDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();
    }

}

