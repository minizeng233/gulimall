package com.junting.gulimall.coupon.service.impl;

import com.junting.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.junting.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.coupon.dao.SeckillSessionDao;
import com.junting.gulimall.coupon.entity.SeckillSessionEntity;
import com.junting.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    SeckillSkuRelationService skuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLate3DaySession() {
        // 获取最近3天的秒杀活动
        List<SeckillSessionEntity> list = this.list(
                new QueryWrapper<SeckillSessionEntity>()
                        .between("start_time", startTime(), endTime()));
        // 设置秒杀活动里的秒杀商品
        if(list != null && list.size() > 0){
            return list.stream().map(session -> {
                // 给每一个活动写入他们的秒杀项
                Long id = session.getId();
                // 根据活动id获取每个sku项
                List<SeckillSkuRelationEntity> entities =
                        skuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
                session.setRelationSkus(entities);
                return session;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private String endTime() {
        LocalDate localDate = LocalDate.now().plusDays(2);
        LocalDateTime of = LocalDateTime.of(localDate, LocalTime.MAX);
        return of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String startTime() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}