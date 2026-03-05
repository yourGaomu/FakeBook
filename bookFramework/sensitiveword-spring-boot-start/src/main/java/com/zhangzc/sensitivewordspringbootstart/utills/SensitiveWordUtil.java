package com.zhangzc.sensitivewordspringbootstart.utills;

import com.zhangzc.sensitivewordspringbootstart.core.MySensitiveWordHelper;
import com.zhangzc.sensitivewordspringbootstart.core.MyWordReplace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@RequiredArgsConstructor
@Component
public class SensitiveWordUtil {
    private final MyWordReplace myWordReplace;
    /*
    * 替换敏感词
    * */
    public String replaceSensitiveWord(String text) {
        //断言机制
        Assert.hasText(text, "text must not be empty");
        return MySensitiveWordHelper.replace(text, myWordReplace);
    }

}
