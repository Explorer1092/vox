package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentRegisterTeacherStatisticsService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentWorkRecordStatisticsService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;

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
public class AgentRegisterTeacherStatisticsHandler extends SpringContainerSupport {

    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentRegisterTeacherStatisticsService agentRegisterTeacherStatisticsService;
    @Inject private EmailServiceClient emailServiceClient;
    public void handle(String dateStr,Integer dayNum){

        Date date = null;
        if(StringUtils.isNotBlank(dateStr)){
            date = DateUtils.stringToDate(dateStr,DateUtils.FORMAT_SQL_DATE);
        }else{
            return;
        }
        emailServiceClient.createPlainEmail().body("刷数据开始，日期：" + DateUtils.dateToString(date,DateUtils.FORMAT_SQL_DATE) + " 天数:" + dayNum).subject("初始化新注册老师数据").to("xianlong.zhang@17zuoye.com;").send();
        generateTeacherData(date,dayNum);
        emailServiceClient.createPlainEmail().body("刷数据结束，日期：" + DateUtils.dateToString(date,DateUtils.FORMAT_SQL_DATE)+ " 天数:" + dayNum).subject("初始化新注册老师数据").to("xianlong.zhang@17zuoye.com;").send();
    }


    // type :   1:统计数据  2：工作记录的T值计算
    public void generateTeacherData(Date date,int dayNum){
        List<AgentGroup> groupList = new ArrayList<>();
        AgentGroup group = baseOrgService.getGroupByName("市场部");
        groupList.add(group);
        groupList.addAll(baseOrgService.getSubGroupList(group.getId()));
        groupList = groupList.stream().filter(g -> g.fetchGroupRoleType() == AgentGroupRoleType.City).collect(Collectors.toList());
        // 生成user的日，周，月数据
        List<Long> groupIdList = groupList.stream().map(AgentGroup::getId).collect(Collectors.toList());
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroups(groupIdList);
        Set<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());

        userIds.forEach(uid ->{
            agentRegisterTeacherStatisticsService.generateUserJobTeacherData(uid, date,  1,dayNum);
            agentRegisterTeacherStatisticsService.generateUserJobTeacherData(uid, date, 2,dayNum);
            agentRegisterTeacherStatisticsService.generateUserJobTeacherData(uid, date, 3,dayNum);
        });
        groupList.forEach(gid->{
            agentRegisterTeacherStatisticsService.generateGroupJobTeacherData(gid.getId(), date,  1,dayNum);
            agentRegisterTeacherStatisticsService.generateGroupJobTeacherData(gid.getId(), date, 2,dayNum);
            agentRegisterTeacherStatisticsService.generateGroupJobTeacherData(gid.getId(), date, 3,dayNum);
        });

    }
}
