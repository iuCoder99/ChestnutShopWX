package com.app.uni_app.infrastructure.es.document;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品索引
 */
@Data
@FieldNameConstants
public class ProductDocument {

    @NotNull(message = "商品 ID 不能为空")
    private Long id;

    @NotNull(message = "分类 ID 不能为空")
    private Long categoryId;

    @NotNull(message = "商品名称不能为空")
    private String name;

    @NotNull(message = "商品封面图 URL 不能为空")
    private String image;

    @NotNull(message = "商品卖点不能为空")
    private String sellPoint;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @NotNull(message = "商品状态不能为空")
    private Integer status;

    /**
     * 浏览量
     */
    private Long viewCount = 0L;

    /**
     * 销量
     */
    private Long salesCount = 0L;

    /**
     * 商品创建时间
     */
    @NotNull(message = "商品创建时间不能为空")
    private LocalDateTime createTime;

    /**
     * 商品更新时间
     */
    private LocalDateTime updateTime;

}
