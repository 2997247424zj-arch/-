package com.example.airplanesalehouduan.passengers.service;

import com.example.airplanesalehouduan.passengers.entity.AircraftType;
import com.example.airplanesalehouduan.passengers.entity.Flight;
import com.example.airplanesalehouduan.passengers.other.AircraftTypeRepository;
import com.example.airplanesalehouduan.passengers.other.UserFlightRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聚合数据导入服务
 * 调用聚合数据API，将结果解析并写入 flights 表，插入或更新时统一设置 aircraft_type_id = 6
 */
@Service
public class JuheImportService {

    // NOTE: 如需替换请改为配置注入
    private static final String API_KEY = "f073c66b268e520c56fcdf00ff99221a";
    private static final String API_URL = "https://apis.juhe.cn/flight/query";

    @Autowired
    private UserFlightRepository userFlightRepository;

    @Autowired
    private AircraftTypeRepository aircraftTypeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 调用聚合API并将结果写入 flights 表，插入或更新时 aircraft_type_id 固定为 6
     * @param departure 三字码或城市名（前端输入）
     * @param arrival 三字码或城市名（前端输入）
     * @param departureDate 出发日期，格式 yyyy-MM-dd
     * @return Map 包含 inserted 和 updated 计数
     * @throws Exception
     */
    @Transactional
    public Map<String, Integer> importAndSave(String departure, String arrival, String departureDate) throws Exception {
        Map<String, Integer> result = new HashMap<>();
        int inserted = 0;
        int updated = 0;

        Map<String, String> params = new HashMap<>();
        params.put("key", API_KEY);
        params.put("departure", departure);
        params.put("arrival", arrival);
        params.put("departureDate", departureDate);
        params.put("flightNo", "");
        params.put("maxSegments", "");

        String query = params.entrySet().stream()
                .map(entry -> {
                    try {
                        return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
                    } catch (Exception e) {
                        return entry.getKey() + "=" + entry.getValue();
                    }
                })
                .collect(Collectors.joining("&"));

        URL url = new URL(API_URL + "?" + query);
        StringBuilder resp = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) resp.append(line);
        }

        JsonNode root = objectMapper.readTree(resp.toString());
        JsonNode resultNode = root.has("result") ? root.get("result") : null;
        if (resultNode == null) {
            result.put("inserted", inserted);
            result.put("updated", updated);
            return result;
        }

        JsonNode flightArray = null;
        if (resultNode.has("flightInfo") && resultNode.get("flightInfo").isArray()) {
            flightArray = resultNode.get("flightInfo");
        } else if (resultNode.has("list") && resultNode.get("list").isArray()) {
            flightArray = resultNode.get("list");
        }

        if (flightArray == null || !flightArray.isArray()) {
            result.put("inserted", inserted);
            result.put("updated", updated);
            return result;
        }

        // 获取 aircraft_type id = 6 的机型实体（若不存在，用 null，会因非空外键导致保存失败，请确保 id=6 存在）
        AircraftType fixedType = aircraftTypeRepository.findById(6).orElse(null);

        for (JsonNode fnode : flightArray) {
            try {
                String flightNoRaw = fnode.has("flightNo") ? fnode.get("flightNo").asText() : null;
                if (flightNoRaw == null || flightNoRaw.trim().isEmpty()) continue;
                String flightNo = flightNoRaw.split("\\|")[0].trim();

                String depCode = fnode.has("departure") ? fnode.get("departure").asText() : departure;
                String arrCode = fnode.has("arrival") ? fnode.get("arrival").asText() : arrival;
                String depDate = fnode.has("departureDate") ? fnode.get("departureDate").asText() : departureDate;
                String depTime = fnode.has("departureTime") ? fnode.get("departureTime").asText() : "00:00";
                String arrDate = fnode.has("arrivalDate") ? fnode.get("arrivalDate").asText() : depDate;
                String arrTime = fnode.has("arrivalTime") ? fnode.get("arrivalTime").asText() : "00:00";

                LocalDateTime schedDep = parseDateTime(depDate, depTime);
                LocalDateTime schedArr = parseDateTime(arrDate, arrTime);

                java.math.BigDecimal price = null;
                if (fnode.has("ticketPrice") && !fnode.get("ticketPrice").isNull()) {
                    try { price = java.math.BigDecimal.valueOf(fnode.get("ticketPrice").asDouble()); } catch (Exception ignored) {}
                }

                String routeInfo = null;
                if (fnode.has("segments") && fnode.get("segments").isArray()) {
                    routeInfo = objectMapper.writeValueAsString(fnode.get("segments"));
                }

                // 查重：flight_no + sched_dep_time
                List<Flight> existed = userFlightRepository.findByFlightNo(flightNo);
                Flight toSave = null;
                for (Flight ex : existed) {
                    if (ex.getSchedDepTime() != null && ex.getSchedDepTime().equals(schedDep)) {
                        toSave = ex;
                        break;
                    }
                }

                if (toSave == null) {
                    Flight f = new Flight();
                    f.setFlightNo(flightNo);
                    f.setAircraftType(fixedType);
                    f.setOriginAirport(depCode);
                    f.setDestAirport(arrCode);
                    f.setSchedDepTime(schedDep);
                    f.setSchedArrTime(schedArr);
                    f.setPrice(price);
                    f.setRouteInfo(routeInfo);
                    userFlightRepository.save(f);
                    inserted++;
                } else {
                    // 更新部分字段并强制设置 aircraft type id = 6
                    toSave.setAircraftType(fixedType);
                    toSave.setSchedArrTime(schedArr);
                    if (price != null) toSave.setPrice(price);
                    if (routeInfo != null) toSave.setRouteInfo(routeInfo);
                    userFlightRepository.save(toSave);
                    updated++;
                }

            } catch (Exception e) {
                // 单条记录处理失败，继续
                System.err.println("解析或保存航班失败: " + e.getMessage());
            }
        }

        result.put("inserted", inserted);
        result.put("updated", updated);
        return result;
    }

    private LocalDateTime parseDateTime(String dateStr, String timeStr) {
        try {
            String t = timeStr.trim();
            if (t.length() == 5) t += ":00";
            return LocalDateTime.parse(dateStr + "T" + t);
        } catch (Exception e) {
            throw new RuntimeException("时间解析失败: " + dateStr + " " + timeStr);
        }
    }
}















