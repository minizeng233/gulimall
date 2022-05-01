package com.junting.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author mini_zeng
 * @create 2022-01-17 16:04
 */
@Data
public class FareVo {

    // 地址
    private MemberAddressVo memberAddressVo;

    // 运费
    private BigDecimal fare;
}
