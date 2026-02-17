package com.app.uni_app.infrastructure.redis.connect;

import lombok.Setter;
import org.springframework.data.redis.core.*;

public class RedisConnector {

    // 由配置类注入
    @Setter
    private static RedisTemplate<String, Object> redisTemplate;

    // ===================== 静态获取各种操作 =====================

    public static ValueOperations<String, Object> opsForValue() {
        return redisTemplate.opsForValue();
    }

    public static HashOperations<String, String, Object> opsForHash() {
        return redisTemplate.opsForHash();
    }

    public static ListOperations<String, Object> opsForList() {
        return redisTemplate.opsForList();
    }

    public static SetOperations<String, Object> opsForSet() {
        return redisTemplate.opsForSet();
    }

    public static ZSetOperations<String, Object> opsForZSet() {
        return redisTemplate.opsForZSet();
    }

    // ===================== 通用方法 =====================

    public static Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public static Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public static Boolean expire(String key, long timeout, java.util.concurrent.TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
}
