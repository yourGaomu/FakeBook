package com.zhangzc.blog.blogai.Utils;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class R implements Serializable {
    public String status;
    public String message;
    public Object data;
    public String errorCode;
    public Boolean success;


    public R() {

    }


    public static R Success() {
        R result = new R();
        result.setStatus("200");
        result.setMessage("success");
        result.setData(null);
        result.setErrorCode(null);
        result.setSuccess(true);
        return result;
    }

    public static R Success(String msg) {
        R result = new R();
        result.setStatus("200");
        result.setMessage(msg);
        result.setData(null);
        result.setErrorCode(null);
        result.setSuccess(true);
        return result;
    }

    public static R Success(String msg, Object data) {
        R result = Success(msg);
        result.setData(data);
        return result;
    }


    public static R Faile(String msg) {
        R result = new R();
        result.setStatus("500");
        result.setMessage(msg);
        result.setData(null);
        result.setErrorCode("500");
        result.setSuccess(false);
        return result;
    }

    public static R Faile(Exception e) {
        R result = new R();
        result.setStatus("500");
        result.setMessage(e.getMessage());
        result.setData(null);
        result.setErrorCode("500");
        result.setSuccess(false);
        return result;
    }

    public static R Boolen(boolean b) {
        R result = new R();
        if (b) {
            result.setStatus("200");
            result.setMessage("操作成功");
            result.setSuccess(true);
        } else  {
            result.setStatus("500");
            result.setMessage("操作失败");
            result.setSuccess(false);
        }
        return result;
    }

    public static R Boolen(boolean b, String msg) {
        R result = new R();
        if (b) {
            result.setStatus("200");
            result.setSuccess(true);
            result.setMessage(msg);
        }else {
            result.setStatus("500");
            result.setSuccess(false);
            result.setMessage(msg);
        }
        return result;
    }

    public static R Boolen(boolean b, String msg, Object data) {
        R result = new R();
        result.setData(data);
        if (b){
            result.setStatus("200");
            result.setMessage(msg);
            result.setSuccess(true);
        }else {
            result.setStatus("500");
            result.setMessage(msg);
            result.setSuccess(false);
        }
        return result;

    }

}
