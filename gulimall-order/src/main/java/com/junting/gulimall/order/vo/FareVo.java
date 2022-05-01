package com.junting.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author mini_zeng
 * @create 2022-01-17 19:58
 */
@Data
public class FareVo {
    private MemberAddressVo memberAddressVo;

    private BigDecimal fare;
}
