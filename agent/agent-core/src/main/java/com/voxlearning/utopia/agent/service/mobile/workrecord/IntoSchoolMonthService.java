package com.voxlearning.utopia.agent.service.mobile.workrecord;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.AgentIntoSchoolStatisticsView;
import com.voxlearning.utopia.agent.view.BaseIntoSchoolStatisticsView;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmTeacherVisitInfo;
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
public class IntoSchoolMonthService {
    @Inject private WorkRecordService workRecordService;
    @Inject private BaseOrgService baseOrgService;

    public Map<String, List<AgentIntoSchoolStatisticsView>> generateGroupCategoryMap(Long groupId, Map<Long, Set<Long>> bdSchoolMap) {
        Map<String, List<AgentIntoSchoolStatisticsView>> result = new HashMap<>();
        Map<Long, List<CrmWorkRecord>> mouthIntoSchoolRecordsMap = workRecordService.loadIntoSchoolRecordsMapByTime(bdSchoolMap, MonthRange.current().getStartDate(), new Date());
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if (group != null) {
            // 获取该部门下所有的子部门
            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId);
            // 按照部门角色分组
            Map<AgentGroupRoleType, List<AgentGroup>> roleGroupMap = subGroupList.stream().collect(Collectors.groupingBy(AgentGroup::fetchGroupRoleType, Collectors.toList()));
            if (group.fetchGroupRoleType() == AgentGroupRoleType.Country) {
                if (roleGroupMap.containsKey(AgentGroupRoleType.Region)) {
                    List<AgentGroup> agentGroups = roleGroupMap.get(AgentGroupRoleType.Region);
                    result.put("Region", createGroupDateList(agentGroups, mouthIntoSchoolRecordsMap, bdSchoolMap.keySet()));
                }
            }
            if (group.fetchGroupRoleType() == AgentGroupRoleType.Country || group.fetchGroupRoleType() == AgentGroupRoleType.Region) {
                if (roleGroupMap.containsKey(AgentGroupRoleType.City)) {
                    List<AgentGroup> agentGroups = roleGroupMap.get(AgentGroupRoleType.City);
                    result.put("City", createGroupDateList(agentGroups, mouthIntoSchoolRecordsMap, bdSchoolMap.keySet()));
                }
            }
            result.put("Bd", generateUserDataList(bdSchoolMap.keySet(), mouthIntoSchoolRecordsMap, bdSchoolMap));
        }
        return result;
    }

    private List<AgentIntoSchoolStatisticsView> createGroupDateList(List<AgentGroup> agentGroups, Map<Long, List<CrmWorkRecord>> mouthIntoSchoolRecordsMap, Set<Long> bdIds) {
        if (CollectionUtils.isEmpty(agentGroups)) {
            return Collections.emptyList();
        }
        List<AgentIntoSchoolStatisticsView> result = new ArrayList<>();
        agentGroups.forEach(p -> {
            AgentIntoSchoolStatisticsView view = initAgentMonthIntoSchoolView(workRecordService.loadGroupBdWorkRecords(p, mouthIntoSchoolRecordsMap), p, bdIds);
            if (view != null) {
                result.add(view);
            }
        });
        return result;
    }

    private AgentIntoSchoolStatisticsView initAgentMonthIntoSchoolView(Map<Long, List<CrmWorkRecord>> bdWorkRecordMap, AgentGroup group, Set<Long> bdIds) {
        if (bdWorkRecordMap == null || group == null) {
            return null;
        }
        Set<Long> validBd = bdWorkRecordMap.keySet().stream().filter(bdIds::contains).collect(Collectors.toSet());
        if(CollectionUtils.isEmpty(validBd)){
            return null;
        }
        List<CrmWorkRecord> monthCrmWorkRecords = bdWorkRecordMap.values().stream().filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
        Integer totalIntoCount = monthCrmWorkRecords.size();
        Double intoSchoolCountAvg = validBd.size() == 0 ? 0.0 : MathUtils.doubleDivide(totalIntoCount, validBd.size(), 1);
        Map<Long, Date> teacherWorkMap = workRecordService.loadMapTeacher(monthCrmWorkRecords);
        // 布置作业的老师ID
        Set<Long> teacherHwCount = workRecordService.loadTeacherIdHw(teacherWorkMap);
        List<Long> allTeacherIds = workRecordService.allTeacherIds(monthCrmWorkRecords);
        Double visitTeacherAvg = monthCrmWorkRecords.size() == 0 ? 0.0 : MathUtils.doubleDivide(allTeacherIds.size(), monthCrmWorkRecords.size(), 1);
        String visitTeacherHwPro = teacherWorkMap.keySet().size() == 0 ? "0" : SafeConverter.toString( MathUtils.doubleDivide(teacherHwCount.size() * 100,teacherWorkMap.keySet().size()), "0");
        return new AgentIntoSchoolStatisticsView(group.getGroupName(), group.getId(), "into_school_statistics.vpage?groupId=" + group.getId(), intoSchoolCountAvg, visitTeacherAvg, visitTeacherHwPro);
    }


    private List<AgentIntoSchoolStatisticsView> generateUserDataList(Set<Long> bdIds, Map<Long, List<CrmWorkRecord>> bdWorkRecordMap, Map<Long, Set<Long>> bdSchoolMap) {
        List<AgentIntoSchoolStatisticsView> result = new ArrayList<>();
        Map<Long, BaseIntoSchoolStatisticsView> bdViews = mapBdViewsByWorkRecord(bdWorkRecordMap, bdSchoolMap);
        List<AgentUser> users = baseOrgService.getUsers(bdIds);
        Map<Long, AgentUser> userMap = users.stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
        bdIds.forEach(p -> {
            if (userMap.containsKey(p)) {
                AgentUser user = userMap.get(p);
                AgentIntoSchoolStatisticsView view = new AgentIntoSchoolStatisticsView(user.getRealName(), user.getId(), "into_school_result.vpage?userId=" + p, bdViews.get(p));
                if (CollectionUtils.isNotEmpty(bdSchoolMap.get(p))) {
                    view.setSchoolTotal(bdSchoolMap.get(p).size());
                }
                result.add(view);
            }
        });
        return result;
    }

    private Map<Long, BaseIntoSchoolStatisticsView> mapBdViewsByWorkRecord(Map<Long, List<CrmWorkRecord>> bdWorkRecordMap, Map<Long, Set<Long>> bdSchoolMap) {
        Map<Long, BaseIntoSchoolStatisticsView> result = new HashMap<>();
        bdWorkRecordMap.forEach((k, v) -> {
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            BaseIntoSchoolStatisticsView bdInfo = new BaseIntoSchoolStatisticsView();
            bdInfo.setIntoSchoolCount(v.size());
            Set<Long> bdManagerSchoolIds = bdSchoolMap.get(k);
            if (CollectionUtils.isEmpty(bdManagerSchoolIds)) {
                bdManagerSchoolIds = new HashSet<>();
            }
            // 已访问学校数
            bdInfo.setVisitedSchoolCount(v.stream().map(CrmWorkRecord::getSchoolId).filter(bdManagerSchoolIds::contains).collect(Collectors.toSet()).size());
            Map<Long, Date> teacherWorkMap = workRecordService.loadMapTeacher(v);
            // 布置作业的老师ID
            Set<Long> teacherHwCount = workRecordService.loadTeacherIdHw(teacherWorkMap);
            Integer teacherSize = 0;
            for (CrmWorkRecord p : v) {
                List<CrmTeacherVisitInfo> visitTeacherList = p.getVisitTeacherList();
                if (CollectionUtils.isNotEmpty(visitTeacherList)) {
                    teacherSize += SafeConverter.toInt(visitTeacherList.stream().filter(CrmTeacherVisitInfo::isRealTeacher).count());
                }
            }
            // 校均拜访老师数
            bdInfo.setVisitTeacherAvg(v.size() == 0 ? 0.0 : MathUtils.doubleDivide(teacherSize, v.size(), 1));
            // 老师布置作业率
            bdInfo.setVisitTeacherHwPro(teacherWorkMap.keySet().size() == 0 ? "0" : SafeConverter.toString( MathUtils.doubleDivide(teacherHwCount.size() * 100, teacherWorkMap.keySet().size()), "0"));
            result.put(k, bdInfo);
        });
        return result;
    }
}
