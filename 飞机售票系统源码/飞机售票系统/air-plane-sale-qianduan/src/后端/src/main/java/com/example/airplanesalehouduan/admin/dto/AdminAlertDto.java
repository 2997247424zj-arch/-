package com.example.airplanesalehouduan.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAlertDto {
    private Long id;
    private String flightNo;
    private String level; // urgent | warning | info
    private String title;
    private String description;
    private LocalDateTime updatedAt;
    private String status; // e.g., delayed, cancelled, reported, processing, resolved
    private String reason;
    private String operatorNote;
}


