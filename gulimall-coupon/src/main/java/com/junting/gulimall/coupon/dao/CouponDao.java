package com.junting.gulimall.coupon.dao;

import com.junting.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-06 16:22:29
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
