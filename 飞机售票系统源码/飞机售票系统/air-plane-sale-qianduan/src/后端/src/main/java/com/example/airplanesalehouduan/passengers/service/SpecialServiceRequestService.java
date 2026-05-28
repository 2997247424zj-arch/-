package com.example.airplanesalehouduan.passengers.service;


import com.example.airplanesalehouduan.passengers.entity.SpecialServiceRequest;
import com.example.airplanesalehouduan.passengers.other.SpecialServiceRequestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 重点旅客预约服务类
 */
@Service
public class SpecialServiceRequestService {

    @Autowired
    private SpecialServiceRequestRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建重点旅客预约
     */
    @Transactional
    public SpecialServiceRequest createRequest(SpecialServiceRequest request) {
        // 设置默认状态
        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            request.setStatus("pending");
        }

        // 设置创建时间
        if (request.getCreatedAt() == null) {
            request.setCreatedAt(LocalDateTime.now());
        }
        request.setUpdatedAt(LocalDateTime.now());

        return repository.save(request);
    }

    /**
     * 根据ID获取预约
     */
    public Optional<SpecialServiceRequest> getById(Long id) {
        return repository.findById(id);
    }

    /**
     * 根据乘客ID获取预约列表（分页）
     */
    public Page<SpecialServiceRequest> getByPassengerId(Integer passengerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByPassengerId(passengerId, pageable);
    }

    /**
     * 根据乘客ID和状态获取预约列表（分页）
     */
    public Page<SpecialServiceRequest> getByPassengerIdAndStatus(Integer passengerId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByPassengerIdAndStatus(passengerId, status, pageable);
    }

    /**
     * 根据乘客ID获取所有预约（不分页）
     */
    public List<SpecialServiceRequest> getAllByPassengerId(Integer passengerId) {
        return repository.findByPassengerId(passengerId);
    }

    /**
     * 根据订单号获取预约
     */
    public List<SpecialServiceRequest> getByOrderNo(String orderNo) {
        return repository.findByOrderNo(orderNo);
    }

    /**
     * 根据乘客ID和订单号获取预约
     */
    public Optional<SpecialServiceRequest> getByPassengerIdAndOrderNo(Integer passengerId, String orderNo) {
        return repository.findByPassengerIdAndOrderNo(passengerId, orderNo);
    }

    /**
     * 更新预约状态
     */
    @Transactional
    public SpecialServiceRequest updateStatus(Long id, String status) {
        Optional<SpecialServiceRequest> optional = repository.findById(id);
        if (optional.isPresent()) {
            SpecialServiceRequest request = optional.get();
            request.setStatus(status);
            request.setUpdatedAt(LocalDateTime.now());
            return repository.save(request);
        }
        throw new RuntimeException("预约不存在");
    }

    /**
     * 更新预约信息
     */
    @Transactional
    public SpecialServiceRequest updateRequest(Long id, SpecialServiceRequest updatedRequest) {
        Optional<SpecialServiceRequest> optional = repository.findById(id);
        if (optional.isPresent()) {
            SpecialServiceRequest existing = optional.get();

            // 更新字段
            if (updatedRequest.getPhone() != null) {
                existing.setPhone(updatedRequest.getPhone());
            }
            if (updatedRequest.getPassengerType() != null) {
                existing.setPassengerType(updatedRequest.getPassengerType());
            }
            if (updatedRequest.getDepartureAirport() != null) {
                existing.setDepartureAirport(updatedRequest.getDepartureAirport());
            }
            if (updatedRequest.getArrivalAirport() != null) {
                existing.setArrivalAirport(updatedRequest.getArrivalAirport());
            }
            if (updatedRequest.getEntryServices() != null) {
                existing.setEntryServices(updatedRequest.getEntryServices());
            }
            if (updatedRequest.getExitServices() != null) {
                existing.setExitServices(updatedRequest.getExitServices());
            }
            if (updatedRequest.getDescription() != null) {
                existing.setDescription(updatedRequest.getDescription());
            }
            if (updatedRequest.getStatus() != null) {
                existing.setStatus(updatedRequest.getStatus());
            }

            existing.setUpdatedAt(LocalDateTime.now());
            return repository.save(existing);
        }
        throw new RuntimeException("预约不存在");
    }

    /**
     * 删除预约
     */
    @Transactional
    public void deleteRequest(Long id) {
        repository.deleteById(id);
    }

    /**
     * 统计乘客的预约数量
     */
    public long countByPassengerId(Integer passengerId) {
        return repository.countByPassengerId(passengerId);
    }

    /**
     * 统计乘客指定状态的预约数量
     */
    public long countByPassengerIdAndStatus(Integer passengerId, String status) {
        return repository.countByPassengerIdAndStatus(passengerId, status);
    }

    /**
     * 根据状态获取预约列表（分页）- 管理员使用
     */
    public Page<SpecialServiceRequest> getByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // 若未传入状态或传入空字符串，返回全部
        if (status == null || status.trim().isEmpty()) {
            return repository.findAll(pageable);
        }
        return repository.findByStatus(status, pageable);
    }

    /**
     * 将Map转换为JSON字符串
     */
    public String mapToJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }

    /**
     * 将JSON字符串转换为Map
     */
    public Map<String, Object> jsonToMap(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}


