package com.app.uni_app.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 商品分类实体类
 * 对应数据库表：category
 * 支持一级/二级分类
 *
 * @author 自动生成
 * @date 2025-12-27
 */
@Data
@TableName("category")
@FieldNameConstants// 显式指定对应数据库表名
public class Category implements Serializable {

    private static final long serialVersionUID = 1L; // 序列化版本号，保证序列化兼容性

    /**
     * 分类ID（主键）
     */
    @TableId(type = IdType.AUTO) // 对应数据库自增主键
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID（0-一级分类）
     */
    private Long parentId = 0L; // 初始化默认值，与数据库默认值一致

    /**
     * 存储子级 category
     */
    @TableField(exist = false)
    private List<Category> children;

    /**
     * 分类图标URL（仅一级分类）
     */
    private String iconUrl = "/static/images/default-category.png"; // 初始化默认值，与数据库默认值一致

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort = 0; // 初始化默认值，与数据库默认值一致

    /**
     * 状态（0-禁用，1-启用）
     * 前端int,数据库int,此处省略enum
     */
    private Integer status = 1; // 使用Boolean对应数据库tinyint(1)，默认值1（启用)

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}