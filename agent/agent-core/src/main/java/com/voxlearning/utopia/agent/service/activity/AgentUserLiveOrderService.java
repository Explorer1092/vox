/**
 * Author:   xianlong.zhang
 * Date:     2018/12/18 15:51
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentRegisterTeacherStatisticsService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class AgentUserLiveOrderService extends AbstractAgentService {

    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentRegisterTeacherStatisticsService agentRegisterTeacherStatisticsService;

    public Map<String,Object> selectRange(Long currentUserId, Long id, Integer idType){
        if(id == 0){
            List<Long> groupIdList = baseOrgService.getManagedGroupIdListByUserId(currentUserId);
            if(CollectionUtils.isNotEmpty(groupIdList)){
                id = groupIdList.get(0);
                idType = AgentConstants.INDICATOR_TYPE_GROUP;
            }else {
                id = currentUserId;
                idType = AgentConstants.INDICATOR_TYPE_USER;
            }
        }

        Map<String,Object> dataMap = new HashMap<>();
        //组织
        Map<String, Object> organizationMap = generateOrganization(id, idType);
        List<Map<String, Object>> groupRoleTypeList = (List<Map<String, Object>>)organizationMap.get("groupRoleTypeList");
        if(idType.equals(AgentConstants.INDICATOR_TYPE_GROUP)){
            dataMap.put("group", agentRegisterTeacherStatisticsService.getDefaultGroup(id));
        }
        dataMap.put("groupRoleTypeList",groupRoleTypeList);
        return dataMap;
    }

    public Map<String,Object> generateOrganization(Long id, Integer idType){
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String,Object>> groupRoleTypeList = new ArrayList<>();
        if(idType.equals(AgentConstants.INDICATOR_TYPE_USER)){
            dataMap.put("groupRoleTypeList",groupRoleTypeList);
            return dataMap;
        }
        AgentGroup group = agentRegisterTeacherStatisticsService.getDefaultGroup(id);
        AgentGroupRoleType currentGroupRoleType = group.fetchGroupRoleType();


        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put("groupRoleType","default");
        defaultMap.put("roleName","默认");
        defaultMap.put("show",true);
        groupRoleTypeList.add(defaultMap);

        //分区、
        if (currentGroupRoleType == AgentGroupRoleType.City){
            groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
        }else if(currentGroupRoleType == AgentGroupRoleType.Area){ //区域
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,false));
            groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
        }else if (currentGroupRoleType == AgentGroupRoleType.Region){//大区
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,false));
            groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
            if (group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)){
                groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area,false));
            }
            //市场
        }else if (currentGroupRoleType == AgentGroupRoleType.Country || currentGroupRoleType == AgentGroupRoleType.Marketing){
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.City,false));
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Area,false));
            groupRoleTypeList.add(generateGroupRoleType(AgentGroupRoleType.Region,false));
            groupRoleTypeList.add(generateRoleType(AgentRoleType.BusinessDeveloper,"专员",false));
        }
        dataMap.put("groupRoleTypeList",groupRoleTypeList);
        return dataMap;
    }

    public Map<String,Object> generateGroupRoleType(AgentGroupRoleType groupRoleType,Boolean ifShow){
        Map<String,Object> groupRoleTypeMap = new HashMap<>();
        groupRoleTypeMap.put("groupRoleType",groupRoleType);
        groupRoleTypeMap.put("roleName",groupRoleType.getRoleName());
        groupRoleTypeMap.put("show",ifShow);
        return groupRoleTypeMap;
    }
    public Map<String,Object> generateRoleType(AgentRoleType roleType,String roleName,Boolean ifShow){
        Map<String,Object> roleTypeMap = new HashMap<>();
        roleTypeMap.put("groupRoleType",roleType);
        roleTypeMap.put("roleName",roleName);
        roleTypeMap.put("show",ifShow);
        return roleTypeMap;
    }

}
