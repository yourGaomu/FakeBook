package com.zhangzc.sharethingchatimpl.nettyHandle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

@Slf4j
public class MyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 打印请求日志
        log.info("收到请求: {} {}", request.method(), request.uri());

        // 构造响应内容
        ByteBuf content = Unpooled.copiedBuffer("hello", CharsetUtil.UTF_8);

        // 创建响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                content);

        // 设置头信息
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

        // 处理 Keep-Alive
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        } else {
            ctx.writeAndFlush(response).addListener(io.netty.channel.ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Netty处理异常", cause);
        ctx.close();
    }
}
