package com.example.airplanesalehouduan.passengers.other;

import com.example.airplanesalehouduan.passengers.entity.BaggageManagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 行李管理数据访问接口
 * 提供行李的CRUD操作和查询方法
 */
@Repository
public interface BaggageManagementRepository extends JpaRepository<BaggageManagement, Long>, JpaSpecificationExecutor<BaggageManagement> {

    /**
     * 根据乘客ID分页查询行李列表（按登记时间倒序）
     */
    Page<BaggageManagement> findByPassengerIdOrderByRegisteredTimeDesc(Integer passengerId, Pageable pageable);

    /**
     * 根据乘客ID和状态分页查询行李列表
     */
    Page<BaggageManagement> findByPassengerIdAndStatusOrderByRegisteredTimeDesc(
            Integer passengerId,
            String status,
            Pageable pageable);

    /**
     * 根据行李编号查询行李
     */
    Optional<BaggageManagement> findByBaggageNo(String baggageNo);

    /**
     * 根据乘客ID和行李编号查询行李（用于验证行李是否属于该乘客）
     */
    Optional<BaggageManagement> findByPassengerIdAndBaggageNo(Integer passengerId, String baggageNo);

    /**
     * 根据订单号查询行李列表
     */
    @Query("SELECT b FROM BaggageManagement b WHERE b.orderno = :orderno ORDER BY b.registeredTime DESC")
    java.util.List<BaggageManagement> findByOrderno(@Param("orderno") String orderno);

    /**
     * 根据乘客ID和订单号查询行李列表
     */
    @Query("SELECT b FROM BaggageManagement b WHERE b.passengerId = :passengerId AND b.orderno = :orderno ORDER BY b.registeredTime DESC")
    java.util.List<BaggageManagement> findByPassengerIdAndOrderno(
            @Param("passengerId") Integer passengerId,
            @Param("orderno") String orderno);

    /**
     * 根据航班号查询行李列表
     */
    @Query("SELECT b FROM BaggageManagement b WHERE b.flightNo = :flightNo ORDER BY b.registeredTime DESC")
    java.util.List<BaggageManagement> findByFlightNo(@Param("flightNo") String flightNo);
}

