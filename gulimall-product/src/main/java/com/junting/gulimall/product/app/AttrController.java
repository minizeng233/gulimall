package com.junting.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.junting.gulimall.product.entity.ProductAttrValueEntity;
import com.junting.gulimall.product.service.ProductAttrValueService;
import com.junting.gulimall.product.vo.AttrRespVo;
import com.junting.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.junting.gulimall.product.service.AttrService;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.R;



/**
 * 商品属性
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:31
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 修改
     */
    @PostMapping("/update/{spuId}")
    ////@RequiresPermissions("product:attr:update")
    public R updateByspuId(@PathVariable("spuId") long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){
//        attrService.updateAttr(attr);
        productAttrValueService.updateByspuId(spuId,entities);
        return R.ok();
    }

    /**
     * 列表
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R listByspuId(@PathVariable("spuId") Long spuId){
//        PageUtils page = attrService.queryPage(params);
        List<ProductAttrValueEntity> data = attrService.listByspuId(spuId);
        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @GetMapping("/{type}/list/{catelogId}")
    ////@RequiresPermissions("product:attr:list")
    public R listByIds(@RequestParam Map<String, Object> params,
                       @PathVariable("catelogId") Long catelogId,
                        @PathVariable("type") String type){
//        PageUtils page = attrService.queryPage(params);

        PageUtils page = attrService.queryByIds(params,catelogId,type);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    ////@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    ////@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);
    AttrRespVo attr = attrService.getByCategory(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    ////@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr){

//		attrService.save(attr);
    //保存外再关联关系表
        attrService.saveAndRelation(attr);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    ////@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrRespVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    ////@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
