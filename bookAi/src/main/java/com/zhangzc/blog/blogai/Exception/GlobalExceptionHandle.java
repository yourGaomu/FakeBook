package com.zhangzc.blog.blogai.Exception;


import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.fastjson2.JSONObject;
import com.zhangzc.blog.blogai.Utils.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandle {

    // 专门处理SSE接口的异常
    @ExceptionHandler(ApiException.class)
    public void handleSseApiException(ApiException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 判断是否是SSE请求
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("text/event-stream")) {
            // 返回SSE格式的错误信息
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                // 按照SSE格式返回错误
                writer.write("data: " + JSONObject.toJSONString(R.Faile(e.getMessage())) + "\n\n");
                writer.flush();
                // 发送结束信号
                writer.write("data: [DONE]\n\n");
                writer.flush();
            }
        } else {
            // 非SSE请求，返回普通JSON
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            try (PrintWriter writer = response.getWriter()) {
                writer.write(JSONObject.toJSONString(R.Faile(e.getMessage())));
                writer.flush();
            }
        }
    }


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
