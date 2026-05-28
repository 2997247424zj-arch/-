package com.example.airplanesalehouduan.passengers.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.passengers.service.JuheImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 提供手动触发从聚合数据导入航班的接口
 */
@RestController
@RequestMapping("/api/flights")
public class FlightImportController {

    @Autowired
    private JuheImportService juheImportService;

    /**
     * 导入并写入航班数据（前端调用）
     * 参数：
     *   departure: 出发地（三字码或城市名）
     *   arrival: 到达地（三字码或城市名）
     *   departureDate: yyyy-MM-dd
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<?>> importFlights(
            @RequestParam String departure,
            @RequestParam String arrival,
            @RequestParam String departureDate) {
        try {
            Map<String, Integer> res = juheImportService.importAndSave(departure, arrival, departureDate);
            return ResponseEntity.ok(ApiResponse.success("导入完成", res));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("导入失败: " + e.getMessage()));
        }
    }
}



