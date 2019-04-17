package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.bean.ClazzAlterStatistics;
import com.voxlearning.utopia.agent.service.common.BaseGroupCategoryService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 换班待处理的统计查询
 *
 * @author chunlin.yu
 * @create 2017-09-21 16:58
 **/
@Named
public class ClazzAlterService extends BaseGroupCategoryService<ClazzAlterStatistics> {

    @Inject
    AgentResourceService agentResourceService;


    @Override
    public Map<String, List<ClazzAlterStatistics>> generateGroupCategoryMap(Long groupId) {
        throw new UnsupportedOperationException();
    }

    public List<ClazzAlterStatistics> generateGroupUserCategoryMap(Long groupId, AgentRoleType userRole){
        throw new UnsupportedOperationException();
    }

    public List<ClazzAlterStatistics> generateGroupClazzAlterStatistics(Long groupId) {
        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        if (agentGroup.fetchGroupRoleType() != AgentGroupRoleType.City){
            List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(agentGroup.getId());
            subGroupList = subGroupList.stream().filter(p -> p.fetchGroupRoleType() != null).collect(Collectors.toList());
            return generateGroupDataList(subGroupList);
        }else {
            return super.generateGroupUserCategoryMap(agentGroup.getId(), AgentRoleType.BusinessDeveloper);
        }
    }

    @Override
    protected List<ClazzAlterStatistics> generateGroupDataList(Collection<AgentGroup> groupList) {
        List<ClazzAlterStatistics> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(groupList)){
            groupList.forEach(item -> {
                List<Long> managedSchoolIds = baseOrgService.getManagedSchoolListByGroupId(item.getId());
                long result = agentResourceService.countPendingClazzAlterationBySchool(managedSchoolIds, 10, 0);
                resultList.add(generateClazzAlterStatistics(item.getGroupName(),result,item.getId(),IdType.GROUP,"clazz_alter_statistics.vpage?groupId="+item.getId()));
            });
        }
        resultList.sort((o1,o2)->{
            return Long.compare(o2.getCount(), o1.getCount());
        });
        return resultList;
    }


    @Override
    protected List<ClazzAlterStatistics> generateUserDataList(Collection<Long> userIds) {
        List<ClazzAlterStatistics> clazzAlterStatisticsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(userIds)){
            List<AgentUser> users = baseOrgService.getUsers(userIds);
            if (CollectionUtils.isNotEmpty(users)){
                users.forEach(item -> {
                    List<Long> schoolIds = baseOrgService.loadBusinessSchoolByUserId(item.getId());
                    long result = agentResourceService.countPendingClazzAlterationBySchool(schoolIds, 10, 0);
                    clazzAlterStatisticsList.add(generateClazzAlterStatistics(item.getRealName(),result,item.getId(),IdType.USER,"clazz_alter.vpage?idType=" + IdType.USER.name() + "&id="+item.getId()));
                });
            }
        }
        clazzAlterStatisticsList.sort((o1,o2)->{
            return Long.compare(o2.getCount(), o1.getCount());
        });
        return clazzAlterStatisticsList;
    }

    @Override
    protected ClazzAlterStatistics generateUnAssignedSchoolData(Long groupId, Collection<Long> schoolIds) {
        long result = agentResourceService.countPendingClazzAlterationBySchool(schoolIds, 10, 0);
        return generateClazzAlterStatistics("未分配",result,groupId,IdType.GROUP,"clazz_alter.vpage?idType=" + IdType.OTHER_SCHOOL.name() + "&id="+groupId);
    }

    private ClazzAlterStatistics generateClazzAlterStatistics(String name,long count,Long id,IdType idType,String url){
        ClazzAlterStatistics statistics = new ClazzAlterStatistics();
        statistics.setName(name);
        statistics.setCount(count);
        statistics.setNextUrl(url);
        statistics.setId(id);
        statistics.setIdType(idType.name());
        return statistics;
    }

    public enum IdType{
        USER,
        GROUP,
        OTHER_SCHOOL;
    }
}
