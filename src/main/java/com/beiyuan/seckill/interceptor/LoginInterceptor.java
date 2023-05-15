package com.beiyuan.seckill.interceptor;

import com.beiyuan.seckill.config.UserContext;
import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.service.IUserService;
import com.beiyuan.seckill.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: beiyuan
 * @date: 2023/5/13  18:09
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    IUserService userService;

    //true放行，false拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        setLoginUserToUserContext(request,response);
        return true;
    }

    private void setLoginUserToUserContext(HttpServletRequest request, HttpServletResponse response) {
        String userTicket= CookieUtil.getCookieValue(request,"userTicket");
        if(userTicket==null){
            return ;
        }
        User user=userService.getUserByRedisCookie(userTicket,request,response);
        UserContext.setUser(user);//更新ThreadLocal<User>
    }
}
