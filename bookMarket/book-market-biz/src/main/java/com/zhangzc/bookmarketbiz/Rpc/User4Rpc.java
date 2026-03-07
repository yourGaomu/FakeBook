package com.zhangzc.bookmarketbiz.Rpc;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookuserapi.Api.UserFeignApi;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUserByIdReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class User4Rpc {

    private final UserFeignApi userFeignApi;


    public FindUserByIdRspDTO getUserInfoByUserID(Long userID) {
       try{
           R<FindUserByIdRspDTO> byId = userFeignApi.findById(FindUserByIdReqDTO.builder()
                   .id(userID)
                   .build());
           FindUserByIdRspDTO data = byId.getData();
           if (data != null) {
               return data;
           }else {
               return null;
           }
       } catch (Exception e) {
           throw new RuntimeException("调用用户RPC查询用户信息出现错误");
       }
    }
}
