package com.beiyuan.seckill.config;

import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.service.IUserService;
import com.beiyuan.seckill.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义用户参数
 * 对controller的入参进行判断、处理
 * 可以理解为对controller 进行手动参数注入
 *
 * 好像只是参数处理，并没有进行请求拦截，之后再做吧
 * @author: beiyuan
 * @date: 2023/5/1  16:08
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    IUserService userService;

    //只对入参类型为User的进行处理，这个返回true后才会走下面这个方法
    // 若参数有User，但是没有传递过来 实参，则最后为null
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //System.out.println("参数解析");
        Class<?>clazz = parameter.getParameterType();
        return clazz== User.class;
    }

    //如果有User这个类型的参数，则用响应的ticket去redis中获取user对象并注入这个参数

    //这里其实就是对象缓存
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        //第一种写法
//        HttpServletRequest request=webRequest.getNativeRequest(HttpServletRequest.class);
//        HttpServletResponse response=webRequest.getNativeResponse(HttpServletResponse.class);
//        //获取请求中cookie
//        String ticket= CookieUtil.getCookieValue(request,"userTicket");
//        return userService.getUserByRedisCookie(ticket,request,response);

        //获取已经登陆的
        return UserContext.getUser();
    }
}
