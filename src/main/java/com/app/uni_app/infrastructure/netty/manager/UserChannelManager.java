package com.app.uni_app.infrastructure.netty.manager;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 userId 与 Channel 的映射
 */
@Component
public class UserChannelManager {

    public static final AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf("userId");

    /**
     * userId -> Channel
     */
    private static final Map<Long, Channel> USER_CHANNELS = new ConcurrentHashMap<>();

    public void bind(Long userId, Channel channel) {
        USER_CHANNELS.put(userId, channel);
        channel.attr(USER_ID_KEY).set(userId);
    }

    public void unbind(Channel channel) {
        Long userId = channel.attr(USER_ID_KEY).get();
        if (userId != null) {
            USER_CHANNELS.remove(userId);
        }
    }

    public Channel getChannel(Long userId) {
        return USER_CHANNELS.get(userId);
    }

    public boolean isOnline(Long userId) {
        Channel channel = USER_CHANNELS.get(userId);
        return channel != null && channel.isActive();
    }
}
