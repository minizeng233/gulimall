package com.junting.gulimall.seckill.feign;

import com.junting.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author mini_zeng
 * @create 2022-01-20 17:15
 */

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/lates3DaySession")
    R getLate3DaySession();

}
