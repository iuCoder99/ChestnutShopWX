package com.app.uni_app.pojo.vo;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.pojo.emums.OrderStatusEnum;
import com.app.uni_app.pojo.emums.PayTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderWithItemVO {
    private String orderNo;
    private String userId;
    private String addressId;
    private String totalGoodsAmount;
    private String totalAmount;
    private String freight;
    private String remark;
    private OrderStatusEnum status;
    private PayTypeEnum payType;
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime payTime;
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime createTime;
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime updateTime;
    private List<OrderItemVO> orderItems;
    private OrderAddressVO address;
}