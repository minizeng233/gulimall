package com.junting.gulimall.cart.feign;

import com.junting.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-15 15:17
 */
@FeignClient("gulimall-product")
public interface productFeignService {

    @GetMapping("product/skuinfo/info/{skuId}")
    R SkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfo/{skuId}/price")
    R getPrice(@PathVariable("skuId") Long skuId);
}
