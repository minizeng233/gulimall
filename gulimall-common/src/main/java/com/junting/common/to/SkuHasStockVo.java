package com.junting.common.to;

import lombok.Data;

/**
 * @author mini_zeng
 * @create 2022-01-08 16:48
 */

@Data
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}
