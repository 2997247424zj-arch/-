package com.example.airplanesalehouduan.admin.service;

import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.entity.TicketCancelRequest;
import com.example.airplanesalehouduan.passengers.other.TicketCancelRequestRepository;
import com.example.airplanesalehouduan.passengers.other.OrderRepository;
import com.example.airplanesalehouduan.passengers.other.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统管理员退票申请管理服务
 * 提供退票申请的查询、审核、统计等功能
 */
@Service
public class AdminTicketCancelRequestService {

    @Autowired
    private TicketCancelRequestRepository repository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TicketRepository ticketRepository;

    /**
     * 获取退票申请列表（系统管理员用，支持多条件筛选）
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param status 状态筛选（可选：待处理、通过、拒绝）
     * @param orderNo 订单号（可选，模糊查询）
     * @param applicantName 申请人姓名（可选，模糊查询）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 分页结果
     */
    public Page<TicketCancelRequest> getAdminCancelRequestList(
            Integer page, Integer size, String status, String orderNo,
            String applicantName, String startDate, String endDate) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<TicketCancelRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 状态筛选：将“待处理”映射为同时包含 '待处理' 和 '待审核'
            if (status != null && !status.trim().isEmpty()) {
                String s = status.trim();
                if ("待处理".equals(s)) {
                    predicates.add(root.get("status").in("待处理", "待审核"));
                } else {
                    predicates.add(cb.equal(root.get("status"), s));
                }
            }

            // 订单号模糊查询
            if (orderNo != null && !orderNo.trim().isEmpty()) {
                predicates.add(cb.like(root.get("orderno"), "%" + orderNo + "%"));
            }

            // 申请人姓名模糊查询
            if (applicantName != null && !applicantName.trim().isEmpty()) {
                predicates.add(cb.like(root.get("applicantName"), "%" + applicantName + "%"));
            }

            // 申请日期范围筛选
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDate start = LocalDate.parse(startDate);
                    LocalDateTime startDateTime = start.atStartOfDay();
                    predicates.add(cb.greaterThanOrEqualTo(root.get("requestTime"), startDateTime));
                } catch (Exception e) {
                    // 忽略日期解析错误
                }
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDate end = LocalDate.parse(endDate);
                    LocalDateTime endDateTime = end.atTime(23, 59, 59);
                    predicates.add(cb.lessThanOrEqualTo(root.get("requestTime"), endDateTime));
                } catch (Exception e) {
                    // 忽略日期解析错误
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable);
    }

    /**
     * 根据ID获取退票申请详情
     */
    public Optional<TicketCancelRequest> getById(Long id) {
        return repository.findById(id);
    }

    /**
     * 批准退票申请
     *
     * @param id 申请ID
     * @param processedBy 处理人ID（系统管理员ID）
     * @param remark 备注
     * @return 更新后的申请
     */
    @Transactional
    public TicketCancelRequest approveCancelRequest(Long id, Integer processedBy, String remark) {
        TicketCancelRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("退票申请不存在"));

        if (!"待处理".equals(request.getStatus())) {
            throw new RuntimeException("只能处理待处理的申请");
        }

        request.setStatus("通过");
        request.setProcessedBy(processedBy);
        request.setProcessedAt(LocalDateTime.now());
        if (remark != null && !remark.trim().isEmpty()) {
            request.setRemark(remark);
        }

        // 更新原订单与机票为“待退款”
        try {
            // 更新订单状态
            java.util.Optional<Order> ordOpt = orderRepository.findByOrderNo(request.getOrderno());
            if (ordOpt.isPresent()) {
                Order order = ordOpt.get();
                order.setStatus("待退款");
                orderRepository.save(order);

                // 更新该订单下的所有机票为“待退款”
                java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> tickets = ticketRepository.findByOrderNo(order.getOrderNo());
                if (tickets != null && !tickets.isEmpty()) {
                    for (com.example.airplanesalehouduan.passengers.entity.Ticket t : tickets) {
                        t.setStatus("待退款");
                    }
                    ticketRepository.saveAll(tickets);
                }
            }
        } catch (Exception ex) {
            // 不阻塞主要流程，记录日志以便排查
            ex.printStackTrace();
        }

        return repository.save(request);
    }

    /**
     * 拒绝退票申请
     *
     * @param id 申请ID
     * @param processedBy 处理人ID（系统管理员ID）
     * @param reason 拒绝原因（会写入remark字段）
     * @return 更新后的申请
     */
    @Transactional
    public TicketCancelRequest rejectCancelRequest(Long id, Integer processedBy, String reason) {
        TicketCancelRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("退票申请不存在"));

        if (!"待处理".equals(request.getStatus())) {
            throw new RuntimeException("只能处理待处理的申请");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("拒绝原因不能为空");
        }

        // 标记为不通过（统一使用“不通过”）
        request.setStatus("不通过");
        request.setProcessedBy(processedBy);
        request.setProcessedAt(LocalDateTime.now());
        request.setRemark(reason);

        // 恢复原订单与机票状态为“待出行”
        try {
            java.util.Optional<Order> ordOpt = orderRepository.findByOrderNo(request.getOrderno());
            if (ordOpt.isPresent()) {
                Order order = ordOpt.get();
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

        return repository.save(request);
    }

    /**
     * 批量批准退票申请
     *
     * @param ids 申请ID列表
     * @param processedBy 处理人ID（系统管理员ID）
     * @param remark 备注
     * @return 成功处理的记录数
     */
    @Transactional
    public int batchApprove(List<Long> ids, Integer processedBy, String remark) {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Long id : ids) {
            try {
                // 使用单条批准逻辑，保证订单/机票状态同步
                approveCancelRequest(id, processedBy, remark);
                count++;
            } catch (Exception e) {
                // 忽略单个记录的错误，继续处理其他记录
            }
        }

        return count;
    }

    /**
     * 批量拒绝退票申请
     *
     * @param ids 申请ID列表
     * @param processedBy 处理人ID（系统管理员ID）
     * @param reason 拒绝原因
     * @return 成功处理的记录数
     */
    @Transactional
    public int batchReject(List<Long> ids, Integer processedBy, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("拒绝原因不能为空");
        }

        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Long id : ids) {
            try {
                // 使用单条拒绝逻辑，保证订单/机票状态同步
                rejectCancelRequest(id, processedBy, reason);
                count++;
            } catch (Exception e) {
                // 忽略单个记录的错误，继续处理其他记录
            }
        }

        return count;
    }

    /**
     * 获取退票申请统计数据
     *
     * @return 统计数据Map，包含：
     *   - pending: 待处理数量
     *   - approved: 已通过数量
     *   - rejected: 已拒绝数量
     *   - totalRefundFare: 已通过申请的退款总额
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 统计各状态数量
        long pending = repository.countByStatus("待处理");
        long approved = repository.countByStatus("通过");
        long rejected = repository.countByStatus("拒绝");

        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);

        // 计算已通过申请的退款总额
        Page<TicketCancelRequest> approvedPage = repository.findAll(
                (root, query, cb) -> cb.equal(root.get("status"), "通过"),
                PageRequest.of(0, Integer.MAX_VALUE));

        BigDecimal totalRefundFare = approvedPage.getContent().stream()
                .map(req -> req.getRefundFare() != null ? req.getRefundFare() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (sum, fee) -> sum.add(fee));

        stats.put("totalRefundFare", totalRefundFare.doubleValue());

        return stats;
    }
}

