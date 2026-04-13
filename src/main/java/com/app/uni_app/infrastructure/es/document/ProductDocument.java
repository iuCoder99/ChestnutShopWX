package com.app.uni_app.infrastructure.es.document;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDocument {
    /**
     * 商品ID（主键）
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
}
