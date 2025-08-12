package com.zhangzc.fakebookossapi.Api;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.fakebookossapi.Const.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;


@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface FileFeignApi {

    String PREFIX = "/file";

    @PostMapping(value = PREFIX + "/test")
    R test();

}
