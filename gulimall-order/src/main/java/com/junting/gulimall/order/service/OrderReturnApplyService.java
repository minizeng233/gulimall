package com.junting.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.order.entity.OrderReturnApplyEntity;

import java.util.Map;

/**
 * 订单退货申请
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-16 14:09:47
 */
public interface OrderReturnApplyService extends IService<OrderReturnApplyEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

