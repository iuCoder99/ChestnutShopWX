package com.app.uni_app.infrastructure.rocketmq.constant.order;

public class MqOrderConstant {

    //=========Topic =========
    public static final String TOPIC_ORDER = "order-topic";


    //=========Tags================
    public static final String TAG_ORDER_STATUS = "orderStatus";
    /**
     * 已评价
     */
    public static final String TAG_ORDER_EVALUATED = "orderEvaluated";
    /**
     * 已追评
     */
    public static final String TAG_ORDER_REVIEWED = "orderReviewed";


    //==========Consumer  Group=================
    public static final String CONSUMER_GROUP_ORDER_UPDATE_STATUS = "order-status-consumer-group";
}
