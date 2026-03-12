package com.app.uni_app.common.exception;

/**
 * 自定义业务异常,解决redis空对象处理
 */
public class EmptyObjectException extends RuntimeException {
    public EmptyObjectException(String message) {
        super(message);
    }
}
