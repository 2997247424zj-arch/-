package com.example.airplanesalehouduan.passengers.other;

import com.example.airplanesalehouduan.passengers.entity.TicketCancelRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 取消机票申请仓库
 */
@Repository
public interface TicketCancelRequestRepository extends JpaRepository<TicketCancelRequest, Long>,
        JpaSpecificationExecutor<TicketCancelRequest> {

    Page<TicketCancelRequest> findByPassengerId(Integer passengerId, Pageable pageable);

    Page<TicketCancelRequest> findByPassengerIdAndStatus(Integer passengerId, String status, Pageable pageable);

    List<TicketCancelRequest> findByOrderno(String orderno);

    Optional<TicketCancelRequest> findByPassengerIdAndOrderno(Integer passengerId, String orderno);

    long countByStatus(String status);
}



