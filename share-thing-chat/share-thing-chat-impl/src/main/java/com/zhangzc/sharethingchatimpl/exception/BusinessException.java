package com.zhangzc.sharethingchatimpl.exception;


import com.zhangzc.sharethingchatimpl.enums.ResponseCodeInterface;
import lombok.Getter;

/**
 * 全局业务异常父类（非受检异常）
 */
@Getter // Lombok 注解，自动生成 getter 方法（无需手动写）
public class BusinessException extends RuntimeException {
    // 错误码（前端可根据 code 做不同处理，如 401 未登录、403 无权限）
    private final String code;
    // 附加数据（可选，如返回错误详情）
    private final Object data;

    // 1. 无参构造（默认错误码 500，消息 "系统异常"）
    public BusinessException() {
        this("500", "系统异常", null);
    }

    // 2. 仅传错误消息（默认错误码 500）
    public BusinessException(String message) {
        this("500", message, null);
    }

    // 3. 传错误码 + 消息（最常用）
    public BusinessException(String code, String message) {
        this(code, message, null);
    }

    // 4. 传错误码 + 消息 + 附加数据
    public BusinessException(String code, String message, Object data) {
        super(message); // 调用父类 RuntimeException 的构造方法，保留消息
        this.code = code;
        this.data = data;
    }

    // 5. 传错误码 + 消息 + 原始异常（保留异常链，便于排查问题）
    public BusinessException(String  code, String message, Throwable cause) {
        super(message, cause); // 传递原始异常，保留堆栈信息
        this.code = code;
        this.data = null;
    }

    public BusinessException(ResponseCodeInterface responseCodeInterface) {
        this.code = responseCodeInterface.getCode();
        this.data = responseCodeInterface.getMeg();
    }

}