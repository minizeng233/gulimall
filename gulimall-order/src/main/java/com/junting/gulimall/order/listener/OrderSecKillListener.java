package com.junting.gulimall.order.listener;

import com.junting.common.constant.RabbitInfo;
import com.junting.common.to.mq.SecKillOrderTo;
import com.junting.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author mini_zeng
 * @create 2022-01-21 12:36
 */
@RabbitListener(queues = RabbitInfo.SecKill.delayQueue)
@Component
public class OrderSecKillListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SecKillOrderTo secKillOrderTo, Channel channel, Message message) throws IOException {
        try {
            // 秒杀的时候没有订单，这时候才创建订单
            orderService.createSecKillOrder(secKillOrderTo);
            // 手动确认消费
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
