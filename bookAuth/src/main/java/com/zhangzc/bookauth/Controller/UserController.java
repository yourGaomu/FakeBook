package com.zhangzc.bookauth.Controller;


import com.zhangzc.bookauth.Pojo.Vo.UserLoginReqVO;
import com.zhangzc.bookauth.Service.UserService;
import com.zhangzc.bookauth.Utils.MQUtil;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MQUtil mqUtil;

    @PostMapping("/login")
    @ApiOperationLog(description = "用户登录/注册")
    public R loginAndRegister(@RequestBody UserLoginReqVO userLoginReqVO) throws BizException {
        return userService.loginAndRegister(userLoginReqVO);
    }


    @PostMapping("/logout")
    @ApiOperationLog(description = "账号登出")
    public R logout() {
        return userService.logoutByUserId();
    }


    @PostMapping("/password/update")
    @ApiOperationLog(description = "修改密码")
    public R updatePassword(@RequestBody Map<String,String> updatePasswordReqVO) {
        return userService.updatePassword(updatePasswordReqVO);
    }

}
