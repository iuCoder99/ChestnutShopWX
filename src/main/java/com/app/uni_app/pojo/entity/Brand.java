package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 品牌表实体类
 * 对应数据库表：brand
 *
 * @author 自动生成
 * @date 2025-12-28
 */
@Data
@TableName(value = "brand")
public class Brand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 品牌ID（主键）
     */
    @TableId(type = IdType.AUTO) // 标记主键，对应数据库自增策略
    private Long id;

    /**
     * 品牌名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 品牌Logo URL
     */
    @TableField(value = "logo_url")
    private String logoUrl;

    /**
     * 品牌描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField(value = "status")
    private Boolean status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入/更新时自动填充
    private Date updateTime;
}