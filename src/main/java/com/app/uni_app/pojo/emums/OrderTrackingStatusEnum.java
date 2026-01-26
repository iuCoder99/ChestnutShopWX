package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流状态枚举
 */
@Getter
public enum OrderTrackingStatusEnum  {

    /**
     * 揽收
     */
    COLLECTED("collected", 1, "揽收"),
    /**
     * 运输
     */
    IN_TRANSIT("inTransit", 2, "运输"),
    /**
     * 派送
     */
    DELIVERING("delivering", 3, "派送"),
    /**
     * 签收
     */
    SIGNED("signed", 4, "签收");


    @JsonValue
    private final String value;

    /**
     * 数据库存储编码
     */
    @EnumValue
    private final int code;

    /**
     * 中文描述
     */
    private final String desc;

    OrderTrackingStatusEnum(String value, int code, String desc) {
        this.value = value;
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static OrderTrackingStatusEnum getByValue(String value) {
        for (OrderTrackingStatusEnum statusEnum : OrderTrackingStatusEnum.values()) {
            if (StringUtils.equals(statusEnum.value, value)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("无效的OrderTrackingStatusEnum.value:" + value);
    }

    public static OrderTrackingStatusEnum fromCode(int code) {
        for (OrderTrackingStatusEnum statusEnum : values()) {
            if (statusEnum.code == code) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("无效的OrderTrackingStatusEnum.code:" + code);
    }
}