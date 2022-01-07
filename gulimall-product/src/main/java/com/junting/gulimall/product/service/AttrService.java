package com.junting.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.product.entity.AttrEntity;
import com.junting.gulimall.product.entity.ProductAttrValueEntity;
import com.junting.gulimall.product.vo.AttrGroupRelationVo;
import com.junting.gulimall.product.vo.AttrRespVo;
import com.junting.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:31
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAndRelation(AttrVo attr);

    PageUtils queryByIds(Map<String, Object> params, Long catelogId,String type);

    AttrRespVo getByCategory(Long attrId);

    void updateAttr(AttrRespVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    List<ProductAttrValueEntity> listByspuId(Long spuId);
}

