package com.voxlearning.utopia.agent.service.dimension;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Named
public class AgentDimensionService {

    @Inject
    private BaseOrgService baseOrgService;

    // 返回数据维度
    // 1: 默认， 2：专员， 3分区， 4区域， 5大区
    public Map<Integer, String> getDimensionMap(Long userId, Long id, Integer idType, Boolean includeDefault){
        if(id == 0){
            List<Long> groupIdList = baseOrgService.getManagedGroupIdListByUserId(userId);
            if(CollectionUtils.isNotEmpty(groupIdList)){
                id = groupIdList.get(0);
                idType = AgentConstants.INDICATOR_TYPE_GROUP;
            }else {
                id = userId;
                idType = AgentConstants.INDICATOR_TYPE_USER;
            }
        }

        Map<Integer, String> dataMap = new LinkedHashMap<>();       // 有序
        if(!Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
            return dataMap;
        }

        if(SafeConverter.toBoolean(includeDefault)){
            dataMap.put(1, "默认");
        }
        AgentGroup group = baseOrgService.getGroupById(id);

        AgentGroup targetGroup = group;
        if(group.fetchGroupRoleType() != AgentGroupRoleType.City
                && group.fetchGroupRoleType() != AgentGroupRoleType.Area
                && group.fetchGroupRoleType() != AgentGroupRoleType.Region
                && group.fetchGroupRoleType() != AgentGroupRoleType.Marketing
        ){
            targetGroup = baseOrgService.getSubGroupList(group.getId()).stream()
                    .filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).findFirst().orElse(null);
        }

        if(targetGroup != null){
            if(targetGroup.fetchGroupRoleType() == AgentGroupRoleType.City){
                if(!SafeConverter.toBoolean(includeDefault)){
                    dataMap.put(2, "专员");
                }
            }else if(targetGroup.fetchGroupRoleType() == AgentGroupRoleType.Area){
                dataMap.put(2, "专员");
                dataMap.put(3, "分区");
            }else if(targetGroup.fetchGroupRoleType() == AgentGroupRoleType.Region){
                dataMap.put(2, "专员");
                dataMap.put(3, "分区");
                dataMap.put(4, "区域");
            }else if(targetGroup.fetchGroupRoleType() == AgentGroupRoleType.Marketing){
                dataMap.put(2, "专员");
                dataMap.put(3, "分区");
                dataMap.put(4, "区域");
                dataMap.put(5, "大区");
            }
        }
        return dataMap;
    }




}
