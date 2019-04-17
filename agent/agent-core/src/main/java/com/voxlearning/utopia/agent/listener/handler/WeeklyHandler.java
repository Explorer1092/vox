package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.PerformanceData;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.dao.mongo.AgentPerformanceRankingDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentWeeklyDao;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceRanking;
import com.voxlearning.utopia.agent.persist.entity.AgentWeekSubordinateData;
import com.voxlearning.utopia.agent.persist.entity.AgentWeekly;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WeeklyHandler
 *
 * @author song.wang
 * @date 2016/8/12
 */
@Named
public class WeeklyHandler extends SpringContainerSupport {

    @Inject private BaseOrgService baseOrgService;
    @Inject private PerformanceService performanceService;
    @Inject private AgentWeeklyDao agentWeeklyDao;
    @Inject private AgentPerformanceRankingDao agentPerformanceRankingDao;
    @Inject private WorkRecordService workRecordService;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    @Inject private AgentNotifyService agentNotifyService;


    public void executeCommand(Integer day) {
        Date date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        if (date == null) {
            date = new Date();
        }
//        if (!isSunday(date)) {
//            return;
//        }
//        agentWeeklyDao.deleteByDay(getSaturday(date, -1));
//        calculateWeeklyData(date, AgentRoleType.BusinessDeveloper);
//        calculateWeeklyData(date, AgentRoleType.CityManager);

    }

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
//        Integer thisWeekDay = getSaturday(date, -1); // 获取本周六日期
//        Integer preWeekDay = getSaturday(date, -2); // 获取上周六日期
//        if(thisWeekDay == 20161001 ){// 9月最后一周不生成周报
//            return;
//        }else if(thisWeekDay == 20161105){
//            preWeekDay = 20161101;
//        }else if(thisWeekDay == 20161203){
//            preWeekDay = 20161201;
//        }
//
//
//        Set<Long> userSet = agentGroupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
//        List<AgentWeekly> agentWeeklyList = new ArrayList<>();
//        Set<Long> notifyUsers = new HashSet<>();
//        for (Long userId : userSet) {
//            AgentWeekly agentWeekly = generateWeeklyData(userId, getRankingType(roleType), thisWeekDay, preWeekDay);
//            if (agentWeekly == null) {
//                continue;
//            }
//            agentWeeklyList.add(agentWeekly);
//            if (agentWeeklyList.size() > 100) {
//                agentWeeklyDao.inserts(agentWeeklyList);
//                agentWeeklyList.clear();
//            }
//
//            notifyUsers.add(userId);
//            if (AgentRoleType.CityManager == roleType) {
//                List<AgentUser> managerList = baseOrgService.getUserManager(userId);
//                if (CollectionUtils.isNotEmpty(managerList)) {
//                    notifyUsers.addAll(managerList.stream().map(AgentUser::getId).collect(Collectors.toSet()));
//                }
//            }
//        }
//        agentWeeklyDao.inserts(agentWeeklyList);
//
//        sendNotify(notifyUsers, thisWeekDay, generateWeeklyTitle(thisWeekDay, preWeekDay));
//    }
//
//    private boolean isSunday(Date date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
//    }
//
//    private Integer getSaturday(Date date, int weekDelta) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.add(Calendar.DAY_OF_MONTH, 7 * weekDelta);
//        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
//        return ConversionUtils.toInt(DateFormatUtils.format(calendar, "yyyyMMdd"));
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
//    private String generateWeeklyTitle(Integer thisWeekDay, Integer preWeekDay) {
//        Date startTime = DateUtils.stringToDate(String.valueOf(preWeekDay), "yyyyMMdd");
//        startTime = DateUtils.addDays(startTime, 1);
//        Integer startDay = ConversionUtils.toInt(DateUtils.dateToString(startTime, "yyyyMMdd"));
//        return "周报" + startDay + " - " + thisWeekDay;
//    }
//
//    private AgentWeekly generateWeeklyData(Long userId, Integer rankingType, Integer thisWeekDay, Integer preWeekDay) {
//        AgentWeekly agentWeekly = new AgentWeekly();
//        agentWeekly.setDay(thisWeekDay);
//        agentWeekly.setUserId(userId);
//        AgentUser user = agentUserLoaderClient.load(userId);
//        if (user != null && user.isValidUser()) {
//            agentWeekly.setUserName(user.getRealName());
//        }
//        List<AgentGroup> groupList = baseOrgService.getUserGroups(userId);
//        if(CollectionUtils.isNotEmpty(groupList)){
//            AgentGroup group = groupList.get(0);
//            agentWeekly.setGroupId(group.getId());
//            agentWeekly.setGroupName(group.getGroupName());
//        }
//
//        agentWeekly.setTitle(generateWeeklyTitle(thisWeekDay, preWeekDay));
//
//        // 设置本周涨幅数据
//        PerformanceData thisWeekPerformanceData = performanceService.loadUserPerformance(userId, thisWeekDay); // 获取本周六的业绩
//        PerformanceData preWeekPerformanceData = performanceService.loadUserPerformance(userId, preWeekDay); // 获取上周六的业绩
//        agentWeekly.setJuniorSascFloat(0d - 0d);
//        agentWeekly.setJuniorDascFloat(0d - 0d);
//        agentWeekly.setMiddleSascFloat(0d - 0d);
//
//        // 设置当前完成数据
//        agentWeekly.setJuniorSascCompleteRate(0d);
//        agentWeekly.setJuniorDascCompleteRate(0d);
//        agentWeekly.setMiddleSascCompleteRate(0d);
//
//        // 设置本周排名
//        AgentPerformanceRanking thisWeekRanking = agentPerformanceRankingDao.findByUserId(userId, rankingType, thisWeekDay);
//        if (thisWeekRanking != null) {
//            agentWeekly.setRanking(thisWeekRanking.getRanking());
//        }
//
//        // 设置上周排名
//        AgentPerformanceRanking preWeekRanking = agentPerformanceRankingDao.findByUserId(userId, rankingType, preWeekDay);
//        if (preWeekRanking != null) {
//            agentWeekly.setPreWeekRanking(preWeekRanking.getRanking());
//        }
//
//        // 设置下属数据
//        List<AgentWeekSubordinateData> subordinateDataList = generateSubordinateList(userId, preWeekDay, thisWeekDay);
//        if (CollectionUtils.isNotEmpty(subordinateDataList)) {
//            agentWeekly.setSubordinateDataList(subordinateDataList);
//        }
//        return agentWeekly;
//    }
//
//
//    private List<AgentWeekSubordinateData> generateSubordinateList(Long userId, Integer preWeekDay, Integer thisWeekDay) {
//        List<AgentUser> userList = baseOrgService.getManagedGroupUsers(userId, false);
//        if (CollectionUtils.isEmpty(userList)) {
//            return Collections.emptyList();
//        }
//
//        List<AgentWeekSubordinateData> subordinateDataList = new ArrayList<>();
//        for (AgentUser agentUser : userList) {
//            AgentRoleType roleType = baseOrgService.getUserRole(agentUser.getId());
//            if(AgentRoleType.CityAgent == roleType || AgentRoleType.CityAgentLimited == roleType){
//                continue;
//            }
//            AgentWeekSubordinateData subordinateData = new AgentWeekSubordinateData();
//            subordinateData.setUserId(agentUser.getId());
//            subordinateData.setUserName(agentUser.getRealName());
//
//            // 设置排名浮动
//            Integer rankingType = getRankingType(getUserRole(agentUser.getId()));
//            AgentPerformanceRanking thisWeekRanking = agentPerformanceRankingDao.findByUserId(agentUser.getId(), rankingType, thisWeekDay);// 本周排名
//            AgentPerformanceRanking preWeekRanking = agentPerformanceRankingDao.findByUserId(agentUser.getId(), rankingType, preWeekDay);// 上周排名
//            if(thisWeekRanking != null){
//                // 设置当前排名
//                subordinateData.setRanking(thisWeekRanking.getRanking());
//                Integer thisRanking = thisWeekRanking.getRanking() == null ? 0 : thisWeekRanking.getRanking();
//                // 设置排名浮动
//                if(preWeekRanking != null){
//                    Integer preRanking = preWeekRanking.getRanking() == null ? 0 : preWeekRanking.getRanking();
//                    subordinateData.setRankingFloat(preRanking - thisRanking);
//                }
//            }
//
//            // 设置未进校日期
//            Date startTime = DateUtils.stringToDate(String.valueOf(preWeekDay), "yyyyMMdd");
//            startTime = DateUtils.addDays(startTime, 1);
//            Date endTime = DateUtils.stringToDate(String.valueOf(thisWeekDay), "yyyyMMdd");
//            List<CrmWorkRecord> workRecordList = workRecordService.listByWorkerAndType(agentUser.getId(), CrmWorkRecordType.SCHOOL, startTime, endTime);
//            Integer startDay = ConversionUtils.toInt(DateUtils.dateToString(startTime, "yyyyMMdd"));
//            List<Integer> dayList = DayUtils.getEveryDays(startDay, thisWeekDay);
//            if (CollectionUtils.isNotEmpty(workRecordList)) {
//                Set<Integer> workDaySet = workRecordList.stream().map(p -> ConversionUtils.toInt(DateUtils.dateToString(p.getWorkTime(), "yyyyMMdd"))).collect(Collectors.toSet());
//                dayList = dayList.stream().filter(p -> !workDaySet.contains(p)).collect(Collectors.toList());
//            }
//            dayList = dayList.stream().map(this::convertDayToWeekDay).filter(p -> p > 1 && p < 7).map(k -> (k - 1)).collect(Collectors.toList());
//            subordinateData.setUnWorkedDayList(dayList);
//            subordinateDataList.add(subordinateData);
//
//        }
//        return subordinateDataList;
//    }
//
//    private Integer convertDayToWeekDay(Integer day){
//        Date date = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        return calendar.get(Calendar.DAY_OF_WEEK);
//    }
//
//
//    private AgentRoleType getUserRole(Long userId) {
//        List<AgentRoleType> roleTypeList = baseOrgService.getUserRoleList(userId);
//        if (CollectionUtils.isEmpty(roleTypeList)) {
//            return null;
//        }
//        return roleTypeList.get(0);
//    }
//
//    private void sendNotify(Collection<Long> userIds, Integer day, String title) {
//        if (CollectionUtils.isEmpty(userIds)) {
//            return;
//        }
//        agentNotifyService.sendNotify(
//                AgentNotifyType.WEEKLY_REPORT.getType(),
//                title,
//                title,
//                userIds,
//                "/mobile/report/weekly_detail.vpage?day=" + day
//        );
//    }


}
