package com.junting.gulimall.cart.controller;

import com.junting.gulimall.cart.vo.Cart;
import com.junting.gulimall.cart.service.CartService;
import com.junting.gulimall.cart.vo.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author mini_zeng
 * @create 2022-01-15 11:04
 */
@Slf4j
@Controller
public class CartController {
    @Autowired
    CartService cartService;

    private final String RedirectPATH = "redirect:http://cart.gulimall.com/cart.html";

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    List<CartItem> getCurrentUserCartItems(){
        return cartService.getCurrentUserCartItems();
    }

    @GetMapping("checkItem.html")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check){
        cartService.checkItem(skuId, check);
        return RedirectPATH;
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num){
        cartService.changeItemCount(skuId, num);
        return RedirectPATH;
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);
        return RedirectPATH;
    }

    /**
     * 浏览器有一个cookie：user-key 标识用户身份 一个月后过期
     * 每次访问都会带上这个 user-key
     * 如果没有临时用户 还要帮忙创建一个
     */
    @GetMapping({"/","/cart.html"})
    public String carListPage(Model model) throws ExecutionException, InterruptedException {

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /*** 添加商品到购物车
     *  RedirectAttributes.addFlashAttribute():将数据放在session中，可以在页面中取出，但是只能取一次
     *  RedirectAttributes.addAttribute():将数据拼接在url后面，?skuId=xxx
     * */
    @GetMapping({"/addToCart"})
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) // 重定向数据， 会自动将数据添加到url后面
                            throws ExecutionException, InterruptedException {
        // 添加数量到用户购物车
        CartItem cartItem = cartService.addToCart(skuId, num);
        // 返回skuId告诉哪个添加成功了
        redirectAttributes.addAttribute("skuId",skuId);
        // 重定向到成功页面
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    // 添加sku到购物车响应页面
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam(value = "skuId",required = false) Object skuId, Model model){
        CartItem cartItem = null;
        // 然后在查一遍 购物车
        if(skuId == null){
            model.addAttribute("item", null);
        }else{
            try {
                cartItem = cartService.getCartItem(Long.parseLong((String)skuId));
            } catch (NumberFormatException e) {
                log.warn("恶意操作! 页面传来skuId格式错误");
            }
            model.addAttribute("item", cartItem);
        }
        return "success";
    }

}
