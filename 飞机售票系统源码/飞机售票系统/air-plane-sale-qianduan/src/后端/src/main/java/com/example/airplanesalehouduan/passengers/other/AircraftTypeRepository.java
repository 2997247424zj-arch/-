package com.example.airplanesalehouduan.passengers.other;


import com.example.airplanesalehouduan.passengers.entity.AircraftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AircraftTypeRepository extends JpaRepository<AircraftType, Integer> {

    /**
     * 根据机型代码查找
     */
    Optional<AircraftType> findByTypeCode(String typeCode);

    /**
     * 检查机型代码是否存在
     */
    boolean existsByTypeCode(String typeCode);
}


