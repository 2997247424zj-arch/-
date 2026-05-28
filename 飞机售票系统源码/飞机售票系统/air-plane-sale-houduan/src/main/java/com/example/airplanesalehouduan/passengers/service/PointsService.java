package com.example.airplanesalehouduan.passengers.service;




import com.example.airplanesalehouduan.passengers.entity.LoyaltyPoints;
import com.example.airplanesalehouduan.passengers.other.LoyaltyPointsRepository;
import jakarta.persistence.PrePersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分服务类
 */
@Service
public class PointsService {

    @Autowired
    private LoyaltyPointsRepository loyaltyPointsRepository;

    /**
     * 获取用户积分信息
     */
    public Map<String, Object> getPointsInfo(Integer passengerId) {
        Map<String, Object> result = new HashMap<>();

        // 计算总积分
        Integer totalPoints = loyaltyPointsRepository.calculateTotalPoints(passengerId);
        if (totalPoints == null) {
            totalPoints = 0;
        }

        // 计算本月获得的积分
        Integer monthlyPoints = loyaltyPointsRepository.calculateMonthlyPoints(passengerId);
        if (monthlyPoints == null) {
            monthlyPoints = 0;
        }

        // 计算待确认的积分
        Integer pendingPoints = loyaltyPointsRepository.calculatePendingPoints(passengerId);
        if (pendingPoints == null) {
            pendingPoints = 0;
        }

        // 计算会员等级（根据总积分）
        String memberLevel = calculateMemberLevel(totalPoints);

        // 计算升级进度
        Map<String, Object> upgradeInfo = calculateUpgradeProgress(totalPoints);

        result.put("currentPoints", totalPoints);
        result.put("monthlyPoints", monthlyPoints);
        result.put("pendingPoints", pendingPoints);
        result.put("memberLevel", memberLevel);
        result.put("upgradeProgress", upgradeInfo.get("progress"));
        result.put("pointsToNextLevel", upgradeInfo.get("pointsToNextLevel"));
        result.put("nextLevel", upgradeInfo.get("nextLevel"));

        return result;
    }

    /**
     * 获取积分记录（分页）
     * 使用原生SQL查询，直接获取数据库中的created_at字符串，避免时区转换
     */
    public Map<String, Object> getPointsHistory(Integer passengerId, Integer page, Integer size) {
        int pageNum = page != null && page > 0 ? page - 1 : 0;
        int pageSize = size != null && size > 0 ? size : 10;
        long offset = (long) pageNum * pageSize;

        // 获取总数
        Long total = loyaltyPointsRepository.countByPassengerId(passengerId);

        // 获取分页数据（包含原始created_at字符串）
        List<Map<String, Object>> records = loyaltyPointsRepository.findPointsHistoryWithRawDate(
                passengerId, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", pageNum + 1);
        result.put("size", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));

        return result;
    }

    /**
     * 积分兑换
     * 创建积分记录时，created_at 会自动使用本地计算机时间（通过 @PrePersist 方法）
     */
    @Transactional
    public LoyaltyPoints exchangePoints(Integer passengerId, String exchangeType,
                                        Integer requiredPoints, String itemName) {
        // 检查积分是否充足
        Integer totalPoints = loyaltyPointsRepository.calculateTotalPoints(passengerId);
        if (totalPoints == null || totalPoints < requiredPoints) {
            throw new RuntimeException("积分不足，无法兑换");
        }

        // 创建积分消费记录
        LoyaltyPoints pointsRecord = new LoyaltyPoints();
        pointsRecord.setPassengerId(passengerId);
        pointsRecord.setPoints(requiredPoints);
        pointsRecord.setChangeType("SPEND");
        pointsRecord.setRemark("积分兑换：" + itemName);


        return loyaltyPointsRepository.save(pointsRecord);
    }

    /**
     * 添加积分（购票获得、推荐奖励等）
     * 创建积分记录时，created_at 会自动使用本地计算机时间（通过 @PrePersist 方法）
     */
    @Transactional
    public LoyaltyPoints addPoints(Integer passengerId, Integer points, String changeType,
                                   Long refOrderId, String remark) {
        LoyaltyPoints pointsRecord = new LoyaltyPoints();
        pointsRecord.setPassengerId(passengerId);
        pointsRecord.setPoints(points);
        pointsRecord.setChangeType(changeType);
        pointsRecord.setRefOrderId(refOrderId);
        pointsRecord.setRemark(remark);
        // 注意：createdAt 不需要手动设置
        // 当调用 save() 时，@PrePersist 方法会自动执行，使用 LocalDateTime.now() 设置本地计算机时间
        // LocalDateTimeAttributeConverter 会确保保存到数据库时不进行时区转换

        return loyaltyPointsRepository.save(pointsRecord);
    }

    /**
     * 计算会员等级
     */
    private String calculateMemberLevel(Integer totalPoints) {
        if (totalPoints >= 10000) {
            return "钻石会员";
        } else if (totalPoints >= 5000) {
            return "铂金会员";
        } else if (totalPoints >= 2000) {
            return "黄金会员";
        } else if (totalPoints >= 1000) {
            return "白银会员";
        } else {
            return "普通会员";
        }
    }

    /**
     * 计算升级进度
     */
    private Map<String, Object> calculateUpgradeProgress(Integer totalPoints) {
        Map<String, Object> result = new HashMap<>();

        int currentLevelThreshold = 0;
        int nextLevelThreshold = 0;
        String nextLevel = "";

        if (totalPoints < 1000) {
            currentLevelThreshold = 0;
            nextLevelThreshold = 1000;
            nextLevel = "白银会员";
        } else if (totalPoints < 2000) {
            currentLevelThreshold = 1000;
            nextLevelThreshold = 2000;
            nextLevel = "黄金会员";
        } else if (totalPoints < 5000) {
            currentLevelThreshold = 2000;
            nextLevelThreshold = 5000;
            nextLevel = "铂金会员";
        } else if (totalPoints < 10000) {
            currentLevelThreshold = 5000;
            nextLevelThreshold = 10000;
            nextLevel = "钻石会员";
        } else {
            // 已经是最高等级
            result.put("progress", 100);
            result.put("pointsToNextLevel", 0);
            result.put("nextLevel", "钻石会员");
            return result;
        }

        int pointsToNextLevel = nextLevelThreshold - totalPoints;
        int levelRange = nextLevelThreshold - currentLevelThreshold;
        int progress = (int) ((double) (totalPoints - currentLevelThreshold) / levelRange * 100);

        result.put("progress", progress);
        result.put("pointsToNextLevel", pointsToNextLevel);
        result.put("nextLevel", nextLevel);

        return result;
    }
}

