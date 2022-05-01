package com.junting.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.junting.common.constant.RabbitInfo;
import com.junting.common.enume.OrderStatusEnum;
import com.junting.common.enume.SubmitOrderStatusEnum;
import com.junting.common.exception.NotStockException;
import com.junting.common.to.mq.OrderTo;
import com.junting.common.to.mq.SecKillOrderTo;
import com.junting.common.utils.R;
import com.junting.common.vo.MemberRespVo;
import com.junting.gulimall.order.constant.OrderConstant;
import com.junting.gulimall.order.entity.OrderItemEntity;
import com.junting.gulimall.order.entity.PaymentInfoEntity;
import com.junting.gulimall.order.feign.CartFeignService;
import com.junting.gulimall.order.feign.MemberFeignService;
import com.junting.gulimall.order.feign.ProductFeignService;
import com.junting.gulimall.order.feign.WmsFeignService;
import com.junting.gulimall.order.interceptor.LoginUserInterceptor;
import com.junting.gulimall.order.service.OrderItemService;
import com.junting.gulimall.order.service.PaymentInfoService;
import com.junting.gulimall.order.to.OrderCreateTo;
import com.junting.gulimall.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.order.dao.OrderDao;
import com.junting.gulimall.order.entity.OrderEntity;
import com.junting.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();
    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private WmsFeignService wmsFeignService;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo vo) {
        // 1.保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        //		TRADE_SUCCESS
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());
        infoEntity.setSubject(vo.getSubject());
        infoEntity.setTotalAmount(new BigDecimal(vo.getTotal_amount()));
        infoEntity.setCreateTime(vo.getGmt_create());
        paymentInfoService.save(infoEntity);
        // 2.修改订单状态信息
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            // 支付成功
            String orderSn = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    //提交订单
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        //准备封装的OrderConfirmVo对象
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //获取当前用户
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        //获取上下文请求信息(根据线程获取，不同线程不一样)
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //1.开启线程封装address信息
            //获取上下文信息，异步有丢上下文信息问题，远程有丢请求头问题（cookie）
            RequestContextHolder.setRequestAttributes(attributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //2.开启线程封装orderitem信息
            // feign在远程调用之前要构造请求(丢失请求头，无cookie) 调用很多拦截器
            //获取上下文信息，异步有丢上下文信息问题，远程有丢请求头问题（cookie）
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> orderItemVos = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(orderItemVos);
        }, executor).thenRunAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> items = confirmVo.getItems();
            // 获取所有商品的id
            List<Long> skus = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkuHasStock(skus);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                // 各个商品id 与 他们库存状态的映射map // 学习下收集成map的用法
                Map<Long, Boolean> stocks = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(stocks);
            }
        }, executor);

        // 3.查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);
        // 4.其他数据在类内部自动计算   (价格等)

        // TODO 5.防重令牌 设置用户的令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        //放入redis
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token);
        //传向前端
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

//    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        //将提交的数据放入上下文，让其它方法可以获取
        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo submitVo = new SubmitOrderResponseVo();
        // 0：正常
        submitVo.setCode(0);
        // 去服务器创建订单,验令牌,验价格,锁库存
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        // 1. 验证令牌 [必须保证原子性] 返回 0 or 1
        // 0 令牌删除失败 1删除成功   script 脚本
        String script = "if redis.call('get',KEYS[1]) == ARGV[1]" +
                "then return redis.call('del',KEYS[1]) " +
                "else return 0 " +
                "end";
        String orderToken = vo.getOrderToken(); //页面token
        // 原子验证令牌 删除令牌
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                orderToken);
        if (result == 0L) { // 令牌验证失败
            submitVo.setCode(SubmitOrderStatusEnum.TOKENERROR.getCode());
        } else {  // 令牌验证成功
            // 1 .创建订单等信息 // 有生成订单号、计算总价
            OrderCreateTo order = createOrder();
            // 2. 验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal voPayPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(voPayPrice).doubleValue()) < 0.01) {
                // 金额对比成功
                // 3.保存订单 挪到后面
                //TODO 3.本地服务调用保存订单
                saveOrder(order);
                // 远程锁库存
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    // 锁定的skuId 这个skuId要锁定的数量
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                //TODO 4.库存锁定,远程调用
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    // 库存足够 锁定成功 给MQ发送订单消息，到时为支付则取消
                    submitVo.setOrderEntity(order.getOrder());
                    OrderEntity order1 = order.getOrder();

                    rabbitTemplate.convertAndSend(RabbitInfo.Order.exchange,RabbitInfo.Order.delayRoutingKey,order.getOrder());

                    //TODO 5.远程设置积分信息
//                    int i = 10/0;
                } else {
                    // 锁定失败
                    String msg = (String) r.get("msg");
                    throw new NotStockException(msg);
                }
            }else{
                // 价格验证失败
                submitVo.setCode(SubmitOrderStatusEnum.CHECKPRICE.getCode());
                log.warn("验价失败");
            }
        }
        return submitVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        log.info("\n收到过期的订单信息--准关闭订单:" + entity.getOrderSn());
        // 因为消息发送过来的订单已经是很久前的了，中间可能被改动，因此要查询最新的订单
        OrderEntity orderEntity = this.getById(entity.getId());
        //如果订单还处于新创建的状态，说明超时未支付，进行关单
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            // 发送给MQ告诉它有一个订单被自动关闭了
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            try {
                // 保证消息 100% 发出去 每一个消息在数据库保存详细信息
                // 定期扫描数据库 将失败的消息在发送一遍
                rabbitTemplate.convertAndSend(RabbitInfo.Order.exchange,
                        RabbitInfo.Order.baseRoutingKey, orderTo);
            } catch (AmqpException e) {
                // 将没发送成功的消息进行重试发送.
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);
        // 保留2位小数位向上补齐
        payVo.setTotal_amount(order.getTotalAmount().add(order.getFreightAmount() == null ? new BigDecimal("0") : order.getFreightAmount()).setScale(2, BigDecimal.ROUND_UP).toString());
        payVo.setOut_trade_no(order.getOrderSn());
        List<OrderItemEntity> entities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
        payVo.setSubject("gullmall");
        payVo.setBody("gullmall");
        if (null != entities.get(0).getSkuName() && entities.get(0).getSkuName().length() > 1) {
//			payVo.setSubject(entities.get(0).getSkuName());
//			payVo.setBody(entities.get(0).getSkuName());
            payVo.setSubject("glmall");
            payVo.setBody("glmall");
        }
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberRespVo respVo = LoginUserInterceptor.threadLocal.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),

                // 查询这个用户的最新订单 [降序排序]
                new QueryWrapper<OrderEntity>().eq("member_id", respVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> order_sn = page.getRecords().stream().map(order -> {
            // 查询这个订单关联的所有订单项
            List<OrderItemEntity> orderSn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(orderSn);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(order_sn);
        return new PageUtils(page);
    }

    @Override
    public void createSecKillOrder(SecKillOrderTo secKillOrderTo) {
        log.info("\n创建秒杀订单");
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(secKillOrderTo.getOrderSn());
        entity.setMemberId(secKillOrderTo.getMemberId());
        entity.setCreateTime(new Date());
        entity.setPayAmount(secKillOrderTo.getSeckillPrice());
        entity.setTotalAmount(secKillOrderTo.getSeckillPrice());
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setPayType(1);
        // TODO 还有挺多的没设置
        BigDecimal price = secKillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + secKillOrderTo.getNum()));
        entity.setPayAmount(price);

        this.save(entity);

        // 保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(secKillOrderTo.getOrderSn());
        itemEntity.setRealAmount(price);
        itemEntity.setOrderId(entity.getId());
        itemEntity.setSkuQuantity(secKillOrderTo.getNum());
        R info = productFeignService.getSpuInfoBySkuId(secKillOrderTo.getSkuId());
        SpuInfoVo spuInfo = info.getData(new TypeReference<SpuInfoVo>() {});
        itemEntity.setSpuId(spuInfo.getId());
        itemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        itemEntity.setSpuName(spuInfo.getSpuName());
        itemEntity.setCategoryId(spuInfo.getCatalogId());
        itemEntity.setGiftGrowth(secKillOrderTo.getSeckillPrice().multiply(new BigDecimal(secKillOrderTo.getNum())).intValue());
        itemEntity.setGiftIntegration(secKillOrderTo.getSeckillPrice().multiply(new BigDecimal(secKillOrderTo.getNum())).intValue());
        itemEntity.setPromotionAmount(new BigDecimal("0.0"));
        itemEntity.setCouponAmount(new BigDecimal("0.0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0.0"));
        orderItemService.save(itemEntity);
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItems = orderItems.stream().map(item -> {
            item.setOrderId(orderEntity.getId());
            item.setSpuName(item.getSpuName());
            item.setOrderSn(order.getOrder().getOrderSn());
//            orderItemService.save(item);
            return item;
        }).collect(Collectors.toList());
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder () {
            //封装orderCreateTo信息
            OrderCreateTo orderCreateTo = new OrderCreateTo();
            // 1. 生成一个订单号
            String orderSn = IdWorker.getTimeId();
            // 填充订单的各种基本信息，价格信息
            OrderEntity orderEntity = buildOrderSn(orderSn);
            // 2. 获取所有订单项   // 从里面已经设置好了用户该使用的价格
            List<OrderItemEntity> items = buildOrderItems(orderSn);

            // 3.根据订单项计算价格	传入订单 、订单项 计算价格、积分、成长值等相关信息
            computerPrice(orderEntity, items);
            orderCreateTo.setOrder(orderEntity);
            orderCreateTo.setOrderItems(items);
            return orderCreateTo;
        }
        /**
         * 验价  传入订单（包含了优惠券等，最终价格也要放到他里面）和订单项
         */
        private void computerPrice (OrderEntity orderEntity, List < OrderItemEntity > items){
            // 叠加每一个订单项的金额
            BigDecimal coupon = new BigDecimal("0.0");
            BigDecimal integration = new BigDecimal("0.0");
            BigDecimal promotion = new BigDecimal("0.0");
            BigDecimal gift = new BigDecimal("0.0");
            BigDecimal growth = new BigDecimal("0.0");

            // 总价
            BigDecimal totalPrice = new BigDecimal("0.0");
            for (OrderItemEntity item : items) {  // 这段逻辑不是特别合理，最重要的是累积总价，别的可以跳过
                // 优惠券的金额
                coupon = coupon.add(item.getCouponAmount());
                // 积分优惠的金额
                integration = integration.add(item.getIntegrationAmount());
                // 打折的金额
                promotion = promotion.add(item.getPromotionAmount());
                BigDecimal realAmount = item.getRealAmount();
                totalPrice = totalPrice.add(realAmount);

                // 购物获取的积分、成长值
                gift.add(new BigDecimal(item.getGiftIntegration().toString()));
                growth.add(new BigDecimal(item.getGiftGrowth().toString()));
            }

            // 1.订单价格相关 总额、应付总额
            orderEntity.setTotalAmount(totalPrice);
            orderEntity.setPayAmount(totalPrice.add(orderEntity.getFreightAmount()));

            orderEntity.setPromotionAmount(promotion);
            orderEntity.setIntegrationAmount(integration);
            orderEntity.setCouponAmount(coupon);

            // 设置积分、成长值
            orderEntity.setIntegration(gift.intValue());
            orderEntity.setGrowth(growth.intValue());

            // 设置订单的删除状态
            orderEntity.setDeleteStatus(OrderStatusEnum.CREATE_NEW.getCode());

        }

        /**
         * 为 orderSn 订单构建订单数据
         */
        private List<OrderItemEntity> buildOrderItems (String orderSn){
            //这里是最后一次来确认购物项的价格 这个远程方法还会查询一次数据库
            List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
            List<OrderItemEntity> itemEntities = null;
            if (cartItems != null && cartItems.size() > 0) {
                itemEntities = cartItems.stream().map(cartItem -> {
                    OrderItemEntity itemEntity = buildOrderItem(cartItem);
                    itemEntity.setOrderSn(orderSn);
                    return itemEntity;
                }).collect(Collectors.toList());
            }
            return itemEntities;
        }
        /**
         * 构建某一个订单项
         */ // OrderServiceImpl
        private OrderItemEntity buildOrderItem (OrderItemVo cartItem){
            OrderItemEntity itemEntity = new OrderItemEntity();
            // 1.订单信息： 订单号
            // 已经在items里设置了
            // 2.商品spu信息
            Long skuId = cartItem.getSkuId();
            // 远程获取spu的信息
            R r = productFeignService.getSpuInfoBySkuId(skuId);
            SpuInfoVo spuInfo = r.getData(new TypeReference<SpuInfoVo>() {
            });
            itemEntity.setSpuId(spuInfo.getId());
            itemEntity.setSpuBrand(spuInfo.getBrandId().toString());
            itemEntity.setSpuName(spuInfo.getSpuName());
            itemEntity.setCategoryId(spuInfo.getCatalogId());
            // 3.商品的sku信息
            itemEntity.setSkuId(cartItem.getSkuId());
            itemEntity.setSkuName(cartItem.getTitle());
            itemEntity.setSkuPic(cartItem.getImage());
            itemEntity.setSkuPrice(cartItem.getPrice());
            // 把一个集合按照指定的字符串进行分割得到一个字符串
            // 属性list生成一个string
            String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
            itemEntity.setSkuAttrsVals(skuAttr);
            itemEntity.setSkuQuantity(cartItem.getCount());
            // 4.积分信息 买的数量越多积分越多 成长值越多
            itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
            itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
            // 5.订单项的价格信息 优惠金额
            itemEntity.setPromotionAmount(new BigDecimal("0.0")); // 促销打折
            itemEntity.setCouponAmount(new BigDecimal("0.0")); // 优惠券
            itemEntity.setIntegrationAmount(new BigDecimal("0.0")); // 积分
            // 当前订单项的原价
            BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
            // 减去各种优惠的价格
            BigDecimal subtract =
                    orign.subtract(itemEntity.getCouponAmount()) // 优惠券逻辑没有写，应该去coupon服务查用户的sku优惠券
                            .subtract(itemEntity.getPromotionAmount()) // 官方促销
                            .subtract(itemEntity.getIntegrationAmount()); // 京豆/积分
            itemEntity.setRealAmount(subtract);
            return itemEntity;
        }

        private OrderEntity buildOrderSn (String orderSn){
            OrderEntity entity = new OrderEntity();
            entity.setOrderSn(orderSn);
            entity.setCreateTime(new Date());
            entity.setCommentTime(new Date());
            entity.setReceiveTime(new Date());
            entity.setDeliveryTime(new Date());
            MemberRespVo respVo = LoginUserInterceptor.threadLocal.get();
            entity.setMemberId(respVo.getId());
            entity.setMemberUsername(respVo.getUsername());
            entity.setBillReceiverEmail(respVo.getEmail());
            // 2. 获取收获地址信息
            OrderSubmitVo submitVo = confirmVoThreadLocal.get();
            R fare = wmsFeignService.getFare(submitVo.getAddrId());//邮费
            FareVo resp = fare.getData(new TypeReference<FareVo>() {
            });
            entity.setFreightAmount(resp.getFare());
            entity.setReceiverCity(resp.getMemberAddressVo().getCity());
            entity.setReceiverDetailAddress(resp.getMemberAddressVo().getDetailAddress());
            entity.setDeleteStatus(OrderStatusEnum.CREATE_NEW.getCode());
            entity.setReceiverPhone(resp.getMemberAddressVo().getPhone());
            entity.setReceiverName(resp.getMemberAddressVo().getName());
            entity.setReceiverPostCode(resp.getMemberAddressVo().getPostCode());
            entity.setReceiverProvince(resp.getMemberAddressVo().getProvince());
            entity.setReceiverRegion(resp.getMemberAddressVo().getRegion());
            // 设置订单为新建状态
            entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
            entity.setAutoConfirmDay(7);//自动确认收货

            return entity;
        }

    }