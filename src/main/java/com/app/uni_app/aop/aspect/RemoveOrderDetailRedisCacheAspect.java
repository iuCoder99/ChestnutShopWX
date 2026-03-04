package com.app.uni_app.aop.aspect;


import com.app.uni_app.common.result.Result;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class RemoveOrderDetailRedisCacheAspect {
    @Pointcut("@annotation(com.app.uni_app.aop.annotation.RemoveOrderDetailRedisCacheAnnotation)")
    public void pointCut() {
    }

    @Around("pointCut()")
    @SuppressWarnings("unchecked")
    public Object afterReturnSuccess(ProceedingJoinPoint joinPoint) throws Throwable {
        Object resultObject = joinPoint.proceed();
        if (Objects.isNull(resultObject)) {
            throw new Throwable();
        }
        Result<Object> result = (Result<Object>) resultObject;
        Boolean isSuccess = result.getSuccess();
        if (!isSuccess) {
            return result;
        }
        Object[] args = joinPoint.getArgs();
        if (args.length==0){
            return result;
        }
        Object arg = args[0];
        if (!(arg instanceof String)){
            return result;
        }
        String key = RedisKeyGenerator.orderKey((String) arg);
        RedisConnector.delete(key);
        return result;
    }
}
