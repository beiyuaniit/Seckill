package com.beiyuan.seckill.vo;

import com.beiyuan.seckill.validator.IsMobile;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author: beiyuan
 * @date: 2023/4/30  14:37
 */
@Data
@ToString
public class LoginVo {
    //id就是手机号
    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    @Length(min=32)
    private String password;
}
