package com.beiyuan.seckill.controller;

import com.beiyuan.seckill.service.IUserService;
import com.beiyuan.seckill.vo.LoginVo;
import com.beiyuan.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author: beiyuan
 * @date: 2023/4/30  14:16
 */
@Controller
@RequestMapping("login")
@Slf4j
public class LoginController {

    @Autowired
    IUserService userService;
    @RequestMapping("toLogin")
    public String toLogin() {
        return "login";
    }

    @PostMapping("doLogin")
    @ResponseBody
    //@Valid 表示对LoginVo进行校验
    public RespBean doLogin(@Valid LoginVo loginVo ,
        HttpServletRequest request, HttpServletResponse response) {
        System.out.println(loginVo);
        log.info("{}", loginVo);
        return userService.doLogin(loginVo,request,response);
    }
}
