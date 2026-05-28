package com.example.airplanesalehouduan.passengers.controller;



import com.example.airplanesalehouduan.Login.dto.ApiResponse;

import com.example.airplanesalehouduan.passengers.other.FrequentPassengerRequest;
import com.example.airplanesalehouduan.passengers.other.FrequentPassengerResponse;
import com.example.airplanesalehouduan.passengers.service.FrequentPassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 常用乘客控制器
 * 提供常用乘客的CRUD接口
 *
 * 注意：所有接口都需要从请求头或请求参数中获取userId
 * 实际项目中应该从session或token中获取，这里为了简化，从请求参数获取
 * 生产环境应该使用拦截器或过滤器来验证用户身份
 */
@RestController
@RequestMapping("/api/frequent-passengers")
public class FrequentPassengerController {

    @Autowired
    private FrequentPassengerService frequentPassengerService;

    /**
     * 添加常用乘客
     * POST /api/frequent-passengers?userId=1
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> addFrequentPassenger(
            @RequestParam Integer userId,
            @RequestBody FrequentPassengerRequest request) {
        try {
            FrequentPassengerResponse response = frequentPassengerService.addFrequentPassenger(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("添加常用乘客成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取用户的所有常用乘客
     * GET /api/frequent-passengers?userId=1
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getFrequentPassengers(@RequestParam Integer userId) {
        try {
            List<FrequentPassengerResponse> passengers = frequentPassengerService.getFrequentPassengers(userId);
            Map<String, Object> data = new HashMap<>();
            data.put("list", passengers);
            data.put("total", passengers.size());
            return ResponseEntity.ok(ApiResponse.success("查询成功", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据ID获取常用乘客详情
     * GET /api/frequent-passengers/{id}?userId=1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getFrequentPassengerById(
            @PathVariable Long id,
            @RequestParam Integer userId) {
        try {
            FrequentPassengerResponse response = frequentPassengerService.getFrequentPassengerById(userId, id);
            return ResponseEntity.ok(ApiResponse.success("查询成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新常用乘客信息
     * PUT /api/frequent-passengers/{id}?userId=1
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateFrequentPassenger(
            @PathVariable Long id,
            @RequestParam Integer userId,
            @RequestBody FrequentPassengerRequest request) {
        try {
            FrequentPassengerResponse response = frequentPassengerService.updateFrequentPassenger(userId, id, request);
            return ResponseEntity.ok(ApiResponse.success("更新常用乘客成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除常用乘客（软删除）
     * DELETE /api/frequent-passengers/{id}?userId=1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteFrequentPassenger(
            @PathVariable Long id,
            @RequestParam Integer userId) {
        try {
            frequentPassengerService.deleteFrequentPassenger(userId, id);
            return ResponseEntity.ok(ApiResponse.success("删除常用乘客成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 设置默认常用乘客
     * PUT /api/frequent-passengers/{id}/set-default?userId=1
     */
    @PutMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<?>> setDefaultFrequentPassenger(
            @PathVariable Long id,
            @RequestParam Integer userId) {
        try {
            frequentPassengerService.setDefaultFrequentPassenger(userId, id);
            return ResponseEntity.ok(ApiResponse.success("设置默认常用乘客成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取默认常用乘客
     * GET /api/frequent-passengers/default?userId=1
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<?>> getDefaultFrequentPassenger(@RequestParam Integer userId) {
        try {
            FrequentPassengerResponse response = frequentPassengerService.getDefaultFrequentPassenger(userId);
            if (response == null) {
                return ResponseEntity.ok(ApiResponse.success("暂无默认常用乘客", null));
            }
            return ResponseEntity.ok(ApiResponse.success("查询成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

