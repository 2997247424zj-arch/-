package com.example.airplanesalehouduan.passengers.service;

import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.entity.TicketCancelRequest;
import com.example.airplanesalehouduan.passengers.other.OrderRepository;
import com.example.airplanesalehouduan.passengers.other.TicketCancelRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

/**
 * 取消机票申请服务类
 */
@Service
public class TicketCancelRequestService {

    @Autowired
    private TicketCancelRequestRepository repository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.example.airplanesalehouduan.passengers.other.TicketRepository ticketRepository;

    /**
     * 乘客提交取消申请
     * 1）生成 cancel_no
     * 2）自动填充 request_time、status=待处理
     * 3）根据订单金额计算退款金额（若前端未传入）
     * 4）更新原订单状态为“取消-待处理”
     */
    @Transactional
    public TicketCancelRequest createRequest(TicketCancelRequest request) throws Exception {
        if (request.getPassengerId() == null) {
            throw new IllegalArgumentException("乘客ID不能为空");
        }
        if (request.getOrderno() == null || request.getOrderno().trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        if (request.getFlightNo() == null || request.getFlightNo().trim().isEmpty()) {
            throw new IllegalArgumentException("航班号不能为空");
        }
        if (request.getRoute() == null || request.getRoute().trim().isEmpty()) {
            throw new IllegalArgumentException("航线不能为空");
        }
        if (request.getDepartureTime() == null) {
            throw new IllegalArgumentException("起飞时间不能为空");
        }

        // 生成取消编号
        if (request.getCancelNo() == null || request.getCancelNo().trim().isEmpty()) {
            request.setCancelNo(generateCancelNo());
        }

        // 申请时间
        if (request.getRequestTime() == null) {
            request.setRequestTime(LocalDateTime.now());
        }

        // 默认状态：待处理
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            request.setStatus("待处理");
        }

        // 默认机票号为空
        if (request.getTicketNo() != null && request.getTicketNo().trim().isEmpty()) {
            request.setTicketNo(null);
        }

        // 处理手续费和退款金额
        BigDecimal cancelFee = request.getCancelFee() != null ? request.getCancelFee() : BigDecimal.ZERO;
        BigDecimal refundFare = request.getRefundFare();

        Optional<Order> orderOpt = orderRepository.findByOrderNo(request.getOrderno());
        if (orderOpt.isEmpty()) {
            throw new Exception("原订单不存在，无法发起取消");
        }

        Order order = orderOpt.get();
        // 校验订单归属
        if (!order.getPassengerId().equals(request.getPassengerId())) {
            throw new Exception("订单不属于当前乘客，无法发起取消");
        }

        if (refundFare == null) {
            BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            refundFare = totalAmount.subtract(cancelFee);
            if (refundFare.compareTo(BigDecimal.ZERO) < 0) {
                refundFare = BigDecimal.ZERO;
            }
            request.setRefundFare(refundFare);
        }

        TicketCancelRequest saved = repository.save(request);

        // 原订单状态改为“待审核”，并将订单下机票状态置为“待审核”
        order.setStatus("待审核");
        orderRepository.save(order);
        try {
            java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> tickets = ticketRepository.findByOrderNo(order.getOrderNo());
            if (tickets != null && !tickets.isEmpty()) {
                for (com.example.airplanesalehouduan.passengers.entity.Ticket t : tickets) {
                    t.setStatus("待审核");
                }
                ticketRepository.saveAll(tickets);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return saved;
    }

    /**
     * 生成取消编号
     */
    private String generateCancelNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timePart = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(900) + 100;
        // TCA = Ticket Cancel Application
        return "TCA" + timePart + random;
    }
}


