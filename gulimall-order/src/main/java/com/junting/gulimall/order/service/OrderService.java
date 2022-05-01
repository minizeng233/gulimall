package com.junting.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.to.mq.SecKillOrderTo;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.order.entity.OrderEntity;
import com.junting.gulimall.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-16 14:09:47
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 处理支付宝的返回数据
     */
    String handlePayResult(PayAsyncVo vo);

    /**
        *@Description   订单确定页需要返回的数据
        *@author mini_zeng
        *@Date 2022/1/16
        *@Param
        *@return com.junting.gulimall.order.vo.OrderConfirmVo
        **/
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    void createSecKillOrder(SecKillOrderTo secKillOrderTo);
}

