package com.junting.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.product.entity.BrandEntity;
import com.junting.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:31
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveCatetory(CategoryBrandRelationEntity categoryBrandRelation);

    void updateByBrandName(Long brandId, String brandName);

    void updateByCategoryName(Long categoryCatId, String categoryName);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

