package com.junting.gulimall.cart.service;

import com.junting.gulimall.cart.vo.Cart;
import com.junting.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author mini_zeng
 * @create 2022-01-15 11:06
 */

public interface CartService {
    Cart getCart() throws ExecutionException, InterruptedException;

    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getCurrentUserCartItems();
}
