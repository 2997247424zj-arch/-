package com.example.airplanesalehouduan.passengers.service;


import com.example.airplanesalehouduan.passengers.entity.FrequentPassenger;
import com.example.airplanesalehouduan.passengers.other.FrequentPassengerRepository;
import com.example.airplanesalehouduan.passengers.other.FrequentPassengerRequest;
import com.example.airplanesalehouduan.passengers.other.FrequentPassengerResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 常用乘客服务类
 * 负责处理常用乘客的业务逻辑，确保数据隔离
 */
@Service
public class FrequentPassengerService {

    @Autowired
    private FrequentPassengerRepository frequentPassengerRepository;

    /**
     * 添加常用乘客
     * @param userId 用户ID（从session中获取，确保数据隔离）
     * @param request 常用乘客信息
     * @return 添加后的常用乘客信息
     * @throws Exception 如果身份证号已存在或其他验证失败
     */
    @Transactional
    public FrequentPassengerResponse addFrequentPassenger(Integer userId, FrequentPassengerRequest request) throws Exception {
        // 验证必填字段
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new Exception("乘客姓名不能为空");
        }
        if (request.getIdCard() == null || request.getIdCard().trim().isEmpty()) {
            throw new Exception("身份证号不能为空");
        }

        // 验证身份证号格式（18位）
        String idCard = request.getIdCard().trim();
        if (!idCard.matches("^\\d{17}[\\dXx]$")) {
            throw new Exception("身份证号格式不正确，请输入18位身份证号");
        }

        // 检查该用户是否已存在该身份证号的常用乘客
        if (frequentPassengerRepository.existsByUserIdAndIdCard(userId, idCard)) {
            throw new Exception("该身份证号的常用乘客已存在");
        }

        // 如果设置为默认，先取消其他默认
        if (request.getIsDefault() != null && request.getIsDefault()) {
            frequentPassengerRepository.clearDefaultByUserId(userId);
        }

        // 创建新的常用乘客
        FrequentPassenger passenger = new FrequentPassenger();
        passenger.setUserId(userId);
        passenger.setName(request.getName().trim());
        passenger.setIdCard(idCard.toUpperCase()); // 统一转为大写
        passenger.setRelationship(request.getRelationship());
        passenger.setPhone(request.getPhone());
        passenger.setRemarks(request.getRemarks());
        passenger.setIsDefault(request.getIsDefault() != null && request.getIsDefault());
        passenger.setStatus(FrequentPassenger.PassengerStatus.active);

        FrequentPassenger saved = frequentPassengerRepository.save(passenger);
        return convertToResponse(saved);
    }

    /**
     * 更新常用乘客信息
     * @param userId 用户ID（确保数据隔离）
     * @param id 常用乘客ID
     * @param request 更新的信息
     * @return 更新后的常用乘客信息
     * @throws Exception 如果常用乘客不存在或验证失败
     */
    @Transactional
    public FrequentPassengerResponse updateFrequentPassenger(Integer userId, Long id, FrequentPassengerRequest request) throws Exception {
        // 查找常用乘客（确保属于当前用户）
        Optional<FrequentPassenger> optional = frequentPassengerRepository.findByIdAndUserId(id, userId);
        if (optional.isEmpty()) {
            throw new Exception("常用乘客不存在或无权限访问");
        }

        FrequentPassenger passenger = optional.get();

        // 如果已删除，不允许更新
        if (passenger.getStatus() == FrequentPassenger.PassengerStatus.deleted) {
            throw new Exception("该常用乘客已删除，无法更新");
        }

        // 验证必填字段
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            passenger.setName(request.getName().trim());
        }
        if (request.getIdCard() != null && !request.getIdCard().trim().isEmpty()) {
            String idCard = request.getIdCard().trim();
            // 验证身份证号格式
            if (!idCard.matches("^\\d{17}[\\dXx]$")) {
                throw new Exception("身份证号格式不正确，请输入18位身份证号");
            }
            // 如果身份证号改变，检查是否与其他常用乘客冲突
            if (!idCard.equalsIgnoreCase(passenger.getIdCard())) {
                if (frequentPassengerRepository.existsByUserIdAndIdCard(userId, idCard)) {
                    throw new Exception("该身份证号的常用乘客已存在");
                }
                passenger.setIdCard(idCard.toUpperCase());
            }
        }

        // 更新其他字段
        if (request.getRelationship() != null) {
            passenger.setRelationship(request.getRelationship());
        }
        if (request.getPhone() != null) {
            passenger.setPhone(request.getPhone());
        }
        if (request.getRemarks() != null) {
            passenger.setRemarks(request.getRemarks());
        }

        // 处理默认设置
        if (request.getIsDefault() != null) {
            if (request.getIsDefault()) {
                // 设置为默认，先取消其他默认
                frequentPassengerRepository.clearDefaultByUserId(userId);
            }
            passenger.setIsDefault(request.getIsDefault());
        }

        FrequentPassenger updated = frequentPassengerRepository.save(passenger);
        return convertToResponse(updated);
    }

    /**
     * 删除常用乘客（软删除）
     * @param userId 用户ID（确保数据隔离）
     * @param id 常用乘客ID
     * @throws Exception 如果常用乘客不存在
     */
    @Transactional
    public void deleteFrequentPassenger(Integer userId, Long id) throws Exception {
        Optional<FrequentPassenger> optional = frequentPassengerRepository.findByIdAndUserId(id, userId);
        if (optional.isEmpty()) {
            throw new Exception("常用乘客不存在或无权限访问");
        }

        FrequentPassenger passenger = optional.get();
        passenger.setStatus(FrequentPassenger.PassengerStatus.deleted);
        frequentPassengerRepository.save(passenger);
    }

    /**
     * 获取用户的所有有效常用乘客
     * @param userId 用户ID
     * @return 常用乘客列表
     */
    public List<FrequentPassengerResponse> getFrequentPassengers(Integer userId) {
        List<FrequentPassenger> passengers = frequentPassengerRepository.findByUserIdAndStatus(
                userId,
                FrequentPassenger.PassengerStatus.active
        );
        return passengers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取常用乘客详情
     * @param userId 用户ID（确保数据隔离）
     * @param id 常用乘客ID
     * @return 常用乘客信息
     * @throws Exception 如果常用乘客不存在
     */
    public FrequentPassengerResponse getFrequentPassengerById(Integer userId, Long id) throws Exception {
        Optional<FrequentPassenger> optional = frequentPassengerRepository.findByIdAndUserId(id, userId);
        if (optional.isEmpty()) {
            throw new Exception("常用乘客不存在或无权限访问");
        }
        return convertToResponse(optional.get());
    }

    /**
     * 设置默认常用乘客
     * @param userId 用户ID
     * @param id 常用乘客ID
     * @throws Exception 如果常用乘客不存在
     */
    @Transactional
    public void setDefaultFrequentPassenger(Integer userId, Long id) throws Exception {
        Optional<FrequentPassenger> optional = frequentPassengerRepository.findByIdAndUserId(id, userId);
        if (optional.isEmpty()) {
            throw new Exception("常用乘客不存在或无权限访问");
        }

        FrequentPassenger passenger = optional.get();
        if (passenger.getStatus() == FrequentPassenger.PassengerStatus.deleted) {
            throw new Exception("该常用乘客已删除，无法设置为默认");
        }

        // 取消其他默认
        frequentPassengerRepository.clearDefaultByUserId(userId);
        // 设置当前为默认
        passenger.setIsDefault(true);
        frequentPassengerRepository.save(passenger);
    }

    /**
     * 获取用户的默认常用乘客
     * @param userId 用户ID
     * @return 默认常用乘客，如果没有则返回null
     */
    public FrequentPassengerResponse getDefaultFrequentPassenger(Integer userId) {
        Optional<FrequentPassenger> optional = frequentPassengerRepository.findByUserIdAndIsDefaultTrueAndStatus(
                userId,
                FrequentPassenger.PassengerStatus.active
        );
        return optional.map(this::convertToResponse).orElse(null);
    }

    /**
     * 实体转响应DTO
     */
    private FrequentPassengerResponse convertToResponse(FrequentPassenger passenger) {
        FrequentPassengerResponse response = new FrequentPassengerResponse();
        BeanUtils.copyProperties(passenger, response);
        response.setStatus(passenger.getStatus().name());
        return response;
    }
}
