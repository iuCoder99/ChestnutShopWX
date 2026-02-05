package com.app.uni_app.infrastructure.netty;

import com.app.uni_app.infrastructure.netty.handler.ChatHandler;
import com.app.uni_app.infrastructure.netty.handler.JwtAuthHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Netty 服务启动类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatNettyServer {

    @Value("${netty.port}")
    private int port;

    private final JwtAuthHandler jwtAuthHandler;
    private final ChatHandler chatHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new HttpServerCodec());
                                pipeline.addLast(new ChunkedWriteHandler());
                                pipeline.addLast(new HttpObjectAggregator(65536));
                                // 增加空闲状态检测，60秒没读写则触发事件
                                pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                                // 1. 自定义鉴权处理器
                                pipeline.addLast(jwtAuthHandler);
                                // 2. 处理 WebSocket 握手 (关闭 extensions 以提高兼容性)
                                pipeline.addLast(new WebSocketServerProtocolHandler("/ws/chat", null, false, 65536));
                                // 3. 自定义业务处理器
                                pipeline.addLast(chatHandler);
                            }
                        });

                log.info("【Netty】服务已启动，监听端口: {}", port);
                ChannelFuture f = b.bind(port).sync();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("【Netty】服务异常: ", e);
            } finally {
                stop();
            }
        }).start();
    }

    @PreDestroy
    public void stop() {
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        log.info("【Netty】Netty Chat Server 已关闭");
    }
}
