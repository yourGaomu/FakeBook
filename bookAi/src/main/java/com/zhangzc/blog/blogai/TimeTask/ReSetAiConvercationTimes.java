package com.zhangzc.blog.blogai.TimeTask;

import com.zhangzc.blog.blogai.Pojo.domain.TAi;
import com.zhangzc.blog.blogai.Service.TAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReSetAiConvercationTimes {

    private final TAiService tAiService;

    //设置定时任务，周期为每一天的凌晨2点进行
    @Scheduled(cron = "0 0 2 * * ?")
    public void reSetAiConvercationTimes() {
        //重置所有人的已经注册了的次数
        log.info("当前的时间是{}，开始重置次数", LocalDate.now());
        CompletableFuture.runAsync(() -> {
        tAiService.lambdaUpdate()
                .eq(TAi::getIsBanned, 0)
                .set(TAi::getChatCount, 100)
                .update();
    });
    }
}
