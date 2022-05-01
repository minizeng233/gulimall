package com.junting.gulimall.product.web;

import com.junting.gulimall.product.service.SkuInfoService;
import com.junting.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;

/**
 * @author mini_zeng
 * @create 2022-01-12 21:42
 */

@Controller
public class itemController {
    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {

        SkuItemVo vo = skuInfoService.item(skuId);

        model.addAttribute("item", vo);
        return "item";
    }
}
