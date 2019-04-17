package com.voxlearning.utopia.agent.salary.autumn2016;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.FormerEmployeeData;
import com.voxlearning.utopia.agent.bean.PerformanceData;
import com.voxlearning.utopia.agent.salary.SalaryCalculator;
import com.voxlearning.utopia.agent.salary.type.SalaryKpiType;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.entity.agent.AgentPerformanceConfig;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * RegionManagerSalaryCalculator
 *
 * @author song.wang
 * @date 2016/9/19
 */
@Named
@Slf4j
public class RegionManagerSalaryCalculator extends SalaryCalculator {

    private static final double TARGET_FIRST_RATIO = 0.7;  // 指标 1 占比
    private static final double TARGET_SECOND_RATIO = 0.3;  // 指标 2 占比
    private static final double TARGET_THIRD_RATIO = 0;  // 指标 3 占比

    private static final double[] RANKING_SALARY = {45000, 35000, 30000, 20000};

    @Override
    public void calculate(List<AgentUser> users) {
//        if(CollectionUtils.isEmpty(users)){
//            return;
//        }
//
//        List<Properties> userScoreList = users.stream().map(p -> {
//            Properties userScore = new Properties();
//            userScore.put("user", p);
//            userScore.put("score", this.calculateScore(p));
//            return userScore;
//        }).collect(Collectors.toList());
//
//        Collections.sort(userScoreList, (o1, o2) -> ((Double)o2.get("score")).compareTo((Double)o1.get("score")));
//        List<AgentUser> userList = userScoreList.stream().map(p -> (AgentUser)p.get("user")).collect(Collectors.toList());
//        for(AgentUser user : userList){
//            int ranking = userList.indexOf(user) + 1;
//            calculateSalary(user, ranking);
//        }
    }

    @Override
    public void calculateFormer(List<FormerEmployeeData> formerEmployeeDataList) {

    }


    private double calculateScore(AgentUser user, PerformanceData performanceData){
        double score1 = calculatePerformanceScore(performanceData);
        AgentPerformanceConfig performanceConfig = agentPerformanceConfigService.findByUserIdAndMonth(user.getId(), context.getSalaryMonth());
        double score2 = performanceConfig == null ? 0d : ConversionUtils.toDouble(performanceConfig.getIndicator1(), 0d);
        double score3 = performanceConfig == null ? 0d : ConversionUtils.toDouble(performanceConfig.getIndicator2(), 0d);
        return MathUtils.doubleAdd(MathUtils.doubleMultiply(score1, TARGET_FIRST_RATIO, 6), MathUtils.doubleMultiply(score2, TARGET_SECOND_RATIO, 6), MathUtils.doubleMultiply(score3, TARGET_THIRD_RATIO, 6));
    }

    private double calculatePerformanceScore(PerformanceData performanceData){
        return MathUtils.doubleAdd(MathUtils.doubleMultiply(0.00d, 0.25, 5), MathUtils.doubleMultiply(0.00d, 0.35, 5), MathUtils.doubleMultiply(0.00d, 0.4, 5));
    }

    private void calculateSalary(AgentUser user, int ranking){
        // 删除数据
        agentUserKpiResultSpring2016Persistence.deleteByUserId(user.getId(), context.getSalaryMonth());

        Date salaryStartDate = DayUtils.getFirstDayOfMonth(context.getRunTime());
        Date salaryEndDate = DayUtils.getLastDayOfMonth(context.getRunTime());
        double rankingSalary = getSalaryByRanking(ranking);
        String salaryNote = generateSalaryNote(user, ranking);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.RANKING_SALARY, 1L, (long)ranking, rankingSalary, salaryNote);
    }

    private double getSalaryByRanking(int ranking){
        if(ranking > 0 && ranking < 4){
            return RANKING_SALARY[0];
        }else if(ranking > 3 && ranking < 7){
            return RANKING_SALARY[1];
        }else if(ranking > 6 && ranking < 10){
            return RANKING_SALARY[2];
        }else if(ranking > 9 && ranking < 14){
            return RANKING_SALARY[3];
        }
        return 0;
    }

    private String generateSalaryNote(AgentUser user, int ranking){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("小学单活目标：").append(0);
        stringBuilder.append(", 小学单活完成：").append(0);
        stringBuilder.append(", 小学单活完成率：").append(0.000d);
        stringBuilder.append(", 小学单活系数：").append(0.25);
        stringBuilder.append(", 小学双活目标：").append(0);
        stringBuilder.append(", 小学双活完成：").append(0);
        stringBuilder.append(", 小学双活完成率：").append(0.000d);
        stringBuilder.append(", 小学双活系数：").append(0.35);
        stringBuilder.append(", 中学单活目标：").append(0);
        stringBuilder.append(", 中学单活完成：").append(0);
        stringBuilder.append(", 中学单活完成率：").append(0.00d);
        stringBuilder.append(", 中学单活系数：").append(0.4);

        stringBuilder.append(", 绩效指标得分：").append(calculatePerformanceScore(null));
        stringBuilder.append(", 绩效指标系数：").append(TARGET_FIRST_RATIO);

        AgentPerformanceConfig performanceConfig = agentPerformanceConfigService.findByUserIdAndMonth(user.getId(), context.getSalaryMonth());
        String targetName2 = ", 指标2";
        String targetName3 = ", 指标3";
        if(performanceConfig != null ){
            if(StringUtils.isNotBlank(performanceConfig.getIndicator1Name())){
                targetName2 = performanceConfig.getIndicator1Name();
            }
            if(StringUtils.isNotBlank(performanceConfig.getIndicator2Name())){
                targetName3 = performanceConfig.getIndicator2Name();
            }
        }
        stringBuilder.append(", ").append(targetName2).append(": ").append(performanceConfig == null ? 0d :ConversionUtils.toDouble(performanceConfig.getIndicator1(), 0d));
        stringBuilder.append(", ").append(targetName2).append("系数：").append(TARGET_SECOND_RATIO);

        stringBuilder.append(", ").append(targetName3).append(": ").append(performanceConfig == null ? 0d :ConversionUtils.toDouble(performanceConfig.getIndicator2(), 0d));
        stringBuilder.append(", ").append(targetName3).append("系数：").append(TARGET_THIRD_RATIO);

        stringBuilder.append(", 综合得分：").append(calculateScore(user, null));
        stringBuilder.append(", 综合排名：").append(ranking);

        return stringBuilder.toString();
    }


}
