package com.junting.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.junting.common.utils.R;
import com.junting.gulimall.ware.feign.MemberFeignService;
import com.junting.gulimall.ware.vo.FareVo;
import com.junting.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.ware.dao.WareInfoDao;
import com.junting.gulimall.ware.entity.WareInfoEntity;
import com.junting.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    MemberFeignService memberFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and(w -> {
                w.eq("id",key).or().like("name",key)
                .or().like("address",key).or().like("areacode",key);
            });
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),wrapper);
        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        R info = memberFeignService.addrInfo(addrId);
        FareVo fareVo = new FareVo();
        // 获取用户地址
        MemberAddressVo addressVo = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        fareVo.setMemberAddressVo(addressVo);
        if (addressVo != null) {
            // 假设电话后2位为运费
            String phone = addressVo.getPhone();
            if (phone == null || phone.length() < 2) {
                phone = new Random().nextInt(100) + "";
            }
            BigDecimal decimal = new BigDecimal(phone.substring(phone.length() - 1));
            fareVo.setFare(decimal);
        } else {
            fareVo.setFare(new BigDecimal("20"));
        }
        return fareVo;
    }

}