package com.example.airplanesalehouduan.admin.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.admin.dto.AdminAircraftTypeRequestDTO;
import com.example.airplanesalehouduan.admin.entity.AdminAircraftType;
import com.example.airplanesalehouduan.admin.service.AdminAircraftTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员机型管理控制器
 */
@RestController
@RequestMapping("/api/admin/aircraft-types")
public class AdminAircraftTypeController {

    @Autowired
    private AdminAircraftTypeService adminAircraftTypeService;

    /**
     * 获取所有可用机型列表（支持分页）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAircraftTypes(
            @RequestParam(required = false, defaultValue = "active") String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            AdminAircraftType.Status statusEnum = null;
            if (!"all".equals(status)) {
                statusEnum = "active".equals(status)
                        ? AdminAircraftType.Status.active
                        : AdminAircraftType.Status.retired;
            }

            Page<AdminAircraftType> aircraftTypesPage = adminAircraftTypeService.getAircraftTypesPage(page, size, statusEnum);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("data", aircraftTypesPage.getContent());
            responseData.put("total", aircraftTypesPage.getTotalElements());
            responseData.put("page", aircraftTypesPage.getNumber());
            responseData.put("size", aircraftTypesPage.getSize());
            responseData.put("totalPages", aircraftTypesPage.getTotalPages());

            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据ID获取机型详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getAircraftTypeById(@PathVariable Integer id) {
        try {
            AdminAircraftType aircraftType = adminAircraftTypeService.getAircraftTypeById(id)
                    .orElseThrow(() -> new RuntimeException("机型不存在"));
            return ResponseEntity.ok(ApiResponse.success("查询成功", aircraftType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建机型
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAircraftType(@RequestBody AdminAircraftTypeRequestDTO requestDTO) {
        try {
            AdminAircraftType aircraftType = requestDTO.toEntity();
            AdminAircraftType created = adminAircraftTypeService.createAircraftType(aircraftType);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("创建机型成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新机型
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateAircraftType(
            @PathVariable Integer id,
            @RequestBody AdminAircraftTypeRequestDTO requestDTO) {
        try {
            AdminAircraftType aircraftType = requestDTO.toEntity();
            AdminAircraftType updated = adminAircraftTypeService.updateAircraftType(id, aircraftType);
            return ResponseEntity.ok(ApiResponse.success("更新机型成功", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除机型
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteAircraftType(@PathVariable Integer id) {
        try {
            adminAircraftTypeService.deleteAircraftType(id);
            return ResponseEntity.ok(ApiResponse.success("删除机型成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 批量删除机型
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<?>> deleteAircraftTypes(@RequestBody Map<String, List<Integer>> request) {
        try {
            List<Integer> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("请提供要删除的机型ID列表"));
            }
            adminAircraftTypeService.deleteAircraftTypes(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除机型成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 批量创建机型
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<?>> createAircraftTypes(@RequestBody List<AdminAircraftTypeRequestDTO> requestDTOs) {
        try {
            List<AdminAircraftType> aircraftTypes = requestDTOs.stream()
                    .map(AdminAircraftTypeRequestDTO::toEntity)
                    .toList();
            List<AdminAircraftType> created = adminAircraftTypeService.createAircraftTypes(aircraftTypes);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("批量创建机型成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 多字段查询机型（支持分页）
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchAircraftTypes(
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            AdminAircraftType.Status statusEnum = null;
            if (status != null && !status.trim().isEmpty()) {
                if ("active".equals(status)) {
                    statusEnum = AdminAircraftType.Status.active;
                } else if ("retired".equals(status)) {
                    statusEnum = AdminAircraftType.Status.retired;
                }
            }
            Page<AdminAircraftType> aircraftTypesPage = adminAircraftTypeService.searchAircraftTypes(
                    typeCode, manufacturer, model, statusEnum, page, size);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("data", aircraftTypesPage.getContent());
            responseData.put("total", aircraftTypesPage.getTotalElements());
            responseData.put("page", aircraftTypesPage.getNumber());
            responseData.put("size", aircraftTypesPage.getSize());
            responseData.put("totalPages", aircraftTypesPage.getTotalPages());

            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取所有制造商列表（用于下拉选择）
     */
    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<?>> getAllManufacturers() {
        try {
            List<String> manufacturers = adminAircraftTypeService.getAllManufacturers();
            return ResponseEntity.ok(ApiResponse.success("查询成功", manufacturers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取所有型号列表（用于下拉选择）
     */
    @GetMapping("/models")
    public ResponseEntity<ApiResponse<?>> getAllModels() {
        try {
            List<String> models = adminAircraftTypeService.getAllModels();
            return ResponseEntity.ok(ApiResponse.success("查询成功", models));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

