package com.beiyuan.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beiyuan.seckill.entity.SeckillGoods;
import com.beiyuan.seckill.mapper.SeckillGoodsMapper;
import com.beiyuan.seckill.service.ISeckillGoodsService;
import org.springframework.stereotype.Service;

/**
 * @author: beiyuan
 * @date: 2023/5/1  17:03
 */
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements ISeckillGoodsService {
}
