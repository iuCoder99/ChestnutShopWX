package com.app.uni_app.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 统一结果封装类
 * @param <T>
 */
@Data
@Schema(description = "统一响应结果封装")
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "请求是否成功（true=成功，false=失败）", example = "true")
    private Boolean success;

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "错误信息（失败时返回，成功时为null）", example = "操作成功")
    private String message;

    @Schema(description = "响应数据（成功时返回具体数据，失败时为null）")//错误信息
    private T data; //数据


    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.success = true;
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.success = true;
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result result = new Result();
        result.message = message;
        result.code = ResultCode.ERROR.getCode();
        result.success = false;
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        Result result = new Result();
        result.success = false;
        result.code = resultCode.getCode();
        result.message = resultCode.getMessage();
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result result = new Result();
        result.success = false;
        result.code = code;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(int httpStatusCode, String message) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = null;
        if (Objects.nonNull(attributes)) {
            response = attributes.getResponse();
        }

        if (Objects.nonNull(response)) {
            response.setStatus(httpStatusCode);
        }

        return error((Integer) httpStatusCode, message);
    }

    public static <T> Result<T> error(int httpStatusCode, Integer code, String message) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = null;
        if (Objects.nonNull(attributes)) {
            response = attributes.getResponse();
        }

        if (Objects.nonNull(response)) {
            response.setStatus(httpStatusCode);
        }

        return error(code, message);
    }
   

}