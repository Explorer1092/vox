package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.daily.AgentDailyScoreStatisticsService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AgentDailyScoreStatisticsHandler
 *
 * @author deliang.che
 * @since  2018/11/28
 */
@Named
public class AgentDailyScoreStatisticsHandler extends SpringContainerSupport {

    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentDailyScoreStatisticsService agentDailyScoreStatisticsService;

    public void handle(Long startTime, Long endTime){
        Date startDate = null;
        Date endDate = null;
        if(startTime != null && startTime > 0){
            startDate = new Date(startTime);
        }
        if(endTime != null && endTime > 0){
            endDate = new Date(endTime);
        }
        generateData(startDate, endDate);
    }


    public void generateData(Date startDate, Date endDate){
        List<AgentGroup> groupList = new ArrayList<>();
        AgentGroup group = baseOrgService.getGroupByName("市场部");
        groupList.add(group);
        groupList.addAll(baseOrgService.getSubGroupList(group.getId()));

        // 开始时间，结束时间不能超过当天早上
        if(startDate == null || !startDate.before(DayRange.current().getStartDate())){
            startDate = DateUtils.addDays(new Date(), -1);
        }
        if(endDate == null || endDate.getTime() > DayRange.current().getStartTime()){
            endDate = DayRange.current().getStartDate();
        }
        // 生成部门的日, 周， 月数据
        agentDailyScoreStatisticsService.generateGroupStatisticsData(groupList, startDate, endDate, 1);
        agentDailyScoreStatisticsService.generateGroupStatisticsData(groupList, startDate, endDate, 2);
        agentDailyScoreStatisticsService.generateGroupStatisticsData(groupList, startDate, endDate, 3);

        // 生成user的日，周，月数据
        List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroups(groupIdList);
        Set<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        agentDailyScoreStatisticsService.generateUserStatisticsData(userIds, startDate, endDate, 1);
        agentDailyScoreStatisticsService.generateUserStatisticsData(userIds, startDate, endDate, 2);
        agentDailyScoreStatisticsService.generateUserStatisticsData(userIds, startDate, endDate, 3);

    }
}
