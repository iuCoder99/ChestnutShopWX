package com.app.uni_app.infrastructure.rocketmq.consumer.order;

import com.app.uni_app.infrastructure.rocketmq.constant.order.MqOrderConstant;
import com.app.uni_app.pojo.emums.OrderStatusEnum;
import com.app.uni_app.service.OrderService;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;


/**
 * 接受更改订单状态的通知
 * map<订单号,新订单状态枚举>
 */
@Component
@RocketMQMessageListener(
        topic = MqOrderConstant.TOPIC_ORDER
        , selectorExpression = MqOrderConstant.TAG_ORDER_STATUS
        , consumerGroup = MqOrderConstant.CONSUMER_GROUP_ORDER_UPDATE_STATUS
)
public class OrderStatusConsumer implements RocketMQListener<Map<String, Object>> {
    @Resource
    private OrderService orderService;

    public static final String ORDER_ID = "orderId";
    public static final String ORDER_NO = "orderNo";
    public static final String ORDER_STATUS_ENUM = "orderStatusEnum";

    @Override
    public void onMessage(Map<String, Object> map) {
        Object orderId = map.get(ORDER_ID);
        Object orderNo = map.get(ORDER_NO);
        Long orderIdLong = Objects.isNull(orderId) ? null : Long.valueOf(orderId.toString());
        String orderNoStr= Objects.isNull(orderNo) ? null : orderNo.toString();
        OrderStatusEnum orderStatusEnum = OrderStatusEnum.getByValue(map.get(ORDER_STATUS_ENUM).toString());
        orderService.updateOrderStatus(orderIdLong, orderNoStr, orderStatusEnum);
    }
}
