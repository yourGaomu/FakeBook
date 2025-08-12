package com.zhangzc.fakebookossapi.test;

import com.zhangzc.fakebookossapi.Api.FileFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class text {

    private final FileFeignApi fileFeignApi;
}
