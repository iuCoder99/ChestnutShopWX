package com.app.uni_app.common.exception;

import com.app.uni_app.common.constant.MessageConstant;
/**
 * 用户未登录异常
 */
public class UnauthenticatedException extends BusinessException {
    private static final int code = 401;


    public UnauthenticatedException() {
        super(code, MessageConstant.USER_NOT_LOGIN);

    }

    public UnauthenticatedException(String message) {
        super(code, message);
    }
}
