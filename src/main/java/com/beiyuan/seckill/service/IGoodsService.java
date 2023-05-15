package com.beiyuan.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.beiyuan.seckill.entity.Goods;
import com.beiyuan.seckill.vo.GoodsVo;

import java.util.List;

/**
 * @author: beiyuan
 * @date: 2023/5/1  17:01
 */
public interface IGoodsService extends IService<Goods> {
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
