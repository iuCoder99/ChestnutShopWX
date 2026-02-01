package com.app.uni_app.infrastructure.netty;

import com.app.uni_app.infrastructure.netty.handler.ChatHandler;
import com.app.uni_app.infrastructure.netty.handler.JwtAuthHandler;
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
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast(new HttpServerCodec());
                                ch.pipeline().addLast(new ChunkedWriteHandler());
                                ch.pipeline().addLast(new HttpObjectAggregator(65536));
                                // 1. 自定义鉴权处理器
                                ch.pipeline().addLast(jwtAuthHandler);
                                // 2. 处理 WebSocket 握手
                                ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws/chat", null, true));
                                // 3. 自定义业务处理器
                                ch.pipeline().addLast(chatHandler);
                            }
                        });

                log.info("Netty Chat Server 正在启动，端口: {}", port);
                ChannelFuture f = b.bind(port).sync();
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Netty 服务启动异常", e);
                Thread.currentThread().interrupt();
            } finally {
                stop();
            }
        }).start();
    }

    @PreDestroy
    public void stop() {
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        log.info("Netty Chat Server 已关闭");
    }
}
