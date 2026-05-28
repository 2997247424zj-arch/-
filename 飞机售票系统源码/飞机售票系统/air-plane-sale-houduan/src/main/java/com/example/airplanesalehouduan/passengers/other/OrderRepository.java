package com.example.airplanesalehouduan.passengers.other;


import com.example.airplanesalehouduan.passengers.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问接口
 * 提供订单的CRUD操作和查询方法
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * 根据乘客ID分页查询订单列表（按创建时间倒序）
     */
    Page<Order> findByPassengerIdOrderByCreatedAtDesc(Integer passengerId, Pageable pageable);

    /**
     * 根据乘客ID和状态分页查询订单列表
     */
    Page<Order> findByPassengerIdAndStatusOrderByCreatedAtDesc(
            Integer passengerId,
            String status,
            Pageable pageable);

    /**
     * 根据订单号查询订单
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据乘客ID和订单号查询订单（用于验证订单是否属于该乘客）
     */
    Optional<Order> findByPassengerIdAndOrderNo(Integer passengerId, String orderNo);

    /**
     * 根据乘客ID查询最近的订单（用于快捷接口）
     */
    @Query("SELECT o FROM PassengerOrder o WHERE o.passengerId = :passengerId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByPassengerId(@Param("passengerId") Integer passengerId, Pageable pageable);

    /**
     * 根据乘客ID统计订单总数
     */
    long countByPassengerId(Integer passengerId);

    /**
     * 根据乘客ID和状态统计订单数量
     */
    long countByPassengerIdAndStatus(Integer passengerId, String status);

    /**
     * 根据乘客ID和日期范围查询订单
     */
    @Query("SELECT o FROM PassengerOrder o WHERE o.passengerId = :passengerId " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findByPassengerIdAndDateRange(
            @Param("passengerId") Integer passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 根据乘客ID和日期范围查询订单（用于统计）
     */
    @Query("SELECT o FROM PassengerOrder o WHERE o.passengerId = :passengerId " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "ORDER BY o.createdAt ASC")
    List<Order> findByPassengerIdAndDateRangeForStats(
            @Param("passengerId") Integer passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 统计乘客的总消费金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM PassengerOrder o WHERE o.passengerId = :passengerId")
    java.math.BigDecimal sumTotalAmountByPassengerId(@Param("passengerId") Integer passengerId);

    /**
     * 统计指定日期范围内的总消费金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM PassengerOrder o WHERE o.passengerId = :passengerId " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    java.math.BigDecimal sumTotalAmountByPassengerIdAndDateRange(
            @Param("passengerId") Integer passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 统计指定日期范围内的订单数量
     */
    @Query("SELECT COUNT(o) FROM PassengerOrder o WHERE o.passengerId = :passengerId " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    long countByPassengerIdAndDateRange(
            @Param("passengerId") Integer passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 查询乘客的所有订单（用于统计热门航线）
     */
    @Query("SELECT o FROM PassengerOrder o WHERE o.passengerId = :passengerId AND o.route IS NOT NULL")
    List<Order> findAllByPassengerIdWithRoute(@Param("passengerId") Integer passengerId);

    /**
     * 查询乘客订单的所有不同状态值（从数据库动态获取）
     */
    @Query("SELECT DISTINCT o.status FROM PassengerOrder o WHERE o.passengerId = :passengerId")
    List<String> findDistinctStatusByPassengerId(@Param("passengerId") Integer passengerId);
}
