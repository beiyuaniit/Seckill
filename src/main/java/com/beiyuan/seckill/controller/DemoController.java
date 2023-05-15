package com.beiyuan.seckill.controller;

import com.beiyuan.seckill.entity.Order;
import com.beiyuan.seckill.entity.SeckillOrder;
import com.beiyuan.seckill.config.rabbitmqtest.MQSenderTest;
import com.beiyuan.seckill.service.IOrderService;
import com.beiyuan.seckill.service.ISeckillGoodsService;
import com.beiyuan.seckill.service.ISeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: beiyuan
 * @date: 2023/4/30  13:20
 */
@Controller
@RequestMapping("demo")
@Slf4j
public class DemoController {

    @Autowired
    ISeckillGoodsService seckillGoodsService;

    @Autowired
    IOrderService orderService;

    @Autowired
    ISeckillOrderService seckillOrderService;

    @Autowired
    MQSenderTest mqSenderTest;

    @RequestMapping("hello")
    public String hello(Model model){
        model.addAttribute("name","xxxxx");
        return "hello";
    }

    /*
    测试发送RabbitMQ消息
     */
    @RequestMapping("mq/sendDefault")
    @ResponseBody
    public void mqSend(){
        mqSenderTest.sendDefaultExchange("Hello");
    }

    @GetMapping("mq/sendFanout")
    @ResponseBody
    public void mqSendFanout(){
        mqSenderTest.sendFanoutExchange("this is a fanout message");
    }

    @GetMapping("mq/sendDirect01")
    @ResponseBody
    public void mqSendDirect01(){
        mqSenderTest.sendDirestExchange01("msg : direct 01 red");
    }

    @GetMapping("mq/sendDirect02")
    @ResponseBody
    public void mqSendDirect02(){
        mqSenderTest.sendDirestExchange02("msg : direct 02 green");
    }

    @GetMapping("mq/sendTopic01")
    @ResponseBody
    public void mqSendTopic01(){
        mqSenderTest.sendTopicExchange01("msg : topic 01");
    }


    @GetMapping("mq/sendTopic02")
    @ResponseBody
    public void mqSendTopic02(){
        mqSenderTest.sendTopicExchange02("msg : topic 02");
    }

    @GetMapping("mq/sendHeaders01")
    @ResponseBody
    public void mqSendHeader01(){
        mqSenderTest.sendHeadersExchange01("msg : headers01 red");
    }

    @GetMapping("mq/sendHeaders02")
    @ResponseBody
    public void mqSendHeader02(){
        log.info("headers 02");
        mqSenderTest.sendHeadersExchange02("msg : headers02 red and green");
    }


    @GetMapping("test")
    @ResponseBody
    public String test(){
        //生成订单
        Order order=new Order();
        order.setUserId(2L);
        order.setGoodsId(2L);
        order.setDeliveryAddrId(0L);
        order.setGoodsName("iphone");
        order.setGoodsCount(1); //当前抢购的数量

        order.setOrderChannel(1);  //
        order.setStatus(0);

        orderService.save(order);
        //生成秒杀订单。为什么要这里，是为了方便后续处理，因为秒杀场景复杂多变
        System.out.println(order.toString());

        SeckillOrder seckillOrder=new SeckillOrder();
        //除了id都不能为null
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(2L);
        seckillOrder.setGoodsId(3L);
        seckillOrderService.save(seckillOrder);

        return order.toString()+seckillOrder.toString();
    }


}
