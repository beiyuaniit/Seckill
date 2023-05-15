package com.beiyuan.seckill.config;

import com.beiyuan.seckill.entity.User;

/**
 * 获取当前线程处理的user对象，当前线程的User上下文，保存一些User信息
 * @author: beiyuan
 * @date: 2023/5/13  17:49
 */
public class UserContext {
    private static ThreadLocal<User> userHolder=new ThreadLocal<>();

    public static void setUser(User user){
        userHolder.set(user);
    }

    public static User getUser(){
        return userHolder.get();
    }
}
