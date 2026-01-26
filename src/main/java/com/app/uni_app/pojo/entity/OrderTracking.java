package com.app.uni_app.pojo.entity;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.pojo.emums.OrderTrackingStatusEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 订单物流跟踪表 实体类
 * 对应表：order_tracking
 */
@Data
@TableName("order_tracking")
public class OrderTracking {

    /**
     * 物流跟踪 id(主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 关联订单 id
     */
    private String orderId;


    /**
     * 物流状态(1-揽收, 2-运输, 3-派送, 4-签收)
     */
    private OrderTrackingStatusEnum logisticsStatus;

    /**
     * 所在地点
     */
    private String location;

    /**
     * 描述信息
     */
    private String description;


    /**
     * 创建时间(跟踪时间)
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    private LocalDateTime createTime;


}