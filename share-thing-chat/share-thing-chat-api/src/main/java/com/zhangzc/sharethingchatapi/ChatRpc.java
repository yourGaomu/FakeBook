package com.zhangzc.sharethingchatapi;

public interface ChatRpc {
    Boolean sendMessage(String fromUserId, String toUserId, String content);

    Boolean sendMessage(String toUserId, String content);
}
