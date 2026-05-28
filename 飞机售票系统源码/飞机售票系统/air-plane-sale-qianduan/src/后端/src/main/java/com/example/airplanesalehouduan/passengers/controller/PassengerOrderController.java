package com.example.airplanesalehouduan.passengers.controller;

import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.entity.Ticket;
import com.example.airplanesalehouduan.passengers.other.OrderRepository;
import com.example.airplanesalehouduan.passengers.other.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Passenger-facing order endpoints.
 *
 * Purpose: Provide a simple passenger refund endpoint that does NOT call any third-party
 * payment provider. When a passenger clicks "退款" in the My Orders page this endpoint
 * will mark the order and its tickets as "已完成" and return a success message.
 *
 * This is intentionally minimal so as not to change existing Alipay-related logic.
 */
@RestController
@RequestMapping("/passenger/orders")
public class PassengerOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Passenger-initiated refund (no third-party integration).
     * Accepts JSON body with either { orderNo: "..." } or { orderId: 123 }.
     * If order is found, set order.status = "已完成" and all related ticket.status = "已完成".
     */
    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> passengerRefund(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (body == null) {
                resp.put("success", false);
                resp.put("message", "请求体不能为空");
                return ResponseEntity.badRequest().body(resp);
            }

            String orderNo = null;
            Long orderId = null;

            if (body.get("orderNo") != null) {
                orderNo = String.valueOf(body.get("orderNo"));
            }
            if (body.get("orderId") != null) {
                try {
                    orderId = Long.valueOf(String.valueOf(body.get("orderId")));
                } catch (Exception ignore) { orderId = null; }
            }

            Optional<Order> orderOpt = Optional.empty();
            if (orderNo != null && !orderNo.trim().isEmpty()) {
                try {
                    orderOpt = orderRepository.findByOrderNo(orderNo);
                } catch (Exception ignore) { orderOpt = Optional.empty(); }
            } else if (orderId != null) {
                try {
                    orderOpt = orderRepository.findById(orderId);
                } catch (Exception ignore) { orderOpt = Optional.empty(); }
            } else {
                resp.put("success", false);
                resp.put("message", "orderNo 或 orderId 至少其一必须提供");
                return ResponseEntity.badRequest().body(resp);
            }

            if (!orderOpt.isPresent()) {
                resp.put("success", false);
                resp.put("message", "未找到订单");
                return ResponseEntity.badRequest().body(resp);
            }

            Order order = orderOpt.get();
            String finalOrderNo = order.getOrderNo() != null ? order.getOrderNo() : (orderNo != null ? orderNo : "");

            // 标记订单为已完成（中文）
            order.setStatus("已完成");
            orderRepository.save(order);

            // 更新该订单下所有机票为已完成（中文）
            try {
                List<Ticket> tickets = ticketRepository.findByOrderNo(finalOrderNo);
                if (tickets != null && !tickets.isEmpty()) {
                    for (Ticket t : tickets) {
                        t.setStatus("已完成");
                    }
                    ticketRepository.saveAll(tickets);
                }
            } catch (Exception ticketEx) {
                ticketEx.printStackTrace();
            }

            resp.put("success", true);
            resp.put("message", "退款成功");
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            ex.printStackTrace();
            resp.put("success", false);
            resp.put("message", "处理退款失败: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(resp);
        }
    }
}


