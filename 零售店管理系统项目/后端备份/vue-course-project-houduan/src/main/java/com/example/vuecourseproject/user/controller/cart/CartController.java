package com.example.vuecourseproject.user.controller.cart;

import com.example.vuecourseproject.user.entity.cart.CartItem;
import com.example.vuecourseproject.user.other.cart.CartItemDTO;
import com.example.vuecourseproject.user.other.password.Result;
import com.example.vuecourseproject.user.service.cart.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 获取购物车所有商品
    @GetMapping
    public Result<List<CartItem>> getCartItems(@RequestParam String username) {
        List<CartItem> cartItems = cartService.getAllCartItems(username);
        return Result.success(cartItems);
    }

    // 添加商品到购物车
    @PostMapping
    public Result<String> addToCart(@RequestBody CartItemDTO cartItemDTO) {
        return cartService.addToCart(cartItemDTO);
    }

    // 更新购物车商品数量
    @PutMapping("/{productId}")
    public Result<String> updateQuantity(
            @PathVariable Long productId,
            @RequestParam String username,
            @RequestParam Integer quantity) {
        return cartService.updateQuantity(productId, username, quantity);
    }

    // 从购物车移除商品
    @DeleteMapping("/{productId}")
    public Result<String> removeFromCart(
            @PathVariable Long productId,
            @RequestParam String username) {
        return cartService.removeFromCart(productId, username);
    }

    // 清空购物车
    @DeleteMapping
    public Result<String> clearCart(@RequestParam String username) {
        return cartService.clearCart(username);
    }
}