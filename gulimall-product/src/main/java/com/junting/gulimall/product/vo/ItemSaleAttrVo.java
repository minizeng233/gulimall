package com.junting.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-12 22:45
 */
@ToString
@Data
public class ItemSaleAttrVo {
    //销售属性的name
    private String attrName;
    //销售属性的values
    /** AttrValueWithSkuIdVo两个属性 attrValue、skuIds */
    private List<AttrValueWithSkuIdVo> attrValues;

    private Long attrId;
}
