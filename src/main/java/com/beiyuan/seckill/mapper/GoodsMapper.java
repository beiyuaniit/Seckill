package com.beiyuan.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beiyuan.seckill.entity.Goods;
import com.beiyuan.seckill.vo.GoodsVo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: beiyuan
 * @date: 2023/5/1  16:53
 */
@Repository
public interface GoodsMapper extends  BaseMapper<Goods>{
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
