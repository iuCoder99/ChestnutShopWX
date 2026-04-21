package com.app.uni_app.infrastructure.redis.generator;

import com.app.uni_app.infrastructure.redis.constant.key.RedisConstant;
import com.app.uni_app.pojo.emums.CouponUseStatusEnum;

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
    public static String banner() {
        return RedisConstant.PREFIX_BANNER + RedisConstant.ALL;

    }

    /**
     * hotProduct RedisKey
     * product:hot
     * @return
     */
    public static String hotProductKey() {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.HOT;
    }

    /**
     * hotProduct HashKey
     * id: + hotProductId
     * @param hotProductId
     * @return
     */
    public static String hotProductHashKey(Long hotProductId) {
        return RedisConstant.ID + hotProductId;
    }

    /**
     * hotProductIdList
     * product: + hot: + idList
     * @return
     */
    public static String hotProductIdList() {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.HOT + ":" + RedisConstant.ID_LIST;
    }

    /**
     * productDetail
     * product: + detail: + productId
     * @param productId
     * @return
     */
    public static String productDetail(Long productId) {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.DETAIL + productId;
    }

    /**
     * productCollection
     * product: + collection: + productId
     * @param productId
     * @return
     */
    public static String productCollection(Long productId) {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.COLLECTION + productId;

    }


    /**
     * es最大商品id缓存
     * product: + maxProductId
     * @return value key
     */
    public static String maxProductId(){
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.MAX_PRODUCT_ID;
    }

    /**
     * cartKey 用户购物车
     * cart: + user: + userId
     * @param userId
     * @return
     */
    public static String cartKey(Long userId) {
        return RedisConstant.PREFIX_CART + RedisConstant.USER + userId;
    }

    /**
     * cartHashKey
     * product: + productId + "," + productSpec + specId
     * @return
     */
    public static String cartHashKey(String productId, String specId) {
        return RedisConstant.PRODUCT + productId + "," + RedisConstant.PRODUCT_SPEC + specId;

    }

    /**
     * categoryTreeKey
     * category: + tree
     * @return
     */
    public static String categoryTreeKey() {
        return RedisConstant.PREFIX_CATEGORY + RedisConstant.TREE;
    }

    /**
     * categoryTreeHashKey
     * firstCategory: + firstCategoryId;
     * @param firstCategoryId
     * @return
     */
    public static String categoryTreeHashKey(Long firstCategoryId) {
        return RedisConstant.FIRST_CATEGORY + firstCategoryId;
    }


    /**
     * orderKey
     * order: + detail: + orderNo: + orderNo
     * @param orderNo
     * @return
     */
    public static String orderKey(String orderNo) {
        return RedisConstant.PREFIX_ORDER + RedisConstant.DETAIL + RedisConstant.ORDER_NO + orderNo;
    }

    /**
     * firstCommentKey
     * product: + firstComment: + firstCommentId
     * @param firstCommentId
     * @return
     */
    public static String firstCommentKey(Long firstCommentId) {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.FIRST_COMMENT + firstCommentId;
    }

    /**
     * appendCommentKey
     * product: + appendComment: + firstComment: + firstCommentId
     * @param firstCommentId
     * @return
     */
    public static String appendCommentKey(Long firstCommentId) {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.APPEND_COMMENT + RedisConstant.FIRST_COMMENT + firstCommentId;
    }


    /**
     * secondCommentKey
     * product: + secondComment: + secondCommentId
     * @param secondCommentId
     * @return
     */
    public static String secondCommentKey(Long secondCommentId) {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.SECOND_COMMENT + secondCommentId;
    }

    /**
     * product: + productId + : +commentCount:
     * @param productId
     * @return
     */
    public static String productCommentCount(Long productId) {
        return RedisConstant.PREFIX_PRODUCT + productId + ":" + RedisConstant.COMMENT_COUNT;
    }

    /**
     * productCommentLikeList
     * product: + commentLikeMessageList
     */
    public static String productCommentLikeMessageList() {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.COMMENT_LIKE_MESSAGE_LIST;
    }

    /**
     * productCommentUserIdList
     * product: + comment: + commentId + : + user: +idList
     * @param commentId
     * @return
     */
    public static String productCommentUserIdList(Long commentId) {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.COMMENT + commentId + ":" + RedisConstant.USER + RedisConstant.ID_LIST;
    }

    /**
     * userProductCommentIdList
     * product: + user: + user: + idList
     */
    public static String userProductCommentIdList(Long userId) {
        return RedisConstant.PREFIX_PRODUCT + RedisConstant.COMMENT + RedisConstant.USER + userId + RedisConstant.ID_LIST;
    }


    /**
     * couponFixedTimeUnBeginZSet
     * coupon: + couponFixedTimeUnBegin
     */
    public static String couponFixedTimeUnBeginZSet() {
        return RedisConstant.PREFIX_COUPON + RedisConstant.COUPON_FIXED_TIME_UN_BEGIN;
    }


    /**
     * couponFixedTimeInProgressZSet
     * coupon: + couponFixedTimeInProgress
     */
    public static String couponFixedTimeInProgressZSet() {
        return RedisConstant.PREFIX_COUPON + RedisConstant.COUPON_FIXED_TIME_IN_PROGRESS;
    }


    /**
     * couponUseStatus
     * coupon: + couponId + : + couponUseStatus: + key + idList
     * @param couponId
     * @param couponUseStatusEnum
     * @return
     */
    public static String couponUseStatusIdSet(Long couponId, CouponUseStatusEnum couponUseStatusEnum) {
        return RedisConstant.PREFIX_COUPON + couponId + ":" + RedisConstant.USE_STATUS + couponUseStatusEnum.getKey() + RedisConstant.ID_LIST;
    }


    /**
     * couponDetail
     * coupon: + couponId + : + detail:
     * @param couponId
     * @return
     */
    public static String couponDetail(Long couponId) {
        return RedisConstant.PREFIX_COUPON + couponId + ":" + RedisConstant.DETAIL;
    }

    /**
     * couponAfterReceiveTimeUnBeginZSet
     * coupon:  + couponAfterReceiveTimeUnBegin
     * @return
     */
    public static String couponAfterReceiveTimeUnBeginZSet() {
        return RedisConstant.PREFIX_COUPON  + ":" + RedisConstant.AFTER_RECEIVE_TIME_UN_BEGIN;
    }


    /**
     * couponAfterReceiveTimeInProgressZSet
     * coupon: + couponAfterReceiveTimeInProgress
     * @return
     */
    public static String couponAfterReceiveTimeInProgressZSet() {
        return RedisConstant.PREFIX_COUPON + RedisConstant.AFTER_RECEIVE_TIME_IN_PROGRESS;
    }


}
