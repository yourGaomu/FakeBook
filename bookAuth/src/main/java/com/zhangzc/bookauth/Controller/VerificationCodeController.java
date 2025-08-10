package com.zhangzc.bookauth.Controller;


import com.zhangzc.bookauth.Service.VerificationCodeService;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@Slf4j
public class VerificationCodeController {

    @Resource
    private VerificationCodeService verificationCodeService;

    @PostMapping("/verification/code/send")
    @ApiOperationLog(description = "发送qq邮箱验证码")
    public R send(@RequestBody Map<String, String> sendVerificationCodeReqVO) throws BizException {
        return verificationCodeService.send(sendVerificationCodeReqVO);
    }

}

