package com.app.uni_app.infrastructure.redis.generator;

import com.app.uni_app.infrastructure.redis.constant.RedisConstant;

/**
 * Redis key 拼接器
 */
public class RedisKeyGenerator {

    /**
     * 用户登录信息
     * login:user: + userId
     * @param userId
     * @return
     */
    public static String loginUser(long userId) {
        return RedisConstant.PREFIX_LOGIN + RedisConstant.USER + userId;
    }


    /**
     *刷新 Token
     * login:refresh:token + UUID
     * @return
     */
    public static String loginRefreshToken(String UUID) {
        return RedisConstant.PREFIX_LOGIN + RedisConstant.REFRESH + RedisConstant.TOKEN + UUID;
    }


    /**
     * banner 轮播图
     * @return
     */
    public static String banner(){
        return RedisConstant.PREFIX_BANNER +RedisConstant.ALL;

    }

}
