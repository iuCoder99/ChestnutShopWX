package com.app.uni_app.common.handler;


import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //TODO 业务异常处理...

    /**
     * 处理SQL异常
     * "用户名已存在"
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({DuplicateKeyException.class, SQLIntegrityConstraintViolationException.class})
    public Result exceptionHandler(Exception ex) {
        //Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            return Result.error(MessageConstant.USER_NAME_EXISTS);
        }
        log.error("globalExceptionHandler拦截到:" + ex.getClass() + ";异常信息:" + ex.getMessage());
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
        log.error("globalExceptionHandler拦截到:" + ex.getClass() + ";异常信息:" + ex.getMessage());
        return Result.error(MessageConstant.TOM_CAT_ERROR);
    }

/**
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public Result MExceptionHandler(Exception ex) {
        String message = ex.getMessage();
        if (StringUtils.contains(message, "message")) {
            //message [请输入正确的手机号格式]]
            int start = StringUtils.lastIndexOf(message, "message") + 7 + 2;
            //  请输入正确的手机号格式
            String substring = StringUtils.substring(message, start, message.length() - 1 - 2);
            return Result.error(substring);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
**/
    /**
     * 捕获所有数据库保存数据相关异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler({SQLException.class})
    public Result<?> handleDbException(Exception e) {
        log.error("数据库操作失败", e);
        return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR); // 统一返回失败
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
        log.error("globalExceptionHandler拦截到:" + e.getClass() + ";异常信息:" + e.getMessage());
        return Result.error(MessageConstant.TOM_CAT_ERROR);
    }
}
