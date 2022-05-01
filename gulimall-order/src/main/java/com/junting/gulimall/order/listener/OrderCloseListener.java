package com.junting.gulimall.order.listener;

import com.junting.common.constant.RabbitInfo;
import com.junting.gulimall.order.entity.OrderEntity;
import com.junting.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author mini_zeng
 * @create 2022-01-19 10:36
 */
@Service
@RabbitListener(queues = RabbitInfo.Order.releaseQueue)
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity entity,
                         Channel channel,
                         Message message) throws IOException {
        try {
            orderService.closeOrder(entity);
            //TODO 手动调用支付宝收单
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
