package com.zhangzc.bookuserbiz.Controller;


import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUserByIdReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUsersByIdsReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import com.zhangzc.bookuserbiz.Pojo.Vo.UpdateUserInfoReqVO;
import com.zhangzc.bookuserbiz.Service.UserService;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * 用户信息修改
     * 
     * @param updateUserInfoReqVO
     * @return
     */
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        return userService.updateUserInfo(updateUserInfoReqVO);
    }


    // ===================================== 对其他服务提供的接口 =====================================


    @PostMapping("/findByIds")
    @ApiOperationLog(description = "批量查询用户信息")
    public R<List<FindUserByIdRspDTO>> findByIds(@RequestBody FindUsersByIdsReqDTO findUsersByIdsReqDTO) {
        return userService.findByIds(findUsersByIdsReqDTO);
    }

    @PostMapping("/register")
    @ApiOperationLog(description = "用户注册")
    public R register(@RequestBody Map<String,String> registerUserReqDTO) {
        return userService.register(registerUserReqDTO);
    }

    @PostMapping("/find")
    @ApiOperationLog(description = "根据手机号查询用户信息")
    public R findByPhone(@RequestBody Map<String,String> findUserByPhoneReqDTO) throws BizException {
        return userService.findByPhone(findUserByPhoneReqDTO);
    }


    @PostMapping("/password/update")
    @ApiOperationLog(description = "密码更新")
    public R updatePassword(@RequestBody Map<String,String> updateUserPasswordReqDTO) {
        return userService.updatePassword(updateUserPasswordReqDTO);
    }

    @PostMapping("/findById")
    @ApiOperationLog(description = "查询用户信息")
    public R<FindUserByIdRspDTO> findById(@RequestBody FindUserByIdReqDTO findUserByIdReqDTO) {
        return userService.findById(findUserByIdReqDTO);
    }
    

}

