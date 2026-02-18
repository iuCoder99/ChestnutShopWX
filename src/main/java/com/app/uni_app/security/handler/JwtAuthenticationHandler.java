package com.app.uni_app.security.handler;

import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.result.ResultCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.SignatureException;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * JWT 认证异常处理器
 * 使用 Spring 的 @RestControllerAdvice 统一处理 Filter 抛出的异常
 */
@Slf4j
@Order(1)
@RestControllerAdvice
public class JwtAuthenticationHandler {


    @ExceptionHandler(UnauthenticatedException.class)
    public Result<Object> handleUnauthenticatedException(Exception e){
        log.error("token 不存在: {}",e.getMessage());
        return Result.error(HttpResponseStatus.UNAUTHORIZED.code(),ResultCode.NO_TOKEN.getCode(), MessageConstant.NO_ACCESS_TOKEN);
    }

    /**
     * Token 过期异常
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public Result<Object> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("Token 已过期: {}", e.getMessage());
        return Result.error(HttpResponseStatus.UNAUTHORIZED.code(),ResultCode.ACCESS_TOKEN_EXPIRED.getCode(),MessageConstant.TOKEN_EXPIRED);
    }

    /**
     * Token 签名/格式/解码异常
     */
    @ExceptionHandler({SignatureException.class, MalformedJwtException.class, DecodingException.class})
    public Result<Object> handleJwtException(Exception e) {
        log.error("Token 签名错误或格式错误: {}", e.getMessage());
        return Result.error(HttpResponseStatus.UNAUTHORIZED.code(),ResultCode.AUTHENTICATION_SIGNATURE_ERROR.getCode(),MessageConstant.TOKEN_INVALID);
    }


}
