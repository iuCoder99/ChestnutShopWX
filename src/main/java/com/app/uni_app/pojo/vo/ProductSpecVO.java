package com.app.uni_app.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ProductSpecVO {
    /**
     * 商品规格价格
     */
    private BigDecimal price;
    /**
     * 商品 ID
     */
    private Long productId;
    /**
     * 库存数量
     */
    private Integer stock;
}
