package com.zhangzc.bookuserbiz.ExceptionHands;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
@Slf4j
@ResponseBody
public class GlobalExceptionHandle{

    @ExceptionHandler(value=BizException.class)
    public R handleException(HttpServletRequest request, BizException e){
        log.warn("{} request fail, errorCode: {}, errorMessage: {}", request.getRequestURI(), e.getCode(), e.getMessage());
        return R.fail(e);
    }

    @ExceptionHandler(value = Exception.class)
    public R handleException(HttpServletRequest request,Exception e) {
        log.warn("{} request fail, errorMessage: {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getMessage());
    }
}
