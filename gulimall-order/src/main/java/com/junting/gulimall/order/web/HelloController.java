package com.junting.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author mini_zeng
 * @create 2022-01-16 14:50
 */
@Controller
public class HelloController {

    @GetMapping("/{page}")
    public String hello(@PathVariable("page") String page){

        return page;
    }
}
