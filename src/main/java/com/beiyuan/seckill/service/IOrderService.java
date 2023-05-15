package com.beiyuan.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beiyuan.seckill.entity.Order;
import com.beiyuan.seckill.entity.OrderDetailVo;
import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.vo.GoodsVo;

/**
 * @author: beiyuan
 * @date: 2023/5/1  17:02
 */
public interface IOrderService extends IService<Order> {
    Order seckill(User user, GoodsVo goodsVo);

    OrderDetailVo detail(Long orderId);

    String createPath(User user, Long goodsId);

    boolean checkPath(String path, User user, Long goodsId);

    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
