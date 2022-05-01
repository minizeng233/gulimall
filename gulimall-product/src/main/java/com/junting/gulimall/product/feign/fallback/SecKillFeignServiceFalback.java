package com.junting.gulimall.product.feign.fallback;

import com.junting.common.exception.BizCodeEnum;
import com.junting.common.utils.R;
import com.junting.gulimall.product.feign.SeckillFeignService;
import org.springframework.stereotype.Component;

/**
 * 远程调用失败后，降级
 * 默认回调
 */
@Component
public class SecKillFeignServiceFalback implements SeckillFeignService {

    @Override
    public R getSkuSeckillInfo(Long skuId) {
        System.out.println("getSkuSeckillInfo触发熔断");
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
    }
}
