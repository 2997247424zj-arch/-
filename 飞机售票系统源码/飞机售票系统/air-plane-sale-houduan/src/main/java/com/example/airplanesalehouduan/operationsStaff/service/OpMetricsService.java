package com.example.airplanesalehouduan.operationsStaff.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.example.airplanesalehouduan.operationsStaff.dto.OpMetricsResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 运营指标聚合服务（定时/按需聚合），放在 operationsStaff 包下以便权限分离
 *
 * 实现要点：
 * - 使用 JdbcTemplate 做轻量聚合查询（直接使用 flights 表）
 * - 定时刷新并缓存到内存（满足“自动执行代码”的要求）
 * - 提供按需读取最新数据的能力（避免前端等待定时器首次运行）
 */
@Service
public class OpMetricsService {

    private final JdbcTemplate jdbcTemplate;

    // 内存缓存最新聚合结果
    private volatile Map<String, Object> latestMetrics = new HashMap<>();
    private volatile String lastUpdated = null;
    // SSE 订阅者（线程安全）
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public OpMetricsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 定时刷新指标（默认每 60 秒）
     */
    @Scheduled(fixedRateString = "${metrics.refresh.ms:60000}", initialDelay = 5000)
    public void refreshMetrics() {
        try {
            Map<String, Object> computed = computeMetrics();
            latestMetrics = computed;
            lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            // 捕获异常，避免调度线程终止；记录到控制台（按需可以换成日志）
            System.err.println("OpMetricsService.refreshMetrics error: " + e.getMessage());
        }
    }

    /**
     * 对外获取最新指标（若缓存为空，立即计算）
     */
    public OpMetricsResponse getLatestMetrics() {
        if (latestMetrics == null || latestMetrics.isEmpty()) {
            try {
                Map<String, Object> computed = computeMetrics();
                latestMetrics = computed;
                lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                // 若计算失败（例如数据库表不存在、连接异常等），返回可用的空结构以保证前端正常显示占位数据
                System.err.println("OpMetricsService.getLatestMetrics compute failed: " + e.getMessage());
                Map<String, Object> metricsStats = new HashMap<>();
                Map<String, Object> realtimeStatus = new HashMap<>();
                latestMetrics = new HashMap<>();
                latestMetrics.put("metricsStats", metricsStats);
                latestMetrics.put("realtimeStatus", realtimeStatus);
                lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> ms = (Map<String, Object>) latestMetrics.getOrDefault("metricsStats", new HashMap<>());
        @SuppressWarnings("unchecked")
        Map<String, Object> rs = (Map<String, Object>) latestMetrics.getOrDefault("realtimeStatus", new HashMap<>());
        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> history = (Map<String, List<Map<String, Object>>>) latestMetrics.getOrDefault("onTimeHistory", new HashMap<>());
        return new OpMetricsResponse(ms, rs, history, lastUpdated);
    }

    /**
     * 为 SSE 客户端注册订阅器。调用方应在需要时关闭 emitter（例如客户端断开）。
     */
    public SseEmitter registerEmitter(SseEmitter emitter) {
        if (emitter == null) return null;
        emitters.add(emitter);
        // 当连接完成或发生错误时，从集合中移除
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));
        // 立刻推送当前最新数据（非阻塞）
        try {
            emitter.send(latestMetrics == null ? computeMetrics() : latestMetrics);
        } catch (Exception ignore) {}
        return emitter;
    }

    /**
     * 计算指标（直接对 flights 表做聚合）
     *
     * 说明：为保证最小侵入性与兼容性，此处使用简单的统计指标作为占位且基于 flights.status。
     * 可以在后续迭代中使用更多表（tickets、seats 等）完善计算。
     */
    private Map<String, Object> computeMetrics() {
        try {
            Map<String, Object> metricsStats = new HashMap<>();
            Map<String, Object> realtimeStatus = new HashMap<>();

            // 使用所有数据进行统计（全量聚合）
            Integer allTotal = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM flights", Integer.class);
            Integer allOn = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM flights WHERE status = 'departed'", Integer.class);
            Integer allDelayed = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM flights WHERE status = 'delayed'", Integer.class);
            Integer allCancelled = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM flights WHERE status = 'cancelled'", Integer.class);

            int tTotal = (allTotal == null) ? 0 : allTotal;
            int tOn = (allOn == null) ? 0 : allOn;
            int tDelayed = (allDelayed == null) ? 0 : allDelayed;
            int tCancelled = (allCancelled == null) ? 0 : allCancelled;

            // 重新定义准点：使用未延误且未取消的航班作为准点近似（因为没有实际起飞时间）
            int tOnComputed = Math.max(0, tTotal - tDelayed - tCancelled);
            double onTimeRate = tTotal == 0 ? 0.0 : (double) tOnComputed * 100.0 / tTotal;
            double delayRate = tTotal == 0 ? 0.0 : (double) tDelayed * 100.0 / tTotal;
            double cancelRate = tTotal == 0 ? 0.0 : (double) tCancelled * 100.0 / tTotal;

            // 全量统计时不计算“昨日”对比（设置为 0），前端展示的变化值保留为 0
            double yOnTimeRate = 0.0;
            double yDelayRate = 0.0;
            double yEfficiency = 0.0;

            // 近似效率（placeholder）：以 100 - 延误率 作为简单近似
            double efficiency = Math.max(0.0, 100.0 - delayRate);

            metricsStats.put("onTimeRate", round(onTimeRate, 1));
            metricsStats.put("onTimeChange", round(onTimeRate - yOnTimeRate, 1));
            metricsStats.put("todayOnTime", tOnComputed);
            metricsStats.put("todayTotal", tTotal);
            metricsStats.put("delayRate", round(delayRate, 1));
            metricsStats.put("delayChange", round(delayRate - yDelayRate, 1));
            metricsStats.put("avgDelayTime", 0);
            metricsStats.put("cancelRate", round(cancelRate, 1));
            metricsStats.put("cancelChange", 0);
            metricsStats.put("todayCancel", tCancelled);
            metricsStats.put("efficiency", round(efficiency, 1));
            metricsStats.put("efficiencyChange", round(efficiency - yEfficiency, 1));
            metricsStats.put("utilization", 0); // 占位

            // 实时状态：使用 flights.status 的当前分布
            Integer departed = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flights WHERE status = 'departed'", Integer.class);
            Integer boarding = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flights WHERE status = 'boarding'", Integer.class);
            // 不需要单独统计 scheduled，因为前端当前无对应展示字段，避免未使用局部变量告警
            Integer arriving = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flights WHERE status = 'arrived'", Integer.class);
            Integer delayed = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flights WHERE status = 'delayed'", Integer.class);

            realtimeStatus.put("inFlight", (departed == null) ? 0 : departed);
            realtimeStatus.put("inFlightChange", 0);
            realtimeStatus.put("departing", (boarding == null) ? 0 : boarding);
            realtimeStatus.put("departingSoon", 0);
            realtimeStatus.put("arriving", (arriving == null) ? 0 : arriving);
            realtimeStatus.put("arrivingSoon", 0);
            realtimeStatus.put("delayed", (delayed == null) ? 0 : delayed);
            realtimeStatus.put("avgDelay", 0);
            // 计算利用率（已发/已到航班数占全部航班数的近似）
            Integer utilizedCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flights WHERE status IN ('departed','arrived')", Integer.class);
            int uCount = (utilizedCount == null) ? 0 : utilizedCount;
            double utilization = tTotal == 0 ? 0.0 : (double) uCount * 100.0 / tTotal;
            metricsStats.put("utilization", round(utilization, 1));

            // 生成最近 7/30/90 天的准点率历史（按 sched_dep_time 日期聚合）
            Map<String, List<Map<String, Object>>> historyByPeriod = new HashMap<>();
            int[] periods = new int[]{7, 30, 90};
            LocalDate today = LocalDate.now();
            for (int p : periods) {
                List<Map<String, Object>> hist = new ArrayList<>();
                for (int d = p - 1; d >= 0; d--) {
                    LocalDate day = today.minusDays(d);
                    Date sqlDate = Date.valueOf(day);
                    Integer dayTotal = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM flights WHERE DATE(sched_dep_time) = ?", new Object[]{sqlDate}, Integer.class);
                    Integer dayDelayed = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM flights WHERE DATE(sched_dep_time) = ? AND status = 'delayed'", new Object[]{sqlDate}, Integer.class);
                    Integer dayCancelled = jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM flights WHERE DATE(sched_dep_time) = ? AND status = 'cancelled'", new Object[]{sqlDate}, Integer.class);
                    int dt = (dayTotal == null) ? 0 : dayTotal;
                    int dd = (dayDelayed == null) ? 0 : dayDelayed;
                    int dc = (dayCancelled == null) ? 0 : dayCancelled;
                    int onComputed = Math.max(0, dt - dd - dc);
                    double dayOnRate = dt == 0 ? 0.0 : (double) onComputed * 100.0 / dt;
                    Map<String, Object> item = new HashMap<>();
                    if (p == 7) {
                        // 使用中文周名以便前端直接显示短标签
                        String label = day.getDayOfWeek().getValue() == 1 ? "周一" : day.getDayOfWeek().getValue() == 2 ? "周二"
                                : day.getDayOfWeek().getValue() == 3 ? "周三" : day.getDayOfWeek().getValue() == 4 ? "周四"
                                : day.getDayOfWeek().getValue() == 5 ? "周五" : day.getDayOfWeek().getValue() == 6 ? "周六" : "周日";
                        item.put("label", label);
                    } else {
                        // 30/90 使用 MM-dd 作为标签
                        item.put("label", String.format("%02d-%02d", day.getMonthValue(), day.getDayOfMonth()));
                    }
                    item.put("value", round(dayOnRate, 1));
                    hist.add(item);
                }
                historyByPeriod.put(p + "d", hist);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("metricsStats", metricsStats);
            result.put("realtimeStatus", realtimeStatus);
            result.put("onTimeHistory", historyByPeriod);
            // 计算完成后尝试通知所有 SSE 客户端（弱通知，连接断开时清理）
            notifyEmitters(result);
            return result;
        } catch (Exception e) {
            // 若聚合失败（例如 flights 表不存在或 JDBC 问题），返回空占位结构，前端会使用默认展示值
            System.err.println("OpMetricsService.computeMetrics failed: " + e.getMessage());
            Map<String, Object> metricsStats = new HashMap<>();
            metricsStats.put("onTimeRate", 0.0);
            metricsStats.put("onTimeChange", 0.0);
            metricsStats.put("todayOnTime", 0);
            metricsStats.put("todayTotal", 0);
            metricsStats.put("delayRate", 0.0);
            metricsStats.put("delayChange", 0.0);
            metricsStats.put("avgDelayTime", 0);
            metricsStats.put("cancelRate", 0.0);
            metricsStats.put("cancelChange", 0.0);
            metricsStats.put("todayCancel", 0);
            metricsStats.put("efficiency", 100.0);
            metricsStats.put("efficiencyChange", 0.0);
            metricsStats.put("utilization", 0.0);

            Map<String, Object> realtimeStatus = new HashMap<>();
            realtimeStatus.put("inFlight", 0);
            realtimeStatus.put("inFlightChange", 0);
            realtimeStatus.put("departing", 0);
            realtimeStatus.put("departingSoon", 0);
            realtimeStatus.put("arriving", 0);
            realtimeStatus.put("arrivingSoon", 0);
            realtimeStatus.put("delayed", 0);
            realtimeStatus.put("avgDelay", 0);

            Map<String, Object> result = new HashMap<>();
            result.put("metricsStats", metricsStats);
            result.put("realtimeStatus", realtimeStatus);
            notifyEmitters(result);
            return result;
        }
    }

    private void notifyEmitters(Map<String, Object> payload) {
        if (payload == null) return;
        Iterator<SseEmitter> it = emitters.iterator();
        while (it.hasNext()) {
            SseEmitter emitter = it.next();
            try {
                emitter.send(payload);
            } catch (Exception e) {
                // 发送失败或连接断开，移除
                emitters.remove(emitter);
                try { emitter.complete(); } catch (Exception ignore) {}
            }
        }
    }

    private double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}


