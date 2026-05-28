package com.example.airplanesalehouduan.passengers.controller;



import com.example.airplanesalehouduan.passengers.entity.LoyaltyPoints;
import com.example.airplanesalehouduan.passengers.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分管理Controller
 */
@RestController
@RequestMapping("/api/passenger/points")

public class PointsController {

    @Autowired
    private PointsService pointsService;

    /**
     * 获取用户积分信息
     * GET /api/passenger/points?passengerId=xxx
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPointsInfo(@RequestParam Integer passengerId) {
        try {
            Map<String, Object> pointsInfo = pointsService.getPointsInfo(passengerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取积分信息成功");
            response.put("data", pointsInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取积分信息失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取积分记录（分页）
     * GET /api/passenger/points/history?passengerId=xxx&page=1&size=10
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getPointsHistory(
            @RequestParam Integer passengerId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            // 使用原生SQL查询，直接获取数据库中的created_at字符串，避免时区转换
            Map<String, Object> historyData = pointsService.getPointsHistory(passengerId, page, size);

            // 转换为前端需要的格式
            Map<String, Object> data = new HashMap<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawRecords = (List<Map<String, Object>>) historyData.get("records");
            data.put("records", rawRecords.stream().map(rawRecord -> {
                Map<String, Object> record = new HashMap<>();
                record.put("id", rawRecord.get("id"));

                String changeType = (String) rawRecord.get("change_type");
                String remark = (String) rawRecord.get("remark");
                record.put("title", getTitleByChangeType(changeType, remark));

                // description直接使用remark字段
                record.put("description", remark != null ? remark : "");

                // date直接使用数据库中的created_at_str（通过DATE_FORMAT获取的原始字符串）
                // 不进行任何时区转换，完全使用数据库中的原始值
                String createdAtStr = (String) rawRecord.get("created_at_str");
                record.put("date", createdAtStr != null ? createdAtStr : "");

                record.put("points", rawRecord.get("points"));
                record.put("type", "EARN".equals(changeType) ? "earn" : "spend");
                record.put("icon", "EARN".equals(changeType) ? "➕" : "➖");
                return record;
            }).toList());
            data.put("total", historyData.get("total"));
            data.put("page", historyData.get("page"));
            data.put("size", historyData.get("size"));
            data.put("totalPages", historyData.get("totalPages"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取积分记录成功");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取积分记录失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 积分兑换
     * POST /api/passenger/points/exchange
     */
    @PostMapping("/exchange")
    public ResponseEntity<Map<String, Object>> exchangePoints(@RequestBody Map<String, Object> request) {
        try {
            Integer passengerId = (Integer) request.get("passengerId");
            String exchangeType = (String) request.get("exchangeType");
            Integer requiredPoints = (Integer) request.get("requiredPoints");
            String itemName = (String) request.get("itemName");

            if (passengerId == null || exchangeType == null || requiredPoints == null || itemName == null) {
                throw new RuntimeException("参数不完整");
            }

            LoyaltyPoints pointsRecord = pointsService.exchangePoints(passengerId, exchangeType, requiredPoints, itemName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "积分兑换成功");
            response.put("data", pointsRecord);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "积分兑换失败：" + e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据变动类型生成标题
     */
    private String getTitleByChangeType(String changeType, String remark) {
        if (changeType == null) {
            return "积分变动";
        }

        switch (changeType) {
            case "EARN":
                if (remark != null && remark.contains("购票")) {
                    return "购票获得积分";
                } else if (remark != null && remark.contains("推荐")) {
                    return "推荐好友奖励";
                } else if (remark != null && remark.contains("活动")) {
                    return "活动奖励";
                } else if (remark != null && remark.contains("生日")) {
                    return "生日礼物";
                }
                return "获得积分";
            case "SPEND":
                if (remark != null && remark.contains("兑换")) {
                    return "积分兑换" + (remark.contains("代金券") ? "代金券" :
                            remark.contains("升舱") ? "升舱券" : "权益");
                }
                return "积分消费";
            default:
                return "积分变动";
        }
    }
}

