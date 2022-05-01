package com.junting.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mini_zeng
 * @create 2022-01-18 21:22
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StockDetailTo {
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 1-已锁定  2-已解锁  3-扣减
     */
    private Integer lockStatus;
}
