package com.zhangzc.bookcountbiz.Controller;


import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountapi.Pojo.Dto.Req.FindUserCountsByIdReqDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.Resp.FindUserCountsByIdRspDTO;
import com.zhangzc.bookcountbiz.Service.UserCountService;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/count")
@Slf4j
public class UserCountController {

    @Resource
    private UserCountService userCountService;

    @PostMapping(value = "/user/data")
    @ApiOperationLog(description = "获取用户计数数据")
    public R<FindUserCountsByIdRspDTO> findUserCountData(@RequestBody FindUserCountsByIdReqDTO findUserCountsByIdReqDTO) throws BizException {
        return userCountService.findUserCountData(findUserCountsByIdReqDTO);
    }

}

