package com.junting.common.to.mq;

import lombok.Data;

/**
 * @author mini_zeng
 * @create 2022-01-18 21:22
 */
@Data
public class StockLockedTo {

    /**
     * 库存工作单id
     */
    private Long id;

    /**
     * 工作详情id
     */
    private StockDetailTo detailTo;
}
