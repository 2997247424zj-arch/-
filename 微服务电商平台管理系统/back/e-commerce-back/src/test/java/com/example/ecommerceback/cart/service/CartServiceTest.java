package com.example.ecommerceback.cart.service;

import com.example.ecommerceback.cart.entity.CartItem;
import com.example.ecommerceback.cart.mapper.CartMapper;
import com.example.ecommerceback.product.entity.Product;
import com.example.ecommerceback.product.mapper.ProductMapper;
import com.example.ecommerceback.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartServiceTest {

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAdd_Success() {
        Long userId = 1L;
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setQuantity(2);

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(8999.00);
        product.setStock(10);
        product.setStatus(1);

        when(cartMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(productMapper.selectById(1L)).thenReturn(product);
        when(cartMapper.insert(any(CartItem.class))).thenReturn(1);

        Result<Void> result = cartService.add(userId, item);

        assertEquals(200, result.getCode());
        verify(cartMapper, times(1)).insert(any(CartItem.class));
    }

    @Test
    void testList_Success() {
        Long userId = 1L;
        List<CartItem> mockCartItems = new ArrayList<>();

        CartItem item1 = new CartItem();
        item1.setId(1L);
        item1.setUserId(userId);
        item1.setProductId(1L);
        item1.setQuantity(2);
        item1.setIsSelected(1);
        mockCartItems.add(item1);

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setPrice(8999.00);
        product.setStock(50);
        product.setCoverImg("https://example.com/iphone15.jpg");

        when(cartMapper.selectByUser(userId)).thenReturn(mockCartItems);
        when(productMapper.selectById(1L)).thenReturn(product);

        Result<List<CartItem>> result = cartService.list(userId);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("iPhone 15", result.getData().get(0).getProductName());
    }

    @Test
    void testUpdateQuantity_Success() {
        Long userId = 1L;
        Long cartId = 1L;

        CartItem mockCartItem = new CartItem();
        mockCartItem.setId(cartId);
        mockCartItem.setUserId(userId);
        mockCartItem.setProductId(1L);
        mockCartItem.setQuantity(2);

        Product product = new Product();
        product.setId(1L);
        product.setStock(99);

        when(cartMapper.selectById(cartId)).thenReturn(mockCartItem);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(cartMapper.updateById(any(CartItem.class))).thenReturn(1);

        Result<Void> result = cartService.updateQuantity(userId, cartId, 3);

        assertEquals(200, result.getCode());
        verify(cartMapper, times(1)).updateById(any(CartItem.class));
    }

    @Test
    void testDelete_Success() {
        Long userId = 1L;
        Long cartId = 1L;

        CartItem mockCartItem = new CartItem();
        mockCartItem.setId(cartId);
        mockCartItem.setUserId(userId);

        when(cartMapper.selectById(cartId)).thenReturn(mockCartItem);
        when(cartMapper.deleteById(cartId)).thenReturn(1);

        Result<Void> result = cartService.delete(userId, cartId);

        assertEquals(200, result.getCode());
        verify(cartMapper, times(1)).deleteById(cartId);
    }
}
