package com.app.uni_app.aop.aspect;


import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.util.SessionUtils;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
public class RemoveOrderSessionAspect {

    @Resource
    private SessionUtils sessionUtils;

    @Pointcut(value = "@annotation(com.app.uni_app.aop.annotation.RemoveOrderSessionAnnotation)")
    public void pointcut() {
    }

    @AfterReturning(pointcut = "pointcut()", returning = "result")
    public void afterReturnSuccess(Result<?> result) {
        if (Objects.isNull(result)) {
            return;
        }
        Boolean success = result.getSuccess();
        if (!success) {
            return;
        }
        sessionUtils.removeUserAllOrder();
    }
}
