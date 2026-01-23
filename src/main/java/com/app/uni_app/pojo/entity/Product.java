package com.app.uni_app.pojo.entity;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商品表实体类
 * 对应数据库表：product
 *
 * @author 自动生成
 * @date 2025-12-28
 */
@Data
@TableName(value = "product")
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID（主键）
     */
    @TableId(type = IdType.AUTO) // 对应数据库自增主键
    private Long id;

    /**
     * 关联分类 ID
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 商品名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 商品卖点/简介
     */
    @TableField(value = "sell_point")
    private String sellPoint;

    /**
     * 基础价格（最低规格价格）
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 企业批量价格（有值则表示启用企业价）
     */
    @TableField(value = "enterprise_price")
    private BigDecimal enterprisePrice;

    /**
     * 总库存数量（所有规格库存之和）
     */
    @TableField(value = "stock")
    private Long stock;

    /**
     * 商品封面图 URL
     */
    @TableField(value = "cover_image")
    private String image;

    /**
     * 商品详情（富文本）
     */
    @TableField(value = "description")
    private String description;

    /**
     * 浏览量
     */
    @TableField(value = "view_count")
    private Long viewCount;

    /**
     * 销量
     */
    @TableField(value = "sales_count")
    private Long salesCount;

    /**
     * 状态（0-下架，1-上架）
     */
    @TableField(value = "status")
    private CommonStatus status;

    /**
     * 是否收藏,0-否,1-是
     */
    @TableField(exist = false)
    private Long isCollection;

    /**
     * 商品图片 URL 列表
     */
    @TableField(exist = false)
    private List<String> imageUrls;

    /**
     *商品规格列表
     */
    @TableField(exist = false)
    private List<ProductSpec> specList;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @TableField(value = "create_time", fill = FieldFill.INSERT) // 插入时自动填充
    private Date createTime;

    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE) // 插入/更新时自动填充
    private Date updateTime;

}