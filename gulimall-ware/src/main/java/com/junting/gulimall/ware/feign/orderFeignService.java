package com.junting.gulimall.ware.feign;

import com.junting.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author mini_zeng
 * @create 2022-01-18 23:00
 */
@FeignClient("gulimall-order")
public interface orderFeignService {
    /**
     * 查询订单状态
     */
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
