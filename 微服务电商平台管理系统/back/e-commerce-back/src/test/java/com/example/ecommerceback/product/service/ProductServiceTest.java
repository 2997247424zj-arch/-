package com.example.ecommerceback.product.service;

import com.example.ecommerceback.product.entity.Product;
import com.example.ecommerceback.product.mapper.ProductCategoryMapper;
import com.example.ecommerceback.product.mapper.ProductMapper;
import com.example.ecommerceback.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductCategoryMapper productCategoryMapper;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListAll_Success() {
        // 准备测试数据
        List<Product> mockProducts = new ArrayList<>();
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone 15");
        product1.setPrice(8999.00);
        mockProducts.add(product1);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("MacBook Pro");
        product2.setPrice(14999.00);
        mockProducts.add(product2);

        // Mock行为
        when(productMapper.selectList(any())).thenReturn(mockProducts);

        // 执行测试
        Result<List<Product>> result = productService.listAll();

        // 验证结果
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().size() > 0);
    }

    @Test
    void testListByCategory_Success() {
        // 准备测试数据
        List<Product> mockProducts = new ArrayList<>();
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(8999.00);
        mockProducts.add(product);

        // Mock行为
        when(productMapper.selectByCategory(1L)).thenReturn(mockProducts);

        // 执行测试
        Result<List<Product>> result = productService.listByCategory(1L);

        // 验证结果
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(productMapper, times(1)).selectByCategory(1L);
    }

    @Test
    void testDetail_Success() {
        // 准备测试数据
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("iPhone 15");
        mockProduct.setPrice(8999.00);
        mockProduct.setDescription("苹果最新旗舰手机");

        // Mock行为
        when(productMapper.selectById(1L)).thenReturn(mockProduct);

        // 执行测试
        Result<Product> result = productService.detail(1L);

        // 验证结果
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(productMapper, times(1)).selectById(1L);
    }

    @Test
    void testDetail_NotFound() {
        // Mock行为
        when(productMapper.selectById(999L)).thenReturn(null);

        // 执行测试
        Result<Product> result = productService.detail(999L);

        // 验证结果
        assertEquals(404, result.getCode());
    }
}
