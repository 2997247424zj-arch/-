package com.example.ecommerceback.pay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ecommerceback.order.entity.Order;
import com.example.ecommerceback.order.mapper.OrderMapper;
import com.example.ecommerceback.order.service.OrderService;
import com.example.ecommerceback.pay.entity.PayRecord;
import com.example.ecommerceback.pay.mapper.PayRecordMapper;
import com.example.ecommerceback.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PayServiceTest {

    @Mock
    private PayRecordMapper payRecordMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PayService payService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate_Success() {
        String orderNo = "ORD202604230001";
        Integer payType = 1;

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(1L);
        order.setStatus(0);
        order.setPayAmount(99.9);
        order.setTotalAmount(99.9);

        PayRecord savedRecord = new PayRecord();
        savedRecord.setPayNo("PAY-ABC123");
        savedRecord.setStatus(0);

        when(orderMapper.selectOne(any(QueryWrapper.class))).thenReturn(order);
        when(payRecordMapper.insert(any(PayRecord.class))).thenReturn(1);
        doNothing().when(orderService).updateStatus(anyString(), anyInt());
        when(payRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(savedRecord);
        when(payRecordMapper.updateById(any(PayRecord.class))).thenReturn(1);

        Result<String> result = payService.create(orderNo, payType);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(payRecordMapper, times(1)).insert(any(PayRecord.class));
    }

    @Test
    void testNotify_Success() {
        String payNo = "PAY-ABC123";
        boolean success = true;

        PayRecord mockPayRecord = new PayRecord();
        mockPayRecord.setId(1L);
        mockPayRecord.setOrderNo("ORD202604230001");
        mockPayRecord.setPayNo(payNo);
        mockPayRecord.setStatus(0);

        when(payRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockPayRecord);
        when(payRecordMapper.updateById(any(PayRecord.class))).thenReturn(1);
        doNothing().when(orderService).updateStatus(anyString(), anyInt());

        Result<Void> result = payService.notify(payNo, success);

        assertEquals(200, result.getCode());
        verify(payRecordMapper, times(1)).updateById(any(PayRecord.class));
        verify(orderService, times(1)).updateStatus(anyString(), anyInt());
    }

    @Test
    void testNotify_PayRecordNotFound() {
        when(payRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        Result<Void> result = payService.notify("PAY-ABC123", true);

        assertEquals(404, result.getCode());
    }

    @Test
    void testNotify_PaymentAlreadyProcessed() {
        PayRecord mockPayRecord = new PayRecord();
        mockPayRecord.setId(1L);
        mockPayRecord.setOrderNo("ORD202604230001");
        mockPayRecord.setPayNo("PAY-ABC123");
        mockPayRecord.setStatus(1);

        when(payRecordMapper.selectOne(any(QueryWrapper.class))).thenReturn(mockPayRecord);

        Result<Void> result = payService.notify("PAY-ABC123", true);

        assertEquals(400, result.getCode());
    }
}
