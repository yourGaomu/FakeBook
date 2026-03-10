package com.zhangzc.smsspringbootstart.service;

public interface smsSender {
    public boolean sendMessage(String signName, String templateCode, String phone, String templateParam);
}
