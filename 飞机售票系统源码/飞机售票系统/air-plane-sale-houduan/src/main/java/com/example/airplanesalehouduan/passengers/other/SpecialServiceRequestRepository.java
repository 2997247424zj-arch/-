package com.example.airplanesalehouduan.passengers.other;


import com.example.airplanesalehouduan.passengers.entity.SpecialServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 重点旅客预约Repository
 */
@Repository
public interface SpecialServiceRequestRepository extends JpaRepository<SpecialServiceRequest, Long> {

    /**
     * 根据乘客ID查询所有预约（分页）
     */
    Page<SpecialServiceRequest> findByPassengerId(Integer passengerId, Pageable pageable);

    /**
     * 根据乘客ID和状态查询预约（分页）
     */
    Page<SpecialServiceRequest> findByPassengerIdAndStatus(Integer passengerId, String status, Pageable pageable);

    /**
     * 根据乘客ID查询所有预约（不分页）
     */
    List<SpecialServiceRequest> findByPassengerId(Integer passengerId);

    /**
     * 根据订单号查询预约
     */
    List<SpecialServiceRequest> findByOrderNo(String orderNo);

    /**
     * 根据乘客ID和订单号查询预约
     */
    Optional<SpecialServiceRequest> findByPassengerIdAndOrderNo(Integer passengerId, String orderNo);

    /**
     * 根据状态查询预约（分页）
     */
    Page<SpecialServiceRequest> findByStatus(String status, Pageable pageable);

    /**
     * 统计乘客的预约数量
     */
    long countByPassengerId(Integer passengerId);

    /**
     * 统计乘客指定状态的预约数量
     */
    long countByPassengerIdAndStatus(Integer passengerId, String status);
}


