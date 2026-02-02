package com.app.uni_app.pojo.emums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum FeedbackStatusEnum {
    /**
     * 待处理
     */
    PENDING("pending", 0, "待处理"),
    /**
     * 已回复
     */
    REPLIED("replied", 1, "已回复"),
    /**
     * 已处理
     */
    HANDLED("handled", 2, "已处理");

    @JsonValue
    private final String value;
    @EnumValue
    private final Integer number;
    private final String desc;

    FeedbackStatusEnum(String value, Integer number, String desc) {
        this.value = value;
        this.number = number;
        this.desc = desc;
    }

    /**
     * static 根据 value 返回枚举
     *
     * @param value 字符串状态值（pending/replied/handled）
     * @return 对应的 FeedbackStatusEnum 枚举实例
     */
    public static FeedbackStatusEnum getByValue(String value) {
        for (FeedbackStatusEnum feedbackStatus : values()) {
            if (feedbackStatus.value.equals(value)) {
                return feedbackStatus;
            }
        }
        throw new IllegalArgumentException("无效的FeedbackStatusEnum.value:" + value);
    }

    /**
     * 前端传字符串,自动转换为枚举
     *
     * @param value 字符串状态值（pending/replied/handled）
     * @return 对应的 FeedbackStatusEnum 枚举实例
     */
    @JsonCreator
    public static FeedbackStatusEnum fromValue(String value) {
        return getByValue(value);
    }
}