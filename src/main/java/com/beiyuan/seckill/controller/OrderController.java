package com.beiyuan.seckill.controller;

import com.beiyuan.seckill.entity.OrderDetailVo;
import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.service.IOrderService;
import com.beiyuan.seckill.vo.RespBean;
import com.beiyuan.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: beiyuan
 * @date: 2023/5/1  16:59
 */
@Controller
@RequestMapping("order")
public class OrderController {

    @Autowired
    IOrderService orderService;
    @GetMapping("detail")
    @ResponseBody
    public RespBean detail(User user,Long orderId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detailVo=orderService.detail(orderId);
        return RespBean.success(detailVo);
    }
}
