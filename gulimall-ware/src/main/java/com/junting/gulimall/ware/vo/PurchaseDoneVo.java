package com.junting.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-07 9:07
 */

@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id ;   //采购单id

    private List<PurchaseItemDoneVo> items;
}
