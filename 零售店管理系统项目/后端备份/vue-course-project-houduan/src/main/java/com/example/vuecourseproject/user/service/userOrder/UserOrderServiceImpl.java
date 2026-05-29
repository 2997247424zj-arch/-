package com.example.vuecourseproject.user.service.userOrder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.vuecourseproject.user.entity.address.Address;
import com.example.vuecourseproject.user.entity.userOrder.UserOrder;
import com.example.vuecourseproject.user.entity.userOrder.UserOrderItem;
import com.example.vuecourseproject.user.mapper.userOrder.UserOrderItemMapper;
import com.example.vuecourseproject.user.mapper.userOrder.UserOrderMapper;
import com.example.vuecourseproject.user.other.userOrder.CreateOrderDTO;
import com.example.vuecourseproject.user.service.address.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserOrderServiceImpl implements UserOrderService {

    @Autowired
    private UserOrderMapper orderMapper;

    @Autowired
    private UserOrderItemMapper orderItemMapper;

    @Autowired
    private AddressService addressService;

    @Override
    @Transactional
    public UserOrder createOrder(CreateOrderDTO orderDTO) {
        // 1. 根据addressId获取地址信息
        Address address = addressService.getAddressById(orderDTO.getAddressId());
        if (address == null) {
            throw new RuntimeException("地址不存在");
        }

        // 2. 创建订单基本信息
        UserOrder order = new UserOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setCreateTime(LocalDateTime.now());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setRemark(orderDTO.getRemark());

        // 设置从地址服务获取的地址信息
        order.setConsignee(address.getUsername());
        order.setPhone(address.getPhone());
        order.setAddress(formatAddress(address));

        // 设置从DTO传入的username（唯一新增的代码）
        order.setUserName(orderDTO.getUsername());

        // 3. 计算总金额
        BigDecimal totalAmount = orderDTO.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        // 4. 保存订单
        orderMapper.insert(order);

        // 5. 保存订单项
        List<UserOrderItem> items = orderDTO.getItems().stream()
                .map(dto -> {
                    UserOrderItem item = new UserOrderItem();
                    item.setOrderId(order.getId());
                    item.setProductId(dto.getProductId());
                    item.setProductName(dto.getProductName());
                    item.setPrice(dto.getPrice());
                    item.setQuantity(dto.getQuantity());
                    item.setSubtotal(dto.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
                    return item;
                })
                .collect(Collectors.toList());

        for (UserOrderItem item : items) {
            orderItemMapper.insert(item);
        }

        // 6. 返回完整订单信息
        order.setItems(items);
        return order;
    }

    private String generateOrderNumber() {
        return "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String formatAddress(Address address) {
        return address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailedAddress();
    }

    @Override
    public List<UserOrder> getAllOrders() {
        List<UserOrder> orders = orderMapper.selectList(new QueryWrapper<UserOrder>().orderByDesc("create_time"));

        // 为每个订单加载订单项
        orders.forEach(order -> {
            List<UserOrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<UserOrderItem>().eq("order_id", order.getId())
            );
            order.setItems(items);
        });

        return orders;
    }

    @Override
    public UserOrder getOrderById(Long orderId) {
        UserOrder order = orderMapper.selectById(orderId);
        if (order != null) {
            List<UserOrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<UserOrderItem>().eq("order_id", orderId)
            );
            order.setItems(items);
        }
        return order;
    }
}