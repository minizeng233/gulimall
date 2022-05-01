package com.junting.gulimall.product.feign;

import com.junting.common.to.SkuHasStockVo;
import com.junting.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-08 17:04
 */

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> SkuIds);
}
