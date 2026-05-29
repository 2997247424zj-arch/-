package com.example.vuecourseproject.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vuecourseproject.admin.entity.KuProductEntity;
import com.example.vuecourseproject.admin.mapper.KuProductMapper;
import com.example.vuecourseproject.admin.other.KuCategoryDTO;
import com.example.vuecourseproject.admin.other.KuInventoryReportDTO;
import com.example.vuecourseproject.admin.other.KuProductDTO;
import com.example.vuecourseproject.admin.other.KuProductWithStock;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KuProductServiceImpl implements KuProductService {

    private final KuProductMapper productMapper;
    private final KuCategoryService categoryService;

    public KuProductServiceImpl(KuProductMapper productMapper, KuCategoryService categoryService) {
        this.productMapper = productMapper;
        this.categoryService = categoryService;
    }

    @Override
    public Page<KuProductDTO> getProductPage(int page, int size, String keyword) {
        QueryWrapper<KuProductEntity> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like("name", keyword)
                    .or()
                    .like("barcode", keyword);
        }

        Page<KuProductEntity> entityPage = productMapper.selectPage(new Page<>(page, size), queryWrapper);

        Page<KuProductDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(entityPage, dtoPage, "records");

        List<KuCategoryDTO> categories = categoryService.getEnabledCategories();

        List<KuProductDTO> dtos = entityPage.getRecords().stream().map(entity -> {
            KuProductDTO dto = new KuProductDTO();
            BeanUtils.copyProperties(entity, dto);

            categories.stream()
                    .filter(category -> category.getId().equals(entity.getCategoryId()))
                    .findFirst()
                    .ifPresent(category -> dto.setCategoryName(category.getName()));

            return dto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    @Override
    public KuProductDTO getProductById(Long id) {
        KuProductEntity entity = productMapper.selectById(id);
        if (entity == null) {
            return null;
        }

        KuProductDTO dto = new KuProductDTO();
        BeanUtils.copyProperties(entity, dto);

        categoryService.getEnabledCategories().stream()
                .filter(category -> category.getId().equals(entity.getCategoryId()))
                .findFirst()
                .ifPresent(category -> dto.setCategoryName(category.getName()));

        return dto;
    }

    @Override
    public List<KuInventoryReportDTO> getInventoryReport(String keyword) {
        List<KuProductWithStock> productsWithStock = productMapper.selectProductsWithStock(keyword);

        List<KuCategoryDTO> categories = categoryService.getEnabledCategories();

        return productsWithStock.stream().map(item -> {
            KuInventoryReportDTO dto = new KuInventoryReportDTO();
            dto.setId(item.getProductId().toString());
            dto.setName(item.getProductName());

            categories.stream()
                    .filter(category -> category.getId().equals(item.getCategoryId()))
                    .findFirst()
                    .ifPresent(category -> dto.setCategory(category.getName()));

            dto.setCurrentStock(item.getStock());
            dto.setPrice(item.getPrice());
            dto.setTotalValue(item.getPrice().multiply(new BigDecimal(item.getStock())));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalInventoryValue() {
        List<KuInventoryReportDTO> inventory = getInventoryReport(null);
        return inventory.stream()
                .map(KuInventoryReportDTO::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public void restockProduct(Long productId, Integer amount) {
        KuProductEntity product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        product.setStock(product.getStock() + amount);
        productMapper.updateById(product);
    }

    @Override
    public Map<String, BigDecimal> getInventoryDistribution() {
        // 获取所有启用的分类
        List<KuCategoryDTO> categories = categoryService.getEnabledCategories();

        // 获取每个分类的商品库存总值
        return categories.stream().collect(Collectors.toMap(
                KuCategoryDTO::getName,
                category -> {
                    // 查询该分类下所有商品的库存总值
                    QueryWrapper<KuProductEntity> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("category_id", category.getId());
                    queryWrapper.select("IFNULL(SUM(stock * price), 0) as total_value");

                    Map<String, Object> result = productMapper.selectMaps(queryWrapper).get(0);
                    return new BigDecimal(result.get("total_value").toString());
                }
        ));
    }

}