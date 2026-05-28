package com.example.airplanesalehouduan.operationsStaff.service;

import com.example.airplanesalehouduan.operationsStaff.dto.OpNetworkOverviewResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Date;
import java.util.*;

@Service
public class OpNetworkService {

    private final JdbcTemplate jdbcTemplate;
    private volatile OpNetworkOverviewResponse cached = null;
    private volatile String lastUpdated = null;

    public OpNetworkService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedRateString = "${network.refresh.ms:120000}", initialDelay = 5000)
    public void refreshNetworkStats() {
        try {
            cached = computeOverview();
            lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (cached != null) cached.setLastUpdated(lastUpdated);
        } catch (Exception e) {
            System.err.println("OpNetworkService.refreshNetworkStats error: " + e.getMessage());
        }
    }

    public OpNetworkOverviewResponse getOverview() {
        if (cached == null) {
            cached = computeOverview();
            lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (cached != null) cached.setLastUpdated(lastUpdated);
        }
        return cached;
    }

    /**
     * 获取分页的热门航线（支持 page/size 分页），返回结构便于前端渲染并包含 total/page/size
     */
    public Map<String, Object> getTopRoutesPaginated(int page, int size, String sortBy) {
        Map<String, Object> resp = new HashMap<>();
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        try {
            String orderClause = "ORDER BY revenue DESC";
            if ("orders".equalsIgnoreCase(sortBy)) orderClause = "ORDER BY orders DESC";
            else if ("occupancy".equalsIgnoreCase(sortBy)) orderClause = "ORDER BY occupancy DESC";

            // total distinct routes
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT CONCAT(origin_airport,'-',dest_airport)) FROM flights", Integer.class);
            int totalRoutes = (total == null) ? 0 : total;

            int offset = page * size;
            List<Map<String, Object>> rows = jdbcTemplate.query(
                    "SELECT f.origin_airport as origin, f.dest_airport as dest, CONCAT(f.origin_airport,' → ',f.dest_airport) as route, " +
                            "SUM(f.price) as revenue, COUNT(t.id) as orders, COUNT(f.id) as flight_count " +
                            "FROM flights f LEFT JOIN tickets t ON f.id = t.flight_id " +
                            "GROUP BY f.origin_airport, f.dest_airport " + orderClause + " LIMIT ? OFFSET ?",
                    new Object[]{size, offset},
                    (rs, rowNum) -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("origin", rs.getString("origin"));
                        m.put("dest", rs.getString("dest"));
                        m.put("route", rs.getString("route"));
                        m.put("name", rs.getString("route"));
                        m.put("revenue", rs.getDouble("revenue"));
                        m.put("orders", rs.getInt("orders"));
                        m.put("flightCount", rs.getInt("flight_count"));
                        return m;
                    });

            // 补充 avgPrice / occupancy / frequency（按页内逐条查询，适合小 page size）
            for (Map<String, Object> r : rows) {
                try {
                    String origin = (String) r.getOrDefault("origin", "");
                    String dest = (String) r.getOrDefault("dest", "");

                    Double avgPrice = jdbcTemplate.queryForObject(
                            "SELECT AVG(t.total_price) FROM tickets t JOIN flights f2 ON t.flight_id = f2.id WHERE f2.origin_airport = ? AND f2.dest_airport = ?",
                            new Object[]{origin, dest}, Double.class);
                    if (avgPrice == null) avgPrice = 0.0;

                    Integer totalSeats = jdbcTemplate.queryForObject(
                            "SELECT COUNT(s.id) FROM seats s JOIN flights f2 ON s.flight_id = f2.id WHERE f2.origin_airport = ? AND f2.dest_airport = ?",
                            new Object[]{origin, dest}, Integer.class);
                    int seatsCount = (totalSeats == null) ? 0 : totalSeats;

                    Integer flightCount = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM flights WHERE origin_airport = ? AND dest_airport = ?",
                            new Object[]{origin, dest}, Integer.class);
                    int fCount = (flightCount == null) ? 0 : flightCount;

                    double frequencyPerDay = (fCount > 0) ? ((double) fCount) / 30.0 : 0.0;

                    int ordersCount = ((Number) r.getOrDefault("orders", 0)).intValue();
                    double occupancy = (seatsCount > 0) ? ((double) ordersCount) * 100.0 / seatsCount : 0.0;

                    r.put("avgPrice", Math.round(avgPrice * 100.0) / 100.0);
                    r.put("occupancy", Math.round(occupancy * 10.0) / 10.0);
                    r.put("frequency", Math.round(frequencyPerDay * 100.0) / 100.0);
                } catch (Exception e) {
                    r.put("avgPrice", 0.0);
                    r.put("occupancy", 0.0);
                    r.put("frequency", 0.0);
                }
            }

            resp.put("topRoutes", rows);
            resp.put("total", totalRoutes);
            resp.put("page", page);
            resp.put("size", size);
            return resp;
        } catch (Exception e) {
            System.err.println("OpNetworkService.getTopRoutesPaginated failed: " + e.getMessage());
            resp.put("topRoutes", new ArrayList<>());
            resp.put("total", 0);
            resp.put("page", page);
            resp.put("size", size);
            return resp;
        }
    }

    private OpNetworkOverviewResponse computeOverview() {
        Map<String, Object> networkStats = new HashMap<>();

        Integer totalRoutes = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT CONCAT(origin_airport,'-',dest_airport)) FROM flights", Integer.class);
        int tRoutes = (totalRoutes == null) ? 0 : totalRoutes;
        networkStats.put("totalRoutes", tRoutes);

        // 使用订单/票务的真实销售额作为航线/网络营收来源，而不是 flights.price（后者为航班基础票价，可能与实际售出金额不一致）
        Double totalRevenue = jdbcTemplate.queryForObject("SELECT SUM(total_amount) FROM orders", Double.class);
        double tRevenue = (totalRevenue == null) ? 0.0 : totalRevenue;
        networkStats.put("totalRevenue", tRevenue);

        Integer totalTraffic = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tickets", Integer.class);
        int tTraffic = (totalTraffic == null) ? 0 : totalTraffic;
        networkStats.put("totalTraffic", tTraffic);

        // 补充兼容前端的汇总字段（平均航线营收、日均流量、增长占位、网络效率、连通性、新增航线）
        double avgRouteRevenue = (tRoutes > 0) ? (tRevenue / (double) tRoutes) : 0.0;
        networkStats.put("avgRouteRevenue", Math.round(avgRouteRevenue * 100.0) / 100.0);
        // growth placeholders (后续可与历史比较计算)
        networkStats.put("revenueGrowth", 0);
        double dailyTraffic = (double) tTraffic / 30.0;
        networkStats.put("dailyTraffic", Math.round(dailyTraffic));
        networkStats.put("trafficGrowth", 0);
        // network efficiency placeholder：使用简单比例（已存在航班比例）
        networkStats.put("networkEfficiency", 0);
        networkStats.put("efficiencyChange", 0);
        // connectivity placeholder（可由 networkCities 生成更丰富的描述）
        networkStats.put("connectivity", "");
        // 新增航线（近30天内新增的不同航线数）
        Integer newRoutes = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT CONCAT(origin_airport,'-',dest_airport)) FROM flights WHERE DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)",
                Integer.class);
        networkStats.put("newRoutes", (newRoutes == null) ? 0 : newRoutes);

        // Top routes by revenue (GROUP BY origin,dest) — 使用 origin/dest 字段以便后续细化计算
        List<Map<String, Object>> topRoutes = jdbcTemplate.query(
                // 按实际售出金额（tickets.total_price）统计航线营收，避免使用 flights.price 导致统计口径不一致
                "SELECT f.origin_airport as origin, f.dest_airport as dest, CONCAT(f.origin_airport,' → ',f.dest_airport) as route, SUM(t.total_price) as revenue, COUNT(t.id) as orders, COUNT(f.id) as flight_count " +
                        "FROM flights f LEFT JOIN tickets t ON f.id = t.flight_id " +
                        "GROUP BY f.origin_airport, f.dest_airport ORDER BY revenue DESC LIMIT 10",
                (rs, rowNum) -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("origin", rs.getString("origin"));
                    m.put("dest", rs.getString("dest"));
                    m.put("route", rs.getString("route"));
                    m.put("name", rs.getString("route")); // 兼容前端对 name 的访问
                    m.put("revenue", rs.getDouble("revenue"));
                    m.put("orders", rs.getInt("orders"));
                    m.put("flightCount", rs.getInt("flight_count"));
                    return m;
                });

        // 为每条航线补充 avgPrice / occupancy / frequency 等字段（使用单独查询避免 JOIN 倍增问题）
        for (Map<String, Object> r : topRoutes) {
            try {
                String origin = (String) r.getOrDefault("origin", "");
                String dest = (String) r.getOrDefault("dest", "");

                // 平均票价（基于 tickets.total_price）
                Double avgPrice = jdbcTemplate.queryForObject(
                        "SELECT AVG(t.total_price) FROM tickets t JOIN flights f2 ON t.flight_id = f2.id WHERE f2.origin_airport = ? AND f2.dest_airport = ?",
                        new Object[]{origin, dest}, Double.class);
                if (avgPrice == null) avgPrice = 0.0;
                // 航班总座位数（统计 seats 表）
                Integer totalSeats = jdbcTemplate.queryForObject(
                        "SELECT COUNT(s.id) FROM seats s JOIN flights f2 ON s.flight_id = f2.id WHERE f2.origin_airport = ? AND f2.dest_airport = ?",
                        new Object[]{origin, dest}, Integer.class);
                int seatsCount = (totalSeats == null) ? 0 : totalSeats;

                // 近似频次（航班数 / 30 天，若 flightCount 可用，优先使用 flightCount）
                Integer flightCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM flights WHERE origin_airport = ? AND dest_airport = ?",
                        new Object[]{origin, dest}, Integer.class);
                int fCount = (flightCount == null) ? 0 : flightCount;
                double frequencyPerDay = 0.0;
                if (fCount > 0) {
                    frequencyPerDay = ((double) fCount) / 30.0;
                } else {
                    Object fcObj = r.get("flightCount");
                    if (fcObj instanceof Number) {
                        double fc = ((Number) fcObj).doubleValue();
                        frequencyPerDay = fc / 30.0;
                    }
                }

                // 上座率 = orders / totalSeats
                int ordersCount = ((Number) r.getOrDefault("orders", 0)).intValue();
                double occupancy = 0.0;
                if (seatsCount > 0) {
                    occupancy = ((double) ordersCount) * 100.0 / seatsCount;
                }

                // 四舍五入并写回
                double avgPriceRounded = Math.round(avgPrice * 100.0) / 100.0;
                double occupancyRounded = Math.round(occupancy * 10.0) / 10.0;
                double freqRounded = Math.round(frequencyPerDay * 100.0) / 100.0;

                r.put("avgPrice", avgPriceRounded);
                r.put("occupancy", occupancyRounded);
                r.put("frequency", freqRounded);
                // 简单趋势占位：当 orders > avgOrders 标记为 up，否则 stable（可用历史数据进一步优化）
                try {
                    double avgOrders = 0.0;
                    if (tRoutes > 0) avgOrders = (double) ((Number) networkStats.getOrDefault("totalTraffic", 0)).doubleValue() / (double) Math.max(1, tRoutes);
                    int ordersInt = ((Number) r.getOrDefault("orders", 0)).intValue();
                    if (ordersInt > avgOrders * 1.2) {
                        r.put("trend", "up");
                        r.put("trendText", "较高");
                    } else if (ordersInt < avgOrders * 0.8) {
                        r.put("trend", "down");
                        r.put("trendText", "较低");
                    } else {
                        r.put("trend", "stable");
                        r.put("trendText", "持平");
                    }
                } catch (Exception ignore) {
                    r.put("trend", "stable");
                    r.put("trendText", "");
                }
            } catch (Exception e) {
                // 容错：若单条航线的补充计算失败，设置默认值继续处理其他航线
                r.put("avgPrice", 0.0);
                r.put("occupancy", 0.0);
                r.put("frequency", 0.0);
                r.put("trend", "stable");
                r.put("trendText", "");
            }
        }

        // Route revenue series by period (7d/30d/90d) - 按日期聚合 tickets.total_price
        Map<String, List<Map<String, Object>>> routeRevenueByPeriod = new HashMap<>();
        int[] periods = new int[]{7, 30, 90};
        LocalDate today = LocalDate.now();
        for (int p : periods) {
            List<Map<String, Object>> hist = new ArrayList<>();
            for (int d = p - 1; d >= 0; d--) {
                LocalDate day = today.minusDays(d);
                Date sqlDate = Date.valueOf(day);
                Double dayRevenue = jdbcTemplate.queryForObject(
                        "SELECT SUM(t.total_price) FROM tickets t JOIN flights f ON t.flight_id = f.id WHERE DATE(f.sched_dep_time) = ?",
                        new Object[]{sqlDate}, Double.class);
                double val = (dayRevenue == null) ? 0.0 : dayRevenue;
                Map<String, Object> item = new HashMap<>();
                if (p == 7) {
                    // 周短标签：周一..周日
                    String label = day.getDayOfWeek().getValue() == 1 ? "周一" : day.getDayOfWeek().getValue() == 2 ? "周二"
                            : day.getDayOfWeek().getValue() == 3 ? "周三" : day.getDayOfWeek().getValue() == 4 ? "周四"
                            : day.getDayOfWeek().getValue() == 5 ? "周五" : day.getDayOfWeek().getValue() == 6 ? "周六" : "周日";
                    item.put("label", label);
                } else {
                    item.put("label", String.format("%02d-%02d", day.getMonthValue(), day.getDayOfMonth()));
                }
                item.put("value", Math.round(val * 100.0) / 100.0);
                hist.add(item);
            }
            routeRevenueByPeriod.put(p + "d", hist);
        }

        // Simple network cities list
        List<Map<String, Object>> networkCities = jdbcTemplate.query(
                "SELECT origin_airport as city, COUNT(*) as routes FROM flights GROUP BY origin_airport ORDER BY routes DESC LIMIT 20",
                (rs, rowNum) -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getString("city"));
                    m.put("name", rs.getString("city"));
                    m.put("routes", rs.getInt("routes"));
                    return m;
                });

        return new OpNetworkOverviewResponse(networkStats, topRoutes, routeRevenueByPeriod, networkCities, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}


