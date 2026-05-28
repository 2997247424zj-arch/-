package com.example.airplanesalehouduan.passengers.service;

import com.example.airplanesalehouduan.passengers.entity.Ticket;
import com.example.airplanesalehouduan.passengers.entity.TicketDto;
import com.example.airplanesalehouduan.passengers.other.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private com.example.airplanesalehouduan.passengers.other.OrderRepository orderRepository;

    private RowMapper<TicketDto> ticketMapper = new RowMapper<TicketDto>() {
        @Override
        public TicketDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            TicketDto t = new TicketDto();
            t.setId(rs.getLong("id"));
            t.setOrderNo(rs.getString("order_no"));
            t.setPassengerId(rs.getInt("passenger_id"));
            t.setPassengerName(rs.getString("passenger_name"));
            t.setIdCard(rs.getString("id_card"));
            t.setPhone(rs.getString("phone"));
            t.setFlightId(rs.getInt("flight_id"));
            t.setFlightNo(rs.getString("flight_no"));
            t.setOriginAirport(rs.getString("origin_airport"));
            t.setDestAirport(rs.getString("dest_airport"));
            t.setRoute(rs.getString("route"));
            java.sql.Timestamp dep = rs.getTimestamp("departure_time");
            if (dep != null) t.setDepartureTime(dep.toLocalDateTime());
            java.sql.Timestamp arr = rs.getTimestamp("arrival_time");
            if (arr != null) t.setArrivalTime(arr.toLocalDateTime());
            t.setSeatNumber(rs.getString("seat_number"));
            t.setSeatClass(rs.getString("seat_class"));
            t.setBasePrice(rs.getBigDecimal("base_price"));
            t.setSeatFee(rs.getBigDecimal("seat_fee"));
            t.setDiscountFee(rs.getBigDecimal("discount_fee"));
            t.setTotalPrice(rs.getBigDecimal("total_price"));
            t.setTicketNo(rs.getString("ticket_no"));
            t.setStatus(rs.getString("status"));
            return t;
        }
    };

    public List<TicketDto> listTickets(Integer passengerId, Integer page, Integer size, String ticketNo) {
        int offset = Math.max(0, (page != null ? page : 0)) * (size != null ? size : 10);
        // 不再依赖 seats 表的 JOIN，直接使用 tickets 表内的 seat_number 字段
        StringBuilder sql = new StringBuilder("SELECT tickets.* FROM tickets WHERE tickets.passenger_id = ? AND (tickets.status IS NULL OR tickets.status <> 'deleted') ");
        if (ticketNo != null && !ticketNo.trim().isEmpty()) {
            sql.append(" AND tickets.ticket_no LIKE ? ");
            sql.append(" ORDER BY tickets.created_at DESC LIMIT ? OFFSET ? ");
            return jdbcTemplate.query(sql.toString(), ticketMapper, passengerId, "%" + ticketNo.trim() + "%", size, offset);
        } else {
            sql.append(" ORDER BY tickets.created_at DESC LIMIT ? OFFSET ? ");
            return jdbcTemplate.query(sql.toString(), ticketMapper, passengerId, size, offset);
        }
    }

    public int countTickets(Integer passengerId, String ticketNo) {
        String sql = "SELECT COUNT(1) FROM tickets WHERE passenger_id = ? AND (status IS NULL OR status <> 'deleted') " + (ticketNo != null && !ticketNo.isEmpty() ? " AND ticket_no LIKE ? " : "");
        if (ticketNo != null && !ticketNo.isEmpty()) {
            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, passengerId, "%" + ticketNo + "%");
            return cnt != null ? cnt : 0;
        } else {
            Integer cnt = jdbcTemplate.queryForObject(sql, Integer.class, passengerId);
            return cnt != null ? cnt : 0;
        }
    }

    public TicketDto getTicketDetail(String ticketIdOrNo, Integer passengerId) {
        // 不再 JOIN seats，直接使用 tickets.seat_number 字段
        String sql = "SELECT tickets.* FROM tickets WHERE (tickets.id = ? OR tickets.ticket_no = ?) AND tickets.passenger_id = ? AND (tickets.status IS NULL OR tickets.status <> 'deleted') LIMIT 1";
        List<TicketDto> list = jdbcTemplate.query(sql, ticketMapper, ticketIdOrNo, ticketIdOrNo, passengerId);
        return list.isEmpty() ? null : list.get(0);
    }

    public Ticket getTicketByTicketNo(String ticketNo, Integer passengerId) {
        return ticketRepository.findByPassengerIdAndTicketNo(passengerId, ticketNo).orElse(null);
    }

    @Transactional
    public boolean softDeleteTicket(Long ticketId, Integer passengerId) {
        try {
            // 首先验证机票是否存在且属于该乘客
            Ticket ticket = ticketRepository.findByIdAndPassengerIdAndDeletedAtIsNull(ticketId, passengerId);
            if (ticket == null) {
                return false; // 机票不存在或不属于该乘客或已被删除
            }

            // 执行软删除
            int result = ticketRepository.softDeleteByIdAndPassengerId(ticketId, passengerId);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean softDeleteTicketByTicketNo(String ticketNo, Integer passengerId) {
        try {
            // 首先验证机票是否存在且属于该乘客
            System.out.println("Soft deleting ticket by ticketNo: " + ticketNo + ", passengerId: " + passengerId);

            Ticket ticket = ticketRepository.findByPassengerIdAndTicketNo(passengerId, ticketNo).orElse(null);
            System.out.println("Found ticket: " + ticket);

            if (ticket == null) {
                System.out.println("Ticket not found or does not belong to passenger");
                return false; // 机票不存在或不属于该乘客
            }

            // 执行软删除
            System.out.println("Executing soft delete for ticketId: " + ticket.getId());
            int result = ticketRepository.softDeleteByIdAndPassengerId(ticket.getId(), passengerId);
            System.out.println("Soft delete result: " + result);

            // 如果软删除成功，同时将该订单标记为 deleted（软删除标记），以便后续查询不再返回该订单
            if (result > 0) {
                try {
                    String orderNo = ticket.getOrderNo();
                    if (orderNo != null && !orderNo.trim().isEmpty()) {
                        java.util.Optional<com.example.airplanesalehouduan.passengers.entity.Order> ordOpt = orderRepository.findByOrderNo(orderNo);
                        if (ordOpt != null && ordOpt.isPresent()) {
                            com.example.airplanesalehouduan.passengers.entity.Order ord = ordOpt.get();
                            ord.setStatus("deleted");
                            orderRepository.save(ord);
                        }
                    }
                } catch (Exception orderEx) {
                    // 不影响软删除结果，但记录日志以便排查
                    orderEx.printStackTrace();
                    System.err.println("尝试将订单标记为 deleted 失败: " + orderEx.getMessage());
                }
            }

            return result > 0;
        } catch (Exception e) {
            System.out.println("Exception during soft delete: ");
            e.printStackTrace();
            // 重新抛出异常，让事务正确回滚
            throw e;
        }
    }
}


