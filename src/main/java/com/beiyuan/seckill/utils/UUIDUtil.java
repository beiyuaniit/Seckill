package com.beiyuan.seckill.utils;

import java.util.UUID;

/**
 * 获取随机UUID的工具类
 * @author: beiyuan
 * @date: 2023/5/1  14:33
 */
public class UUIDUtil {
    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
