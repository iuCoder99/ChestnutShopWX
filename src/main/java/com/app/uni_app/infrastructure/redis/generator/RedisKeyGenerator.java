package com.app.uni_app.infrastructure.redis.generator;

import com.app.uni_app.infrastructure.redis.constant.RedisConstant;

/**
 * Redis key 拼接器
 */
public class RedisKeyGenerator {

    /**
     * login:user: + userId
     * @param userId
     * @return
     */
    public static String loginUser(long userId) {
        return RedisConstant.PREFIX_LOGIN + RedisConstant.USER + userId;
    }

    /**
     * order:user: + userId
     * @param userId
     * @return
     */
    public static String orderUser(long userId) {
        return RedisConstant.PREFIX_ORDER + RedisConstant.USER + userId;
    }

}
