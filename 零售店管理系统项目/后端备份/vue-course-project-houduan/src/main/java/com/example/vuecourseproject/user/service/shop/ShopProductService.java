package com.example.vuecourseproject.user.service.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vuecourseproject.user.entity.shop.ShopProduct;
import com.example.vuecourseproject.user.other.shop.ShopProductVO;


import java.math.BigDecimal;
import java.util.List;

public interface ShopProductService {
    Page<ShopProduct> getProductsByPage(int pageNum, int pageSize);
    ShopProduct getById(Long id);
    List<ShopProduct> getProductsByCategory(Long categoryId);

    List<ShopProduct> searchProducts(String keyword);

    List<ShopProduct> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<ShopProduct> getNewProducts();

    List<ShopProduct> getDiscountedProducts();

    ShopProductVO getProductDetail(Long productId);

    Page<ShopProduct> getProductsWithFilter(Long categoryId, String keyword,
                                            BigDecimal minPrice, BigDecimal maxPrice,
                                            String sortBy, int pageNum, int pageSize);
}

