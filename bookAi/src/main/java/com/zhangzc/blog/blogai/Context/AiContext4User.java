package com.zhangzc.blog.blogai.Context;

/**
 * 用户对话上下文，用于在 Tool 执行时获取用户意图
 */
public class AiContext4User {
    private static final ThreadLocal<Boolean> ENABLE_WEB_SEARCH = new ThreadLocal<>();

    public static void setEnableWebSearch(Boolean enable) {
        ENABLE_WEB_SEARCH.set(enable);
    }

    public static Boolean getEnableWebSearch() {
        return ENABLE_WEB_SEARCH.get();
    }

    public static void clear() {
        ENABLE_WEB_SEARCH.remove();
    }
}
