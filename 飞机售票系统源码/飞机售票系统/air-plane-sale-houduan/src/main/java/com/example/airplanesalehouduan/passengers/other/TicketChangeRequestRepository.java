package com.example.airplanesalehouduan.passengers.other;



import com.example.airplanesalehouduan.passengers.entity.TicketChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 改签申请仓库
 */
@Repository
public interface TicketChangeRequestRepository extends JpaRepository<TicketChangeRequest, Long>, JpaSpecificationExecutor<TicketChangeRequest> {

    Page<TicketChangeRequest> findByPassengerId(Integer passengerId, Pageable pageable);

    Page<TicketChangeRequest> findByPassengerIdAndStatus(Integer passengerId, String status, Pageable pageable);

    List<TicketChangeRequest> findByOrderno(String orderno);

    Optional<TicketChangeRequest> findByPassengerIdAndOrderno(Integer passengerId, String orderno);

    /**
     * 根据状态查询（管理员用）
     */
    Page<TicketChangeRequest> findByStatus(String status, Pageable pageable);

    /**
     * 根据申请时间范围查询（管理员用）
     */
    @Query("SELECT t FROM TicketChangeRequest t WHERE t.requestTime >= :startDate AND t.requestTime <= :endDate")
    Page<TicketChangeRequest> findByRequestTimeBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 统计待处理申请数量
     */
    long countByStatus(String status);
}



