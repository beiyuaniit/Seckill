package com.beiyuan.seckill.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 验证是否是手机号的注解
 * @author: beiyuan
 * @date: 2023/5/1  14:01
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
//用于校验的类
@Constraint(validatedBy = {IsMobileValidator.class})
public @interface IsMobile {

    //默认必须要有手机号码
    boolean required() default true;

    String message() default "手机号码格式错误";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
