package com.example.airplanesalehouduan.passengers.controller;

import com.example.airplanesalehouduan.passengers.entity.TicketCancelRequest;
import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.entity.Ticket;
import com.example.airplanesalehouduan.passengers.other.TicketCancelRequestRepository;
import com.example.airplanesalehouduan.passengers.other.OrderRepository;
import com.example.airplanesalehouduan.passengers.other.TicketRepository;
import com.example.airplanesalehouduan.passengers.service.TicketCancelRequestService;
import com.example.airplanesalehouduan.Login.repository.UserRepository;
import com.example.airplanesalehouduan.Login.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * 取消机票申请控制器（普通乘客使用）
 */
@RestController
@RequestMapping("/api/ticket-cancel-requests")
public class TicketCancelRequestController {

    @Autowired
    private TicketCancelRequestService service;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TicketCancelRequestRepository ticketCancelRequestRepository;

    /**
     * 乘客提交取消申请（点击“确认取消”）
     * 前端传入：orderno、flightNo、route、departureTime、cancelFee、refundFare、reason 等
     * 后端自动生成 cancelNo、requestTime、status=待处理，并将原订单状态改为“取消-待处理”
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRequest(@RequestBody Map<String, Object> body) {
        try {
            // 先解析订单号，尽量使用订单信息补全缺失字段
            String orderno = asString(body.get("orderno"));
            if (orderno == null || orderno.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("订单号不能为空"));
            }

            // 尝试从订单补全 passengerId / flightNo / route / departureTime / applicantName
            Integer passengerId = parseInteger(body.get("passengerId"));
            String flightNo = asString(body.get("flightNo"));
            String route = asString(body.get("route"));
            LocalDateTime departureTime = parseDateTime(asString(body.get("departureTime")));
            String applicantName = asString(body.get("applicantName"));

            // 尝试按 orderno 找到订单以补全信息
            // Simpler: fetch orderOpt to a variable to use for filling
            Order order = null;
            try {
                java.util.Optional<Order> orderOpt = orderRepository.findByOrderNo(orderno);
                if (orderOpt.isPresent()) order = orderOpt.get();
            } catch (Exception ignore) {}

            if (passengerId == null && order != null) passengerId = order.getPassengerId();
            if ((flightNo == null || flightNo.trim().isEmpty()) && order != null) flightNo = order.getFlightNo();
            if ((route == null || route.trim().isEmpty()) && order != null) route = order.getRoute();
            if (departureTime == null && order != null) departureTime = order.getDepartureTime();
            if ((applicantName == null || applicantName.trim().isEmpty()) && passengerId != null) {
                try {
                    java.util.Optional<User> uopt = userRepository.findById(passengerId);
                    if (uopt != null && uopt.isPresent() && uopt.get().getRealName() != null) {
                        applicantName = uopt.get().getRealName();
                    } else if (order != null && order.getPassengerName() != null) {
                        applicantName = order.getPassengerName();
                    }
                } catch (Exception ignore) {
                    if (order != null && order.getPassengerName() != null) applicantName = order.getPassengerName();
                }
            }

            // 最终校验必须字段：passengerId、flightNo、route、departureTime
            if (passengerId == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("乘客ID不能为空"));
            }
            if (flightNo == null || flightNo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("航班号不能为空"));
            }
            if (route == null || route.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("航线不能为空"));
            }
            if (departureTime == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("起飞时间格式不正确"));
            }

            TicketCancelRequest request = new TicketCancelRequest();
            request.setPassengerId(passengerId);
            request.setOrderno(orderno);
            request.setApplicantName(applicantName);
            request.setFlightNo(flightNo);
            request.setRoute(route);
            request.setDepartureTime(departureTime);
            request.setReason(asString(body.get("reason")));

            // 尝试从请求体获取机票号，如果前端提供则直接使用；否则尝试从 tickets 表中按订单号收集机票号（逗号分隔）
            String ticketNoFromBody = asString(body.get("ticketNo"));
            if (ticketNoFromBody != null && !ticketNoFromBody.trim().isEmpty()) {
                request.setTicketNo(ticketNoFromBody.trim());
            } else {
                try {
                    java.util.List<Ticket> ticketsForOrder = ticketRepository.findByOrderNo(orderno);
                    if (ticketsForOrder != null && !ticketsForOrder.isEmpty()) {
                        java.util.List<String> ticketNos = new java.util.ArrayList<>();
                        for (Ticket t : ticketsForOrder) {
                            if (t.getTicketNo() != null && !t.getTicketNo().trim().isEmpty()) {
                                ticketNos.add(t.getTicketNo().trim());
                            }
                        }
                        if (!ticketNos.isEmpty()) {
                            // 若一个订单存在多张机票，则以逗号分隔保存（展示用）
                            request.setTicketNo(String.join(",", ticketNos));
                        }
                    }
                } catch (Exception ignore) {}
            }

            // 金额字段
            java.math.BigDecimal cancelFee = parseDecimal(body.get("cancelFee"));
            if (cancelFee == null) cancelFee = java.math.BigDecimal.ZERO;
            request.setCancelFee(cancelFee);
            request.setRefundFare(parseDecimal(body.get("refundFare")));

            // 设置为待审核（前端语义：提交后为待审核）
            request.setStatus("待审核");

            TicketCancelRequest saved = service.createRequest(request);

            // 同步更新订单：total_amount -= cancelFee（不得为负），并设置订单状态为“待审核”
            try {
                if (order == null) {
                    java.util.Optional<Order> ordOpt = orderRepository.findByOrderNo(orderno);
                    if (ordOpt.isPresent()) order = ordOpt.get();
                }
                if (order != null) {
                    java.math.BigDecimal orig = order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO;
                    java.math.BigDecimal newTotal = orig.subtract(cancelFee);
                    if (newTotal.compareTo(java.math.BigDecimal.ZERO) < 0) newTotal = java.math.BigDecimal.ZERO;
                    order.setTotalAmount(newTotal);
                    order.setStatus("待审核");
                    orderRepository.save(order);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // 更新该订单下的所有机票状态为“待审核”
            try {
                java.util.List<Ticket> tickets = ticketRepository.findByOrderNo(orderno);
                if (tickets != null && !tickets.isEmpty()) {
                    for (Ticket t : tickets) {
                        t.setStatus("待审核");
                    }
                    ticketRepository.saveAll(tickets);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // 确保 cancel request 的 status 为 “待审核”（service 默认可能设置为“待处理”）
            try {
                if (saved != null) {
                    saved.setStatus("待审核");
                    ticketCancelRequestRepository.save(saved);
                }
            } catch (Exception ignore) {}

            return ResponseEntity.ok(createSuccessResponse("取消申请提交成功", saved));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(createErrorResponse(ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("创建取消申请失败: " + e.getMessage()));
        }
    }

    /**
     * 乘客确认已完成退款（点击我的订单的退款按钮后的回调）
     * POST /api/ticket-cancel-requests/orders/{orderNo}/confirm-refund
     * 将对应订单和机票状态均设置为“已完成”
     */
    @PostMapping("/orders/{orderNo}/confirm-refund")
    public ResponseEntity<Map<String, Object>> confirmRefundCompleted(@PathVariable String orderNo) {
        try {
            if (orderNo == null || orderNo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("订单号不能为空"));
            }

            java.util.Optional<Order> ordOpt = orderRepository.findByOrderNo(orderNo);
            if (ordOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("订单不存在"));
            }
            Order order = ordOpt.get();
            // 将订单标为已完成
            order.setStatus("已完成");
            orderRepository.save(order);

            // 将订单下所有机票标为已完成
            java.util.List<Ticket> tickets = ticketRepository.findByOrderNo(orderNo);
            if (tickets != null && !tickets.isEmpty()) {
                for (Ticket t : tickets) {
                    t.setStatus("已完成");
                }
                ticketRepository.saveAll(tickets);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "退款已确认，订单与机票已标记为已完成");
            resp.put("orderNo", orderNo);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("确认退款失败: " + e.getMessage()));
        }
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private java.math.BigDecimal parseDecimal(Object value) {
        if (value == null) return null;
        try {
            return new java.math.BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 尝试解析多种格式的日期时间（"yyyy-MM-dd HH:mm" 或 "yyyy-MM-dd'T'HH:mm:ss" 等）
     */
    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        try {
            // 统一替换空格为T，便于解析
            if (text.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}(:\\d{2})?")) {
                text = text.replace(' ', 'T');
            }
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", message);
        resp.put("data", data);
        return resp;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", false);
        resp.put("message", message);
        resp.put("data", null);
        return resp;
    }
}


