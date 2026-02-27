package com.app.uni_app.infrastructure.redis.constant;

/**
 * 规范:一级key: PREFIX_... = "...:"
 *     二级key: ... ="...:"
 *     ......
 *     变量key拼接在 RedisKeyGenerator
 */
public class RedisConstant {
    public static final String PREFIX_LOGIN = "login:";
    public static final String PREFIX_BANNER = "banner:";
    public static final String PREFIX_PRODUCT = "product:";
    public static final String PREFIX_CART = "cart:";
    public static final String PREFIX_CATEGORY= "category:";
    public static final String PREFIX_ORDER = "order:";
    public static final String USER = "user:";
    public static final String REFRESH = "refresh:";
    public static final String TOKEN = "token:";
    public static final String ID = "id:";
    public static final String TREE ="tree";
    public static final String FIRST_CATEGORY = "firstCategory:";
    public static final String DETAIL = "detail:";
    public static final String COLLECTION = "collection:";
    public static final String USER_ID_LIST = "userIdList";
    public static final String PRODUCT = "product:";
    public static final String PRODUCT_SPEC = "productSpec:";
    public static final String HOT = "hot";
    public static final String ID_LIST="idList";
    public static final String ALL = "all";

}

