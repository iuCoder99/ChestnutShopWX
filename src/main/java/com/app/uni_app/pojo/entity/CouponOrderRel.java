package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单-优惠券核销关联表
 */
@Data
@TableName("coupon_order_rel")
public class CouponOrderRel {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单 ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 订单项 ID（单品核销用）
     */
    @TableField("order_item_id")
    private Long orderItemId;

    /**
     * 用户券 ID
     */
    @TableField("user_coupon_id")
    private Long userCouponId;

    /**
     * 活动 ID
     */
    @TableField("activity_id")
    private Long activityId;

    /**
     * 本次抵扣金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 1正常核销 2退款回滚 3作废
     */
    @TableField("rel_status")
    private Integer relStatus;

    /**
     * 核销时间
     */
    @TableField("use_time")
    private LocalDateTime useTime;

    /**
     * 退款回滚时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}