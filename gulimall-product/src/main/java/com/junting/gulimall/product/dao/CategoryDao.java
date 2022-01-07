package com.junting.gulimall.product.dao;

import com.junting.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:31
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
