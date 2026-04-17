package com.app.uni_app.aop.aspect.common;

import com.app.uni_app.aop.annotation.common.ParamCheckAnnotation;
import com.app.uni_app.aop.annotation.emums.CheckEnum;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 参数校验AOP切面：自动拦截带@ParamCheck的方法，校验参数
 */
@Aspect
@Component
public class ParamCheckAspect {

    private static final Logger log = LoggerFactory.getLogger(ParamCheckAspect.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


    @Before("@annotation(paramCheckAnnotation)")
    public void checkMethodParams(JoinPoint joinPoint, ParamCheckAnnotation paramCheckAnnotation) {
        // 1. 获取基础信息
        String executeTime = LocalDateTime.now().format(FORMATTER);
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs(); // 参数值

        String[] paramNames = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            paramNames[i] = "param" + (i + 1);
        }

        // 2. 执行参数校验
        List<String> invalidFields = validateParams(args, paramNames, paramCheckAnnotation.mode());

        // 3. 存在非法参数：打印日志 + 抛NPE
        if (!invalidFields.isEmpty()) {
            handleError(executeTime, methodName, invalidFields);
        }
    }

    /**
     * 核心校验逻辑
     */
    private List<String> validateParams(Object[] args, String[] paramNames, CheckEnum mode) {
        List<String> invalidFields = new ArrayList<>();

        if (args == null || args.length == 0) {
            return invalidFields;
        }

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String paramName = paramNames[i];

            // 模式1：仅校验null（所有类型通用）
            if (mode == CheckEnum.NOT_NULL) {
                if (arg == null) {
                    invalidFields.add(paramName);
                }
                continue;
            }

            // 模式2：校验非空（字符串特殊处理）
            if (mode == CheckEnum.NOT_BLANK) {
                if (arg == null) {
                    invalidFields.add(paramName);
                } else if (arg instanceof String && !StringUtils.hasText((String) arg)) {
                    invalidFields.add(paramName);
                }
            }
        }
        return invalidFields;
    }

    /**
     * 统一处理错误：打印日志 + 抛出空指针异常
     */
    private void handleError(String executeTime, String methodName, List<String> invalidFields) {
        NullPointerException exception = new NullPointerException("传入非法参数");

        // 打印规范日志
        log.error("""
                        ==============================================
                        【参数校验失败】
                        执行时间：{}
                        执行方法：{}
                        非法参数字段：{}
                        异常堆栈：
                        """,
                executeTime, methodName, invalidFields, exception);

        // 手动抛出异常
        throw exception;
    }
}