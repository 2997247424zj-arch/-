package com.example.airplanesalehouduan.passengers.controller;


import com.example.airplanesalehouduan.passengers.entity.Coupon;
import com.example.airplanesalehouduan.passengers.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券管理Controller
 */
@RestController
@RequestMapping("/api/passenger/coupons")

public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 获取优惠券列表
     * GET /api/passenger/coupons?passengerId=xxx&filter=全部
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCoupons(
            @RequestParam Integer passengerId,
            @RequestParam(required = false, defaultValue = "全部") String filter) {
        try {
            List<Map<String, Object>> coupons = couponService.getCoupons(passengerId, filter);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取优惠券列表成功");
            response.put("data", coupons);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取优惠券列表失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 领取优惠券
     * POST /api/passenger/coupons/{id}/receive?passengerId=xxx
     */
    @PostMapping("/{id}/receive")
    public ResponseEntity<Map<String, Object>> receiveCoupon(
            @PathVariable Long id,
            @RequestParam Integer passengerId) {
        try {
            Coupon coupon = couponService.receiveCoupon(passengerId, id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "领取优惠券成功");
            response.put("data", coupon);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "领取优惠券失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 使用优惠券
     * POST /api/passenger/coupons/{id}/use?passengerId=xxx
     */
    @PostMapping("/{id}/use")
    public ResponseEntity<Map<String, Object>> useCoupon(
            @PathVariable Long id,
            @RequestParam Integer passengerId) {
        try {
            Coupon coupon = couponService.useCoupon(passengerId, id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "使用优惠券成功");
            response.put("data", coupon);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "使用优惠券失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }
}

