package com.app.uni_app.aop.annotation;


import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//生成 Javadoc 时包含该注解
@Documented
public @interface RemoveOrderSessionAnnotation {

}
