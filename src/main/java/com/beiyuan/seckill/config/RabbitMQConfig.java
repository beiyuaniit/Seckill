package com.beiyuan.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: beiyuan
 * @date: 2023/5/12  18:11
 */
@Configuration
/*
用于定义配置类，可替换xml配置文件，被注解的类内部包含有一个或多个被@Bean注解的方法，
这些方法将会被AnnotationConfigApplicationContext或AnnotationConfigWebApplicationContext类进行扫描，
并用于构建bean定义，初始化Spring容器
 */
public class RabbitMQConfig {
    private static final String QUEUE_TOPIC_SECKILL="seckillQueue";
    private static final String EXCHANGE_TOPIC_SECKILL="seckillExchange";
    private static final String ROUTINGKEY_TOPIC_SECKILL="seckill.#";
    @Bean
    public Queue topicQueueSeckill(){
        return new Queue(QUEUE_TOPIC_SECKILL);
    }

    @Bean
    public TopicExchange topicExchangeSeckill(){
        return new TopicExchange(EXCHANGE_TOPIC_SECKILL);
    }

    @Bean
    public Binding bindingTopicSeckill(){
        return BindingBuilder.bind(topicQueueSeckill()).to(topicExchangeSeckill()).with(ROUTINGKEY_TOPIC_SECKILL);
    }

}
