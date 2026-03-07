package com.zhangzc.bookcommon.Exceptions;

import lombok.Data;

@Data
public class BizException extends Exception {
    public String msg;
    public String code;

    public BizException(ExceptionInterface exceptionInterface) {
        this.code = exceptionInterface.getCode();
        this.msg = exceptionInterface.getMeg();
    }
    public BizException(String reason) {
        this.code = "500";
        this.msg = reason;
    }

}
