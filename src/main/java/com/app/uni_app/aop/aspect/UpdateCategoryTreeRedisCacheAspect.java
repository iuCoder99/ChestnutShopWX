package com.app.uni_app.aop.aspect;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.service.CategoryService;
import jakarta.annotation.Resource;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Objects;

@Aspect
public class UpdateCategoryTreeRedisCacheAspect {

    @Pointcut(value = "@annotation(com.app.uni_app.aop.annotation.UpdateCategoryTreeRedisCacheAnnotation)")
    private void pointCut() {
    }

    @Resource
    private CategoryService categoryService;

    @AfterReturning(pointcut = "pointCut()", returning = "result")
    public void afterReturnSuccess(Result<Object> result) {
        if (Objects.isNull(result)) {
            return;

        }
        boolean isSuccess = result.getSuccess();
        if (!isSuccess) {
            return;
        }
        categoryService.updateCategoryTreeRedisCache();

    }
}

