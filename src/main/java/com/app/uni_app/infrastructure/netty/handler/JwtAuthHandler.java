package com.app.uni_app.infrastructure.netty.handler;

import com.app.uni_app.common.constant.JwtTokenClaimsConstant;
import com.app.uni_app.common.util.JwtUtils;
import com.app.uni_app.infrastructure.netty.manager.UserChannelManager;
import com.app.uni_app.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手拦截与 Token 校验 (底层适配版)
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class JwtAuthHandler extends ChannelInboundHandlerAdapter {

    private final JwtProperties jwtProperties;
    private final UserChannelManager userChannelManager;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest request)) {
            ctx.fireChannelRead(msg);
            return;
        }

        String uri = request.uri();
        
        // 1. 过滤非聊天路径
        if (!uri.startsWith("/ws/chat")) {
            ctx.fireChannelRead(msg);
            return;
        }

        log.info("【Netty】收到握手请求: URI={}", uri);

        try {
            // 2. 提取并校验 Token
            QueryStringDecoder decoder = new QueryStringDecoder(uri);
            Map<String, List<String>> parameters = decoder.parameters();
            List<String> tokens = parameters.get("token");

            if (tokens == null || tokens.isEmpty()) {
                log.error("【Netty】握手失败: 缺少 Token");
                ctx.close();
                ReferenceCountUtil.release(msg);
                return;
            }

            String token = tokens.get(0);
            Claims claims = JwtUtils.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtTokenClaimsConstant.SYS_USER_ID).toString());

            // 3. 绑定用户信息
            userChannelManager.bind(userId, ctx.channel());
            log.info("【Netty】身份校验通过: userId={}", userId);

            // 4. 重置 URI 并传递给下一个处理器 (WebSocketServerProtocolHandler)
            request.setUri("/ws/chat");
            ctx.fireChannelRead(request); // 这里不需要 retain，因为我们没释放， ownership 转移给下一个 handler

        } catch (Exception e) {
            log.error("【Netty】鉴权失败: {}", e.getMessage());
            ctx.close();
            ReferenceCountUtil.release(msg);
        }
    }
}
