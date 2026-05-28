package com.example.airplanesalehouduan.passengers.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.passengers.entity.BaggageManagement;
import com.example.airplanesalehouduan.passengers.service.BaggageManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 行李管理控制器（乘客端）
 * 提供普通乘客的行李管理接口
 * 实现数据隔离，确保用户只能查询和管理自己的行李
 */
@RestController
@RequestMapping("/api")
public class BaggageManagementController {

    @Autowired
    private BaggageManagementService baggageService;

    /**
     * 创建行李登记
     * 接口路径：POST /api/baggage
     *
     * @param requestBody 请求体，包含passengerId和行李数据
     * @return 创建的行李记录
     */
    @PostMapping("/baggage")
    public ResponseEntity<ApiResponse<?>> createBaggage(@RequestBody Map<String, Object> requestBody) {
        try {
            // 从请求体中获取passengerId
            Integer passengerId = null;
            if (requestBody.containsKey("passengerId")) {
                Object pid = requestBody.get("passengerId");
                if (pid instanceof Integer) {
                    passengerId = (Integer) pid;
                } else if (pid instanceof String) {
                    passengerId = Integer.parseInt((String) pid);
                } else if (pid instanceof Number) {
                    passengerId = ((Number) pid).intValue();
                }
            }

            if (passengerId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("乘客ID不能为空"));
            }

            // 构建BaggageManagement对象
            BaggageManagement baggageData = new BaggageManagement();
            if (requestBody.containsKey("orderno")) {
                baggageData.setOrderno((String) requestBody.get("orderno"));
            }
            if (requestBody.containsKey("baggageType")) {
                baggageData.setBaggageType((String) requestBody.get("baggageType"));
            }
            if (requestBody.containsKey("baggageCount")) {
                Object count = requestBody.get("baggageCount");
                if (count instanceof Integer) {
                    baggageData.setBaggageCount((Integer) count);
                } else if (count instanceof Number) {
                    baggageData.setBaggageCount(((Number) count).intValue());
                }
            }
            if (requestBody.containsKey("totalWeight")) {
                Object weight = requestBody.get("totalWeight");
                if (weight != null) {
                    if (weight instanceof Number) {
                        baggageData.setTotalWeight(java.math.BigDecimal.valueOf(((Number) weight).doubleValue()));
                    }
                }
            }
            if (requestBody.containsKey("weightLimit")) {
                Object weightLimit = requestBody.get("weightLimit");
                if (weightLimit != null) {
                    if (weightLimit instanceof Number) {
                        baggageData.setWeightLimit(java.math.BigDecimal.valueOf(((Number) weightLimit).doubleValue()));
                    }
                }
            }
            if (requestBody.containsKey("baggageFee")) {
                Object baggageFee = requestBody.get("baggageFee");
                if (baggageFee != null) {
                    if (baggageFee instanceof Number) {
                        baggageData.setBaggageFee(java.math.BigDecimal.valueOf(((Number) baggageFee).doubleValue()));
                    }
                }
            }
            if (requestBody.containsKey("excessFee")) {
                Object excessFee = requestBody.get("excessFee");
                if (excessFee != null) {
                    if (excessFee instanceof Number) {
                        baggageData.setExcessFee(java.math.BigDecimal.valueOf(((Number) excessFee).doubleValue()));
                    }
                }
            }
            if (requestBody.containsKey("dimensions")) {
                baggageData.setDimensions((String) requestBody.get("dimensions"));
            }
            if (requestBody.containsKey("description")) {
                baggageData.setDescription((String) requestBody.get("description"));
            }
            if (requestBody.containsKey("remark")) {
                baggageData.setRemark((String) requestBody.get("remark"));
            }
            // 处理到达时间字段（支持两种字段名：arrival_time_flight 和 arrivalTimeFlight）
            Object arrivalTimeObj = null;
            if (requestBody.containsKey("arrival_time_flight")) {
                arrivalTimeObj = requestBody.get("arrival_time_flight");
            } else if (requestBody.containsKey("arrivalTimeFlight")) {
                arrivalTimeObj = requestBody.get("arrivalTimeFlight");
            }

            if (arrivalTimeObj != null && arrivalTimeObj instanceof String) {
                try {
                    String timeStr = ((String) arrivalTimeObj).trim();
                    // 移除时区信息（Z, +08:00等）
                    if (timeStr.endsWith("Z")) {
                        timeStr = timeStr.substring(0, timeStr.length() - 1);
                    }
                    // 移除时区偏移（+08:00, -05:00等）
                    int timezoneIndex = timeStr.indexOf("+");
                    if (timezoneIndex == -1) {
                        timezoneIndex = timeStr.indexOf("-", 10); // 从第10个字符开始查找（避免匹配日期中的-）
                    }
                    if (timezoneIndex > 0) {
                        timeStr = timeStr.substring(0, timezoneIndex);
                    }
                    // 将T替换为空格，统一格式
                    timeStr = timeStr.replace("T", " ");
                    // 解析日期时间
                    java.time.format.DateTimeFormatter formatter;
                    if (timeStr.length() == 16) {
                        // 格式：yyyy-MM-dd HH:mm
                        formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    } else {
                        // 格式：yyyy-MM-dd HH:mm:ss
                        formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    }
                    baggageData.setArrivalTimeFlight(java.time.LocalDateTime.parse(timeStr, formatter));
                } catch (Exception e) {
                    // 如果解析失败，记录日志但不中断流程，后续会从订单中获取
                    System.err.println("解析到达时间失败: " + e.getMessage() + ", 将使用订单的到达时间");
                }
            }

            BaggageManagement baggage = baggageService.createBaggage(passengerId, baggageData);
            return ResponseEntity.ok(ApiResponse.success("行李登记成功", baggage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("登记失败: " + e.getMessage()));
        }
    }

    /**
     * 获取我的行李列表（分页）
     * 接口路径：GET /api/baggage
     *
     * @param passengerId 乘客ID（从请求参数获取）
     * @param page 页码（可选，默认0）
     * @param size 每页大小（可选，默认10）
     * @param status 状态（可选，如：registered, checked_in, in_transit, arrived, delivered, lost, damaged, delayed）
     * @return 行李列表和分页信息
     */
    @GetMapping("/baggage")
    public ResponseEntity<ApiResponse<?>> getBaggageList(
            @RequestParam(required = true) Integer passengerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            Page<BaggageManagement> baggagePage = baggageService.getBaggageListByPassengerId(
                    passengerId, page, size, status);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", baggagePage.getContent());
            responseData.put("total", baggagePage.getTotalElements());
            responseData.put("page", baggagePage.getNumber());
            responseData.put("size", baggagePage.getSize());
            responseData.put("totalPages", baggagePage.getTotalPages());
            responseData.put("hasNext", baggagePage.hasNext());
            responseData.put("hasPrevious", baggagePage.hasPrevious());

            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取行李详情
     * 接口路径：GET /api/baggage/{id}
     *
     * @param id 行李ID
     * @param passengerId 乘客ID（从请求参数获取，用于验证权限）
     * @return 行李详情
     */
    @GetMapping("/baggage/{id}")
    public ResponseEntity<ApiResponse<?>> getBaggageDetail(
            @PathVariable Long id,
            @RequestParam(required = true) Integer passengerId) {
        try {
            BaggageManagement baggage = baggageService.getBaggageDetail(passengerId, id);
            return ResponseEntity.ok(ApiResponse.success("查询成功", baggage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 乘客端：提取行李（仅当运营标记为 arrived 时允许）
     * 接口路径：POST /api/baggage/{id}/pickup
     *
     * @param id 行李ID
     * @param requestBody 请求体，包含 passengerId（兼容前端兼容性）
     * @return 更新后的行李记录
     */
    @PostMapping("/baggage/{id}/pickup")
    public ResponseEntity<ApiResponse<?>> pickupBaggage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Integer passengerId = null;
            if (requestBody != null && requestBody.containsKey("passengerId")) {
                Object pid = requestBody.get("passengerId");
                if (pid instanceof Integer) passengerId = (Integer) pid;
                else if (pid instanceof Number) passengerId = ((Number) pid).intValue();
                else if (pid instanceof String) passengerId = Integer.parseInt((String) pid);
            }

            if (passengerId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("passengerId 为必填参数"));
            }

            BaggageManagement updated = baggageService.passengerPickup(passengerId, id);
            return ResponseEntity.ok(ApiResponse.success("提取成功", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("提取失败: " + e.getMessage()));
        }
    }
}

