package com.example.airplanesalehouduan.passengers.other;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 常用乘客请求DTO
 * 用于接收前端传来的常用乘客信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FrequentPassengerRequest {
    private String name;           // 乘客姓名（必填）
    private String idCard;         // 身份证号（必填，18位）
    private String relationship;   // 与用户的关系（可选）
    private String phone;          // 联系电话（可选）
    private String remarks;        // 备注信息（可选）
    private Boolean isDefault;     // 是否设为默认（可选）
}
