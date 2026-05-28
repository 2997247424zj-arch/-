package com.example.airplanesalehouduan.passengers.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.passengers.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 座位控制器
 * 处理座位相关的API请求
 */
@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    /**
     * 根据航班ID获取座位列表
     * GET /api/seats/flight/{flightId}
     */
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<ApiResponse<?>> getSeatsByFlightId(@PathVariable Integer flightId) {
        try {
            List<Map<String, Object>> seats = seatService.getSeatsByFlightId(flightId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("seats", seats);
            responseData.put("total", seats.size());
            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询座位失败：" + e.getMessage()));
        }
    }

    /**
     * 根据航班ID获取座位布局（包含机型布局信息和实际座位状态）
     * GET /api/seats/flight/{flightId}/layout
     */
    @GetMapping("/flight/{flightId}/layout")
    public ResponseEntity<ApiResponse<?>> getSeatLayoutByFlightId(@PathVariable Integer flightId) {
        try {
            Map<String, Object> layout = seatService.getSeatLayoutByFlightId(flightId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", layout));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询座位布局失败：" + e.getMessage()));
        }
    }

    /**
     * 根据航班ID和舱位等级获取可用座位数
     * GET /api/seats/flight/{flightId}/available-count?cabinClass=economy
     */
    @GetMapping("/flight/{flightId}/available-count")
    public ResponseEntity<ApiResponse<?>> getAvailableSeatCount(
            @PathVariable Integer flightId,
            @RequestParam(required = false) String cabinClass) {
        try {
            Long count = seatService.getAvailableSeatCountByCabinClass(flightId, cabinClass);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("count", count);
            responseData.put("cabinClass", cabinClass);
            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询可用座位数失败：" + e.getMessage()));
        }
    }

    /**
     * 为航班创建座位（根据机型布局）
     * POST /api/seats/flight/{flightId}/create
     */
    @PostMapping("/flight/{flightId}/create")
    public ResponseEntity<ApiResponse<?>> createSeatsForFlight(@PathVariable Integer flightId) {
        try {
            seatService.createSeatsForFlight(flightId);
            return ResponseEntity.ok(ApiResponse.success("座位创建成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("创建座位失败：" + e.getMessage()));
        }
    }

    /**
     * 批量更新座位状态（例如：将选中的座位设置为 occupied）
     * POST /api/seats/flight/{flightId}/status
     * body: { "seatIds": [1,2,3], "status": "occupied" }
     */
    @PostMapping("/flight/{flightId}/status")
    public ResponseEntity<ApiResponse<?>> updateSeatStatus(
            @PathVariable Integer flightId,
            @RequestBody Map<String, Object> body) {
        try {
            Object idsObj = body.get("seatIds");
            Object statusObj = body.get("status");
            if (!(idsObj instanceof List) || !(statusObj instanceof String)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("参数 seatIds 或 status 缺失"));
            }

            @SuppressWarnings("unchecked")
            List<Number> rawIds = (List<Number>) idsObj;
            String status = (String) statusObj;

            List<Integer> seatIds = rawIds.stream()
                    .map(Number::intValue)
                    .toList();

            seatService.updateSeatStatus(flightId, seatIds, status);
            return ResponseEntity.ok(ApiResponse.success("更新座位状态成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新座位状态失败：" + e.getMessage()));
        }
    }
}

