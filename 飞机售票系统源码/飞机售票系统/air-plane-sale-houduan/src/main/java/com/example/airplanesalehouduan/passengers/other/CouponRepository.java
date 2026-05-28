package com.example.airplanesalehouduan.passengers.other;

import com.example.airplanesalehouduan.passengers.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 优惠券Repository
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * 根据优惠券代码查询
     */
    Optional<Coupon> findByCode(String code);

    /**
     * 查询乘客可用的优惠券（专属券或通用券，且未过期）
     */
    @Query("SELECT c FROM Coupon c " +
            "WHERE (c.passengerId = :passengerId OR c.passengerId IS NULL) " +
            "AND c.status IN ('UNUSED', 'AVAILABLE') " +
            "AND c.validFrom <= :now AND c.validTo >= :now")
    List<Coupon> findAvailableCoupons(@Param("passengerId") Integer passengerId, @Param("now") LocalDateTime now);

    /**
     * 查询乘客的所有优惠券（包括专属券和通用券）
     */
    @Query("SELECT c FROM Coupon c " +
            "WHERE (c.passengerId = :passengerId OR c.passengerId IS NULL) " +
            "ORDER BY c.validTo ASC")
    List<Coupon> findAllByPassengerId(@Param("passengerId") Integer passengerId);

    /**
     * 查询乘客未领取的优惠券（通用券或专属券）
     */
    @Query("SELECT c FROM Coupon c " +
            "WHERE (c.passengerId = :passengerId OR c.passengerId IS NULL) " +
            "AND c.status = 'UNUSED' " +
            "AND c.validFrom <= :now AND c.validTo >= :now")
    List<Coupon> findUnusedCoupons(@Param("passengerId") Integer passengerId, @Param("now") LocalDateTime now);

    /**
     * 查询乘客已过期的优惠券
     */
    @Query("SELECT c FROM Coupon c " +
            "WHERE (c.passengerId = :passengerId OR c.passengerId IS NULL) " +
            "AND (c.validTo < :now OR c.status = 'EXPIRED')")
    List<Coupon> findExpiredCoupons(@Param("passengerId") Integer passengerId, @Param("now") LocalDateTime now);

    /**
     * 查询乘客已使用的优惠券
     */
    @Query("SELECT c FROM Coupon c " +
            "WHERE c.passengerId = :passengerId " +
            "AND c.status = 'USED'")
    List<Coupon> findUsedCoupons(@Param("passengerId") Integer passengerId);
}

