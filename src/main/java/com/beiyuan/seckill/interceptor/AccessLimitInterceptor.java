package com.beiyuan.seckill.interceptor;


import com.beiyuan.seckill.annotation.AccessLimit;
import com.beiyuan.seckill.config.UserContext;
import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.service.IUserService;
import com.beiyuan.seckill.utils.CookieUtil;
import com.beiyuan.seckill.vo.RespBean;
import com.beiyuan.seckill.vo.RespBeanEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解拦截器
 * @author: beiyuan
 * @date: 2023/5/13  17:40
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    IUserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(handler instanceof HandlerMethod){
            HandlerMethod hm=(HandlerMethod)handler;
            //拿到方法上的注解
            AccessLimit accessLimit=(AccessLimit)hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit==null){
                //没有该注解，则放行
                return true;
            }
            int second=accessLimit.second();
            int maxCount=accessLimit.maxCount();
            boolean needLogin=accessLimit.needLogin();

            //判断是否合理
            String key=request.getRequestURI();

            if(needLogin){
                User user=UserContext.getUser();
                if (user==null){
                    //渲染响应
                    render(response, RespBeanEnum.SESSION_ERROR);
                    return false;
                }
                key+=":"+user.getId();
            }
            Integer count= (Integer) redisTemplate.opsForValue().get(key);
            if(count==null){
                //第一次进来，或者上一时间段的key已经失效。重新key及其失效时间,value=1表示访问了第一次
                redisTemplate.opsForValue().set(key,1,maxCount, TimeUnit.SECONDS);
            }else if(count<maxCount){
                redisTemplate.opsForValue().increment(key);
            }else {
                //超过访问次数
                render(response,RespBeanEnum.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return  true;
    }

    //因为没有走controller，所以要手动渲染response
    private void render( HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter printWriter=response.getWriter();
        RespBean respBean=RespBean.error(respBeanEnum);
        //写入到响应的输出流
        printWriter.write(new ObjectMapper().writeValueAsString(respBean));
        printWriter.flush();
        printWriter.close();
    }
}
