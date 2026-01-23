package com.app.uni_app.common.generator;

import com.app.uni_app.common.constant.RedisConstant;

/**
 * Redis key 拼接器
 */
public class RedisKeyGenerator {
    /**
     * order:user: + userId
     * @param userId
     * @return
     */
    public static String orderUser(long userId) {
        return RedisConstant.PREFIX_ORDER + RedisConstant.USER + userId;
    }
}
