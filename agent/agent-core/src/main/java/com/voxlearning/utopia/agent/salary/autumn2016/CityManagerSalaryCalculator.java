package com.voxlearning.utopia.agent.salary.autumn2016;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.FormerEmployeeData;
import com.voxlearning.utopia.agent.bean.PerformanceData;
import com.voxlearning.utopia.agent.bean.PerformanceRankingData;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.salary.SalaryCalculator;
import com.voxlearning.utopia.agent.salary.type.SalaryKpiType;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CityManagerSalaryCalculator
 *
 * @author song.wang
 * @date 2016/9/19
 */
@Named
@Slf4j
public class CityManagerSalaryCalculator extends SalaryCalculator {
    private static final Integer TARGET_JUNIOR_SASC = 1; // 小学单活指标
    private static final Integer TARGET_JUNIOR_DASC = 2; // 小学双活指标
    private static final Integer TARGET_MIDDLE_SASC = 3; // 中学单活指标
    private static final double MIDDLE_TARGET_RATE = 0.5; // 中学完成率折扣点
    private static final double SALARY_DISCOUNT = 0.8; // 中学指标未完成当月预算的指定比例，则扣除当月总提成的20%

    // S+类城市提成方案
    private static final double[][] SCHEME_SS_CITY = {
            {0, 0, 0, 0, 0, 0},      // 子数组长度为 6 ， 前三项表示直营模式的各项指标（小学单活， 小学双活， 中学单活），后三项表示代理模式的各项指标（小学单活， 小学双活， 中学单活）
            {0, 2100, 2400, 0, 1800, 2025},
            {3000, 4200, 4800, 2550, 3600, 4050},
            {4500, 6300, 7200, 3825, 5400, 6075}
    };

    // S类城市提成方案
    private static final double[][] SCHEME_S_CITY = {
            {0, 0, 0, 0, 0, 0},      // 子数组长度为 6 ， 前三项表示直营模式的各项指标（小学单活， 小学双活， 中学单活），后三项表示代理模式的各项指标（小学单活， 小学双活， 中学单活）
            {0, 1820, 2080, 0, 1560, 1755},
            {2600, 3640, 4160, 2210, 3120, 3510},
            {3900, 5460, 6240, 3315, 4680, 5265}
    };

    // A类城市提成方案
    private static final double[][] SCHEME_A_CITY = {
            {0, 0, 0, 0, 0, 0},// 子数组长度为 6 ， 前三项表示直营模式的各项指标（小学单活， 小学双活， 中学单活），后三项表示代理模式的各项指标（小学单活， 小学双活， 中学单活）
            {0, 1400, 1600, 0, 1200, 1350},
            {2000, 2800, 3200, 1700, 2400, 2700},
            {3000, 4200, 4800, 2550, 3600, 4050}
    };

    // B类城市提成方案
    private static final double[][] SCHEME_B_CITY = {
            {0, 0, 0, 0, 0, 0},// 子数组长度为 6 ， 前三项表示直营模式的各项指标（小学单活， 小学双活， 中学单活），后三项表示代理模式的各项指标（小学单活， 小学双活， 中学单活）
            {0, 1120, 1280, 0, 960, 1080},
            {1600, 2240, 2560, 1360, 1920, 2160},
            {2400, 3360, 3840, 2040, 2880, 3240}
    };

    private static final double[] RANKING_SALARY = {25000, 10000, 5000};

    @Override
    public void calculate(List<AgentUser> users) {
        if(CollectionUtils.isEmpty(users)){
            return;
        }
        // 计算在职人员工资数据
        for(AgentUser user : users){
            // 删除数据
            agentUserKpiResultSpring2016Persistence.deleteByUserId(user.getId(), context.getSalaryMonth());
            // 计算提成
            calculateCurrentEmployeeSalary(user);
            // 计算绩效
            calculateRankingSalary(user);
        }
    }

    @Override
    public void calculateFormer(List<FormerEmployeeData> formerEmployeeDataList) {
        // 计算离职人员工资数据
        if(CollectionUtils.isNotEmpty(formerEmployeeDataList)){
            for(FormerEmployeeData formerEmployeeData : formerEmployeeDataList){
                // 删除数据
                agentUserKpiResultSpring2016Persistence.deleteByUserId(formerEmployeeData.getUserId(), context.getSalaryMonth());
                // 计算工资
                calculateFormerEmployeeSalary(formerEmployeeData);
            }
        }
    }

    // 计算提成
    public void calculateCurrentEmployeeSalary(AgentUser user){

        Date salaryStartDate = getSalaryStartDate(user);
        Date salaryEndDate = getSalaryEndDate(user);

        int salaryDays = getSalaryDays(user);
        if(salaryDays == 0){
            String note = generateSalaryNote(user, null, null, false, null, null, null, true);
            saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_SASC, 0L, 0L, 0D, note);
            saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_DASC, 0L, 0L, 0D, note);
            saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.MIDDLE_SASC, 0L, 0L, 0D, note);
            return;
        }

        // 获取用户所在的用户级别
        AgentCityLevelType cityLevelType = getUserCityLevel(user.getId());
        boolean isJuniorAgentModel = isAgentModel(user.getId(), SchoolLevel.JUNIOR);
        boolean isMiddleAgentModel = isAgentModel(user.getId(), SchoolLevel.MIDDLE);

        calculateSalary(user, null, cityLevelType, isJuniorAgentModel, isMiddleAgentModel, salaryStartDate, salaryEndDate);
    }

    // 计算提成
    public void calculateFormerEmployeeSalary(FormerEmployeeData formerEmployeeData){
        AgentUser user = baseUserService.getUser(formerEmployeeData.getUserId());
        Date salaryStartDate = getSalaryStartDate(user);
        Date salaryEndDate = getSalaryEndDate(user);
        int salaryDays = getSalaryDays(user);
        if(salaryDays == 0){
            String note = generateSalaryNote(user, null, null, false, null, null, null, true);
            saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_SASC, 0L, 0L, 0D, note);
            saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_DASC, 0L, 0L, 0D, note);
            saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.MIDDLE_SASC, 0L, 0L, 0D, note);
            return;
        }

        AgentCityLevelType cityLevelType = formerEmployeeData.getCityLevelType();
        boolean isJuniorAgentModel = formerEmployeeData.getIsJuniorAgentModel() == null ? false : formerEmployeeData.getIsJuniorAgentModel();
        boolean isMiddleAgentModel = formerEmployeeData.getIsMiddleAgentModel() == null ? false : formerEmployeeData.getIsMiddleAgentModel();
        calculateSalary(user, null, cityLevelType, isJuniorAgentModel, isMiddleAgentModel, salaryStartDate, salaryEndDate);
    }

    private void calculateSalary (AgentUser user, PerformanceData performanceData, AgentCityLevelType cityLevelType, boolean isJuniorAgentModel, boolean isMiddleAgentModel, Date salaryStartDate, Date salaryEndDate){
        // 获取小学单活提成
        double juniorSascSalary = getSalaryDataFromScheme(cityLevelType, 0.00d, TARGET_JUNIOR_SASC, isJuniorAgentModel);
        // 获取小学双活提成
        double juniorDascSalary = getSalaryDataFromScheme(cityLevelType, 0.00d, TARGET_JUNIOR_DASC, isJuniorAgentModel);
        // 获取小学单活提成
        double middleSascSalary = getSalaryDataFromScheme(cityLevelType, 0.00d, TARGET_MIDDLE_SASC, isMiddleAgentModel);


        // 10、11、12月，如中学单活完成率指标未完成当月预算60％，则扣除当月总提成的20%
        boolean isMiddleCompleteTargetRate = true;
        if(context.getRunDay() > 20161001) {
            // 判断用户是否负责中学
            List<Long> middleSchoolList = baseOrgService.getManagedMiddleSchoolList(user.getId());
            if (CollectionUtils.isNotEmpty(middleSchoolList)) {  // 用户负责中学
                if (0.00d < MIDDLE_TARGET_RATE) {
                    juniorSascSalary = MathUtils.doubleMultiply(juniorSascSalary, SALARY_DISCOUNT);
                    juniorDascSalary = MathUtils.doubleMultiply(juniorDascSalary, SALARY_DISCOUNT);
                    middleSascSalary = MathUtils.doubleMultiply(middleSascSalary, SALARY_DISCOUNT);
                    isMiddleCompleteTargetRate = false;
                }
            }
        }

        // 获取用户有效天数比例
        double salaryDayRate = getSalaryDayRate(user);
        juniorSascSalary = MathUtils.doubleMultiply(juniorSascSalary, salaryDayRate);
        juniorDascSalary = MathUtils.doubleMultiply(juniorDascSalary, salaryDayRate);
        middleSascSalary = MathUtils.doubleMultiply(middleSascSalary, salaryDayRate);

        // 小学单活工资数据
        String juniorSascNote = generateSalaryNote(user, cityLevelType, TARGET_JUNIOR_SASC, isJuniorAgentModel, (long)0, 0, 0.00d, isMiddleCompleteTargetRate);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_SASC, (long)0, (long)0, juniorSascSalary, juniorSascNote);

        // 小学双活工资数据
        String juniorDascNote = generateSalaryNote(user, cityLevelType, TARGET_JUNIOR_DASC, isJuniorAgentModel, (long)0, 0, 0.00d, isMiddleCompleteTargetRate);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_DASC, (long)0, (long)0, juniorDascSalary, juniorDascNote);

        // 中学单活工资数据
        String middleSascNote = generateSalaryNote(user, cityLevelType, TARGET_MIDDLE_SASC, isMiddleAgentModel, (long)0, 0, 0.00d, isMiddleCompleteTargetRate);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.MIDDLE_SASC, (long)0, (long)0, middleSascSalary, middleSascNote);
    }

    // 计算绩效
    public void calculateRankingSalary(AgentUser user){
        // 获取当前用户排名
        PerformanceRankingData rankingData = agentPerformanceRankingService.getRankingDataByUserId(user.getId(), 2, context.getRunDay());
        if(rankingData == null){ // 用户没参与排名
            return;
        }
        Integer ranking = rankingData.getRanking();
        double rankingSalary =getRankingSalary(ranking);
        if(rankingSalary > 0){
            // 小学双活工资数据
            Date salaryStartDate = getSalaryStartDate(user);
            Date salaryEndDate = getSalaryEndDate(user);

            String rankingSalaryNote = generateRankingSalaryNote(ranking);
            saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.RANKING_SALARY, 1L, ranking.longValue(), rankingSalary, rankingSalaryNote);
        }

    }

    private double getRankingSalary(int ranking){
        if(ranking == 1 ){
            return RANKING_SALARY[0];
        }else if(ranking > 1 && ranking < 6){
            return RANKING_SALARY[1];
        }else if(ranking > 5 && ranking < 13){
            return RANKING_SALARY[2];
        }
        return 0;
    }

    private double getSalaryDataFromScheme(AgentCityLevelType cityLevelType, double completeRate, Integer target, boolean isAgentModel){
        int row = getRowIndex(cityLevelType, completeRate);
        int column = getColumnIndex(target, isAgentModel);
        if(AgentCityLevelType.CityLevelSS == cityLevelType){
            return SCHEME_SS_CITY[row][column];
        }else if(AgentCityLevelType.CityLevelS == cityLevelType){
            return SCHEME_S_CITY[row][column];
        }else if(AgentCityLevelType.CityLevelA == cityLevelType){
            return SCHEME_A_CITY[row][column];
        }else if(AgentCityLevelType.CityLevelB == cityLevelType){
            return SCHEME_B_CITY[row][column];
        }
        return 0;
    }

    private int getRowIndex(AgentCityLevelType cityLevelType, double completeRate){
        int row = 0;
        if(AgentCityLevelType.CityLevelSS == cityLevelType || AgentCityLevelType.CityLevelS == cityLevelType || AgentCityLevelType.CityLevelA == cityLevelType || AgentCityLevelType.CityLevelB == cityLevelType){
            if(completeRate < 0.5){
                row = 0;
            }else if(completeRate >= 0.5 && completeRate < 0.85){
                row = 1;
            }else if(completeRate >= 0.85 && completeRate < 1.0){
                row = 2;
            }else {
                row = 3;
            }
        }
        return row;
    }

    private int getColumnIndex(Integer target, boolean isAgentModel){
        int colIndex = 0;
        if(TARGET_JUNIOR_SASC == target){
            colIndex = 0;
        }else if(TARGET_JUNIOR_DASC == target){
            colIndex = 1;
        }else if(TARGET_MIDDLE_SASC == target){
            colIndex = 2;
        }
        if(isAgentModel){
            colIndex = colIndex + 3;
        }
        return colIndex;
    }

    private String generateSalaryNote(AgentUser user, AgentCityLevelType cityLevelType,  Integer target, boolean isAgentModel, Long budget, Integer complete, Double completeRate, boolean isMiddleCompletePersent){
        StringBuilder stringBuilder = new StringBuilder();
        int salaryDays = getSalaryDays(user);
        if(salaryDays == 0){
            // 有效工作天数为0
            // 入职时间在本月的
            if(user.getContractStartDate() != null && user.getContractStartDate().after(DayUtils.getFirstDayOfMonth(context.getRunTime()))){
                stringBuilder.append("用户入职时间：").append(DateUtils.dateToString(user.getContractStartDate(), "yyyy-MM-dd"));
            }
            // 离职职时间在本月的
            if(user.getContractEndDate() != null && user.getContractEndDate().after(DayUtils.getFirstDayOfMonth(context.getRunTime())) && user.getContractEndDate().before(DayUtils.getLastDayOfMonth(context.getRunTime()))){
                stringBuilder.append("用户入职时间：").append(DateUtils.dateToString(user.getContractEndDate(), "yyyy-MM-dd"));
            }
            stringBuilder.append(", 本月有效工作天数：").append(salaryDays);
            return stringBuilder.toString();
        }
        stringBuilder.append("城市级别：").append(cityLevelType != null ? cityLevelType.getValue() : "");
        stringBuilder.append(", 模式：").append(isAgentModel? "代理模式" : "直营模式");
        stringBuilder.append(", 完成率：").append(completeRate);
        stringBuilder.append(", 基数：").append(getSalaryDataFromScheme(cityLevelType, completeRate, target, isAgentModel)).append("元");
        stringBuilder.append(", 本月有效工作天数：").append(salaryDays);
        int monthDays = DayUtils.getMonthDays(context.getRunTime());
        stringBuilder.append(", 本月总天数：").append(monthDays);
        stringBuilder.append(", 天数比例：").append(getSalaryDayRate(user));
        if(!isMiddleCompletePersent){
            stringBuilder.append(", 中学指标未完成当月预算的").append(MIDDLE_TARGET_RATE);
            stringBuilder.append(", 中学系数：").append(SALARY_DISCOUNT);
        }
        return stringBuilder.toString();
    }

    private String generateRankingSalaryNote(Integer ranking){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("天玑榜排名第").append(ranking).append("名，进行绩效奖励");
        return stringBuilder.toString();
    }
}
