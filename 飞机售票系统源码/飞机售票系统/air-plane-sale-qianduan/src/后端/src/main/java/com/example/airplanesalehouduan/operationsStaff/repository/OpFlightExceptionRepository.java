package com.example.airplanesalehouduan.operationsStaff.repository;

import com.example.airplanesalehouduan.operationsStaff.entity.OpFlightException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpFlightExceptionRepository extends JpaRepository<OpFlightException, Long> {

    // 获取最近上报的异常（按 reported_at 降序），支持分页限制
    @Query("select e from OpFlightException e order by e.reportedAt desc")
    List<OpFlightException> findRecentExceptions(Pageable pageable);
}


