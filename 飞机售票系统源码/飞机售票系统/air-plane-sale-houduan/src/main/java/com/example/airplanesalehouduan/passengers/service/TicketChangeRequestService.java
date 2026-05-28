package com.example.airplanesalehouduan.passengers.service;

import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.entity.TicketChangeRequest;
import com.example.airplanesalehouduan.passengers.other.OrderRepository;
import com.example.airplanesalehouduan.passengers.other.TicketChangeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 改签申请服务类
 */
@Service
public class TicketChangeRequestService {

    @Autowired
    private TicketChangeRequestRepository repository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.example.airplanesalehouduan.passengers.other.TicketRepository ticketRepository;

    /**
     * 乘客提交改签申请（点击“确认改签”）
     * 1）生成 change_no
     * 2）自动填充 request_time、status=待处理
     * 3）更新原订单状态为“改签”
     */
    @Transactional
    public TicketChangeRequest createRequest(TicketChangeRequest request) throws Exception {
        if (request.getPassengerId() == null) {
            throw new IllegalArgumentException("乘客ID不能为空");
        }
        if (request.getOrderno() == null || request.getOrderno().trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }

        // 生成改签编号
        if (request.getChangeNo() == null || request.getChangeNo().trim().isEmpty()) {
            request.setChangeNo(generateChangeNo());
        }

        // 申请时间
        if (request.getRequestTime() == null) {
            request.setRequestTime(LocalDateTime.now());
        }

        // 默认状态：乘客提交后改签记录为“待审核”，原订单/机票为“待处理”
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            request.setStatus("待审核");
        }

        TicketChangeRequest saved = repository.save(request);

        // 原订单状态改为“待处理”，并把原订单下的机票标记为“待处理”
        Optional<Order> orderOpt = orderRepository.findByOrderNo(request.getOrderno());
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // 简单校验：必须是该乘客的订单
            if (!order.getPassengerId().equals(request.getPassengerId())) {
                throw new Exception("订单不属于当前乘客，无法发起改签");
            }
            order.setStatus("待处理");
            orderRepository.save(order);

            // 将该订单下的所有机票状态置为“待处理”
            try {
                java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> tickets =
                        ticketRepository.findByOrderNo(order.getOrderNo());
                if (tickets != null && !tickets.isEmpty()) {
                    for (com.example.airplanesalehouduan.passengers.entity.Ticket t : tickets) {
                        t.setStatus("待处理");
                    }
                    ticketRepository.saveAll(tickets);
                }
            } catch (Exception ex) {
                // 不阻塞申请流程，但打印错误以便排查
                ex.printStackTrace();
                System.err.println("更新原订单下机票状态为待处理失败: " + ex.getMessage());
            }
        } else {
            throw new Exception("原订单不存在，无法发起改签");
        }
        // 创建新的订单与机票（用于改签后的新订单），并标记为“待审核”
        try {
            Order newOrder = new Order();
            Order oldOrder = orderOpt.get();
            newOrder.setPassengerId(request.getPassengerId());
            newOrder.setOrderNo(generateOrderNo());
            newOrder.setPassengerName(request.getApplicantName() != null && !request.getApplicantName().trim().isEmpty()
                    ? request.getApplicantName()
                    : oldOrder.getPassengerName());
            newOrder.setRoute(request.getNewRoute());
            newOrder.setFlightNo(request.getNewFlightNo());
            newOrder.setDepartureTime(request.getNewDepartureTime());
            newOrder.setArrivalTime(oldOrder.getArrivalTime());
            // 新订单金额：原订单金额 + fareDiff
            java.math.BigDecimal oldAmount = oldOrder.getTotalAmount() != null ? oldOrder.getTotalAmount() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal fareDiff = request.getFareDiff() != null ? request.getFareDiff() : java.math.BigDecimal.ZERO;
            newOrder.setTotalAmount(oldAmount.add(fareDiff));
            newOrder.setStatus("待审核");
            orderRepository.save(newOrder);
            // 将新订单号写回改签记录
            request.setNewOrderNo(newOrder.getOrderNo());
            repository.save(request);

            // 为原订单下每张机票创建对应的新机票并关联到新订单，状态为“待审核”
            try {
                java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> oldTickets = ticketRepository.findByOrderNo(request.getOrderno());
                if (oldTickets != null && !oldTickets.isEmpty()) {
                    java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> createdNewTickets = new java.util.ArrayList<>();
                    for (com.example.airplanesalehouduan.passengers.entity.Ticket oldT : oldTickets) {
                        com.example.airplanesalehouduan.passengers.entity.Ticket newT = new com.example.airplanesalehouduan.passengers.entity.Ticket();
                        newT.setOrderNo(newOrder.getOrderNo());
                        newT.setPassengerId(oldT.getPassengerId());
                        newT.setPassengerName(oldT.getPassengerName());
                        newT.setIdCard(oldT.getIdCard() != null ? oldT.getIdCard() : "");
                        newT.setFlightId(oldT.getFlightId());
                        newT.setFlightNo(request.getNewFlightNo() != null && !request.getNewFlightNo().isEmpty() ? request.getNewFlightNo() : oldT.getFlightNo());
                        newT.setOriginAirport(oldT.getOriginAirport());
                        newT.setDestAirport(oldT.getDestAirport());
                        newT.setRoute(request.getNewRoute() != null && !request.getNewRoute().isEmpty() ? request.getNewRoute() : oldT.getRoute());
                        newT.setDepartureTime(request.getNewDepartureTime() != null ? request.getNewDepartureTime() : oldT.getDepartureTime());
                        newT.setArrivalTime(oldT.getArrivalTime());
                        newT.setPhone(oldT.getPhone() != null ? oldT.getPhone() : "");
                        newT.setSeatNumber(oldT.getSeatNumber());
                        newT.setSeatClass(oldT.getSeatClass());
                        newT.setBasePrice(oldT.getBasePrice() != null ? oldT.getBasePrice() : java.math.BigDecimal.ZERO);
                        newT.setSeatFee(oldT.getSeatFee() != null ? oldT.getSeatFee() : java.math.BigDecimal.ZERO);
                        newT.setDiscountFee(oldT.getDiscountFee() != null ? oldT.getDiscountFee() : java.math.BigDecimal.ZERO);
                        newT.setTotalPrice(oldT.getTotalPrice() != null ? oldT.getTotalPrice().add(fareDiff) : java.math.BigDecimal.ZERO.add(fareDiff));
                        newT.setTicketNo(generateTicketNo());
                        newT.setStatus("待审核");
                        createdNewTickets.add(ticketRepository.save(newT));
                    }
                    if (!createdNewTickets.isEmpty()) {
                        String newTicketNos = createdNewTickets.stream().map(com.example.airplanesalehouduan.passengers.entity.Ticket::getTicketNo).reduce((a, b) -> a + "," + b).orElse("");
                        newOrder.setTicketNo(newTicketNos);
                        orderRepository.save(newOrder);
                    }
                }
            } catch (Exception tEx) {
                tEx.printStackTrace();
                System.err.println("创建改签新机票失败: " + tEx.getMessage());
            }
        } catch (Exception exCreate) {
            exCreate.printStackTrace();
            System.err.println("创建改签新订单失败: " + exCreate.getMessage());
        }

        return saved;
    }

    /**
     * 根据ID获取改签申请
     */
    public Optional<TicketChangeRequest> getById(Long id) {
        return repository.findById(id);
    }

    /**
     * 乘客分页查看自己的改签记录
     */
    public Page<TicketChangeRequest> getByPassengerId(Integer passengerId, int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.trim().isEmpty()) {
            return repository.findByPassengerIdAndStatus(passengerId, status, pageable);
        }
        return repository.findByPassengerId(passengerId, pageable);
    }

    /**
     * 根据订单号查询改签记录
     */
    public List<TicketChangeRequest> getByOrderNo(String orderno) {
        return repository.findByOrderno(orderno);
    }

    public Optional<TicketChangeRequest> getByPassengerIdAndOrderNo(Integer passengerId, String orderno) {
        return repository.findByPassengerIdAndOrderno(passengerId, orderno);
    }

    /**
     * 修改改签申请状态（运营审核）
     * 如果状态变成“通过”，自动创建新的订单信息
     */
    @Transactional
    public TicketChangeRequest updateStatus(Long id, String status, Integer processedBy, String remark, String newOrderNo) throws Exception {
        TicketChangeRequest request = repository.findById(id)
                .orElseThrow(() -> new Exception("改签申请不存在"));

        String normalizedStatus = status != null ? status.trim() : "";

        // 如果之前已是“通过/approved”，避免重复生成订单
        boolean alreadyApproved = "通过".equals(request.getStatus())
                || "approved".equalsIgnoreCase(request.getStatus());

        request.setStatus(normalizedStatus);
        request.setProcessedBy(processedBy);
        request.setRemark(remark);
        request.setProcessedAt(LocalDateTime.now());

        // 如果审核通过，若改签记录尚未包含 newOrderNo 则创建新订单；否则使用已有的 newOrderNo
        boolean shouldCreateNewOrder = ("通过".equals(normalizedStatus) || "approved".equalsIgnoreCase(normalizedStatus))
                && !alreadyApproved
                && (request.getNewOrderNo() == null || request.getNewOrderNo().trim().isEmpty());

        Order createdOrder = null;
        if (shouldCreateNewOrder) {
            createdOrder = createNewOrderForChange(request);
            // 将新订单号写入改签记录的 newOrderNo 字段（优先以后端实际创建的订单号为准）
            if (createdOrder != null && createdOrder.getOrderNo() != null) {
                request.setNewOrderNo(createdOrder.getOrderNo());
            } else if (newOrderNo != null && !newOrderNo.trim().isEmpty()) {
                // 兜底：如果没有生成新订单但前端提供了 newOrderNo，仍写入该值
                request.setNewOrderNo(newOrderNo.trim());
            }
        } else if (newOrderNo != null && !newOrderNo.trim().isEmpty()) {
            // 若未创建新订单但前端提供了 newOrderNo，保存该值
            request.setNewOrderNo(newOrderNo.trim());
        }

        // 保存状态变更（先保存以持久化 newOrderNo）
        TicketChangeRequest saved = repository.save(request);

        // 根据审核结果执行对应的业务状态迁移
        try {
            // 取原订单（可能不存在）
            java.util.Optional<Order> oldOrderOpt = orderRepository.findByOrderNo(request.getOrderno());
            Order oldOrder = oldOrderOpt.orElse(null);

            // 获取新订单（若存在）
            Order newOrder = null;
            if (request.getNewOrderNo() != null && !request.getNewOrderNo().trim().isEmpty()) {
                newOrder = orderRepository.findByOrderNo(request.getNewOrderNo()).orElse(null);
            } else if (createdOrder != null) {
                newOrder = createdOrder;
            }

            if ("通过".equals(normalizedStatus) || "approved".equalsIgnoreCase(normalizedStatus)) {
                // 审批通过：原订单/原机票 -> 已完成；改签表 -> 通过；新机票 -> 已出票；新订单 -> 待支付
                if (oldOrder != null) {
                    try {
                        oldOrder.setStatus("已完成");
                        orderRepository.save(oldOrder);
                        // 更新原订单下所有机票为 已完成
                        java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> oldTickets = ticketRepository.findByOrderNo(oldOrder.getOrderNo());
                        if (oldTickets != null && !oldTickets.isEmpty()) {
                            for (com.example.airplanesalehouduan.passengers.entity.Ticket t : oldTickets) {
                                t.setStatus("已完成");
                            }
                            ticketRepository.saveAll(oldTickets);
                        }
                    } catch (Exception ignore) { }
                }

                if (newOrder != null) {
                    try {
                        newOrder.setStatus("待支付");
                        orderRepository.save(newOrder);
                        // 将新订单下的机票（如果已存在）标记为 已出票
                        java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> newTickets = ticketRepository.findByOrderNo(newOrder.getOrderNo());
                        if (newTickets != null && !newTickets.isEmpty()) {
                            for (com.example.airplanesalehouduan.passengers.entity.Ticket t : newTickets) {
                                t.setStatus("已出票");
                            }
                            ticketRepository.saveAll(newTickets);
                        }
                    } catch (Exception ignore) { }
                }

                // 标记改签记录为 通过（已经设置并保存）
            } else {
                // 拒绝/不通过路径：按描述恢复原订单/机票为 待出行；改签表标为 不通过；新机票标为 驳回；新订单标为 驳回
                if (oldOrder != null) {
                    try {
                        oldOrder.setStatus("待出行");
                        orderRepository.save(oldOrder);
                        java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> oldTickets = ticketRepository.findByOrderNo(oldOrder.getOrderNo());
                        if (oldTickets != null && !oldTickets.isEmpty()) {
                            for (com.example.airplanesalehouduan.passengers.entity.Ticket t : oldTickets) {
                                t.setStatus("待出行");
                            }
                            ticketRepository.saveAll(oldTickets);
                        }
                    } catch (Exception ignore) { }
                }

                if (newOrder != null) {
                    try {
                        newOrder.setStatus("驳回");
                        orderRepository.save(newOrder);
                        java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> newTickets = ticketRepository.findByOrderNo(newOrder.getOrderNo());
                        if (newTickets != null && !newTickets.isEmpty()) {
                            for (com.example.airplanesalehouduan.passengers.entity.Ticket t : newTickets) {
                                t.setStatus("驳回");
                            }
                            ticketRepository.saveAll(newTickets);
                        }
                    } catch (Exception ignore) { }
                }
                // 将改签记录状态统一为“不通过”（如果传入的是“拒绝”，将其映射为“不通过”）
                if ("拒绝".equals(normalizedStatus) || "rejected".equalsIgnoreCase(normalizedStatus)) {
                    saved.setStatus("不通过");
                    repository.save(saved);
                }
            }
        } catch (Exception transEx) {
            transEx.printStackTrace();
        }

        return saved;

    }

    /**
     * 兼容旧调用：不带 newOrderNo 参数的 updateStatus 重载
     */
    @Transactional
    public TicketChangeRequest updateStatus(Long id, String status, Integer processedBy, String remark) throws Exception {
        return updateStatus(id, status, processedBy, remark, null);
    }

    /**
     * 为通过的改签申请创建新的订单
     * 根据数据表orders的字段一一对应设置
     */
    private Order createNewOrderForChange(TicketChangeRequest request) throws Exception {
        Optional<Order> oldOrderOpt = orderRepository.findByOrderNo(request.getOrderno());
        if (oldOrderOpt.isEmpty()) {
            throw new Exception("原订单不存在，无法创建新订单");
        }

        Order oldOrder = oldOrderOpt.get();

        Order newOrder = new Order();

        // 根据orders表字段一一对应设置
        // passenger_id: 乘客ID（关联users表）
        newOrder.setPassengerId(request.getPassengerId());

        // order_no: 订单号（唯一）
        newOrder.setOrderNo(generateOrderNo());

        // passenger_name: 姓名（冗余字段，便于查询）
        newOrder.setPassengerName(request.getApplicantName() != null && !request.getApplicantName().trim().isEmpty()
                ? request.getApplicantName()
                : oldOrder.getPassengerName());

        // route: 航线（格式：出发地 → 目的地）
        newOrder.setRoute(request.getNewRoute());

        // flight_no: 航班号
        newOrder.setFlightNo(request.getNewFlightNo());

        // ticket_no: 机票号（从改签申请中获取，如果为空则设为null）
        newOrder.setTicketNo(request.getTicketNo() != null && !request.getTicketNo().trim().isEmpty()
                ? request.getTicketNo()
                : null);

        // departure_time: 起飞时间
        newOrder.setDepartureTime(request.getNewDepartureTime());
        // arrival_time: 回退为原订单到达时间（兼容旧流程）
        newOrder.setArrivalTime(oldOrder.getArrivalTime());

        // total_amount: 订单总金额
        // 计算逻辑：原票价 + fare_diff（不包含改签费）
        BigDecimal oldAmount = oldOrder.getTotalAmount() != null
                ? oldOrder.getTotalAmount()
                : BigDecimal.ZERO;
        BigDecimal fareDiff = request.getFareDiff() != null
                ? request.getFareDiff()
                : BigDecimal.ZERO;

        // 新订单金额 = 原票价 + fare_diff
        newOrder.setTotalAmount(oldAmount.add(fareDiff));

        // status: 订单状态（改签通过后新订单直接标记为"待出行"）
        newOrder.setStatus("待出行");

        // created_at 和 updated_at 由 @CreationTimestamp 和 @UpdateTimestamp 自动设置

        orderRepository.save(newOrder);
        return newOrder;
    }

    /**
     * 生成改签编号
     */
    private String generateChangeNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timePart = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(900) + 100;
        return "TCR" + timePart + random;
    }

    /**
     * 生成新订单号
     * 例如：ORD202511151620123
     */
    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timePart = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(900) + 100;
        return "ORD" + timePart + random;
    }

    /**
     * 生成用于改签新机票的票号
     * 格式：TKT + 时间戳 + 随机数
     */
    private String generateTicketNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timePart = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(900000) + 100000;
        return "TKT" + timePart + random;
    }

    /**
     * 管理员查询改签申请列表（支持多条件筛选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param status 状态筛选（可选）
     * @param orderNo 订单号筛选（可选，模糊查询）
     * @param applicantName 申请人姓名筛选（可选，模糊查询）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 改签申请分页结果
     */
    public Page<TicketChangeRequest> getAdminChangeRequestList(
            int page,
            int size,
            String status,
            String orderNo,
            String applicantName,
            String startDate,
            String endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestTime"));

        Specification<TicketChangeRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 状态筛选：当传入“待处理”时，包含两种后端状态 '待处理' 和 '待审核'
            if (status != null && !status.trim().isEmpty()) {
                String s = status.trim();
                if ("待处理".equals(s)) {
                    predicates.add(root.get("status").in("待处理", "待审核"));
                } else {
                    predicates.add(cb.equal(root.get("status"), s));
                }
            }

            // 订单号搜索
            if (orderNo != null && !orderNo.trim().isEmpty()) {
                predicates.add(cb.like(root.get("orderno"), "%" + orderNo + "%"));
            }

            // 申请人姓名搜索
            if (applicantName != null && !applicantName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("applicantName")), "%" + applicantName.toLowerCase() + "%"));
            }

            // 日期范围筛选
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
                    predicates.add(cb.greaterThanOrEqualTo(root.get("requestTime"), start));
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
                    predicates.add(cb.lessThanOrEqualTo(root.get("requestTime"), end));
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable);
    }

    /**
     * 批量批准改签申请
     * @param ids 改签申请ID列表
     * @param processedBy 处理人ID
     * @param remark 备注
     * @return 处理结果
     */
    @Transactional
    public int batchApprove(List<Long> ids, Integer processedBy, String remark) throws Exception {
        int count = 0;
        for (Long id : ids) {
            try {
                updateStatus(id, "通过", processedBy, remark);
                count++;
            } catch (Exception e) {
                // 记录错误但继续处理其他申请
                System.err.println("批准改签申请失败，ID: " + id + ", 错误: " + e.getMessage());
            }
        }
        return count;
    }

    /**
     * 批量拒绝改签申请
     * @param ids 改签申请ID列表
     * @param processedBy 处理人ID
     * @param remark 拒绝原因（作为remark）
     * @return 处理结果
     */
    @Transactional
    public int batchReject(List<Long> ids, Integer processedBy, String remark) throws Exception {
        int count = 0;
        for (Long id : ids) {
            try {
                updateStatus(id, "拒绝", processedBy, remark);
                count++;
            } catch (Exception e) {
                // 记录错误但继续处理其他申请
                System.err.println("拒绝改签申请失败，ID: " + id + ", 错误: " + e.getMessage());
            }
        }
        return count;
    }

    /**
     * 获取统计数据
     * @return 统计数据Map
     */
    public java.util.Map<String, Object> getStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("pending", repository.countByStatus("待处理"));
        stats.put("approved", repository.countByStatus("通过"));
        stats.put("rejected", repository.countByStatus("拒绝"));
        return stats;
    }
}


