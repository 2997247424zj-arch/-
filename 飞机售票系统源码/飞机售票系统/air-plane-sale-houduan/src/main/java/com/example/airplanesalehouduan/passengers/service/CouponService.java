package com.example.airplanesalehouduan.passengers.service;



import com.example.airplanesalehouduan.passengers.entity.Coupon;
import com.example.airplanesalehouduan.passengers.other.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 优惠券服务类
 */
@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    /**
     * 获取优惠券列表（根据筛选条件）
     */
    public List<Map<String, Object>> getCoupons(Integer passengerId, String filter) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> coupons;

        if ("可用".equals(filter) || "available".equalsIgnoreCase(filter)) {
            coupons = couponRepository.findAvailableCoupons(passengerId, now);
        } else if ("未领取".equals(filter) || "unused".equalsIgnoreCase(filter)) {
            coupons = couponRepository.findUnusedCoupons(passengerId, now);
        } else if ("已过期".equals(filter) || "expired".equalsIgnoreCase(filter)) {
            coupons = couponRepository.findExpiredCoupons(passengerId, now);
        } else {
            // 全部
            coupons = couponRepository.findAllByPassengerId(passengerId);
        }

        // 转换为前端需要的格式
        return coupons.stream().map(coupon -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", coupon.getId());
            map.put("code", coupon.getCode());
            map.put("name", coupon.getName() != null ? coupon.getName() : "优惠券");

            // 折扣类型和折扣值
            map.put("discount_type", coupon.getDiscountType());
            map.put("discount_value", coupon.getDiscountValue());

            // 处理描述：如果是百分比折扣，确保描述中包含折数信息
            String description = coupon.getDescription();
            if (description != null && ("百分比折扣".equals(coupon.getDiscountType()) ||
                    "PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType()) ||
                    "percentage".equalsIgnoreCase(coupon.getDiscountType()))) {
                // 计算折数：0.9 → 9折, 0.85 → 8.5折
                BigDecimal discountValue = coupon.getDiscountValue();
                double discountDouble = discountValue.doubleValue();
                String discountText;

                if (discountDouble < 1 && discountDouble > 0) {
                    // 小数格式，转换为折数
                    double zhe = discountDouble * 10;
                    if (zhe % 1 == 0) {
                        discountText = String.format("%.0f折", zhe);
                    } else {
                        discountText = String.format("%.1f折", zhe);
                    }
                } else {
                    // 已经是百分比值（如90），转换为折数
                    double zhe = discountDouble / 10;
                    if (zhe % 1 == 0) {
                        discountText = String.format("%.0f折", zhe);
                    } else {
                        discountText = String.format("%.1f折", zhe);
                    }
                }

                // 如果描述中已经有折数，更新为正确的折数
                if (description.matches(".*\\d+(\\.\\d+)?折.*")) {
                    description = description.replaceAll("\\d+(\\.\\d+)?折", discountText);
                }
                // 如果描述中有百分比信息，替换为折数
                else if (description.matches(".*\\d+%.*")) {
                    description = description.replaceAll("\\d+%", discountText);
                }
                // 如果描述中没有折数信息，添加折数信息
                else if (!description.contains("折")) {
                    // 在描述末尾添加折数信息
                    description = description + "（" + discountText + "优惠）";
                }
            }
            map.put("description", description);

            // 金额显示（用于兼容）
            map.put("amount", coupon.getDiscountValue().intValue());
            map.put("minAmount", coupon.getMinSpend() != null ? coupon.getMinSpend().intValue() : 0);
            map.put("min_spend", coupon.getMinSpend() != null ? coupon.getMinSpend() : BigDecimal.ZERO);

            // 有效期开始和结束日期 - 使用下划线命名以匹配前端
            // 格式化为 "YYYY-MM-DD HH:mm:ss" 格式
            if (coupon.getValidFrom() != null) {
                LocalDateTime validFrom = coupon.getValidFrom();
                String validFromStr = String.format("%04d-%02d-%02d %02d:%02d:%02d",
                        validFrom.getYear(), validFrom.getMonthValue(), validFrom.getDayOfMonth(),
                        validFrom.getHour(), validFrom.getMinute(), validFrom.getSecond());
                map.put("valid_from", validFromStr);
            }

            if (coupon.getValidTo() != null) {
                LocalDateTime validTo = coupon.getValidTo();
                String validToStr = String.format("%04d-%02d-%02d %02d:%02d:%02d",
                        validTo.getYear(), validTo.getMonthValue(), validTo.getDayOfMonth(),
                        validTo.getHour(), validTo.getMinute(), validTo.getSecond());
                map.put("valid_to", validToStr);
                // 同时保留expiryDate字段以兼容旧代码
                map.put("expiryDate", validTo.toString().substring(0, 10));
            }

            // 原始状态
            map.put("status", coupon.getStatus());

            // 注意：前端会根据valid_from和valid_to自动计算状态，这里保留原始状态用于参考
            // 前端会优先检查日期范围来判断状态
            String statusText;
            if (coupon.getStatus() == null || "UNUSED".equals(coupon.getStatus())) {
                if (now.isAfter(coupon.getValidTo())) {
                    statusText = "已过期";
                } else {
                    statusText = "未领取";
                }
            } else if ("AVAILABLE".equals(coupon.getStatus())) {
                if (now.isAfter(coupon.getValidTo())) {
                    statusText = "已过期";
                } else {
                    statusText = "可用";
                }
            } else if ("USED".equals(coupon.getStatus())) {
                statusText = "已使用";
            } else {
                statusText = "已过期";
            }
            map.put("statusText", statusText);

            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 领取优惠券
     */
    @Transactional
    public Coupon receiveCoupon(Integer passengerId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("优惠券不存在"));

        // 检查是否是通用券或专属券
        if (coupon.getPassengerId() != null && !coupon.getPassengerId().equals(passengerId)) {
            throw new RuntimeException("该优惠券不属于您");
        }

        // 检查是否已领取
        if ("AVAILABLE".equals(coupon.getStatus()) || "USED".equals(coupon.getStatus())) {
            throw new RuntimeException("该优惠券已领取或已使用");
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(coupon.getValidTo())) {
            coupon.setStatus("EXPIRED");
            couponRepository.save(coupon);
            throw new RuntimeException("该优惠券已过期");
        }

        // 如果是通用券，需要创建专属券副本
        if (coupon.getPassengerId() == null) {
            // 创建专属券
            Coupon personalCoupon = new Coupon();
            personalCoupon.setCode(coupon.getCode() + "_" + passengerId);
            personalCoupon.setPassengerId(passengerId);
            personalCoupon.setDiscountType(coupon.getDiscountType());
            personalCoupon.setDiscountValue(coupon.getDiscountValue());
            personalCoupon.setMinSpend(coupon.getMinSpend());
            personalCoupon.setValidFrom(coupon.getValidFrom());
            personalCoupon.setValidTo(coupon.getValidTo());
            personalCoupon.setStatus("AVAILABLE");
            personalCoupon.setName(coupon.getName());
            personalCoupon.setDescription(coupon.getDescription());
            return couponRepository.save(personalCoupon);
        } else {
            // 专属券直接更新状态
            coupon.setStatus("AVAILABLE");
            return couponRepository.save(coupon);
        }
    }

    /**
     * 使用优惠券
     */
    @Transactional
    public Coupon useCoupon(Integer passengerId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("优惠券不存在"));

        // 检查是否属于该乘客
        if (coupon.getPassengerId() == null || !coupon.getPassengerId().equals(passengerId)) {
            throw new RuntimeException("该优惠券不属于您");
        }

        // 检查状态
        if (!"AVAILABLE".equals(coupon.getStatus())) {
            throw new RuntimeException("该优惠券不可用");
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(coupon.getValidTo())) {
            coupon.setStatus("EXPIRED");
            couponRepository.save(coupon);
            throw new RuntimeException("该优惠券已过期");
        }

        // 标记为已使用
        coupon.setStatus("USED");
        return couponRepository.save(coupon);
    }
}
