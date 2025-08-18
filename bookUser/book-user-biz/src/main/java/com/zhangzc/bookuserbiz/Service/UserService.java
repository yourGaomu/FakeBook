package com.zhangzc.bookuserbiz.Service;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUserByIdReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import com.zhangzc.bookuserbiz.Pojo.Vo.UpdateUserInfoReqVO;

import java.util.Map;

public interface UserService {
    R updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);

    R register(Map<String, String> registerUserReqDTO);

    R findByPhone(Map<String, String> findUserByPhoneReqDTO) throws BizException;

    R updatePassword(Map<String, String> updateUserPasswordReqDTO);

    R<FindUserByIdRspDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO);
}
