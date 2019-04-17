package com.voxlearning.utopia.service.business.impl.activity.entity;

import com.voxlearning.utopia.entity.crm.ActivityConfig;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ReportContext {

    public ReportContext() {
    }

    public ReportContext(ActivityConfig activityConfig) {
        this.activity = activityConfig;
    }

    private boolean writeDatabase = true;

    ActivityConfig activity;
    Integer maxScore;                       // 最高分
    Integer minScore;                       // 最低分
    Long scoreSum = 0L;                     // 分数总合
    Long maxScoreSum = 0L;                  // 最高分总合

    Long takeTimeSum = 0L;                  // 耗时总和
    Long takeTimeCount = 0L;                // 耗时数

    Set<Long> user = new HashSet<>();       // 参与人

    public void addUser(Long userId) {
        user.add(userId);
    }

    public void addScore(Integer score) {
        if (maxScore == null || score > maxScore) maxScore = score;
        if (minScore == null || score < minScore) minScore = score;
        scoreSum += score;
    }

    public void addMaxScore(Integer maxScore) {
        maxScoreSum += maxScore;
    }

    public void addTakeTime(Long takeTime) {
        takeTimeSum += takeTime;
        ++takeTimeCount;
    }

    public Integer getUserCount() {
        return user.size();
    }

    public Double getAvgScore() {
        if (user.isEmpty()) {
            return 0d;
        }
        Double average = new Double(maxScoreSum) / user.size();
        DecimalFormat df = new DecimalFormat("0.00");
        return Double.valueOf(df.format(average));
    }

    public Integer getAvgTime() {
        if (takeTimeCount == 0) return 0;
        Long avgTime = takeTimeSum / takeTimeCount;
        return BigDecimal.valueOf(avgTime).divide(BigDecimal.valueOf(60), 0, BigDecimal.ROUND_HALF_UP).intValue();
    }
}
