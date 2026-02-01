package com.app.uni_app.pojo.dto;


import lombok.Data;

@Data
public class CartProductDTO {
    /**
     * 商品 id
     */
    private String productId;
    /**
     * 规格 id
     */
    private String specId;
    /**
     * 数量
     */
    private Integer quantity;


}
