package com.junting.gulimall.seckill.controller;

import com.junting.common.to.SeckillSkuRedisTo;
import com.junting.common.utils.R;
import com.junting.gulimall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-20 21:54
 */
@Controller
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId, // session_skuID
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num, Model model){
        String orderSn = seckillService.kill(killId,key,num);
        // 1.判断是否登录
        model.addAttribute("orderSn", orderSn);
//        return R.ok().setData(orderSn);
        return "success";
    }

}
