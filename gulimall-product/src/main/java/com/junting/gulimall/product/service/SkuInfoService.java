package com.junting.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.product.entity.SkuInfoEntity;
import com.junting.gulimall.product.vo.SkuInfoVo;
import com.junting.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:32
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
        *@Description   通过spuId查询所有需要上架的sku的信息
        *@author mini_zeng
        *@Date 2022/1/8
        *@Param spuId
        *@return java.util.List<com.junting.gulimall.product.entity.SkuInfoEntity>
        **/
    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;

}

