package com.zhangzc.bookauth.Service;

import com.zhangzc.bookauth.Pojo.Vo.UserLoginReqVO;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import org.springframework.cloud.client.loadbalancer.Response;

import java.util.Map;

public interface UserService {
    R loginAndRegister(UserLoginReqVO userLoginReqVO) throws BizException;

    R logoutByUserId();

    R updatePassword(Map<String, String> updatePasswordReqVO);
}
