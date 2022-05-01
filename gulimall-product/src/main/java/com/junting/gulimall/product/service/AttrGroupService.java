package com.junting.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.product.entity.AttrGroupEntity;
import com.junting.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.junting.gulimall.product.vo.SpuItemAttrGroup;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:31
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {
    
    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, long catelogId);

    List<AttrGroupWithAttrsVo> getAttrGroupByCategoryId(Long catelogId);


    List<SpuItemAttrGroup> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

