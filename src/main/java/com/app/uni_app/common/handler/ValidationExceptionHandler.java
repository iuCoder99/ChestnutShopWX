package com.app.uni_app.common.handler;


import com.app.uni_app.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全局参数校验异常处理器
 */
@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {

    /**
     * 处理@RequestBody + @Validated 校验失败异常（JSON请求体）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 取第一个校验失败的字段提示语（用户友好）
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数格式错误";
        log.warn("请求体参数校验失败：{}", message, e);
        return Result.error(400, message);
    }

    /**
     * 处理@RequestParam/@PathVariable + @Validated 校验失败异常（Query/Path参数）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        // 拼接所有校验失败提示（多参数场景）
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
        log.warn("路径/查询参数校验失败：{}", message, e);
        return Result.error(400, message);
    }

    /**
     * 处理DTO绑定失败异常（如类型不匹配）
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("参数绑定失败：{}", message, e);
        return Result.error(400, message);
    }
}
