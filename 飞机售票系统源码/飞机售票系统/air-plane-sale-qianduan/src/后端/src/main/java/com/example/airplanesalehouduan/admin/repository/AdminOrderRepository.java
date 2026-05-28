package com.example.airplanesalehouduan.admin.repository;


import com.example.airplanesalehouduan.admin.entity.AdminOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问接口
 * 提供订单的CRUD操作和查询方法
 */
@Repository
public interface AdminOrderRepository extends JpaRepository<AdminOrder, Long>, JpaSpecificationExecutor<AdminOrder> {

    /**
     * 根据乘客ID分页查询订单列表（按创建时间倒序）
     * @param passengerId 乘客ID
     * @param pageable 分页参数
     * @return 订单分页结果
     */
    Page<AdminOrder> findByPassengerIdOrderByCreatedAtDesc(Integer passengerId, Pageable pageable);

    /**
     * 根据乘客ID和状态分页查询订单列表
     * @param passengerId 乘客ID
     * @param status 订单状态（字符串类型）
     * @param pageable 分页参数
     * @return 订单分页结果
     */
    Page<AdminOrder> findByPassengerIdAndStatusOrderByCreatedAtDesc(
            Integer passengerId,
            String status,
            Pageable pageable);

    /**
     * 根据订单号查询订单
     * @param orderNo 订单号
     * @return 订单对象
     */
    Optional<AdminOrder> findByOrderNo(String orderNo);

    /**
     * 根据乘客ID和订单号查询订单（用于验证订单是否属于该乘客）
     * @param passengerId 乘客ID
     * @param orderNo 订单号
     * @return 订单对象
     */
    Optional<AdminOrder> findByPassengerIdAndOrderNo(Integer passengerId, String orderNo);

    /**
     * 根据乘客ID查询最近的订单（用于快捷接口）
     * @param passengerId 乘客ID
     * @param pageable 限制数量
     * @return 订单列表
     */
    @Query("SELECT o FROM AdminOrder o WHERE o.passengerId = :passengerId ORDER BY o.createdAt DESC")
    List<AdminOrder> findRecentOrdersByPassengerId(@Param("passengerId") Integer passengerId, Pageable pageable);

    /**
     * 根据乘客ID统计订单总数
     * @param passengerId 乘客ID
     * @return 订单总数
     */
    long countByPassengerId(Integer passengerId);

    /**
     * 根据乘客ID和状态统计订单数量
     * @param passengerId 乘客ID
     * @param status 订单状态（字符串类型）
     * @return 订单数量
     */
    long countByPassengerIdAndStatus(Integer passengerId, String status);

    /**
     * 根据乘客ID和日期范围查询订单
     * @param passengerId 乘客ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页参数
     * @return 订单分页结果
     */
    @Query("SELECT o FROM AdminOrder o WHERE o.passengerId = :passengerId " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "ORDER BY o.createdAt DESC")
    Page<AdminOrder> findByPassengerIdAndDateRange(
            @Param("passengerId") Integer passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 根据乘客ID和日期范围查询订单（用于统计）
     * @param passengerId 乘客ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 订单列表
     */
    @Query("SELECT o FROM AdminOrder o WHERE o.passengerId = :passengerId " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "ORDER BY o.createdAt ASC")
    List<AdminOrder> findByPassengerIdAndDateRangeForStats(
            @Param("passengerId") Integer passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 统计乘客的总消费金额
     * @param passengerId 乘客ID
     * @return 总消费金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM AdminOrder o WHERE o.passengerId = :passengerId")
    BigDecimal sumTotalAmountByPassengerId(@Param("passengerId") Integer passengerId);

    /**
     * 统计指定日期范围内的总消费金额
     * @param passengerId 乘客ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总消费金额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM AdminOrder o WHERE o.passengerId = :passengerId " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal sumTotalAmountByPassengerIdAndDateRange(
            @Param("passengerId") Integer passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 统计指定日期范围内的订单数量
     * @param passengerId 乘客ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 订单数量
     */
    @Query("SELECT COUNT(o) FROM AdminOrder o WHERE o.passengerId = :passengerId " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    long countByPassengerIdAndDateRange(
            @Param("passengerId") Integer passengerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 查询乘客的所有订单（用于统计热门航线）
     * @param passengerId 乘客ID
     * @return 订单列表
     */
    @Query("SELECT o FROM AdminOrder o WHERE o.passengerId = :passengerId AND o.route IS NOT NULL")
    List<AdminOrder> findAllByPassengerIdWithRoute(@Param("passengerId") Integer passengerId);
}

