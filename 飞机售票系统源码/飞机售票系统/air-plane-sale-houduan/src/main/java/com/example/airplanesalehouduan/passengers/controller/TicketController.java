package com.example.airplanesalehouduan.passengers.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.example.airplanesalehouduan.passengers.entity.TicketDto;
import com.example.airplanesalehouduan.passengers.service.TicketService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/passenger/tickets")
public class TicketController {
    @Autowired
    private TicketService ticketService;
    private DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private Map<String, Object> ticketToMap(TicketDto t) {
        Map<String, Object> m = new HashMap<>();
        if (t == null) return m;
        m.put("id", t.getId());
        m.put("order_no", t.getOrderNo());
        m.put("passenger_id", t.getPassengerId());
        m.put("passenger_name", t.getPassengerName());
        m.put("id_card", t.getIdCard());
        m.put("phone", t.getPhone());
        m.put("flight_id", t.getFlightId());
        m.put("flight_no", t.getFlightNo());
        m.put("origin_airport", t.getOriginAirport());
        m.put("dest_airport", t.getDestAirport());
        m.put("route", t.getRoute());
        m.put("departure_time", t.getDepartureTime() == null ? null : t.getDepartureTime().format(dtf));
        m.put("arrival_time", t.getArrivalTime() == null ? null : t.getArrivalTime().format(dtf));
        m.put("seat_number", t.getSeatNumber());
        m.put("seat_class", t.getSeatClass());
        m.put("base_price", t.getBasePrice());
        m.put("seat_fee", t.getSeatFee());
        m.put("discount_fee", t.getDiscountFee());
        m.put("total_price", t.getTotalPrice());
        m.put("ticket_no", t.getTicketNo());
        m.put("status", t.getStatus());
        return m;
    }

    // 支持分页和按机票号搜索，每次必须传 passengerId（与当前登录账户一致）
    @GetMapping
    public ResponseEntity<?> listTickets(
            @RequestParam("passengerId") Integer passengerId,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "ticketNo", required = false) String ticketNo
    ) {
        if (passengerId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "passengerId 为必填参数"));
        }
        List<TicketDto> list = ticketService.listTickets(passengerId, page, size, ticketNo);
        int total = ticketService.countTickets(passengerId, ticketNo);
        Map<String, Object> data = new HashMap<>();
        List<Map<String,Object>> mapped = list.stream().map(this::ticketToMap).collect(Collectors.toList());
        data.put("list", mapped);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        data.put("totalPages", (int)Math.ceil((double)total / (double)size));
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "查询成功");
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getTicketDetail(
            @PathVariable("ticketId") String ticketId,
            @RequestParam("passengerId") Integer passengerId
    ) {
        if (passengerId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "passengerId 为必填参数"));
        }
        TicketDto t = ticketService.getTicketDetail(ticketId, passengerId);
        if (t == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "未找到机票"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "查询成功", "data", ticketToMap(t)));
    }

    // 返回可下载的 URL（前端将打开该链接或后续扩展为直接返回pdf blob）
    @GetMapping("/{ticketId}/print")
    public ResponseEntity<?> printTicket(
            @PathVariable("ticketId") String ticketId,
            @RequestParam("passengerId") Integer passengerId
    ) {
        if (passengerId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "passengerId 为必填参数"));
        }
        TicketDto t = ticketService.getTicketDetail(ticketId, passengerId);
        if (t == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "未找到机票"));
        }
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset='utf-8'><title>机票 ")
                .append(t.getTicketNo() == null ? "" : t.getTicketNo())
                .append("</title><style>body{font-family:Arial,Helvetica,sans-serif;padding:20px} .ticket{border:1px solid #333;padding:12px;width:700px;} h1{font-size:18px}</style></head><body>");
        html.append("<div class='ticket'>");
        html.append("<h1>机票 - ").append(t.getTicketNo() == null ? "-" : t.getTicketNo()).append("</h1>");
        html.append("<p><strong>乘客：</strong>").append(t.getPassengerName() == null ? "-" : t.getPassengerName()).append("</p>");
        html.append("<p><strong>身份证号：</strong>").append(t.getIdCard() == null ? "-" : t.getIdCard()).append("</p>");
        html.append("<p><strong>电话：</strong>").append(t.getPhone() == null ? "-" : t.getPhone()).append("</p>");
        html.append("<p><strong>航班号：</strong>").append(t.getFlightNo() == null ? "-" : t.getFlightNo()).append("</p>");
        html.append("<p><strong>航线：</strong>").append(t.getRoute() == null ? "-" : t.getRoute()).append("</p>");
        html.append("<p><strong>起飞：</strong>").append(t.getDepartureTime() == null ? "-" : t.getDepartureTime().toString()).append("</p>");
        html.append("<p><strong>到达：</strong>").append(t.getArrivalTime() == null ? "-" : t.getArrivalTime().toString()).append("</p>");
        html.append("<p><strong>座位：</strong>").append(t.getSeatNumber() == null ? "-" : t.getSeatNumber()).append("</p>");
        html.append("<p><strong>舱位：</strong>").append(t.getSeatClass() == null ? "-" : t.getSeatClass()).append("</p>");
        html.append("<p><strong>总价：</strong>").append(t.getTotalPrice() == null ? "-" : "¥" + t.getTotalPrice().toString()).append("</p>");
        html.append("<hr/>");
        html.append("<p>请使用浏览器的打印功能进行打印。</p>");
        html.append("</div>");
        html.append("<script>window.onload=function(){window.print();}</script>");
        html.append("</body></html>");

        String encoded = Base64.getEncoder().encodeToString(html.toString().getBytes(StandardCharsets.UTF_8));
        String dataUrl = "data:text/html;base64," + encoded;
        return ResponseEntity.ok(Map.of("success", true, "message", "生成成功", "data", Map.of("url", dataUrl)));
    }
}


