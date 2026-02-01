package com.app.uni_app.infrastructure.netty.handler;

import com.app.uni_app.common.constant.JwtTokenClaimsConstant;
import com.app.uni_app.common.util.JwtUtils;
import com.app.uni_app.infrastructure.netty.manager.UserChannelManager;
import com.app.uni_app.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手拦截与 Token 校验
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class JwtAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final JwtProperties jwtProperties;
    private final UserChannelManager userChannelManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        if (!uri.startsWith("/ws/chat")) {
            ctx.close();
            return;
        }

        // 提取 token
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        Map<String, List<String>> parameters = decoder.parameters();
        List<String> tokens = parameters.get("token");

        if (tokens == null || tokens.isEmpty()) {
            log.warn("WebSocket 握手失败: 未携带 Token");
            ctx.close();
            return;
        }

        String token = tokens.get(0);
        try {
            // 校验 Token
            Claims claims = JwtUtils.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtTokenClaimsConstant.SYS_USER_ID).toString());

            // 绑定用户与 Channel
            userChannelManager.bind(userId, ctx.channel());
            log.info("WebSocket 连接成功: userId={}", userId);

            // 传递给下一个处理器 (WebSocketServerProtocolHandler)
            request.setUri("/ws/chat"); // 重置 URI 供后续协议处理器匹配
            ctx.fireChannelRead(request.retain());

        } catch (Exception e) {
            log.error("WebSocket 握手失败: Token 无效, error={}", e.getMessage());
            ctx.close();
        }
    }
}
