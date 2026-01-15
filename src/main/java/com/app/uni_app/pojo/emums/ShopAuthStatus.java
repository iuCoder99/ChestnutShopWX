package com.app.uni_app.pojo.emums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ShopAuthStatus {
    /**
     * 未认证
     */
    UN_AUTH(0, "unauth"),
    /**
     * 认证成功
     */
    AUTH_SUCCESS(1,"authsuccess"),
    /**
     * 认证中
     */
    AU_thing(2,"authing"),
    /**
     * 认证失败
     */
    AUTH_FAILED(3,"authfailed");

    private final Integer node;
    @JsonValue
    private final String desc;


    ShopAuthStatus(Integer node, String desc) {
        this.node = node;
        this.desc = desc;
    }

    /**
     * 核心方法：根据数据库的 Integer 值，查找对应的枚举（用于查库后转换）
     */
    public static ShopAuthStatus getDesc(Integer code) {
        // 防止 code 为 null（比如数据库字段没值），返回默认的未认证
        if (code == null) {
            return UN_AUTH;
        }
        for (ShopAuthStatus status : values()) {
            if (status.node.equals(code)) {
                return status;
            }
        }
        // 传入非法值时，也返回未认证（或抛出异常，根据业务调整）
        return UN_AUTH;
    }
}
