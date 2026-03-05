package com.zhangzc.sharethingchatimpl.domain.dto;

import lombok.Data;

import java.util.Map;

/**
 * WebSocket 通信协议包
 * 所有的 WebSocket 消息都应该符合这个格式
 */
@Data
public class WsMessagePacket {

    /**
     * 命令类型 (区分业务逻辑)
     * - 1: LOGIN (登录/认证)
     * - 2: CHAT (单聊消息)
     * - 3: GROUP_CHAT (群聊消息)
     * - 4: HEARTBEAT (心跳)
     * - 100: ACK (消息确认)
     */
    private Integer command;

    /**
     * 消息类型 (当 command 为 2: CHAT 或 3: GROUP_CHAT 时有效)
     * @see com.zhangzc.sharethingchatimpl.enums.NettyUserSendMessage
     * 1: 文本
     * 2: 图片
     * 3: 语音
     * 4: 视频
     */
    private Integer msgType;

    /**
     * 业务数据 (对应具体的 Payload)
     * 可以是 ChatMessage 的部分字段，或者登录 Token 等
     */
    private Map<String, Object> data;
}
