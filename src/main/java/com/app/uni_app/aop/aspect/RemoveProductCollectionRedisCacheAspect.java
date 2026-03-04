package com.app.uni_app.aop.aspect;


import com.app.uni_app.common.result.Result;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Aspect
@Component
public class RemoveProductCollectionRedisCacheAspect {
    @Pointcut(value = "@annotation(com.app.uni_app.aop.annotation.RemoveProductCollectionRedisCacheAnnotation)")
    public void pointCut() {
    }

    @Around("pointCut()")
    @SuppressWarnings("unchecked")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = joinPoint.proceed();
        if (Objects.isNull(proceed)) {
            throw new Throwable();

        }
        Result<Object> result = (Result<Object>) proceed;
        if (!result.getSuccess()) {
            return result;

        }
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return result;

        }
        if (!(args[0] instanceof String products)) {
            return result;

        }
        List<String> productList = Arrays.stream(StringUtils.split(products, ",")).toList();
        RedisConnector.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                for (String productId : productList) {
                    String key = RedisKeyGenerator.productCollection(Long.valueOf(productId));
                    RedisConnector.delete(key);
                }
                return null;
            }
        });
        return result;


    }

}
