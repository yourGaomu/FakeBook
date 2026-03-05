package com.zhangzc.sharethingchatimpl.nettyHandle;


import com.zhangzc.sharethingchatimpl.nettySession.NettyUserSession;
import com.zhangzc.sharethingchatimpl.utils.SpringContextUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatHandle extends ChannelInboundHandlerAdapter {

    public NettyUserSession getNettyUserSession() {
        return SpringContextUtil.getBean(NettyUserSession.class);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //如果是心跳机制的事件发生
        if (evt instanceof IdleStateEvent idleStateEvent) {
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    //读空闲
                    break;
                case WRITER_IDLE:
                    //写空闲
                    break;
                case ALL_IDLE:
                    //全程空闲
                    handleAllIdle(ctx, idleStateEvent);
                    break;
            }
        }
    }

    private void handleAllIdle(ChannelHandlerContext ctx, IdleStateEvent idleStateEvent) throws InterruptedException {
        //进行channel的关闭以及redis的释放
        Channel channel = ctx.channel();
        try {
            getNettyUserSession().removeSession(channel);
        } finally {
           ctx.close().sync();
        }
    }
}
