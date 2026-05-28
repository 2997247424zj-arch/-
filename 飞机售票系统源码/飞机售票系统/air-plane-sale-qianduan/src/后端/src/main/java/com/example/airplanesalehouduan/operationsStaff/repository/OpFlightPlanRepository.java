package com.example.airplanesalehouduan.operationsStaff.repository;

import com.example.airplanesalehouduan.operationsStaff.entity.OpFlightPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 航班计划仓储（仅供 @operationsStaff 使用）
 */
public interface OpFlightPlanRepository extends JpaRepository<OpFlightPlan, Integer> {

    List<OpFlightPlan> findByPlanDate(LocalDate planDate);
}
