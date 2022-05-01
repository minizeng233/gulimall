package com.junting.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.junting.common.valid.AddGroup;
import com.junting.common.valid.UpdateGroup;
import com.junting.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.junting.gulimall.product.entity.BrandEntity;
import com.junting.gulimall.product.service.BrandService;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.R;



/**
 * 品牌
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-03 13:56:31
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    ////@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){

        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    ////@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    ////@RequiresPermissions("product:brand:save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand/*, BindingResult result*/){
//        if(result.hasErrors()){
//            Map<String,String> map = new HashMap<>();
//            //1、获取校验的错误结果
//            result.getFieldErrors().forEach((item)->{
//                //FieldError 获取到错误提示
//                String message = item.getDefaultMessage();
//                //获取错误的属性的名字
//                String field = item.getField();
//                map.put(field,message);
//            });
//            return R.error(400,"提交的数据不合法").put("data",map);
//        }else {
            brandService.save(brand);
            return R.ok();
//        }
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    ////@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){

//		brandService.updateById(brand);

        brandService.updateRelation(brand);
        return R.ok();
    }

    /**
     * 修改显示状态
     */
    @RequestMapping("/update/showstatus")
    ////@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }


    /**
     * 删除
     */
    @RequestMapping("/delete")
    ////@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
