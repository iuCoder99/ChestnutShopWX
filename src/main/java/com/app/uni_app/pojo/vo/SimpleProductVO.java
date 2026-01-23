package com.app.uni_app.pojo.vo;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SimpleProductVO  {

    /**
     * 商品 id
     */
    private Long id;

    /**
     * 关联分类 ID
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品卖点/简介
     */
    private String sellPoint;

    /**
     * 基础价格（最低规格价格）
     */
    private BigDecimal price;

    /**
     * 企业批量价格（有值则表示启用企业价）
     */
    private BigDecimal enterprisePrice;

    /**
     * 总库存数量（所有规格库存之和）
     */
    private Long stock;

    /**
     * 商品封面图 URL
     */
    private String image;

    /**
     * 商品详情（富文本）
     */
    private String description;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 销量
     */
    private Long salesCount;

    /**
     * 状态（0-下架，1-上架）
     */
    private CommonStatus status;



    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private Date createTime;

    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private Date updateTime;
}
