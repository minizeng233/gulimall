package com.junting.gulimall.product.vo;

import com.junting.gulimall.product.entity.SkuInfoEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author mini_zeng
 * @create 2022-01-20 23:00
 */
@Data
public class SeckillInfoVo {

    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 商品的秒杀随机码
     */
    private String randomCode;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    // 新添加的一个
    SkuInfoEntity skuInfoVo;



    /**
     *  商品秒杀的开始时间
     */
    private Long startTime;

    /**
     *  商品秒杀的结束时间
     */
    private Long endTime;
}
