package com.app.uni_app.pojo.vo;

import lombok.Data;

@Data
public class OrderItemVO {
    private String productId;
    private String specId;
    private Integer quantity;
    private String price;
    private String productName;
    private String productImage;
    private String specText;
}
