package com.example.ecommerceback.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ecommerceback.cart.entity.CartItem;
import com.example.ecommerceback.cart.mapper.CartMapper;
import com.example.ecommerceback.order.entity.Order;
import com.example.ecommerceback.order.entity.OrderItem;
import com.example.ecommerceback.order.mapper.OrderItemMapper;
import com.example.ecommerceback.order.mapper.OrderMapper;
import com.example.ecommerceback.product.entity.Product;
import com.example.ecommerceback.product.mapper.ProductMapper;
import com.example.ecommerceback.user.entity.Address;
import com.example.ecommerceback.user.mapper.AddressMapper;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate_Success() {
        Long userId = 1L;
        Long addressId = 1L;

        Address address = new Address();
        address.setId(addressId);
        address.setUserId(userId);

        List<CartItem> mockCartItems = new ArrayList<>();
        CartItem item1 = new CartItem();
        item1.setId(1L);
        item1.setProductId(1L);
        item1.setQuantity(2);
        item1.setIsSelected(1);
        mockCartItems.add(item1);

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone 15");
        product1.setPrice(8999.00);
        product1.setStock(100);

        when(addressMapper.selectById(addressId)).thenReturn(address);
        when(cartMapper.selectList(any(QueryWrapper.class))).thenReturn(mockCartItems);
        when(productMapper.selectById(1L)).thenReturn(product1);
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        }).when(orderMapper).insert(any(Order.class));
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(cartMapper.deleteBatchIds(any())).thenReturn(1);

        Result<String> result = orderService.create(userId, addressId);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(orderMapper, times(1)).insert(any(Order.class));
    }

    @Test
    void testCreate_EmptyCart() {
        Long userId = 1L;
        Long addressId = 1L;

        Address address = new Address();
        address.setId(addressId);
        address.setUserId(userId);

        when(addressMapper.selectById(addressId)).thenReturn(address);
        when(cartMapper.selectList(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        Result<String> result = orderService.create(userId, addressId);

        assertEquals(400, result.getCode());
    }

    @Test
    void testList_Success() {
        Long userId = 1L;
        List<Order> mockOrders = new ArrayList<>();

        Order order1 = new Order();
        order1.setId(1L);
        order1.setOrderNo("ORD202604230001");
        order1.setUserId(userId);
        order1.setTotalAmount(22997.00);
        order1.setStatus(0);
        order1.setAddressId(10L);
        mockOrders.add(order1);

        Address address = new Address();
        address.setId(10L);
        address.setUserId(userId);
        address.setReceiver("Tom");
        address.setPhone("13800138000");
        address.setProvince("Guangdong");
        address.setCity("Shenzhen");
        address.setDistrict("Nanshan");
        address.setDetailAddress("Science Park");

        when(orderMapper.selectByUser(userId)).thenReturn(mockOrders);
        when(addressMapper.selectById(10L)).thenReturn(address);

        Result<List<Order>> result = orderService.list(userId);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("Tom", result.getData().get(0).getReceiverName());
    }

    @Test
    void testDetail_Success() {
        Long userId = 1L;
        String orderNo = "ORD202604230001";

        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setOrderNo(orderNo);
        mockOrder.setUserId(userId);
        mockOrder.setTotalAmount(22997.00);
        mockOrder.setStatus(0);
        mockOrder.setAddressId(10L);

        List<OrderItem> mockOrderItems = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrderId(1L);
        item.setProductId(1L);
        item.setProductName("iPhone 15");
        item.setPrice(8999.00);
        item.setQuantity(2);
        mockOrderItems.add(item);

        Address address = new Address();
        address.setId(10L);
        address.setUserId(userId);
        address.setReceiver("Tom");
        address.setPhone("13800138000");
        address.setProvince("Guangdong");
        address.setCity("Shenzhen");
        address.setDistrict("Nanshan");
        address.setDetailAddress("Science Park");

        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockOrder);
        when(orderItemMapper.selectList(any(QueryWrapper.class))).thenReturn(mockOrderItems);
        when(addressMapper.selectById(10L)).thenReturn(address);

        Result<Object> result = orderService.detail(userId, orderNo);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testCancel_Success() {
        Long userId = 1L;
        String orderNo = "ORD202604230001";

        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setOrderNo(orderNo);
        mockOrder.setUserId(userId);
        mockOrder.setStatus(0);

        List<OrderItem> mockOrderItems = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrderId(1L);
        item.setProductId(1L);
        item.setQuantity(2);
        mockOrderItems.add(item);

        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setStock(98);

        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockOrder);
        when(orderItemMapper.selectList(any(QueryWrapper.class))).thenReturn(mockOrderItems);
        when(productMapper.selectById(1L)).thenReturn(mockProduct);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        Result<Void> result = orderService.cancel(userId, orderNo);

        assertEquals(200, result.getCode());
        verify(orderMapper, times(1)).updateById(any(Order.class));
    }
}
