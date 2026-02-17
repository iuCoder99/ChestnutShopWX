package com.app.uni_app.infrastructure.redis.config;

import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.connect.StringRedisConnector;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * <p>
 * 核心目标：
 * 1. 解决存入对象时类型丢失（变成 LinkedHashMap）的问题。
 * 2. 解决 Java8 时间类型（LocalDateTime）序列化报错的问题。
 */
@Configuration
public class RedisConfig {

    /**
     * 通用 RedisTemplate（支持任意对象存取）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 1. 创建 ObjectMapper（JSON 处理核心）
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 关键配置：保留类型信息
        // 存入 Redis 时，JSON 会多出一个 "@class" 字段，记录类全名
        // 取出时，Jackson 自动根据 "@class" 转回原对象，而不是 LinkedHashMap
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        // 关键配置：支持 LocalDateTime 等时间类型
        objectMapper.registerModule(new JavaTimeModule());

        // 2. 创建序列化器
        // Key 使用 String 序列化（可读性好）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // Value 使用 JSON 序列化（支持对象）
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 3. 设置序列化规则
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        // 注入到工具类，方便静态调用
        RedisConnector.setRedisTemplate(template);
        return template;
    }

    /**
     * 字符串专用 RedisTemplate（性能稍高，仅存取 String）
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        
        // StringRedisTemplate 默认全用 String 序列化，无需额外配置
        
        StringRedisConnector.setStringRedisTemplate(template);
        return template;
    }
}