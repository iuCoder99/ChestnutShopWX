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
     * banner:all
     * @return
     */
    public static String banner(){
        return RedisConstant.PREFIX_BANNER +RedisConstant.ALL;

    }

    /**
     * hotProduct RedisKey
     * product:hot
     * @return
     */
    public static String hotProductKey(){
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.HOT ;
    }

    /**
     * hotProduct HashKey
     * id: + hotProductId
     * @param hotProductId
     * @return
     */
    public static String hotProductHashKey(Long hotProductId){
        return RedisConstant.ID + hotProductId;
    }

    /**
     * hotProductIdList
     * product: + hot: + idList
     * @return
     */
    public static String hotProductIdList(){
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.HOT + ":" + RedisConstant.ID_LIST;
    }

    /**
     * productDetail
     * product: + detail: + productId
     * @param productId
     * @return
     */
    public static String productDetail(Long productId){
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.DETAIL + productId;
    }

    public static String productCollection(Long productId){
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.COLLECTION + productId;

    }


}
