package com.beiyuan.seckill.rabbitmq;

import com.beiyuan.seckill.entity.SeckillMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: beiyuan
 * @date: 2023/5/12  18:27
 */
@Service
public class MQSender {
    @Autowired
    RabbitTemplate rabbitTemplate;

    /*
    发送秒杀消息到mq
     */
    public void sendSeckillMessage(String message) {
        rabbitTemplate.convertAndSend("seckillExchange","seckill.message",message);
    }
}
