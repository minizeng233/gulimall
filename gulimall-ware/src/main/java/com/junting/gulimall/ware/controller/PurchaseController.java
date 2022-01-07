package com.junting.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.junting.gulimall.ware.vo.MergeVo;
import com.junting.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.junting.gulimall.ware.entity.PurchaseEntity;
import com.junting.gulimall.ware.service.PurchaseService;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.R;



/**
 * 采购信息
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-06 20:27:27
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 完成采购单
     * @return
     */
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVo doneVo){

        purchaseService.done(doneVo);

        return R.ok();
    }

    /**
     * 领取采购单
     * @return
     */
    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids){

        purchaseService.received(ids);

        return R.ok();
    }

    /**
     * 列表
     */
    @PostMapping("/merge")
    ////@RequiresPermissions("ware:purchase:list")
    public void Merge(@RequestBody MergeVo mergeVo){
        purchaseService.mergePurchase(mergeVo);

    }

    /**
     * 列表
     */
    @RequestMapping("/unreceive/list")
    ////@RequiresPermissions("ware:purchase:list")
    public R listUnreceive(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceive(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    ////@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    ////@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    ////@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    ////@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    ////@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
