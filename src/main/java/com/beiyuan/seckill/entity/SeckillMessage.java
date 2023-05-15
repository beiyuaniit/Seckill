package com.beiyuan.seckill.entity;

import com.beiyuan.seckill.vo.GoodsVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.pl.NIP;

/**
 * @author: beiyuan
 * @date: 2023/5/12  17:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage {
    private User user;

    private Long goodsId;
}
