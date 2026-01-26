package com.app.uni_app.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
@Data
public class OrderWithTrackingVO {

    @Schema(name = "订单 ID")
    private String orderId;

    @Schema(name = "物流公司")
    private String logisticsCompany;

    @Schema(name = "物流单号")
    private String logisticsNo;

    @Schema(name = "物流跟踪")
    private List<OrderTrackingVO> orderTrackings;
}
