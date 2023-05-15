package com.beiyuan.seckill.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手机号码校验类，判断是否是合法的手机号码
 * @author: beiyuan
 * @date: 2023/4/30  15:03
 */
public class ValidatorUtil {
    //用正则表达式校验
    private static final Pattern mobile_pattern=Pattern.compile("[1]([3-9)])[0-9]{9}$");

    public static final boolean isMobile(String  mobile){
        if(StringUtils.isEmpty(mobile)){
            return false;
        }
        //验证
        Matcher matcher=mobile_pattern.matcher(mobile);
        return matcher.matches();
    }
}
