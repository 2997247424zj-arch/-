package com.example.airplanesalehouduan.passengers.other;


import com.example.airplanesalehouduan.passengers.entity.FrequentPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 常用乘客数据访问层
 * 提供常用乘客的CRUD操作，所有查询都基于user_id进行数据隔离
 */
@Repository
public interface FrequentPassengerRepository extends JpaRepository<FrequentPassenger, Long> {

    /**
     * 根据用户ID查询所有有效的常用乘客
     * @param userId 用户ID
     * @return 常用乘客列表
     */
    List<FrequentPassenger> findByUserIdAndStatus(Integer userId, FrequentPassenger.PassengerStatus status);

    /**
     * 根据用户ID查询所有常用乘客（包括已删除的）
     * @param userId 用户ID
     * @return 常用乘客列表
     */
    List<FrequentPassenger> findByUserId(Integer userId);

    /**
     * 根据ID和用户ID查询常用乘客（确保数据隔离）
     * @param id 常用乘客ID
     * @param userId 用户ID
     * @return 常用乘客
     */
    Optional<FrequentPassenger> findByIdAndUserId(Long id, Integer userId);

    /**
     * 检查用户是否已存在该身份证号的常用乘客
     * @param userId 用户ID
     * @param idCard 身份证号
     * @return 是否存在
     */
    boolean existsByUserIdAndIdCard(Integer userId, String idCard);

    /**
     * 根据用户ID和身份证号查询常用乘客
     * @param userId 用户ID
     * @param idCard 身份证号
     * @return 常用乘客
     */
    Optional<FrequentPassenger> findByUserIdAndIdCard(Integer userId, String idCard);

    /**
     * 查询用户的默认常用乘客
     * @param userId 用户ID
     * @return 默认常用乘客
     */
    Optional<FrequentPassenger> findByUserIdAndIsDefaultTrueAndStatus(Integer userId, FrequentPassenger.PassengerStatus status);

    /**
     * 取消用户的所有默认常用乘客
     * @param userId 用户ID
     */
    @Modifying
    @Query("UPDATE PassengerFrequentPassenger fp SET fp.isDefault = false WHERE fp.userId = :userId")
    void clearDefaultByUserId(@Param("userId") Integer userId);

    /**
     * 统计用户的常用乘客数量
     * @param userId 用户ID
     * @param status 状态
     * @return 数量
     */
    long countByUserIdAndStatus(Integer userId, FrequentPassenger.PassengerStatus status);
}
