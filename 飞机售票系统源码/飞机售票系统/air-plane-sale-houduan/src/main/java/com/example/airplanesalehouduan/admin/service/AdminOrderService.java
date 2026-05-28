package com.example.airplanesalehouduan.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.airplanesalehouduan.admin.entity.AdminOrder;
import com.example.airplanesalehouduan.admin.repository.AdminOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

/**
 * 管理员订单管理服务
 */
@Service
public class AdminOrderService {

    @Autowired
    private AdminOrderRepository orderRepository;

    /**
     * 分页查询订单列表
     */
    public Page<AdminOrder> getOrderList(Integer page, Integer size,
                                         String orderNumber, String customer,
                                         String status, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<AdminOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 订单号搜索
            if (orderNumber != null && !orderNumber.trim().isEmpty()) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderNumber + "%"));
            }

            // 客户姓名搜索
            if (customer != null && !customer.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("passengerName")), "%" + customer.toLowerCase() + "%"));
            }

            // 状态筛选 - 直接使用字符串状态，不进行枚举转换
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                // 默认隐藏已标记为 deleted 的订单（软删除），除非前端显式传入 status 参数
                predicates.add(cb.notEqual(root.get("status"), "deleted"));
            }

            // 日期范围筛选
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec, pageable);
    }

    /**
     * 根据ID获取订单
     */
    public Optional<AdminOrder> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * 根据订单号获取订单
     */
    public Optional<AdminOrder> getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    /**
     * 删除订单
     */
    @Transactional
    public void deleteOrder(Long id) {
        AdminOrder ord = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("订单不存在"));
        // 软删除：将订单状态标记为 'deleted'，并保存（不物理删除数据）
        ord.setStatus("deleted");
        // 保存更新后的实体，updatedAt 会自动更新
        orderRepository.save(ord);
    }
}

