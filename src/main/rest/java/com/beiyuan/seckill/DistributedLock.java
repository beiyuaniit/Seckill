package com.beiyuan.seckill;

import com.beiyuan.seckill.utils.UUIDUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author: beiyuan
 * @date: 2023/5/12  22:23
 */
@SpringBootTest
public class DistributedLock {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void testLock01(){
        //占位，设置成功才拿到锁
        Boolean isLock=redisTemplate.opsForValue().setIfAbsent("k1","v1");
        if(isLock){
            System.out.println("拿到锁了，进行正常操作");
            //释放锁
            //缺点：此处抛异常就不能释放锁了
            //Integer.parseInt("xxx");
            redisTemplate.delete("k1");
        }else {
            System.out.println("锁正在被占用");
        }
    }

    //配有超时时间处理抛异常不能释放锁
    @Test
    public void testLock02(){
        Boolean isLock=redisTemplate.opsForValue().setIfAbsent("k2","v2",5, TimeUnit.SECONDS);
        if(isLock){
            System.out.println("拿到锁");
            //Integer.parseInt("xxx");
            //缺点：A不抛异常的情况下，执行时间超过了，B线程拿到锁。此时A执行完成后释放锁，就释放了B的锁
            redisTemplate.delete("k2");
        }else {
            System.out.println("锁被占用");
        }
    }

    @Autowired
    RedisScript<Boolean> lockScript;

    @Test
    public void testLock03() throws InterruptedException {
        String uuid= UUIDUtil.uuid();
        Boolean isLock=redisTemplate.opsForValue().setIfAbsent("k3",uuid,1500, TimeUnit.SECONDS);
        if(isLock){
            System.out.println("拿到锁");
            //Integer.parseInt("xxx");
            //增加UUID判断，保证不会释放错锁
//            if(redisTemplate.opsForValue().get("k2")==uuid){
//                redisTemplate.delete("k2");
//            }
            Thread.sleep(1000*30);
            //增加UUID判断，保证不会释放错锁，比较锁和释放锁且要保证原子性
            //用lua脚本，可放java客户端和redis服务器，放java客户端性能较差，每次要带过去；放服务器修改麻烦
            redisTemplate.execute(lockScript, Collections.singletonList("k3"),uuid);
        }else {
            System.out.println("锁被占用");
        }
    }
}
