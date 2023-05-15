package com.beiyuan.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beiyuan.seckill.entity.Goods;
import com.beiyuan.seckill.mapper.GoodsMapper;
import com.beiyuan.seckill.service.IGoodsService;
import com.beiyuan.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: beiyuan
 * @date: 2023/5/1  17:03
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper,Goods>implements IGoodsService {

    @Autowired
    GoodsMapper goodsMapper;
    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoByGoodsId(Long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}
