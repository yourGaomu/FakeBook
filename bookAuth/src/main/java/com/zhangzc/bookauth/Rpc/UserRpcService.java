package com.zhangzc.bookauth.Rpc;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookuserapi.Api.UserFeignApi;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByPhoneRspDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class UserRpcService {

    private final UserFeignApi userFeignApi;


    public Long registerUser(String phone) {

        Map<String, String> map = new HashMap<>();
        map.put("phone", phone);
        R response = userFeignApi.registerUser(map);

        if (!response.isSuccess()) {
            return null;
        }
        Object data = response.getData();
        Long userId = Long.valueOf(String.valueOf(data));
        return userId;
    }

    public FindUserByPhoneRspDTO findUserByPhone(String phone) {
        Map<String, String> findUserByPhoneReqDTO = new HashMap<>();
        findUserByPhoneReqDTO.put("phone", phone);

        R response = userFeignApi.findByPhone(findUserByPhoneReqDTO);

        if (!response.isSuccess()) {
            return null;
        }

        return (FindUserByPhoneRspDTO) response.getData();
    }

    public void updatePassword(String encodePassword) {
        Map<String,String> updateUserPasswordReqDTO = new HashMap<>();
        updateUserPasswordReqDTO.put("encodePassword",encodePassword);

        userFeignApi.updatePassword(updateUserPasswordReqDTO);
    }
}
