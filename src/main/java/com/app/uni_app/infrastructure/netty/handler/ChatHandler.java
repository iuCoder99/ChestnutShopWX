package com.app.uni_app.infrastructure.netty.handler;

import com.app.uni_app.infrastructure.netty.manager.UserChannelManager;
import com.app.uni_app.pojo.entity.ChatMessage;
import com.app.uni_app.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * 处理消息收发、业务分发
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final UserChannelManager userChannelManager;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("【Netty】物理连接已建立: {}", ctx.channel().id());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long userId = ctx.channel().attr(UserChannelManager.USER_ID_KEY).get();
        log.warn("【Netty】物理连接已断开: 用户ID={}, ChannelId={}", userId, ctx.channel().id());
        userChannelManager.unbind(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            Long userId = ctx.channel().attr(UserChannelManager.USER_ID_KEY).get();
            log.info("【Netty】WebSocket 握手完成，协议升级成功！用户ID: {}, 终端ID: {}", userId, ctx.channel().id());
        } else if (evt instanceof io.netty.handler.timeout.IdleStateEvent) {
            log.warn("【Netty】连接空闲超时，即将关闭: {}", ctx.channel().id());
            ctx.close();
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TextWebSocketFrame frame) {
            log.info("【Netty】收到文本帧: {}", frame.text());
        } else if (msg instanceof io.netty.handler.codec.http.websocketx.WebSocketFrame) {
            log.info("【Netty】收到非文本帧: {}", msg.getClass().getSimpleName());
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String text = frame.text();
        log.info("【Netty】收到来自用户 {} 的消息帧: {}", ctx.channel().attr(UserChannelManager.USER_ID_KEY).get(), text);

        try {
            JsonNode jsonNode = objectMapper.readTree(text);
            if (!jsonNode.has("action")) {
                log.warn("【Netty】消息缺少 action 字段: {}", text);
                return;
            }
            
            String action = jsonNode.get("action").asText();
            JsonNode data = jsonNode.get("data");
            Long currentUserId = ctx.channel().attr(UserChannelManager.USER_ID_KEY).get();

            log.info("【Netty】处理业务 Action: {}, 用户: {}", action, currentUserId);

            switch (action) {
                case "SEND_MSG":
                    handleSendMsg(ctx, currentUserId, data);
                    break;
                case "READ_REPORT":
                    handleReadReport(currentUserId, data);
                    break;
                case "HEARTBEAT":
                    ctx.channel().writeAndFlush(new TextWebSocketFrame("{\"action\":\"PONG\"}"));
                    break;
                default:
                    log.warn("【Netty】未知 Action: {}", action);
            }
        } catch (Exception e) {
            log.error("【Netty】消息解析异常: {}", e.getMessage());
        }
    }

    private void handleSendMsg(ChannelHandlerContext ctx, Long fromUserId, JsonNode data) throws Exception {
        if (data == null || !data.has("toUserId")) {
            log.warn("【Netty】SEND_MSG 请求缺少必要数据");
            return;
        }

        Long toUserId = data.get("toUserId").asLong();
        String content = data.get("content").asText();
        Integer msgType = data.get("msgType").asInt();
        Long productId = data.has("productId") ? data.get("productId").asLong() : null;

        log.info("【Netty】发送逻辑: from={}, to={}, content={}", fromUserId, toUserId, content);

        ChatMessage message = chatService.saveAndGetMessage(fromUserId, toUserId, content, msgType, productId);

        // 推送给目标
        Channel targetChannel = userChannelManager.getChannel(toUserId);
        if (targetChannel != null && targetChannel.isActive()) {
            String pushJson = objectMapper.writeValueAsString(new Object() {
                public String action = "PUSH_MSG";
                public Object data = new Object() {
                    public Long fromUserId = message.getFromUserId();
                    public Integer msgType = message.getMsgType();
                    public String content = message.getContent();
                    public Long productId = message.getProductId();
                    public String createTime = message.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                };
            });
            targetChannel.writeAndFlush(new TextWebSocketFrame(pushJson));
            log.info("【Netty】消息已推送到目标用户: {}", toUserId);
        }

        // 回复 ACK
        String ackJson = objectMapper.writeValueAsString(new Object() {
            public String action = "MSG_ACK";
            public Object data = new Object() {
                public Long msgId = message.getId();
                public String status = "SUCCESS";
            };
        });
        ctx.channel().writeAndFlush(new TextWebSocketFrame(ackJson));
    }

    private void handleReadReport(Long userId, JsonNode data) {
        if (data != null && data.has("contactId")) {
            Long contactId = data.get("contactId").asLong();
            chatService.clearUnread(userId, contactId);
            log.info("【Netty】用户 {} 已读来自 {} 的消息", userId, contactId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("【Netty】连接出现异常，原因: {}", cause.getMessage());
        ctx.close();
    }
}
