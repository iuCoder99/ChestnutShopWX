package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券模板表
 */
@Data
@TableName("coupon")
public class Coupon {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券模板编码
     */
    @TableField("coupon_no")
    private String couponNo;

    /**
     * 活动名称
     */
    @TableField("activity_name")
    private String activityName;

    /**
     * 1满减 2折扣 3无门槛 4单品券
     */
    @TableField("coupon_type")
    private Integer couponType;

    /**
     * 满减/无门槛金额
     */
    @TableField("face_value")
    private BigDecimal faceValue;

    /**
     * 折扣率 8.8=88折
     */
    @TableField("discount_rate")
    private BigDecimal discountRate;

    /**
     * 折扣上限
     */
    @TableField("max_discount")
    private BigDecimal maxDiscount;

    /**
     * 使用门槛
     */
    @TableField("min_spend")
    private BigDecimal minSpend;

    /**
     * 总发行量 0不限
     */
    @TableField("total_quota")
    private Integer totalQuota;

    /**
     * 已核销数
     */
    @TableField("used_quota")
    private Integer usedQuota;

    /**
     * 已领取数
     */
    @TableField("receive_quota")
    private Integer receiveQuota;

    /**
     * 1固定时间 2领券后N天
     */
    @TableField("valid_mode")
    private Integer validMode;

    /**
     * 固定有效期开始
     */
    @TableField("valid_start")
    private LocalDateTime validStart;

    /**
     * 固定有效期结束
     */
    @TableField("valid_end")
    private LocalDateTime validEnd;

    /**
     * 领券后有效天数
     */
    @TableField("receive_valid_days")
    private Integer receiveValidDays;

    /**
     * 单人限领
     */
    @TableField("limit_per_person")
    private Integer limitPerPerson;

    /**
     * 1全部 2新人 3会员 4指定人群
     */
    @TableField("user_limit_type")
    private Integer userLimitType;

    /**
     * 1全场 2指定商品 3指定分类
     */
    @TableField("use_scope")
    private Integer useScope;

    /**
     * 互斥组唯一编码
     */
    @TableField("mutex_group_code")
    private Long mutexGroupCode;

    /**
     * 0未开始 1发放中 2已结束 3作废
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否隐藏
     */
    @TableField("is_elimination")
    private Integer isElimination;

    /**
     * 发行时间
     */
    @TableField("release_time")
    private LocalDateTime releaseTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    /**
     * 优惠券互斥配置组
     */
    @TableField(exist = false)
    List<CouponMutexGroup> couponMutexGroupList;


    /**
     * 优惠券适用范围
     */
    @TableField(exist = false)
    List<CouponScopeDetail> couponScopeDetailList;


    /**
     * 用户优惠券使用情况列表
     */
    @TableField(exist = false)
    List<CouponUser> couponUserList;

}