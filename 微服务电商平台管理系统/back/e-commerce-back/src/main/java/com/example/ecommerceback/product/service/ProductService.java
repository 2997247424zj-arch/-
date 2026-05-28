package com.example.ecommerceback.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ecommerceback.product.entity.Product;
import com.example.ecommerceback.product.entity.ProductCategory;
import com.example.ecommerceback.product.mapper.ProductCategoryMapper;
import com.example.ecommerceback.product.mapper.ProductMapper;
import com.example.ecommerceback.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;

    public Result<List<ProductCategory>> listCategories() {
        QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1).orderByAsc("sort").orderByAsc("id");
        return Result.success(productCategoryMapper.selectList(queryWrapper));
    }

    public Result<List<Product>> listByCategory(Long categoryId) {
        List<Product> list = productMapper.selectByCategory(categoryId);
        list.forEach(this::normalizeProduct);
        return Result.success(list);
    }

    public Result<List<Product>> listAll() {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1).orderByDesc("id");
        List<Product> list = productMapper.selectList(queryWrapper);
        list.forEach(this::normalizeProduct);
        return Result.success(list);
    }

    public Result<Map<String, Object>> queryProducts(
        Integer page,
        Integer size,
        Long categoryId,
        String keyword,
        Double minPrice,
        Double maxPrice,
        String sortBy
    ) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);

        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("name", keyword).or().like("description", keyword));
        }
        if (minPrice != null) {
            queryWrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le("price", maxPrice);
        }

        applySort(queryWrapper, sortBy);

        int currentPage = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 12 : size;

        Page<Product> result = productMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);
        result.getRecords().forEach(this::normalizeProduct);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", currentPage);
        data.put("size", pageSize);
        return Result.success(data);
    }

    public Result<List<Product>> search(String keyword) {
        try {
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.and(wrapper -> wrapper.like("name", keyword).or().like("description", keyword));
            queryWrapper.eq("status", 1);
            queryWrapper.orderByDesc("id");

            List<Product> list = productMapper.selectList(queryWrapper);
            list.forEach(this::normalizeProduct);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }

    public Result<Product> detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || (product.getStatus() != null && product.getStatus() != 1)) {
            return Result.error(404, "商品不存在");
        }
        return Result.success(normalizeProduct(product));
    }

    public Result<Integer> stock(Long id) {
        Product product = productMapper.selectById(id);
        return product != null ? Result.success(product.getStock()) : Result.error(404, "商品不存在");
    }

    @Transactional
    public Result<Product> create(Product product) {
        normalizeProduct(product);
        productMapper.insert(product);
        return Result.success(product);
    }

    public Result<Page<Product>> getProductList(int page, int size, String keyword, Long categoryId) {
        try {
            Page<Product> pageParam = new Page<>(page, size);
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> wrapper.like("name", keyword).or().like("description", keyword));
            }
            if (categoryId != null) {
                queryWrapper.eq("category_id", categoryId);
            }

            queryWrapper.orderByDesc("id");
            Page<Product> result = productMapper.selectPage(pageParam, queryWrapper);
            result.getRecords().forEach(this::normalizeProduct);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "获取商品列表失败: " + e.getMessage());
        }
    }

    @Transactional
    public Result<Product> addProduct(Product product) {
        try {
            if (!StringUtils.hasText(product.getName())) {
                return Result.error(400, "商品名称不能为空");
            }
            if (product.getPrice() == null || product.getPrice() <= 0) {
                return Result.error(400, "商品价格必须大于 0");
            }
            if (product.getStock() == null || product.getStock() < 0) {
                return Result.error(400, "商品库存不能为负数");
            }
            if (product.getStatus() == null) {
                product.setStatus(1);
            }

            normalizeProduct(product);
            productMapper.insert(product);
            return Result.success(product);
        } catch (Exception e) {
            return Result.error(500, "添加商品失败: " + e.getMessage());
        }
    }

    @Transactional
    public Result<Product> updateProduct(Product product) {
        try {
            if (product.getId() == null) {
                return Result.error(400, "商品 ID 不能为空");
            }

            Product existingProduct = productMapper.selectById(product.getId());
            if (existingProduct == null) {
                return Result.error(404, "商品不存在");
            }

            if (StringUtils.hasText(product.getName())) {
                existingProduct.setName(product.getName());
            }
            if (product.getDescription() != null) {
                existingProduct.setDescription(product.getDescription());
            }
            if (product.getPrice() != null && product.getPrice() > 0) {
                existingProduct.setPrice(product.getPrice());
            }
            if (product.getStock() != null && product.getStock() >= 0) {
                existingProduct.setStock(product.getStock());
            }
            if (product.getCategoryId() != null) {
                existingProduct.setCategoryId(product.getCategoryId());
            }
            if (StringUtils.hasText(product.getCoverImg())) {
                existingProduct.setCoverImg(product.getCoverImg());
            }
            if (StringUtils.hasText(product.getImgUrl())) {
                existingProduct.setImgUrl(product.getImgUrl());
            }
            if (StringUtils.hasText(product.getImage())) {
                existingProduct.setImage(product.getImage());
            }
            if (product.getStatus() != null) {
                existingProduct.setStatus(product.getStatus());
            }

            normalizeProduct(existingProduct);
            productMapper.updateById(existingProduct);
            return Result.success(existingProduct);
        } catch (Exception e) {
            return Result.error(500, "更新商品失败: " + e.getMessage());
        }
    }

    @Transactional
    public Result<String> deleteProduct(Long id) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            productMapper.deleteById(id);
            return Result.success("商品删除成功");
        } catch (Exception e) {
            return Result.error(500, "删除商品失败: " + e.getMessage());
        }
    }

    @Transactional
    public Result<String> updateProductStatus(Long id, Integer status) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            product.setStatus(status);
            productMapper.updateById(product);
            return Result.success("商品状态更新成功");
        } catch (Exception e) {
            return Result.error(500, "更新商品状态失败: " + e.getMessage());
        }
    }

    @Transactional
    public Result<String> updateProductStock(Long id, Integer stock) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error(404, "商品不存在");
            }
            if (stock < 0) {
                return Result.error(400, "库存不能为负数");
            }
            product.setStock(stock);
            productMapper.updateById(product);
            return Result.success("库存更新成功");
        } catch (Exception e) {
            return Result.error(500, "更新库存失败: " + e.getMessage());
        }
    }

    private void applySort(QueryWrapper<Product> queryWrapper, String sortBy) {
        if ("price_asc".equals(sortBy)) {
            queryWrapper.orderByAsc("price");
            return;
        }
        if ("price_desc".equals(sortBy)) {
            queryWrapper.orderByDesc("price");
            return;
        }
        if ("sales".equals(sortBy)) {
            queryWrapper.orderByDesc("id");
            return;
        }
        queryWrapper.orderByDesc("id");
    }

    private Product normalizeProduct(Product product) {
        if (product == null) {
            return null;
        }

        if (!StringUtils.hasText(product.getCoverImg())) {
            if (StringUtils.hasText(product.getImgUrl())) {
                product.setCoverImg(product.getImgUrl());
            } else if (StringUtils.hasText(product.getImage())) {
                product.setCoverImg(product.getImage());
            }
        }
        if (!StringUtils.hasText(product.getImgUrl()) && StringUtils.hasText(product.getCoverImg())) {
            product.setImgUrl(product.getCoverImg());
        }
        if (!StringUtils.hasText(product.getImage()) && StringUtils.hasText(product.getCoverImg())) {
            product.setImage(product.getCoverImg());
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }

        return product;
    }
}
