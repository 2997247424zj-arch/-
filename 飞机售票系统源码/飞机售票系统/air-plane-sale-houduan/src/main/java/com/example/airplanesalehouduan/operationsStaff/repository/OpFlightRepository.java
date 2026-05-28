package com.example.airplanesalehouduan.operationsStaff.repository;

import com.example.airplanesalehouduan.operationsStaff.entity.OpFlight;
import com.example.airplanesalehouduan.operationsStaff.entity.OpFlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 航班信息仓储（仅供 @operationsStaff 使用）
 */
public interface OpFlightRepository extends JpaRepository<OpFlight, Integer> {

    interface PlanStatusCountProjection {
        String getStatus();

        long getCnt();
    }

    /**
     * 按计划日期统计状态分布
     * 优先使用 flight_plans.status，其次 flights.status
     */
    @Query(value = """
            SELECT COALESCE(p.status, f.status) AS status, COUNT(*) AS cnt
            FROM flight_plans p
            JOIN flights f ON p.flight_id = f.id
            WHERE (:planDate IS NULL OR p.plan_date = :planDate)
            GROUP BY COALESCE(p.status, f.status)
            """, nativeQuery = true)
    List<PlanStatusCountProjection> countByPlanStatus(@Param("planDate") LocalDate planDate);
}
