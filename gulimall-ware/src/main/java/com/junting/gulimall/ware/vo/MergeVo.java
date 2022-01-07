package com.junting.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-06 22:53
 */

@Data
public class MergeVo {
    private List<Long> items;  //items: [1, 2]
    private Long purchaseId;  //purchaseId: 1
}
