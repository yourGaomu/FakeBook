package com.zhangzc.bookuserbiz.Const;

public interface MQConstants {
    /**
    * 因为用户信息修改了所以要进行延时双删
     * * */
    String TOPIC_DELAY_USER_INFO_UPDATE = "DelayUserInfoUpdateTopic";
}
