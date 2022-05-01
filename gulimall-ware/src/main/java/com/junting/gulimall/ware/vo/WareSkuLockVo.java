package com.junting.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-17 22:52
 */

@Data
public class WareSkuLockVo {
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 要锁住的所有库存信息
     */
    private List<OrderItemVo> locks;
}
