package com.beiyuan.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beiyuan.seckill.entity.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.beiyuan.seckill.exception.GlobalException;
import com.beiyuan.seckill.mapper.OrderMapper;
import com.beiyuan.seckill.mapper.SeckillOrderMapper;
import com.beiyuan.seckill.service.IGoodsService;
import com.beiyuan.seckill.service.IOrderService;
import com.beiyuan.seckill.service.ISeckillGoodsService;
import com.beiyuan.seckill.service.ISeckillOrderService;
import com.beiyuan.seckill.utils.MD5Util;
import com.beiyuan.seckill.utils.UUIDUtil;
import com.beiyuan.seckill.vo.GoodsVo;
import com.beiyuan.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * @author: beiyuan
 * @date: 2023/5/1  17:03
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    //要用到秒杀表的
    @Autowired
    ISeckillGoodsService seckillGoodsService;

    @Autowired
    IOrderService orderService;

    @Autowired
    IGoodsService goodsService;
    @Autowired
    OrderMapper orderMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ISeckillOrderService seckillOrderService;
    //用订单去秒杀
    @Override
    @Transactional
    public Order seckill(User user, GoodsVo goodsVo) {
        //再防止一次重复抢购
        if(orderService.getOne(new QueryWrapper<Order>().eq("user_id",user.getId()))!=null){
            return null;
        }

        SeckillGoods  seckillGoods= seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id",goodsVo.getId()));
        //库存减一。这里不用判断库存吗？前面判断了
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);

        //这样写不行。seckillGoods.getStockCount()不具有原子性。两个用户同时进行还是会超卖
        /*
        seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().set("stock_count",seckillGoods.getStockCount())
                .eq("goods_id",goodsVo.getId()).gt("stock_count",0));//stock_count要大于0
         */
        //seckillGoodsService.updateById(seckillGoods);  减一和判断大于0放在一条语句里，具有原子性。innodb更新行时会加行锁（悲观锁
        boolean result=seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = stock_count-1")

                .eq("goods_id",goodsVo.getId()).gt("stock_count",0));//stock_count要大于0
        //不行，因为result可能根本没成功
        /*
        if(seckillGoods.getStockCount()<1){
            return null;
        }
         */
        if(!result){
            return null;
        }


        //生成订单
        Order order=new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1); //当前抢购的数量
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);  //
        order.setStatus(0);


        order.setCreateDate(new Timestamp(new Date().getTime()));
        orderService.save(order);
        //生成秒杀订单。为什么要这里，是为了方便后续处理，因为秒杀场景复杂多变
        SeckillOrder seckillOrder=new SeckillOrder();
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrder.setUserId(user.getId());
        seckillOrderService.save(seckillOrder);

        redisTemplate.opsForValue().set("user:"+user.getId()+":"+goodsVo.getId(),seckillOrder,1, TimeUnit.DAYS);
        if(seckillGoods.getStockCount()<1){
            //库存是否为空
            redisTemplate.opsForValue().set("isEmptyStock:"+seckillGoods.getGoodsId(),0);
        }

        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId==null){
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order=orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        return new OrderDetailVo(order, goodsVo);
    }

    //获取秒杀路径。写在这里 是为了生成订单
    @Override
    public String createPath(User user, Long goodsId) {
        String path= MD5Util.md5(UUIDUtil.uuid()+"123456a");//uuid保证每个用户拿到的秒杀路径都是不一致的
        redisTemplate.opsForValue().set("seckillPath:"+user.getId()+":"+goodsId,path,1,TimeUnit.MINUTES);
        return path;
    }

    //写在这里 也是为了生成订单，其他地方不会再用
    @Override
    public boolean checkPath(String path, User user, Long goodsId) {
        if(user==null || StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath=(String) redisTemplate.opsForValue().get("seckillPath:"+user.getId()+":"+goodsId);
        return redisPath.equals(path);
    }

    //写在这里也是为了订单服务，如果其他很多地方用到，则可以考虑单独出来
    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if(user==null || StringUtils.isEmpty(captcha)){
            return false;
        }
        //前端传过来的captcha是captcha.text()，所以可以直接校验
        String redisCaptcha=(String) redisTemplate.opsForValue().get("captcha:"+user.getId()+":"+goodsId);
        return redisCaptcha.equals(captcha);
    }
}
