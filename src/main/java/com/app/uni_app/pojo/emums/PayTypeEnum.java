package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 支付方式枚举
 */
@Getter
public enum PayTypeEnum {
    /**
     * 未支付
     */
    UNPAID("unpaid",0,"未支付"),
    /**
     * 微信支付
     */
    WECHAT_PAY("wechatPay", 1, "微信支付"),
    /**
     * 支付宝
     */
    ALI_PAY("aliPay", 2, "支付宝支付");


    @JsonValue
    private final String value;
    /**
     * 数据库存储编码（对应枚举名）
     */
    @EnumValue
    private final int code;

    /**
     * 中文描述
     */
    private final String desc;

    PayTypeEnum(String value, int code, String desc) {
        this.value = value;
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public PayTypeEnum getByValue(String value) {
        for (PayTypeEnum payTypeEnum : PayTypeEnum.values()) {
            if (StringUtils.equals(value, payTypeEnum.value)) {
                return payTypeEnum;
            }
        }
        throw new IllegalArgumentException("无效的PayTypeEnum.value:" + value);

    }
}
