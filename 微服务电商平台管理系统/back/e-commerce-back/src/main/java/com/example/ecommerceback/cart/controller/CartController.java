package com.example.ecommerceback.cart.controller;

import com.example.ecommerceback.cart.entity.CartItem;
import com.example.ecommerceback.cart.service.CartService;
import com.example.ecommerceback.utils.Result;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public Result<Void> add(HttpSession session, @RequestBody Map<String, Integer> data) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        CartItem item = new CartItem();
        item.setProductId(data.get("productId") == null ? null : Long.valueOf(data.get("productId")));
        item.setQuantity(data.get("quantity"));
        return cartService.add(userId, item);
    }

    @GetMapping("/list")
    public Result<List<CartItem>> list(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return cartService.list(userId);
    }

    @PostMapping("/update")
    public Result<Void> update(HttpSession session, @RequestBody Map<String, Object> data) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Long id = Long.valueOf(data.get("id").toString());
        Integer quantity = Integer.valueOf(data.get("quantity").toString());
        if (data.containsKey("isSelected")) {
            Integer isSelected = Integer.valueOf(data.get("isSelected").toString());
            return cartService.update(userId, id, quantity, isSelected);
        }
        return cartService.updateQuantity(userId, id, quantity);
    }

    @PostMapping("/delete")
    public Result<Void> delete(HttpSession session, @RequestBody Map<String, List<Long>> data) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return cartService.delete(userId, data.get("ids"));
    }
}
