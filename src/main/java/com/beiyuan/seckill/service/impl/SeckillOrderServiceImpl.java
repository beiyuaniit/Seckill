package com.beiyuan.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beiyuan.seckill.entity.SeckillOrder;
import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.mapper.SeckillOrderMapper;
import com.beiyuan.seckill.service.ISeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author: beiyuan
 * @date: 2023/5/1  17:03
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {
    @Autowired
    SeckillOrderMapper seckillOrderMapper;

    @Autowired
    RedisTemplate redisTemplate;
    /*
    获取秒杀结果
     */
    @Override
    public Long getResult(User user, Long goodsId) {
        SeckillOrder seckillOrder=seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().eq("user_id",user.getId())
                .eq("goods_id",goodsId));
        if(seckillOrder!=null){
            return seckillOrder.getOrderId();
        }else if(redisTemplate.hasKey("isStockEmpty:"+goodsId)){
            return -1L;
        }else {
            return 0L;
        }
    }
}
