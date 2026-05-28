package com.example.airplanesalehouduan.operationsStaff.service;

import com.example.airplanesalehouduan.operationsStaff.dto.OpFlightPlanDto;
import com.example.airplanesalehouduan.operationsStaff.dto.OpStatusDistributionDto;
import com.example.airplanesalehouduan.operationsStaff.repository.OpFlightPlanRepository;
import com.example.airplanesalehouduan.operationsStaff.repository.OpFlightRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 航空运营（OCC）相关业务逻辑（仅 @operationsStaff 使用）
 */
@Service
public class OpOperationsService {

    private final OpFlightPlanRepository planRepository;
    private final OpFlightRepository flightRepository;

    public OpOperationsService(OpFlightPlanRepository planRepository,
                               OpFlightRepository flightRepository) {
        this.planRepository = planRepository;
        this.flightRepository = flightRepository;
    }

    /**
     * 获取指定计划日期下的航班状态分布
     */
    public OpStatusDistributionDto getStatusDistribution(LocalDate planDate) {
        var rows = flightRepository.countByPlanStatus(planDate);
        boolean allZero = rows == null || rows.isEmpty() || rows.stream().allMatch(r -> r.getCnt() == 0);
        // 如果指定 planDate 没有数据，兜底返回全部（避免前端看到全 0）
        if (planDate != null && allZero) {
            rows = flightRepository.countByPlanStatus(null);
        }
        return OpStatusDistributionDto.from(rows);
    }

    /**
     * 获取指定计划日期的航班计划列表
     */
    public List<OpFlightPlanDto> getPlans(LocalDate planDate) {
        // 如果未指定 planDate，则返回所有计划（满足“显示所有而不是某一天”的需求）
        if (planDate == null) {
            return planRepository.findAll().stream()
                    .map(OpFlightPlanDto::from)
                    .collect(Collectors.toList());
        }
        return planRepository.findByPlanDate(planDate).stream()
                .map(OpFlightPlanDto::from)
                .collect(Collectors.toList());
    }
}
