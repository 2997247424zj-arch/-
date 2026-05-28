package com.example.ecommerceback.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;

    @Transactional
    public Result<String> create(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
            return Result.error(400, "收货地址不存在");
        }

        List<CartItem> selected = cartMapper.selectList(new QueryWrapper<CartItem>()
            .eq("user_id", userId)
            .eq("is_selected", 1));

        if (selected.isEmpty()) {
            return Result.error(400, "购物车未选中商品");
        }

        double totalAmount = 0;
        for (CartItem ci : selected) {
            Product product = productMapper.selectById(ci.getProductId());
            if (product == null || product.getStock() == null || product.getStock() < ci.getQuantity()) {
                return Result.error(400, "商品库存不足或不存在: " + ci.getProductId());
            }
            totalAmount += product.getPrice() * ci.getQuantity();
        }

        String orderNo = generateOrderNo();
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        for (CartItem ci : selected) {
            Product product = productMapper.selectById(ci.getProductId());
            product.setStock(product.getStock() - ci.getQuantity());
            productMapper.updateById(product);

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(ci.getQuantity());
            item.setTotalPrice(product.getPrice() * ci.getQuantity());
            item.setCreateTime(LocalDateTime.now());
            orderItemMapper.insert(item);
        }

        List<Long> cartIds = selected.stream().map(CartItem::getId).collect(Collectors.toList());
        cartMapper.deleteBatchIds(cartIds);

        return Result.success(orderNo);
    }

    public Result<List<Order>> list(Long userId) {
        List<Order> orders = orderMapper.selectByUser(userId);
        orders.forEach(this::fillOrderAddress);
        return Result.success(orders);
    }

    public Result<List<Order>> getUserOrders(Long userId) {
        try {
            List<Order> orders = orderMapper.selectByUser(userId);
            for (Order order : orders) {
                List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", order.getId())
                );
                order.setOrderItems(items);
                fillOrderAddress(order);
            }
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error(500, "获取订单列表失败: " + e.getMessage());
        }
    }

    public Result<Object> detail(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new QueryWrapper<Order>()
            .eq("order_no", orderNo)
            .eq("user_id", userId));
        if (order == null) {
            return Result.error(404, "订单不存在");
        }

        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>()
            .eq("order_id", order.getId()));
        fillOrderAddress(order);
        return Result.success(new OrderDetailDTO(order, items));
    }

    @Transactional
    public Result<Void> cancel(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new QueryWrapper<Order>()
            .eq("order_no", orderNo)
            .eq("user_id", userId));
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.error(400, "只能取消待付款订单");
        }

        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>()
            .eq("order_id", order.getId()));

        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null && product.getStock() != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.updateById(product);
            }
        }

        order.setStatus(2);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success();
    }

    @Transactional
    public void updateStatus(String orderNo, int newStatus) {
        Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        if (order != null) {
            order.setStatus(newStatus);
            if (newStatus == 1) {
                order.setPayTime(LocalDateTime.now());
            }
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }
    }

    public Result<Page<Order>> getAllOrders(int page, int size, Integer status, String keyword) {
        try {
            Page<Order> pageParam = new Page<>(page, size);
            QueryWrapper<Order> queryWrapper = new QueryWrapper<>();

            if (status != null) {
                queryWrapper.eq("status", status);
            }
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like("order_no", keyword);
            }

            queryWrapper.orderByDesc("create_time");
            Page<Order> result = orderMapper.selectPage(pageParam, queryWrapper);

            for (Order order : result.getRecords()) {
                List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", order.getId())
                );
                order.setOrderItems(items);
                fillOrderAddress(order);
            }

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "获取订单列表失败: " + e.getMessage());
        }
    }

    @Transactional
    public Result<String> adminUpdateOrderStatus(String orderNo, Integer status) {
        try {
            Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));
            if (order == null) {
                return Result.error(404, "订单不存在");
            }

            order.setStatus(status);
            if (status != null && status == 1) {
                order.setPayTime(LocalDateTime.now());
            }
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            return Result.success("订单状态更新成功");
        } catch (Exception e) {
            return Result.error(500, "更新订单状态失败: " + e.getMessage());
        }
    }

    public Result<Object> adminGetOrderDetail(String orderNo) {
        try {
            Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));
            if (order == null) {
                return Result.error(404, "订单不存在");
            }

            List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>()
                .eq("order_id", order.getId()));
            fillOrderAddress(order);
            return Result.success(new OrderDetailDTO(order, items));
        } catch (Exception e) {
            return Result.error(500, "获取订单详情失败: " + e.getMessage());
        }
    }

    @Transactional
    public Result<String> deleteOrder(String orderNo) {
        try {
            Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));
            if (order == null) {
                return Result.error(404, "订单不存在");
            }

            orderItemMapper.delete(new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
            orderMapper.deleteById(order.getId());
            return Result.success("订单删除成功");
        } catch (Exception e) {
            return Result.error(500, "删除订单失败: " + e.getMessage());
        }
    }

    @Transactional
    public Result<String> confirmReceive(Long userId, String orderNo) {
        try {
            Order order = orderMapper.selectOne(new QueryWrapper<Order>()
                .eq("order_no", orderNo)
                .eq("user_id", userId));
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            if (order.getStatus() != 1) {
                return Result.error(400, "只有已支付的订单才能确认收货");
            }

            order.setStatus(3);
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            return Result.success("确认收货成功");
        } catch (Exception e) {
            return Result.error(500, "确认收货失败: " + e.getMessage());
        }
    }

    private void fillOrderAddress(Order order) {
        if (order == null || order.getAddressId() == null) {
            return;
        }
        Address address = addressMapper.selectById(order.getAddressId());
        if (address == null) {
            return;
        }

        order.setReceiverName(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(
            String.join(" ",
                safe(address.getProvince()),
                safe(address.getCity()),
                safe(address.getDistrict()),
                safe(address.getDetailAddress())
            ).trim()
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String generateOrderNo() {
        return "ORDER" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
            UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
    }

    public static class OrderDetailDTO {
        public Order order;
        public List<OrderItem> items;

        public OrderDetailDTO(Order order, List<OrderItem> items) {
            this.order = order;
            this.items = items;
        }
    }
}
