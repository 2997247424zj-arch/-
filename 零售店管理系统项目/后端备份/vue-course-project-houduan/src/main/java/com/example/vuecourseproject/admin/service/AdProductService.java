package com.example.vuecourseproject.admin.service;
import com.example.vuecourseproject.admin.mapper.AdProductCategoryMapper;
import com.example.vuecourseproject.admin.mapper.AdProductMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdProductService {
    private final AdProductMapper adproductMapper;
    private final AdProductCategoryMapper categoryMapper;

    public AdProductService(AdProductMapper productMapper,
                          AdProductCategoryMapper categoryMapper) {
        this.adproductMapper = productMapper;
        this.categoryMapper = categoryMapper;
    }

    /**
     * 删除商品并处理关联分类
     */
    @Transactional
    public void deleteProduct(Long id) {
        // 检查商品是否存在
        if (!adproductMapper.existsById(id)) {
            throw new RuntimeException("商品不存在，ID: " + id);
        }

        // 获取商品分类ID
        Long categoryId = adproductMapper.getCategoryId(id);

        // 删除商品
        int affectedRows = adproductMapper.deleteById(id);
        if (affectedRows == 0) {
            throw new RuntimeException("删除商品失败，ID: " + id);
        }

        // 检查分类是否还有商品，如果没有可以执行其他逻辑
        if (categoryId != null) {
            int productCount = categoryMapper.countProductsByCategory(categoryId);
            if (productCount == 0) {
                // 可以在这里添加分类空置后的处理逻辑
                // 例如：logger.info("分类 {} 下已无商品", categoryId);
            }
        }
    }
}