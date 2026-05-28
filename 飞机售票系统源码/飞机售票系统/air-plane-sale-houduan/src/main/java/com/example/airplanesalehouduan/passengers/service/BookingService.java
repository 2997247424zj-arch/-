package com.example.airplanesalehouduan.passengers.service;

import com.example.airplanesalehouduan.passengers.entity.Flight;
import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.entity.Ticket;
import com.example.airplanesalehouduan.passengers.other.UserFlightRepository;
import com.example.airplanesalehouduan.passengers.other.OrderRepository;
import com.example.airplanesalehouduan.passengers.other.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.airplanesalehouduan.passengers.other.LoyaltyPointsRepository;
import com.example.airplanesalehouduan.passengers.entity.LoyaltyPoints;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 预订服务类
 * 负责处理航班预订业务逻辑，包括创建订单、机票和积分记录
 */
@Service
public class BookingService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserFlightRepository flightRepository;

    @Autowired
    private PointsService pointsService;

    @Autowired
    private LoyaltyPointsRepository loyaltyPointsRepository;

    @Autowired
    private TicketDocumentService ticketDocumentService;

    /**
     * 预订航班
     * @param passengerId 乘客ID
     * @param flightId 航班ID
     * @param passengers 乘客信息列表
     * @param totalAmount 订单总金额
     * @param usedPoints 使用的积分（可选，如果为null或0则不使用积分）
     * @param appliedCoupons 已应用的优惠券列表（可选）
     * @return 订单对象
     */
    @Transactional
    public Order bookFlight(Integer passengerId, Integer flightId, List<PassengerInfo> passengers,
                            BigDecimal totalAmount, Integer usedPoints, List<CouponInfo> appliedCoupons) {
        // 1. 查询航班信息
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("航班不存在"));

        // 2. 生成订单号
        String orderNo = generateOrderNo();

        // 3. 构建航线信息
        String route = flight.getOriginAirport() + " → " + flight.getDestAirport();

        // 4. 创建订单
        Order order = new Order();
        order.setPassengerId(passengerId);
        order.setOrderNo(orderNo);
        order.setPassengerName(passengers.get(0).getName()); // 使用第一个乘客的姓名
        order.setRoute(route);
        order.setFlightNo(flight.getFlightNo());
        order.setDepartureTime(flight.getSchedDepTime());
        order.setArrivalTime(flight.getSchedArrTime());
        order.setTotalAmount(totalAmount);
        order.setStatus("待出行"); // 订单状态设置为"待出行"
        order = orderRepository.save(order);

        // 5. 计算订单级别的总优惠（用于在机票上记录订单级的 discount_fee）
        java.math.BigDecimal totalDiscount = java.math.BigDecimal.ZERO;
        if (appliedCoupons != null && !appliedCoupons.isEmpty()) {
            for (CouponInfo c : appliedCoupons) {
                try {
                    if (c != null && c.getAppliedAmount() != null) {
                        totalDiscount = totalDiscount.add(java.math.BigDecimal.valueOf(c.getAppliedAmount()));
                    }
                } catch (Exception ignore) {}
            }
        }

        // 5. 为每个乘客创建机票（机票上的 totalPrice 与 discountFee 使用订单级的数值）
        List<Ticket> tickets = new ArrayList<>();
        for (PassengerInfo passenger : passengers) {
            Ticket ticket = createTicket(order, flight, passenger, appliedCoupons, totalAmount, totalDiscount);
            tickets.add(ticketRepository.save(ticket));
        }

        // 6. 更新订单的ticket_no字段，将所有机票号用逗号分隔
        if (!tickets.isEmpty()) {
            String ticketNos = tickets.stream()
                    .map(Ticket::getTicketNo)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            order.setTicketNo(ticketNos);
            order = orderRepository.save(order);
        }

        // 7. 如果使用了积分，记录积分消费（确保 ref_order_id 被写入）
        if (usedPoints != null && usedPoints > 0) {
            try {
                LoyaltyPoints spendRecord = pointsService.addPoints(passengerId, -usedPoints, "SPEND", order.getId(),
                        "购票使用积分：" + usedPoints + "分");
                // 防御性检查：若保存后的记录未包含 refOrderId，则补写一遍（部分环境下可能存在持久化延迟或映射异常）
                try {
                    if (spendRecord != null && spendRecord.getRefOrderId() == null) {
                        spendRecord.setRefOrderId(order.getId());
                        loyaltyPointsRepository.save(spendRecord);
                    }
                } catch (Exception ignoreInner) {
                    // 记录失败不阻塞购票流程
                    System.err.println("尝试补写 loyalty_points.ref_order_id 失败: " + ignoreInner.getMessage());
                }
            } catch (Exception e) {
                // 记录失败不阻塞购票流程，但输出日志以便排查
                System.err.println("记录积分消费失败: " + e.getMessage());
            }
        }

        // 8. 购票成功后，根据订单金额给予积分奖励（每1元=1积分）
        int earnedPoints = totalAmount.intValue();
        if (earnedPoints > 0) {
            pointsService.addPoints(passengerId, earnedPoints, "EARN", order.getId(),
                    "购票获得积分：" + earnedPoints + "分");
        }

        // 注意：机票文档生成由前端在预订成功后自动下载，不在此处生成

        return order;
    }

    /**
     * 创建机票
     */
    private Ticket createTicket(Order order, Flight flight, PassengerInfo passenger, List<CouponInfo> appliedCoupons, java.math.BigDecimal orderTotalAmount, java.math.BigDecimal orderTotalDiscount) {
        Ticket ticket = new Ticket();
        ticket.setOrderNo(order.getOrderNo());
        ticket.setPassengerId(order.getPassengerId());
        ticket.setPassengerName(passenger.getName());
        ticket.setIdCard(passenger.getIdCard());
        ticket.setPhone(passenger.getPhone());
        ticket.setFlightId(flight.getId());
        ticket.setFlightNo(flight.getFlightNo());
        ticket.setOriginAirport(flight.getOriginAirport());
        ticket.setDestAirport(flight.getDestAirport());
        ticket.setRoute(order.getRoute());
        ticket.setDepartureTime(flight.getSchedDepTime());
        ticket.setArrivalTime(flight.getSchedArrTime());
        ticket.setSeatNumber(passenger.getSeatNumber());
        // 将seat_class转换为中文（经济舱、商务舱、头等舱）
        ticket.setSeatClass(convertSeatClassToChinese(passenger.getSeatClass()));

        // 计算价格
        BigDecimal basePrice = flight.getPrice() != null ? flight.getPrice() : BigDecimal.valueOf(680.0);
        BigDecimal seatFee = passenger.getSeatFee() != null ? passenger.getSeatFee() : BigDecimal.ZERO;

        // 计算优惠金额与总价：按需求使用订单级的总优惠与总价记录到机票上（不改变其他流程）
        BigDecimal discountFee = (orderTotalDiscount != null) ? orderTotalDiscount : BigDecimal.ZERO;
        BigDecimal totalPrice = (orderTotalAmount != null) ? orderTotalAmount : basePrice.add(seatFee).subtract(discountFee);

        ticket.setBasePrice(basePrice);
        ticket.setSeatFee(seatFee);
        ticket.setDiscountFee(discountFee);
        ticket.setTotalPrice(totalPrice);
        ticket.setTicketNo(generateTicketNo());
        ticket.setStatus("已出票");

        return ticket;
    }

    /**
     * 生成订单号
     * 格式：ORD + 时间戳 + 随机数
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + timestamp + random;
    }

    /**
     * 生成机票号
     * 格式：TKT + 时间戳 + 随机数
     */
    private String generateTicketNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "TKT" + timestamp + random;
    }

    /**
     * 将座位等级转换为中文
     * @param seatClass 座位等级（economy/business/first 或中文）
     * @return 中文座位等级（经济舱/商务舱/头等舱）
     */
    private String convertSeatClassToChinese(String seatClass) {
        if (seatClass == null || seatClass.trim().isEmpty()) {
            return "经济舱";
        }
        String lowerSeatClass = seatClass.toLowerCase().trim();
        switch (lowerSeatClass) {
            case "economy":
            case "经济舱":
                return "经济舱";
            case "business":
            case "商务舱":
                return "商务舱";
            case "first":
            case "头等舱":
                return "头等舱";
            default:
                return seatClass; // 如果已经是中文或其他格式，直接返回
        }
    }

    /**
     * 乘客信息内部类
     */
    public static class PassengerInfo {
        private String name;
        private String idCard;
        private String phone;
        private String seatNumber;
        private String seatClass;
        private BigDecimal seatFee;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }

        public String getSeatClass() {
            return seatClass;
        }

        public void setSeatClass(String seatClass) {
            this.seatClass = seatClass;
        }

        public BigDecimal getSeatFee() {
            return seatFee;
        }

        public void setSeatFee(BigDecimal seatFee) {
            this.seatFee = seatFee;
        }
    }

    /**
     * 优惠券信息内部类
     */
    public static class CouponInfo {
        private Long id;
        private String name;
        private String type; // cash 或 percentage
        private Double appliedAmount; // 实际应用的优惠金额

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getAppliedAmount() {
            return appliedAmount;
        }

        public void setAppliedAmount(Double appliedAmount) {
            this.appliedAmount = appliedAmount;
        }
    }
}

