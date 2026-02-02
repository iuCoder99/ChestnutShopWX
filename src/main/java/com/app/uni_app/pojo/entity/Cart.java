package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 购物车表实体类
 * 对应数据库表：cart
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "cart")
public class Cart {

    /**
     * 购物车ID（主键）
     * 对应数据库字段：id
     * 自增策略匹配建表语句的 auto_increment
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID（外键）
     * 对应数据库字段：user_id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 关联商品ID（外键）
     * 对应数据库字段：product_id
     */
    @TableField(value = "product_id")
    private Long productId;

    /**
     * 关联规格ID（外键，可空）
     * 对应数据库字段：spec_id
     */
    @TableField(value = "spec_id")
    private Long specId;

    /**
     * 购买数量
     * 对应数据库字段：quantity
     * 默认值：1（与建表语句一致）
     */
    @TableField(value = "quantity")
    private Integer quantity = 1;

    /**
     * 是否选中（0-否，1-是）
     * 对应数据库字段：checked
     * 默认值：0（与建表语句一致）
     * 数据库类型 tinyint(1)，对应Java布尔类型或Integer均可，此处用Integer更兼容
     */
    @TableField(value = "checked")
    private Integer checked = 0;

    /**
     * 创建时间
     * 对应数据库字段：create_time
     * 自动填充策略：插入时自动填充（匹配 CURRENT_TIMESTAMP）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 对应数据库字段：update_time
     * 自动填充策略：插入和更新时自动填充（匹配 CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


