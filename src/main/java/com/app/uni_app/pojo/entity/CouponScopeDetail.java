package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 优惠券适用商品/分类明细表
 */
@Data
@TableName("coupon_scope_detail")
public class CouponScopeDetail {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券 id
     */
    @TableField("coupon_id")
    private Long couponId;

    /**
     * 1商品 2分类
     */
    @TableField("scope_type")
    private Integer scopeType;

    /**
     * 商品ID/分类ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}