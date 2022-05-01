package com.junting.gulimall.product.dao;

import com.junting.common.constant.ProductConstant;
import com.junting.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:32
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void upSpuStatus(@Param("spuId") Long spuId,
                     @Param("code") int code);
}
