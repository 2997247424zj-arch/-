package com.example.airplanesalehouduan.operationsStaff.service;

import com.example.airplanesalehouduan.operationsStaff.entity.OpFlightException;
import com.example.airplanesalehouduan.operationsStaff.repository.OpFlightExceptionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OpExceptionsService {

    private final OpFlightExceptionRepository exceptionRepository;

    public OpExceptionsService(OpFlightExceptionRepository exceptionRepository) {
        this.exceptionRepository = exceptionRepository;
    }

    /**
     * 获取最近上报的异常事件
     */
    public List<OpFlightException> listRecentExceptions(int limit) {
        if (limit <= 0) limit = 20;
        return exceptionRepository.findRecentExceptions(PageRequest.of(0, limit));
    }

    /**
     * 分页获取最近上报的异常事件
     */
    public List<OpFlightException> listRecentExceptions(int page, int size) {
        if (size <= 0) size = 20;
        if (page < 0) page = 0;
        return exceptionRepository.findRecentExceptions(PageRequest.of(page, size));
    }

    /**
     * 统计异常事件总数
     */
    public long countExceptions() {
        return exceptionRepository.count();
    }

    /**
     * 更新异常处理状态和备注（运营处理）
     */
    public Optional<OpFlightException> updateExceptionStatus(Long id, String status, String operatorNote) {
        var opt = exceptionRepository.findById(id);
        if (opt.isPresent()) {
            OpFlightException ex = opt.get();
            ex.setStatus(status);
            if (operatorNote != null) ex.setOperatorNote(operatorNote);
            if ("resolved".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                ex.setResolvedAt(LocalDateTime.now());
            }
            exceptionRepository.save(ex);
        }
        return opt;
    }
}


