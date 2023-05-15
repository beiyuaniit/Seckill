package com.beiyuan.seckill.validator;

import com.beiyuan.seckill.utils.ValidatorUtil;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 用于校验手机号的类
 * @author: beiyuan
 * @date: 2023/5/1  14:04
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {
    private boolean required=false;
    //初始化
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required=constraintAnnotation.required();
    }


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(required){
            return ValidatorUtil.isMobile(value);
        }else {
            //非必填的话，没有就返回true，有则校验
            if(StringUtils.isEmpty(value)){
                return true;
            }else {
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
