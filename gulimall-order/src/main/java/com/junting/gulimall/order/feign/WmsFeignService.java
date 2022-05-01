package com.junting.gulimall.order.feign;

import com.junting.common.utils.R;
import com.junting.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-17 14:57
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {

    @PostMapping("ware/waresku/hasStock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    /**
     * 锁定库存
     */
    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
