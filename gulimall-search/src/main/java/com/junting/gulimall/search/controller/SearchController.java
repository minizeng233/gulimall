package com.junting.gulimall.search.controller;

import com.junting.gulimall.search.service.MallSearchService;
import com.junting.gulimall.search.vo.SearchParam;
import com.junting.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mini_zeng
 * @create 2022-01-11 9:56
 */

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;
    //检索服务
    @GetMapping("/list.html")
    public String  listPage(SearchParam param, Model model, HttpServletRequest request){
        String queryString = request.getQueryString();
        param.set_queryString(queryString);
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }


}
