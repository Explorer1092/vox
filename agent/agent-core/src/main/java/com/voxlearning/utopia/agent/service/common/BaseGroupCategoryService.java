package com.voxlearning.utopia.agent.service.common;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 *
 * @author song.wang
 * @date 2017/9/20
 */
public abstract class BaseGroupCategoryService<E> extends AbstractAgentService {


    @Inject protected BaseOrgService baseOrgService;

    /**
     * 设置部门数据（按大区查看，按分区查看）
     * @param groupId
     * @return
     */
    public Map<String, List<E>> generateGroupCategoryMap(Long groupId){
        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
        // 当前部门是全国 或者大区级部门， 则获取子部门的业绩数据，并根据部门角色分组，生成按大区看，按城市看的部门列表
        if(AgentGroupRoleType.Country == groupRoleType || AgentGroupRoleType.Region == groupRoleType || AgentGroupRoleType.Area == groupRoleType){

            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId); // 获取该部门下所有的子部门

            // 按照部门角色分组
            Map<AgentGroupRoleType, List<AgentGroup>> roleGroupMap = subGroupList.stream().filter(p -> null != p && null != p.getRoleId()).collect(Collectors.groupingBy(AgentGroup::fetchGroupRoleType, Collectors.toList()));

            if(MapUtils.isNotEmpty(roleGroupMap)){
                return generateByAgentGroup(roleGroupMap);
            }
        }
        return new HashMap<>();
    }

    /**
     * 按照AgentGroup生成数据
     * @param roleGroupMap
     * @return
     */
    protected Map<String, List<E>> generateByAgentGroup(Map<AgentGroupRoleType, List<AgentGroup>> roleGroupMap){
        // 设置部门数据（按大区查看，按城市查看）
        Map<String, List<E>> groupCategoryMap = new HashMap<>();
        for(AgentGroupRoleType k : roleGroupMap.keySet()){
            List<AgentGroup> groupList = roleGroupMap.get(k);
            List<E> dataList = generateGroupDataList(groupList);
            if(dataList == null){
                dataList = new ArrayList<>();
            }
            dataList = dataList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            groupCategoryMap.put(k.name(), dataList);
        }
        return groupCategoryMap;
    }

    /**
     * 组装部门数据
     * @param groupList
     * @return
     */
    protected abstract List<E> generateGroupDataList(Collection<AgentGroup> groupList);

    /**
     * 设置User数据（按专员看）
     * @param groupId
     * @param userRole
     * @return
     */
    public List<E> generateGroupUserCategoryMap(Long groupId, AgentRoleType userRole){
        List<E> userDataList = new ArrayList<>();
        List<Long> userIdList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, userRole.getId()); // 获取该部门下指定角色的人员列表

        if(CollectionUtils.isNotEmpty(userIdList)){
            List<E> dataList = generateUserDataList(userIdList);
            if(CollectionUtils.isNotEmpty(dataList)){
                dataList = dataList.stream().filter(Objects::nonNull).collect(Collectors.toList());
                userDataList.addAll(dataList);
            }
        }

        List<Long> otherSchoolIds = baseOrgService.getCityManageOtherSchoolByGroupId(groupId);
        if(CollectionUtils.isNotEmpty(otherSchoolIds)){
            E model = generateUnAssignedSchoolData(groupId, otherSchoolIds);
            if (null != model){
                userDataList.add(model);
            }
        }
        return userDataList;
    }

    /**
     * 组装User数据
     * @param userIds
     * @return
     */
    protected abstract List<E> generateUserDataList(Collection<Long> userIds);

    /**
     * 组装未分配学校数据
     * @param groupId
     * @param schoolIds
     * @return
     */
    protected abstract E generateUnAssignedSchoolData(Long groupId, Collection<Long> schoolIds);


}
