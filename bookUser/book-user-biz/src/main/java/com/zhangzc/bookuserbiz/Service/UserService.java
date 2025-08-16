package com.zhangzc.bookuserbiz.Service;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookuserbiz.Pojo.Vo.UpdateUserInfoReqVO;
import org.springframework.cloud.client.loadbalancer.Response;

import java.util.Map;

public interface UserService {
    R updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);

    R register(Map<String, String> registerUserReqDTO);

    R findByPhone(Map<String, String> findUserByPhoneReqDTO) throws BizException;

    R updatePassword(Map<String, String> updateUserPasswordReqDTO);
}
