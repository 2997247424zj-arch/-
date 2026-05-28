package com.example.airplanesalehouduan.passengers.other;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 常用乘客响应DTO
 * 用于返回给前端的常用乘客信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrequentPassengerResponse {
    private Long id;                    // 主键ID
    private String name;                // 乘客姓名
    private String idCard;              // 身份证号（前端可能需要掩码显示）
    private String relationship;        // 与用户的关系
    private String phone;               // 联系电话
    private String remarks;             // 备注信息
    private Boolean isDefault;          // 是否设为默认
    private String status;              // 状态
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;     // 更新时间
}

