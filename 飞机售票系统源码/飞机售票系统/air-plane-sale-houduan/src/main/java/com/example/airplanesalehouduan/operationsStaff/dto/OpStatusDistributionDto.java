package com.example.airplanesalehouduan.operationsStaff.dto;

import com.example.airplanesalehouduan.operationsStaff.entity.OpFlightStatus;
import com.example.airplanesalehouduan.operationsStaff.repository.OpFlightRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 今日航班状态分布 DTO（对接前端“今日航班状态分布”模块）
 */
public class OpStatusDistributionDto {

    private long scheduled;
    private long delayed;
    private long cancelled;
    private long boarding;
    private long departed;
    private long arrived;

    public static OpStatusDistributionDto from(List<OpFlightRepository.PlanStatusCountProjection> rows) {
        Map<OpFlightStatus, Long> map = new EnumMap<>(OpFlightStatus.class);
        for (OpFlightStatus status : OpFlightStatus.values()) {
            map.put(status, 0L);
        }
        for (OpFlightRepository.PlanStatusCountProjection row : rows) {
            String raw = row.getStatus();
            if (raw == null) continue;
            OpFlightStatus status = normalize(raw);
            if (status != null) {
                map.put(status, row.getCnt());
            }
        }
        OpStatusDistributionDto dto = new OpStatusDistributionDto();
        dto.scheduled = map.get(OpFlightStatus.scheduled);
        dto.delayed = map.get(OpFlightStatus.delayed);
        dto.cancelled = map.get(OpFlightStatus.cancelled);
        dto.boarding = map.get(OpFlightStatus.boarding);
        dto.departed = map.get(OpFlightStatus.departed);
        dto.arrived = map.get(OpFlightStatus.arrived);
        return dto;
    }

    /**
     * 兼容多语言 / 大小写 / 不同写法的状态映射
     */
    private static OpFlightStatus normalize(String raw) {
        String clean = raw.trim();
        String v = clean.toLowerCase();
        // 英文常见写法
        if (Set.of("scheduled", "on_time", "on-time", "ontime", "normal").contains(v)) {
            return OpFlightStatus.scheduled;
        }
        if (Set.of("delayed", "delay", "late").contains(v)) {
            return OpFlightStatus.delayed;
        }
        if (Set.of("cancelled", "canceled", "cancel", "cnl").contains(v)) {
            return OpFlightStatus.cancelled;
        }
        if (Set.of("boarding", "board").contains(v)) {
            return OpFlightStatus.boarding;
        }
        if (Set.of("departed", "depart", "off").contains(v)) {
            return OpFlightStatus.departed;
        }
        if (Set.of("arrived", "arrive", "landed").contains(v)) {
            return OpFlightStatus.arrived;
        }
        // 中文兼容
        if (Set.of("准点", "正点", "正常").contains(clean)) {
            return OpFlightStatus.scheduled;
        }
        if (Set.of("延误", "晚点").contains(clean)) {
            return OpFlightStatus.delayed;
        }
        if (Set.of("取消").contains(clean)) {
            return OpFlightStatus.cancelled;
        }
        if (Set.of("登机").contains(clean)) {
            return OpFlightStatus.boarding;
        }
        if (Set.of("起飞", "已起飞").contains(clean)) {
            return OpFlightStatus.departed;
        }
        if (Set.of("到达", "抵达").contains(clean)) {
            return OpFlightStatus.arrived;
        }
        return null;
    }

    public long getScheduled() {
        return scheduled;
    }

    public long getDelayed() {
        return delayed;
    }

    public long getCancelled() {
        return cancelled;
    }

    public long getBoarding() {
        return boarding;
    }

    public long getDeparted() {
        return departed;
    }

    public long getArrived() {
        return arrived;
    }
}
