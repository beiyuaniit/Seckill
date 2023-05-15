package com.beiyuan.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.exception.GlobalException;
import com.beiyuan.seckill.mapper.UserMapper;
import com.beiyuan.seckill.service.IUserService;
import com.beiyuan.seckill.utils.CookieUtil;
import com.beiyuan.seckill.utils.MD5Util;
import com.beiyuan.seckill.utils.UUIDUtil;
import com.beiyuan.seckill.utils.ValidatorUtil;
import com.beiyuan.seckill.vo.LoginVo;
import com.beiyuan.seckill.vo.RespBean;
import com.beiyuan.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author: beiyuan
 * @date: 2023/4/30  14:50
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisTemplate<String,Object>redisTemplate;

//    //用户注册 ,算了不写先
//    //登陆验证
//    @Override
//    public RespBean doRegister(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
//        String mobile= loginVo.getMobile();
//        String password=loginVo.getPassword();
//        User user=new User();
//
//        //生成注册并登陆成功的cookie
//        String ticket= UUIDUtil.uuid();
//        // request.getSession().setAttribute(ticket,user); //存入请求的session域，表示本次登陆有效。若是spring session的话，会自动把这个session存入到redis
//
//        //将用户信息存入到redis中，自己加个前缀
//        //设置30分钟有效时间,cookir没有设置过期，那就session设置过期吧
//        redisTemplate.opsForValue().set("user:"+ticket,user,30, TimeUnit.MINUTES);
//
//        //根据请求等信息把cookie设置给响应
//        CookieUtil.setCookie(request,response,"userTicket",ticket);
//        return RespBean.success(ticket);
//    }

    //登陆验证
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile= loginVo.getMobile();
        String password=loginVo.getPassword();

        log.info(password);
        //通过@Valid进行手机号码和非空校验了
//        //非空判断
//        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        //验证手机号
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }
        //查询
        User user=userMapper.selectById(mobile);
        if(user==null  || !MD5Util.formPassToDBPass(password,user.getSalt()).equals(user.getPassword())){
           // return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
//        if(!MD5Util.formPassToDBPass(password,user.getSalt()).equals(user.getPassword())){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }

        //生成登陆成功的cookie
        String ticket= UUIDUtil.uuid();
       // request.getSession().setAttribute(ticket,user); //存入请求的session域，表示本次登陆有效。若是spring session的话，会自动把这个session存入到redis
        log.info("");
        //将用户信息存入到redis中，自己加个前缀
        //设置30分钟有效时间,cookir没有设置过期，那就session设置过期吧
        redisTemplate.opsForValue().set("user:"+ticket,user,30, TimeUnit.MINUTES);

        //根据请求等信息把cookie设置给响应
        CookieUtil.setCookie(request,response,"userTicket",ticket);
        return RespBean.success(ticket);
    }

    //根据cookie获取用户
    @Override
    public User getUserByRedisCookie(String userTicket,HttpServletRequest request,HttpServletResponse response) {
        if(userTicket==null){
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        //再存一次到response，防止已经cookie过期
        if(user!=null){
            CookieUtil.setCookie(request,response,"userTicket",userTicket);
        }
        return user;
    }

    /*
    更新密码
     */
    //每次更新数据库都要先删除redis .userTicket先登陆才能更改密码
    @Override
    public RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response) {
        User user=getUserByRedisCookie(userTicket,request,response);
        if(user==null){
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.formPassToDBPass(password,user.getSalt()));
        int result=userMapper.updateById(user);
        if(1==result){
            redisTemplate.delete("user:"+userTicket);
            //改完密码后要重新登陆。所以这里不再加入redis
            return RespBean.success();
        }

        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
