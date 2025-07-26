package com.zhangzc.bookauth.Contoller;

import com.zhangzc.bookauth.Pojo.User;
import com.zhangzc.fakebookspringbootstartbizoperationlog.Aspect.AspectClass.ApiOperationLog;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class test {


    @GetMapping("/test")
    @ApiOperationLog(description = "测试一下")
    public String test(){
        User user = new User();
        user.setId(1);
        user.setUsername("zhangzc");
        System.out.println(JsonUtils.toJsonString(user));
        return JsonUtils.toJsonString(user);
    }
}
