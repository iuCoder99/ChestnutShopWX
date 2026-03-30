package com.app.uni_app.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

    @Data
    @Schema(description = "管理员创建优惠券活动请求参数")
    public class CouponCreateDTO {

        @NotBlank(message = "活动名称不能为空")
        @Schema(description = "优惠券活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "88折会员专属券")
        private String activityName;

        @NotNull(message = "优惠券类型不能为空")
        @Schema(description = "优惠券类型：1满减 2折扣 3无门槛 4单品券", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        private Integer couponType;

        @Schema(description = "满减/无门槛面额，折扣券传null", example = "null")
        private BigDecimal faceValue;

        @Schema(description = "折扣比例，例8.8=88折，非折扣券传null", example = "8.80")
        private BigDecimal discountRate;

        @Schema(description = "折扣最高优惠上限", example = "30.00")
        private BigDecimal maxDiscount;

        @NotNull(message = "最低消费门槛不能为空")
        @Schema(description = "使用最低消费门槛，0=无门槛", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.00")
        private BigDecimal minSpend;

        @NotNull(message = "总发行量不能为空")
        @Schema(description = "总发放库存，0不限量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
        private Integer totalQuota;

        @NotNull(message = "有效期模式不能为空")
        @Schema(description = "有效期模式：1固定时间 2领券后N天有效", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        private Integer validMode;

        @Schema(description = "固定有效期开始时间，模式2传null", example = "null")
        private LocalDateTime validStart;

        @Schema(description = "固定有效期结束时间，模式2传null", example = "null")
        private LocalDateTime validEnd;

        @Schema(description = "领券后有效天数，模式1传null", example = "15")
        private Integer receiveValidDays;

        @NotNull(message = "单人限领张数不能为空")
        @Schema(description = "单人限领张数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer limitPerPerson;

        @NotNull(message = "用户限制类型不能为空")
        @Schema(description = "用户限制：1全部 2新人 3会员 4指定人群", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer userLimitType;

        @NotNull(message = "使用范围不能为空")
        @Schema(description = "使用范围：1全场 2指定商品 3指定分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer useScope;

        @Schema(description = "互斥组ID，0无互斥", example = "0")
        private Long mutexGroupId;

        @NotNull(message = "活动状态不能为空")
        @Schema(description = "活动状态：0未开始 1发放中 2已结束 3作废", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer status;

        @NotNull(message = "发行时间不能为空")
        @Schema(description = "优惠券发行时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-27 12:00:00")
        private LocalDateTime releaseTime;
}