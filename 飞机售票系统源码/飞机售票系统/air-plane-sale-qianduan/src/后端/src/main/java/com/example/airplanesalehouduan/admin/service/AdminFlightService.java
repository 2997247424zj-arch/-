package com.example.airplanesalehouduan.admin.service;

import com.example.airplanesalehouduan.admin.dto.AdminFlightResponseDTO;
import com.example.airplanesalehouduan.admin.entity.AdminFlight;
import com.example.airplanesalehouduan.admin.entity.AdminAircraftType;
import com.example.airplanesalehouduan.admin.repository.AdminFlightRepository;
import com.example.airplanesalehouduan.admin.repository.AdminAircraftTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.example.airplanesalehouduan.passengers.other.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 管理员航班管理服务
 */
@Service
public class AdminFlightService {

    @Autowired
    private AdminFlightRepository adminFlightRepository;

    @Autowired
    private AdminAircraftTypeRepository adminAircraftTypeRepository;
    
    @Autowired
    private TicketRepository ticketRepository;

    /**
     * 分页查询航班列表（返回DTO）
     */
    public Page<AdminFlightResponseDTO> getFlightList(Integer page, Integer size,
                                                      String departureDateStart, String departureDateEnd,
                                                      String airline, String departure, String destination) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "schedDepTime"));

        Specification<AdminFlight> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 出发日期范围筛选
            if (departureDateStart != null && !departureDateStart.trim().isEmpty()) {
                try {
                    LocalDateTime startDate = LocalDateTime.parse(departureDateStart + "T00:00:00");
                    predicates.add(cb.greaterThanOrEqualTo(root.get("schedDepTime"), startDate));
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }

            if (departureDateEnd != null && !departureDateEnd.trim().isEmpty()) {
                try {
                    LocalDateTime endDate = LocalDateTime.parse(departureDateEnd + "T23:59:59");
                    predicates.add(cb.lessThanOrEqualTo(root.get("schedDepTime"), endDate));
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }

            // 航空公司筛选（通过机型制造商筛选）
            if (airline != null && !airline.trim().isEmpty()) {
                predicates.add(cb.like(
                        root.join("adminAircraftType").get("manufacturer"),
                        "%" + airline + "%"
                ));
            }

            // 出发地筛选
            if (departure != null && !departure.trim().isEmpty()) {
                predicates.add(cb.like(root.get("originAirport"), "%" + departure + "%"));
            }

            // 目的地筛选
            if (destination != null && !destination.trim().isEmpty()) {
                predicates.add(cb.like(root.get("destAirport"), "%" + destination + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AdminFlight> flightPage = adminFlightRepository.findAll(spec, pageable);

        // 转换为DTO
        return flightPage.map(AdminFlightResponseDTO::fromEntity);
    }

    /**
     * 根据ID获取航班
     */
    public Optional<AdminFlight> getFlightById(Integer id) {
        return adminFlightRepository.findById(id);
    }

    /**
     * 创建航班
     */
    @Transactional
    public AdminFlight createFlight(AdminFlight adminFlight) {
        // 验证机型是否存在
        if (adminFlight.getAdminAircraftType() != null &&
                adminFlight.getAdminAircraftType().getId() != null) {
            Optional<AdminAircraftType> aircraftType = adminAircraftTypeRepository
                    .findById(adminFlight.getAdminAircraftType().getId());
            if (aircraftType.isEmpty()) {
                throw new RuntimeException("机型不存在");
            }
            adminFlight.setAdminAircraftType(aircraftType.get());
        }
        // 唯一性检查：flightNo + schedDepTime 不可重复
        if (adminFlight.getFlightNo() != null && adminFlight.getSchedDepTime() != null) {
            boolean exists = adminFlightRepository.existsByFlightNoAndSchedDepTime(adminFlight.getFlightNo(), adminFlight.getSchedDepTime());
            if (exists) {
                throw new RuntimeException("航班号与计划出发时间的组合已存在，无法创建重复航班");
            }
        }
        return adminFlightRepository.save(adminFlight);
    }

    /**
     * 根据创建请求DTO创建航班，兼容 aircraftTypeId 或 aircraftModel 字段
     */
    @Transactional
    public AdminFlight createFlightFromDTO(com.example.airplanesalehouduan.admin.dto.AdminFlightCreateRequestDTO dto) {
        AdminFlight adminFlight = new AdminFlight();
        adminFlight.setFlightNo(dto.getFlightNo());
        adminFlight.setSchedDepTime(dto.getSchedDepTime());
        adminFlight.setSchedArrTime(dto.getSchedArrTime());
        // 保持数据库长度限制兼容（截断到合理长度）
        if (dto.getOriginAirport() != null) {
            adminFlight.setOriginAirport(dto.getOriginAirport().length() > 64 ? dto.getOriginAirport().substring(0, 64) : dto.getOriginAirport());
        }
        if (dto.getDestAirport() != null) {
            adminFlight.setDestAirport(dto.getDestAirport().length() > 64 ? dto.getDestAirport().substring(0, 64) : dto.getDestAirport());
        }
        adminFlight.setRouteInfo(dto.getRouteInfo());
        // 解析并设置机型
        AdminAircraftType resolved = null;
        if (dto.getAircraftTypeId() != null) {
            resolved = adminAircraftTypeRepository.findById(dto.getAircraftTypeId())
                    .orElseThrow(() -> new RuntimeException("机型不存在"));
        } else if (dto.getAircraftModel() != null && !dto.getAircraftModel().trim().isEmpty()) {
            String m = dto.getAircraftModel().trim();
            // 先按 typeCode 查找，再按 model 查找
            Optional<AdminAircraftType> byCode = adminAircraftTypeRepository.findByTypeCode(m);
            if (byCode.isPresent()) resolved = byCode.get();
            else {
                Optional<AdminAircraftType> byModel = adminAircraftTypeRepository.findByModel(m);
                if (byModel.isPresent()) resolved = byModel.get();
            }
            // 若仍未找到，则创建一个新的机型记录（以便外键不为null）
            if (resolved == null) {
                AdminAircraftType newType = new AdminAircraftType();
                // 尝试使用 m 作为 typeCode（需保证唯一），若过长则截断
                newType.setTypeCode(m.length() > 20 ? m.substring(0, 20) : m);
                newType.setModel(m);
                newType.setManufacturer("");
                newType.setStatus(AdminAircraftType.Status.active);
                resolved = adminAircraftTypeRepository.save(newType);
            }
        } else {
            throw new RuntimeException("缺少机型信息（请提供 aircraftTypeId 或 aircraftModel）");
        }
        adminFlight.setAdminAircraftType(resolved);
        // Price
        if (dto.getPrice() != null) {
            adminFlight.setPrice(dto.getPrice());
        }
        // RouteInfo: 只保留允许的字段 stops, distance_km, meal_service
        adminFlight.setRouteInfo(sanitizeRouteInfo(dto.getRouteInfo()));
        // 默认状态
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                adminFlight.setStatus(AdminFlight.FlightStatus.valueOf(dto.getStatus()));
            } catch (Exception e) {
                adminFlight.setStatus(AdminFlight.FlightStatus.scheduled);
            }
        } else {
            adminFlight.setStatus(AdminFlight.FlightStatus.scheduled);
        }
        // 唯一性检查：避免违反数据库唯一约束
        if (adminFlight.getFlightNo() != null && adminFlight.getSchedDepTime() != null) {
            boolean exists = adminFlightRepository.existsByFlightNoAndSchedDepTime(adminFlight.getFlightNo(), adminFlight.getSchedDepTime());
            if (exists) {
                throw new RuntimeException("航班号与计划出发时间的组合已存在，无法创建重复航班");
            }
        }
        return adminFlightRepository.save(adminFlight);
    }

    /**
     * 更新航班
     */
    @Transactional
    public AdminFlight updateFlight(Integer id, AdminFlight adminFlight) {
        AdminFlight existingAdminFlight = adminFlightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("航班不存在"));

        existingAdminFlight.setFlightNo(adminFlight.getFlightNo());

        // 验证并设置机型
        if (adminFlight.getAdminAircraftType() != null &&
                adminFlight.getAdminAircraftType().getId() != null) {
            Optional<AdminAircraftType> aircraftType = adminAircraftTypeRepository
                    .findById(adminFlight.getAdminAircraftType().getId());
            if (aircraftType.isEmpty()) {
                throw new RuntimeException("机型不存在");
            }
            existingAdminFlight.setAdminAircraftType(aircraftType.get());
        }

        existingAdminFlight.setOriginAirport(adminFlight.getOriginAirport());
        existingAdminFlight.setDestAirport(adminFlight.getDestAirport());
        existingAdminFlight.setSchedDepTime(adminFlight.getSchedDepTime());
        existingAdminFlight.setSchedArrTime(adminFlight.getSchedArrTime());
        existingAdminFlight.setStatus(adminFlight.getStatus());
        existingAdminFlight.setRouteInfo(adminFlight.getRouteInfo());

        return adminFlightRepository.save(existingAdminFlight);
    }

    /**
     * 使用 DTO 更新航班，兼容 aircraftTypeId / aircraftModel
     */
    @Transactional
    public AdminFlight updateFlightFromDTO(Integer id, com.example.airplanesalehouduan.admin.dto.AdminFlightCreateRequestDTO dto) {
        AdminFlight existing = adminFlightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("航班不存在"));
        if (dto.getFlightNo() != null) existing.setFlightNo(dto.getFlightNo());
        if (dto.getSchedDepTime() != null) existing.setSchedDepTime(dto.getSchedDepTime());
        if (dto.getSchedArrTime() != null) existing.setSchedArrTime(dto.getSchedArrTime());
        if (dto.getOriginAirport() != null) existing.setOriginAirport(dto.getOriginAirport().length() > 64 ? dto.getOriginAirport().substring(0,64) : dto.getOriginAirport());
        if (dto.getDestAirport() != null) existing.setDestAirport(dto.getDestAirport().length() > 64 ? dto.getDestAirport().substring(0,64) : dto.getDestAirport());
        if (dto.getRouteInfo() != null) existing.setRouteInfo(dto.getRouteInfo());

        // 处理机型
        AdminAircraftType resolved = null;
        if (dto.getAircraftTypeId() != null) {
            resolved = adminAircraftTypeRepository.findById(dto.getAircraftTypeId())
                    .orElseThrow(() -> new RuntimeException("机型不存在"));
        } else if (dto.getAircraftModel() != null && !dto.getAircraftModel().trim().isEmpty()) {
            String m = dto.getAircraftModel().trim();
            Optional<AdminAircraftType> byCode = adminAircraftTypeRepository.findByTypeCode(m);
            if (byCode.isPresent()) resolved = byCode.get();
            else {
                Optional<AdminAircraftType> byModel = adminAircraftTypeRepository.findByModel(m);
                if (byModel.isPresent()) resolved = byModel.get();
            }
            if (resolved == null) {
                AdminAircraftType newType = new AdminAircraftType();
                newType.setTypeCode(m.length() > 20 ? m.substring(0,20) : m);
                newType.setModel(m);
                newType.setManufacturer("");
                newType.setStatus(AdminAircraftType.Status.active);
                resolved = adminAircraftTypeRepository.save(newType);
            }
        }
        if (resolved != null) existing.setAdminAircraftType(resolved);

        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                existing.setStatus(AdminFlight.FlightStatus.valueOf(dto.getStatus()));
            } catch (Exception e) {
                // ignore invalid status
            }
        }
        // price
        if (dto.getPrice() != null) existing.setPrice(dto.getPrice());
        // sanitize routeInfo
        if (dto.getRouteInfo() != null) existing.setRouteInfo(sanitizeRouteInfo(dto.getRouteInfo()));

        return adminFlightRepository.save(existing);
    }

    // 只保留 routeInfo 中允许的字段（stops:int, distance_km:int, meal_service:boolean）
    private String sanitizeRouteInfo(String rawJson) {
        if (rawJson == null || rawJson.trim().isEmpty()) return null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(rawJson);
            ObjectNode out = mapper.createObjectNode();
            if (node.has("stops") && node.get("stops").isNumber()) {
                out.put("stops", node.get("stops").intValue());
            }
            if (node.has("distance_km") && node.get("distance_km").isNumber()) {
                out.put("distance_km", node.get("distance_km").intValue());
            }
            if (node.has("meal_service")) {
                JsonNode ms = node.get("meal_service");
                if (ms.isBoolean()) {
                    out.put("meal_service", ms.booleanValue());
                } else if (ms.isTextual()) {
                    String tv = ms.asText().toLowerCase();
                    out.put("meal_service", tv.equals("true") || tv.equals("1") || tv.equals("yes"));
                } else if (ms.isNumber()) {
                    out.put("meal_service", ms.intValue() != 0);
                }
            }
            // 如果输出为空，返回 null
            if (out.size() == 0) return null;
            return mapper.writeValueAsString(out);
        } catch (JsonProcessingException e) {
            // 如果解析失败，尝试简单提取数字/boolean，但为了安全返回 null
            return null;
        }
    }

    /**
     * 根据ID获取航班（返回DTO）
     */
    public Optional<AdminFlightResponseDTO> getFlightDTOById(Integer id) {
        return adminFlightRepository.findById(id)
                .map(AdminFlightResponseDTO::fromEntity);
    }

    /**
     * 删除航班
     */
    @Transactional
    public void deleteFlight(Integer id) {
        if (!adminFlightRepository.existsById(id)) {
            throw new RuntimeException("航班不存在");
        }
        // 检查是否存在关联机票，若有则拒绝删除并给出友好提示
        try {
            long relatedTickets = ticketRepository.countByFlightId(id);
            if (relatedTickets > 0) {
                throw new RuntimeException("存在关联机票，无法删除航班，请先删除相关机票或取消关联");
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("检查关联机票失败，删除已中止");
        }

        adminFlightRepository.deleteById(id);
    }

    /**
     * 批量删除航班
     */
    @Transactional
    public void deleteFlights(List<Integer> ids) {
        // 在批量删除前检查每个航班是否存在关联机票，若有则拒绝并列出相关航班ID
        List<Integer> blocked = new ArrayList<>();
        for (Integer id : ids) {
            try {
                long cnt = ticketRepository.countByFlightId(id);
                if (cnt > 0) blocked.add(id);
            } catch (Exception e) {
                throw new RuntimeException("检查航班关联机票失败，删除已中止");
            }
        }
        if (!blocked.isEmpty()) {
            throw new RuntimeException("以下航班存在关联机票，无法删除: " + blocked.toString() + "，请先删除相关机票或取消关联");
        }

        adminFlightRepository.deleteAllById(ids);
    }
}

