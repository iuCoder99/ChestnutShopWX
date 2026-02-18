package com.app.uni_app.common.result;

import lombok.Getter;

/**
 * 业务状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    VALIDATE_FAILED(404, "参数检验失败"),
    LOGIN_ERROR(20001, "用户名或密码错误"),
    USER_NOT_EXIST(20002, "用户不存在"),
    USER_EXIST(20003, "用户已存在"),

    NO_TOKEN(10001,"无 token"),
    ACCESS_TOKEN_EXPIRED(10002,"认证 token 过期"), //自动调用刷新接口
    REFRESH_TOKEN_EXPIRED(10003,"刷新 token 过期"),
    AUTHENTICATION_SIGNATURE_ERROR(10004,"认证签名错误/篡改");


    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
