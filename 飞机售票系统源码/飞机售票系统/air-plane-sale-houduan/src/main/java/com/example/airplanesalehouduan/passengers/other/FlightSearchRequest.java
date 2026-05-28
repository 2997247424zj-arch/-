package com.example.airplanesalehouduan.passengers.other;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 航班搜索请求DTO
 */
@Data
public class FlightSearchRequest {

    /**
     * 出发城市/机场代码
     */
    private String departure;

    /**
     * 到达城市/机场代码
     */
    private String destination;

    /**
     * 出发日期，格式：yyyy-MM-dd 或 yyyy/MM/dd
     */
    private String date;

    /**
     * 乘客人数（可选）
     */
    private Integer passengers;

    /**
     * 舱位等级（可选），如：economy, business, first
     * 使用 @JsonProperty 注解映射前端传递的 "class" 参数
     */
    @JsonProperty("class")
    private String cabinClass;
}


