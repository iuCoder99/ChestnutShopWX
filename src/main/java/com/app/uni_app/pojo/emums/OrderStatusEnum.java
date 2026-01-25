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
    PENDING_PAYMENT("pendingPayment", 1, "待支付",2),
    /**
     * 待确认
     */
    PENDING_CONFIRM("pendingConfirm", 2, "待确认",-1),
    /**
     * 待发货
     */
    PENDING_SHIPMENT("pendingShipment", 3, "待发货",3),
    /**
     * 待收货
     */
    PENDING_RECEIPT("pendingReceipt", 4, "待收货",4),
    /**
     * 已完成
     */
    COMPLETED("completed", 5, "已完成",5),
    /**
     * 已取消
     */
    CANCELLED("cancelled", 6, "已取消",-1),
    /**
     * 售后
     */
    AFTER_SALE("afterSale", 7, "售后",5);

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

    /**
     * 商品归属页面,与 OrderPageEnum 建立联系
     */
    private final int pageCode;


    OrderStatusEnum(String value, int code, String desc, int pageCode) {
        this.value = value;
        this.code = code;
        this.desc = desc;
        this.pageCode = pageCode;
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