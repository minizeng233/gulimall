package com.junting.gulimall.seckill.service;

import com.junting.common.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-20 17:12
 */

public interface SeckillService {

    void uploadSeckillSkuLatest3Day();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);


    String kill(String killId, String key, Integer num);
}
