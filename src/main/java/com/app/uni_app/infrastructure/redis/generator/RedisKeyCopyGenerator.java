package com.app.uni_app.infrastructure.redis.generator;


/**
 * redis 缓存副本 key 生成器
 */
public class RedisKeyCopyGenerator {

    public static final String PREFIX_COPY = "copy:";


    public static String copyKey(String key) {
        return PREFIX_COPY + key;
    }
}
