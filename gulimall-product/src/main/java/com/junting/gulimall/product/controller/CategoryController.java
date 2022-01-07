package com.junting.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.junting.gulimall.product.entity.CategoryEntity;
import com.junting.gulimall.product.service.CategoryService;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.R;



/**
 * 商品三级分类
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:31
 */


@RestController
@RequestMapping("product/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @RequestMapping("/list/tree")
    ////@RequiresPermissions("product:category:list")
    public R list(@RequestParam Map<String, Object> params){

        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("data", entities);
    }



    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    ////@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    ////@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    ////@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		//categoryService.updateById(category);
        categoryService.updateByReation(category);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update/sort")
    ////@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    ////@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
        //categoryService.removeByIds(Arrays.asList(catIds));
        categoryService.removeMenuByIds(Arrays.asList(catIds));

        return R.ok();
    }

}
