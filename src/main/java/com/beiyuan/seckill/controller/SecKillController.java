package com.beiyuan.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.beiyuan.seckill.annotation.AccessLimit;
import com.beiyuan.seckill.entity.Order;
import com.beiyuan.seckill.entity.SeckillMessage;
import com.beiyuan.seckill.entity.SeckillOrder;
import com.beiyuan.seckill.entity.User;
import com.beiyuan.seckill.exception.GlobalException;
import com.beiyuan.seckill.rabbitmq.MQSender;
import com.beiyuan.seckill.service.IGoodsService;
import com.beiyuan.seckill.service.IOrderService;
import com.beiyuan.seckill.service.ISeckillOrderService;
import com.beiyuan.seckill.utils.JsonUtil;
import com.beiyuan.seckill.vo.GoodsVo;
import com.beiyuan.seckill.vo.RespBean;
import com.beiyuan.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: beiyuan
 * @date: 2023/5/2  21:14
 */
@Controller
@RequestMapping("seckill")
@Slf4j
public class SecKillController implements InitializingBean {

    @Autowired
    IGoodsService goodsService;
    @Autowired
    ISeckillOrderService seckillOrderService;

    @Autowired
    IOrderService orderService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedisScript<Long> stockScript;
    @Autowired
    MQSender mqSender;


    Map<Long,Boolean>EmptyStockMap=new HashMap<>();
    //系统初始化完成后，将库存存到redis中
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(goodsVoList)){
            return;
        }
        goodsVoList.forEach(goodsVo ->{
            EmptyStockMap.put(goodsVo.getId(),false);
            redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(),goodsVo.getStockCount());
        });

    }

    @PostMapping("/{path}/doSeckill")
    @ResponseBody
    //这里做一些前置判断,redis预减库存
    public RespBean doSecKill(@PathVariable String path, User user, Long goodsId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        //校验路径
        boolean check=orderService.checkPath(path,user,goodsId);
        if(!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        //重复抢购改为redis，提前进行一次判断重复了根本进不来了mq
        SeckillOrder seckillOrder= (SeckillOrder) redisTemplate.opsForValue().get("user:"+user.getId()+":"+goodsId);
        if(seckillOrder!=null){
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        //内存标记，减少redis访问
        if(EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存.但是真的无法解决同一个用户进行了重复的预减。所以可以通过拦截器让每个用户只能请求一次
        //这里预减库存单机情况下没有什么问题，但是分布式情况下可能预减为负数
        //Long stock=redisTemplate.opsForValue().decrement("seckillGoods:"+goodsId);

        //用lua脚本实现判断大于0和预减为原子性，大于0才能预减.由于redis单线程执行，这样即使分布式也能保证不会减少到负数。不是加锁
        Long stock=(Long)redisTemplate.execute(stockScript,Collections.singletonList("seckillGoods:"+goodsId),Collections.EMPTY_LIST);
        if(stock==0){
            EmptyStockMap.put(goodsId,true);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //有拦截器就不用rabbitmq了吗？不是的，rabbitmq是起到了削峰的作用，比如10个商品，却有10000个请求同时进来
        //不能够将后面的消息丢弃，因为还要返回秒杀结果
        //发给mq
        SeckillMessage seckillMessage=new SeckillMessage(user,goodsId);
        //其实可以用一些工具比如fastJson
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));

        return RespBean.success(0);
    }

    /*
    获取秒杀结果
    有订单id：成功
    -1 失败
    0 排队中
     */
    @GetMapping("getResult")
    @ResponseBody
    public RespBean getResult(User user,Long goodsId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);//没登陆
        }
        Long seckillOrderId=seckillOrderService.getResult(user,goodsId);
        return RespBean.success(seckillOrderId);
    }

    /*
    秒杀开始前隐藏地址，开始后才能获取秒杀接口地址
     */
    @GetMapping("path")
    @ResponseBody
    @AccessLimit(second = 5,maxCount = 5,needLogin = true)
    public RespBean getPath(User user,Long goodsId,String captcha){
        //前端传过来的captcha是captcha.text()
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }


        if(!orderService.checkCaptcha(user,goodsId,captcha)){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        //秒杀的时候顺便进行验证码校验
        String path=orderService.createPath(user,goodsId);
        return RespBean.success(path);
    }

    @GetMapping("captcha")
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if(user==null){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //通过流输出给响应，就不用ResponseBody
        response.setContentType("image/jpg");
        response.setHeader("Pargam","No-cache");//不缓存，每次都获取新的
        response.setHeader("Cache-Conttol","no-cache");
        response.setDateHeader("Expires",0);
        //生产数学公式验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32,3);
        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId,captcha.text(),300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.info("验证码生成失败："+e.getMessage());

        }

    }
//    /*
//    没有用到 静态页面
//     */
//    @GetMapping("doSeckill2")
//    public String doSecKill2(Model model,User user,Long goodsId){
//        if(user==null){
//            return "login";
//        }
//        model.addAttribute("user",user);
//
//        GoodsVo goodsVo=goodsService.findGoodsVoByGoodsId(goodsId);
//        //不能用前端的库存，因为秒杀库存数变化很快，通过goosId再进行查询
//        //这里判断的是所有商品的库存
//        if(goodsVo.getStockCount()<1){
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "secKillFail";
//        }
//        //每个用户只能买一次。买了则放在seckill_order里面。判断是否重复购买
//        SeckillOrder seckillOrder=seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("goods_id",goodsId).
//                eq("user_id",user.getId()));
//        if(seckillOrder!=null){
//            model.addAttribute("errmsg",RespBeanEnum.REPEATE_ERROR.getMessage());
//            return "secKillFail";
//        }
//        //开始抢购.
//        Order order=orderService.seckill(user,goodsVo);      //秒杀后返回订单
//        model.addAttribute("order",order);
//        model.addAttribute("goods",goodsVo);
//        System.out.println("秒杀成功");
//        return "orderDetail";
//    }


//    @GetMapping("doSeckill")
//    @ResponseBody
//    //这里做一些前置判断
//    public RespBean doSecKill(User user, Long goodsId){
//        if(user==null){
//            return RespBean.error(RespBeanEnum.SESSION_ERROR);
//        }
//
//        //只是把商品及其库存到redis中，用来判断是否有库存。并没有进行预减。买了后会更新redis的
            //这里是秒杀后再更新，所以是正确的
//        GoodsVo goodsVo= (GoodsVo) redisTemplate.opsForValue().get("goods:"+goodsId);
//        if(goodsVo==null){
//            goodsVo=goodsService.findGoodsVoByGoodsId(goodsId);
//            redisTemplate.opsForValue().set("goods:"+goodsId,goodsVo);
//        }
//
//        //不能用前端的库存，因为秒杀库存数变化很快，通过goosId再进行查询
//        //这里判断的是所有商品的库存
//        if(goodsVo.getStockCount()<1){
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
//        //每个用户只能买一次。买了则放在seckill_order里面。判断是否重复购买
////        SeckillOrder seckillOrder=seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("goods_id",goodsId).
////                eq("user_id",user.getId()));
//        //改为redis
//        SeckillOrder seckillOrder= (SeckillOrder) redisTemplate.opsForValue().get("user:"+user.getId()+":"+goodsId);
//        if(seckillOrder!=null){
//            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
//        }
//        //判断后开始真正的抢购.
//        Order order=orderService.seckill(user,goodsVo);      //秒杀后返回订单 goodsVo里面的库存已经减一了
//        //更新redis中的库存
//        redisTemplate.opsForValue().set("goods:"+goodsId,goodsVo);
//        //System.out.println("秒杀成功");
//        return RespBean.success(order);
//    }





}
