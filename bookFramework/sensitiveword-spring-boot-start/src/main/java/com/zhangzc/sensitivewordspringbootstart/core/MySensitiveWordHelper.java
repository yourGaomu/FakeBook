package com.zhangzc.sensitivewordspringbootstart.core;


import com.github.houbb.sensitive.word.api.IWordReplace;
import com.github.houbb.sensitive.word.api.IWordResultHandler;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.check.WordChecks;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.github.houbb.sensitive.word.support.replace.WordReplaces;
import com.github.houbb.sensitive.word.support.resultcondition.WordResultConditions;
import com.github.houbb.sensitive.word.support.tag.WordTags;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MySensitiveWordHelper {
    /**
     * 1	ignoreCase	忽略大小写	true
     * 2	ignoreWidth	忽略半角圆角	true
     * 3	ignoreNumStyle	忽略数字的写法	true
     * 4	ignoreChineseStyle	忽略中文的书写格式	true
     * 5	ignoreEnglishStyle	忽略英文的书写格式	true
     * 6	ignoreRepeat	忽略重复词	false
     * 7	enableNumCheck	是否启用数字检测。	false
     * 8	enableEmailCheck	是有启用邮箱检测	false
     * 9	enableUrlCheck	是否启用链接检测	false
     * 10	enableIpv4Check	是否启用IPv4检测	false
     * 11	enableWordCheck	是否启用敏感单词检测	true
     * 12	numCheckLen	数字检测，自定义指定长度。	8
     * 13	wordTag	词对应的标签	none
     * 14	charIgnore	忽略的字符	none
     * 15	wordResultCondition	针对匹配的敏感词额外加工，比如可以限制英文单词必须全匹配	恒为真
     * 16	wordCheckNum	数字检测策略(v0.25.0开始支持)	WordChecks.num()
     * 17	wordCheckEmail	邮箱检测策略(v0.25.0开始支持)	WordChecks.email()
     * 18	wordCheckUrl	URL检测策略(v0.25.0开始支持)	(WordChecks.url()
     * 19	wordCheckIpv4	ipv4检测策略(v0.25.0开始支持)	WordChecks.ipv4()
     * 20	wordCheckWord	敏感词检测策略(v0.25.0开始支持)	WordChecks.word()
     * 21	wordReplace	替换策略	WordReplaces.defaults()
     * 22	wordFailFast	敏感词匹配模式是否快速返回	true
     * 23	wordFormatText	文本整体级别的格式化处理策略(v0.28.0)	WordFormatTexts.defaults()
     * 24	wordWarmUp	预热策略(v0.29.0)	WordWarmUps.defaults()
     * */

    private static final SensitiveWordBs WORD_BS = SensitiveWordBs.newInstance()
            .ignoreCase(true)
            .ignoreWidth(true)
            .ignoreNumStyle(true)
            .ignoreChineseStyle(true)
            .ignoreEnglishStyle(true)
            .ignoreRepeat(false)
            .enableNumCheck(false)
            .enableEmailCheck(false)
            .enableUrlCheck(false)
            .enableIpv4Check(false)
            .enableWordCheck(true)
            .wordFailFast(true)
            .wordCheckNum(WordChecks.num())
            .wordCheckEmail(WordChecks.email())
            .wordCheckUrl(WordChecks.url())
            .wordCheckIpv4(WordChecks.ipv4())
            .wordCheckWord(WordChecks.word())
            .numCheckLen(8)
            .wordTag(WordTags.none())
            .charIgnore(SensitiveWordCharIgnores.defaults())
            .wordResultCondition(WordResultConditions.alwaysTrue())
            .init();

    public static boolean contains(String target) {
        return WORD_BS.contains(target);
    }

    public static List<String> findAll(String target) {
        return WORD_BS.findAll(target);
    }

    public static String findFirst(String target) {
        return WORD_BS.findFirst(target);
    }

    public static String replace(String target, IWordReplace replace) {
        SensitiveWordBs sensitiveWordBs = SensitiveWordBs.newInstance().wordReplace(replace).init();
        return sensitiveWordBs.replace(target);
    }

    public static String replace(String target, char replaceChar) {
        IWordReplace replace = WordReplaces.chars(replaceChar);
        return replace(target, replace);
    }

    public static String replace(String target) {
        return WORD_BS.replace(target);
    }

    public static <R> List<R> findAll(String target, IWordResultHandler<R> handler) {
        return WORD_BS.findAll(target, handler);
    }

    public static <R> R findFirst(String target, IWordResultHandler<R> handler) {
        return (R)WORD_BS.findFirst(target, handler);
    }

}
