package com.voxlearning.utopia.agent.service.workspace;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.bean.CancleFakeTeacherInfo;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 判假老师
 * Created by zangtao on 2017/3/9.
 */
@Named
public class FakeTeacherRelevantService extends AbstractAgentService{

    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private BaseOrgService baseOrgService;

    /**
     * 指定日期范围内取消判假老师记录
     * @param startDate
     * @param emdDate
     * @return
     */
    public List<CancleFakeTeacherInfo> cancleFakeTeacherList(Date startDate , Date emdDate){
        List<UserServiceRecord>  surList = userLoaderClient.loadUserServiceRecordByType(UserServiceRecordOperationType.市场取消老师判假.name(),startDate,emdDate);
        List<CancleFakeTeacherInfo> cftiList = new ArrayList();
        if(CollectionUtils.isNotEmpty(surList)){
            for(UserServiceRecord usr : surList){
                //agent 用户取消的老师判假
                if(usr.getOperatorId().matches("^\\d+$") && StringUtils.isNotBlank(usr.getOperatorName())){
                   AgentUser au =  baseOrgService.getUser(Long.valueOf(usr.getOperatorId()));
                   if(au == null){ //过滤admin 的数据
                      continue;
                   }
                    CancleFakeTeacherInfo cfti = new CancleFakeTeacherInfo();
                    cfti.setCancleDate(usr.getCreateTime());
                    cfti.setOperationName(au.getRealName());
                    cfti.setTeacherId(usr.getUserId());
                    if(usr.getUserId() !=null && usr.getUserId()!= 0l){
                        Teacher teacher = teacherLoaderClient.loadTeacher(usr.getUserId());
                        cfti.setTeacherName(teacher==null?"":teacher.fetchRealname());
                    }
                    cfti.setReason(usr.getComments());//需要特殊处理
                    cfti.setDepartment("");
                    cfti.setRegion("");
                    List<Long> groupRegionIds = baseOrgService.getGroupListByRole(Long.valueOf(usr.getOperatorId()), AgentGroupRoleType.Region);
                    List<Long> groupCityIds = baseOrgService.getGroupListByRole(Long.valueOf(usr.getOperatorId()), AgentGroupRoleType.City);
                    if(CollectionUtils.isNotEmpty(groupRegionIds)){
                        AgentGroup  agentGroupRegion = baseOrgService.getGroupById(groupRegionIds.get(0));
                        cfti.setRegion(agentGroupRegion.getGroupName());//组别所在区域
                    }
                    if(CollectionUtils.isNotEmpty(groupCityIds)){
                        AgentGroup  agentGroupCity = baseOrgService.getGroupById(groupCityIds.get(0));
                        cfti.setDepartment(agentGroupCity.getGroupName());//组别所在区域
                    }
                    cftiList.add(cfti);
                }

            }
        }

        return cftiList;
    }
}
