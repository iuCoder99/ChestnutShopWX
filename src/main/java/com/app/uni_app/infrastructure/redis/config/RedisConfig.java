package com.app.uni_app.infrastructure.redis.config;

import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.connect.StringRedisConnector;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

/**
 * Redis 配置类
 * <p>
 * 企业级最佳实践：
 * 1. 使用 GenericJackson2JsonRedisSerializer 作为 Value 序列化器。
 * 2. 启用 activateDefaultTyping 并配合 BasicPolymorphicTypeValidator (白名单) 防止反序列化漏洞。
 * 3. 使用 Mixin 优化集合类型序列化，避免 Wrapper Array 结构，减少 JSON 体积并提高可读性。
 * 4. 统一 ObjectMapper 配置（日期格式、时区、容错性）。
 */
@Configuration
public class RedisConfig {

    /**
     * 定义一个 Mixin，强制禁用类型信息。
     * 用于 Collection 和 Map，避免序列化为 ["java.util.ArrayList", [...]] 这种 Wrapper Array。
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    abstract class NoTypeInfoMixin {}

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 1. 构建企业级 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 1.1 基础配置：容错性与格式化
        // 忽略未知的 JSON 字段，防止新增字段导致旧代码反序列化失败
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许空字符串转为 null 对象
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        // 1.2 日期时间配置
        objectMapper.registerModule(new JavaTimeModule()); // 支持 Java 8 时间 (LocalDateTime)
        // 禁用将日期序列化为时间戳，改为 ISO-8601 字符串 (或者自定义格式)
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 统一日期格式 yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        // 1.3 安全与多态配置 (核心)
        // 构建白名单校验器，防止 RCE 漏洞，仅允许必要的类进行多态反序列化
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class) // 允许基础类型
                .allowIfBaseType("java.util")  // 允许常见集合
                .allowIfBaseType("java.time")  // 允许时间类型
                .allowIfBaseType("com.app")    // 关键：允许本项目包下的类
                .build();

        // 激活 DefaultTyping，记录类型信息 (@class)
        // 使用 NON_FINAL 策略，适用于绝大多数场景
        objectMapper.activateDefaultTyping(
                ptv,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 【关键修复】创建副本给 RedisConnector 使用，并禁用 DefaultTyping
        // 这样 RedisConnector.convertValue(pojo, Map.class) 才能真正把 POJO 转成 Map，
        // 而不是尝试反序列化回 POJO 本身导致 "Not a subtype of Map" 错误。
        ObjectMapper connectorMapper = objectMapper.copy();
        connectorMapper.deactivateDefaultTyping();
        RedisConnector.initHashMapper(connectorMapper);

        // 1.4 集合类型优化 (Mixin) - 已移除
        // 为了遵循最标准的 Jackson 行为，不再强制禁用集合的类型信息。
        // 虽然这意味着 List 可能会被序列化为 ["java.util.ArrayList", [...]]，
        // 但这是开启 DefaultTyping 后的标准行为，保证了反序列化时的绝对类型安全。
        // objectMapper.addMixIn(Collection.class, NoTypeInfoMixin.class);
        // objectMapper.addMixIn(Map.class, NoTypeInfoMixin.class);

        // 2. 创建序列化器
        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // Value 使用配置好的 JSON 序列化器
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

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        StringRedisConnector.setStringRedisTemplate(template);
        return template;
    }
}
