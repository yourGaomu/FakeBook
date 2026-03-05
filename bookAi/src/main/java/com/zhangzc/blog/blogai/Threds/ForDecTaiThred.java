package com.zhangzc.blog.blogai.Threds;


import com.zhangzc.blog.blogai.Pojo.domain.TAi;
import com.zhangzc.blog.blogai.Service.TAiService;

import java.util.concurrent.Callable;

public class ForDecTaiThred implements Callable<String> {

    private String Id;
    private final TAiService tAiService;

    public ForDecTaiThred(TAiService tAiService,String Id) {
        this.tAiService = tAiService;
        this.Id = Id;
    }

    @Override
    public String call() throws Exception {
        boolean update = tAiService.lambdaUpdate()
                .eq(TAi::getId, Id)
                .setSql("chat_count = chat_count - 1") // 使用 SQL 语句实现减一
                .update();
        return update?"Chat count decremented successfully":"Chat count decremented failed";
    }
}
