package com.example.vuecourseproject.user.service.cart;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.vuecourseproject.user.entity.cart.CartItem;
import com.example.vuecourseproject.user.entity.shop.ShopProduct;
import com.example.vuecourseproject.user.mapper.cart.CartItemMapper;
import com.example.vuecourseproject.user.mapper.shop.ShopProductMapper;
import com.example.vuecourseproject.user.other.cart.CartItemDTO;
import com.example.vuecourseproject.user.other.password.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ShopProductMapper shopProductMapper;

    // 获取所有购物车商品
    public List<CartItem> getAllCartItems(String username) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUsername, username);
        List<CartItem> cartItems = cartItemMapper.selectList(wrapper);

        // 关联商品信息
        cartItems.forEach(item -> {
            ShopProduct product = shopProductMapper.selectById(item.getId());
            item.setProduct(product);
        });

        return cartItems;
    }

    @Transactional
    public Result<String> addToCart(CartItemDTO cartItemDTO) {
        Long productId = cartItemDTO.getProductId();
        String username = cartItemDTO.getUsername();
        Integer quantity = cartItemDTO.getQuantity();

        // 检查商品是否存在
        ShopProduct product = shopProductMapper.selectById(productId);
        if (product == null) {
            return (Result<String>) Result.error("商品不存在");
        }

        // 检查购物车是否已有该商品
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getId, productId)
                .eq(CartItem::getUsername, username);
        CartItem existingItem = cartItemMapper.selectOne(wrapper);

        if (existingItem != null) {
            // 更新数量
            CartItem updateItem = new CartItem();
            updateItem.setQuantity(existingItem.getQuantity() + quantity);

            // 使用update方法而不是updateById
            cartItemMapper.update(updateItem, wrapper);
            return (Result<String>) Result.success("购物车商品数量已更新");
        } else {
            // 新增购物车项
            CartItem newItem = new CartItem();
            newItem.setId(productId);
            newItem.setUsername(username);
            newItem.setName(product.getName());
            newItem.setPrice(product.getPrice());
            newItem.setOriginalPrice(product.getOriginalPrice());
            newItem.setDescription(product.getDescription());
            newItem.setDiscount(product.getDiscount());
            newItem.setQuantity(quantity);

            cartItemMapper.insert(newItem);
            return (Result<String>) Result.success("商品已添加到购物车");
        }
    }

    // 更新购物车商品数量
    // 更新购物车商品数量
    @Transactional
    public Result<String> updateQuantity(Long productId, String username, Integer quantity) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getId, productId)
                .eq(CartItem::getUsername, username);

        // 只更新quantity字段
        CartItem updateItem = new CartItem();
        updateItem.setQuantity(quantity);

        int result = cartItemMapper.update(updateItem, wrapper);
        if (result > 0) {
            return (Result<String>) Result.success("购物车商品数量已更新");
        } else {
            return (Result<String>) Result.error("购物车中不存在该商品");
        }
    }
    // 从购物车移除商品
    @Transactional
    public Result<String> removeFromCart(Long productId, String username) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getId, productId)
                .eq(CartItem::getUsername, username);
        int result = cartItemMapper.delete(wrapper);

        if (result > 0) {
            return (Result<String>) Result.success("商品已从购物车移除");
        } else {
            return (Result<String>) Result.error("购物车中不存在该商品");
        }
    }

    // 清空购物车
    @Transactional
    public Result<String> clearCart(String username) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUsername, username);
        cartItemMapper.delete(wrapper);
        return (Result<String>) Result.success("购物车已清空");
    }
}







