package com.app.uni_app.pojo.emums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券使用状态枚举
 * 对应 use_status 字段：0未用 1已用 2已过期 3已退回
 */
@Getter
@AllArgsConstructor
public enum CouponUseStatusEnum {

    /**
     * 0 - 未开始
     */
    UN_BEGIN(0,"unBegin","未开始"),

    /**
     * 1 - 未使用
     */
    UNUSED(1, "unused", "未用"),

    /**
     * 2 - 已使用
     */
    USED(2, "used", "已用"),

    /**
     * 3 - 已过期
     */
    EXPIRED(3, "expired", "已过期"),

    /**
     * 4 - 已退回/已返还
     */
    RETURNED(4, "returned", "已退回");

    /**
     * 1. 数据库存储数字
     */
    private final Integer code;

    /**
     * 2. 英文小写（Redis key 使用）
     */
    private final String key;

    /**
     * 3. 中文描述
     */
    private final String desc;

    public static CouponUseStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CouponUseStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}