package com.beiyuan.seckill.annotation;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解，加在方法上。
 * 每x秒内最多访问y次
 * 是否需要登陆
 * @author: beiyuan
 * @date: 2023/5/13  17:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {
    int second();
    int maxCount();
    boolean needLogin();
}
