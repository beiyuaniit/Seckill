package com.beiyuan.seckill.rabbitmq;

import com.beiyuan.seckill.entity.Order;
import com.beiyuan.seckill.entity.SeckillMessage;
import com.beiyuan.seckill.entity.SeckillOrder;
import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.exception.GlobalException;
import com.beiyuan.seckill.service.IGoodsService;
import com.beiyuan.seckill.service.IOrderService;
import com.beiyuan.seckill.utils.JsonUtil;
import com.beiyuan.seckill.vo.GoodsVo;
import com.beiyuan.seckill.vo.RespBean;
import com.beiyuan.seckill.vo.RespBeanEnum;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author: beiyuan
 * @date: 2023/5/12  18:28
 */
@Service
public class MQReceiver {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    IOrderService orderService;

    @Autowired
    IGoodsService goodsService;


    @RabbitListener(queues = "seckillQueue")
    public void receiveSeckillMessage(String msg){
        SeckillMessage seckillMessage= JsonUtil.jsonStr2Object(msg,SeckillMessage.class);
        User user= seckillMessage.getUser();
        Long goodsId=seckillMessage.getGoodsId();

        GoodsVo goodsVo= (GoodsVo) redisTemplate.opsForValue().get("goods:"+goodsId);
        //判断库存
        if(goodsVo==null){
            goodsVo=goodsService.findGoodsVoByGoodsId(goodsId);
            redisTemplate.opsForValue().set("goods:"+goodsId,goodsVo);
        }
        if(goodsVo.getStockCount()<1){
            return ;
        }

        //重复抢购则不进数据库，一层层来
        SeckillOrder seckillOrder= (SeckillOrder) redisTemplate.opsForValue().get("user:"+user.getId()+":"+goodsId);
        if(seckillOrder!=null){
            return;
        }
        //判断后开始真正的抢购.
        try {
            Order order=orderService.seckill(user,goodsVo);      //秒杀后返回订单 goodsVo里面的库存已经减一了
        }catch ( Exception e){
            //抛异常好像消息消费不成功，会一直循环
            e.printStackTrace();
        }

        //更新redis中的库存
        redisTemplate.opsForValue().set("goods:"+goodsId,goodsVo);
    }
}
