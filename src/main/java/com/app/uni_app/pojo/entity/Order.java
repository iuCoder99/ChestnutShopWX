package com.app.uni_app.pojo.entity;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.emums.OrderStatusEnum;
import com.app.uni_app.pojo.emums.PayTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单表实体（MyBatis-Plus）
 */
@Data
@TableName("`order`")
@Accessors(chain = true)
@FieldNameConstants
public class Order {

    /**
     * 订单ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号（唯一，格式：年月日+随机数）
     */
    @TableField
    private String orderNo;

    /**
     * 关联用户ID（外键）
     */
    private Long userId;

    /**
     * 关联地址ID（外键）
     */
    private Long addressId;

    /**
     * 商品总价
     */
    private BigDecimal totalGoodsAmount;

    /**
     * 运费（默认0.00）
     */
    private BigDecimal freight = BigDecimal.ZERO;

    /**
     * 实付款金额（商品总价+运费）
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态（默认待支付）
     */
    private OrderStatusEnum status = OrderStatusEnum.PENDING_PAYMENT;

    /**
     * 支付方式（微信支付/支付宝，可为空）
     */
    private PayTypeEnum payType;

    /**
     * 支付时间（可为空）
     */
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime payTime;

    /**
     * 发货时间（可为空）
     */
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime deliverTime;

    /**
     * 确认收货时间（可为空）
     */
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime receiveTime;

    /**
     * 取消时间（可为空）
     */
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime cancelTime;

    /**
     * 订单取消原因（仅已取消状态有效，可为空）
     */
    private String cancelReason;

    /**
     * 订单备注（用户填写，可为空）
     */
    private String remark;

    /**
     * 是否评价
     */
    private CommonStatus is_evaluate;

    /**
     * 是否删除
     */
    private CommonStatus is_deleted;

    /**
     * 快递公司（可为空）
     */
    private String logisticsCompany;

    /**
     * 物流单号（可为空）
     */
    private String logisticsNo;

    /**
     * 创建时间（默认当前时间）
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime createTime;

    /**
     * 更新时间（默认当前时间，更新时自动刷新）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime updateTime;


    @TableField(exist = false)
    private List<OrderItem> orderItems;

    @TableField(exist = false)
    private List<OrderTracking> orderTrackings;

    public BigDecimal getTotalGoodsAmount() {
        return this.totalAmount.subtract(this.freight);
    }
}