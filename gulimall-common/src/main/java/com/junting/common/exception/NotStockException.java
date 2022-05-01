package com.junting.common.exception;

/**
 * @author mini_zeng
 * @create 2022-01-17 23:22
 */
public class NotStockException extends RuntimeException{

    private Long skuId;

    public NotStockException(String msg) {
        super(msg + "号商品没有足够的库存了");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
