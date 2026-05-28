package com.example.airplanesalehouduan.pay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.other.OrderRepository;
import com.example.airplanesalehouduan.passengers.service.BookingService;
import com.example.airplanesalehouduan.passengers.service.BaggageManagementService;
import com.example.airplanesalehouduan.passengers.entity.BaggageManagement;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.stream.Collectors;
import java.util.Objects;
import java.sql.Timestamp;
import com.example.airplanesalehouduan.Login.repository.UserRepository;
import com.example.airplanesalehouduan.Login.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * AliPay 支付回调与发起控制器
 * 说明：
 * - 保持原有支付宝查询逻辑不变
 * - 将 out_trade_no 使用为临时订单号，支付成功后才创建订单
 */
@Controller
@RequestMapping("/api/alipay")
public class AliPayController {
    @Autowired
    private com.example.airplanesalehouduan.pay.PayUtil payUtil;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BaggageManagementService baggageManagementService;
    @Autowired
    private com.example.airplanesalehouduan.passengers.other.TicketRepository ticketRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserRepository userRepository;

    // 临时存储预订信息，key为临时订单号，value为预订请求信息
    private final ConcurrentHashMap<String, BookingRequest> pendingBookings = new ConcurrentHashMap<>();
    // 临时存储行李登记信息（支付成功后再创建行李记录）
    private final ConcurrentHashMap<String, BaggageRequest> pendingBaggage = new ConcurrentHashMap<>();
    // 临时存储改签请求（支付成功后再将订单/机票状态设置为待审核）
    private final ConcurrentHashMap<String, RescheduleRequest> pendingReschedules = new ConcurrentHashMap<>();
    // 临时存储取消请求（支付成功后再将订单/机票状态设置为待审核）
    private final ConcurrentHashMap<String, CancelRequest> pendingCancels = new ConcurrentHashMap<>();

    /**
     * 发起支付宝支付（用于"确认预订"后跳转到支付宝）
     * 前端应传入完整的预订信息（乘客、航班、金额等），服务器将：
     * 1) 生成临时订单号，将预订信息暂存到内存中；
     * 2) 使用临时订单号作为 out_trade_no 传给支付宝发起支付；
     * 3) 支付完成后回调接口从临时存储中获取预订信息，创建订单和机票，状态设为"待出行"。
     */
    @ResponseBody
    @PostMapping("/pay")
    public String alipay(@RequestBody BookingRequest bookingRequest) throws AlipayApiException {
        try {
            // 1) 生成临时订单号
            String tempOrderNo = generateTempOrderNo();

            // 2) 将预订信息暂存到内存中
            pendingBookings.put(tempOrderNo, bookingRequest);

            // 3) 使用临时订单号作为 out_trade_no 发起支付宝支付
            float totalAmount = bookingRequest.getTotalAmount().floatValue();
            String subject = "航班预订 - " + tempOrderNo;
            return payUtil.sendRequestToAlipay(tempOrderNo, totalAmount, subject);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("发起支付失败: " + ex.getMessage());
        }
    }

    /**
     * 发起行李登记支付（乘客登记行李后跳转支付宝支付）
     * 前端应传入 passengerId、baggage（行李数据）和 totalAmount（合计费用）
     */
    @ResponseBody
    @PostMapping("/payBaggage")
    public String payBaggage(@RequestBody BaggageRequest baggageRequest) throws AlipayApiException {
        try {
            // 1) 生成临时订单号
            String tempOrderNo = generateTempOrderNo();

            // 2) 将行李信息暂存到内存中
            pendingBaggage.put(tempOrderNo, baggageRequest);

            // 3) 发起支付宝支付，使用临时订单号作为 out_trade_no
            // 根据需求：最终支付金额应当只包含“行李费用 (baggageFee)”，不包含超重/超规费用（excessFee）。
            java.math.BigDecimal computedAmount = java.math.BigDecimal.ZERO;
            Map<String, Object> bagMapForCalc = baggageRequest.getBaggage();
            boolean foundBaggageFee = false;
            if (bagMapForCalc != null) {
                Object bf = bagMapForCalc.get("baggageFee");
                if (bf instanceof Number) {
                    computedAmount = computedAmount.add(java.math.BigDecimal.valueOf(((Number) bf).doubleValue()));
                    foundBaggageFee = true;
                } else if (bf instanceof String && !((String) bf).isEmpty()) {
                    computedAmount = computedAmount.add(new java.math.BigDecimal((String) bf));
                    foundBaggageFee = true;
                }
            }
            // 如果前端未传 baggageFee，则回退使用 baggageRequest.totalAmount（兼容旧前端）
            if (!foundBaggageFee && baggageRequest.getTotalAmount() != null) {
                computedAmount = computedAmount.add(baggageRequest.getTotalAmount());
            }
            float totalAmount = computedAmount.floatValue();
            String subject = "行李登记 - " + tempOrderNo;
            return payUtil.sendRequestToAlipay(tempOrderNo, totalAmount, subject);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("发起行李支付失败: " + ex.getMessage());
        }
    }

    /**
     * 发起改签支付（乘客提交改签并支付改签费/差价）
     * 前端应传入 orderNo、changeFee、priceDiff、reason、newFlight 等信息
     * 支付成功后将把对应订单与机票状态更新为 "待审核"
     */
    @ResponseBody
    @PostMapping("/payReschedule")
    public String payReschedule(@RequestBody RescheduleRequest rescheduleRequest) throws AlipayApiException {
        try {
            String tempOrderNo = generateTempOrderNo();
            pendingReschedules.put(tempOrderNo, rescheduleRequest);

            // 计算应付金额：改签服务费 + 票价差价（如果有）
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            if (rescheduleRequest.getChangeFee() != null) total = total.add(rescheduleRequest.getChangeFee());
            if (rescheduleRequest.getPriceDiff() != null) total = total.add(rescheduleRequest.getPriceDiff());
            float totalAmount = total.floatValue();

            String subject = "改签支付 - " + rescheduleRequest.getOrderNo();
            return payUtil.sendRequestToAlipay(tempOrderNo, totalAmount, subject);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("发起改签支付失败: " + ex.getMessage());
        }
    }

    /**
     * 发起取消（退订）支付（乘客提交取消申请并支付取消手续费）
     * 前端应传入 orderNo、cancelFee、refundFare、reason 等信息
     * 支付成功后将把对应订单与机票状态更新为 "待审核"
     */
    @ResponseBody
    @PostMapping("/payCancel")
    public String payCancel(@RequestBody CancelRequest cancelRequest) throws AlipayApiException {
        try {
            String tempOrderNo = generateTempOrderNo();
            // 若前端未传 passengerId，则尝试从订单表补全（保证回调插入/更新时有有效 passenger_id）
            try {
                if (cancelRequest.getPassengerId() == null) {
                    String orderNo = cancelRequest.getOrderNo();
                    if (orderNo != null) {
                        java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> ordOpt = orderRepository.findByOrderNo(orderNo);
                        if (ordOpt != null && ordOpt.isPresent()) {
                            cancelRequest.setPassengerId(ordOpt.get().getPassengerId());
                            // 仅补全 CancelRequest 中存在的字段（flightNo）
                            try {
                                com.example.airplanesalehouduan.passengers.entity.Order ord = ordOpt.get();
                                if (cancelRequest.getFlightNo() == null && ord.getFlightNo() != null) {
                                    cancelRequest.setFlightNo(ord.getFlightNo());
                                }
                                // 补全 applicantName 优先使用 users.real_name
                                if ((cancelRequest.getApplicantName() == null || cancelRequest.getApplicantName().isEmpty())
                                        && cancelRequest.getPassengerId() != null) {
                                    try {
                                        java.util.Optional<User> uopt = userRepository.findById(cancelRequest.getPassengerId());
                                        if (uopt != null && uopt.isPresent() && uopt.get().getRealName() != null) {
                                            cancelRequest.setApplicantName(uopt.get().getRealName());
                                        } else if (ord.getPassengerName() != null) {
                                            cancelRequest.setApplicantName(ord.getPassengerName());
                                        }
                                    } catch (Exception ignoreUser) {
                                        if (ord.getPassengerName() != null) cancelRequest.setApplicantName(ord.getPassengerName());
                                    }
                                }
                            } catch (Exception ignoreInner) {}
                        }
                    }
                }
            } catch (Exception ignore) {}

            // 如果前端未传 flightNo，也在此处尝试从订单补齐（无论 passengerId 是否存在）
            try {
                if (cancelRequest.getFlightNo() == null) {
                    String orderNo = cancelRequest.getOrderNo();
                    if (orderNo != null) {
                        java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> ordOpt2 = orderRepository.findByOrderNo(orderNo);
                        if (ordOpt2 != null && ordOpt2.isPresent()) {
                            try {
                                com.example.airplanesalehouduan.passengers.entity.Order ord2 = ordOpt2.get();
                                if (ord2.getFlightNo() != null) cancelRequest.setFlightNo(ord2.getFlightNo());
                            } catch (Exception ignoreInner) {}
                        }
                    }
                }
            } catch (Exception ignore) {}

            pendingCancels.put(tempOrderNo, cancelRequest);

            // 计算应付金额：通常为取消手续费（cancelFee），如果没有则使用 refundFare 的反向逻辑回退为 0
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            if (cancelRequest.getCancelFee() != null) total = total.add(cancelRequest.getCancelFee());
            float totalAmount = total.floatValue();

            String subject = "取消申请支付 - " + cancelRequest.getOrderNo();
            return payUtil.sendRequestToAlipay(tempOrderNo, totalAmount, subject);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("发起取消支付失败: " + ex.getMessage());
        }
    }

    /**
     * 发起已存在订单的支付宝支付（列表快捷支付）
     * 前端应传入 orderNo 和 amount（金额）
     * 为不改变现有支付逻辑，直接将 orderNo 作为 out_trade_no 传给支付宝
     */
    @ResponseBody
    @PostMapping("/payOrder")
    public String payOrder(@RequestBody PayOrderRequest payOrderRequest) throws AlipayApiException {
        try {
            String orderNo = payOrderRequest.getOrderNo();
            java.math.BigDecimal amt = payOrderRequest.getAmount();
            float totalAmount = (amt != null) ? amt.floatValue() : 0f;
            String subject = "订单支付 - " + orderNo;
            return payUtil.sendRequestToAlipay(orderNo, totalAmount, subject);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("发起订单支付失败: " + ex.getMessage());
        }
    }

    /**
     * 生成临时订单号
     * 格式：TEMP + 时间戳 + 随机数
     */
    private String generateTempOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "TEMP" + timestamp + random;
    }

    /**
     * 生成用于改签的新订单号
     * 格式：ORD + 时间戳 + 随机数
     */
    private String generateOrderNoForChange() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%03d", new Random().nextInt(900) + 100);
        return "ORD" + timestamp + random;
    }

    /**
     * 生成用于改签的新机票号
     * 格式：TKT + 时间戳 + 随机数
     */
    private String generateTicketNoForChange() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "TKT" + timestamp + random;
    }

    /**
     * 支付完成后的同步跳转（支付宝会跳转到此处）
     * 收到 out_trade_no 后再次调用支付宝查询接口确认支付状态
     * 支付成功后，从临时存储中获取预订信息，创建订单和机票，状态设为"待出行"
     */
    @GetMapping("/toSuccess")
    public String returns(String out_trade_no) throws ParseException {
        String query = payUtil.query(out_trade_no);
        System.out.println("支付宝查询返回： " + query);
        JSONObject jsonObject = JSONObject.parseObject(query);
        Object o = jsonObject.get("alipay_trade_query_response");
        Map map = (Map) o;
        System.out.println(map);
        Object s = map.get("trade_status");

        if ("TRADE_SUCCESS".equals(s)) {
            System.out.println("订单支付成功，out_trade_no=" + out_trade_no);

            // 从临时存储中获取预订信息
            BookingRequest bookingRequest = pendingBookings.remove(out_trade_no);

            if (bookingRequest != null) {
                try {
                    // 支付成功后创建订单和机票，状态设为"待出行"
                    Order order = bookingService.bookFlight(
                            bookingRequest.getPassengerId(),
                            bookingRequest.getFlightId(),
                            bookingRequest.getPassengers(),
                            bookingRequest.getTotalAmount(),
                            bookingRequest.getUsedPoints(),
                            bookingRequest.getAppliedCoupons()
                    );

                    System.out.println("订单创建成功，订单号=" + order.getOrderNo());
                    // 如果预订请求中使用了积分，但之前的积分消费记录没有 ref_order_id（可能是先消费积分兑换代金券），
                    // 在支付成功并创建订单后，将对应的 loyalty_points 的 ref_order_id 填写为新订单的 id。
                    try {
                        Integer usedPts = bookingRequest.getUsedPoints();
                        // 如果 bookingRequest 未直接记录 usedPoints，则尝试根据 appliedCoupons 估算（如积分兑换代金券：20 积分 = ¥1）
                        if ((usedPts == null || usedPts <= 0) && bookingRequest.getAppliedCoupons() != null && !bookingRequest.getAppliedCoupons().isEmpty()) {
                            double sumApplied = 0d;
                            for (BookingService.CouponInfo ci : bookingRequest.getAppliedCoupons()) {
                                try {
                                    if (ci != null && ci.getAppliedAmount() != null) sumApplied += ci.getAppliedAmount();
                                } catch (Exception ignore) {}
                            }
                            if (sumApplied > 0d) {
                                // 20 points = ¥1
                                usedPts = (int) Math.round(sumApplied * 20d);
                            }
                        }
                        if (usedPts != null && usedPts > 0) {
                            // 获取该乘客未关联订单的消费型积分记录（最新的先更新）
                            try {
                                java.util.List<java.util.Map<String, Object>> lpRows =
                                        jdbcTemplate.queryForList("SELECT id, points FROM loyalty_points WHERE passenger_id = ? AND change_type = 'SPEND' AND ref_order_id IS NULL ORDER BY created_at DESC", bookingRequest.getPassengerId());
                                int remaining = usedPts;
                                for (java.util.Map<String, Object> row : lpRows) {
                                    Object ptsObj = row.get("points");
                                    int pts = 0;
                                    if (ptsObj instanceof Number) {
                                        pts = Math.abs(((Number) ptsObj).intValue());
                                    } else if (ptsObj != null) {
                                        try {
                                            pts = Math.abs(Integer.parseInt(ptsObj.toString()));
                                        } catch (Exception ignore) {}
                                    }
                                    Object idObj = row.get("id");
                                    if (idObj == null) continue;
                                    Long lpId = null;
                                    if (idObj instanceof Number) lpId = ((Number) idObj).longValue();
                                    else {
                                        try { lpId = Long.valueOf(idObj.toString()); } catch (Exception ignore) {}
                                    }
                                    if (lpId == null) continue;
                                    try {
                                        jdbcTemplate.update("UPDATE loyalty_points SET ref_order_id = ? WHERE id = ?", order.getId(), lpId);
                                    } catch (Exception updateEx) {
                                        updateEx.printStackTrace();
                                    }
                                    remaining -= pts;
                                    if (remaining <= 0) break;
                                }
                            } catch (Exception qEx) {
                                qEx.printStackTrace();
                                System.err.println("更新 loyalty_points 查询失败: " + qEx.getMessage());
                            }
                        }
                    } catch (Exception ignorePointUpdate) {
                        // 不阻塞主流程，仅记录日志
                        System.err.println("将订单ID回写到 loyalty_points.ref_order_id 时发生错误: " + ignorePointUpdate.getMessage());
                    }
                    // 使用支付宝返回的 trade_no 作为最终的订单号，替换原先随机生成的系统订单号
                    try {
                        Object tradeNoObj = map.get("trade_no");
                        if (tradeNoObj != null) {
                            String tradeNoStr = tradeNoObj.toString();
                            if (tradeNoStr != null && !tradeNoStr.isEmpty()) {
                                String oldOrderNo = order.getOrderNo();
                                order.setOrderNo(tradeNoStr);
                                orderRepository.save(order);

                                // 同步更新该订单下所有机票的 orderNo 字段，保持一致
                                try {
                                    java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> relatedTickets =
                                            ticketRepository.findByOrderNo(oldOrderNo);
                                    if (relatedTickets != null && !relatedTickets.isEmpty()) {
                                        for (com.example.airplanesalehouduan.passengers.entity.Ticket t : relatedTickets) {
                                            t.setOrderNo(tradeNoStr);
                                        }
                                        ticketRepository.saveAll(relatedTickets);
                                    }
                                } catch (Exception ticketUpdateEx) {
                                    ticketUpdateEx.printStackTrace();
                                    System.err.println("更新机票 orderNo 失败: " + ticketUpdateEx.getMessage());
                                }
                            }
                        }
                    } catch (Exception exUpdate) {
                        exUpdate.printStackTrace();
                        System.err.println("将支付宝 trade_no 写入订单失败: " + exUpdate.getMessage());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("支付成功后创建订单失败: " + ex.getMessage());
                    // 即使创建订单失败，也跳转到个人中心，避免用户停留在支付页面
                }
            } else {
                // 未命中预订缓存，尝试命中行李缓存或改签缓存
                BaggageRequest baggageRequest = pendingBaggage.remove(out_trade_no);
                if (baggageRequest != null) {
                    try {
                        // 将请求中的 baggage Map 转换为 BaggageManagement 实例（兼容前端时间格式）
                        BaggageManagement baggageEntity = new BaggageManagement();
                        Map<String, Object> bagMap = baggageRequest.getBaggage();
                        if (bagMap != null) {
                            if (bagMap.containsKey("orderno")) baggageEntity.setOrderno((String) bagMap.get("orderno"));
                            if (bagMap.containsKey("baggageType")) baggageEntity.setBaggageType((String) bagMap.get("baggageType"));
                            if (bagMap.containsKey("baggageCount")) {
                                Object cnt = bagMap.get("baggageCount");
                                if (cnt instanceof Number) baggageEntity.setBaggageCount(((Number) cnt).intValue());
                                else if (cnt instanceof String) baggageEntity.setBaggageCount(Integer.parseInt((String) cnt));
                            }
                            if (bagMap.containsKey("totalWeight")) {
                                Object w = bagMap.get("totalWeight");
                                if (w instanceof Number) baggageEntity.setTotalWeight(java.math.BigDecimal.valueOf(((Number) w).doubleValue()));
                                else if (w instanceof String && !((String) w).isEmpty()) baggageEntity.setTotalWeight(new java.math.BigDecimal((String) w));
                            }
                            if (bagMap.containsKey("weightLimit")) {
                                Object wl = bagMap.get("weightLimit");
                                if (wl instanceof Number) baggageEntity.setWeightLimit(java.math.BigDecimal.valueOf(((Number) wl).doubleValue()));
                                else if (wl instanceof String && !((String) wl).isEmpty()) baggageEntity.setWeightLimit(new java.math.BigDecimal((String) wl));
                            }
                            if (bagMap.containsKey("baggageFee")) {
                                Object bf = bagMap.get("baggageFee");
                                if (bf instanceof Number) baggageEntity.setBaggageFee(java.math.BigDecimal.valueOf(((Number) bf).doubleValue()));
                                else if (bf instanceof String && !((String) bf).isEmpty()) baggageEntity.setBaggageFee(new java.math.BigDecimal((String) bf));
                            }
                            if (bagMap.containsKey("excessFee")) {
                                Object ef = bagMap.get("excessFee");
                                if (ef instanceof Number) baggageEntity.setExcessFee(java.math.BigDecimal.valueOf(((Number) ef).doubleValue()));
                                else if (ef instanceof String && !((String) ef).isEmpty()) baggageEntity.setExcessFee(new java.math.BigDecimal((String) ef));
                            }
                            if (bagMap.containsKey("dimensions")) baggageEntity.setDimensions((String) bagMap.get("dimensions"));
                            if (bagMap.containsKey("description")) baggageEntity.setDescription((String) bagMap.get("description"));
                            if (bagMap.containsKey("remark")) baggageEntity.setRemark((String) bagMap.get("remark"));

                            // 解析到达时间字符串，兼容 "yyyy-MM-dd HH:mm:ss" 和带时区/带T的形式
                            Object arrivalObj = null;
                            if (bagMap.containsKey("arrivalTimeFlight")) arrivalObj = bagMap.get("arrivalTimeFlight");
                            if (arrivalObj == null && bagMap.containsKey("arrival_time_flight")) arrivalObj = bagMap.get("arrival_time_flight");
                            if (arrivalObj != null && arrivalObj instanceof String) {
                                try {
                                    String timeStr = ((String) arrivalObj).trim();
                                    if (timeStr.endsWith("Z")) timeStr = timeStr.substring(0, timeStr.length() - 1);
                                    int timezoneIndex = timeStr.indexOf("+");
                                    if (timezoneIndex == -1) timezoneIndex = timeStr.indexOf("-", 10);
                                    if (timezoneIndex > 0) timeStr = timeStr.substring(0, timezoneIndex);
                                    timeStr = timeStr.replace("T", " ");
                                    java.time.format.DateTimeFormatter formatter;
                                    if (timeStr.length() == 16) formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                    else formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    baggageEntity.setArrivalTimeFlight(java.time.LocalDateTime.parse(timeStr, formatter));
                                } catch (Exception e) {
                                    System.err.println("解析行李到达时间失败: " + e.getMessage() + ", 将使用订单的到达时间");
                                }
                            }
                        }

                        BaggageManagement created = baggageManagementService.createBaggage(
                                baggageRequest.getPassengerId(),
                                baggageEntity
                        );
                        System.out.println("行李登记创建成功，baggageNo=" + created.getBaggageNo());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.err.println("支付成功后创建行李失败: " + ex.getMessage());
                    }
                } else {
                    // 处理取消支付回调：将对应订单与机票状态标记为"待审核"
                    CancelRequest cancelReq = pendingCancels.remove(out_trade_no);
                    if (cancelReq != null) {
                        try {
                            String orderNo = cancelReq.getOrderNo();
                            if (orderNo != null) {
                                orderRepository.findByOrderNo(orderNo).ifPresent(order -> {
                                    order.setStatus("待审核");
                                    orderRepository.save(order);
                                });

                                // 更新该订单下的所有机票状态为"待审核"
                                java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> tickets =
                                        ticketRepository.findByOrderNo(orderNo);
                                if (tickets != null && !tickets.isEmpty()) {
                                    for (com.example.airplanesalehouduan.passengers.entity.Ticket t : tickets) {
                                        t.setStatus("待审核");
                                    }
                                    ticketRepository.saveAll(tickets);

                                    try {
                                        // 将机票号拼接后更新到 ticket_cancel_requests.ticket_no（按 orderno + passenger_id 精确匹配）
                                        String ticketNos = tickets.stream()
                                                .map(t -> {
                                                    try {
                                                        return t.getTicketNo();
                                                    } catch (Exception e) {
                                                        return null;
                                                    }
                                                })
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.joining(","));

                                        if (ticketNos != null && !ticketNos.isEmpty()) {
                                            // 确保使用非空的 passengerId：若请求中为空，从订单表补齐
                                            Integer passengerId = cancelReq.getPassengerId();
                                            try {
                                                if (passengerId == null) {
                                                    java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> ordOpt = orderRepository.findByOrderNo(orderNo);
                                                    if (ordOpt != null && ordOpt.isPresent()) {
                                                        passengerId = ordOpt.get().getPassengerId();
                                                    }
                                                }
                                            } catch (Exception ignore) {}

                                            // passengerId 必须有效（表约束 NOT NULL 且外键），若无则记录错误并跳过
                                            if (passengerId == null) {
                                                System.err.println("无法确定 passenger_id，跳过更新/插入 ticket_cancel_requests，orderNo=" + orderNo);
                                            } else {
                                                String sql = "UPDATE ticket_cancel_requests SET ticket_no = ?, status = ? WHERE orderno = ? AND passenger_id = ?";
                                                int updated = jdbcTemplate.update(sql, ticketNos, "待审核", orderNo, passengerId);
                                                if (updated == 0) {
                                                    try {
                                                        // 未命中时，创建一条新的取消申请记录（尽量补全可用字段）
                                                        String cancelNo = generateTempOrderNo().replaceFirst("^TEMP", "CNL");
                                                        String applicantName = cancelReq.getApplicantName();
                                                        String route = "";
                                                        Timestamp departureTs = null;
                                                        // 尝试从订单中补全航线与起飞时间、申请人姓名（优先使用 cancelReq 中已有的 applicantName，再用 users.real_name 回退，最后使用 order.passengerName）
                                                        try {
                                                            java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> ordOpt = orderRepository.findByOrderNo(orderNo);
                                                            String flightNoToInsert = cancelReq.getFlightNo();
                                                            if (ordOpt != null && ordOpt.isPresent()) {
                                                                com.example.airplanesalehouduan.passengers.entity.Order ord = ordOpt.get();
                                                                if ((applicantName == null || applicantName.isEmpty()) && passengerId != null) {
                                                                    try {
                                                                        java.util.Optional<User> uopt = userRepository.findById(passengerId);
                                                                        if (uopt != null && uopt.isPresent() && uopt.get().getRealName() != null) {
                                                                            applicantName = uopt.get().getRealName();
                                                                        } else if (ord.getPassengerName() != null) {
                                                                            applicantName = ord.getPassengerName();
                                                                        }
                                                                    } catch (Exception ignoreUser) {
                                                                        if (ord.getPassengerName() != null) applicantName = ord.getPassengerName();
                                                                    }
                                                                } else if ((applicantName == null || applicantName.isEmpty()) && ord.getPassengerName() != null) {
                                                                    applicantName = ord.getPassengerName();
                                                                }
                                                                if (ord.getRoute() != null) route = ord.getRoute();
                                                                if (ord.getDepartureTime() != null) departureTs = Timestamp.valueOf(ord.getDepartureTime());
                                                                if (flightNoToInsert == null && ord.getFlightNo() != null) {
                                                                    flightNoToInsert = ord.getFlightNo();
                                                                }
                                                            }
                                                            // 如果 flightNo 仍为 null，回退为空字符串以满足 NOT NULL 约束
                                                            if (flightNoToInsert == null) flightNoToInsert = "";
                                                            // 如果 departureTs 仍为 null，回退为当前时间以满足 NOT NULL 约束
                                                            if (departureTs == null) departureTs = new Timestamp(System.currentTimeMillis());

                                                            String insertSql = "INSERT INTO ticket_cancel_requests (cancel_no, orderno, passenger_id, applicant_name, ticket_no, flight_no, route, departure_time, cancel_fee, refund_fare, status, reason) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
                                                            jdbcTemplate.update(
                                                                    insertSql,
                                                                    cancelNo,
                                                                    orderNo,
                                                                    passengerId,
                                                                    applicantName,
                                                                    ticketNos,
                                                                    flightNoToInsert,
                                                                    route,
                                                                    departureTs,
                                                                    cancelReq.getCancelFee(),
                                                                    cancelReq.getRefundFare(),
                                                                    "待审核",
                                                                    cancelReq.getReason()
                                                            );
                                                        } catch (Exception ex2) {
                                                            ex2.printStackTrace();
                                                            System.err.println("插入取消申请记录失败: " + ex2.getMessage());
                                                        }
                                                    } catch (Exception ex2) {
                                                        ex2.printStackTrace();
                                                        System.err.println("插入取消申请记录失败: " + ex2.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        System.err.println("更新取消申请的 ticket_no 失败: " + ex.getMessage());
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            System.err.println("处理取消支付回调失败: " + ex.getMessage());
                        }
                    } else {
                        // 处理改签支付回调：将对应订单与机票状态标记为"待审核"，并创建/更新 ticket_change_requests
                        RescheduleRequest reschedule = pendingReschedules.remove(out_trade_no);
                        if (reschedule != null) {
                            try {
                                String orderNo = reschedule.getOrderNo();
                                if (orderNo != null) {
                                    orderRepository.findByOrderNo(orderNo).ifPresent(order -> {
                                        // 保持与乘客提交申请时的语义一致：原订单标记为“待处理”
                                        order.setStatus("待处理");
                                        orderRepository.save(order);
                                    });

                                    // 更新该订单下的所有机票状态为"待处理"
                                    java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> tickets =
                                            ticketRepository.findByOrderNo(orderNo);
                                    if (tickets != null && !tickets.isEmpty()) {
                                        for (com.example.airplanesalehouduan.passengers.entity.Ticket t : tickets) {
                                            t.setStatus("待处理");
                                        }
                                        ticketRepository.saveAll(tickets);
                                    }

                                    try {
                                        // 将机票号拼接后更新到 ticket_change_requests.ticket_no（按 orderno + passenger_id 精确匹配）
                                        String ticketNos = "";
                                        try {
                                            ticketNos = tickets.stream()
                                                    .map(t -> {
                                                        try {
                                                            return t.getTicketNo();
                                                        } catch (Exception e) {
                                                            return null;
                                                        }
                                                    })
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.joining(","));
                                        } catch (Exception ignore) {}

                                        // 确保使用非空的 passengerId：若请求中为空，从订单表补齐
                                        Integer passengerId = reschedule.getPassengerId();
                                        try {
                                            if (passengerId == null) {
                                                java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> ordOpt = orderRepository.findByOrderNo(orderNo);
                                                if (ordOpt != null && ordOpt.isPresent()) {
                                                    passengerId = ordOpt.get().getPassengerId();
                                                }
                                            }
                                        } catch (Exception ignore) {}

                                        if (passengerId == null) {
                                            System.err.println("无法确定 passenger_id，跳过更新/插入 ticket_change_requests，orderNo=" + orderNo);
                                        } else {
                                            String sql = "UPDATE ticket_change_requests SET ticket_no = ?, status = ? WHERE orderno = ? AND passenger_id = ?";
                                            int updated = jdbcTemplate.update(sql, ticketNos, "待审核", orderNo, passengerId);
                                            if (updated == 0) {
                                                try {
                                                    // 未命中时，创建一条新的改签申请记录（尽量补全可用字段）
                                                    String changeNo = generateTempOrderNo().replaceFirst("^TEMP", "TCR");
                                                    String applicantName = reschedule.getPassengerId() != null ? null : null;
                                                    String oldFlightNo = "";
                                                    String oldRoute = "";
                                                    Timestamp oldDepartureTs = null;
                                                    String newFlightNo = "";
                                                    String newRoute = "";
                                                    Timestamp newDepartureTs = null;
                                                    Timestamp newArrivalTs = null;
                                                    java.math.BigDecimal changeFee = null;
                                                    java.math.BigDecimal fareDiff = null;

                                                    // 尝试从 reschedule 请求中读取可用字段
                                                    try {
                                                        if (reschedule.getNewFlight() != null) {
                                                            Map<String, Object> nf = reschedule.getNewFlight();
                                                            Object nfFlightNo = nf.get("flightNumber");
                                                            if (nfFlightNo == null) nfFlightNo = nf.get("flightNo");
                                                            if (nfFlightNo == null) nfFlightNo = nf.get("newFlightNo");
                                                            if (nfFlightNo != null) newFlightNo = String.valueOf(nfFlightNo);

                                                            Object dep = nf.get("schedDepTime");
                                                            if (dep == null) dep = nf.get("departureTime");
                                                            if (dep == null) dep = nf.get("newDepartureTime");
                                                            if (dep != null && dep instanceof String) {
                                                                String timeStr = ((String) dep).trim();
                                                                if (timeStr.endsWith("Z")) timeStr = timeStr.substring(0, timeStr.length() - 1);
                                                                int timezoneIndex = timeStr.indexOf("+");
                                                                if (timezoneIndex == -1) timezoneIndex = timeStr.indexOf("-", 10);
                                                                if (timezoneIndex > 0) timeStr = timeStr.substring(0, timezoneIndex);
                                                                timeStr = timeStr.replace("T", " ");
                                                                try {
                                                                    // 如果前端只传了时间 (e.g. "08:00"), 则尝试从 newFlight 或 reschedule 本体读取日期字段进行合成
                                                                    if (timeStr.matches("^\\d{2}:\\d{2}(:\\d{2})?$")) {
                                                                        String datePart = null;
                                                                        try {
                                                                            Object dobj = nf.get("date");
                                                                            if (dobj == null) dobj = nf.get("newDepartureDate");
                                                                            if (dobj == null) dobj = nf.get("departureDate");
                                                                            if (dobj == null) dobj = reschedule.getNewFlight() != null ? reschedule.getNewFlight().get("date") : null;
                                                                            if (dobj != null) datePart = String.valueOf(dobj).trim();
                                                                        } catch (Exception ignoreDate) { }
                                                                        if (datePart != null && !datePart.isEmpty()) {
                                                                            // 补全秒字段
                                                                            if (timeStr.length() == 5) timeStr = timeStr + ":00";
                                                                            String combined = datePart + " " + timeStr;
                                                                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                                                            newDepartureTs = Timestamp.valueOf(java.time.LocalDateTime.parse(combined, formatter));
                                                                        } else {
                                                                            // 无日期信息则按原逻辑尝试解析（可能会失败并回退）
                                                                            java.time.format.DateTimeFormatter formatter;
                                                                            if (timeStr.length() == 16) formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                                                            else formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                                                            newDepartureTs = Timestamp.valueOf(java.time.LocalDateTime.parse(timeStr, formatter));
                                                                        }
                                                                    } else {
                                                                        java.time.format.DateTimeFormatter formatter;
                                                                        if (timeStr.length() == 16) formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                                                        else formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                                                        newDepartureTs = Timestamp.valueOf(java.time.LocalDateTime.parse(timeStr, formatter));
                                                                    }
                                                                } catch (Exception e) {
                                                                    // ignore parsing error, 后续有回退处理
                                                                }
                                                            }

                                                            // 解析到达时间（如果前端提供）
                                                            Object arr = nf.get("schedArrTime");
                                                            if (arr == null) arr = nf.get("arrivalTime");
                                                            if (arr != null && arr instanceof String) {
                                                                String arrStr = ((String) arr).trim();
                                                                if (arrStr.endsWith("Z")) arrStr = arrStr.substring(0, arrStr.length() - 1);
                                                                int tzIdx = arrStr.indexOf("+");
                                                                if (tzIdx == -1) tzIdx = arrStr.indexOf("-", 10);
                                                                if (tzIdx > 0) arrStr = arrStr.substring(0, tzIdx);
                                                                arrStr = arrStr.replace("T", " ");
                                                                try {
                                                                    java.time.format.DateTimeFormatter formatterArr;
                                                                    if (arrStr.length() == 16) formatterArr = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                                                    else formatterArr = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                                                    newArrivalTs = Timestamp.valueOf(java.time.LocalDateTime.parse(arrStr, formatterArr));
                                                                } catch (Exception ignoredArr) {}
                                                            }

                                                            Object origin = nf.get("departure");
                                                            Object dest = nf.get("destination");
                                                            if (origin != null || dest != null) {
                                                                newRoute = String.valueOf(origin == null ? "" : origin) + " → " + String.valueOf(dest == null ? "" : dest);
                                                            }
                                                        }
                                                    } catch (Exception ignore) {}

                                                    // 尝试从订单中补全旧航班信息与申请人姓名
                                                    try {
                                                        java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> ordOpt = orderRepository.findByOrderNo(orderNo);
                                                        if (ordOpt != null && ordOpt.isPresent()) {
                                                            com.example.airplanesalehouduan.passengers.entity.Order ord = ordOpt.get();
                                                            if (applicantName == null || applicantName.trim().isEmpty()) {
                                                                try {
                                                                    java.util.Optional<User> uopt = userRepository.findById(passengerId);
                                                                    if (uopt != null && uopt.isPresent() && uopt.get().getRealName() != null) {
                                                                        applicantName = uopt.get().getRealName();
                                                                    } else if (ord.getPassengerName() != null) {
                                                                        applicantName = ord.getPassengerName();
                                                                    }
                                                                } catch (Exception ignoreUser) {
                                                                    if (ord.getPassengerName() != null) applicantName = ord.getPassengerName();
                                                                }
                                                            }
                                                            if (oldFlightNo == null || oldFlightNo.trim().isEmpty()) {
                                                                if (ord.getFlightNo() != null) oldFlightNo = ord.getFlightNo();
                                                            }
                                                            if (oldRoute == null || oldRoute.trim().isEmpty()) {
                                                                if (ord.getRoute() != null) oldRoute = ord.getRoute();
                                                            }
                                                            if (oldDepartureTs == null && ord.getDepartureTime() != null) {
                                                                oldDepartureTs = Timestamp.valueOf(ord.getDepartureTime());
                                                            }
                                                        }
                                                    } catch (Exception ignore) {}

                                                    // 金额字段
                                                    try {
                                                        changeFee = reschedule.getChangeFee();
                                                    } catch (Exception ignore) {}
                                                    try {
                                                        fareDiff = reschedule.getPriceDiff();
                                                    } catch (Exception ignore) {}

                                                    // 如果仍有必要字段为空，设置合理回退值以满足非空约束
                                                    if (oldFlightNo == null) oldFlightNo = "";
                                                    if (oldRoute == null) oldRoute = "";
                                                    if (oldDepartureTs == null) oldDepartureTs = new Timestamp(System.currentTimeMillis());
                                                    if (newFlightNo == null) newFlightNo = "";
                                                    if (newRoute == null) newRoute = "";
                                                    if (newDepartureTs == null) newDepartureTs = new Timestamp(System.currentTimeMillis());

                                                    String insertSql = "INSERT INTO ticket_change_requests (change_no, orderno, passenger_id, applicant_name, ticket_no, old_flight_no, old_route, old_departure_time, new_flight_no, new_route, new_departure_time, change_fee, fare_diff, status, reason) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                                    jdbcTemplate.update(
                                                            insertSql,
                                                            changeNo,
                                                            orderNo,
                                                            passengerId,
                                                            applicantName,
                                                            ticketNos,
                                                            oldFlightNo,
                                                            oldRoute,
                                                            oldDepartureTs,
                                                            newFlightNo,
                                                            newRoute,
                                                            newDepartureTs,
                                                            changeFee,
                                                            fareDiff,
                                                            "待审核",
                                                            reschedule.getReason()
                                                    );

                                                    // 支付成功：基于原订单与改签请求生成新的订单与对应机票（状态为“待审核”）
                                                    try {
                                                        java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> oldOrdOpt = orderRepository.findByOrderNo(orderNo);
                                                        if (oldOrdOpt != null && oldOrdOpt.isPresent()) {
                                                            com.example.airplanesalehouduan.passengers.entity.Order oldOrd = oldOrdOpt.get();

                                                            com.example.airplanesalehouduan.passengers.entity.Order newOrder = new com.example.airplanesalehouduan.passengers.entity.Order();
                                                            Integer finalPassengerId = passengerId != null ? passengerId : oldOrd.getPassengerId();
                                                            newOrder.setPassengerId(finalPassengerId);
                                                            newOrder.setOrderNo(generateOrderNoForChange());

                                                            // 乘客姓名优先策略（不改变原有逻辑的前提下做小幅增强）：
                                                            // 1. 优先使用前端在改签请求中传入的 passengers 数组里的 passengerName（按 passengerId 精确匹配；若 passengerId 不存在则取第一个可用姓名）
                                                            // 2. 若无则按原逻辑尝试从 users 表读取 realName
                                                            // 3. 最后回退为原订单的 passengerName
                                                            String pName = null;
                                                            try {
                                                                if (reschedule != null && reschedule.getPassengers() != null && !reschedule.getPassengers().isEmpty()) {
                                                                    for (Map<String, Object> pm : reschedule.getPassengers()) {
                                                                        if (pm == null) continue;
                                                                        Object pidObj = pm.get("passengerId");
                                                                        Object pnameObj = pm.get("passengerName");
                                                                        if (pnameObj == null) continue;
                                                                        String candName = String.valueOf(pnameObj);
                                                                        // 若 finalPassengerId 可用，优先按 ID 精确匹配
                                                                        if (finalPassengerId != null && pidObj != null) {
                                                                            try {
                                                                                if (String.valueOf(pidObj).equals(String.valueOf(finalPassengerId))) {
                                                                                    pName = candName;
                                                                                    break;
                                                                                }
                                                                            } catch (Exception ignoreMatch) {}
                                                                        } else {
                                                                            // 没有 passengerId 可匹配，取第一个非空姓名作为候选
                                                                            pName = candName;
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            } catch (Exception ignore) {}
                                                            // 回退到 users.real_name（原有逻辑）
                                                            if (pName == null) {
                                                                try {
                                                                    if (finalPassengerId != null) {
                                                                        java.util.Optional<com.example.airplanesalehouduan.Login.entity.User> uopt = userRepository.findById(finalPassengerId);
                                                                        if (uopt != null && uopt.isPresent() && uopt.get().getRealName() != null) pName = uopt.get().getRealName();
                                                                    }
                                                                } catch (Exception ignore) {}
                                                            }
                                                            if (pName == null) pName = oldOrd.getPassengerName();
                                                            newOrder.setPassengerName(pName);

                                                            newOrder.setRoute(newRoute != null ? newRoute : oldOrd.getRoute());
                                                            newOrder.setFlightNo(newFlightNo != null ? newFlightNo : oldOrd.getFlightNo());
                                                            if (newDepartureTs != null) newOrder.setDepartureTime(newDepartureTs.toLocalDateTime());
                                                            else newOrder.setDepartureTime(oldOrd.getDepartureTime());
                                                            // 到达时间：优先使用 newArrivalTs（若解析成功），否则回退为原订单到达时间
                                                            try {
                                                                if (newArrivalTs != null) newOrder.setArrivalTime(newArrivalTs.toLocalDateTime());
                                                                else newOrder.setArrivalTime(oldOrd.getArrivalTime());
                                                            } catch (Exception ignoreArr) {
                                                                newOrder.setArrivalTime(oldOrd.getArrivalTime());
                                                            }

                                                            java.math.BigDecimal oldAmountVal = oldOrd.getTotalAmount() != null ? oldOrd.getTotalAmount() : java.math.BigDecimal.ZERO;
                                                            java.math.BigDecimal fareDiffVal = fareDiff != null ? fareDiff : java.math.BigDecimal.ZERO;
                                                            newOrder.setTotalAmount(oldAmountVal.add(fareDiffVal));
                                                            newOrder.setStatus("待审核");
                                                            orderRepository.save(newOrder);
                                                            // 将新订单号写入之前插入的 ticket_change_requests 的 new_order_no 字段（若存在 changeNo）
                                                            try {
                                                                if (changeNo != null && !changeNo.isEmpty()) {
                                                                    jdbcTemplate.update("UPDATE ticket_change_requests SET new_order_no = ? WHERE change_no = ?", newOrder.getOrderNo(), changeNo);
                                                                }
                                                            } catch (Exception exUpdateNewOrderNo) {
                                                                exUpdateNewOrderNo.printStackTrace();
                                                                System.err.println("写入 ticket_change_requests.new_order_no 失败: " + exUpdateNewOrderNo.getMessage());
                                                            }

                                                            // 为原订单下每张机票创建对应的新机票并关联到新订单
                                                            try {
                                                                java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> oldTickets = ticketRepository.findByOrderNo(orderNo);
                                                                if (oldTickets != null && !oldTickets.isEmpty()) {
                                                                    java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> createdNewTickets = new java.util.ArrayList<>();
                                                                    for (int ti = 0; ti < oldTickets.size(); ti++) {
                                                                        com.example.airplanesalehouduan.passengers.entity.Ticket oldT = oldTickets.get(ti);
                                                                        com.example.airplanesalehouduan.passengers.entity.Ticket newT = new com.example.airplanesalehouduan.passengers.entity.Ticket();
                                                                        newT.setOrderNo(newOrder.getOrderNo());
                                                                        newT.setPassengerId(oldT.getPassengerId());
                                                                        newT.setPassengerName(oldT.getPassengerName());
                                                                        newT.setIdCard(oldT.getIdCard() != null ? oldT.getIdCard() : "");

                                                                        Integer newFlightId = null;
                                                                        try {
                                                                            if (reschedule.getNewFlight() != null) {
                                                                                Object fid = reschedule.getNewFlight().get("id");
                                                                                if (fid == null) fid = reschedule.getNewFlight().get("flightId");
                                                                                if (fid != null) newFlightId = Integer.valueOf(String.valueOf(fid));
                                                                            }
                                                                        } catch (Exception ignore) {}
                                                                        newT.setFlightId(newFlightId != null ? newFlightId : oldT.getFlightId());

                                                                        newT.setFlightNo(newFlightNo != null && !newFlightNo.isEmpty() ? newFlightNo : oldT.getFlightNo());
                                                                        String origin = oldT.getOriginAirport();
                                                                        String dest = oldT.getDestAirport();
                                                                        try {
                                                                            if (reschedule.getNewFlight() != null) {
                                                                                Object oObj = reschedule.getNewFlight().get("departure");
                                                                                Object dObj = reschedule.getNewFlight().get("destination");
                                                                                if (oObj != null) origin = String.valueOf(oObj);
                                                                                if (dObj != null) dest = String.valueOf(dObj);
                                                                            }
                                                                        } catch (Exception ignore) {}
                                                                        newT.setOriginAirport(origin != null ? origin : "");
                                                                        newT.setDestAirport(dest != null ? dest : "");
                                                                        newT.setRoute(newRoute != null && !newRoute.isEmpty() ? newRoute : oldT.getRoute());
                                                                        if (newDepartureTs != null) newT.setDepartureTime(newDepartureTs.toLocalDateTime());
                                                                        else newT.setDepartureTime(oldT.getDepartureTime());

                                                                        java.time.LocalDateTime newArr = null;
                                                                        try {
                                                                            if (reschedule.getNewFlight() != null) {
                                                                                Object arrObj = reschedule.getNewFlight().get("schedArrTime");
                                                                                if (arrObj == null) arrObj = reschedule.getNewFlight().get("arrivalTime");
                                                                                if (arrObj != null && arrObj instanceof String) {
                                                                                    String timeStr = ((String) arrObj).trim();
                                                                                    if (timeStr.endsWith("Z")) timeStr = timeStr.substring(0, timeStr.length() - 1);
                                                                                    int timezoneIndex2 = timeStr.indexOf("+");
                                                                                    if (timezoneIndex2 == -1) timezoneIndex2 = timeStr.indexOf("-", 10);
                                                                                    if (timezoneIndex2 > 0) timeStr = timeStr.substring(0, timezoneIndex2);
                                                                                    timeStr = timeStr.replace("T", " ");
                                                                                    java.time.format.DateTimeFormatter formatter2;
                                                                                    if (timeStr.length() == 16) formatter2 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                                                                    else formatter2 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                                                                    newArr = java.time.LocalDateTime.parse(timeStr, formatter2);
                                                                                }
                                                                            }
                                                                        } catch (Exception ignore) {}
                                                                        if (newArr == null) newArr = oldT.getArrivalTime();
                                                                        newT.setArrivalTime(newArr);

                                                                        // phone: 优先使用前端传入的 passengers 中的 phone 字段，回退为旧机票的 phone
                                                                        String phoneVal = oldT.getPhone() != null ? oldT.getPhone() : "";
                                                                        try {
                                                                            if (reschedule.getPassengers() != null) {
                                                                                // 尝试按 passengerId 或 passengerName 匹配电话
                                                                                for (int pi = 0; pi < reschedule.getPassengers().size(); pi++) {
                                                                                    Map<String, Object> pm = reschedule.getPassengers().get(pi);
                                                                                    Object pid = pm.get("passengerId");
                                                                                    Object pname = pm.get("passengerName");
                                                                                    if ((pid != null && String.valueOf(pid).equals(String.valueOf(oldT.getPassengerId())))
                                                                                            || (pname != null && String.valueOf(pname).equalsIgnoreCase(String.valueOf(oldT.getPassengerName())))) {
                                                                                        Object ph = pm.get("phone");
                                                                                        if (ph == null) ph = pm.get("mobile");
                                                                                        if (ph == null) ph = pm.get("phoneNumber");
                                                                                        if (ph != null) {
                                                                                            phoneVal = String.valueOf(ph);
                                                                                            break;
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        } catch (Exception ignorePhone) {}
                                                                        newT.setPhone(phoneVal);
                                                                        // seat number: 优先使用 seatAssignments（按 passengerId 匹配），再按 passengers 中的 seatNumber，最后按索引回退到对应位置
                                                                        String seatNumVal = oldT.getSeatNumber();
                                                                        try {
                                                                            if (reschedule.getSeatAssignments() != null) {
                                                                                for (Map<String, Object> sa : reschedule.getSeatAssignments()) {
                                                                                    Object pid2 = sa.get("passengerId");
                                                                                    Object pname2 = sa.get("passengerName");
                                                                                    if ((pid2 != null && String.valueOf(pid2).equals(String.valueOf(oldT.getPassengerId())))
                                                                                            || (pname2 != null && String.valueOf(pname2).equalsIgnoreCase(String.valueOf(oldT.getPassengerName())))) {
                                                                                        Object sn = sa.get("seatNumber");
                                                                                        if (sn == null) sn = sa.get("seatLabel");
                                                                                        if (sn == null) sn = sa.get("seatId");
                                                                                        if (sn != null) {
                                                                                            seatNumVal = String.valueOf(sn);
                                                                                            break;
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            // 再尝试 passengers 数组内的 seatNumber 字段
                                                                            if ((seatNumVal == null || seatNumVal.isEmpty()) && reschedule.getPassengers() != null) {
                                                                                for (Map<String, Object> pm2 : reschedule.getPassengers()) {
                                                                                    Object pid3 = pm2.get("passengerId");
                                                                                    Object pname3 = pm2.get("passengerName");
                                                                                    if ((pid3 != null && String.valueOf(pid3).equals(String.valueOf(oldT.getPassengerId())))
                                                                                            || (pname3 != null && String.valueOf(pname3).equalsIgnoreCase(String.valueOf(oldT.getPassengerName())))) {
                                                                                        Object sn2 = pm2.get("seatNumber");
                                                                                        if (sn2 == null) sn2 = pm2.get("seatLabel");
                                                                                        if (sn2 != null) {
                                                                                            seatNumVal = String.valueOf(sn2);
                                                                                            break;
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                            // 最后尝试按索引回退（如果 seatAssignments 长度与 oldTickets 长度匹配）
                                                                            if ((seatNumVal == null || seatNumVal.isEmpty()) && reschedule.getSeatAssignments() != null) {
                                                                                try {
                                                                                    if (ti >= 0 && ti < reschedule.getSeatAssignments().size()) {
                                                                                        Map<String, Object> saFallback = reschedule.getSeatAssignments().get(ti);
                                                                                        Object sfn = saFallback.get("seatNumber");
                                                                                        if (sfn == null) sfn = saFallback.get("seatLabel");
                                                                                        if (sfn == null) sfn = saFallback.get("seatId");
                                                                                        if (sfn != null) seatNumVal = String.valueOf(sfn);
                                                                                    }
                                                                                } catch (Exception ignoreIdx) {}
                                                                            }
                                                                        } catch (Exception ignoreSeat) {}
                                                                        newT.setSeatNumber(seatNumVal);
                                                                        newT.setSeatClass(oldT.getSeatClass());
                                                                        newT.setBasePrice(oldT.getBasePrice() != null ? oldT.getBasePrice() : java.math.BigDecimal.ZERO);
                                                                        newT.setSeatFee(oldT.getSeatFee() != null ? oldT.getSeatFee() : java.math.BigDecimal.ZERO);
                                                                        newT.setDiscountFee(oldT.getDiscountFee() != null ? oldT.getDiscountFee() : java.math.BigDecimal.ZERO);

                                                                        if (createdNewTickets.isEmpty() && fareDiffVal.compareTo(java.math.BigDecimal.ZERO) != 0) {
                                                                            newT.setTotalPrice(oldT.getTotalPrice().add(fareDiffVal));
                                                                        } else {
                                                                            newT.setTotalPrice(oldT.getTotalPrice());
                                                                        }
                                                                        newT.setTicketNo(generateTicketNoForChange());
                                                                        newT.setStatus("待审核");
                                                                        createdNewTickets.add(ticketRepository.save(newT));
                                                                    }
                                                                    if (!createdNewTickets.isEmpty()) {
                                                                        String newTicketNos = createdNewTickets.stream().map(com.example.airplanesalehouduan.passengers.entity.Ticket::getTicketNo).reduce((a, b) -> a + "," + b).orElse("");
                                                                        newOrder.setTicketNo(newTicketNos);
                                                                        orderRepository.save(newOrder);
                                                                    }
                                                                }
                                                            } catch (Exception exCreateT) {
                                                                exCreateT.printStackTrace();
                                                                System.err.println("创建新机票失败: " + exCreateT.getMessage());
                                                            }
                                                        }
                                                    } catch (Exception exCreateOrder) {
                                                        exCreateOrder.printStackTrace();
                                                        System.err.println("创建改签新订单失败: " + exCreateOrder.getMessage());
                                                    }
                                                } catch (Exception ex2) {
                                                    ex2.printStackTrace();
                                                    System.err.println("插入改签申请记录失败: " + ex2.getMessage());
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        System.err.println("更新改签申请的 ticket_no 失败: " + ex.getMessage());
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                System.err.println("处理改签支付回调失败: " + ex.getMessage());
                            }
                        } else {
                            System.err.println("未找到临时订单信息，out_trade_no=" + out_trade_no);
                            // 支付成功但未命中临时缓存：尝试将该 out_trade_no 视为已存在订单的订单号，
                            // 并将订单状态改为“待出行”，同时将该订单下的所有机票状态改为“已出票”。
                            try {
                                java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> existingOrderOpt = orderRepository.findByOrderNo(out_trade_no);
                                if (existingOrderOpt != null && existingOrderOpt.isPresent()) {
                                    com.example.airplanesalehouduan.passengers.entity.Order existingOrder = existingOrderOpt.get();
                                    try {
                                        existingOrder.setStatus("待出行");
                                        orderRepository.save(existingOrder);
                                    } catch (Exception exStatus) {
                                        exStatus.printStackTrace();
                                        System.err.println("更新订单状态为 待出行 失败: " + exStatus.getMessage());
                                    }

                                    try {
                                        java.util.List<com.example.airplanesalehouduan.passengers.entity.Ticket> relatedTickets =
                                                ticketRepository.findByOrderNo(out_trade_no);
                                        if (relatedTickets != null && !relatedTickets.isEmpty()) {
                                            for (com.example.airplanesalehouduan.passengers.entity.Ticket t : relatedTickets) {
                                                t.setStatus("已出票");
                                            }
                                            ticketRepository.saveAll(relatedTickets);
                                        }
                                    } catch (Exception exTicket) {
                                        exTicket.printStackTrace();
                                        System.err.println("更新机票状态为 已出票 失败: " + exTicket.getMessage());
                                    }
                                } else {
                                    System.err.println("未找到对应订单，orderNo=" + out_trade_no);
                                }
                            } catch (Exception exOrderLookup) {
                                exOrderLookup.printStackTrace();
                                System.err.println("尝试将 out_trade_no 作为已存在订单处理时出错: " + exOrderLookup.getMessage());
                            }
                        }
                    }
                }
            }

            // 跳转到个人中心页面
            return "redirect:http://localhost:5173/#/portal/passengers";
        } else {
            // 支付失败或未完成
            System.out.println("订单支付未完成或失败，状态=" + s);
            // 清理临时存储
            pendingBookings.remove(out_trade_no);
            return "redirect:http://localhost:5173/#/portal/passengers";
        }
    }

    /**
     * 后端发起退款请求（将钱退回给买家）
     * 前端传入 orderNo（系统订单号）和可选的 refundAmount、reason
     * 为了不破坏原有逻辑：此接口仅在找到订单后尝试调用支付宝退款接口并在支付宝返回成功码时更新订单状态为 已退款
     */

    // RefundRequest 已在文件前部声明，避免重复定义。

    /**
     * 预订请求 DTO（用于接收前端传入的预订信息）
     */
    public static class BookingRequest {
        private Integer passengerId;
        private Integer flightId;
        private BigDecimal totalAmount;
        private Integer usedPoints;
        private List<BookingService.PassengerInfo> passengers;
        private List<BookingService.CouponInfo> appliedCoupons;

        public Integer getPassengerId() {
            return passengerId;
        }

        public void setPassengerId(Integer passengerId) {
            this.passengerId = passengerId;
        }

        public Integer getFlightId() {
            return flightId;
        }

        public void setFlightId(Integer flightId) {
            this.flightId = flightId;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public Integer getUsedPoints() {
            return usedPoints;
        }

        public void setUsedPoints(Integer usedPoints) {
            this.usedPoints = usedPoints;
        }

        public List<BookingService.PassengerInfo> getPassengers() {
            return passengers;
        }

        public void setPassengers(List<BookingService.PassengerInfo> passengers) {
            this.passengers = passengers;
        }

        public List<BookingService.CouponInfo> getAppliedCoupons() {
            return appliedCoupons;
        }

        public void setAppliedCoupons(List<BookingService.CouponInfo> appliedCoupons) {
            this.appliedCoupons = appliedCoupons;
        }
    }
    /**
     * 行李支付请求 DTO（用于接收前端传入的行李登记并发起支付）
     */
    public static class BaggageRequest {
        private Integer passengerId;
        private Map<String, Object> baggage;
        private BigDecimal totalAmount;

        public Integer getPassengerId() {
            return passengerId;
        }

        public void setPassengerId(Integer passengerId) {
            this.passengerId = passengerId;
        }

        public Map<String, Object> getBaggage() {
            return baggage;
        }

        public void setBaggage(Map<String, Object> baggage) {
            this.baggage = baggage;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
    /**
     * 改签支付请求 DTO（用于接收前端传入的改签信息并发起支付）
     */
    public static class RescheduleRequest {
        private String orderNo;
        private BigDecimal changeFee;
        private BigDecimal priceDiff;
        private String reason;
        private Map<String, Object> newFlight;
        private Integer passengerId;
        private java.util.List<Map<String, Object>> passengers;
        private java.util.List<Map<String, Object>> seatAssignments;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public BigDecimal getChangeFee() {
            return changeFee;
        }

        public void setChangeFee(BigDecimal changeFee) {
            this.changeFee = changeFee;
        }

        public BigDecimal getPriceDiff() {
            return priceDiff;
        }

        public void setPriceDiff(BigDecimal priceDiff) {
            this.priceDiff = priceDiff;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Map<String, Object> getNewFlight() {
            return newFlight;
        }

        public void setNewFlight(Map<String, Object> newFlight) {
            this.newFlight = newFlight;
        }

        public Integer getPassengerId() {
            return passengerId;
        }

        public void setPassengerId(Integer passengerId) {
            this.passengerId = passengerId;
        }
        public java.util.List<Map<String, Object>> getPassengers() {
            return passengers;
        }

        public void setPassengers(java.util.List<Map<String, Object>> passengers) {
            this.passengers = passengers;
        }

        public java.util.List<Map<String, Object>> getSeatAssignments() {
            return seatAssignments;
        }

        public void setSeatAssignments(java.util.List<Map<String, Object>> seatAssignments) {
            this.seatAssignments = seatAssignments;
        }
    }
    /**
     * 取消支付请求 DTO（用于接收前端传入的取消申请并发起支付）
     */
    public static class CancelRequest {
        private String orderNo;
        private java.math.BigDecimal cancelFee;
        private java.math.BigDecimal refundFare;
        private String reason;
        private Integer passengerId;
        private String flightNo;
        private String applicantName;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public java.math.BigDecimal getCancelFee() {
            return cancelFee;
        }

        public void setCancelFee(java.math.BigDecimal cancelFee) {
            this.cancelFee = cancelFee;
        }

        public java.math.BigDecimal getRefundFare() {
            return refundFare;
        }

        public void setRefundFare(java.math.BigDecimal refundFare) {
            this.refundFare = refundFare;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Integer getPassengerId() {
            return passengerId;
        }

        public void setPassengerId(Integer passengerId) {
            this.passengerId = passengerId;
        }

        public String getFlightNo() {
            return flightNo;
        }

        public void setFlightNo(String flightNo) {
            this.flightNo = flightNo;
        }

        public String getApplicantName() {
            return applicantName;
        }

        public void setApplicantName(String applicantName) {
            this.applicantName = applicantName;
        }
    }
    /**
     * 列表快捷支付请求 DTO（用于对已有订单发起支付宝支付）
     */
    public static class PayOrderRequest {
        private String orderNo;
        private java.math.BigDecimal amount;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public java.math.BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(java.math.BigDecimal amount) {
            this.amount = amount;
        }
    }
    /**
     * 退款请求 DTO
     */
    public static class RefundRequest {
        private String orderNo;
        private java.math.BigDecimal refundAmount;
        private String reason;

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public java.math.BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(java.math.BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}