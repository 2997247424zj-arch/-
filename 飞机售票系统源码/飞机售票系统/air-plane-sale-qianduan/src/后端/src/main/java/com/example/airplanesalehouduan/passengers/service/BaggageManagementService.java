package com.example.airplanesalehouduan.passengers.service;

import com.example.airplanesalehouduan.passengers.entity.BaggageManagement;
import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.other.BaggageManagementRepository;
import com.example.airplanesalehouduan.passengers.other.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 行李管理服务类
 * 负责处理行李相关的业务逻辑
 * 实现乘客数据隔离，确保用户只能查询自己的行李
 */
@Service
public class BaggageManagementService {

    @Autowired
    private BaggageManagementRepository baggageRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 生成行李编号
     * 格式：BG + 日期(yyyyMMdd) + 随机字符串(6位)
     */
    private String generateBaggageNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "BG" + dateStr + randomStr;
    }

    /**
     * 创建行李登记
     * @param passengerId 乘客ID
     * @param baggageData 行李数据
     * @return 创建的行李记录
     */
    @Transactional
    public BaggageManagement createBaggage(Integer passengerId, BaggageManagement baggageData) {
        // 验证订单是否存在且属于该乘客
        Optional<Order> orderOpt = orderRepository.findByPassengerIdAndOrderNo(
                passengerId, baggageData.getOrderno());
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("订单不存在或不属于当前用户");
        }

        Order order = orderOpt.get();

        // 创建行李记录
        BaggageManagement baggage = new BaggageManagement();
        baggage.setBaggageNo(generateBaggageNo());
        baggage.setPassengerId(passengerId);
        baggage.setPassengerName(order.getPassengerName());
        baggage.setOrderno(baggageData.getOrderno());
        baggage.setFlightNo(order.getFlightNo()); // 修复：使用getFlightNo()而不是getFlightNumber()
        baggage.setRoute(order.getRoute());
        baggage.setDepartureTime(order.getDepartureTime());
        // 设置航班到达时间：优先使用前端传递的值，如果没有则使用订单的到达时间
        if (baggageData.getArrivalTimeFlight() != null) {
            baggage.setArrivalTimeFlight(baggageData.getArrivalTimeFlight());
        } else if (order.getArrivalTime() != null) {
            baggage.setArrivalTimeFlight(order.getArrivalTime());
        }
        baggage.setBaggageType(baggageData.getBaggageType());
        baggage.setBaggageCount(baggageData.getBaggageCount());
        baggage.setTotalWeight(baggageData.getTotalWeight());
        baggage.setWeightLimit(baggageData.getWeightLimit());
        baggage.setDimensions(baggageData.getDimensions());
        baggage.setBaggageFee(baggageData.getBaggageFee() != null ? baggageData.getBaggageFee() : java.math.BigDecimal.ZERO);
        baggage.setExcessFee(baggageData.getExcessFee() != null ? baggageData.getExcessFee() : java.math.BigDecimal.ZERO);
        baggage.setStatus("registered");
        baggage.setDescription(baggageData.getDescription());
        baggage.setRemark(baggageData.getRemark());
        baggage.setRegisteredTime(LocalDateTime.now());

        return baggageRepository.save(baggage);
    }

    /**
     * 获取乘客的行李列表（分页，支持状态筛选）
     * @param passengerId 乘客ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param status 状态（可选）
     * @return 行李分页结果
     */
    public Page<BaggageManagement> getBaggageListByPassengerId(
            Integer passengerId,
            int page,
            int size,
            String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registeredTime"));

        if (status != null && !status.trim().isEmpty()) {
            return baggageRepository.findByPassengerIdAndStatusOrderByRegisteredTimeDesc(
                    passengerId, status, pageable);
        } else {
            return baggageRepository.findByPassengerIdOrderByRegisteredTimeDesc(passengerId, pageable);
        }
    }

    /**
     * 获取乘客的行李详情
     * @param passengerId 乘客ID
     * @param baggageId 行李ID
     * @return 行李详情
     */
    public BaggageManagement getBaggageDetail(Integer passengerId, Long baggageId) {
        Optional<BaggageManagement> baggageOpt = baggageRepository.findById(baggageId);
        if (baggageOpt.isEmpty()) {
            throw new RuntimeException("行李记录不存在");
        }

        BaggageManagement baggage = baggageOpt.get();
        if (!baggage.getPassengerId().equals(passengerId)) {
            throw new RuntimeException("无权访问该行李记录");
        }

        return baggage;
    }

    /**
     * 运营端：获取所有行李列表（带筛选和分页）
     * @param page 页码
     * @param size 每页大小
     * @param status 状态（可选）
     * @param baggageNo 行李编号（可选）
     * @param orderno 订单号（可选）
     * @param flightNo 航班号（可选）
     * @param passengerName 乘客姓名（可选）
     * @return 行李分页结果
     */
    public Page<BaggageManagement> getAllBaggageList(
            int page,
            int size,
            String status,
            String baggageNo,
            String orderno,
            String flightNo,
            String passengerName) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registeredTime"));

        Specification<BaggageManagement> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 状态筛选
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 行李编号搜索
            if (baggageNo != null && !baggageNo.trim().isEmpty()) {
                predicates.add(cb.like(root.get("baggageNo"), "%" + baggageNo + "%"));
            }

            // 订单号搜索
            if (orderno != null && !orderno.trim().isEmpty()) {
                predicates.add(cb.like(root.get("orderno"), "%" + orderno + "%"));
            }

            // 航班号搜索
            if (flightNo != null && !flightNo.trim().isEmpty()) {
                predicates.add(cb.like(root.get("flightNo"), "%" + flightNo + "%"));
            }

            // 乘客姓名搜索
            if (passengerName != null && !passengerName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("passengerName")),
                        "%" + passengerName.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return baggageRepository.findAll(spec, pageable);
    }

    /**
     * 运营端：获取行李详情
     * @param baggageId 行李ID
     * @return 行李详情
     */
    public BaggageManagement getBaggageDetailById(Long baggageId) {
        Optional<BaggageManagement> baggageOpt = baggageRepository.findById(baggageId);
        if (baggageOpt.isEmpty()) {
            throw new RuntimeException("行李记录不存在");
        }
        return baggageOpt.get();
    }

    /**
     * 运营端：更新行李状态
     * @param baggageId 行李ID
     * @param status 新状态
     * @param processedBy 处理人ID
     * @param operatorRemark 运营备注
     * @return 更新后的行李记录
     */
    @Transactional
    public BaggageManagement updateBaggageStatus(
            Long baggageId,
            String status,
            Integer processedBy,
            String operatorRemark) {
        Optional<BaggageManagement> baggageOpt = baggageRepository.findById(baggageId);
        if (baggageOpt.isEmpty()) {
            throw new RuntimeException("行李记录不存在");
        }

        BaggageManagement baggage = baggageOpt.get();
        baggage.setStatus(status);
        baggage.setProcessedBy(processedBy);
        baggage.setProcessedAt(LocalDateTime.now());
        if (operatorRemark != null && !operatorRemark.trim().isEmpty()) {
            baggage.setOperatorRemark(operatorRemark);
        }

        // 根据状态更新相应的时间字段
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case "checked_in":
                if (baggage.getCheckedInTime() == null) {
                    baggage.setCheckedInTime(now);
                }
                break;
            case "arrived":
                if (baggage.getArrivalTimeBaggage() == null) {
                    baggage.setArrivalTimeBaggage(now);
                }
                break;
            case "delivered":
                if (baggage.getDeliveredTime() == null) {
                    baggage.setDeliveredTime(now);
                }
                break;
        }

        return baggageRepository.save(baggage);
    }

    /**
     * 乘客端：提取行李（只能在运营标记为 arrived / 已到达 后提取）
     * @param passengerId 乘客ID
     * @param baggageId 行李ID
     * @return 更新后的行李记录
     */
    @Transactional
    public BaggageManagement passengerPickup(Integer passengerId, Long baggageId) {
        Optional<BaggageManagement> baggageOpt = baggageRepository.findById(baggageId);
        if (baggageOpt.isEmpty()) {
            throw new RuntimeException("行李记录不存在");
        }

        BaggageManagement baggage = baggageOpt.get();
        // 权限校验：仅允许该乘客操作自己的行李
        if (!baggage.getPassengerId().equals(passengerId)) {
            throw new RuntimeException("无权操作该行李");
        }

        // 状态校验：只有已到达(arrived)才能提取
        if (!"arrived".equals(baggage.getStatus())) {
            throw new RuntimeException("当前行李不可提取（仅当行李状态为已到达时可提取）");
        }

        // 更新状态为 delivered（已提取），并记录提取时间与处理者（记录为乘客本人）
        baggage.setStatus("delivered");
        baggage.setProcessedBy(passengerId);
        baggage.setProcessedAt(LocalDateTime.now());
        if (baggage.getDeliveredTime() == null) {
            baggage.setDeliveredTime(LocalDateTime.now());
        }

        return baggageRepository.save(baggage);
    }
}

