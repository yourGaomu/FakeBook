package com.zhangzc.sharethingchatimpl.enums;

import lombok.Getter;

/**
 * Netty用户消息发送相关枚举
 * 定义消息类型、状态等常量
 */
@Getter
public enum NettyUserSendMessage {

    // 消息类型 - 文本消息
    TEXT_MESSAGE(1, "text", "文本消息"),
    // 消息类型 - 图片消息
    IMAGE_MESSAGE(2, "image", "图片消息"),
    // 消息类型 - 语音消息
    VOICE_MESSAGE(3, "voice", "语音消息"),
    // 消息类型 - 视频消息
    VIDEO_MESSAGE(4, "video", "视频消息"),
    // 消息发送状态 - 发送中
    SENDING(10, "sending", "消息发送中"),
    // 消息发送状态 - 发送成功
    SEND_SUCCESS(11, "success", "消息发送成功"),
    // 消息发送状态 - 发送失败
    SEND_FAILED(12, "failed", "消息发送失败");

    // 获取编码
    // 枚举编码
    private final int code;
    // 获取标识
    // 枚举标识（英文）
    private final String flag;
    // 获取描述
    // 枚举描述
    private final String desc;

    /**
     * 构造方法
     * @param code 编码
     * @param flag 标识
     * @param desc 描述
     */
    NettyUserSendMessage(int code, String flag, String desc) {
        this.code = code;
        this.flag = flag;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举项（可选扩展方法）
     * @param code 编码
     * @return 对应的枚举项
     */
    public static NettyUserSendMessage getByCode(int code) {
        for (NettyUserSendMessage message : values()) {
            if (message.getCode() == code) {
                return message;
            }
        }
        throw new IllegalArgumentException("无效的消息编码: " + code);
    }

    /**
     * 根据标识获取枚举项（可选扩展方法）
     * @param flag 标识
     * @return 对应的枚举项
     */
    public static NettyUserSendMessage getByFlag(String flag) {
        for (NettyUserSendMessage message : values()) {
            if (message.getFlag().equals(flag)) {
                return message;
            }
        }
        throw new IllegalArgumentException("无效的消息标识: " + flag);
    }
}