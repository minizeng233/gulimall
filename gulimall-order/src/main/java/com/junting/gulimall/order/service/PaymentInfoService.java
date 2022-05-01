package com.junting.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.order.entity.PaymentInfoEntity;

import java.util.Map;

/**
 * 支付信息表
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-16 14:09:47
 */
public interface PaymentInfoService extends IService<PaymentInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

