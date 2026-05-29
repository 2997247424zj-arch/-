package com.example.vuecourseproject.user.service.shop;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.vuecourseproject.user.entity.shop.ShopProduct;
import com.example.vuecourseproject.user.mapper.shop.ShopProductCategoryMapper;
import com.example.vuecourseproject.user.mapper.shop.ShopProductMapper;
import com.example.vuecourseproject.user.other.shop.ShopProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ShopProductServiceImpl extends ServiceImpl<ShopProductMapper, ShopProduct> implements ShopProductService {

    @Autowired
    private ShopProductMapper productMapper;

    @Autowired
    private ShopProductCategoryMapper categoryMapper;

    @Override
    public Page<ShopProduct> getProductsByPage(int pageNum, int pageSize) {
        Page<ShopProduct> page = new Page<>(pageNum, pageSize);
        return productMapper.selectPage(page, new QueryWrapper<ShopProduct>().orderByDesc("create_time"));
    }
    @Override
    public ShopProduct getById(Long id) {
        return productMapper.selectById(id);
    }
    @Override
    public List<ShopProduct> getProductsByCategory(Long categoryId) {
        return productMapper.selectByCategoryId(categoryId);
    }

    @Override
    public List<ShopProduct> searchProducts(String keyword) {
        return productMapper.searchProducts(keyword);
    }

    @Override
    public List<ShopProduct> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productMapper.selectByPriceRange(minPrice, maxPrice);
    }

    @Override
    public List<ShopProduct> getNewProducts() {
        QueryWrapper<ShopProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_new", true).orderByDesc("create_time");
        return productMapper.selectList(queryWrapper);
    }

    @Override
    public List<ShopProduct> getDiscountedProducts() {
        QueryWrapper<ShopProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt("discount", 0).orderByDesc("discount");
        return productMapper.selectList(queryWrapper);
    }

    @Override
    public ShopProductVO getProductDetail(Long productId) {
        ShopProduct product = productMapper.selectById(productId);
        if (product == null) {
            return null;
        }

        ShopProductVO vo = new ShopProductVO();
        // 这里可以设置VO的各种属性，包括关联的分类信息等
        return vo;
    }

    @Override
    public Page<ShopProduct> getProductsWithFilter(Long categoryId, String keyword,
                                                   BigDecimal minPrice, BigDecimal maxPrice,
                                                   String sortBy, int pageNum, int pageSize) {
        Page<ShopProduct> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ShopProduct> queryWrapper = new QueryWrapper<>();

        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq("category_id", categoryId);
        }

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", keyword)
                    .or()
                    .like("description", keyword));
        }

        if (minPrice != null && maxPrice != null) {
            queryWrapper.between("price", minPrice, maxPrice);
        }

        // 排序处理
        if (sortBy != null) {
            switch (sortBy) {
                case "priceAsc":
                    queryWrapper.orderByAsc("price");
                    break;
                case "priceDesc":
                    queryWrapper.orderByDesc("price");
                    break;
                case "newest":
                    queryWrapper.orderByDesc("create_time");
                    break;
                default:
                    queryWrapper.orderByDesc("id");
            }
        }

        return productMapper.selectPage(page, queryWrapper);
    }
}
