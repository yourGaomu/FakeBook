package com.zhangzc.redisspringbootstart.redisConst;

public class RedisZHashConst {
    public static final String ARTICLE_MONGO_INFO = "article:mongo:info";

    public static String getArticleMongoInfo(String articleId) {
        return ARTICLE_MONGO_INFO + articleId;
    }

}
