package com.app.uni_app.pojo.emums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CommonDefault {

    DEFAULT(true, 1, "默认"),
    NO_DEFAULT(false, 0, "非默认");

    @JsonValue
    private final Boolean isDefault;

    @EnumValue
    private final Integer number;

    private final String desc;

    CommonDefault(Boolean isDefault, Integer number, String desc) {
        this.isDefault = isDefault;
        this.number = number;
        this.desc = desc;
    }


    public static CommonDefault getByIsDefault(Boolean isDefault) {
        if (isDefault) {
            return DEFAULT;
        } else {
            return NO_DEFAULT;
        }
    }


    @JsonCreator
    public static CommonDefault fromIsDefault(Boolean isDefault) {
        return getByIsDefault(isDefault);
    }


}
