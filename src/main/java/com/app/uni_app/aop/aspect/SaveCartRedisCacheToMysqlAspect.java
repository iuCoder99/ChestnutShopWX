package com.app.uni_app.aop.aspect;

import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.job.delay.SaveCartRedisCacheDelayJob;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class SaveCartRedisCacheToMysqlAspect {

    @Resource
    private SaveCartRedisCacheDelayJob saveCartRedisCacheDelayJob;


    @Pointcut(value = "@annotation(com.app.uni_app.aop.annotation.SaveCartRedisCacheToMysqlAnnotation)")
    public void pointCut() {
    }

    /**
     * 清除空对象
     */
    @Before("pointCut()")
    public void beforeMethodExecution(){
        String userId = BaseContext.getUserId();
        String cartKey = RedisKeyGenerator.cartKey(Long.valueOf(userId));
        String emptyCartHashKey = RedisKeyGenerator.cartHashKey("0", "0");
        RedisConnector.opsForHash().delete(cartKey, emptyCartHashKey);
    }


    /**
     * cart RedisCache 延迟存库
     * @param result
     */
    @AfterReturning(pointcut = "pointCut()", returning = "result")
    public void afterReturnSuccess(Result<?> result) {
        if (Objects.isNull(result)) {
            return;
        }
        Boolean isSuccess = result.getSuccess();
        if (!isSuccess) {
            return;
        }
        String userId = BaseContext.getUserId();
        saveCartRedisCacheDelayJob.setUserIdToDelayedQueue(userId);
    }
}
