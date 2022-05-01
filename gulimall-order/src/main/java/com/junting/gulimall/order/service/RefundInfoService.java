package com.junting.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.order.entity.RefundInfoEntity;

import java.util.Map;

/**
 * 退款信息
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-16 14:09:47
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

