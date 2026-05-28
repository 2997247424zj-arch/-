package com.example.airplanesalehouduan.admin.repository;


import com.example.airplanesalehouduan.admin.entity.AdminFlight;
import com.example.airplanesalehouduan.admin.entity.AdminFlight.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminFlightRepository extends JpaRepository<AdminFlight, Integer>, JpaSpecificationExecutor<AdminFlight> {

    /**
     * 根据航班号查找
     */
    List<AdminFlight> findByFlightNo(String flightNo);

    /**
     * 检查特定航班号和出发时间组合是否已存在（用于避免唯一约束冲突）
     */
    boolean existsByFlightNoAndSchedDepTime(String flightNo, LocalDateTime schedDepTime);

    /**
     * 搜索航班：根据出发机场、到达机场和出发日期范围
     */
    @Query("SELECT f FROM AdminFlight f WHERE " +
            "f.originAirport = :originAirport AND " +
            "f.destAirport = :destAirport AND " +
            "DATE(f.schedDepTime) = DATE(:departureDate) AND " +
            "f.status IN :statusList " +
            "ORDER BY f.schedDepTime ASC")
    List<AdminFlight> searchFlights(
            @Param("originAirport") String originAirport,
            @Param("destAirport") String destAirport,
            @Param("departureDate") LocalDateTime departureDate,
            @Param("statusList") List<FlightStatus> statusList
    );

    /**
     * 搜索航班：根据出发机场、到达机场和出发日期范围（日期范围查询）
     */
    @Query("SELECT f FROM AdminFlight f WHERE " +
            "f.originAirport = :originAirport AND " +
            "f.destAirport = :destAirport AND " +
            "f.schedDepTime >= :startDate AND " +
            "f.schedDepTime < :endDate AND " +
            "f.status IN :statusList " +
            "ORDER BY f.schedDepTime ASC")
    List<AdminFlight> searchFlightsByDateRange(
            @Param("originAirport") String originAirport,
            @Param("destAirport") String destAirport,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statusList") List<FlightStatus> statusList
    );
}

