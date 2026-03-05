package com.zhangzc.sharethingchatimpl.nettyHandle;

import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.kafkaspringbootstart.utills.KafkaUtills;
import com.zhangzc.leaf.server.service.SegmentService;
import com.zhangzc.sharethingchatimpl.enums.NettyUserSendMessage;
import com.zhangzc.sharethingchatimpl.domain.dto.WsMessagePacket;
import com.zhangzc.sharethingchatimpl.nettySession.NettyUserSession;
import com.zhangzc.sharethingchatimpl.utils.SpringContextUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MyWsServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    // 管理所有连接的 ChannelGroup，GlobalEventExecutor.INSTANCE 是全局单例执行器
    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        return SpringContextUtil.getBean("threadPoolTaskExecutor", ThreadPoolTaskExecutor.class);
    }

    private KafkaUtills getKafkaUtills() {
        return SpringContextUtil.getBean(KafkaUtills.class);
    }

    private NettyUserSession getNettyUserSession() {
        return SpringContextUtil.getBean(NettyUserSession.class);
    }

    private SegmentService getSegmentService() {
        return SpringContextUtil.getBean(SegmentService.class);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("{} 上线了", channel.remoteAddress());
        channelGroup.add(channel);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("{} 下线了", channel.remoteAddress());
        getNettyUserSession().removeSession(channel);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Channel currentChannel = ctx.channel();
        String content = msg.text();
        log.info("收到消息: {}", content);

        WsMessagePacket packet;
        try {
            packet = JsonUtils.parseObject(content, WsMessagePacket.class);
        } catch (Exception e) {
            log.warn("消息格式错误，无法反序列化: {}, 错误信息: {}", content, e.getMessage());
            return;
        }

        if (packet == null || packet.getCommand() == null) {
            return;
        }

        NettyUserSession userSession = getNettyUserSession();
        WsMessagePacket message = null;
        switch (packet.getCommand()) {
            case 1: // LOGIN
                message = handleLogin(currentChannel, packet.getData(), userSession);
                break;
            case 2: // CHAT
                message = handleChat(currentChannel, packet, userSession);
                break;
            case 4: // HEARTBEAT
                message = handleHeartbeat(currentChannel);
                break;
            default:
                log.warn("未知指令: {}", packet.getCommand());
        }
        //发送kafka消息存储起来
        //只有发送消息才可以传入
        if (message != null && packet.getCommand() == 2) {
            WsMessagePacket finalMessage = message;
            CompletableFuture.runAsync(() -> {
                try {
                    Integer type = packet.getMsgType();
                    // 默认为文本消息
                    if (type == null) {
                        type = 1;
                    }
                    NettyUserSendMessage nettyUserSendMessage = NettyUserSendMessage.getByCode(type);
                    getKafkaUtills().sendMessage(nettyUserSendMessage.getFlag(), finalMessage);
                } catch (Exception e) {
                    log.error("处理消息类型或发送Kafka失败", e);
                }
            }, getThreadPoolTaskExecutor());
        }
    }

    private WsMessagePacket handleLogin(Channel channel, Map<String, Object> data, NettyUserSession userSession) {
        if (data == null || !data.containsKey("userId")) {
            return null;
        }
        String userId = String.valueOf(data.get("userId"));
        userSession.addSession(userId, channel);
        // 回复登录成功
        WsMessagePacket resp = new WsMessagePacket();
        resp.setCommand(100); // ACK
        Map<String, Object> respData = new HashMap<>();
        respData.put("type", "login");
        respData.put("status", "success");
        resp.setData(respData);
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.toJsonString(resp)));
        return resp;
    }

    private WsMessagePacket handleChat(Channel channel, WsMessagePacket packet, NettyUserSession userSession) {
        Map<String, Object> data = packet.getData();
        if (data == null)
            return null;

        String toUserId = (String) data.get("toUserId");
        if (toUserId == null)
            return null;
        // 1. 构建转发/存储的消息包
        WsMessagePacket forwardPacket = new WsMessagePacket();
        // 补充发送者信息 (防止客户端伪造)
        String fromUserId = userSession.getUserIdByChannelId(channel.id().asLongText());
        data.put("fromUserId", fromUserId);

        forwardPacket.setCommand(2); // CHAT
        forwardPacket.setMsgType(packet.getMsgType()); // 转发时保留消息类型
        forwardPacket.setData(data);

        // 2. 查找目标用户的所有 Channel
        List<Channel> toChannels = userSession.getLocalChannelsByUserId(toUserId);
        if (toChannels != null && !toChannels.isEmpty()) {
            // 转发消息
            String json = JsonUtils.toJsonString(forwardPacket);
            for (Channel toChannel : toChannels) {
                toChannel.writeAndFlush(new TextWebSocketFrame(json));
            }
        } else {
            // 用户不在线，仅记录日志，后续由Kafka消费者处理存储
            log.info("用户 {} 不在线，消息将存入数据库", toUserId);
        }

        // 3. 多端同步：推给发送者自己的其他设备 (Sync to sender's other devices)
        List<Channel> myChannels = userSession.getLocalChannelsByUserId(fromUserId);
        if (myChannels != null && !myChannels.isEmpty()) {
            String json = JsonUtils.toJsonString(forwardPacket);
            String currentChannelId = channel.id().asLongText();
            for (Channel myChannel : myChannels) {
                // 排除当前发送消息的设备，避免重复
                if (!myChannel.id().asLongText().equals(currentChannelId)) {
                    myChannel.writeAndFlush(new TextWebSocketFrame(json));
                }
            }
        }

        return forwardPacket;
    }

    private WsMessagePacket handleHeartbeat(Channel channel) {
        WsMessagePacket pong = new WsMessagePacket();
        pong.setCommand(4);
        Map<String, Object> respData = new HashMap<>();
        respData.put("msg", "PONG");
        pong.setData(respData);
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.toJsonString(pong)));
        return pong;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket异常", cause);
        ctx.close();
    }
}
