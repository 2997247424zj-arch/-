package com.example.airplanesalehouduan.admin.service;

import com.example.airplanesalehouduan.admin.dto.AdminAlertDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminAlertsService {

    private final JdbcTemplate jdbcTemplate;

    public AdminAlertsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取最新的异常告警（从 flights 表衍生）
     */
    public List<AdminAlertDto> getLatestAlerts(int page, int size, String statusFilter) {
        // fetch from flights
        String sqlFlights = "SELECT id, flight_no, status, updated_at FROM flights WHERE status IN ('delayed','cancelled')";
        List<AdminAlertDto> fromFlights = jdbcTemplate.query(sqlFlights, (rs, rowNum) -> mapFlightRowToDto(rs));

        // fetch from flight_exceptions if table exists
        List<AdminAlertDto> fromExceptions;
        try {
            // use reported_at (DB column) instead of created_at — table uses reported_at per schema
            String sqlExceptions = "SELECT * FROM flight_exceptions ORDER BY reported_at DESC";
            fromExceptions = jdbcTemplate.query(sqlExceptions, (rs, rowNum) -> mapExceptionRowToDto(rs));
        } catch (Exception ex) {
            // table may not exist in some environments
            fromExceptions = List.of();
        }

        // merge lists and sort by time (most recent first)
        List<AdminAlertDto> merged = new java.util.ArrayList<>();
        merged.addAll(fromFlights);
        merged.addAll(fromExceptions);

        // 按时间从新到旧排序
        merged.sort((a, b) -> {
            java.time.LocalDateTime ta = a.getUpdatedAt() == null ? java.time.LocalDateTime.MIN : a.getUpdatedAt();
            java.time.LocalDateTime tb = b.getUpdatedAt() == null ? java.time.LocalDateTime.MIN : b.getUpdatedAt();
            return tb.compareTo(ta);
        });

        // 不再对 flightNo+title 去重，保持数据库中每一条 flight_exceptions 记录都能看到。
        // 根据 statusFilter 进行筛选：如果 statusFilter 为 "pending"，则返回 status != 'resolved' 的项（包括 null）；如果为 "processed"，则只返回 status == 'resolved'。
        java.util.List<AdminAlertDto> filtered;
        if (statusFilter == null || statusFilter.isBlank()) {
            filtered = merged;
        } else {
            String sf = statusFilter.trim().toLowerCase();
            filtered = merged.stream().filter(item -> {
                String st = item.getStatus() == null ? "" : item.getStatus().toString().toLowerCase();
                if ("pending".equals(sf)) {
                    // 非 resolved 都视为未处理
                    return !"resolved".equals(st);
                } else if ("processed".equals(sf) || "resolved".equals(sf)) {
                    return "resolved".equals(st);
                } else {
                    // 如果传入具体 status 字符串，则严格匹配该状态（大小写不敏感）
                    return sf.equals(st);
                }
            }).toList();
        }

        // 只在返回时按 page/size 做分页（避免一次性返回太多数据影响前端）。
        int start = Math.max(0, page * size);
        if (start >= filtered.size()) {
            return List.of();
        }
        int end = Math.min(filtered.size(), start + size);
        return filtered.subList(start, end);
    }

    private AdminAlertDto mapRowToDto(ResultSet rs) throws SQLException {
        // legacy mapper kept for compatibility (not used)
        return mapFlightRowToDto(rs);
    }

    private AdminAlertDto mapFlightRowToDto(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String flightNo = rs.getString("flight_no");
        String status = rs.getString("status");
        LocalDateTime updatedAt = rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : LocalDateTime.now();

        String level = "info";
        String title = flightNo + " 航班通知";
        String desc = "航班状态：" + status;
        if ("delayed".equals(status)) {
            level = "urgent";
            title = flightNo + " 航班严重延误";
            desc = flightNo + "航班延误超过预计，请立即处理";
        } else if ("cancelled".equals(status)) {
            level = "warning";
            title = flightNo + " 航班已取消";
            desc = flightNo + "航班已被取消，请安排乘客改签或退票";
        }
        return new AdminAlertDto(id, flightNo, level, title, desc, updatedAt, status, null, null);
    }

    private AdminAlertDto mapExceptionRowToDto(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String flightNo = null;
        try {
            flightNo = rs.getString("flight_no");
        } catch (SQLException e) {
            // column may not exist; leave flightNo null
        }
        String status = null;
        try {
            status = rs.getString("status");
        } catch (SQLException e) {
            status = null;
        }
        String note = null;
        try {
            note = rs.getString("operator_note");
        } catch (SQLException e) {
            note = null;
        }
        LocalDateTime createdAt = null;
        try {
            // schema defines reported_at as the timestamp column
            java.sql.Timestamp ts = rs.getTimestamp("reported_at");
            createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        } catch (SQLException e) {
            createdAt = LocalDateTime.now();
        }

        String level = "warning";
        String title = flightNo != null && !flightNo.isBlank() ? (flightNo + " 异常事件") : "航班异常事件";
        String reason = null;
        try {
            reason = rs.getString("reason");
        } catch (SQLException e) {
            reason = null;
        }
        String desc = (reason != null && !reason.isBlank()) ? reason :
                (note != null && !note.isBlank() ? note : (status != null ? ("异常：" + status) : "异常事件"));

        return new AdminAlertDto(id, flightNo, level, title, desc, createdAt, status, reason, note);
    }

    /**
     * 创建一个异常事件记录
     * body 可能包含：exceptionNo, flightId, flightNo, exceptionType, reportedBy, reason, delayMinutes, status
     */
    public long createException(java.util.Map<String, Object> body) {
        String exceptionNo = body.getOrDefault("exceptionNo", "EX" + System.currentTimeMillis()).toString();
        Integer flightId = body.get("flightId") == null ? null : Integer.valueOf(body.get("flightId").toString());
        String flightNo = body.getOrDefault("flightNo", null) == null ? null : body.get("flightNo").toString();
        String exceptionType = body.getOrDefault("exceptionType", "unknown").toString();
        Integer reportedBy = body.get("reportedBy") == null ? null : Integer.valueOf(body.get("reportedBy").toString());
        String reason = body.getOrDefault("reason", null) == null ? null : body.get("reason").toString();
        Integer delayMinutes = body.get("delayMinutes") == null ? null : Integer.valueOf(body.get("delayMinutes").toString());
        String status = body.getOrDefault("status", "reported").toString();

        final String sql = "INSERT INTO flight_exceptions (exception_no, flight_id, flight_no, exception_type, reported_by, reason, delay_minutes, status, operator_note) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, exceptionNo);
            if (flightId != null) ps.setInt(2, flightId); else ps.setNull(2, java.sql.Types.INTEGER);
            if (flightNo != null) ps.setString(3, flightNo); else ps.setNull(3, java.sql.Types.VARCHAR);
            ps.setString(4, exceptionType);
            if (reportedBy != null) ps.setInt(5, reportedBy); else ps.setNull(5, java.sql.Types.INTEGER);
            if (reason != null) ps.setString(6, reason); else ps.setNull(6, java.sql.Types.VARCHAR);
            if (delayMinutes != null) ps.setInt(7, delayMinutes); else ps.setNull(7, java.sql.Types.INTEGER);
            ps.setString(8, status);
            ps.setNull(9, java.sql.Types.VARCHAR);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key == null ? -1L : key.longValue();
    }

    /**
     * 更新异常事件状态与运营备注
     */
    public void updateExceptionStatus(long id, String status, String operatorNote) {
        if (status == null) throw new IllegalArgumentException("status required");
        if ("resolved".equalsIgnoreCase(status)) {
            String sql = "UPDATE flight_exceptions SET status = ?, operator_note = ?, resolved_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, status, operatorNote, id);
        } else {
            String sql = "UPDATE flight_exceptions SET status = ?, operator_note = ? WHERE id = ?";
            jdbcTemplate.update(sql, status, operatorNote, id);
        }
    }
}


