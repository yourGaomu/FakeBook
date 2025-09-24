package com.zhangzc.booksearchbiz.Controller;


import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchUserReqVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchUserRspVO;
import com.zhangzc.booksearchbiz.Service.UserService;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user")
    @ApiOperationLog(description = "搜索用户")
    public PageResponse<SearchUserRspVO> searchUser(@RequestBody @Validated SearchUserReqVO searchUserReqVO) {
        return userService.searchUser(searchUserReqVO);
    }


}
