package com.zhangzc.blog.blogai.Exception;


import com.zhangzc.blog.blogai.Utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandle {

    @ExceptionHandler(value = IsEmptyForQN.class)
    public R exceptionHandler(IsEmptyForQN e) {
        log.error(e.getMessage());
        return R.Faile(e.getMessage());
    }


    @ExceptionHandler(value = IsNoRole.class)
    public R exceptionHandler(IsNoRole e) {
        log.error(e.getMessage());
        return R.Faile(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public  R exceptionHandler(Exception e) {
        log.error(e.getMessage());
        return R.Faile(e.getMessage());
    }

}
