package com.example.airplanesalehouduan.passengers.other;
import com.example.airplanesalehouduan.passengers.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserFlightRepository extends JpaRepository<Flight, Integer> {

    /**
     * 根据航班号查找
     */
    List<Flight> findByFlightNo(String flightNo);

    /**
     * 搜索航班：根据出发机场、到达机场和出发日期范围
     */
    @Query("SELECT f FROM PassengerFlight f WHERE " +
            "f.originAirport = :originAirport AND " +
            "f.destAirport = :destAirport AND " +
            "DATE(f.schedDepTime) = DATE(:departureDate) AND " +
            "f.status IN :statusList " +
            "ORDER BY f.schedDepTime ASC")
    List<Flight> searchFlights(
            @Param("originAirport") String originAirport,
            @Param("destAirport") String destAirport,
            @Param("departureDate") LocalDateTime departureDate,
            @Param("statusList") List<Flight.FlightStatus> statusList
    );

    /**
     * 搜索航班：根据出发机场、到达机场和出发日期范围（日期范围查询）
     */
    @Query("SELECT f FROM PassengerFlight f WHERE " +
            "f.originAirport = :originAirport AND " +
            "f.destAirport = :destAirport AND " +
            "f.schedDepTime >= :startDate AND " +
            "f.schedDepTime < :endDate AND " +
            "f.status IN :statusList " +
            "ORDER BY f.schedDepTime ASC")
    List<Flight> searchFlightsByDateRange(
            @Param("originAirport") String originAirport,
            @Param("destAirport") String destAirport,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statusList") List<Flight.FlightStatus> statusList
    );
}

