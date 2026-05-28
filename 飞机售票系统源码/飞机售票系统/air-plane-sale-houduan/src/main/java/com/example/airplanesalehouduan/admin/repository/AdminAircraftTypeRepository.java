package com.example.airplanesalehouduan.admin.repository;

import com.example.airplanesalehouduan.admin.entity.AdminAircraftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 机型管理Repository
 */
@Repository
public interface AdminAircraftTypeRepository extends JpaRepository<AdminAircraftType, Integer> {

    /**
     * 根据类型代码查找
     */
    Optional<AdminAircraftType> findByTypeCode(String typeCode);

    /**
     * 根据型号查找
     */
    Optional<AdminAircraftType> findByModel(String model);

    /**
     * 根据ID查找
     */
    Optional<AdminAircraftType> findById(Integer id);
}

