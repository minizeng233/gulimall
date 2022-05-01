package com.junting.gulimall.order.to;

import com.junting.gulimall.order.entity.OrderEntity;
import com.junting.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-17 19:44
 */

@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    /**
     * 订单计算的应付价格
     */
    private BigDecimal payPrice;

    /**
     * 运费
     */
    private BigDecimal fare;
}
