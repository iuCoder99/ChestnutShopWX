package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户持有优惠券表
 */
@Data
@TableName("coupon_user")
public class CouponUser {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 优惠券模板 ID
     */
    @TableField("coupon_id")
    private Long couponId;

    /**
     * 个人生效时间
     */
    @TableField("valid_start")
    private LocalDateTime validStart;

    /**
     * 个人过期时间
     */
    @TableField("valid_end")
    private LocalDateTime validEnd;

    /**
     * 0未开始 1未用 2已用 3已过期 4已退回
     */
    @TableField("use_status")
    private Integer useStatus;

    /**
     * 领券时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}