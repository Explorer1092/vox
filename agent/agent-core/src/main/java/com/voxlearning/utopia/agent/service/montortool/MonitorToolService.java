package com.voxlearning.utopia.agent.service.montortool;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.monitor.ClassAlterTableData;
import com.voxlearning.utopia.agent.mapper.ClazzAlterMapper;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceMapperService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.ClazzTeacherAlteration;
import com.voxlearning.utopia.service.user.consumer.ClazzTeacherAlterationLoaderClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.calculateDateDay;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by yaguang.wang
 * on 2017/8/1.
 */
@Named
public class MonitorToolService extends AbstractAgentService {

    @Inject private ClazzTeacherAlterationLoaderClient clazzTeacherAlterationLoaderClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    // local
    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentResourceMapperService agentResourceMapperService;
    @Inject private AgentDictSchoolService agentDictSchoolService;
    @Inject private BaseDictService baseDictService;
    @Inject private AgentGroupSupport agentGroupSupport;

    public List<ClazzAlterMapper> loadBeginSomeDayClassAlterData(Long time, Integer startDay) {
        DayRange dayRange;
        if (time == null) {
            dayRange = DayRange.current();
        } else {
            dayRange = DayRange.newInstance(time);
        }
        Date endDate = dayRange.previous().getEndDate();
        Date startDate = calculateDateDay(dayRange.previous().getStartDate(), -startDay);
        Date today = DayRange.current().getStartDate();
        Date yesterday = DayRange.current().previous().getStartDate();
        List<ClazzAlterMapper> mapperList = clazzTeacherAlterationLoaderClient.loadClazzTeacherAlterationByCreateTime(startDate, endDate)
                .stream()
                .filter(p -> p.getState() != ClazzTeacherAlterationState.CANCELED)
                .filter(p -> p.getState() == ClazzTeacherAlterationState.PENDING || (p.getCreateDatetime().after(yesterday) && p.getState() != ClazzTeacherAlterationState.PENDING && p.getUpdateDatetime().after(today)))
                .filter(p -> p.getType() == ClazzTeacherAlterationType.TRANSFER || p.getType() == ClazzTeacherAlterationType.REPLACE || p.getType() == ClazzTeacherAlterationType.LINK)
                .map(this::clazzTeacherAlterationMap).collect(toList());
        // 过滤掉假老师的申请
        Set<Long> teacherIdSet = new HashSet<>();
        teacherIdSet.addAll(mapperList.stream().map(ClazzAlterMapper::getApplicantId).collect(Collectors.toList()));
        teacherIdSet.addAll(mapperList.stream().map(ClazzAlterMapper::getRespondentId).collect(Collectors.toList()));
        Map<Long, CrmTeacherSummary> teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacherIdSet);
        teacherSummary = teacherSummary.values().stream().filter(p -> p != null && p.getManualFakeTeacher()).collect(Collectors.toMap(CrmTeacherSummary::getTeacherId, Function.identity()));
        //Map<Long, Map<String, String>> teacherMap = agentResourceMapperService.simpleFindTeacher(teacherIds);
        return agentResourceMapperService.mapClazzAlteration(mapperList, teacherSummary);
    }




    public List<ClassAlterTableData> exportClazzAlter() {
        List<ClassAlterTableData> result = new ArrayList<>();
        List<ClazzAlterMapper> mapperList = loadBeginSomeDayClassAlterData(null, 10);
        // 昨天的开始
        Date today = DayRange.current().getStartDate();
        Date yesterday = DayRange.current().previous().getStartDate();
        // 过滤掉字典表之后结果根据学校分组
        Map<Long, List<ClazzAlterMapper>> schoolMapper = mapperList
                .stream()
                .filter(mapper -> baseDictService.isDictSchool(mapper.getSchoolId()))
                .collect(Collectors.groupingBy(ClazzAlterMapper::getSchoolId));

        // 根据学校找到对应专员
        Map<Long, List<AgentUserSchool>> schoolUsers = baseOrgService.getUserSchoolBySchools(schoolMapper.keySet())
                .values().stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(AgentUserSchool::getUserId));
        String nowDate = DateUtils.getTodaySqlDate();
        Set<Long> allBdSchoolIds = new HashSet<>();
        // 整合信息
        for (Map.Entry<Long, List<AgentUserSchool>> entry : schoolUsers.entrySet()) {
            Long userId = entry.getKey();
            Set<Long> schools = entry.getValue().stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(schools)) {
                continue;
            }
            allBdSchoolIds.addAll(schools);
            List<ClazzAlterMapper> userAlters = schoolClazzAlterMapper(schools, schoolMapper);
            if (CollectionUtils.isEmpty(userAlters)) {
                continue;
            }
            // 待处理的数量
            List<ClazzAlterMapper> pendingAlter = userAlters.stream().filter(p -> p.getState() == ClazzTeacherAlterationState.PENDING).collect(toList());
            // 昨天创建的
            List<ClazzAlterMapper> yesterdayIncrease = userAlters.stream().filter(p -> p.getCreateTime().after(yesterday)
                    && (p.getState() == ClazzTeacherAlterationState.PENDING || (p.getState() != ClazzTeacherAlterationState.PENDING && p.getUpdateTime().after(today)))).collect(toList());
            AgentGroup bdGroup = baseOrgService.getUserGroupsFirstOne(userId, AgentRoleType.BusinessDeveloper);
            if (bdGroup == null) {
                continue;
            }
            AgentGroup pBdGroup = baseOrgService.getGroupById(bdGroup.getParentId());
            if (pBdGroup == null) {
                continue;
            }
            ClassAlterTableData data = new ClassAlterTableData();
            AgentUser agentUser = baseOrgService.getUser(userId);
            data.setDate(nowDate);
            data.setExecutor(agentUser.getRealName());
            data.setRole(AgentRoleType.BusinessDeveloper.getRoleName());
            data.setUntreated(pendingAlter.size());
            data.setDepartmentName(bdGroup.getGroupName());
            data.setRegionName(pBdGroup.getGroupName());
            data.setYesterdayIncrease(yesterdayIncrease.size());
            result.add(data);
        }
        Set<Long> allCmSchoolIds = schoolMapper.keySet().stream().filter(s -> !allBdSchoolIds.contains(s)).collect(Collectors.toSet());

        Map<Long, List<ClazzAlterMapper>> userAlterMap = new HashMap<>();
        Map<Long, Long> userGroupMap = new HashMap<>();
        for(Long schoolId : allCmSchoolIds){
            // 获取负责该学校的分区列表
            List<Long> groupIds = agentGroupSupport.getGroupIdsBySchool(schoolId, Collections.singletonList(AgentGroupRoleType.City));
            if(CollectionUtils.isNotEmpty(groupIds)){
                Long cityManager = baseOrgService.getGroupManager(groupIds.get(0));
                if(cityManager != null){
                    userGroupMap.put(cityManager, groupIds.get(0));
                    List<ClazzAlterMapper> schoolAlterList = schoolMapper.get(schoolId);
                    if(CollectionUtils.isEmpty(schoolAlterList)){
                        continue;
                    }
                    List<ClazzAlterMapper> userAlterList = userAlterMap.get(cityManager);
                    if(CollectionUtils.isEmpty(userAlterList)){
                        userAlterList = new ArrayList<>();
                        userAlterMap.put(cityManager, userAlterList);
                    }
                    userAlterList.addAll(schoolAlterList);
                }
            }
        }

        Map<Long, AgentUser> userMap = baseOrgService.getUsers(userAlterMap.keySet()).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
        Map<Long, AgentGroup> groupMap = baseOrgService.getGroupByIds(userGroupMap.values()).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
        if(MapUtils.isNotEmpty(groupMap)){
            Map<Long, AgentGroup> pgroupMap = baseOrgService.getGroupByIds(groupMap.values().stream().map(AgentGroup::getParentId).collect(Collectors.toList())).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
            if(MapUtils.isNotEmpty(pgroupMap)){
                groupMap.putAll(pgroupMap);
            }
        }


        for(Long userId : userAlterMap.keySet()){
            List<ClazzAlterMapper> cmSchoolClazzAlter = userAlterMap.get(userId);
            List<ClazzAlterMapper> pendingAlter = cmSchoolClazzAlter.stream().filter(p -> p.getState() == ClazzTeacherAlterationState.PENDING).collect(toList());
            List<ClazzAlterMapper> yesterdayIncrease = cmSchoolClazzAlter.stream().filter(p -> p.getCreateTime().after(yesterday)
                    && (p.getState() == ClazzTeacherAlterationState.PENDING || (p.getState() != ClazzTeacherAlterationState.PENDING && p.getUpdateTime().after(today)))).collect(toList());
            ClassAlterTableData data = new ClassAlterTableData();
            data.setDate(nowDate);
            Long groupId = userGroupMap.get(userId);
            AgentGroup group = groupMap.get(groupId);
            if(group != null){
                data.setDepartmentName(group.getGroupName());
                AgentGroup pgroup = groupMap.get(group.getParentId());
                if(pgroup != null){
                    data.setRegionName(pgroup.getGroupName());
                }
            }
            AgentUser user = userMap.get(userId);
            if(user != null){
                data.setExecutor(user.getRealName());
            }
            data.setRole(AgentRoleType.CityManager.getRoleName());
            data.setUntreated(pendingAlter.size());
            data.setYesterdayIncrease(yesterdayIncrease.size());
            result.add(data);
        }
        return result;
    }

    private List<ClazzAlterMapper> schoolClazzAlterMapper(Set<Long> schools, Map<Long, List<ClazzAlterMapper>> schoolMapper) {
        List<ClazzAlterMapper> userAlters = new ArrayList<>();
        schools.forEach(p -> {
            List<ClazzAlterMapper> clazzAlterMappers = schoolMapper.get(p);
            if (CollectionUtils.isNotEmpty(clazzAlterMappers)) {
                userAlters.addAll(clazzAlterMappers);
            }
        });
        return userAlters;
    }

    private ClazzAlterMapper clazzTeacherAlterationMap(ClazzTeacherAlteration clazzTeacherAlteration) {
        ClazzAlterMapper mapper = new ClazzAlterMapper();
        mapper.setRecordId(clazzTeacherAlteration.getId());
        mapper.setSchoolId(clazzTeacherAlteration.getSchoolId());
        mapper.setClazzId(clazzTeacherAlteration.getClazzId());
        mapper.setApplicantId(clazzTeacherAlteration.getApplicantId());
        mapper.setRespondentId(clazzTeacherAlteration.getRespondentId());
        mapper.setType(clazzTeacherAlteration.getType());
        mapper.setState(clazzTeacherAlteration.getState());
        mapper.setCcProcessState(clazzTeacherAlteration.getCcProcessState());
        mapper.setCreateTime(clazzTeacherAlteration.getCreateDatetime());
        mapper.setUpdateTime(clazzTeacherAlteration.getUpdateDatetime());
        mapper.setUpdateTimeLong(SafeConverter.toLong(clazzTeacherAlteration.getUpdateDatetime().getTime()));
        mapper.setCreateTimeLong(SafeConverter.toLong(clazzTeacherAlteration.getCreateDatetime().getTime()));
        return mapper;
    }
}
