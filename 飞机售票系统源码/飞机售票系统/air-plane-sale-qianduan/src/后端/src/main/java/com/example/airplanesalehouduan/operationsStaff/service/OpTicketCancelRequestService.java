package com.example.airplanesalehouduan.operationsStaff.service;

import com.example.airplanesalehouduan.passengers.entity.TicketCancelRequest;
import com.example.airplanesalehouduan.passengers.other.TicketCancelRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 航空运营 - 退票申请管理服务
 *
 * 说明：
 * - 面向航空运营/系统管理员使用的退票审核列表
 * - 基于乘客侧的 TicketCancelRequest 实体与仓库实现
 */
@Service
public class OpTicketCancelRequestService {

    private final TicketCancelRequestRepository cancelRequestRepository;
    private final com.example.airplanesalehouduan.passengers.other.OrderRepository orderRepository;
    private final com.example.airplanesalehouduan.passengers.other.TicketRepository ticketRepository;

    public OpTicketCancelRequestService(TicketCancelRequestRepository cancelRequestRepository,
                                        com.example.airplanesalehouduan.passengers.other.OrderRepository orderRepository,
                                        com.example.airplanesalehouduan.passengers.other.TicketRepository ticketRepository) {
        this.cancelRequestRepository = cancelRequestRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * 分页查询退票申请列表（支持按状态、订单号、申请人、申请日期范围筛选）
     */
    public Page<TicketCancelRequest> queryPage(
            String status,
            String orderNo,
            String applicantName,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        Specification<TicketCancelRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                String s = status.trim();
                if ("待处理".equals(s)) {
                    predicates.add(root.get("status").in("待处理", "待审核"));
                } else if ("拒绝".equals(s)) {
                    // 兼容前端可能传递"拒绝"的情况，映射为"不通过"
                    predicates.add(cb.equal(root.get("status"), "不通过"));
                } else {
                    predicates.add(cb.equal(root.get("status"), s));
                }
            }
            if (orderNo != null && !orderNo.isBlank()) {
                predicates.add(cb.equal(root.get("orderno"), orderNo));
            }
            if (applicantName != null && !applicantName.isBlank()) {
                predicates.add(cb.like(root.get("applicantName"), "%" + applicantName + "%"));
            }
            if (startDate != null) {
                LocalDateTime from = startDate.atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("requestTime"), from));
            }
            if (endDate != null) {
                LocalDateTime to = endDate.atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("requestTime"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return cancelRequestRepository.findAll(spec, pageable);
    }

    /**
     * 审核通过退票申请
     */
    @Transactional
    public TicketCancelRequest approve(Long id, Integer processedBy, String remark) throws Exception {
        TicketCancelRequest request = cancelRequestRepository.findById(id)
                .orElseThrow(() -> new Exception("退票申请不存在"));

        request.setStatus("通过");
        request.setProcessedBy(processedBy);
        request.setProcessedAt(LocalDateTime.now());
        if (remark != null && !remark.isBlank()) {
            request.setRemark(remark);
        }

        // 更新原订单与机票为“待退款”
        try {
            java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> ordOpt = orderRepository.findByOrderNo(request.getOrderno());
            if (ordOpt.isPresent()) {
                com.example.airplanesalehouduan.passengers.entity.Order order = ordOpt.get();
                order.setStatus("待退款");
                orderRepository.save(order);

                java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> tickets = ticketRepository.findByOrderNo(order.getOrderNo());
                if (tickets != null && !tickets.isEmpty()) {
                    for (com.example.airplanesalehouduan.passengers.entity.Ticket t : tickets) {
                        t.setStatus("待退款");
                    }
                    ticketRepository.saveAll(tickets);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return cancelRequestRepository.save(request);
    }

    /**
     * 审核拒绝退票申请
     */
    @Transactional
    public TicketCancelRequest reject(Long id, Integer processedBy, String reason) throws Exception {
        TicketCancelRequest request = cancelRequestRepository.findById(id)
                .orElseThrow(() -> new Exception("退票申请不存在"));

        request.setStatus("不通过");
        request.setProcessedBy(processedBy);
        request.setProcessedAt(LocalDateTime.now());

        if (reason != null && !reason.isBlank()) {
            // 运营备注中记录拒绝原因
            request.setRemark(reason);
        }

        // 恢复原订单与机票为“待出行”
        try {
            java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> ordOpt = orderRepository.findByOrderNo(request.getOrderno());
            if (ordOpt.isPresent()) {
                com.example.airplanesalehouduan.passengers.entity.Order order = ordOpt.get();
                order.setStatus("待出行");
                orderRepository.save(order);

                java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> tickets = ticketRepository.findByOrderNo(order.getOrderNo());
                if (tickets != null && !tickets.isEmpty()) {
                    for (com.example.airplanesalehouduan.passengers.entity.Ticket t : tickets) {
                        // 拒绝退票时，机票应恢复为“已出票”
                        t.setStatus("已出票");
                    }
                    ticketRepository.saveAll(tickets);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return cancelRequestRepository.save(request);
    }

    /**
     * 批量通过
     */
    @Transactional
    public void batchApprove(List<Long> ids, Integer processedBy, String remark) {
        LocalDateTime now = LocalDateTime.now();
        List<TicketCancelRequest> list = cancelRequestRepository.findAllById(ids);
        for (TicketCancelRequest request : list) {
            request.setStatus("通过");
            request.setProcessedBy(processedBy);
            request.setProcessedAt(now);
            if (remark != null && !remark.isBlank()) {
                request.setRemark(remark);
            }
        }
        cancelRequestRepository.saveAll(list);
    }

    /**
     * 批量拒绝
     */
    @Transactional
    public void batchReject(List<Long> ids, Integer processedBy, String reason) {
        LocalDateTime now = LocalDateTime.now();
        List<TicketCancelRequest> list = cancelRequestRepository.findAllById(ids);
        for (TicketCancelRequest request : list) {
            request.setStatus("不通过");
            request.setProcessedBy(processedBy);
            request.setProcessedAt(now);
            if (reason != null && !reason.isBlank()) {
                request.setRemark(reason);
            }
        }
        cancelRequestRepository.saveAll(list);
    }

    /**
     * 统计数据：待处理数量、已通过数量、已拒绝数量、退款总额
     */
    public CancelStatistics getStatistics() {
        long pending = cancelRequestRepository.countByStatus("待处理");
        long approved = cancelRequestRepository.countByStatus("通过");
        long rejected = cancelRequestRepository.countByStatus("不通过");

        // 简化实现：遍历一次数据库计算退款总额（仅通过的记录）
        BigDecimal totalRefund = BigDecimal.ZERO;
        List<TicketCancelRequest> approvedList =
                cancelRequestRepository.findAll((root, query, cb) ->
                        cb.equal(root.get("status"), "通过"));
        for (TicketCancelRequest request : approvedList) {
            if (request.getRefundFare() != null) {
                totalRefund = totalRefund.add(request.getRefundFare());
            }
        }

        CancelStatistics statistics = new CancelStatistics();
        statistics.setPending(pending);
        statistics.setApproved(approved);
        statistics.setRejected(rejected);
        statistics.setTotalRefundFare(totalRefund);
        return statistics;
    }

    /**
     * 退票统计 DTO（仅在运营模块内部使用）
     */
    public static class CancelStatistics {
        private long pending;
        private long approved;
        private long rejected;
        private BigDecimal totalRefundFare;

        public long getPending() {
            return pending;
        }

        public void setPending(long pending) {
            this.pending = pending;
        }

        public long getApproved() {
            return approved;
        }

        public void setApproved(long approved) {
            this.approved = approved;
        }

        public long getRejected() {
            return rejected;
        }

        public void setRejected(long rejected) {
            this.rejected = rejected;
        }

        public BigDecimal getTotalRefundFare() {
            return totalRefundFare;
        }

        public void setTotalRefundFare(BigDecimal totalRefundFare) {
            this.totalRefundFare = totalRefundFare;
        }
    }
}


