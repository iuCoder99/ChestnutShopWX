package com.app.uni_app.infrastructure.redis.connect;


import lombok.Setter;
import org.springframework.data.redis.core.*;

public class StringRedisConnector {

    // 由配置类注入 StringRedisTemplate（处理纯字符串）
    @Setter
    private static StringRedisTemplate stringRedisTemplate;

    // ===================== 纯字符串场景 - 各种数据结构操作 =====================
    public static ValueOperations<String, String> opsForValue() {
        return stringRedisTemplate.opsForValue();
    }

    public static HashOperations<String, String, String> opsForHash() {
        return stringRedisTemplate.opsForHash();
    }

    public static ListOperations<String, String> opsForList() {
        return stringRedisTemplate.opsForList();
    }

    public static SetOperations<String, String> opsForSet() {
        return stringRedisTemplate.opsForSet();
    }

    public static ZSetOperations<String, String> opsForZSet() {
        return stringRedisTemplate.opsForZSet();
    }

    // ===================== 通用方法 =====================
    public static Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    public static Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public static Boolean expire(String key, long timeout, java.util.concurrent.TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }
}
