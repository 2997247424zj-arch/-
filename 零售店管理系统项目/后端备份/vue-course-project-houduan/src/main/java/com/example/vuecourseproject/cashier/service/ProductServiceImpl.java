package com.example.vuecourseproject.cashier.service;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.example.vuecourseproject.cashier.entity.Product;
import com.example.vuecourseproject.cashier.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;
    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<Product> search(String query) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.lambda().like(Product::getName, query).or().like(Product::getBarcode, query);
        return productMapper.selectList(wrapper);
    }

    @Override
    public List<Product> findAll() {
        return productMapper.selectList(null);
    }
}