package com.example.airplanesalehouduan.admin.dto;

import com.example.airplanesalehouduan.admin.entity.AdminAircraftType;
import lombok.Data;

/**
 * 机型请求DTO
 */
@Data
public class AdminAircraftTypeRequestDTO {
    private String typeCode;
    private String manufacturer;
    private String model;
    private String seatLayout; // JSON格式的字符串
    private AdminAircraftType.Status status;

    /**
     * 转换为实体类
     */
    public AdminAircraftType toEntity() {
        AdminAircraftType entity = new AdminAircraftType();
        entity.setTypeCode(this.typeCode);
        entity.setManufacturer(this.manufacturer);
        entity.setModel(this.model);
        entity.setSeatLayout(this.seatLayout);
        entity.setStatus(this.status != null ? this.status : AdminAircraftType.Status.active);
        return entity;
    }
}

