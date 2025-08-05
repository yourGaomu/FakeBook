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

/**
 * @author: zhangzc
 * @date: 2025/5/29 15:32
 * @version: v1.0.0
 * @description: TODO
 **/
@RestController
@RequestMapping("/user")
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


    @GetMapping("/test")
    public void test() {
        mqUtil.sendCode("2823626561@qq.com","验证码","123456");
    }
}
