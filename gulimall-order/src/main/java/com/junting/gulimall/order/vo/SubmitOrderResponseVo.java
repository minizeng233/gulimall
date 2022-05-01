package com.junting.gulimall.order.vo;

import com.junting.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author mini_zeng
 * @create 2022-01-17 18:33
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity orderEntity;

    /**
     * 错误状态码： 0----成功
     * 1 库存不足
     * 2 验证失败
     */
    private Integer code;
}
