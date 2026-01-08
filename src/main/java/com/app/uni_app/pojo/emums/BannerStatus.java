package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum BannerStatus {
    /**
     * 启用
     */
    ACTIVE("active", 1,"启用"),
    /**
     * 禁用
     */
    INACTIVE("inactive",0,"禁用");
    @JsonValue
    private final String value;
    @EnumValue
    private final Integer number;
    private final String desc;


    BannerStatus(String value, Integer number,String desc) {
        this.value = value;
        this.number = number;
        this.desc = desc;
    }

    /**
     * static 根据 value 返回枚举
     *
     * @param value
     * @return
     */
    public static BannerStatus getByValue(String value) {
        for (BannerStatus bannerStatus : values()) {
            if (bannerStatus.value.equals(value)) {
                return bannerStatus;
            }
        }
        throw new IllegalArgumentException("无效的BannerStatus.value:" + value);
    }


    /**
     * 前端传字符串,自动转换为枚举
     *
     * @param value
     * @return
     */
    @JsonCreator
    public static BannerStatus fromValue(String value) {
        return getByValue(value);
    }


}
