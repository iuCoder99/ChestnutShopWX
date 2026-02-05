package com.app.uni_app.common.handler;


import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.exception.PayException;
import com.app.uni_app.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@Order(2)
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //TODO 业务异常处理...


    @ExceptionHandler(PayException.class)
    public Result handlePayException() {
        return Result.error(MessageConstant.NETWORK_ERROR);
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(HttpServletRequest request, NoResourceFoundException e) {
        // 判断是否是 favicon.ico 请求，是则不打印日志
        if ("/favicon.ico".equals(request.getRequestURI())) {
            return;
        }
        // 其他资源找不到的异常，正常打印日志
        log.error("globalExceptionHandler拦截到:{};异常信息:{}", e.getClass(), e.getMessage(), e);
    }

    /**
     * "用户名已存在"
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({DuplicateKeyException.class, SQLIntegrityConstraintViolationException.class})
    public Result DKExceptionHandler(Exception ex) {
        //Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            return Result.error(MessageConstant.USER_NAME_EXISTS);
        }
        log.error("DK ExceptionHandler拦截到:{};异常信息:{}", ex.getClass(), ex.getMessage());
        return Result.error(MessageConstant.TOM_CAT_ERROR);
    }

    /**
     * BCrypt 用户登录账户校验异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public Result IAEExceptionHandler(Exception ex) {
        String message = ex.getMessage();
        if (message.contains("Invalid salt version")) {
            return Result.error(MessageConstant.LOGIN_ERROR);
        }
        log.error("IAE ExceptionHandler拦截到:{};异常信息:{}", ex.getClass(), ex.getMessage());
        return Result.error(MessageConstant.TOM_CAT_ERROR);
    }

    /**
     * 捕获所有数据库保存数据相关异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({SQLException.class})
    public Result<?> SQLExceptionHandler(Exception ex) {
        log.error("数据库操作失败", ex);
        log.error(" SQL ExceptionHandler拦截到:{};异常信息:{}", ex.getClass(), ex.getMessage());
        return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
    }

    /**
     * 全局异常拦截
     * "服务器异常"
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public Result globalExceptionHandler(Exception e) {
        log.error("完整异常栈:", e);
        log.error("globalExceptionHandler拦截到:{};异常信息:{}", e.getClass(), e.getMessage());
        return Result.error(MessageConstant.TOM_CAT_ERROR);
    }
}
