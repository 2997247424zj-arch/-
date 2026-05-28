package com.example.airplanesalehouduan.passengers.service;




import com.example.airplanesalehouduan.passengers.entity.Flight;
import com.example.airplanesalehouduan.passengers.other.UserFlightRepository;
import com.example.airplanesalehouduan.passengers.other.FlightSearchRequest;
import com.example.airplanesalehouduan.passengers.other.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 航班服务类
 * 负责处理航班搜索等业务逻辑
 */
@Service
public class FlightService {

    @Autowired
    private UserFlightRepository userFlightRepository;

    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private com.example.airplanesalehouduan.passengers.service.JuheImportService juheImportService;

    /**
     * 搜索航班
     * @param request 搜索请求参数
     * @return 航班列表
     */
    public List<Map<String, Object>> searchFlights(FlightSearchRequest request) {
        // 参数验证
        if (request.getDeparture() == null || request.getDeparture().trim().isEmpty()) {
            throw new IllegalArgumentException("出发城市不能为空");
        }
        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            throw new IllegalArgumentException("到达城市不能为空");
        }
        if (request.getDate() == null || request.getDate().trim().isEmpty()) {
            throw new IllegalArgumentException("出发日期不能为空");
        }

        // 解析日期
        LocalDate departureDate = parseDate(request.getDate());
        LocalDateTime startDateTime = departureDate.atStartOfDay();
        LocalDateTime endDateTime = departureDate.plusDays(1).atStartOfDay();

        // 定义允许的状态列表
        List<Flight.FlightStatus> allowedStatuses = Arrays.asList(
                Flight.FlightStatus.scheduled,
                Flight.FlightStatus.delayed,
                Flight.FlightStatus.boarding
        );

        // 查询航班（先使用本地数据库）
        List<Flight> flights = userFlightRepository.searchFlightsByDateRange(
                request.getDeparture().trim(),
                request.getDestination().trim(),
                startDateTime,
                endDateTime,
                allowedStatuses
        );

        // 如果本地没有命中结果，则调用聚合API插入数据，再次查询并返回（保持原有逻辑不变）
        if (flights == null || flights.isEmpty()) {
            try {
                juheImportService.importAndSave(request.getDeparture().trim(), request.getDestination().trim(), departureDate.toString());
                // 重新查询以返回新插入的数据
                flights = userFlightRepository.searchFlightsByDateRange(
                        request.getDeparture().trim(),
                        request.getDestination().trim(),
                        startDateTime,
                        endDateTime,
                        allowedStatuses
                );
            } catch (Exception e) {
                // 记录错误但不改变原有返回行为（仍返回空列表或原始查询结果）
                System.err.println("聚合数据导入失败: " + e.getMessage());
            }
        }

        // 转换为前端需要的格式（带上舱位等级，用于计算对应舱位余票）
        return convertToFlightResponse(flights, request.getCabinClass());
    }

    /**
     * 解析日期字符串
     * 支持多种格式：yyyy-MM-dd, yyyy/MM/dd, yyyy-MM-dd HH:mm:ss 等
     */
    private LocalDate parseDate(String dateStr) {
        String[] patterns = {
                "yyyy-MM-dd",
                "yyyy/MM/dd",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss"
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (pattern.contains("HH:mm:ss")) {
                    LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
                    return dateTime.toLocalDate();
                } else {
                    return LocalDate.parse(dateStr, formatter);
                }
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }

        throw new IllegalArgumentException("日期格式不正确，支持的格式：yyyy-MM-dd 或 yyyy/MM/dd");
    }

    /**
     * 将Flight实体转换为前端需要的响应格式
     * @param flights 航班列表
     * @param cabinClass 前端传入的舱位等级（economy/business/first 或中文）
     */
    private List<Map<String, Object>> convertToFlightResponse(List<Flight> flights, String cabinClass) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Flight flight : flights) {
            Map<String, Object> flightMap = new HashMap<>();
            flightMap.put("id", flight.getId());
            flightMap.put("flightNumber", flight.getFlightNo());

            // 机型信息
            String airline = null;
            if (flight.getAircraftType() != null) {
                String manufacturer = flight.getAircraftType().getManufacturer();
                String model = flight.getAircraftType().getModel();

                // 从机型制造商中提取航空公司名称
                if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                    airline = manufacturer.trim();
                }

                // 构建 aircraft 字段：制造商 + 型号
                String aircraft = manufacturer != null ? manufacturer : "";
                if (model != null && !model.trim().isEmpty()) {
                    aircraft += (aircraft.isEmpty() ? "" : " ") + model.trim();
                }
                flightMap.put("aircraft", aircraft);
                flightMap.put("aircraftType", flight.getAircraftType().getTypeCode());
            }

            // 如果从机型信息中没有获取到航空公司，则根据航班号前缀识别
            if (airline == null || airline.isEmpty()) {
                airline = getAirlineByFlightNo(flight.getFlightNo());
            }
            flightMap.put("airline", airline);

            flightMap.put("departure", flight.getOriginAirport());
            flightMap.put("destination", flight.getDestAirport());

            // 格式化时间
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            flightMap.put("departureTime", flight.getSchedDepTime().format(timeFormatter));
            flightMap.put("arrivalTime", flight.getSchedArrTime().format(timeFormatter));

            // 计算飞行时长
            long minutes = java.time.Duration.between(flight.getSchedDepTime(), flight.getSchedArrTime()).toMinutes();
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            String duration = hours + "小时" + (remainingMinutes > 0 ? remainingMinutes + "分钟" : "");
            flightMap.put("duration", duration);

            // 状态
            flightMap.put("status", flight.getStatus().name());

            // 价格：优先使用数据库中的价格字段，如果为 null 则沿用原来的默认价格逻辑
            flightMap.put("price", calculatePrice(flight));
            // 余票数量：根据舱位等级查询对应舱位的可用座位数
            flightMap.put("seats", calculateAvailableSeats(flight, cabinClass));

            // 日期信息
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            flightMap.put("date", flight.getSchedDepTime().format(dateFormatter));

            result.add(flightMap);
        }

        return result;
    }

    /**
     * 计算航班价格
     * 优先从 flights.price 字段获取，如果为空则回退到原有的默认价格逻辑
     */
    private Double calculatePrice(Flight flight) {
        if (flight.getPrice() != null) {
            return flight.getPrice().doubleValue();
        }
        // 保持与之前相同的默认值，避免改变原有业务逻辑
        return 680.0;
    }

    /**
     * 计算可用座位数（从座位表中查询）
     * 如果传入了舱位等级，则按舱位等级统计；否则统计整架飞机的可用座位数
     */
    private Integer calculateAvailableSeats(Flight flight, String cabinClass) {
        try {
            Long count;
            if (cabinClass == null || cabinClass.trim().isEmpty()) {
                // 所有舱位的总余票
                count = seatRepository.countAvailableSeatsByFlightId(flight.getId());
            } else {
                // 将前端传入的舱位等级转换为 seats 表中的中文舱位
                String dbCabinClass = convertCabinClassToDb(cabinClass);
                count = seatRepository.countAvailableSeatsByFlightIdAndCabinClass(flight.getId(), dbCabinClass);
            }
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            // 查询异常时，返回 0，避免误导
            return 0;
        }
    }

    /**
     * 将前端传入的舱位等级（英文/中文）转换为 seats 表中的中文舱位
     */
    private String convertCabinClassToDb(String cabinClass) {
        if (cabinClass == null) {
            return null;
        }
        switch (cabinClass.toLowerCase()) {
            case "economy":
            case "经济舱":
                return "经济舱";
            case "business":
            case "商务舱":
                return "商务舱";
            case "first":
            case "头等舱":
                return "头等舱";
            default:
                return cabinClass;
        }
    }

    /**
     * 根据航班号前缀识别航空公司
     * @param flightNo 航班号，如 MU5432, CA1234 等
     * @return 航空公司名称
     */
    private String getAirlineByFlightNo(String flightNo) {
        if (flightNo == null || flightNo.length() < 2) {
            return "未知航空公司";
        }

        // 提取航班号前缀（前2位字母或数字+字母）
        String prefix = flightNo.substring(0, 2).toUpperCase();

        // 中国主要航空公司代码映射
        switch (prefix) {
            case "CA": return "中国国际航空";
            case "MU": return "中国东方航空";
            case "CZ": return "中国南方航空";
            case "HU": return "海南航空";
            case "ZH": return "深圳航空";
            case "MF": return "厦门航空";
            case "3U": return "四川航空";
            case "9C": return "春秋航空";
            case "JD": return "首都航空";
            case "HO": return "吉祥航空";
            case "FM": return "上海航空";
            case "KN": return "中国联合航空";
            case "PN": return "西部航空";
            case "G5": return "华夏航空";
            case "KY": return "昆明航空";
            case "8L": return "祥鹏航空";
            case "GS": return "天津航空";
            case "EU": return "成都航空";
            case "NS": return "河北航空";
            case "GJ": return "长龙航空";
            case "Y8": return "扬子江快运";
            default: return "未知航空公司";
        }
    }
}



