package com.example.airplanesalehouduan.passengers.service;

import com.example.airplanesalehouduan.passengers.entity.AircraftType;
import com.example.airplanesalehouduan.passengers.entity.Flight;
import com.example.airplanesalehouduan.passengers.entity.Seat;
import com.example.airplanesalehouduan.passengers.other.AircraftTypeRepository;
import com.example.airplanesalehouduan.passengers.other.SeatRepository;
import com.example.airplanesalehouduan.passengers.other.UserFlightRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 座位服务类
 * 负责处理座位相关的业务逻辑
 */
@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserFlightRepository flightRepository;

    @Autowired
    private AircraftTypeRepository aircraftTypeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据航班ID获取座位列表
     */
    public List<Map<String, Object>> getSeatsByFlightId(Integer flightId) {
        List<Seat> seats = seatRepository.findByFlightId(flightId);
        return convertSeatsToMap(seats);
    }

    /**
     * 批量更新座位状态（前端可以传英文或中文，这里统一转换为中文存库）
     */
    @Transactional
    public void updateSeatStatus(Integer flightId, List<Integer> seatIds, String status) {
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }

        // 英文/中文状态统一映射为中文“可用”/“已占用”
        String normalized;
        if (status == null || status.trim().isEmpty() ||
                "available".equalsIgnoreCase(status) || "可用".equals(status)) {
            normalized = "可用";
        } else if ("occupied".equalsIgnoreCase(status) ||
                "reserved".equalsIgnoreCase(status) ||
                "maintenance".equalsIgnoreCase(status) ||
                "已占用".equals(status)) {
            normalized = "已占用";
        } else {
            throw new IllegalArgumentException("非法的座位状态: " + status);
        }

        List<Seat> seats = seatRepository.findByFlightIdAndSeatIds(flightId, seatIds);
        if (seats.isEmpty()) {
            throw new IllegalArgumentException("未找到对应的座位记录");
        }
        for (Seat seat : seats) {
            seat.setStatus(normalized);
        }
        seatRepository.saveAll(seats);
    }

    /**
     * 根据航班ID和舱位等级获取可用座位数
     */
    public Long getAvailableSeatCountByCabinClass(Integer flightId, String cabinClass) {
        if (cabinClass == null || cabinClass.trim().isEmpty() || "全部舱位".equals(cabinClass)) {
            return seatRepository.countAvailableSeatsByFlightId(flightId);
        }
        // 转换前端传入的舱位等级到数据库存储的格式
        String dbCabinClass = convertCabinClassToDb(cabinClass);
        return seatRepository.countAvailableSeatsByFlightIdAndCabinClass(flightId, dbCabinClass);
    }

    /**
     * 根据航班ID获取座位布局（用于前端渲染）
     */
    public Map<String, Object> getSeatLayoutByFlightId(Integer flightId) {
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        if (flightOpt.isEmpty()) {
            throw new IllegalArgumentException("航班不存在");
        }

        Flight flight = flightOpt.get();
        AircraftType aircraftType = flight.getAircraftType();

        if (aircraftType == null || aircraftType.getSeatLayout() == null) {
            throw new IllegalArgumentException("机型座位布局信息不存在");
        }

        // 解析座位布局JSON
        Map<String, Object> seatLayout;
        try {
            seatLayout = objectMapper.readValue(aircraftType.getSeatLayout(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("座位布局JSON格式错误: " + e.getMessage());
        }

        // 获取该航班的所有座位
        List<Seat> seats = seatRepository.findByFlightId(flightId);
        Map<String, Seat> seatMap = new HashMap<>();
        for (Seat seat : seats) {
            seatMap.put(seat.getSeatNumber(), seat);
        }

        // 构建座位布局数据
        Map<String, Object> result = new HashMap<>();
        result.put("layout", seatLayout);
        result.put("seats", convertSeatsToMap(seats));

        return result;
    }

    /**
     * 批量创建座位（根据机型布局）
     */
    @Transactional
    public void createSeatsForFlight(Integer flightId) {
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        if (flightOpt.isEmpty()) {
            throw new IllegalArgumentException("航班不存在");
        }

        Flight flight = flightOpt.get();
        AircraftType aircraftType = flight.getAircraftType();

        if (aircraftType == null || aircraftType.getSeatLayout() == null) {
            throw new IllegalArgumentException("机型座位布局信息不存在");
        }

        // 检查是否已存在座位
        List<Seat> existingSeats = seatRepository.findByFlightId(flightId);
        if (!existingSeats.isEmpty()) {
            return; // 已存在座位，不重复创建
        }

        // 解析座位布局JSON
        Map<String, Object> seatLayout;
        try {
            seatLayout = objectMapper.readValue(aircraftType.getSeatLayout(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("座位布局JSON格式错误: " + e.getMessage());
        }

        // 根据布局创建座位
        List<Seat> seats = new ArrayList<>();

        // 假设seatLayout格式为：
        // {
        //   "business": { "rows": [1, 2], "positions": ["A", "B", "D", "E"], "price": 200 },
        //   "economy": { "rows": [10, 30], "positions": ["A", "B", "C", "D", "E", "F"], "price": 0 }
        // }

        @SuppressWarnings("unchecked")
        Map<String, Object> business = (Map<String, Object>) seatLayout.get("business");
        @SuppressWarnings("unchecked")
        Map<String, Object> economy = (Map<String, Object>) seatLayout.get("economy");

        // 创建商务舱座位
        if (business != null) {
            seats.addAll(createSeatsForCabin(flight, business, "商务舱", "business"));
        }

        // 创建经济舱座位
        if (economy != null) {
            seats.addAll(createSeatsForCabin(flight, economy, "经济舱", "economy"));
        }

        // 批量保存
        if (!seats.isEmpty()) {
            seatRepository.saveAll(seats);
        }
    }

    /**
     * 为某个舱位创建座位
     */
    private List<Seat> createSeatsForCabin(Flight flight, Map<String, Object> cabinConfig,
                                           String cabinClass, String cabinCode) {
        List<Seat> seats = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Integer> rows = (List<Integer>) cabinConfig.get("rows");
        @SuppressWarnings("unchecked")
        List<String> positions = (List<String>) cabinConfig.get("positions");
        Object priceObj = cabinConfig.get("price");
        BigDecimal price = priceObj != null ?
                new BigDecimal(priceObj.toString()) : BigDecimal.ZERO;

        if (rows == null || positions == null) {
            return seats;
        }

        // 处理行号范围（如 [10, 30] 表示10-30排）
        List<Integer> rowList = new ArrayList<>();
        if (rows.size() == 2) {
            int start = rows.get(0);
            int end = rows.get(1);
            for (int i = start; i <= end; i++) {
                rowList.add(i);
            }
        } else {
            rowList = rows;
        }

        // 为每排每个位置创建座位
        for (Integer row : rowList) {
            for (String position : positions) {
                Seat seat = new Seat();
                seat.setFlight(flight);
                seat.setSeatNumber(row + position);
                seat.setCabinClass(cabinClass);
                seat.setRownumber(row);
                seat.setSeatPosition(position);
                seat.setPrice(price);
                seat.setAvailableCount(1);
                // 默认初始状态为中文“可用”
                seat.setStatus("可用");
                seats.add(seat);
            }
        }

        return seats;
    }

    /**
     * 将Seat实体列表转换为Map列表
     */
    private List<Map<String, Object>> convertSeatsToMap(List<Seat> seats) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Seat seat : seats) {
            Map<String, Object> seatMap = new HashMap<>();
            seatMap.put("id", seat.getId());
            seatMap.put("seatNumber", seat.getSeatNumber());
            seatMap.put("cabinClass", seat.getCabinClass());
            seatMap.put("row", seat.getRownumber());
            seatMap.put("position", seat.getSeatPosition());
            seatMap.put("price", seat.getPrice() != null ? seat.getPrice().doubleValue() : 0.0);
            seatMap.put("status", seat.getStatus());
            seatMap.put("availableCount", seat.getAvailableCount());
            result.add(seatMap);
        }
        return result;
    }

    /**
     * 转换前端传入的舱位等级到数据库存储格式
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
}

