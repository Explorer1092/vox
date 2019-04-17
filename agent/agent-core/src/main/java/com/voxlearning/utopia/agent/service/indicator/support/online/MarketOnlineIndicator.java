package com.voxlearning.utopia.agent.service.indicator.support.online;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicatorWithBudget;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.indicator.OnlineIndicatorService;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 市场部下各个部分数据的实现
 *
 * @author song.wang
 * @date 2018/11/6
 */
@Named
public class MarketOnlineIndicator extends OnlineIndicatorFactory {

    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;
    @Inject
    private OnlineIndicatorService onlineIndicatorService;
    @Inject
    private BaseOrgService baseOrgService;

    @Override
    public SumOnlineIndicatorWithBudget generateOverview(Long id, Integer dataType, Integer day, Integer schoolLevelFlag) {
        List<Integer> schoolLevelIds = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, dataType, schoolLevelFlag);

        SumOnlineIndicatorWithBudget indicatorWithBudget = null;
        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
            indicatorWithBudget = onlineIndicatorService.loadGroupSumDataWithBudget(id, day, schoolLevelIds);
        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            indicatorWithBudget = onlineIndicatorService.loadUserSumDataWithBudget(id, day, schoolLevelIds);
        }
        return indicatorWithBudget;
    }

    @Override
    public Map<Long, SumOnlineIndicatorWithBudget> generateGroupDataList(Collection<Long> groupIds, Integer day, Integer schoolLevelFlag) {
        Map<Long, SumOnlineIndicatorWithBudget> indicatorWithBudgetMap = new HashMap<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return indicatorWithBudgetMap;
        }
        Map<Long, Integer> groupCompositeLevelMap = agentSchoolLevelSupport.fetchTargetCompositeSchoolLevel(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, schoolLevelFlag);
        Map<Integer, List<Long>> levelGroupListMap = new HashMap<>();
        groupCompositeLevelMap.forEach((k, v) -> {
            List<Long> groupList = levelGroupListMap.get(v);
            if(CollectionUtils.isEmpty(groupList)){
                groupList = new ArrayList<>();
                levelGroupListMap.put(v, groupList);
            }
            groupList.add(k);
        });

        levelGroupListMap.forEach((k, v) -> indicatorWithBudgetMap.putAll(onlineIndicatorService.loadGroupSumDataWithBudget(v, day, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(k))));
        return indicatorWithBudgetMap;
    }

    @Override
    public Map<Long, SumOnlineIndicatorWithBudget> generateUserDataList(Collection<Long> userIds, Integer day, Integer schoolLevelFlag) {
        Map<Long, SumOnlineIndicatorWithBudget> indicatorWithBudgetMap = new HashMap<>();
        if(CollectionUtils.isEmpty(userIds)){
            return indicatorWithBudgetMap;
        }
        Map<Long, Integer> groupCompositeLevelMap = agentSchoolLevelSupport.fetchTargetCompositeSchoolLevel(userIds, AgentConstants.INDICATOR_TYPE_USER, schoolLevelFlag);
        Map<Integer, List<Long>> levelGroupListMap = new HashMap<>();
        groupCompositeLevelMap.forEach((k, v) -> {
            List<Long> groupList = levelGroupListMap.get(v);
            if(CollectionUtils.isEmpty(groupList)){
                groupList = new ArrayList<>();
                levelGroupListMap.put(v, groupList);
            }
            groupList.add(k);
        });

        levelGroupListMap.forEach((k, v) -> indicatorWithBudgetMap.putAll(onlineIndicatorService.loadUserSumDataWithBudget(v, day, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(k))));
        return indicatorWithBudgetMap;
    }

    @Override
    public SumOnlineIndicatorWithBudget generateUnallocatedData(Long groupId, Integer day, Integer schoolLevelFlag) {
        List<Integer> schoolLevelList = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(groupId, AgentConstants.INDICATOR_TYPE_GROUP, schoolLevelFlag);
        return onlineIndicatorService.loadGroupUnallocatedSumDataWithBudget(groupId, day, schoolLevelList);
    }

    @Override
    public Collection<Long> fetchSchoolList(Long id, Integer dataType, Integer schoolLevelFlag) {
        List<Long> schoolIds = new ArrayList<>();
        if (Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)) {
            List<Integer> schoolLevels = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, AgentConstants.INDICATOR_TYPE_USER, schoolLevelFlag);
            if(CollectionUtils.isNotEmpty(schoolLevels)){
                List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolByUser(id);
                List<Long> tmpSchoolIds = userSchools.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toList());
                schoolIds = baseOrgService.getSchoolListByLevels(tmpSchoolIds, schoolLevels.stream().map(SchoolLevel::safeParse).collect(Collectors.toList()));
            }

        } else if (Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_UNALLOCATED)) {
            List<Integer> schoolLevels = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, AgentConstants.INDICATOR_TYPE_GROUP, schoolLevelFlag);
            if(CollectionUtils.isNotEmpty(schoolLevels)){
                List<Long> tmpSchoolIds = baseOrgService.getCityManageOtherSchoolByGroupId(id).stream().collect(Collectors.toList());
                schoolIds = baseOrgService.getSchoolListByLevels(tmpSchoolIds, schoolLevels.stream().map(SchoolLevel::safeParse).collect(Collectors.toList()));
            }
        }
        return schoolIds;
    }
}
