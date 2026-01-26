package com.app.uni_app.pojo.vo;

import com.app.uni_app.common.constant.DatePatternConstants;
import com.app.uni_app.pojo.emums.OrderTrackingStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class OrderTrackingVO {

    @Schema(name = "物流状态")
    private OrderTrackingStatusEnum logisticsStatus;

    @Schema(name = "所在地点")
    private String location;

    @Schema(name = "描述")
    private String description;


    @DateTimeFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @JsonFormat(pattern = DatePatternConstants.DATE_TIME_FORM)
    @Schema(name = "创建时间(跟踪时间)")
    private LocalDateTime createTime;
}
