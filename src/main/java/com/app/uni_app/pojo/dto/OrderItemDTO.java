package com.app.uni_app.pojo.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
    private String productId;
    private String specId;
    private Integer quantity;
    private String price;
    private String productName;
    private String productImage;
    private String specText;

}
