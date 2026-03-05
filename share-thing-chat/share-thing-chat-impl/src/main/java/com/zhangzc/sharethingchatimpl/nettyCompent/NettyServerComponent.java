package com.zhangzc.sharethingchatimpl.nettyCompent;

import com.zhangzc.sharethingchatimpl.nettyService.NettyServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NettyServerComponent {

    @Value("${netty.server.port:9000}")
    private int port;

    private NettyServer nettyServer;

    @PostConstruct
    public void startServer() {
        nettyServer = new NettyServer(port);
        try {
            nettyServer.start(); // 启动Netty服务
            log.info("Netty服务启动成功，监听端口: {}", port);
        } catch (Exception e) {
            log.error("Netty服务启动失败", e);
            // 抛出异常以终止Spring容器启动，避免应用在部分组件失败的情况下运行
            throw new RuntimeException("Netty服务启动失败", e);
        }
    }

    @PreDestroy
    public void stopServer() {
        if (nettyServer != null) {
            nettyServer.stop();
            log.info("Netty服务已停止");
        }
    }
}
