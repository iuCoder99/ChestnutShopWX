package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户类型枚举（个人/店铺）
 */
@Getter
public enum UserType {
    /**
     * 个人用户
     */
    PERSONAL("PERSONAL", "个人用户"),
    /**
     * 企业用户
     */
    ENTERPRISE("SHOP", "店铺");

    /**
     * 数据库存储值（和数据库enum值一致）
     */
    @EnumValue
    private final String dbValue;

    /**
     * 前端展示值（JSON序列化时返回）
     */
    @JsonValue
    private final String desc;

    UserType(String dbValue, String desc) {
        this.dbValue = dbValue;
        this.desc = desc;
    }

    public static UserType getByDesc(String desc) {
        for (UserType userType : UserType.values()) {
            if (userType.desc.equals(desc)) {
                return userType;
            }
        }
        throw new IllegalArgumentException("无效的UserType.value:" + desc);
    }

    @JsonCreator
    public static UserType formDesc(String desc) {
        return getByDesc(desc);
    }
}