package com.junting.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.product.entity.SpuInfoEntity;
import com.junting.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:32
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void spuInfoEntity(SpuSaveVo vo);

    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
        *@Description   商品上架功能
        *@author mini_zeng
        *@Date 2022/1/8
        *@Param spuId
     **/
    void spuUp(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

