package com.zhangzc.sharethingchatimpl.nettyService;

import com.zhangzc.sharethingchatimpl.nettyHandle.HeartBeatHandle;
import com.zhangzc.sharethingchatimpl.nettyHandle.MyWsServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyServer {
    // 优化：指定线程数，bossGroup 通常 1 个即可
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        // HTTP 编解码
                        ch.pipeline().addLast(new HttpServerCodec());
                        // 以块方式写入
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        // HTTP 消息聚合，将多个消息部分合并成一个 FullHttpRequest
                        ch.pipeline().addLast(new HttpObjectAggregator(65536));
                        // WebSocket 协议处理，路径为 /ws
                        //添加心跳检测
                        ch.pipeline().addLast(new IdleStateHandler(40, 50, 60));
                        ch.pipeline().addLast(new HeartBeatHandle());
                        // 它会处理握手、Ping/Pong 等控制帧
                        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                        // 添加自定义的业务处理器
                        ch.pipeline().addLast(new MyWsServerHandler());
                    }
                });
        // 绑定端口，同步等待成功
        ChannelFuture future = bootstrap.bind(port).sync();
        // 注意：这里不要调用 closeFuture().sync()，否则会阻塞主线程，导致 Spring Boot 无法启动完成
    }

    public void stop() {
        // 关闭Netty服务
        try{
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
