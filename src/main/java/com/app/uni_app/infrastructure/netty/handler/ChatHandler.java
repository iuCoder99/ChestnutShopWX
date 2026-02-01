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
    private final ObjectMapper objectMapper; // Spring 容器中的 Jackson ObjectMapper

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String text = frame.text();
        log.debug("收到消息: {}", text);

        JsonNode jsonNode = objectMapper.readTree(text);
        String action = jsonNode.get("action").asText();
        JsonNode data = jsonNode.get("data");

        Long currentUserId = ctx.channel().attr(UserChannelManager.USER_ID_KEY).get();

        switch (action) {
            case "SEND_MSG":
                handleSendMsg(currentUserId, data);
                break;
            case "READ_REPORT":
                handleReadReport(currentUserId, data);
                break;
            case "HEARTBEAT":
                // 响应心跳 (可选)
                break;
            default:
                log.warn("未知 Action: {}", action);
        }
    }

    private void handleSendMsg(Long fromUserId, JsonNode data) throws Exception {
        Long toUserId = data.get("toUserId").asLong();
        String content = data.get("content").asText();
        Integer msgType = data.get("msgType").asInt();
        Long productId = data.has("productId") ? data.get("productId").asLong() : null;

        // 1. 持久化并更新会话
        ChatMessage message = chatService.saveAndGetMessage(fromUserId, toUserId, content, msgType, productId);

        // 2. 如果目标在线，实时推送
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
        }

        // 3. 给发送者一个 ACK (可选)
        Channel currentChannel = userChannelManager.getChannel(fromUserId);
        if (currentChannel != null) {
            String ackJson = objectMapper.writeValueAsString(new Object() {
                public String action = "MSG_ACK";
                public Object data = new Object() {
                    public Long msgId = message.getId();
                    public String status = "SUCCESS";
                };
            });
            currentChannel.writeAndFlush(new TextWebSocketFrame(ackJson));
        }
    }

    private void handleReadReport(Long userId, JsonNode data) {
        Long contactId = data.get("contactId").asLong();
        chatService.clearUnread(userId, contactId);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        userChannelManager.unbind(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Netty 异常: {}", cause.getMessage());
        ctx.close();
    }
}
