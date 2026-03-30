package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CouponValidModeEnum {

    /**
     * 1 - 固定时间
     */
    FIXED_TIME(1, "固定时间"),

    /**
     * 2 - 领券后N天
     */
    AFTER_RECEIVE(2, "领券后 N 天");

    /**
     * 数据库存储的值
     */
    @EnumValue
    private final Integer code;

    /**
     * 描述
     */
    private final String desc;

}
