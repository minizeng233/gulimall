package com.junting.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-05 21:45
 */

@Data
public class AttrRespVo extends AttrVo{
    //所属的分类的名字
    private String catelogName;
    //所属的分组名字
    private String groupName;
    //全路径
    private Long[] catelogPath;
}
