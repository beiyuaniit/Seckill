package com.beiyuan.seckill.controller;

import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: beiyuan
 * @date: 2023/4/30  14:15
 */

@Slf4j
@RequestMapping("user")
public class UserController {

    /*
     这个应该是获取已经登陆的用户信息，通过参数处理器进行注入
     */
    @GetMapping("info")
    public RespBean info(User user){
        return RespBean.success(user);
    }
}
