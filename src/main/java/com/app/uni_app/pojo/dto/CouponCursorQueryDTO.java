package com.app.uni_app.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema
@Data
public class CouponCursorQueryDTO {
    @NotNull(message = "优惠券种类不能为空")
    @Schema(description = "优惠券种类", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponType;

    @NotNull(message = "使用场景不能为空")
    @Schema(description = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    private String useScope;

    @NotNull(message = "使用状态不能为空")
    @Schema(description = "使用状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @NotNull(message = "开始查询 id不能为空")
    @Schema(description = "开始查询 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer beginId;

    @NotNull(message = "开始查询值不能为空")
    @Schema(description = "开始查询值(传上一次的时间)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String beginValue;

    @NotNull(message = "查询数量不能为空")
    @Schema(description = "查询数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer selectCount;
}
