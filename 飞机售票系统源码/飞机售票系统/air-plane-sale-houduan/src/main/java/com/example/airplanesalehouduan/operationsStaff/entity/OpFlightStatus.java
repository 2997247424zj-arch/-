package com.example.airplanesalehouduan.operationsStaff.entity;

/**
 * 航班运行状态（与数据库 flights.status 枚举保持一致）
 */
public enum OpFlightStatus {
    scheduled,
    delayed,
    cancelled,
    boarding,
    departed,
    arrived
}
