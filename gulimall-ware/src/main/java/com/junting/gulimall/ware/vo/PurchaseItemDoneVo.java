package com.junting.gulimall.ware.vo;

import lombok.Data;

/**
 * @author mini_zeng
 * @create 2022-01-07 9:09
 */

@Data
public class PurchaseItemDoneVo {
    //itemId:3,status:3,reason:""
    private Long itemId;
    private Integer status;
    private String reason;
}
