package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 优惠券互斥配置组
 */
@Data
@TableName("coupon_mutex_group")
public class CouponMutexGroup {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 互斥组名称
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 唯一编码
     */
    @TableField(value = "group_code", fill = FieldFill.INSERT)
    private Long groupCode;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}