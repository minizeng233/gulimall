package com.junting.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author mini_zeng
 * @create 2022-01-21 12:27
 */
@Data
public class SecKillOrderTo {

    /**
     * 秒杀订单id
     */
    private String orderSn;

    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer num;

    /**
     * 会员id
     */
    private Long memberId;
}
