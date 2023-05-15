package com.beiyuan.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author: beiyuan
 * @date: 2023/5/1  15:40
 */
@Configuration
public class RedisConfig {

    //将值序列化为二进制

    /**
     * 如果只是存储字符串，不用序列化也行，但是存储对象就要定义序列化方式
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String,Object>redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object>redisTemplate=new RedisTemplate<>();
        //key序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //value序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        //hash类型value序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //hash的value序列化
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        //注入连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public DefaultRedisScript<Boolean> lockScript(){
        DefaultRedisScript<Boolean> script=new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/lock.lua"));
        script.setResultType(Boolean.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> stockScript(){
        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/stock.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
