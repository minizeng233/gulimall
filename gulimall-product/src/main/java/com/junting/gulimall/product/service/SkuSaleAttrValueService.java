package com.junting.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.junting.gulimall.product.vo.ItemSaleAttrVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:32
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<ItemSaleAttrVo> getSaleAttrsBuSpuId(Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(Long skuId);

}

