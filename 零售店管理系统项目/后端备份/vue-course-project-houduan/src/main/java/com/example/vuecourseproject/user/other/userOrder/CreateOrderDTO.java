package com.example.vuecourseproject.user.other.userOrder;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderDTO {
    private Long addressId;
    private String paymentMethod;
    private String remark;
    private List<OrderItemDTO> items;
    private String username; // 新增测试用字段
}