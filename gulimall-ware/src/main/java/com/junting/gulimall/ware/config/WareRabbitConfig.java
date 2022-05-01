package com.junting.gulimall.ware.config;

import com.junting.common.constant.RabbitInfo;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mini_zeng
 * @create 2022-01-18 19:23
 */
@Configuration
public class WareRabbitConfig {


    /**
     * 消息转换器
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
     */
    @Bean
    public Exchange stockEventExchange(){

        return new TopicExchange(RabbitInfo.Stock.exchange,
                true, false);
    }

    /**
     * String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
     */
    @Bean
    public Queue stockReleaseQueue(){
        return new Queue(RabbitInfo.Stock.releaseQueue, true,
                false, false);
    }

    @Bean
    public Queue stockDelayQueue(){

        Map<String, Object> args = new HashMap<>();
        // 信死了 交给 [stock-event-exchange] 交换机
        args.put("x-dead-letter-exchange",RabbitInfo.Stock.exchange);
        // 死信路由
        args.put("x-dead-letter-routing-key",RabbitInfo.Stock.releaseRoutingKey);
        args.put("x-message-ttl", RabbitInfo.Stock.ttl);

        return new Queue(RabbitInfo.Stock.delayQueue, true,
                false, false, args);
    }

    /**
     * 延时队列的绑定关系
     * String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments
     */
    @Bean
    public Binding stockLockedBinding(){

        return new Binding(RabbitInfo.Stock.delayQueue, Binding.DestinationType.QUEUE,
                RabbitInfo.Stock.exchange, RabbitInfo.Stock.delayRoutingKey, null);
    }

    /**
     * 普通队列的绑定关系
     * String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments
     */
    @Bean
    public Binding stockLockedReleaseBinding(){

        return new Binding(RabbitInfo.Stock.releaseQueue, Binding.DestinationType.QUEUE,
                RabbitInfo.Stock.exchange,RabbitInfo.Stock.baseRoutingKey
                , null);
    }

}
