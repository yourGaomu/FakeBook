package com.zhangzc.bookrelationbiz.Controller;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookrelationbiz.Pojo.Vo.FollowUserReqVO;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhangzc.bookrelationbiz.Service.RelationService;


@RestController
@RequestMapping("/relation")
@Slf4j
@RequiredArgsConstructor
public class RelationController {


    private final RelationService relationService;

    @PostMapping("/follow")
    @ApiOperationLog(description = "关注用户")
    public R follow(@RequestBody FollowUserReqVO followUserReqVO) {
        return relationService.follow(followUserReqVO);
    }

}
