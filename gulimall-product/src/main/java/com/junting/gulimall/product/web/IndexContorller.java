package com.junting.gulimall.product.web;

import com.junting.gulimall.product.entity.CategoryEntity;
import com.junting.gulimall.product.service.CategoryService;
import com.junting.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Map;

/**
 * @author mini_zeng
 * @create 2022-01-09 14:10
 */

@Controller
public class IndexContorller {
    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","index"})
    public String Index(Model model){
        //首页需返回一级菜单
        List<CategoryEntity> categoryEntities = categoryService.getLevel1();
        model.addAttribute("categorys",categoryEntities);
        return "index";
    }

    @ResponseBody
    @RequestMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }

}
