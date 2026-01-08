package com.app.uni_app.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartDTO {
    /**
     * 本地购物车列表
     */
    List<CartProductDTO> cartItems;
}
