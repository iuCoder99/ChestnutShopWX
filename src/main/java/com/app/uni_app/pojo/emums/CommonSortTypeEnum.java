package com.app.uni_app.pojo.emums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 排序方向枚举
 * 用于标记正序/倒序
 */
@Getter
@AllArgsConstructor
public enum CommonSortTypeEnum {

    /**
     * 正序
     */
    ASC(1, "正序"),

    /**
     * 倒序
     */
    DESC(2, "倒序");

    /**
     * 编码值
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String desc;


    public boolean isAsc() {
        return this.code == 1;
    }


}