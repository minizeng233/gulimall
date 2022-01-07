package com.junting.gulimall.product.service.impl;

import com.junting.gulimall.product.dao.CategoryBrandRelationDao;
import com.junting.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.product.dao.BrandDao;
import com.junting.gulimall.product.entity.BrandEntity;
import com.junting.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.eq("brand_id",key).or().like("name",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),wrapper);

        return new PageUtils(page);
    }

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Transactional
    @Override
    public void updateRelation(BrandEntity brand) {

        this.updateById(brand);
        String brandName = brand.getName();
        Long brandId = brand.getBrandId();
        if (!StringUtils.isEmpty(brandName)){
            categoryBrandRelationService.updateByBrandName(brandId,brandName);
        }
    }

}