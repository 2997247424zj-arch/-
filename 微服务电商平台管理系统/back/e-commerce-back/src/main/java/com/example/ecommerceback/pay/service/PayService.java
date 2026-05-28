package com.example.ecommerceback.pay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ecommerceback.order.entity.Order;
import com.example.ecommerceback.order.mapper.OrderMapper;
import com.example.ecommerceback.order.service.OrderService;
import com.example.ecommerceback.pay.entity.PayRecord;
import com.example.ecommerceback.pay.mapper.PayRecordMapper;
import com.example.ecommerceback.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayService {

    private final PayRecordMapper payRecordMapper;
    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @Transactional
    public Result<String> create(String orderNo, Integer payType) {
        Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (order.getStatus() != null && order.getStatus() != 0) {
            return Result.error(400, "订单状态不允许支付");
        }

        String payNo = "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        PayRecord record = new PayRecord();
        record.setOrderNo(orderNo);
        record.setUserId(order.getUserId());
        record.setPayAmount(order.getPayAmount() != null ? order.getPayAmount() : order.getTotalAmount());
        record.setPayType(payType == null ? 1 : payType);
        record.setPayNo(payNo);
        record.setStatus(0);
        record.setCreateTime(LocalDateTime.now());
        payRecordMapper.insert(record);

        orderService.updateStatus(orderNo, 1);
        PayRecord savedRecord = payRecordMapper.selectOne(new QueryWrapper<PayRecord>().eq("pay_no", payNo));
        if (savedRecord != null) {
            savedRecord.setStatus(1);
            savedRecord.setFinishTime(LocalDateTime.now());
            payRecordMapper.updateById(savedRecord);
        }

        return Result.success(payNo);
    }

    @Transactional
    public Result<Void> notify(String payNo, boolean success) {
        PayRecord record = payRecordMapper.selectOne(new QueryWrapper<PayRecord>().eq("pay_no", payNo));
        if (record == null) {
            return Result.error(404, "支付记录不存在");
        }
        if (record.getStatus() != null && record.getStatus() != 0) {
            return Result.error(400, "支付已处理");
        }

        record.setStatus(success ? 1 : 2);
        record.setFinishTime(LocalDateTime.now());
        payRecordMapper.updateById(record);

        if (success) {
            orderService.updateStatus(record.getOrderNo(), 1);
        }
        return Result.success();
    }
}
