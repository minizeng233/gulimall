package com.junting.gulimall.product.feign;

import com.junting.common.utils.R;
import com.junting.gulimall.product.feign.fallback.SecKillFeignServiceFalback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author mini_zeng
 * @create 2022-01-20 22:58
 */
@FeignClient(value = "gulimall-seckill",fallback = SecKillFeignServiceFalback.class)
public interface SeckillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}