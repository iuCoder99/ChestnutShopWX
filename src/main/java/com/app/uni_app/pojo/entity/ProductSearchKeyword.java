package com.app.uni_app.pojo.entity;

import com.app.uni_app.pojo.emums.CommonStatus;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品搜索关键词表 实体类
 * 对应表：product_search_keyword
 */
@Data
@TableName("product_search_keyword")
public class ProductSearchKeyword implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 搜索关键词，如：短袖T恤、无线耳机
     */
    @TableField("keyword")
    private String keyword;

    /**
     * 是否为热门推荐 0-否 1-是
     */
    @TableField("is_hot")
    private CommonStatus isHot;

    /**
     * 是否展示 0-隐藏 1-展示（违规词可隐藏）
     */
    @TableField("is_show")
    private CommonStatus isShow;

    /**
     * 首次搜索时间【仅创建时赋值，永不修改】
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间【创建+更新时自动赋值】
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
