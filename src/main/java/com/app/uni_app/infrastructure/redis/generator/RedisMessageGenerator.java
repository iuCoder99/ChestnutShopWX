package com.app.uni_app.infrastructure.redis.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RedisMessageGenerator {
    /**
     * redis list message
     */
    public static String CommentLikeMessageCreate(String userId, String commentId, Integer isLike) {
        return userId + ":" + commentId + ":" + isLike;
    }

    public record CommentLikeMessageKey(String userId, String commentId) {
    }

    /**
     * 用于解析 commentLike message
     * @param messageList 信息列表
     * @return 结果 map
     */
    public static Map<CommentLikeMessageKey, Integer> CommentLikeMessageParse(List<String> messageList) {
        if (Objects.isNull(messageList) || messageList.isEmpty()) {
            return new HashMap<>(0);
        }

        HashMap<CommentLikeMessageKey, Integer> resultMap = new HashMap<>(500);
        messageList.forEach(message -> {
            String[] split = message.split(":");
            if (split.length != 3) {
                return;
            }
            String userId = split[0];
            String commentId = split[1];
            String isLike = split[2];
            resultMap.put(new CommentLikeMessageKey(userId, commentId), Integer.valueOf(isLike));
        });
        return resultMap;
    }
}

