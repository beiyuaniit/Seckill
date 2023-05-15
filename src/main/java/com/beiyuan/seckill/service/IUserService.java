package com.beiyuan.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.vo.LoginVo;
import com.beiyuan.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: beiyuan
 * @date: 2023/4/30  14:47
 */
public interface IUserService extends IService<User> {
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);


    User getUserByRedisCookie(String userTicket,HttpServletRequest request,HttpServletResponse response);

    RespBean updatePassword(String userTicket,String password,HttpServletRequest request,HttpServletResponse response);
}
