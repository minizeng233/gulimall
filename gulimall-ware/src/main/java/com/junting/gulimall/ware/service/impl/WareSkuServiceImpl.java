package com.junting.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.junting.common.constant.RabbitInfo;
import com.junting.common.enume.OrderStatusEnum;
import com.junting.common.exception.NotStockException;
import com.junting.common.to.mq.OrderTo;
import com.junting.common.to.mq.StockDetailTo;
import com.junting.common.to.mq.StockLockedTo;
import com.junting.common.utils.R;
import com.junting.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.junting.gulimall.ware.entity.WareOrderTaskEntity;
import com.junting.gulimall.ware.feign.ProductFeignService;
import com.junting.gulimall.ware.feign.orderFeignService;
import com.junting.gulimall.ware.service.WareOrderTaskDetailService;
import com.junting.gulimall.ware.service.WareOrderTaskService;
import com.junting.gulimall.ware.vo.OrderItemVo;
import com.junting.gulimall.ware.vo.OrderVo;
import com.junting.gulimall.ware.vo.SkuHasStockVo;
import com.junting.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.ware.dao.WareSkuDao;
import com.junting.gulimall.ware.entity.WareSkuEntity;
import com.junting.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    private WareOrderTaskService orderTaskService;
    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private orderFeignService orderFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities == null || entities.size() == 0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");

                if(info.getCode() == 0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }
            wareSkuDao.insert(skuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> skuHasStockVos = skuIds.stream().map(item -> {
            Long count = this.baseMapper.getSkuStock(item);
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(item);
            skuHasStockVo.setHasStock(count == null?false:count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return skuHasStockVos;
    }

    // 锁库存  运行时异常会默认回滚
    @Transactional(rollbackFor = NotStockException.class) // 自定义异常
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        List<OrderItemVo> locks = vo.getLocks();//订单项
        List<SkuWareHasStock> lockVOs = locks.stream().map(item -> {
            // 创建订单项
            SkuWareHasStock hasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            hasStock.setSkuId(skuId);
            // 查询本商品在哪有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            hasStock.setWareId(wareIds);
            hasStock.setNum(item.getCount());   //购买数量
            return hasStock;
        }).collect(Collectors.toList());

        for (SkuWareHasStock hasStock : lockVOs) {
            Boolean skuStocked = true;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库有这个库存（注意可能会回滚之前的订单项，没关系）
                throw new NotStockException(skuId.toString());
            }

            // 锁库存之前先保存订单 以便后来消息撤回
            WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
            taskEntity.setOrderSn(vo.getOrderSn());
            orderTaskService.save(taskEntity);

            // 1 如果每一个商品都锁定成功 将当前商品锁定了几件的工作单记录发送给MQ
            // 2 如果锁定失败 前面保存的工作单信息回滚了(发送了消息却回滚库存的情况，没关系，用数据库id查就可以)
            for (Long wareId : wareIds) {
                // 成功就返回 1 失败返回0  （有上下限）
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());//要锁定num个
                // UPDATE返回0代表失败
                if (count == 0) { // 没有更新对，说明锁当前库库存失败，去尝试其他库
                    skuStocked = false;
                } else { // 即1
                    // TODO 告诉MQ库存锁定成功 一个订单锁定成功 消息队列就会有一个消息

                    // 订单项详情
                    WareOrderTaskDetailEntity detailEntity =
                            new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(),
                                    wareId, // 锁定的仓库号
                                    1);
                    // db保存订单sku项工作单详情，告诉商品锁的哪个库存
                    orderTaskDetailService.save(detailEntity);
                    // 发送库存锁定消息到延迟队列
                    // 要发送的内容
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo detailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity, detailTo);
                    // 如果只发详情id，那么如果出现异常数据库回滚了 // 这个地方需要斟酌，都在事务里了，其实没必要
                    stockLockedTo.setDetailTo(detailTo);
                    rabbitTemplate.convertAndSend(RabbitInfo.Stock.exchange,
                            RabbitInfo.Stock.delayRoutingKey, stockLockedTo);
                    // 订单项详情
                    skuStocked = true;
                    break;// 一定要跳出，防止重复发送多余消息
                }
            }
            if (!skuStocked) {
                // 当前商品在所有仓库都没锁柱
                throw new NotStockException(skuId.toString());
            }
        }

        return true;
    }

    /**
     * 流程图：![](https://i0.hdslb.com/bfs/album/cf307afd8fc216266719f5f6512d62379c183335.png)
     * 解锁库存
     * 	查询数据库关系这个订单的详情
     * 		有: 证明库存锁定成功
     * 			1.没有这个订单, 必须解锁
     * 			2.有这个订单 不是解锁库存
     * 				订单状态：已取消,解锁库存
     * 				没取消：不能解锁	;
     * 		没有：就是库存锁定失败， 库存回滚了 这种情况无需回滚
     */
    @Override
    public void unlockStock(StockLockedTo to) {
        log.info("\n收到解锁库存的消息");
        // 库存id
        Long id = to.getId();
        StockDetailTo detailTo = to.getDetailTo();
        Long detailId = detailTo.getId();

        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            // 解锁
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            // 根据订单号 查询订单状态 已取消才解锁库存
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            /**   */
            if (orderStatus.getCode() == 0) {
                // 订单数据返回成功
                OrderVo orderVo = orderStatus.getData(new TypeReference<OrderVo>() {
                });
                // 订单不存在或订单已取消
                if (orderVo == null || orderVo.getStatus() == OrderStatusEnum.CANCLED.getCode()) {
                    // 订单已取消 状态1 已锁定  这样才可以解锁
                    if (byId.getLockStatus() == 1) {
                        unLock(detailTo.getSkuId(), detailTo.getWareId(), detailTo.getSkuNum(), detailId);
                    }
                }
            } else {
                // 消息拒绝 重新放回队列 让别人继续消费解锁
                throw new RuntimeException("远程服务失败");
            }
        } else {
            // 无需解锁
        }
    }

    /**
     * 防止订单服务卡顿 导致订单状态一直改不了 库存消息有限到期 最后导致卡顿的订单 永远无法解锁库存
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo to) {
        log.info("\n订单超时自动关闭,准备解锁库存");
        String orderSn = to.getOrderSn();
        // 查一下最新的库存状态 防止重复解锁库存[Order服务可能会提前解锁]
        WareOrderTaskEntity taskEntity = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskEntityId = taskEntity.getId();
        // 按照工作单找到所有 没有解锁的库存 进行解锁 状态为1等于已锁定
        List<WareOrderTaskDetailEntity> entities = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskEntityId).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unLock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }


    private void unLock(Long skuId, Long wareId, Integer num, Long taskDeailId) {
        // 更新库存
        wareSkuDao.unlockStock(skuId, wareId, num);
        // 更新库存工作单的状态
        WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
        detailEntity.setId(taskDeailId);
        detailEntity.setLockStatus(2);
        orderTaskDetailService.updateById(detailEntity);
    }

    @Data
    class SkuWareHasStock {

        private Long skuId;

        private List<Long> wareId;

        private Integer num;
    }

}