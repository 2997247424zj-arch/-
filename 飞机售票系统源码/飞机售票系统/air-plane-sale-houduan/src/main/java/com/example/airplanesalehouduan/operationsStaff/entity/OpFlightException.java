package com.example.airplanesalehouduan.operationsStaff.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 航班异常/事件（对应数据库表 flight_exceptions）
 */
@Entity
@Table(name = "flight_exceptions")
public class OpFlightException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exception_no", length = 40, nullable = false)
    private String exceptionNo;

    @Column(name = "flight_id", nullable = false)
    private Integer flightId;

    @Column(name = "flight_no", length = 20)
    private String flightNo;

    @Column(name = "exception_type", length = 50, nullable = false)
    private String exceptionType;

    @Column(name = "reported_by")
    private Integer reportedBy;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @Column(name = "delay_minutes")
    private Integer delayMinutes;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "operator_note", columnDefinition = "text")
    private String operatorNote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExceptionNo() {
        return exceptionNo;
    }

    public void setExceptionNo(String exceptionNo) {
        this.exceptionNo = exceptionNo;
    }

    public Integer getFlightId() {
        return flightId;
    }

    public void setFlightId(Integer flightId) {
        this.flightId = flightId;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public Integer getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Integer reportedBy) {
        this.reportedBy = reportedBy;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getOperatorNote() {
        return operatorNote;
    }

    public void setOperatorNote(String operatorNote) {
        this.operatorNote = operatorNote;
    }
}


