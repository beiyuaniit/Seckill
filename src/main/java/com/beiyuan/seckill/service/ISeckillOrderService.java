package com.beiyuan.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beiyuan.seckill.entity.SeckillOrder;
import com.beiyuan.seckill.entity.User;

/**
 * @author: beiyuan
 * @date: 2023/5/1  17:02
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {
    Long getResult(User user, Long goodsId);
}
