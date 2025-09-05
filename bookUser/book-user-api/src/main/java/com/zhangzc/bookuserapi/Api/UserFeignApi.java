package com.zhangzc.bookuserapi.Api;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookuserapi.Const.ApiConstants;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUserByIdReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUsersByIdsReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface UserFeignApi {
    String PREFIX = "/user";


    @PostMapping(value = PREFIX + "/register")
    R registerUser(@RequestBody Map<String,String> registerUserReqDTO);


    @PostMapping(value = PREFIX + "/findByPhone")
    R findByPhone(@RequestBody Map<String,String> findUserByPhoneReqDTO);

    @PostMapping(value = PREFIX + "/password/update")
    R updatePassword(@RequestBody Map<String,String> updateUserPasswordReqDTO);


    @PostMapping("/findById")
    R<FindUserByIdRspDTO> findById(@RequestBody FindUserByIdReqDTO findUserByIdReqDTO);


    @PostMapping(value = PREFIX + "/findByIds")
    R<List<FindUserByIdRspDTO>> findByIds(@RequestBody FindUsersByIdsReqDTO findUsersByIdsReqDTO);
}
