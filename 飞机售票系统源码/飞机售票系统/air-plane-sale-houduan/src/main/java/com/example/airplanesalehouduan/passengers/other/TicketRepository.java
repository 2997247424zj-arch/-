package com.example.airplanesalehouduan.passengers.other;

import com.example.airplanesalehouduan.passengers.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 机票数据访问接口
 * 提供机票的CRUD操作和查询方法
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * 根据订单号查询机票列表
     */
    List<Ticket> findByOrderNo(String orderNo);

    /**
     * 根据乘客ID查询机票列表（分页）- 排除已软删除的机票
     */
    @Query("SELECT t FROM PassengerTicket t WHERE t.passengerId = :passengerId AND (t.status IS NULL OR t.status <> 'deleted') ORDER BY t.createdAt DESC")
    Page<Ticket> findByPassengerIdOrderByCreatedAtDesc(@Param("passengerId") Integer passengerId, Pageable pageable);

    /**
     * 根据机票号查询机票
     */
    Optional<Ticket> findByTicketNo(String ticketNo);

    /**
     * 根据乘客ID和订单号查询机票列表
     */
    List<Ticket> findByPassengerIdAndOrderNo(Integer passengerId, String orderNo);

    /**
     * 根据乘客ID和机票号查询机票（用于验证权限）
     */
    Optional<Ticket> findByPassengerIdAndTicketNo(Integer passengerId, String ticketNo);

    /**
     * 根据订单号统计机票数量
     */
    long countByOrderNo(String orderNo);

    /**
     * 根据航班ID统计机票数量（用于判断航班是否有关联机票）
     */
    long countByFlightId(Integer flightId);

    /**
     * 根据乘客ID统计机票数量 - 排除已软删除的机票
     */
    @Query("SELECT COUNT(t) FROM PassengerTicket t WHERE t.passengerId = :passengerId AND (t.status IS NULL OR t.status <> 'deleted')")
    long countByPassengerId(@Param("passengerId") Integer passengerId);

    /**
     * 根据ID软删除机票（将状态设置为deleted，并记录删除时间）
     */
    @Modifying
    @Query("UPDATE PassengerTicket t SET t.status = 'deleted', t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int softDeleteById(@Param("id") Long id);

    /**
     * 根据ID和乘客ID软删除机票（权限验证）
     */
    @Modifying
    @Query("UPDATE PassengerTicket t SET t.status = 'deleted', t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id AND t.passengerId = :passengerId")
    int softDeleteByIdAndPassengerId(@Param("id") Long id, @Param("passengerId") Integer passengerId);

    /**
     * 查找未被软删除的机票
     */
    @Query("SELECT t FROM PassengerTicket t WHERE (t.status IS NULL OR t.status <> 'deleted')")
    List<Ticket> findAllActive();

    /**
     * 根据乘客ID查找未被软删除的机票
     */
    @Query("SELECT t FROM PassengerTicket t WHERE t.passengerId = :passengerId AND (t.status IS NULL OR t.status <> 'deleted')")
    List<Ticket> findByPassengerIdActive(@Param("passengerId") Integer passengerId);

    /**
     * 根据乘客ID和订单号查找未被软删除的机票
     */
    @Query("SELECT t FROM PassengerTicket t WHERE t.passengerId = :passengerId AND t.orderNo = :orderNo AND (t.status IS NULL OR t.status <> 'deleted')")
    List<Ticket> findByPassengerIdAndOrderNoActive(@Param("passengerId") Integer passengerId, @Param("orderNo") String orderNo);

    /**
     * 根据ID查找未被软删除的机票
     */
    @Query("SELECT t FROM PassengerTicket t WHERE t.id = :id AND (t.status IS NULL OR t.status <> 'deleted')")
    Optional<Ticket> findByIdActive(@Param("id") Long id);

    /**
     * 根据ID和乘客ID查找未被软删除的机票
     */
    @Query("SELECT t FROM PassengerTicket t WHERE t.id = :id AND t.passengerId = :passengerId AND (t.status IS NULL OR t.status <> 'deleted')")
    Ticket findByIdAndPassengerIdAndDeletedAtIsNull(@Param("id") Long id, @Param("passengerId") Integer passengerId);
}

