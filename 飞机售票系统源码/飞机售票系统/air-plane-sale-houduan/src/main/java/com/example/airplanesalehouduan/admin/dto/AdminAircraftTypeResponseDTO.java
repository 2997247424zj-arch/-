package com.example.airplanesalehouduan.admin.dto;

import com.example.airplanesalehouduan.admin.entity.AdminAircraftType;
import lombok.Data;

/**
 * 机型响应DTO
 */
@Data
public class AdminAircraftTypeResponseDTO {
    private Integer id;
    private String typeCode;
    private String manufacturer;
    private String model;
    private String seatLayout; // JSON格式的字符串
    private AdminAircraftType.Status status;

    /**
     * 从实体类转换
     */
    public static AdminAircraftTypeResponseDTO fromEntity(AdminAircraftType entity) {
        AdminAircraftTypeResponseDTO dto = new AdminAircraftTypeResponseDTO();
        dto.setId(entity.getId());
        dto.setTypeCode(entity.getTypeCode());
        dto.setManufacturer(entity.getManufacturer());
        dto.setModel(entity.getModel());
        dto.setSeatLayout(entity.getSeatLayout());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}

