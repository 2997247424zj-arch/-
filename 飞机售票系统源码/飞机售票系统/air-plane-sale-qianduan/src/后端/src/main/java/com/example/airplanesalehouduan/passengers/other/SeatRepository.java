package com.example.airplanesalehouduan.passengers.other;

import com.example.airplanesalehouduan.passengers.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {

    /**
     * 根据航班ID查找所有座位
     */
    List<Seat> findByFlightId(Integer flightId);

    /**
     * 根据航班ID和座位号查找座位
     */
    Optional<Seat> findByFlightIdAndSeatNumber(Integer flightId, String seatNumber);

    /**
     * 根据航班ID和状态查找座位
     */
    List<Seat> findByFlightIdAndStatus(Integer flightId, String status);

    /**
     * 根据航班ID和舱位等级查找座位
     */
    List<Seat> findByFlightIdAndCabinClass(Integer flightId, String cabinClass);

    /**
     * 根据航班ID、舱位等级和状态查找座位
     */
    List<Seat> findByFlightIdAndCabinClassAndStatus(Integer flightId, String cabinClass, String status);

    /**
     * 统计航班某个舱位等级的可用座位数（状态为中文“可用”）
     */
    @Query("SELECT COUNT(s) FROM PassengerSeat s WHERE s.flight.id = :flightId AND s.cabinClass = :cabinClass AND s.status = '可用'")
    Long countAvailableSeatsByFlightIdAndCabinClass(@Param("flightId") Integer flightId, @Param("cabinClass") String cabinClass);

    /**
     * 统计航班所有可用座位数（状态为中文“可用”）
     */
    @Query("SELECT COUNT(s) FROM PassengerSeat s WHERE s.flight.id = :flightId AND s.status = '可用'")
    Long countAvailableSeatsByFlightId(@Param("flightId") Integer flightId);

    /**
     * 根据航班ID和座位ID列表查询座位
     */
    @Query("SELECT s FROM PassengerSeat s WHERE s.flight.id = :flightId AND s.id IN :seatIds")
    List<Seat> findByFlightIdAndSeatIds(@Param("flightId") Integer flightId, @Param("seatIds") List<Integer> seatIds);
}

