package com.example.ecommerceback.cart.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ecommerceback.cart.entity.CartItem;
import com.example.ecommerceback.cart.mapper.CartMapper;
import com.example.ecommerceback.product.entity.Product;
import com.example.ecommerceback.product.mapper.ProductMapper;
import com.example.ecommerceback.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    @Transactional
    public Result<Void> add(Long userId, CartItem item) {
        if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
            return Result.error(400, "商品ID和数量不能为空");
        }

        Product product = productMapper.selectById(item.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            return Result.error(404, "商品不存在或已下架");
        }
        if (product.getStock() != null && item.getQuantity() > product.getStock()) {
            return Result.error(400, "商品库存不足");
        }

        List<CartItem> existing = cartMapper.selectList(new QueryWrapper<CartItem>()
            .eq("user_id", userId)
            .eq("product_id", item.getProductId()));

        if (!existing.isEmpty()) {
            CartItem current = existing.get(0);
            int targetQuantity = current.getQuantity() + item.getQuantity();
            if (product.getStock() != null && targetQuantity > product.getStock()) {
                return Result.error(400, "商品库存不足");
            }
            current.setQuantity(targetQuantity);
            current.setIsSelected(1);
            cartMapper.updateById(current);
        } else {
            item.setUserId(userId);
            item.setIsSelected(1);
            cartMapper.insert(item);
        }

        return Result.success();
    }

    public Result<List<CartItem>> list(Long userId) {
        List<CartItem> list = cartMapper.selectByUser(userId);
        list.forEach(this::fillProductSnapshot);
        return Result.success(list);
    }

    @Transactional
    public Result<Void> updateQuantity(Long userId, Long cartId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return Result.error(400, "数量必须大于 0");
        }

        CartItem ci = cartMapper.selectById(cartId);
        if (ci == null || !ci.getUserId().equals(userId)) {
            return Result.error(404, "购物车项不存在");
        }

        Product product = productMapper.selectById(ci.getProductId());
        if (product != null && product.getStock() != null && quantity > product.getStock()) {
            return Result.error(400, "商品库存不足");
        }

        ci.setQuantity(quantity);
        cartMapper.updateById(ci);
        return Result.success();
    }

    @Transactional
    public Result<Void> delete(Long userId, Long cartId) {
        CartItem ci = cartMapper.selectById(cartId);
        if (ci == null || !ci.getUserId().equals(userId)) {
            return Result.error(404, "购物车项不存在");
        }
        cartMapper.deleteById(cartId);
        return Result.success();
    }

    @Transactional
    public Result<Void> delete(Long userId, List<Long> ids) {
        for (Long id : ids) {
            CartItem ci = cartMapper.selectById(id);
            if (ci == null || !ci.getUserId().equals(userId)) {
                return Result.error(404, "购物车项不存在");
            }
        }
        cartMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @Transactional
    public Result<Void> setSelected(Long userId, Long cartId, Integer isSelected) {
        CartItem ci = cartMapper.selectById(cartId);
        if (ci == null || !ci.getUserId().equals(userId)) {
            return Result.error(404, "购物车项不存在");
        }
        ci.setIsSelected(isSelected != null && isSelected == 1 ? 1 : 0);
        cartMapper.updateById(ci);
        return Result.success();
    }

    @Transactional
    public Result<Void> update(Long userId, Long cartId, Integer quantity, Integer isSelected) {
        CartItem ci = cartMapper.selectById(cartId);
        if (ci == null || !ci.getUserId().equals(userId)) {
            return Result.error(404, "购物车项不存在");
        }

        if (quantity == null || quantity <= 0) {
            return Result.error(400, "数量必须大于 0");
        }

        Product product = productMapper.selectById(ci.getProductId());
        if (product != null && product.getStock() != null && quantity > product.getStock()) {
            return Result.error(400, "商品库存不足");
        }

        ci.setQuantity(quantity);
        ci.setIsSelected(isSelected != null && isSelected == 1 ? 1 : 0);
        cartMapper.updateById(ci);
        return Result.success();
    }

    private void fillProductSnapshot(CartItem item) {
        Product product = productMapper.selectById(item.getProductId());
        if (product == null) {
            return;
        }

        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setStock(product.getStock());
        item.setProductCoverImg(resolveProductImage(product));
    }

    private String resolveProductImage(Product product) {
        if (product.getCoverImg() != null && !product.getCoverImg().isBlank()) {
            return product.getCoverImg();
        }
        if (product.getImgUrl() != null && !product.getImgUrl().isBlank()) {
            return product.getImgUrl();
        }
        return product.getImage();
    }
}
