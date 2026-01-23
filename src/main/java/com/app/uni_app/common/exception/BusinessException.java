package com.app.uni_app.common.exception;

import lombok.Getter;
/**
 * 业务异常父类
 */
@Getter
public abstract class BusinessException extends RuntimeException {
    private  int code;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

}
