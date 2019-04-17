package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.PerformanceData;
import com.voxlearning.utopia.agent.bean.PerformanceRankingData;
import com.voxlearning.utopia.agent.constants.AgentAppContentType;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.constants.AgentRecommendBookRoleType;
import com.voxlearning.utopia.agent.dao.mongo.AgentMonthlyDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentPerformanceRankingDao;
import com.voxlearning.utopia.agent.persist.entity.*;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.AgentPerformanceRankingService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.workspace.AgentAppContentPacketService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MonthlyHandler
 *
 * @author song.wang
 * @date 2016/8/17
 */
@Named
public class MonthlyHandler extends SpringContainerSupport {

    @Inject private AgentMonthlyDao agentMonthlyDao;
    @Inject private BaseOrgService baseOrgService;
    @Inject private PerformanceService performanceService;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    @Inject private AgentPerformanceRankingService performanceRankingService;
    @Inject private WorkRecordService workRecordService;
    @Inject private AgentPerformanceRankingDao agentPerformanceRankingDao;
    @Inject private AgentAppContentPacketService agentAppContentPacketService;
    @Inject private AgentNotifyService agentNotifyService;

    public void executeCommand(Integer day) {
        Date date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        if (date == null) {
            date = new Date();
        }
//        if (!isMonthLastDay(date)) {
//            return;
//        }
        Date performanceDate = performanceService.lastSuccessDataDate();
        if(performanceDate.before(date)){
            date = performanceDate;
        }
        Integer month = ConversionUtils.toInt(DateUtils.dateToString(date, "yyyyMM"));
        agentMonthlyDao.deleteByMonth(month);
//        calculateWeeklyData(date, AgentRoleType.BusinessDeveloper);
//        calculateWeeklyData(date, AgentRoleType.CityManager);
//        calculateWeeklyData(date, AgentRoleType.Region);

    }
//
//    private boolean isMonthLastDay(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//    }

//    private void calculateWeeklyData(Date date, AgentRoleType roleType) {
//        // 获取指定角色所有的用户
//        AgentGroup marketGroup = baseOrgService.getGroupByName("市场部");
//        if (marketGroup == null) {
//            return;
//        }
//        List<AgentGroupUser> agentGroupUserList = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(marketGroup.getId(), roleType.getId());
//        if (CollectionUtils.isEmpty(agentGroupUserList)) {
//            return;
//        }
//
//        Set<Long> userSet = agentGroupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
//        List<AgentMonthly> agentMonthlyList = new ArrayList<>();
//        Set<Long> notifyUsers = new HashSet<>();
//
//        for (Long userId : userSet) {
//            AgentMonthly agentMonthly = generateMonthlyData(userId, roleType, date);
//            if (agentMonthly == null) {
//                continue;
//            }
//            agentMonthlyList.add(agentMonthly);
//            if (agentMonthlyList.size() > 100) {
//                agentMonthlyDao.inserts(agentMonthlyList);
//                agentMonthlyList.clear();
//            }
//            notifyUsers.add(userId);
//        }
//        if(CollectionUtils.isNotEmpty(agentMonthlyList)){
//            agentMonthlyDao.inserts(agentMonthlyList);
//        }
//
//        Integer month = ConversionUtils.toInt(DateUtils.dateToString(date, "yyyyMM"));
//        sendNotify(notifyUsers, month);
//    }
//
//    private Integer getRankingType(AgentRoleType roleType) {
//        Integer type = 3;
//        if (roleType == AgentRoleType.Region) {
//            type = 1;
//        } else if (roleType == AgentRoleType.CityManager) {
//            type = 2;
//        } else {
//            type = 3;
//        }
//        return type;
//    }
//
//    private void sendNotify(Collection<Long> userIds, Integer month) {
//        if (CollectionUtils.isEmpty(userIds)) {
//            return;
//        }
//        String title = "月报" + month;
//        agentNotifyService.sendNotify(
//                AgentNotifyType.MONTHLY_REPORT.getType(),
//                title,
//                title,
//                userIds,
//                "/mobile/report/monthly_detail.vpage?month=" + month
//        );
//    }
//
//
//    private AgentMonthly generateMonthlyData(Long userId, AgentRoleType roleType, Date date) {
//        Integer day = ConversionUtils.toInt(DateUtils.dateToString(date, "yyyyMMdd"));
//        Integer month = ConversionUtils.toInt(DateUtils.dateToString(date, "yyyyMM"));
//
//        AgentMonthly agentMonthly = new AgentMonthly();
//        agentMonthly.setMonth(month);
//        agentMonthly.setUserId(userId);
//        agentMonthly.setUserRole(roleType);
//        AgentUser user = agentUserLoaderClient.load(userId);
//        if (user != null && user.isValidUser()) {
//            agentMonthly.setUserName(user.getRealName());
//        }
//        // 设置业绩数据
//        setPerformanceData(agentMonthly, userId, roleType, day);
//        // 设置进校数据
//        setVisitSchoolData(agentMonthly, userId, roleType, day);
//        // 设置优秀团队，优秀个人
//        setExcellentGroup(agentMonthly, userId, day);
//        // 设置大区进校王
//        setExcelentVisitSchoolData(agentMonthly, userId, day);
//        // 设置老大推荐读物
//        setRecommendBook(agentMonthly, userId);
//        // 设置市经理关联的专员的业绩
//        setManagedUserPerformanceList(agentMonthly, userId, roleType, day);
//
//        // 设置271分布情况
//        setPerformanceDistributionData(agentMonthly, userId, roleType, day);
//
//        return agentMonthly;
//
//    }
//
//    // 设置业绩部分数据
//    private void setPerformanceData(AgentMonthly agentMonthly, Long userId, AgentRoleType roleType, Integer day){
//        PerformanceRankingData performanceRankingData = performanceRankingService.getRankingDataByUserId(userId, getRankingType(roleType), day);
//        if(performanceRankingData != null){
//            agentMonthly.setRanking(performanceRankingData.getRanking());
//        }
//
//        // 设置业绩数据
//        PerformanceData performanceData = performanceService.loadUserPerformance(userId, day);
//        if(performanceData != null){
//            AgentMonthlyPerformance myPerformance = new AgentMonthlyPerformance();
//            myPerformance.setJuniorSascCompleteRate(0d);
//            myPerformance.setJuniorDascCompleteRate(0d);
//            myPerformance.setMiddleSascCompleteRate(0d);
//            agentMonthly.setMyPerformance(myPerformance);
//        }
//    }
//
//    // 设置进校部分数据
//    private void setVisitSchoolData(AgentMonthly agentMonthly, Long userId, AgentRoleType roleType, Integer day){
//        Date endDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
//        Date startDate = DayUtils.getFirstDayOfMonth(endDate);
//        endDate = DateUtils.addDays(endDate, 1);
//        agentMonthly.setIntoSchoolCount(0);
//        agentMonthly.setInPlanIntoSchoolCount(0);
//        agentMonthly.setNotIntoSchoolCount(0);
//        agentMonthly.setVisitSchoolCount(0);
//        agentMonthly.setPerMemberIntoSchoolCount(0);
//        if(roleType == AgentRoleType.BusinessDeveloper){
//            // 获取进校列表
//            List<CrmWorkRecord> workRecordList = workRecordService.listByWorkerAndType(userId, CrmWorkRecordType.SCHOOL, startDate, endDate);
//            if(CollectionUtils.isNotEmpty(workRecordList)){
//                agentMonthly.setIntoSchoolCount(workRecordList.size());
//                List<CrmWorkRecord> inplanRecord = workRecordList.stream().filter(p -> SafeConverter.toBoolean(p.getIsPlanIntoSchool())).collect(Collectors.toList());
//                agentMonthly.setInPlanIntoSchoolCount(inplanRecord.size());
//            }
//
//            // 设置未拜访学校数
//            Set<Long> notIntoSchoolList = workRecordService.getUnVisitSchools(userId, CrmWorkRecordType.SCHOOL, startDate, endDate);
//            agentMonthly.setNotIntoSchoolCount(notIntoSchoolList.size());
//
//        }else if(roleType == AgentRoleType.CityManager){
//            // 获取陪访列表
//            List<CrmWorkRecord> workRecordList = workRecordService.listByWorkerAndType(userId, CrmWorkRecordType.VISIT, startDate, endDate);
//            agentMonthly.setVisitSchoolCount(workRecordList.size());
//
//            List<AgentUser> memberList = baseOrgService.getManagedGroupUsers(userId, false);
//            if(CollectionUtils.isNotEmpty(memberList)){
//                // 过滤出手下专员ID
//                List<Long> agentUserIdList = memberList.stream().filter(p -> baseOrgService.getUserRole(p.getId()) == AgentRoleType.BusinessDeveloper).map(AgentUser::getId).collect(Collectors.toList());
//                if(CollectionUtils.isNotEmpty(agentUserIdList)){
//                    Map<Long, List<CrmWorkRecord>> workRecordMap = workRecordService.listByWorkersAndType(agentUserIdList, CrmWorkRecordType.SCHOOL, startDate, endDate);
//                    List<CrmWorkRecord> businessDeveloperWorkRecordList = new ArrayList<>();
//                    workRecordMap.values().forEach(businessDeveloperWorkRecordList::addAll);
//                    // 设置市经理手下专员的全部进校数量
//                    agentMonthly.setIntoSchoolCount(businessDeveloperWorkRecordList.size());
//                    // 设置计划内进校
//                    int inPlanIntoSchoolCount = (int)businessDeveloperWorkRecordList.stream().filter(p -> SafeConverter.toBoolean(p.getIsPlanIntoSchool())).count();
//                    agentMonthly.setInPlanIntoSchoolCount(inPlanIntoSchoolCount);
//
//                    // 设置团队人均进校
//                    int perMemberIntoSchoolCount = (int)MathUtils.floatDivide(businessDeveloperWorkRecordList.size(), agentUserIdList.size(), 0, BigDecimal.ROUND_CEILING);
//                    agentMonthly.setPerMemberIntoSchoolCount(perMemberIntoSchoolCount);
//                }
//            }
//        }
//    }
//
//
//    // 设置优秀团队及个人数据
//    private void setExcellentGroup(AgentMonthly agentMonthly, Long userId, Integer day){
//        List<AgentMonthlyExcellentGroupAndUser> excellentGroupList = new ArrayList<>();
//        agentMonthly.setExcellentGroupAndUserList(excellentGroupList);
//
//        List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(userId);
//        if(CollectionUtils.isEmpty(groupIdList)){
//            return;
//        }
//        Long groupId = groupIdList.get(0);
//        // 获取当前用户所在的大区级部门
//        AgentGroup group =  baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.Region);
//        if(group == null){
//            return;
//        }
//
//        // 设置优秀团队
//        // 获取同大区下面指定角色的用户
//        List<Long> cityManagerIdList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(group.getId(), AgentRoleType.CityManager.getId());
//        Map<Long, AgentPerformanceRanking> cityManagerRankingMap = agentPerformanceRankingDao.findByUserIds(cityManagerIdList, 2, day);
//        List<AgentPerformanceRanking> cityManagerRankingDataList = MapUtils.isEmpty(cityManagerRankingMap)? new ArrayList<>() : new ArrayList<>(cityManagerRankingMap.values());
//        if(CollectionUtils.isNotEmpty(cityManagerRankingDataList)){
//            Collections.sort(cityManagerRankingDataList, (o1, o2) -> o1.getRanking() - o2.getRanking());
//            excellentGroupList.add(convertToExcellentData(cityManagerRankingDataList.get(0)));
//        }
//        // 设置优秀个人
//        List<Long> businessDeveloperIdList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(group.getId(), AgentRoleType.BusinessDeveloper.getId());
//        Map<Long, AgentPerformanceRanking> businessDeveloperRankingMap = agentPerformanceRankingDao.findByUserIds(businessDeveloperIdList, 3, day);
//        List<AgentPerformanceRanking> businessDeveloperRankingDataList = MapUtils.isEmpty(businessDeveloperRankingMap)? new ArrayList<>() : new ArrayList<>(businessDeveloperRankingMap.values());
//        if(CollectionUtils.isNotEmpty(businessDeveloperRankingDataList)){
//            Collections.sort(businessDeveloperRankingDataList, (o1, o2) -> o1.getRanking() - o2.getRanking());
//            excellentGroupList.add(convertToExcellentData(businessDeveloperRankingDataList.get(0)));
//        }
//        agentMonthly.setExcellentGroupAndUserList(excellentGroupList);
//    }
//
//    private AgentMonthlyExcellentGroupAndUser convertToExcellentData(AgentPerformanceRanking performanceRanking){
//        AgentMonthlyExcellentGroupAndUser excellentGroupAndUser = new AgentMonthlyExcellentGroupAndUser();
//        excellentGroupAndUser.setType(performanceRanking.getType());
//        excellentGroupAndUser.setUserId(performanceRanking.getUserId());
//        excellentGroupAndUser.setUserName(performanceRanking.getUserName());
//        excellentGroupAndUser.setGroupId(performanceRanking.getGroupId());
//        excellentGroupAndUser.setGroupName(performanceRanking.getGroupName());
//        excellentGroupAndUser.setRanking(performanceRanking.getRanking());
//        excellentGroupAndUser.setTotalCount(performanceRanking.getTotalCount());
//        return excellentGroupAndUser;
//    }
//
//    // 设置大区进校王
//    private void setExcelentVisitSchoolData(AgentMonthly agentMonthly, Long userId, Integer day){
//        List<AgentMonthlyVisitSchoolRanking> visitSchoolRankingList = new ArrayList<>();
//        agentMonthly.setVisitSchoolRankingList(visitSchoolRankingList);
//        // 获取用户所在的大区
//        List<Long> groupIdList = baseOrgService.getGroupListByRole(userId, AgentGroupRoleType.Region);
//        if(CollectionUtils.isEmpty(groupIdList)){
//            return;
//        }
//        Date endDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
//        Date startDate = DayUtils.getFirstDayOfMonth(endDate);
//        endDate = DateUtils.addDays(endDate, 1);
//        // 获取该大区下所有的专员
//        List<Long> agentUserIdList = new ArrayList<>();
//        List<Long> businessDevelperIdList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupIdList.get(0), AgentRoleType.BusinessDeveloper.getId());
//        if(CollectionUtils.isNotEmpty(businessDevelperIdList)){
//            agentUserIdList.addAll(businessDevelperIdList);
//        }
//        List<Long> cityManagerUserIdList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupIdList.get(0), AgentRoleType.CityManager.getId());
//        if(CollectionUtils.isNotEmpty(cityManagerUserIdList)){
//            agentUserIdList.addAll(cityManagerUserIdList);
//        }
//        Map<Long, List<CrmWorkRecord>> workRecordMap = workRecordService.listByWorkersAndType(agentUserIdList, CrmWorkRecordType.SCHOOL, startDate, endDate);
//        List<Properties> propertiesList = new ArrayList<>();
//        workRecordMap.forEach((k, v) -> {
//            Properties properties = new Properties();
//            properties.put("userId", k);
//            properties.put("recordCount", v.size());
//            propertiesList.add(properties);
//        });
//
//        Collections.sort(propertiesList, (o1, o2) -> (Integer)o2.get("recordCount") - (Integer)o1.get("recordCount"));
//        if(CollectionUtils.isEmpty(propertiesList)){
//            return;
//        }
//        Properties properties = propertiesList.get(0);
//        AgentMonthlyVisitSchoolRanking agentVisitSchoolRanking = new AgentMonthlyVisitSchoolRanking();
//        Long targetUserId = (Long)properties.get("userId");
//        AgentUser targetUser = baseOrgService.getUser(targetUserId);
//        agentVisitSchoolRanking.setUserId(targetUser.getId());
//        agentVisitSchoolRanking.setUserName(targetUser.getRealName());
//        List<AgentGroup> groupList = baseOrgService.getUserGroups(targetUserId);
//        if(CollectionUtils.isNotEmpty(groupList)){
//            agentVisitSchoolRanking.setGroupId(groupList.get(0).getId());
//            agentVisitSchoolRanking.setGroupName(groupList.get(0).getGroupName());
//        }
//        agentVisitSchoolRanking.setVisitSchoolCount((Integer)properties.get("recordCount"));
//        visitSchoolRankingList.add(agentVisitSchoolRanking);
//    }
//
//    // 设置老大推荐读物
//    private void setRecommendBook(AgentMonthly agentMonthly, Long userId){
//        List<AgentMonthlyRecommendBook> recommendBookList = new ArrayList<>();
//        agentMonthly.setRecommendBookList(recommendBookList);
//
//        List<AgentAppContentPacket> bookList = agentAppContentPacketService.loadByContentType(AgentAppContentType.RECOMMEND_BOOK);
//        AgentRoleType userRole = baseOrgService.getUserRole(userId);
//        if(CollectionUtils.isNotEmpty(bookList) && userRole != null){
//            bookList.stream().filter(p -> AgentRecommendBookRoleType.typeOf(userRole.getId()) == p.getRole()).forEach(p -> {
//                AgentMonthlyRecommendBook book = new AgentMonthlyRecommendBook();
//                book.setBookName(p.getBookName());
//                book.setBookCoverUrl(p.getBookCoverUrl());
//                recommendBookList.add(book);
//            });
//        }
//    }
//
//    // 设置下属的业绩数据
//    private void setManagedUserPerformanceList(AgentMonthly agentMonthly, Long userId, AgentRoleType roleType, Integer day){
//        if(AgentRoleType.CityManager == roleType){
//            List<AgentUser> memberList = baseOrgService.getManagedGroupUsers(userId, false);
//            if(CollectionUtils.isNotEmpty(memberList)){
//                // 过滤出手下专员ID
//                List<Long> agentUserIdList = memberList.stream().filter(p -> baseOrgService.getUserRole(p.getId()) == AgentRoleType.BusinessDeveloper).map(AgentUser::getId).collect(Collectors.toList());
//                if(CollectionUtils.isNotEmpty(agentUserIdList)){
//                    List<AgentMonthlyPerformance> managedUserPerformanceList = agentUserIdList.stream().map(p -> {
//                        PerformanceData performanceData = performanceService.loadUserPerformance(p, day);
//                        AgentMonthlyPerformance performance = new AgentMonthlyPerformance();
//                        performance.setUserId(Long.valueOf(performanceData.getKey()));
//                        performance.setUserName(performanceData.getName());
//                        performance.setJuniorSascCompleteRate(0d);
//                        performance.setJuniorDascCompleteRate(0d);
//                        performance.setMiddleSascCompleteRate(0d);
//                        return performance;
//                    }).collect(Collectors.toList());
//                    agentMonthly.setManagedUserPerformanceList(managedUserPerformanceList);
//                }
//            }
//        }
//    }
//
//
//    // 设置 271 分布
//    private void setPerformanceDistributionData(AgentMonthly agentMonthly, Long userId, AgentRoleType roleType, Integer day){
//        List<AgentMonthlyPerformanceDistribution> performanceDistributionList = new ArrayList<>();
//        agentMonthly.setPerformanceDistributionList(performanceDistributionList);
//        if(AgentRoleType.Region != roleType){
//            return;
//        }
//
//        List<AgentUser> cityManagers = baseOrgService.getManagedGroupUsers(userId, false);
//        if(CollectionUtils.isEmpty(cityManagers)){
//            return;
//        }
//
//        for(AgentUser user : cityManagers){
//            AgentMonthlyPerformanceDistribution performanceDistribution = new AgentMonthlyPerformanceDistribution();
//            performanceDistribution.setUserId(user.getId());
//            performanceDistribution.setRankingRate(1d);
//            performanceDistribution.setInterval1Count(0);
//            performanceDistribution.setInterval2Count(0);
//            performanceDistribution.setInterval3Count(0);
//
//            List<AgentGroup> groupList = baseOrgService.getUserGroups(user.getId());
//            if(CollectionUtils.isNotEmpty(groupList)){
//                AgentGroup group = groupList.get(0);
//                performanceDistribution.setGroupId(group.getId());
//                performanceDistribution.setGroupName(group.getGroupName());
//            }
//
//            AgentPerformanceRanking performanceRanking = agentPerformanceRankingDao.findByUserId(user.getId(), 2, day);
//            if(performanceRanking != null){
//                performanceDistribution.setRankingRate(performanceRanking.calRankingRate());
//            }
//
//            List<AgentUser> memberList = baseOrgService.getManagedGroupUsers(user.getId(), false);
//            if(CollectionUtils.isNotEmpty(memberList)) {
//                // 过滤出手下专员ID
//                List<Long> agentUserIdList = memberList.stream().filter(p -> baseOrgService.getUserRole(p.getId()) == AgentRoleType.BusinessDeveloper).map(AgentUser::getId).collect(Collectors.toList());
//                if (CollectionUtils.isNotEmpty(agentUserIdList)) {
//                    List<AgentPerformanceRanking> performanceRankingList = agentUserIdList.stream().map(p -> agentPerformanceRankingDao.findByUserId(p, 3, day)).filter(p -> p != null).collect(Collectors.toList());
//                    if(CollectionUtils.isNotEmpty(performanceRankingList)){
//                        performanceDistribution.setInterval1Count((int)performanceRankingList.stream().filter(p -> p.calRankingRate() <= 0.2).count());
//                        performanceDistribution.setInterval2Count((int)performanceRankingList.stream().filter(p -> p.calRankingRate() > 0.2 && p.calRankingRate() <= 0.9).count());
//                        performanceDistribution.setInterval3Count((int)performanceRankingList.stream().filter(p -> p.calRankingRate() > 0.9).count());
//                    }
//                }
//            }
//            performanceDistributionList.add(performanceDistribution);
//        }
//    }



}
