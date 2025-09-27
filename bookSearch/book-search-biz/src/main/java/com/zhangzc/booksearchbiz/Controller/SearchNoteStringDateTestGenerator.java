package com.zhangzc.booksearchbiz.Controller;

import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteRspVO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 测试数据生成器（适配 String 类型日期 + 指定 format）
 * 生成 100 条合法测试数据，包含三种日期格式
 */
@Component
public class SearchNoteStringDateTestGenerator {

    // 1. 预设常见话题（增加数据真实性）
    private static final String[] TOPIC_LIST = {
            "生活记录", "美食分享", "旅行攻略", "学习笔记", "技术分享",
            "摄影技巧", "健身打卡", "电影推荐", "书籍读后感", "穿搭指南"
    };

    // 2. 预设标题关键词（含易检索词汇，适配搜索场景）
    private static final String[] TITLE_KEYWORDS = {
            "壁纸", "教程", "推荐", "攻略", "心得", "分享", "技巧", "入门", "进阶", "干货"
    };

    // 3. 预设用户昵称前缀
    private static final String[] NICKNAME_PREFIX = {
            "小哈", "阿明", "小柚", "阿泽", "小桃", "阿凯", "小苏", "阿楠", "小夏", "阿哲"
    };

    // 4. 日期格式化器（对应 dateFormat 的前两种格式）
    private static final DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 随机数工具（确保数据多样性）
    private static final Random RANDOM = new Random();

    /**
     * 生成指定数量的测试数据（默认 100 条）
     * @param count 数据条数
     * @return 测试数据集
     */
    public static List<SearchNoteRspVO> generateTestData(int count) {
        List<SearchNoteRspVO> testDataList = new ArrayList<>(count);

        // 循环生成 100 条数据
        for (int i = 1; i <= count; i++) {
            // 构建单条测试数据
            SearchNoteRspVO noteVO = SearchNoteRspVO.builder()
                    // 笔记ID：自定义唯一ID（10001 开始递增，避免重复）
                    .noteId(10000L + i)
                    // 封面：模拟图片URL（使用 picsum 占位图，支持直接访问）
                    .cover("https://picsum.photos/400/300?noteId=" + (10000L + i))

                    // 话题：从预设列表随机选择
                    .topicName(TOPIC_LIST[RANDOM.nextInt(TOPIC_LIST.length)])

                    // 标题：随机组合 2-3 个关键词（如 "旅行攻略-壁纸推荐"）
                    .title(buildRandomTitle())

                    // 高亮标题：模拟 Easy-ES 高亮结果（用 <strong> 包裹随机关键词）
                    .highlightTitle(buildHighlightTitle())

                    // 头像：模拟用户头像URL（100x100 尺寸，随机参数避免缓存）
                    .avatar("https://picsum.photos/100/100?userId=" + RANDOM.nextInt(1000))

                    // 昵称：前缀 + 随机数字（如 "小哈123"）
                    .nickname(NICKNAME_PREFIX[RANDOM.nextInt(NICKNAME_PREFIX.length)] + RANDOM.nextInt(1000))

                    // 核心：String 类型日期（随机生成三种格式之一，适配 dateFormat）
                    .updateTime(generateRandomStringDate())

                    // 点赞数：0-10000，格式化为字符串（如 "123"、"1.2k"）
                    .likeTotal(RANDOM.nextLong(10001))

                    // 收藏数：0-5000，同点赞格式
                    .collectTotal(RANDOM.nextLong(5001))

                    // 评论数：0-2000，Long 类型
                    .commentTotal((long) RANDOM.nextInt(2001))

                    .build();
            //获取0，1，2三个数字随机
            int type = RANDOM.nextInt(3);
            if (type == 0) {
                noteVO.setType(0);
            } else if (type == 1) {
                noteVO.setType(1);
            } else {
                noteVO.setType(null);
            }
            testDataList.add(noteVO);
        }
        return testDataList;
    }

    /**
     * 辅助方法1：生成随机标题（2-3个关键词组合）
     */
    private static String buildRandomTitle() {
        String keyword1 = TITLE_KEYWORDS[RANDOM.nextInt(TITLE_KEYWORDS.length)];
        String keyword2 = TITLE_KEYWORDS[RANDOM.nextInt(TITLE_KEYWORDS.length)];
        // 50% 概率增加第三个关键词，丰富标题多样性
        if (RANDOM.nextBoolean()) {
            String keyword3 = TITLE_KEYWORDS[RANDOM.nextInt(TITLE_KEYWORDS.length)];
            return keyword1 + "-" + keyword2 + "-" + keyword3;
        }
        return keyword1 + "-" + keyword2;
    }

    /**
     * 辅助方法2：生成高亮标题（模拟 <strong> 标签包裹关键词）
     */
    private static String buildHighlightTitle() {
        String rawTitle = buildRandomTitle();
        String[] titleParts = rawTitle.split("-");
        // 随机选择一个关键词进行高亮
        String highlightPart = titleParts[RANDOM.nextInt(titleParts.length)];
        return rawTitle.replace(highlightPart, "<strong>" + highlightPart + "</strong>");
    }

    /**
     * 辅助方法3：生成 String 类型的随机日期（三种格式随机切换）
     * 格式1：yyyy-MM-dd HH:mm:ss（如 "2024-09-25 18:30:45"）
     * 格式2：yyyy-MM-dd（如 "2024-09-25"）
     * 格式3：epoch_millis（毫秒时间戳字符串，如 "1727250645000"）
     */
    private static LocalDateTime generateRandomStringDate() {
        // 生成近 3 个月内的随机时间（作为基础时间）
        LocalDateTime baseTime = LocalDateTime.now()
                .minus(RANDOM.nextInt(91), ChronoUnit.DAYS) // 0-90 天前
                .minus(RANDOM.nextInt(24), ChronoUnit.HOURS) // 0-23 小时前
                .withMinute(RANDOM.nextInt(60))
                .withSecond(RANDOM.nextInt(60));

        return baseTime;

    }

    /**
     * 辅助方法4：格式化数字（如 1234 → "1.2k"，123 → "123"）
     */
    private static String formatNumber(int number) {
        if (number >= 1000) {
            // 千位以上保留 1 位小数，加 "k" 后缀
            return String.format("%.1fk", number / 1000.0);
        } else {
            // 千位以下直接转字符串
            return String.valueOf(number);
        }
    }

    // ------------------------------ 测试入口 ------------------------------
    public static void main(String[] args) {
        // 生成 100 条测试数据
        List<SearchNoteRspVO> testData = generateTestData(100);

        // 打印前 5 条数据预览，验证格式正确性
        System.out.println("=== 生成 100 条测试数据，前 5 条预览 ===");
        for (int i = 0; i < 5; i++) {
            SearchNoteRspVO vo = testData.get(i);
            System.out.printf(
                    "第 %d 条：\n" +
                            "noteId: %s\n" +
                            "title: %s\n" +
                            "highlightTitle: %s\n" +
                            "updateTime（String 类型日期）: %s\n" +
                            "likeTotal: %s, collectTotal: %s, commentTotal: %s\n" +
                            "----------------------------------------\n",
                    i + 1,
                    vo.getNoteId(),
                    vo.getTitle(),
                    vo.getHighlightTitle(),
                    vo.getUpdateTime(), // 重点验证日期格式
                    vo.getLikeTotal(),
                    vo.getCollectTotal(),
                    vo.getCommentTotal()
            );
        }
        System.out.println("=== 总计生成 " + testData.size() + " 条测试数据 ===");
    }
}