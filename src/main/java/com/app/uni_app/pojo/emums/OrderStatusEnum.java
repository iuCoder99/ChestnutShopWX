package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatusEnum {
    /**
     * 待支付
     */
    PENDING_PAYMENT("pendingPayment", 1, "待支付"),
    /**
     * 待确认
     */
    PENDING_CONFIRM("pendingConfirm", 2, "待确认"),
    /**
     * 待发货
     */
    PENDING_SHIPMENT("pendingShipment", 3, "待发货"),
    /**
     * 待收货
     */
    PENDING_RECEIPT("pendingReceipt", 4, "待收货"),
    /**
     * 已完成
     */
    COMPLETED("completed", 5, "已完成"),
    /**
     * 已取消
     */
    CANCELLED("cancelled", 6, "已取消"),
    /**
     * 售后
     */
    AFTER_SALE("afterSale", 7, "售后");

    @JsonValue
    private final String value;

    /**
     * 数据库存储编码（对应枚举名）
     */
    @EnumValue
    private final int code;

    /**
     * 中文描述
     */
    private final String desc;


    OrderStatusEnum(String value, int code, String desc) {
        this.value = value;
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static OrderStatusEnum getByValue(String value) {
        for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
            if (StringUtils.equals(orderStatusEnum.value, value)) {
                return orderStatusEnum;
            }
        }
        throw new IllegalArgumentException("无效的OrderStatusEnum.value:" + value);
    }


}