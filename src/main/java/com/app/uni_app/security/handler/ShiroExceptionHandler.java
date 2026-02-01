package com.app.uni_app.security.handler;

import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@Slf4j
@RestControllerAdvice
public class ShiroExceptionHandler {

    // 认证失败（令牌过期、无效、未登录）
    @ExceptionHandler(UnauthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleUnauthenticatedException(Exception e) {
        return Result.error(MessageConstant.USER_NOT_LOGIN);

    }

    // 权限不足
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleUnauthorizedException(Exception e){
        return Result.error(MessageConstant.PERMISSION_DENIED);
    }

    
    //shiro 模块级捕捉器
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleShiroException(Exception e){
        return Result.error(MessageConstant.SYSTEM_ERROR);

    }

}
