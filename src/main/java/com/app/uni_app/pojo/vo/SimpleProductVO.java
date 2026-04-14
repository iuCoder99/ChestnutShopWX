package com.app.uni_app.pojo.vo;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
     * 商品封面图 URL
     */
    private String image;

    /**
     * 商品卖点/简介
     */
    private String sellPoint;

    /**
     * 基础价格（最低规格价格）
     */
    private BigDecimal price;

    /**
     * 状态（0-下架，1-上架）
     */
    private CommonStatus status;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 销量
     */
    private Long salesCount;

    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime createTime;

    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime updateTime;
}
