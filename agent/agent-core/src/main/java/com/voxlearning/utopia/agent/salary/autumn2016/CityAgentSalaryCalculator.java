package com.voxlearning.utopia.agent.salary.autumn2016;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.FormerEmployeeData;
import com.voxlearning.utopia.agent.bean.PerformanceData;
import com.voxlearning.utopia.agent.salary.SalaryCalculator;
import com.voxlearning.utopia.agent.salary.type.SalaryKpiType;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.entity.agent.AgentPerformanceConfig;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CityAgentSalaryCalculator
 *
 * @author song.wang
 * @date 2016/9/19
 */
@Named
@Slf4j
public class CityAgentSalaryCalculator extends SalaryCalculator {

    private static final int JUNIOR_CITY_MEETING_SUPPORT_COUNT = 1; // 小学市级专场组会，每学期支持 1 场的费用；
    private static final int JUNIOR_COUNTY_MEETING_SUPPORT_COUNT = 3;// 小学区级专场组会，每个月支持不超过 3 场的费用；
    private static final int JUNIOR_INTERRUPTED_MEETING_SUPPORT_COUNT = 5;// 小学插播组会，每个月支持不超过 5 场的费用；

    private static final int MIDDLE_CITY_MEETING_SUPPORT_COUNT = 1; // 中学市级专场组会，每学期支持 1 场的费用；
    private static final int MIDDLE_COUNTY_MEETING_SUPPORT_COUNT = 3;// 中学区级专场组会，每个月支持不超过 3 场的费用；
    private static final int MIDDLE_INTERRUPTED_MEETING_SUPPORT_COUNT = 5;// 中学插播组会，每个月支持不超过 5 场的费用；

    private static final int JUNIOR_CLUE_TARGET = 15; // 小学线索提供目标
    private static final int MIDDLE_CLUE_TARGET = 15; // 小学线索提供目标

    private static final Integer[] MONTH_LIST = {201609, 201610, 201611, 201612, 201701, 201702};

    private static final double[] SCHEME_JUNIOR_MEETING = {8000, 2500, 1000};
    private static final double[] SCHEME_MIDDLE_MEETING = {10000, 3000, 1200};

    private static final double[][] SCHEME_JUNIOR_CLUE = {
            {0, 10000, 12000},      // 子数组长度为 3 ， 分别表示 ： 完成85%以下， 完成85以上（含85%）100%以下 ， 完成100%（含100%）以上 的支持费用
            {0, 12000, 20000},
            {0, 15000, 24000},
            {0, 17000, 28000},
            {0, 20000, 35000},
            {0, 30000, 50000}
    };

    private static final double[][] SCHEME_MIDDLE_CLUE = {
            {0, 4000, 8000, 20000},      // 子数组长度为 4 ， 分别表示 ： 完成50%以下， 完成50以上（含50%）85%以下 ， 完成85以上（含85%）100%以下 ，完成100%（含100%）以上 的支持费用
            {0, 7000, 14000, 30000},
            {0, 10000, 20000, 40000},
            {0, 13000, 26000, 50000}
    };

    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Override
    public void calculate(List<AgentUser> users) {
        if (CollectionUtils.isEmpty(users)) {
            return;
        }
        for (AgentUser user : users) {
            // 删除数据
            agentUserKpiResultSpring2016Persistence.deleteByUserId(user.getId(), context.getSalaryMonth());
            AgentPerformanceConfig performanceConfig = agentPerformanceConfigService.findByUserIdAndMonth(user.getId(), context.getSalaryMonth());
            // 计算费用
            List<Long> managedJuniorSchools = baseOrgService.getManagedJuniorSchoolList(user.getId());
            List<Long> managedMiddleSchools = baseOrgService.getManagedMiddleSchoolList(user.getId());
            calculateSalary(user, performanceConfig, managedJuniorSchools, managedMiddleSchools, false);
        }
    }

    @Override
    public void calculateFormer(List<FormerEmployeeData> formerEmployeeDataList) {
        // 计算离职人员工资数据
        if (CollectionUtils.isNotEmpty(formerEmployeeDataList)) {
            for (FormerEmployeeData formerEmployeeData : formerEmployeeDataList) {
                // 删除数据
                agentUserKpiResultSpring2016Persistence.deleteByUserId(formerEmployeeData.getUserId(), context.getSalaryMonth());

                AgentUser user = baseUserService.getUser(formerEmployeeData.getUserId());
                // 获取绩效配置信息
                AgentPerformanceConfig performanceConfig = agentPerformanceConfigService.findByUserIdAndMonth(formerEmployeeData.getUserId(), context.getSalaryMonth());

                // 过滤出中小学
                List<Long> managedJuniorSchools = baseOrgService.getSchoolListByLevel(formerEmployeeData.getSchoolIdList(), SchoolLevel.JUNIOR);
                List<Long> managedMiddleSchools = baseOrgService.getSchoolListByLevel(formerEmployeeData.getSchoolIdList(), SchoolLevel.MIDDLE);
                calculateSalary(user, performanceConfig, managedJuniorSchools, managedMiddleSchools, true);
            }
        }

    }


    private void calculateSalary(AgentUser user, AgentPerformanceConfig performanceConfig, List<Long> managedJuniorSchools, List<Long> managedMiddleSchools, boolean isFormer) {

        // 用户负责小学
        if (CollectionUtils.isNotEmpty(managedJuniorSchools)) {
            // 组会支持
            calculateJuniorMeetingSalary(user, performanceConfig);
            // 线索支持
            calculateJuniorClueSalary(user, performanceConfig, managedJuniorSchools, isFormer);
        }

        // 用户负责中学
        if (CollectionUtils.isNotEmpty(managedMiddleSchools)) {
            // 组会支持
            calculateMiddleMeetingSalary(user, performanceConfig);
            // 线索支持
            calculateMiddleClueSalary(user, performanceConfig, managedMiddleSchools, isFormer);
        }
    }

    // 市级专场组会，每学期支持 1 场的费用；
    // 区级专场组会，每个月支持不超过 3 场的费用；
    // 插播组会要求为区级及区级以上组会，每个月支持不超过 5 场的费用；
    public void calculateJuniorMeetingSalary(AgentUser user, AgentPerformanceConfig performanceConfig) {
        Date salaryStartDate = getSalaryStartDate(user);
        Date salaryEndDate = getSalaryEndDate(user);

        int cityMeetingCount = performanceConfig == null ? 0 : ConversionUtils.toInt(performanceConfig.getCityJuniorMeet()); // 获取市级专场组会数
        int countyMeetingCount = performanceConfig == null ? 0 : ConversionUtils.toInt(performanceConfig.getCountyJuniorMeet());// 获取区级专场组会数
        int interruptedMeetingCount = performanceConfig == null ? 0 : ConversionUtils.toInt(performanceConfig.getInterCutJuniorMeet());// 获取插播组会数

        // 计算市级专场组会支持
        int supportCityMeetingCount = getSupportMeetingCount(SalaryKpiType.JUNIOR_MEETING_CITY, cityMeetingCount);
        // 每学期支持 1 场的费用, 判断之前是否已发放过市级专场组会支持
        if (supportCityMeetingCount > 0) {
            // 获取之前的配置数据
            List<Integer> allMonthList = Arrays.asList(MONTH_LIST);
            List<Integer> monthList = allMonthList.stream().filter(p -> p < context.getSalaryMonth()).collect(Collectors.toList());
            List<AgentPerformanceConfig> prePerformanceConfigList = agentPerformanceConfigService.findByMonthList(user.getId(), monthList);
            if (CollectionUtils.isNotEmpty(prePerformanceConfigList)) {
                // 本学期已经支持过
                long count = prePerformanceConfigList.stream().filter(p -> p.getCityJuniorMeet() != null && p.getCityJuniorMeet() > 0).count();
                if (count > 0) {
                    supportCityMeetingCount = 0;
                }
            }
        }
        double cityMeetingSalary = MathUtils.doubleMultiply(getPerMeetingSalary(SalaryKpiType.JUNIOR_MEETING_CITY), supportCityMeetingCount);
        String cityMeetingNote = generateMeetingSalaryNote(SchoolLevel.JUNIOR, SalaryKpiType.JUNIOR_MEETING_CITY, cityMeetingCount, supportCityMeetingCount);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_MEETING_CITY, (long) JUNIOR_CITY_MEETING_SUPPORT_COUNT, (long) cityMeetingCount, cityMeetingSalary, cityMeetingNote);

        // 计算区级专场组会支持
        int supportCountyMeetingCount = getSupportMeetingCount(SalaryKpiType.JUNIOR_MEETING_COUNTY, countyMeetingCount);
        double countyMeetingSalary = MathUtils.doubleMultiply(getPerMeetingSalary(SalaryKpiType.JUNIOR_MEETING_COUNTY), supportCountyMeetingCount);
        String countyMeetingNote = generateMeetingSalaryNote(SchoolLevel.JUNIOR, SalaryKpiType.JUNIOR_MEETING_COUNTY, countyMeetingCount, supportCountyMeetingCount);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_MEETING_COUNTY, (long) JUNIOR_COUNTY_MEETING_SUPPORT_COUNT, (long) countyMeetingCount, countyMeetingSalary, countyMeetingNote);

        // 计算插播组会支持
        int supportInterruptedMeetingCount = getSupportMeetingCount(SalaryKpiType.JUNIOR_MEETING_INTERRUPTED, interruptedMeetingCount);
        double interruptedMeetingSalary = MathUtils.doubleMultiply(getPerMeetingSalary(SalaryKpiType.JUNIOR_MEETING_INTERRUPTED), supportInterruptedMeetingCount);
        String interruptedMeetingNote = generateMeetingSalaryNote(SchoolLevel.JUNIOR, SalaryKpiType.JUNIOR_MEETING_INTERRUPTED, interruptedMeetingCount, supportInterruptedMeetingCount);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_MEETING_INTERRUPTED, (long) JUNIOR_INTERRUPTED_MEETING_SUPPORT_COUNT, (long) interruptedMeetingCount, interruptedMeetingSalary, interruptedMeetingNote);
    }

    // 市级专场组会，每学期支持 1 场的费用；
    // 区级专场组会，每个月支持不超过 3 场的费用；
    // 插播组会要求为区级及区级以上组会，每个月支持不超过 5 场的费用；
    public void calculateMiddleMeetingSalary(AgentUser user, AgentPerformanceConfig performanceConfig) {
        Date salaryStartDate = getSalaryStartDate(user);
        Date salaryEndDate = getSalaryEndDate(user);

        int cityMeetingCount = performanceConfig == null ? 0 : ConversionUtils.toInt(performanceConfig.getCityMiddleMeet()); // 获取市级专场组会数
        int countyMeetingCount = performanceConfig == null ? 0 : ConversionUtils.toInt(performanceConfig.getCountyMiddleMeet());// 获取区级专场组会数
        int interruptedMeetingCount = performanceConfig == null ? 0 : ConversionUtils.toInt(performanceConfig.getInterCutMiddleMeet());// 获取插播组会数

        // 计算市级专场组会支持
        int supportCityMeetingCount = getSupportMeetingCount(SalaryKpiType.MIDDLE_MEETING_CITY, cityMeetingCount);
        // 每学期支持 1 场的费用, 判断之前是否已发放过市级专场组会支持
        if (supportCityMeetingCount > 0) {
            // 获取之前的配置数据
            List<Integer> allMonthList = Arrays.asList(MONTH_LIST);
            List<Integer> monthList = allMonthList.stream().filter(p -> p < context.getSalaryMonth()).collect(Collectors.toList());
            List<AgentPerformanceConfig> prePerformanceConfigList = agentPerformanceConfigService.findByMonthList(user.getId(), monthList);
            if (CollectionUtils.isNotEmpty(prePerformanceConfigList)) {
                // 本学期已经支持过
                long count = prePerformanceConfigList.stream().filter(p -> p.getCityJuniorMeet() != null && p.getCityMiddleMeet() > 0).count();
                if (count > 0) {
                    supportCityMeetingCount = 0;
                }
            }
        }
        double cityMeetingSalary = MathUtils.doubleMultiply(getPerMeetingSalary(SalaryKpiType.MIDDLE_MEETING_CITY), supportCityMeetingCount);
        String cityMeetingNote = generateMeetingSalaryNote(SchoolLevel.MIDDLE, SalaryKpiType.MIDDLE_MEETING_CITY, cityMeetingCount, supportCityMeetingCount);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.MIDDLE_MEETING_CITY, (long) MIDDLE_CITY_MEETING_SUPPORT_COUNT, (long) cityMeetingCount, cityMeetingSalary, cityMeetingNote);

        // 计算区级专场组会支持
        int supportCountyMeetingCount = getSupportMeetingCount(SalaryKpiType.MIDDLE_MEETING_COUNTY, countyMeetingCount);
        double countyMeetingSalary = MathUtils.doubleMultiply(getPerMeetingSalary(SalaryKpiType.MIDDLE_MEETING_COUNTY), supportCountyMeetingCount);
        String countyMeetingNote = generateMeetingSalaryNote(SchoolLevel.MIDDLE, SalaryKpiType.MIDDLE_MEETING_COUNTY, countyMeetingCount, supportCountyMeetingCount);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.MIDDLE_MEETING_COUNTY, (long) MIDDLE_COUNTY_MEETING_SUPPORT_COUNT, (long) countyMeetingCount, countyMeetingSalary, countyMeetingNote);

        // 计算插播组会支持
        int supportInterruptedMeetingCount = getSupportMeetingCount(SalaryKpiType.MIDDLE_MEETING_INTERRUPTED, interruptedMeetingCount);
        double interruptedMeetingSalary = MathUtils.doubleMultiply(getPerMeetingSalary(SalaryKpiType.MIDDLE_MEETING_INTERRUPTED), supportInterruptedMeetingCount);
        String interruptedMeetingNote = generateMeetingSalaryNote(SchoolLevel.MIDDLE, SalaryKpiType.MIDDLE_MEETING_INTERRUPTED, interruptedMeetingCount, supportInterruptedMeetingCount);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.MIDDLE_MEETING_INTERRUPTED, (long) MIDDLE_INTERRUPTED_MEETING_SUPPORT_COUNT, (long) interruptedMeetingCount, interruptedMeetingSalary, interruptedMeetingNote);
    }

    // 获取市场支持的组会数量
    private int getSupportMeetingCount(SalaryKpiType kpiType, int meetingCount) {
        int support = 0;

        if (SalaryKpiType.JUNIOR_MEETING_CITY == kpiType) {
            support = 1;
        } else if (SalaryKpiType.JUNIOR_MEETING_COUNTY == kpiType) {
            support = 3;
        } else if (SalaryKpiType.JUNIOR_MEETING_INTERRUPTED == kpiType) {
            support = 5;
        } else if (SalaryKpiType.MIDDLE_MEETING_CITY == kpiType) {
            support = 1;
        } else if (SalaryKpiType.MIDDLE_MEETING_COUNTY == kpiType) {
            support = 3;
        } else if (SalaryKpiType.MIDDLE_MEETING_INTERRUPTED == kpiType) {
            support = 5;
        }

        if (meetingCount <= support) {
            return meetingCount;
        }
        return support;
    }

    // 获取组会单场支持费用
    public double getPerMeetingSalary(SalaryKpiType kpiType) {
        if (SalaryKpiType.JUNIOR_MEETING_CITY == kpiType) {
            return SCHEME_JUNIOR_MEETING[0];
        } else if (SalaryKpiType.JUNIOR_MEETING_COUNTY == kpiType) {
            return SCHEME_JUNIOR_MEETING[1];
        } else if (SalaryKpiType.JUNIOR_MEETING_INTERRUPTED == kpiType) {
            return SCHEME_JUNIOR_MEETING[2];
        } else if (SalaryKpiType.MIDDLE_MEETING_CITY == kpiType) {
            return SCHEME_MIDDLE_MEETING[0];
        } else if (SalaryKpiType.MIDDLE_MEETING_COUNTY == kpiType) {
            return SCHEME_MIDDLE_MEETING[1];
        } else if (SalaryKpiType.MIDDLE_MEETING_INTERRUPTED == kpiType) {
            return SCHEME_MIDDLE_MEETING[2];
        }
        return 0;
    }

    public void calculateJuniorClueSalary(AgentUser user, AgentPerformanceConfig performanceConfig, List<Long> managedJuniorSchools, boolean isFormer) {
        Date salaryStartDate = getSalaryStartDate(user);
        Date salaryEndDate = getSalaryEndDate(user);

        int juniorClueCount = performanceConfig == null ? 0 : ConversionUtils.toInt(performanceConfig.getJuniorTheMothClue()); // 当月小学线索数

        // 获取学生基数
        Integer managedJuniorSchoolSize = getTotalSchoolSize(managedJuniorSchools);

        // 获取费用基数
        double juniorClueBaseSalary = getSalaryDataFromScheme(SchoolLevel.JUNIOR, managedJuniorSchoolSize, 0.00d);
        // 计算应得金额
        double clueCompleteRate = MathUtils.doubleDivide(juniorClueCount, JUNIOR_CLUE_TARGET, 3);
        double juniorClueSalary = MathUtils.doubleMultiply(juniorClueBaseSalary, clueCompleteRate > 1 ? 1 : clueCompleteRate);

        String salaryNote = generateClueSalaryNote(SchoolLevel.JUNIOR, managedJuniorSchoolSize, 0.00d, juniorClueBaseSalary, juniorClueCount);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.JUNIOR_CLUE_SUPPORT, (long)0, (long)0, juniorClueSalary, salaryNote);
    }

    public void calculateMiddleClueSalary(AgentUser user, AgentPerformanceConfig performanceConfig, List<Long> managedMiddleSchools, boolean isFormer) {
        Date salaryStartDate = getSalaryStartDate(user);
        Date salaryEndDate = getSalaryEndDate(user);

        int middleClueCount = performanceConfig == null ? 0 : ConversionUtils.toInt(performanceConfig.getMiddleTheMothClue()); // 当月中学线索数


        // 获取学生基数
        Integer managedMiddleSchoolSize = getTotalSchoolSize(managedMiddleSchools);

        // 获取费用基数
        double middleClueBaseSalary = getSalaryDataFromScheme(SchoolLevel.MIDDLE, managedMiddleSchoolSize, 0.00d);
        // 计算应得金额
        double clueCompleteRate = MathUtils.doubleDivide(middleClueCount, MIDDLE_CLUE_TARGET, 3);
        double middleClueSalary = MathUtils.doubleMultiply(middleClueBaseSalary, clueCompleteRate > 1 ? 1 : clueCompleteRate);

        String salaryNote = generateClueSalaryNote(SchoolLevel.MIDDLE, managedMiddleSchoolSize, 0.00d, middleClueBaseSalary, middleClueCount);
        saveUserCpaMonthlyResult(user, context.getSalaryMonth(), null, salaryStartDate, salaryEndDate, SalaryKpiType.MIDDLE_CLUE_SUPPORT, (long)0, (long)0, middleClueSalary, salaryNote);
    }


    // 获取学校规模之和
    private Integer getTotalSchoolSize(List<Long> schoolList) {
        if (CollectionUtils.isEmpty(schoolList)) {
            return 0;
        }
        Map<Long, SchoolExtInfo> schoolExtInfoMap = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolsExtInfoAsMap(schoolList)
                .getUninterruptibly();
        if (MapUtils.isEmpty(schoolExtInfoMap)) {
            return 0;
        }
        return schoolExtInfoMap.values().stream().filter(p -> p != null).map(SchoolExtInfo::getSchoolSize).filter(p -> p != null).reduce(0, (x, y) -> (x + y));
    }

    private double getSalaryDataFromScheme(SchoolLevel schoolLevel, Integer schoolSize, double completeRate) {
        int row = getRowIndex(schoolLevel, schoolSize);
        int column = getColumnIndex(schoolLevel, completeRate);
        if (SchoolLevel.JUNIOR == schoolLevel) {
            return SCHEME_JUNIOR_CLUE[row][column];
        } else if (SchoolLevel.MIDDLE == schoolLevel) {
            return SCHEME_MIDDLE_CLUE[row][column];
        }
        return 0d;
    }

    public int getRowIndex(SchoolLevel schoolLevel, Integer schoolSize) {
        int row = 0;
        if (SchoolLevel.JUNIOR == schoolLevel) {
            if (schoolSize < 50001) {
                row = 0;
            } else if (schoolSize > 50000 && schoolSize < 100001) {
                row = 1;
            } else if (schoolSize > 100000 && schoolSize < 150001) {
                row = 2;
            } else if (schoolSize > 150000 && schoolSize < 200001) {
                row = 3;
            } else if (schoolSize > 200000 && schoolSize < 250001) {
                row = 4;
            } else {
                row = 5;
            }
        } else if (SchoolLevel.MIDDLE == schoolLevel) {
            if (schoolSize < 30001) {
                row = 0;
            } else if (schoolSize > 30000 && schoolSize < 60001) {
                row = 1;
            } else if (schoolSize > 60000 && schoolSize < 90001) {
                row = 2;
            } else {
                row = 3;
            }
        }
        return row;
    }

    private int getColumnIndex(SchoolLevel schoolLevel, double completeRate) {
        int column = 0;
        if (SchoolLevel.JUNIOR == schoolLevel) {
            if (completeRate < 0.85) {
                column = 0;
            } else if (completeRate >= 0.85 && completeRate < 1.0) {
                column = 1;
            } else {
                column = 2;
            }
        } else if (SchoolLevel.MIDDLE == schoolLevel) {
            if (completeRate < 0.5) {
                column = 0;
            } else if (completeRate >= 0.5 && completeRate < 0.85) {
                column = 1;
            } else if (completeRate >= 0.85 && completeRate < 1.0) {
                column = 2;
            } else {
                column = 3;
            }
        }
        return column;
    }

    private String generateMeetingSalaryNote(SchoolLevel schoolLevel, SalaryKpiType meetingType, int meetingCount, int supportCount) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("学校级别：").append(schoolLevel == null ? "" : schoolLevel.getDescription());
        stringBuilder.append(", 单场支持费用：").append(getPerMeetingSalary(meetingType));
        stringBuilder.append(", 本月共组会：").append(meetingCount).append("场");
        stringBuilder.append(", 市场支持：").append(supportCount).append("场");
        return stringBuilder.toString();
    }

    private String generateClueSalaryNote(SchoolLevel schoolLevel, Integer schoolSize, double complateRate, double baseSalary, int clueCount) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("学校级别：").append(schoolLevel == null ? "" : schoolLevel.getDescription());
        stringBuilder.append(", 学生基数：").append(schoolSize);
        stringBuilder.append(", 单活完成率：").append(complateRate);
        stringBuilder.append(", 基数：").append(baseSalary).append("元");
        int target = 0;
        if (SchoolLevel.JUNIOR == schoolLevel) {
            target = JUNIOR_CLUE_TARGET;
        } else if (SchoolLevel.MIDDLE == schoolLevel) {
            target = MIDDLE_CLUE_TARGET;
        }
        stringBuilder.append(", 提供线索数目标：").append(target);
        stringBuilder.append(", 提供有效线索数：").append(clueCount);
        stringBuilder.append(", 线索数完成率：").append(MathUtils.doubleDivide(clueCount, target, 3));
        return stringBuilder.toString();
    }

}
