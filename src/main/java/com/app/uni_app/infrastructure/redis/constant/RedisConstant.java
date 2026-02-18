package com.app.uni_app.infrastructure.redis.constant;

/**
 * 规范:一级key: PREFIX_... = "...:"
 *     二级key: ... ="...:"
 *     ......
 *     变量key拼接在 RedisKeyGenerator
 */
public class RedisConstant {
    public static final String PREFIX_LOGIN = "login:";
    public static final String PREFIX_ORDER = "order:";
    public static final String USER = "user:";
    public static final String REFRESH = "refresh:";
    public static final String TOKEN = "token:";


}

