package com.app.uni_app.infrastructure.redis.connect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.data.redis.core.*;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * Redis 连接工具类
 */
public class RedisConnector {

    // 由配置类注入
    @Setter
    private static RedisTemplate<String, Object> redisTemplate;
    
    // 保留 ObjectMapper 引用，以便在需要时进行复杂类型转换
    private static ObjectMapper objectMapper;

    /**
     * 初始化 HashMapper (实际仅初始化 ObjectMapper)
     */
    public static void initHashMapper(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        RedisConnector.objectMapper = objectMapper;
    }
    
    // ===================== 对象 <-> Hash 转换操作 =====================

    /**
     * 将对象直接存为 Hash
     */
    public static void setHashObject(String key, Object object) {
        if (object == null || objectMapper == null) {
            return;
        }
        // 使用 ObjectMapper 转 Map，确保与 JSON 序列化行为一致
        Map<String, Object> map = objectMapper.convertValue(object, new TypeReference<>() {});
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 将 Hash 直接转为对象
     */
    public static <T> T getHashObject(String key, Class<T> clazz) {
        if (objectMapper == null) return null;
        Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);
        if (rawMap.isEmpty()) {
            return null;
        }
        // 直接使用 ObjectMapper 将 Map 转回对象
        return objectMapper.convertValue(rawMap, clazz);
    }

    /**
     * 获取 Hash 中的单个字段并自动转换类型
     */
    public static <T> T getHashField(String key, String field, Class<T> targetClass) {
        Object value = redisTemplate.opsForHash().get(key, field);
        if (value == null) return null;
        
        // 尝试直接转换
        try {
            return objectMapper.convertValue(value, targetClass);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 获取 Hash 中的单个字段并自动转换类型 (支持 TypeReference, 用于复杂泛型如 List<ProductSpec>)
     */
    public static <T> T getHashField(String key, String field, TypeReference<T> typeReference) {
        Object value = redisTemplate.opsForHash().get(key, field);
        if (value == null) return null;

        try {
            return objectMapper.convertValue(value, typeReference);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 通用的管道执行方法，允许自定义多条命令打包
     */
    public static List<Object> executePipelined(SessionCallback<?> sessionCallback) {
        return redisTemplate.executePipelined(sessionCallback);
    }

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
