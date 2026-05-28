package com.example.airplanesalehouduan.admin.controller;


import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.admin.dto.AdminFlightResponseDTO;
import com.example.airplanesalehouduan.admin.entity.AdminFlight;
import com.example.airplanesalehouduan.admin.service.AdminFlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员航班管理控制器
 */
@RestController
@RequestMapping("/api/admin/flights")
public class AdminFlightController {

    @Autowired
    private AdminFlightService adminFlightService;

    /**
     * 获取航班列表（分页）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getFlightList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String departureDateStart,
            @RequestParam(required = false) String departureDateEnd,
            @RequestParam(required = false) String airline,
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination) {
        try {
            Page<AdminFlightResponseDTO> flightPage = adminFlightService.getFlightList(
                    page, size, departureDateStart, departureDateEnd, airline, departure, destination);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("flights", flightPage.getContent());
            responseData.put("data", flightPage.getContent()); // 兼容前端可能使用的data字段
            responseData.put("total", flightPage.getTotalElements());
            responseData.put("page", flightPage.getNumber());
            responseData.put("size", flightPage.getSize());
            responseData.put("totalPages", flightPage.getTotalPages());

            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据ID获取航班详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getFlightById(@PathVariable Integer id) {
        try {
            AdminFlightResponseDTO flightDTO = adminFlightService.getFlightDTOById(id)
                    .orElseThrow(() -> new RuntimeException("航班不存在"));
            return ResponseEntity.ok(ApiResponse.success("查询成功", flightDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建航班
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createFlight(@RequestBody com.example.airplanesalehouduan.admin.dto.AdminFlightCreateRequestDTO requestDTO) {
        try {
            AdminFlight createdAdminFlight = adminFlightService.createFlightFromDTO(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("创建航班成功", createdAdminFlight));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新航班
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateFlight(
            @PathVariable Integer id,
            @RequestBody com.example.airplanesalehouduan.admin.dto.AdminFlightCreateRequestDTO requestDTO) {
        try {
            AdminFlight updatedAdminFlight = adminFlightService.updateFlightFromDTO(id, requestDTO);
            return ResponseEntity.ok(ApiResponse.success("更新航班成功", updatedAdminFlight));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除航班
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteFlight(@PathVariable Integer id) {
        try {
            adminFlightService.deleteFlight(id);
            return ResponseEntity.ok(ApiResponse.success("删除航班成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 批量删除航班
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<?>> deleteFlights(@RequestBody Map<String, List<Object>> request) {
        try {
            List<Object> idObjects = request.get("ids");
            if (idObjects == null || idObjects.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请提供要删除的航班ID列表"));
            }

            // 转换ID列表（支持String和Integer类型）
            List<Integer> ids = idObjects.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return (Integer) id;
                        } else if (id instanceof String) {
                            return Integer.parseInt((String) id);
                        } else {
                            throw new RuntimeException("无效的ID类型: " + id.getClass());
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());

            adminFlightService.deleteFlights(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除航班成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

