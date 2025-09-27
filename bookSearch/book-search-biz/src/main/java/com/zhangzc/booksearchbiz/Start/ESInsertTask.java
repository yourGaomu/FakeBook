package com.zhangzc.booksearchbiz.Start;


import com.zhangzc.booksearchbiz.Const.EsUpdataSign;
import com.zhangzc.booksearchbiz.Utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ESInsertTask implements ApplicationRunner {

    private final RedisUtil redisUtil;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //构建key
        boolean b = redisUtil.hasKey(EsUpdataSign.ES_UPDATA_SIGN);
        if (!b) {
            //如果不存在则要保存
            CompletableFuture.runAsync(() -> {
                //一天时间
                redisUtil.set(EsUpdataSign.ES_UPDATA_SIGN, "1", 60 * 60 * 24);
                InsertEsData();
            });
        }

    }

    private void InsertEsData() {
        log.info("开始同步Es数据");
    }
}
