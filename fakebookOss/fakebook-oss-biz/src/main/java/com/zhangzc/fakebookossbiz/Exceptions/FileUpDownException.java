package com.zhangzc.fakebookossbiz.Exceptions;

import com.zhangzc.bookcommon.Exceptions.ExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum FileUpDownException implements ExceptionInterface {


    // ----------- 通用异常状态码 -----------
    FILE_UPLOAD_ERROR("10000", "文件上传失败"),


        // ----------- 业务异常状态码 -----------
    ;

    private final String meg;
    private final String code;

}
