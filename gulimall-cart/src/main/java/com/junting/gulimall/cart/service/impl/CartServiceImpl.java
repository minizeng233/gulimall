package com.junting.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.junting.common.constant.CartConstant;
import com.junting.common.utils.R;
import com.junting.gulimall.cart.feign.productFeignService;
import com.junting.gulimall.cart.interceptor.CartInterceptor;
import com.junting.gulimall.cart.vo.Cart;
import com.junting.gulimall.cart.service.CartService;
import com.junting.gulimall.cart.vo.CartItem;
import com.junting.gulimall.cart.vo.SkuInfoVo;
import com.junting.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author mini_zeng
 * @create 2022-01-15 11:07
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final String CART_PREFIX = "ATGUIGU:cart:";

    @Autowired
    productFeignService productFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ThreadPoolExecutor executor;

    //获取购物车信息
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();
        // 临时购物车的key // 用户key在哪里设置的以后研究一下
        String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
        // 简单处理一下，以后修改
        if ("ATGUIGU:cart:".equals(tempCartKey)) tempCartKey += "X";

        // 是否登录
        if (userInfoTo.getUserId() != null) {
            // 已登录 对用户的购物车进行操作
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            // 1 如果临时购物车的数据没有进行合并
            List<CartItem> tempItem = getCartItems(tempCartKey);
            if (tempItem != null) {
                // 2 临时购物车有数据 则进行合并
                log.info("\n[" + userInfoTo.getUsername() + "] 的购物车已合并");
                for (CartItem cartItem : tempItem) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
                // 3 清空临时购物车,防止重复添加
                clearCart(tempCartKey);
                // 设置为非临时用户
                userInfoTo.setTempUser(false);
            }
            // 4 获取登录后的购物车数据 [包含合并过来的临时购物车数据]
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        } else {
            // 没登录 获取临时购物车的所有购物项
            cart.setItems(getCartItems(tempCartKey));
        }
        return cart;
    }

    private void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    private List<CartItem> getCartItems(String cartKey) {
        // JSON.toJSONString(obj)的结果是 "{\"check\":  多了个String
        // (String)obj 的结果是 {"check"
        // 使用JSON.toJSONString(obj)会报错
        List<CartItem> list = new ArrayList<>();
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> cartItems = operations.values();
        if (cartItems != null && cartItems.size() > 0) {
            for (Object cartItem : cartItems) {
                String cartItemAsString = (String) cartItem;
                CartItem item = JSON.parseObject(cartItemAsString, CartItem.class);
                list.add(item);
            }
            return list;
        }
        return null;
    }

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        //需要确认操作的购物车是用户的还是临时用户的（判断是否登录）
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        // 查看该用户购物车里是否有指定的skuId
        String res = (String) cartOps.get(skuId.toString());
        // 查看用户购物车里是否已经有了该sku项
        if (StringUtils.isEmpty(res)) {
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                //需要查询skuid的信息,封装CartItem
                R skuInfo = productFeignService.SkuInfo(skuId);
                SkuInfoVo sku = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                // 2. 填充购物项
                cartItem.setCount(num);
                cartItem.setCheck(true);
                cartItem.setImage(sku.getSkuDefaultImg());
                cartItem.setPrice(sku.getPrice());
                cartItem.setTitle(sku.getSkuTitle());
                cartItem.setSkuId(skuId);
            }, executor);

            // 3. 远程查询sku销售属性，销售属性是个list
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);

            // 等待执行完成
            CompletableFuture.allOf(getSkuInfo, getSkuSaleAttrValues).get();

            // sku放到用户购物车redis中
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
        //已有商品只需要修改数量
        else {
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            // 不太可能并发，无需加锁
            cartItem.setCount(cartItem.getCount() + num);
            // sku放到用户购物车redis中
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(o, CartItem.class);
        return cartItem;
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        // 获取要选中的购物项
        CartItem cartItem = getCartItem(skuId);
        // 切换购物车选择状态
        cartItem.setCheck(check == 1 ? true : false);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        // 获取要选中的购物项
        CartItem cartItem = getCartItem(skuId);
        // 切换购物车选择状态
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        // 获取要选中的购物项
        CartItem cartItem = getCartItem(skuId);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        }
        String cartKey = CART_PREFIX + userInfoTo.getUserId();
        List<CartItem> cartItems = getCartItems(cartKey);
        List<CartItem> collect = cartItems.stream().filter(CartItem::getCheck)
                .map(item -> {
                    try {
                        // 因为redis中的价格可能已经不匹配了，所以重新获取一下
                        R r = productFeignService.getPrice(item.getSkuId());
                        String price = (String) r.get("data");
                        item.setPrice(new BigDecimal(price));
                    } catch (Exception e) {
                        log.warn("远程查询商品价格出错 [商品服务未启动]");
                    }
                    return item;
                }).collect(Collectors.toList());
        return collect;
    }

    //需要确认操作的购物车是用户的还是临时用户的（判断是否登录）
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //判断是否登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Long userId = userInfoTo.getUserId();
        String cartKey = CART_PREFIX;
        String key = "";
        if (userId != null) {
            key = cartKey + userId;
        } else {
            key = cartKey + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(key);
        return operations;
    }
}
