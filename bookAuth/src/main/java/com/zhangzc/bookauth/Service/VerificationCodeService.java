package com.zhangzc.bookauth.Service;


import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;

import java.util.Map;

public interface VerificationCodeService {

    /**
     * 发送短信验证码
     *
     *
     * @param sendVerificationCodeReqVO
     * @return
     */
    R send(Map<String,String> sendVerificationCodeReqVO) throws BizException;
}

