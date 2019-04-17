package com.voxlearning.utopia.agent.service.mobile.workrecord;


import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.AgentTodayIntoSchoolView;
import com.voxlearning.utopia.agent.view.BaseTodayIntoSchoolView;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by yaguang.wang
 * on 2017/10/9.
 */
@Named
public class IntoSchoolTodayService {
    @Inject private WorkRecordService workRecordService;
    @Inject private BaseOrgService baseOrgService;

    // fixme 无奈之举 效率太低
    public Map<String, List<AgentTodayIntoSchoolView>> generateGroupCategoryMap(Long groupId, Map<Long, Set<Long>> bdSchoolMap) {
        Map<String, List<AgentTodayIntoSchoolView>> result = new HashMap<>();
        Map<Long, List<CrmWorkRecord>> todayIntoSchoolRecordsMap = workRecordService.loadIntoSchoolRecordsMapByTime(bdSchoolMap, DayRange.current().getStartDate(), DayRange.current().getEndDate());
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if (group != null) {
            // 获取该部门下所有的子部门
            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId);
            // 按照部门角色分组
            Map<AgentGroupRoleType, List<AgentGroup>> roleGroupMap = subGroupList.stream().collect(Collectors.groupingBy(AgentGroup::fetchGroupRoleType, Collectors.toList()));
            if (group.fetchGroupRoleType() == AgentGroupRoleType.Country) {
                if (roleGroupMap.containsKey(AgentGroupRoleType.Region)) {
                    List<AgentGroup> agentGroups = roleGroupMap.get(AgentGroupRoleType.Region);
                    result.put("Region", createGroupDateList(agentGroups, todayIntoSchoolRecordsMap, bdSchoolMap.keySet()));
                }
            }
            if (group.fetchGroupRoleType() == AgentGroupRoleType.Country || group.fetchGroupRoleType() == AgentGroupRoleType.Region) {
                if (roleGroupMap.containsKey(AgentGroupRoleType.City)) {
                    List<AgentGroup> agentGroups = roleGroupMap.get(AgentGroupRoleType.City);
                    result.put("City", createGroupDateList(agentGroups, todayIntoSchoolRecordsMap, bdSchoolMap.keySet()));
                }
            }
            result.put("Bd", generateUserDataList(bdSchoolMap.keySet(), todayIntoSchoolRecordsMap));
        }
        return result;
    }

    private List<AgentTodayIntoSchoolView> createGroupDateList(List<AgentGroup> agentGroups, Map<Long, List<CrmWorkRecord>> todayIntoSchoolRecordsMap, Set<Long> bdIds) {
        if (CollectionUtils.isEmpty(agentGroups)) {
            return Collections.emptyList();
        }
        List<AgentTodayIntoSchoolView> result = new ArrayList<>();
        agentGroups.forEach(p -> {
            AgentTodayIntoSchoolView view = initAgentTodayIntoSchoolView(workRecordService.loadGroupBdWorkRecords(p, todayIntoSchoolRecordsMap), p, bdIds);
            if (view != null) {
                result.add(view);
            }
        });
        return result;
    }

    /**
     * 按部门统计每个部门的数据
     *
     * @param bdWorkRecordMap 每个部门下专员的数据
     * @return 这个部门下专员的聚合
     */
    private AgentTodayIntoSchoolView initAgentTodayIntoSchoolView(Map<Long, List<CrmWorkRecord>> bdWorkRecordMap, AgentGroup group, Set<Long> bdIds) {
        if (bdWorkRecordMap == null || group == null) {
            return null;
        }
        Set<Long> validBd = bdWorkRecordMap.keySet().stream().filter(bdIds::contains).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(validBd)) {
            return null;
        }
        List<CrmWorkRecord> todayBdIntoSchoolRecords = bdWorkRecordMap.values().stream().filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
        Double intoSchoolCountAvg = validBd.size() == 0 ? 0.0 : MathUtils.doubleDivide(todayBdIntoSchoolRecords.size(), validBd.size(), 1);
        List<Long> allTeacherIds = workRecordService.allTeacherIds(todayBdIntoSchoolRecords);
        // 人均单校拜访老师
        Double visitTeacherAvg = todayBdIntoSchoolRecords.size() == 0 ? 0.0 : MathUtils.doubleDivide(allTeacherIds.size(), todayBdIntoSchoolRecords.size(), 1);
        return new AgentTodayIntoSchoolView(group.getGroupName(), group.getId(),  "into_school_statistics.vpage?groupId=" + group.getId(), intoSchoolCountAvg, visitTeacherAvg);
    }


    private List<AgentTodayIntoSchoolView> generateUserDataList(Set<Long> bdIds, Map<Long, List<CrmWorkRecord>> bdWorkRecordMap) {
        List<AgentTodayIntoSchoolView> result = new ArrayList<>();
        Map<Long, BaseTodayIntoSchoolView> bdViews = mapBdViewsByWorkRecord(bdWorkRecordMap);
        List<AgentUser> users = baseOrgService.getUsers(bdIds);
        Map<Long, AgentUser> userMap = users.stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
        bdIds.forEach(p -> {
            if (userMap.containsKey(p)) {
                AgentUser user = userMap.get(p);
                AgentTodayIntoSchoolView view = new AgentTodayIntoSchoolView(user.getRealName(), user.getId(),  "into_school_result.vpage?userId=" + p, bdViews.get(p));
                result.add(view);
            }
        });
        return result;
    }

    private Map<Long, BaseTodayIntoSchoolView> mapBdViewsByWorkRecord(Map<Long, List<CrmWorkRecord>> bdWorkRecordMap) {
        Map<Long, BaseTodayIntoSchoolView> result = new HashMap<>();
        bdWorkRecordMap.forEach((k, v) -> {
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            BaseTodayIntoSchoolView bdTodayInto = new BaseTodayIntoSchoolView();
            bdTodayInto.setIntoSchoolCount(v.size());
            List<Long> teacherIds = workRecordService.allTeacherIds(v);
            bdTodayInto.setVisitTeacherAvg(v.size() == 0 ? 0.0 : MathUtils.doubleDivide(teacherIds.size(), v.size(), 1));
            result.put(k, bdTodayInto);
        });
        return result;
    }
}
