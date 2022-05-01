package com.junting.gulimall.product.service.impl;

import com.junting.gulimall.product.vo.ItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.product.dao.SkuSaleAttrValueDao;
import com.junting.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.junting.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<ItemSaleAttrVo> getSaleAttrsBuSpuId(Long spuId) {
        SkuSaleAttrValueDao dao = this.baseMapper;
        return dao.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {
        SkuSaleAttrValueDao dao = this.baseMapper;
        return dao.getSkuSaleAttrValuesAsStringList(skuId);
    }

}