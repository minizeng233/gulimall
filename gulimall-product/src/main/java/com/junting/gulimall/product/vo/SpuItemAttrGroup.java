package com.junting.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-12 22:48
 */
@ToString
@Data
public class SpuItemAttrGroup {
    private String groupName;

    /** 两个属性attrName、attrValue */
    private List<SpuBaseAttrVo> attrs;
}
