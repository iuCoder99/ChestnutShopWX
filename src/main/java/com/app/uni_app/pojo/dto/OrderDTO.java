package com.app.uni_app.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    private String addressId;
    private String remark;
    private String freight;
    private String totalAmount;
    private List<OrderItemDTO> orderItems;
}
