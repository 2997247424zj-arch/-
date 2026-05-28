package com.example.airplanesalehouduan.passengers.other;



import com.example.airplanesalehouduan.passengers.entity.LoyaltyPoints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 积分变动记录Repository
 */
@Repository
public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Long> {

    /**
     * 根据乘客ID查询积分记录（分页）
     */
    Page<LoyaltyPoints> findByPassengerIdOrderByCreatedAtDesc(Integer passengerId, Pageable pageable);

    /**
     * 根据乘客ID查询所有积分记录
     */
    List<LoyaltyPoints> findByPassengerIdOrderByCreatedAtDesc(Integer passengerId);

    /**
     * 计算乘客总积分（获得-消费）
     * 使用原生SQL查询以支持COALESCE和CASE语句
     */
    @Query(value = "SELECT COALESCE(SUM(CASE WHEN lp.change_type = 'EARN' THEN lp.points ELSE -lp.points END), 0) " +
            "FROM loyalty_points lp WHERE lp.passenger_id = :passengerId",
            nativeQuery = true)
    Integer calculateTotalPoints(@Param("passengerId") Integer passengerId);

    /**
     * 计算本月获得的积分
     * 使用原生SQL查询以支持日期函数
     */
    @Query(value = "SELECT COALESCE(SUM(lp.points), 0) " +
            "FROM loyalty_points lp " +
            "WHERE lp.passenger_id = :passengerId " +
            "AND lp.change_type = 'EARN' " +
            "AND YEAR(lp.created_at) = YEAR(CURRENT_DATE) " +
            "AND MONTH(lp.created_at) = MONTH(CURRENT_DATE)",
            nativeQuery = true)
    Integer calculateMonthlyPoints(@Param("passengerId") Integer passengerId);

    /**
     * 计算待确认的积分（关联订单但订单状态为待确认）
     * 使用原生SQL查询
     */
    @Query(value = "SELECT COALESCE(SUM(lp.points), 0) " +
            "FROM loyalty_points lp " +
            "WHERE lp.passenger_id = :passengerId " +
            "AND lp.change_type = 'EARN' " +
            "AND lp.ref_order_id IS NOT NULL",
            nativeQuery = true)
    Integer calculatePendingPoints(@Param("passengerId") Integer passengerId);

    /**
     * 获取积分记录（包含原始created_at字符串，不进行时区转换）
     * 使用原生SQL查询，直接使用DATE_FORMAT将DATETIME转换为字符串
     */
    @Query(value = "SELECT lp.id, lp.passenger_id, lp.points, lp.change_type, " +
            "lp.ref_order_id, lp.remark, " +
            "DATE_FORMAT(lp.created_at, '%Y-%m-%d %H:%i:%s') as created_at_str " +
            "FROM loyalty_points lp " +
            "WHERE lp.passenger_id = :passengerId " +
            "ORDER BY lp.created_at DESC " +
            "LIMIT :offset, :size",
            nativeQuery = true)
    List<Map<String, Object>> findPointsHistoryWithRawDate(
            @Param("passengerId") Integer passengerId,
            @Param("offset") Long offset,
            @Param("size") Integer size);

    /**
     * 统计积分记录总数
     */
    @Query(value = "SELECT COUNT(*) FROM loyalty_points lp WHERE lp.passenger_id = :passengerId",
            nativeQuery = true)
    Long countByPassengerId(@Param("passengerId") Integer passengerId);
}
