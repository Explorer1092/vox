package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentWorkRecordStatisticsService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AgentWorkRecordStatisticsHandler
 *
 * @author song.wang
 * @date 2018/1/25
 */
@Named
public class AgentWorkRecordStatisticsHandler extends SpringContainerSupport {

    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentWorkRecordStatisticsService agentWorkRecordStatisticsService;
    @Inject private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;
    @Inject private WorkRecordService workRecordService;

    public void handle(Long startTime, Long endTime, Integer type){

        Date startDate = null;
        Date endDate = null;
        if(startTime != null && startTime > 0){
            startDate = new Date(startTime);
        }
        if(endTime != null && endTime > 0){
            endDate = new Date(endTime);
        }

        generateData(startDate, endDate, type);

    }


    // type :   1:统计数据  2：工作记录的T值计算
    public void generateData(Date startDate, Date endDate, Integer type){
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
        if(type == 1){
            // 生成部门的日, 周， 月数据
            agentWorkRecordStatisticsService.generateGroupStatisticsData(groupList, startDate, endDate, 1);
            agentWorkRecordStatisticsService.generateGroupStatisticsData(groupList, startDate, endDate, 2);
            agentWorkRecordStatisticsService.generateGroupStatisticsData(groupList, startDate, endDate, 3);

            // 生成user的日，周，月数据
            List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroups(groupIdList);
            Set<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            agentWorkRecordStatisticsService.generateUserStatisticsData(userIds, startDate, endDate, 1);
            agentWorkRecordStatisticsService.generateUserStatisticsData(userIds, startDate, endDate, 2);
            agentWorkRecordStatisticsService.generateUserStatisticsData(userIds, startDate, endDate, 3);
        }else if(type == 2){
            List<AgentGroupUser> groupUserList = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            Set<Long> allUserIdList = groupUserList.stream().map(AgentGroupUser::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());

            while (startDate.before(endDate)){

                Date targetDate = DateUtils.addMonths(startDate, 1);
                List<WorkRecordData> workRecordDataList = workRecordService.getWorkRecordDataListByUserTypeTime(allUserIdList, null, startDate, targetDate);
                if(CollectionUtils.isNotEmpty(workRecordDataList)){
                    for(WorkRecordData record : workRecordDataList){
                        workRecordService.saveRecordWorkload(record);
                    }
                }
                startDate = targetDate;
            }
        }
    }
}
