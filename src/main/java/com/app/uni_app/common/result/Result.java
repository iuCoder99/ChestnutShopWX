package com.app.uni_app.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一结果封装类
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Boolean success;
    private String message; //错误信息
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.success = true;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.success = true;
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result result = new Result();
        result.message = message;
        result.success = false;
        return result;
    }

}