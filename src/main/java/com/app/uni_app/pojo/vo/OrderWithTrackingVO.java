package com.app.uni_app.pojo.vo;

import lombok.Data;

import java.util.List;
@Data
public class OrderWithTrackingVO {
    private String orderId;
    private String logisticsCompany;
    private String logisticsNo;
    private List<OrderTrackingVO> orderTrackings;
}
