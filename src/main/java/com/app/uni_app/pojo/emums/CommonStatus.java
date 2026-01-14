package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CommonStatus {
    ACTIVE("active",1,"启用"),
    INACTIVE("inactive",0,"禁用");
    @JsonValue
    private final String value;
    @EnumValue
    private final Integer number;

    private final String desc;


    CommonStatus(String value, Integer number, String desc) {
        this.value = value;
        this.number = number;
        this.desc = desc;
    }

    /**
     * 根据传递 number返回 value
     * @param number
     * @return
     */
    public static String getValueByNumber(Integer number){
        for (CommonStatus commonStatus : values()) {
            if (commonStatus.number.equals(number)){
                return commonStatus.value;
            }
        }
        throw new IllegalArgumentException("无效的CommonStatus.number:" + number);
    }


    /**
     * static 根据 value 返回枚举
     *
     * @param value
     * @return
     */
    public static CommonStatus getByValue(String value) {
        for (CommonStatus commonStatus : values()) {
            if (commonStatus.value.equals(value)) {
                return commonStatus;
            }
        }
        throw new IllegalArgumentException("无效的CommonStatus.value:" + value);
    }


    /**
     * 前端传字符串,自动转换为枚举
     *
     * @param value
     * @return
     */
    @JsonCreator
    public static CommonStatus fromValue(String value) {
        return getByValue(value);
    }




}
