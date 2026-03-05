package com.zhangzc.redisspringbootstart.redisConst;

public class RedisSetConst {

    public static final String LIKE_SET = "user:like:articles:";

    public static final String FOLLOW_SET = "user:follow:users:";

    public static String getLikeSetKey(String userId) {
        return LIKE_SET + userId;
    }

    public static String getFollowSetKey(String userId) {
        return FOLLOW_SET + userId;
    }


}
