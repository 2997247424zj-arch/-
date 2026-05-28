package com.example.airplanesalehouduan.admin.dto;

import com.example.airplanesalehouduan.admin.entity.AdminFlight;
import com.example.airplanesalehouduan.admin.entity.AdminAircraftType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 航班响应DTO - 用于前端展示
 * 将后端实体转换为前端需要的格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFlightResponseDTO {
    private String id;
    private String departureDate; // 出发日期 (yyyy-MM-dd)
    private String flightNumber; // 航班号
    private String airline; // 航空公司 (从机型制造商或默认值获取)
    private String aircraftModel; // 飞机型号
    private String departure; // 出发地
    private String destination; // 目的地
    private String departureTime; // 出发时间 (yyyy-MM-dd HH:mm:ss)
    private String duration; // 飞行时间 (如: "3小时10分")
    private Integer quantity; // 数量 (从座位布局计算或默认值)
    private Double price; // 票价 (默认值或从routeInfo获取)

    /**
     * 从实体对象转换为DTO
     */
    public static AdminFlightResponseDTO fromEntity(com.example.airplanesalehouduan.admin.entity.AdminFlight flight) {
        AdminFlightResponseDTO dto = new AdminFlightResponseDTO();

        // 基本信息
        dto.setId(String.valueOf(flight.getId()));
        dto.setFlightNumber(flight.getFlightNo());
        dto.setDeparture(flight.getOriginAirport());
        dto.setDestination(flight.getDestAirport());

        // 日期和时间
        LocalDateTime depTime = flight.getSchedDepTime();
        LocalDateTime arrTime = flight.getSchedArrTime();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        dto.setDepartureDate(depTime.format(dateFormatter));
        dto.setDepartureTime(depTime.format(timeFormatter));

        // 计算飞行时间
        Duration flightDuration = Duration.between(depTime, arrTime);
        long hours = flightDuration.toHours();
        long minutes = flightDuration.toMinutes() % 60;
        dto.setDuration(String.format("%d小时%d分", hours, minutes));

        // 机型信息
        if (flight.getAdminAircraftType() != null) {
            com.example.airplanesalehouduan.admin.entity.AdminAircraftType aircraftType = flight.getAdminAircraftType();
            dto.setAircraftModel(aircraftType.getModel() != null ? aircraftType.getModel() : aircraftType.getTypeCode());

            // 航空公司：从制造商获取，如果没有则使用默认值
            String manufacturer = aircraftType.getManufacturer();
            if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                dto.setAirline(manufacturer);
            } else {
                // 默认航空公司（可以根据航班号前缀判断，这里简化处理）
                dto.setAirline("默认航空公司");
            }

            // 数量：从座位布局计算，如果没有则使用默认值
            dto.setQuantity(calculateSeatCount(aircraftType.getSeatLayout()));
        } else {
            dto.setAircraftModel("未知机型");
            dto.setAirline("未知航空公司");
            dto.setQuantity(200); // 默认座位数
        }

        // 票价：优先使用实体的 price 字段，其次从 routeInfo 中提取
        if (flight.getPrice() != null) {
            dto.setPrice(flight.getPrice());
        } else {
            dto.setPrice(extractPriceFromRouteInfo(flight.getRouteInfo()));
        }

        return dto;
    }

    /**
     * 从座位布局JSON中计算座位总数
     */
    private static Integer calculateSeatCount(String seatLayoutJson) {
        if (seatLayoutJson == null || seatLayoutJson.trim().isEmpty()) {
            return 200; // 默认座位数
        }

        try {
            // 简单的JSON解析（实际可以使用Jackson等库）
            // 这里简化处理，假设格式为 {"business": 40, "economy": 300}
            if (seatLayoutJson.contains("business") || seatLayoutJson.contains("economy")) {
                // 尝试提取数字
                int total = 0;
                String[] parts = seatLayoutJson.split("[^0-9]+");
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        total += Integer.parseInt(part);
                    }
                }
                return total > 0 ? total : 200;
            }
        } catch (Exception e) {
            // 解析失败，返回默认值
        }

        return 200; // 默认座位数
    }

    /**
     * 从路线信息JSON中提取票价
     */
    private static Double extractPriceFromRouteInfo(String routeInfoJson) {
        if (routeInfoJson == null || routeInfoJson.trim().isEmpty()) {
            return 500.0; // 默认票价
        }

        try {
            // 简单的JSON解析（实际可以使用Jackson等库）
            // 这里简化处理，假设格式为 {"price": 780.0}
            if (routeInfoJson.contains("price")) {
                // 尝试提取价格数字
                String[] parts = routeInfoJson.split("[^0-9.]+");
                for (String part : parts) {
                    if (!part.isEmpty() && part.contains(".")) {
                        return Double.parseDouble(part);
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败，返回默认值
        }

        return 500.0; // 默认票价
    }
}

