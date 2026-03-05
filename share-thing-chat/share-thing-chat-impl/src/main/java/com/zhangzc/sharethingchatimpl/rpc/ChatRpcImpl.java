package com.zhangzc.sharethingchatimpl.rpc;

import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.kafkaspringbootstart.utills.KafkaUtills;
import com.zhangzc.sharethingchatapi.ChatRpc;
import com.zhangzc.sharethingchatimpl.domain.dto.WsMessagePacket;
import com.zhangzc.sharethingchatimpl.enums.NettyUserSendMessage;
import com.zhangzc.sharethingchatimpl.nettySession.NettyUserSession;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@DubboService
@RequiredArgsConstructor
public class ChatRpcImpl implements ChatRpc {

    private final NettyUserSession nettyUserSession;
    private final KafkaUtills kafkaUtills;

    /**
     * 用户发送给用户 (Text)
     */
    @Override
    public Boolean sendMessage(String fromUserId, String toUserId, String content) {
        try {
            log.info("RPC发送消息: {} -> {}: {}", fromUserId, toUserId, content);
            
            // 1. 构建消息包
            WsMessagePacket packet = new WsMessagePacket();
            packet.setCommand(2); // CHAT
            packet.setMsgType(NettyUserSendMessage.TEXT_MESSAGE.getCode()); // 默认为文本
            
            Map<String, Object> data = new HashMap<>();
            data.put("fromUserId", fromUserId);
            data.put("toUserId", toUserId);
            data.put("content", content);
            packet.setData(data);

            String json = JsonUtils.toJsonString(packet);

            // 2. 推送给接收方 (如果在线)
            List<Channel> toChannels = nettyUserSession.getLocalChannelsByUserId(toUserId);
            if (toChannels != null && !toChannels.isEmpty()) {
                for (Channel channel : toChannels) {
                    channel.writeAndFlush(new TextWebSocketFrame(json));
                }
            }

            // 3. 推送给发送方其他设备 (多端同步)
            List<Channel> fromChannels = nettyUserSession.getLocalChannelsByUserId(fromUserId);
            if (fromChannels != null && !fromChannels.isEmpty()) {
                for (Channel channel : fromChannels) {
                    channel.writeAndFlush(new TextWebSocketFrame(json));
                }
            }

            // 4. 发送到 Kafka (异步持久化)
            CompletableFuture.runAsync(() -> {
                try {
                    kafkaUtills.sendMessage(NettyUserSendMessage.TEXT_MESSAGE.getFlag(), json);
                } catch (Exception e) {
                    log.error("RPC消息发送Kafka失败", e);
                }
            });

            return true;
        } catch (Exception e) {
            log.error("RPC发送消息异常", e);
            return false;
        }
    }

    /**
     * 系统发送给用户
     */
    @Override
    public Boolean sendMessage(String toUserId, String content) {
        // 系统消息默认发送者为 "SYSTEM"
        return sendMessage("SYSTEM", toUserId, content);
    }
}
