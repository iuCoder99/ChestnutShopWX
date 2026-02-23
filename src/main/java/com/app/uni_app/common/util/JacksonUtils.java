package com.app.uni_app.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类
 * 核心职责：
 * 1. Object 与 JSON String 互转
 * 2. Object 与 Map 互转
 * 3. 通用类型转换 (convert)
 *
 * 注意：
 * - 如果需要处理 Redis Hash 的存取，请使用 specialized RedisConnector
 * - 本类主要用于通用的 JSON 处理和类型转换
 */
@Slf4j
@Component
public class JacksonUtils {

    private static ObjectMapper objectMapper;

    @Autowired
    private ObjectMapper springObjectMapper;

    // 将 Spring 管理的 ObjectMapper 注入给静态变量，确保配置一致（如日期格式）
    @PostConstruct
    public void init() {
        JacksonUtils.objectMapper = springObjectMapper;
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败: {}", object, e);
            // 抛出运行时异常，调用者无需显示捕获
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("JSON反序列化失败: {}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * JSON 字符串转复杂对象 (如 List<User>)
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("JSON反序列化失败: {}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * JSON 字符串转 List
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("JSON转List失败: {}", json, e);
            throw new RuntimeException("JSON 转 List 失败", e);
        }
    }

    /**
     * 对象转 Map
     */
    public static Map<String, Object> toMap(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(object, new TypeReference<>() {
            });
        } catch (IllegalArgumentException e) {
            log.error("对象转Map失败: {}", object, e);
            throw new RuntimeException("对象转 Map 失败", e);
        }
    }

    /**
     * Map 转对象
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(map, clazz);
        } catch (IllegalArgumentException e) {
            log.error("Map转对象失败: {}", map, e);
            throw new RuntimeException("Map 转对象失败", e);
        }
    }

    /**
     * Map 转复杂对象
     */
    public static <T> T fromMap(Map<String, Object> map, TypeReference<T> typeReference) {
        if (map == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(map, typeReference);
        } catch (IllegalArgumentException e) {
            log.error("Map转对象失败: {}", map, e);
            throw new RuntimeException("Map 转对象失败", e);
        }
    }

    /**
     * 通用类型转换 (例如 LinkedHashMap 转 Bean)
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(source, targetClass);
        } catch (IllegalArgumentException e) {
            log.error("类型转换失败: source={}, target={}", source, targetClass.getName(), e);
            throw new RuntimeException("类型转换失败", e);
        }
    }

    /**
     * 通用类型转换 (例如 LinkedHashMap 转 List<Bean>)
     */
    public static <T> T convert(Object source, TypeReference<T> typeReference) {
        if (source == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(source, typeReference);
        } catch (IllegalArgumentException e) {
            log.error("类型转换失败: source={}, target={}", source, typeReference.getType(), e);
            throw new RuntimeException("类型转换失败", e);
        }
    }
}
