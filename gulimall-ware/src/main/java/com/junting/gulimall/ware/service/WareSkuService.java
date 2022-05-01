package com.junting.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.to.mq.OrderTo;
import com.junting.common.to.mq.StockLockedTo;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.ware.entity.WareSkuEntity;
import com.junting.gulimall.ware.vo.SkuHasStockVo;
import com.junting.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-06 20:27:27
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);
    /**
     * 保存库存的时候顺便查到商品价格
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
        *@Description   判断sku是否有库存和数量
        *@author mini_zeng
        *@Date 2022/1/8
        *@Param skuIds
        *@return java.util.List<com.junting.gulimall.ware.vo.SkuHasStockVo>
        **/
    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    /**
     * 由于订单超时而自动释放订单之后来解锁库存
     */
    void unlockStock(StockLockedTo to);

    /**
     * 由于订单关闭后主动发送消息来解锁库存
     */
    void unlockStock(OrderTo to);
}

