package com.junting.gulimall.order.feign;

import com.junting.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-16 23:08
 */

@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();

}