package com.beiyuan.seckill.exception;

import com.beiyuan.seckill.vo.RespBean;
import com.beiyuan.seckill.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author: beiyuan
 * @date: 2023/5/1  14:20
 */
@RestControllerAdvice //返回结果
public class GlobalExceptionHandler {

    //异常处理的注解
    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandler(Exception e){
        if(e instanceof GlobalException){
            //强转
            GlobalException ex=(GlobalException)e;
            return RespBean.error(ex.getRespBeanEnum());
        }else if(e instanceof BindException){ //手机号码格式不对时的会自己抛的异常
            BindException ex=(BindException)e;
            RespBean respBean=RespBean.error(RespBeanEnum.BIND_ERROR);
            //获取异常的信息
            respBean.setMessage("手机号码格式不对"+ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        e.printStackTrace();
        return RespBean.error(RespBeanEnum.ERROR);
    }
}
