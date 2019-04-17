/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.service.mobile.v2;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.VisitSchoolResultDetailData;
import com.voxlearning.utopia.agent.bean.WorkRecordListData;
import com.voxlearning.utopia.agent.bean.WorkRecordMiddleSortData;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.constants.MemorandumType;
import com.voxlearning.utopia.agent.constants.ResearchersJobType;
import com.voxlearning.utopia.agent.dao.mongo.SchoolDayIncreaseDataDao;
import com.voxlearning.utopia.agent.dao.mongo.workload.AgentRecordWorkloadDao;
import com.voxlearning.utopia.agent.persist.AgentOuterResourcePersistence;
import com.voxlearning.utopia.agent.persist.entity.*;
import com.voxlearning.utopia.agent.persist.entity.daily.AgentDailyScoreStatistics;
import com.voxlearning.utopia.agent.persist.entity.organization.AgentOuterResource;
import com.voxlearning.utopia.agent.persist.entity.workload.AgentRecordWorkload;
import com.voxlearning.utopia.agent.service.common.BaseGroupService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.daily.AgentDailyScoreStatisticsService;
import com.voxlearning.utopia.agent.service.memorandum.AgentMemorandumService;
import com.voxlearning.utopia.agent.service.mobile.AgentHiddenTeacherService;
import com.voxlearning.utopia.agent.service.mobile.AgentResearchersService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.mobile.SchoolClueService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentRegisterTeacherStatisticsService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentWorkRecordStatisticsService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.WorkRecordDataCompatibilityService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.organization.AgentOuterResourceService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.service.signin.SignInService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.*;
import com.voxlearning.utopia.agent.view.workrecord.WrStatisticsOverviewRoleData;
import com.voxlearning.utopia.api.constant.Subjects;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.agent.evaluate.EvaluationRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.signin.SignInRecordLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.work.*;
import com.voxlearning.utopia.service.crm.consumer.service.agent.evaluate.EvaluationRecordServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.work.*;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkCrmLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/10/10
 */
@Slf4j
@Named
public class WorkRecordService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;
    @Inject private BaseUserService baseUserService;
    @Inject private BaseOrgService baseOrgService;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private SchoolDayIncreaseDataDao schoolDayIncreaseDataDao;
    @Inject private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject private NewHomeworkCrmLoaderClient newHomeworkCrmLoaderClient;
    @Inject private PerformanceService performanceService;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private AgentResearchersService agentResearchersService;
    @Inject private BaseGroupService baseGroupService;
    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    @ImportService(interfaceClass = CrmSummaryService.class) private CrmSummaryService crmSummaryService;

    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private AgentHiddenTeacherService agentHiddenTeacherService;
    @Inject private AgentWorkRecordStatisticsService agentWorkRecordStatisticsService;
    @Inject private AgentRecordWorkloadDao agentRecordWorkloadDao;
    @Inject
    private AgentDailyScoreStatisticsService agentDailyScoreStatisticsService;
    @Inject
    private AgentRegisterTeacherStatisticsService agentRegisterTeacherStatisticsService;
    @Inject
    private SearchService searchService;
    @Inject
    private SignInService signInService;
    @Inject
    private TeacherResourceService teacherResourceService;
    @Inject
    private WorkRecordTeacherServiceClient workRecordTeacherServiceClient;
    @Inject
    private WorkRecordSchoolServiceClient workRecordSchoolServiceClient;
    @Inject
    private WorkSupporterServiceClient workSupporterServiceClient;
    @Inject
    private WorkRecordMeetingServiceClient workRecordMeetingServiceClient;
    @Inject
    private WorkRecordMeetingLoaderClient workRecordMeetingLoaderClient;
    @Inject
    private WorkRecordSchoolLoaderClient workRecordSchoolLoaderClient;
    @Inject
    private WorkRecordAccompanyServiceClient workRecordAccompanyServiceClient;
    @Inject
    private EvaluationRecordServiceClient evaluationRecordServiceClient;
    @Inject
    private AgentOuterResourcePersistence agentOuterResourcePersistence;
    @Inject
    private AgentOuterResourceService agentOuterResourceService;
    @Inject
    private WorkRecordOuterResourceServiceClient workRecordOuterResourceServiceClient;
    @Inject
    private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject
    private WorkRecordOuterResourceLoaderClient workRecordOuterResourceLoaderClient;
    @Inject
    private WorkRecordTeacherLoaderClient workRecordTeacherLoaderClient;
    @Inject
    private WorkRecordResourceExtensionLoaderClient workRecordResourceExtensionLoaderClient;
    @Inject
    private WorkRecordResourceExtensionServiceClient workRecordResourceExtensionServiceClient;
    @Inject
    private SignInRecordLoaderClient signInRecordLoaderClient;
    @Inject
    private SchoolClueService schoolClueService;
    @Inject
    private WorkRecordDataCompatibilityService workRecordDataCompatibilityService;
    @Inject
    private WorkRecordAccompanyLoaderClient workRecordAccompanyLoaderClient;
    @Inject
    private AgentNotifyService agentNotifyService;
    @Inject
    private AgentMemorandumService agentMemorandumService;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    public static final Map<Long, String> SCHOOL_MASTER_INFO = new HashMap<>();
    private static final String DATE_FORMAT = "yyyyMMdd";

    static {
        SCHOOL_MASTER_INFO.put(100L, "校长");
        SCHOOL_MASTER_INFO.put(190L, "其他");
        SCHOOL_MASTER_INFO.put(290L, "教务主任");
    }

    private static List<Subject> NON_BASEIC_SUBJECTS = Subjects.ALL_SUBJECTS.stream().filter(e -> !Subjects.BASIC_SUBJECTS.contains(e)).collect(Collectors.toList());

    // 过滤当前分组下的进校记录
    public Map<Long, List<CrmWorkRecord>> loadGroupBdWorkRecords(AgentGroup group, Map<Long, List<CrmWorkRecord>> todayIntoSchoolRecordsMap) {
        if (group == null) {
            return Collections.emptyMap();
        }
        Map<Long, List<CrmWorkRecord>> result = new HashMap<>();
        Set<Long> bdIds = baseOrgService.loadGroupUserByGroupId(group.getId(), AgentRoleType.BusinessDeveloper);
        if (CollectionUtils.isEmpty(bdIds)) {
            return null;
        }
        bdIds.forEach(p -> {
            if (p != null) {
                result.put(p, todayIntoSchoolRecordsMap.get(p));
            }
        });
        return result;
    }

    // 根据专员和学校和时间获取进校记录
    public Map<Long, List<CrmWorkRecord>> loadIntoSchoolRecordsMapByTime(Map<Long, Set<Long>> bdSchoolMap, Date startDate, Date endDate) {
        Map<Long, List<CrmWorkRecord>> result = new HashMap<>();
        List<CrmWorkRecord> crmWorkRecords = new ArrayList<>();
        AgentResourceService.batchIds(bdSchoolMap.keySet(), 100).forEach((k, v) -> {
            List<CrmWorkRecord> tempList = crmWorkRecordLoaderClient.listByWorkersAndType(v, CrmWorkRecordType.SCHOOL, startDate, endDate);
            crmWorkRecords.addAll(tempList);
        });
        Map<Long, List<CrmWorkRecord>> tempMap = crmWorkRecords.stream().collect(Collectors.groupingBy(CrmWorkRecord::getWorkerId));
        tempMap.forEach((k, v) -> {
            Set<Long> bdManageJuniorIds = bdSchoolMap.get(k);
            Set<Long> schoolIds = v.stream().map(CrmWorkRecord::getSchoolId).filter(p -> Objects.nonNull(p) && bdManageJuniorIds.contains(p)).collect(Collectors.toSet());
            Set<Long> unManageSchoolIds = v.stream().map(CrmWorkRecord::getSchoolId).filter(p -> Objects.nonNull(p) && !bdManageJuniorIds.contains(p)).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(unManageSchoolIds)) {
                Map<Long, School> schoolMap = raikouSystem.loadSchools(unManageSchoolIds);
                Set<Long> unManageJuniorSchoolIds = schoolMap.values().stream().filter(School::isPrimarySchool).map(School::getId).collect(Collectors.toSet());
                schoolIds.addAll(unManageJuniorSchoolIds);
            }
            List<CrmWorkRecord> tempList = v.stream().filter(p -> Objects.nonNull(p.getSchoolId()) && schoolIds.contains(p.getSchoolId())).collect(Collectors.toList());
            result.put(k, tempList);
        });
        return result;
    }

    // 查询专员这段时间内的小学进校数据
    public List<CrmWorkRecord> loadBdJuniorIntoSchoolRecord(Long bdId, Collection<Long> bdManageJuniorIds, Date startDate, Date endDate) {
        //List<CrmWorkRecord> workerWorkRecords = getWorkerWorkRecords(bdId, CrmWorkRecordType.SCHOOL, startDate, endDate);
        List<CrmWorkRecord> workerWorkRecords = crmWorkRecordLoaderClient.listByWorkerAndType(bdId, CrmWorkRecordType.SCHOOL, startDate, endDate);
        Set<Long> schoolIds = workerWorkRecords.stream().map(CrmWorkRecord::getSchoolId).filter(p -> Objects.nonNull(p) && bdManageJuniorIds.contains(p)).collect(Collectors.toSet());
        Set<Long> unManageSchoolIds = workerWorkRecords.stream().map(CrmWorkRecord::getSchoolId).filter(p -> Objects.nonNull(p) && !bdManageJuniorIds.contains(p)).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(unManageSchoolIds)) {
            Map<Long, School> schoolMap = raikouSystem.loadSchools(unManageSchoolIds);
            Set<Long> unManageJuniorSchoolIds = schoolMap.values().stream().filter(School::isPrimarySchool).map(School::getId).collect(Collectors.toSet());
            schoolIds.addAll(unManageJuniorSchoolIds);
        }
        return workerWorkRecords.stream().filter(p -> Objects.nonNull(p.getSchoolId()) && schoolIds.contains(p.getSchoolId())).collect(Collectors.toList());
    }

    // 查询有小学的专员与学校的对应关系
    public Map<Long, Set<Long>> bdSchoolMap(Map<Long, List<AgentUserSchool>> bdSchools) {
        Map<Long, Set<Long>> bdSchoolMap = new HashMap<>();
        bdSchools.forEach((k, v) -> {
            Set<Long> schoolIds = v.stream().filter(s1 -> s1.getSchoolLevel() == SchoolLevel.JUNIOR.getLevel()).map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
            if (schoolIds.size() != 0) {
                bdSchoolMap.put(k, schoolIds);
            }
        });
        return bdSchoolMap;
    }

    // 根据用户Id获取专员的未达标信息视图
    public List<NoReachIntoSchoolView> loadNoReachIntoSchoolByUserId(Long userId, String date) {
        List<Long> managedGroupIdListByUserId = baseOrgService.getManagedGroupIdListByUserId(userId);
        if (CollectionUtils.isEmpty(managedGroupIdListByUserId)) {
            return Collections.emptyList();
        }
        return loadNoReachIntoSchoolByGroupId(managedGroupIdListByUserId.get(0), date);
    }

    // 根据部门Id获取专员的未达标信息视图
    private List<NoReachIntoSchoolView> loadNoReachIntoSchoolByGroupId(Long groupId, String date) {

        Set<Long> bdIds = baseOrgService.loadGroupUserByGroupId(groupId, AgentRoleType.BusinessDeveloper);
        Map<Long, List<AgentUserSchool>> bdSchools = baseOrgService.getUserSchoolByUsers(bdIds);
        Set<Long> validBd = new HashSet<>();
        Set<Long> finalValidBd = validBd;
        bdSchools.forEach((k, v) -> {
            Long schoolSize = v.stream().filter(s1 -> s1.getSchoolLevel() == SchoolLevel.JUNIOR.getLevel()).map(AgentUserSchool::getSchoolId).count();
            if (schoolSize != 0) {
                finalValidBd.add(k);
            }
        });
        Map<Long, BaseTodayIntoSchoolView> bdView = loadBdTodayIntoSchoolByBdIds(validBd, date);
        Map<Long, BaseTodayIntoSchoolView> isNotReachBd = new HashMap<>();
        bdView.forEach((k, v) -> {
            if (!v.isReach()) {
                isNotReachBd.put(k, v);
            }
        });
        // 未达标专员
        validBd = validBd.stream().filter(p -> !bdView.containsKey(p) || isNotReachBd.containsKey(p)).collect(Collectors.toSet());
        Map<Long, List<AgentGroupUser>> groupUser = baseGroupService.getGroupUsersByUserIds(validBd);
        Map<Long, Long> userGroupMap = new HashMap<>();
        groupUser.forEach((k, v) -> {
            if (CollectionUtils.isNotEmpty(v)) {
                userGroupMap.put(k, v.get(0).getGroupId());
            }
        });
        Map<Long, AgentGroup> groupMap = baseOrgService.getGroupByIds(new HashSet<>(userGroupMap.values())).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
        List<NoReachIntoSchoolView> result = new ArrayList<>();
        userGroupMap.forEach((k, v) -> {
            AgentUser bdUser = baseOrgService.getUser(k);
            NoReachIntoSchoolView noReachIntoSchoolView = NoReachIntoSchoolView.createNoReachIntoSchoolView(groupMap.get(v), bdUser, isNotReachBd.get(k));
            if (noReachIntoSchoolView != null) {
                result.add(noReachIntoSchoolView);
            }
        });
        return result;
    }


    // 本月的进校详情
    public List<IntoSchoolResultListView> loadIntoSchoolResultListView(List<CrmWorkRecord> workRecords) {
        if (CollectionUtils.isEmpty(workRecords)) {
            return Collections.emptyList();
        }
        List<IntoSchoolResultListView> result = new ArrayList<>();
        workRecords.forEach(p -> {
            IntoSchoolResultListView view = new IntoSchoolResultListView();
            view.setSchoolId(p.getSchoolId());
            view.setSchoolName(p.getSchoolName());
            view.setLastVisitTime(DateUtils.dateToString(p.getWorkTime(), "MM-dd"));
            view.setVisitCountLte30(p.getVisitCountLte30());
            if (CollectionUtils.isNotEmpty(p.getVisitTeacherList())) {
                view.setVisitTeacherCount(SafeConverter.toInt(p.getVisitTeacherList().stream().filter(t -> !SCHOOL_MASTER_INFO.containsKey(t.getTeacherId())).count()));
                view.setVisitOtherTeacher(p.getVisitTeacherList().stream().map(t -> SCHOOL_MASTER_INFO.get(t.getTeacherId())).filter(Objects::nonNull).collect(Collectors.toList()));
            }
            result.add(view);
        });
        return result;
    }

    // 获取访后未布置作业老师
    public List<UnAssignHomeworkTeacherView> loadBdUnAssignHwTeacher(Long bdId) {
        List<UnAssignHomeworkTeacherView> result = new ArrayList<>();
        List<CrmWorkRecord> monthWorkRecord = loadBdCrmWorkRecordAndSchoolIds(bdId, MonthRange.current().getStartDate());
        Map<Long, List<CrmWorkRecord>> workRecordSchoolMap = monthWorkRecord.stream().collect(Collectors.groupingBy(CrmWorkRecord::getSchoolId));
        Map<Long, String> schoolName = new HashMap<>();
        Map<Long, Date> schoolWorkMap = new HashMap<>();
        Map<Long, Map<String, String>> teacherInfoMap = new HashMap<>();
        monthWorkRecord.forEach(p -> {
            if (!schoolWorkMap.containsKey(p.getSchoolId())) {
                schoolWorkMap.put(p.getSchoolId(), p.getWorkTime());
                schoolName.put(p.getSchoolId(), p.getSchoolName());
            }
            List<CrmTeacherVisitInfo> visitInfo = p.getVisitTeacherList();
            visitInfo.forEach(v -> {
                if (!teacherInfoMap.containsKey(v.getTeacherId())) {
                    Map<String, String> info = new HashMap<>();
                    info.put("teacherId", SafeConverter.toString(v.getTeacherId()));
                    info.put("teacherName", v.getTeacherName());
                    teacherInfoMap.put(v.getTeacherId(), info);
                }
            });
        });
        schoolWorkMap.forEach((k, v) -> {
            List<CrmWorkRecord> schoolWorkRecords = workRecordSchoolMap.get(k);
            if (CollectionUtils.isEmpty(schoolWorkRecords)) {
                return;
            }
            Map<Long, Date> teacherWorkMap = loadMapTeacher(schoolWorkRecords);
            // 布置作业的老师ID
            Set<Long> teacherHwCount = loadTeacherIdHw(teacherWorkMap);
            // 未布置作业的老师ID
            Set<Long> teacherUnHwCount = teacherWorkMap.keySet().stream().filter(t -> !teacherHwCount.contains(t)).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(teacherUnHwCount)) {
                return;
            }
            UnAssignHomeworkTeacherView view = new UnAssignHomeworkTeacherView();
            view.setSchoolId(k);
            view.setLastVisitTime(v);
            view.setSchoolName(schoolName.get(k));
            view.setVisitTime(schoolWorkRecords.stream().map(p -> DateUtils.dateToString(p.getWorkTime(), "MM-dd")).collect(Collectors.toList()));
            // 进校时间倒序
            view.getVisitTime().sort(Comparator.reverseOrder());
            List<Map<String, String>> teacherInfo = new ArrayList<>();
            teacherUnHwCount.forEach(t -> teacherInfo.add(teacherInfoMap.get(t)));
            view.setTeacherInfo(teacherInfo.stream().filter(Objects::nonNull).collect(Collectors.toList()));
            result.add(view);
        });
        result.sort((o1, o2) -> o2.getLastVisitTime().compareTo(o1.getLastVisitTime()));
        return result;
    }

    // 获取专员小学的一段时间范围内的进校记录
    public List<CrmWorkRecord> loadBdCrmWorkRecordAndSchoolIds(Long bdId, Date startDate) {
        return loadBdCrmWorkRecordAndSchoolIds(bdId, startDate, new Date());
    }

    // 获取专员小学的一段时间范围内的进校记录
    public List<CrmWorkRecord> loadBdCrmWorkRecordAndSchoolIds(Long bdId, Date startDate, Date endDate) {
        List<Long> schoolIds = baseOrgService.getUserSchools(bdId, SchoolLevel.JUNIOR.getLevel());
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        // 本月的工作记录
        return loadBdJuniorIntoSchoolRecord(bdId, schoolIds, startDate, endDate);
    }

    // 进校当日数据 多个专员的ID
    public Map<Long, BaseTodayIntoSchoolView> loadBdTodayIntoSchoolByBdIds(Collection<Long> bdIds, String date) {
        if (CollectionUtils.isEmpty(bdIds)) {
            return Collections.emptyMap();
        }
        Map<Long, BaseTodayIntoSchoolView> result = new HashMap<>();
        bdIds.forEach(p -> {
            BaseTodayIntoSchoolView view = loadBdTodayIntoSchoolByBdId(p, date);
            if (view != null) {
                result.put(p, view);
            }
        });
        return result;
    }


    // 进校当日数据
    public BaseTodayIntoSchoolView loadBdTodayIntoSchoolByBdId(Long bdId, String date) {
        BaseTodayIntoSchoolView bdTodayInto = new BaseTodayIntoSchoolView();
        Date startDate = DateUtils.stringToDate(date, DATE_FORMAT);
        if (startDate == null) {
            startDate = new Date();
        }
        List<CrmWorkRecord> todayCrmWorkRecords = loadBdCrmWorkRecordAndSchoolIds(bdId, startDate, DayRange.newInstance(startDate.getTime()).getEndDate());
        if (CollectionUtils.isEmpty(todayCrmWorkRecords)) {
            return null;
        }
        // 进校次数
        bdTodayInto.setIntoSchoolCount(todayCrmWorkRecords.size());
        List<Long> teacherIds = allTeacherIds(todayCrmWorkRecords);
        bdTodayInto.setVisitTeacherAvg(todayCrmWorkRecords.size() == 0 ? 0.0 : MathUtils.doubleDivide(teacherIds.size(), todayCrmWorkRecords.size(), 1));
        return bdTodayInto;
    }

    // 进校拜访的非其他老师ID
    public List<Long> allTeacherIds(List<CrmWorkRecord> intoSchoolRecords) {
        List<Long> allTeacherIds = new ArrayList<>();
        intoSchoolRecords.forEach(r -> {
            if (CollectionUtils.isNotEmpty(r.getVisitTeacherList())) {
                allTeacherIds.addAll(r.getVisitTeacherList().stream().filter(CrmTeacherVisitInfo::isRealTeacher).map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toList()));
            }
        });
        return allTeacherIds;
    }

    // 进校本月数据
    public BaseIntoSchoolStatisticsView loadBdIntoSchoolStatisticsByBdId(List<CrmWorkRecord> monthCrmWorkRecords, List<Long> bdManageJunior) {
        BaseIntoSchoolStatisticsView bdInfo = new BaseIntoSchoolStatisticsView();
        // 进校次数
        bdInfo.setIntoSchoolCount(monthCrmWorkRecords.size());
        // 已访问学校数
        bdInfo.setVisitedSchoolCount(monthCrmWorkRecords.stream().map(CrmWorkRecord::getSchoolId).filter(bdManageJunior::contains).collect(Collectors.toSet()).size());
        Map<Long, Date> teacherWorkMap = loadMapTeacher(monthCrmWorkRecords);
        // 布置作业的老师ID
        Set<Long> teacherHwCount = loadTeacherIdHw(teacherWorkMap);
        Integer teacherSize = 0;
        for (CrmWorkRecord p : monthCrmWorkRecords) {
            List<CrmTeacherVisitInfo> visitTeacherList = p.getVisitTeacherList();
            if (CollectionUtils.isNotEmpty(visitTeacherList)) {
                teacherSize += SafeConverter.toInt(visitTeacherList.stream().filter(CrmTeacherVisitInfo::isRealTeacher).count());
            }
        }
        // 校均拜访老师数
        bdInfo.setVisitTeacherAvg(monthCrmWorkRecords.size() == 0 ? 0.0 : MathUtils.doubleDivide(teacherSize, monthCrmWorkRecords.size(), 1));
        // 老师布置作业率
        bdInfo.setVisitTeacherHwPro(teacherWorkMap.keySet().size() == 0 ? "0" : String.valueOf(MathUtils.doubleDivide(teacherHwCount.size(), teacherWorkMap.keySet().size(), 2) * 100));
        return bdInfo;
    }

    // 取布置作业的老师
    public Set<Long> loadTeacherIdHw(Map<Long, Date> teacherWorkMap) {
        Set<Long> teacherHwCount = new HashSet<>();
        Map<Long, CrmTeacherSummary> teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(teacherWorkMap.keySet());
        teacherWorkMap.forEach((k, v) -> {
            if (!teacherSummaryMap.containsKey(k)) {
                return;
            }
            if (v == null) {
                return;
            }
            CrmTeacherSummary crmTeacherSummary = teacherSummaryMap.get(k);
            Long latestAssignHomeworkTime = crmTeacherSummary.getLatestAssignHomeworkTime();
            if (latestAssignHomeworkTime == null) {
                return;
            }
            if (v.getTime() < latestAssignHomeworkTime) {
                teacherHwCount.add(k);
            }
        });
        return teacherHwCount;
    }

    // 老师进校时间
    public Map<Long, Date> loadMapTeacher(Collection<CrmWorkRecord> crmWorkRecords) {
        Map<Long, Date> teacherWorkMap = new HashMap<>();
        // 进校记录是按照工作时间倒序的
        crmWorkRecords.forEach(p -> {
            if (p.getWorkTime().after(DayRange.current().getStartDate())) {
                return;
            }
            List<CrmTeacherVisitInfo> visitTeacherList = p.getVisitTeacherList();
            if (CollectionUtils.isNotEmpty(visitTeacherList)) {
                visitTeacherList.forEach(p1 -> {
                    if (!SCHOOL_MASTER_INFO.containsKey(p1.getTeacherId()) && !teacherWorkMap.containsKey(p1.getTeacherId())) {
                        teacherWorkMap.put(p1.getTeacherId(), p.getWorkTime());
                    }
                });
            }
        });
        return teacherWorkMap;
    }

    // ----------------------------------------------以前工作记录的方法----------------------------------------------------------------

    public Map<Long, List<CrmWorkRecord>> getSchoolWorkRecords(Collection<Long> schoolIds, CrmWorkRecordType recordType, Date startDate, Date endDate) {
        if (CollectionUtils.isEmpty(schoolIds) || startDate == null || endDate == null || endDate.before(startDate)) {
            return Collections.emptyMap();
        }
        Map<Long, List<CrmWorkRecord>> workRecords = new HashMap<>();
        AgentResourceService.batchIds(schoolIds, 200).forEach((k, v) -> {
            if (CollectionUtils.isNotEmpty(v)) {
                Map<Long, List<CrmWorkRecord>> tempMap = crmWorkRecordLoaderClient.findBySchools(v);
                if (MapUtils.isNotEmpty(tempMap)) {
                    workRecords.putAll(tempMap);
                }
            }
        });
        Map<Long, List<CrmWorkRecord>> retRecords = new HashMap<>();
        for (Long workerId : workRecords.keySet()) {
            List<CrmWorkRecord> records = workRecords.get(workerId);
            records = records.stream()
                    .filter(p -> recordType == null || recordType == p.getWorkType())
                    .filter(p -> startDate.before(p.getWorkTime()))
                    .filter(p -> endDate.after(p.getWorkTime()))
                    .collect(Collectors.toList());
            retRecords.put(workerId, records);
        }
        return retRecords;
    }

    //-----------------------------------------业务代码 END------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------------------------


    /**
     * 获取老师进校的效果
     */
    public String getWorkContent(List<CrmTeacherVisitInfo> visitTeacherList, Long teacherId) {
        String workContent = "";
        if (CollectionUtils.isEmpty(visitTeacherList)) {
            return workContent;
        }
        for (CrmTeacherVisitInfo info : visitTeacherList) {
            if (info == null) {
                continue;
            }
            if (Objects.equals(info.getTeacherId(), teacherId)) {
                workContent = ConversionUtils.toString(info.getVisitInfo());
                break;
            }
        }
        return workContent;
    }

    public MapMessage getSchoolTeachers(Long schoolId, List<Long> teacherIds) {
        if (schoolId == null) {
            return MapMessage.errorMessage("学校ID不存在");
        }
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("学校信息不存在");
        }
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        Set<Long> allTeacherIds = teacherLoaderClient.loadSchoolTeacherIds(schoolId);

        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(allTeacherIds);
        Collection<Teacher> teacherList = teacherMap.values();

        List<CrmTeacherSummary> teacherSummaryList = crmSummaryLoaderClient.loadSchoolTeachers(schoolId);
        Set<Long> fakeTeacherId = teacherSummaryList.stream().filter(p -> !p.isNotManualFakeTeacher()).map(CrmTeacherSummary::getTeacherId).collect(Collectors.toSet());
        teacherList = teacherList.stream().filter(p -> !fakeTeacherId.contains(p.getId())).collect(Collectors.toSet());
        //过滤隐藏老师

        Set<Long> tempIds = teacherList.stream().map(Teacher::getId).collect(Collectors.toSet());
        Set<Long> hideTeacherIds = new HashSet<>();
        AgentResourceService.batchIds(tempIds, 500).forEach((k, v) -> {
            Map<Long, AgentHiddenTeacher> agentHiddenTeachers = agentHiddenTeacherService.getAgentHiddenTeachers(v);
            if (MapUtils.isNotEmpty(agentHiddenTeachers)) {
                hideTeacherIds.addAll(agentHiddenTeachers.keySet());
            }
        });
        teacherList = teacherList.stream().filter(p -> !hideTeacherIds.contains(p.getId())).collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(teacherList)) {
            MapMessage mapMessage = MapMessage.successMessage()
                    .add("english", new ArrayList<>())
                    .add("math", new ArrayList<>())
                    .add("chinese", new ArrayList<>());
            if (schoolLevel.equals(SchoolLevel.MIDDLE) || schoolLevel.equals(SchoolLevel.HIGH)) {
                mapMessage.add("otherSubject", new ArrayList<>());
            }
            return mapMessage;
        }
        teacherList = teacherList.stream().filter(p -> p.getSubject() != null).collect(Collectors.toSet());

//        tempIds = teacherList.stream().map(Teacher::getId).collect(Collectors.toSet());
//        //获取老师主副账号
//        Map<Long, Long> subMainTeacherIdMap = teacherLoaderClient.loadMainTeacherIds(tempIds);
//        //过滤掉所有老师副账号
//        Collection<Teacher> mainTeacherList = teacherList.stream().filter(p -> !subMainTeacherIdMap.containsKey(p.getId())).collect(Collectors.toSet());

        Map<Subject, List<Teacher>> teacherMapBySubject = teacherList.stream().collect(Collectors.groupingBy(Teacher::getSubject, Collectors.toList()));
        MapMessage mapMessage = MapMessage.successMessage()
                .add("english", getTeacherDataBySubjectMap(teacherMapBySubject, Subject.ENGLISH, teacherIds))
                .add("math", getTeacherDataBySubjectMap(teacherMapBySubject, Subject.MATH, teacherIds))
                .add("chinese", getTeacherDataBySubjectMap(teacherMapBySubject, Subject.CHINESE, teacherIds));

        if (schoolLevel.equals(SchoolLevel.MIDDLE) || schoolLevel.equals(SchoolLevel.HIGH)) {
            mapMessage.add("otherSubject", getTeacherDataBySubjectMap(teacherMapBySubject, NON_BASEIC_SUBJECTS, teacherIds));
        }
        return mapMessage;
    }

    // 封装老师信息
    private List<TeacherData> getTeacherDataBySubjectMap(Map<Subject, List<Teacher>> teacherMapBySubject, Subject subject, List<Long> teacherIds) {
        if (!teacherMapBySubject.containsKey(subject)) {
            return Collections.emptyList();
        }
        List<Teacher> crmTeacherSummaries = teacherMapBySubject.get(subject);
        return getTeacherData(crmTeacherSummaries, teacherIds);
    }

    // 封装老师信息
    private List<TeacherData> getTeacherDataBySubjectMap(Map<Subject, List<Teacher>> teacherMapBySubject, List<Subject> subjects, List<Long> teacherIds) {
        if (CollectionUtils.isEmpty(subjects)) {
            return Collections.emptyList();
        }
        List<Teacher> crmTeacherSummaries = new ArrayList<>();
        subjects.forEach(subject -> {
            List<Teacher> tempList = teacherMapBySubject.get(subject);
            if (CollectionUtils.isNotEmpty(tempList)) {
                crmTeacherSummaries.addAll(tempList);
            }
        });
        return getTeacherData(crmTeacherSummaries, teacherIds);
    }

    public List<TeacherData> getTeacherData(Collection<Teacher> teachers, List<Long> checkedTeacherIds) {
        if (CollectionUtils.isEmpty(teachers)) {
            return Collections.emptyList();
        }
        List<Teacher> sortTeachers = new ArrayList<>();
        List<Teacher> newTeachers = teachers.stream().filter(p -> DateUtils.getTodayStart().before(p.getCreateTime())).collect(Collectors.toList());
        Comparator cmp = Collator.getInstance(java.util.Locale.CHINA);
        List<Teacher> oldTeachers = teachers.stream().filter(p -> !DateUtils.getTodayStart().before(p.getCreateTime())).sorted((o1, o2) -> cmp.compare(SafeConverter.toString(o1.getProfile().getRealname()), SafeConverter.toString(o2.getProfile().getRealname()))).collect(Collectors.toList());
        sortTeachers.addAll(newTeachers);
        sortTeachers.addAll(oldTeachers);
        List<TeacherData> res = new ArrayList<>();
        Set<Long> existTeacherIds = new HashSet<>();

        //获取老师ids
        List<Long> teacherIds = sortTeachers.stream().map(Teacher::getId).collect(Collectors.toList());
        //获取老师主副账号对应关系
        Map<Long, List<Long>> mainSubTeacherIdMap = teacherLoaderClient.loadSubTeacherIds(teacherIds);
        //获取老师副主账号对应关系
        Map<Long, Long> subMainTeacherIdMap = teacherLoaderClient.loadMainTeacherIds(teacherIds);

        sortTeachers.forEach(p -> {
            if (p == null) {
                return;
            }
            if (SafeConverter.toBoolean(p.getDisabled())) {
                return;
            }
            Long teacherId = p.getId();
            if (existTeacherIds.contains(teacherId)) {
                return;
            }
            existTeacherIds.add(teacherId);
            TeacherData data = new TeacherData();
            data.setTeacherId(teacherId);
            UserProfile profile = p.getProfile();
            data.setTeacherName(profile == null ? "老师无姓名" : profile.getRealname());
            data.setCreateTime(p.getCreateTime());
            data.setSubject(p.getSubject());
            if (checkedTeacherIds.contains(p.getId())) {
                data.setChecked(true);
            } else {
                data.setChecked(false);
            }
            data.setOrigin("teacher");
            data.setUnRegTeacher(false);

            List<Long> subTeacherIds = mainSubTeacherIdMap.get(teacherId);
            //如果该老师是主账号，set该老师ID
            if (CollectionUtils.isNotEmpty(subTeacherIds)) {
                data.setMainTeacherId(teacherId);
                //如果该老师是副账号，获取相应的主账号，set
            } else {
                Long mainTeacherId = subMainTeacherIdMap.get(teacherId);
                if (mainTeacherId != null) {
                    data.setMainTeacherId(mainTeacherId);
                }
            }

//            //如果该老师存在副账号
//            if (mainSubTeacherIdMap.containsKey(p.getId()) && mainSubTeacherIdMap.get(p.getId()).size()>0){
//                List<Map<String,Object>> subTeacherList = new ArrayList<>();
//                mainSubTeacherIdMap.get(p.getId()).forEach(item->{
//                    Map<String,Object> subTeacherMap = new HashMap<>();
//                    if (teacherMap.containsKey(item) && null != teacherMap.get(item)){
//                        //科目
//                        String subjectValue = "";
//                        Subject subject = teacherMap.get(item).getSubject();
//                        if (subject == Subject.CHINESE){
//                            subjectValue = "语";
//                        }
//                        if (subject == Subject.MATH){
//                            subjectValue = "数";
//                        }
//                        if (subject == Subject.ENGLISH){
//                            subjectValue = "外";
//                        }
//                        //老师ID
//                        Long teacherId = teacherMap.get(item).getId();
//                        subTeacherMap.put("subject",subjectValue);
//                        subTeacherMap.put("teacherId",teacherId);
//                        subTeacherList.add(subTeacherMap);
//                    }
//                });
//                data.setSubTeacherList(subTeacherList);
//            }
            res.add(data);
        });
        return res;
    }

    public TeacherData getTeacherData() {
        return new TeacherData();
    }


    public Map<String, List<CrmTeacherVisitInfo>> createCrmTeacherVisitInfoFromList(List<Map<String, Object>> visitTeacher) {
        if (visitTeacher == null) {
            return Collections.emptyMap();
        }
        Map<String, List<CrmTeacherVisitInfo>> res = new HashMap<>();
        List<CrmTeacherVisitInfo> list = new ArrayList<>();
        visitTeacher.forEach(p -> CollectionUtils.addNonNullElement(list, getTeacherVisitInfo(p)));
        res.put("list", list);
        return res;
    }

    public List<CrmTeacherVisitInfo> getVisitTeachers(Set<Long> teacherIdSet) {
        if (teacherIdSet == null) {
            return Collections.emptyList();
        }

        List<CrmTeacherVisitInfo> res = new ArrayList<>();
        teacherIdSet.forEach(p -> CollectionUtils.addNonNullElement(res, getTeacherVisitInfo(p)));
        return res;
    }

    public List<WorkRecordTeacher> generateVisitTeachers(Set<Long> teacherIdSet, FollowUpType followUpType) {
        if (teacherIdSet == null) {
            return Collections.emptyList();
        }

        List<WorkRecordTeacher> res = new ArrayList<>();
        teacherIdSet.forEach(p -> CollectionUtils.addNonNullElement(res, generateTeacherVisitInfo(p, followUpType)));
        return res;
    }

    private CrmTeacherVisitInfo getTeacherVisitInfo(Long teacherId) {
        CrmTeacherVisitInfo teacherVisitInfo = null;
        if (teacherId != null && SCHOOL_MASTER_INFO.containsKey(teacherId)) {
            teacherVisitInfo = new CrmTeacherVisitInfo();
            teacherVisitInfo.setTeacherId(teacherId);
            teacherVisitInfo.setTeacherName(SCHOOL_MASTER_INFO.get(teacherId));
        } else {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (teacher != null) {
                teacherVisitInfo = new CrmTeacherVisitInfo();
                teacherVisitInfo.setTeacherId(teacherId);
                UserProfile profile = teacher.getProfile();
                teacherVisitInfo.setTeacherName(profile == null ? "" : profile.getRealname());
                teacherVisitInfo.setSubject(teacher.getSubject());
            }
        }
        return teacherVisitInfo;
    }

    private WorkRecordTeacher generateTeacherVisitInfo(Long teacherId, FollowUpType followUpType) {
        WorkRecordTeacher workRecordTeacher = null;
        if (teacherId != null && SCHOOL_MASTER_INFO.containsKey(teacherId)) {
            workRecordTeacher = new WorkRecordTeacher();
            workRecordTeacher.setTeacherId(teacherId);
            workRecordTeacher.setTeacherName(SCHOOL_MASTER_INFO.get(teacherId));
            workRecordTeacher.setFollowUpType(followUpType);
        } else {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (teacher != null) {
                workRecordTeacher = new WorkRecordTeacher();
                workRecordTeacher.setTeacherId(teacherId);
                UserProfile profile = teacher.getProfile();
                workRecordTeacher.setTeacherName(profile == null ? "" : profile.getRealname());
                workRecordTeacher.setSubjects(Collections.singletonList(teacher.getSubject()));
                workRecordTeacher.setFollowUpType(followUpType);
            }
        }
        return workRecordTeacher;
    }

    private CrmTeacherVisitInfo getTeacherVisitInfo(Map<String, Object> visitTeacher) {
        Long teacherId = ConversionUtils.toLong(visitTeacher.get("teacherId"));
        CrmTeacherVisitInfo info = getTeacherVisitInfo(teacherId);
        info.setVisitInfo(SafeConverter.toString(visitTeacher.get("visitInfo")));
        return info;
    }

//    public MapMessage saveCrmWorkRecord(CrmWorkRecord crmWorkRecord) {
//        if (crmWorkRecord == null) {
//            return MapMessage.errorMessage("工作记录为空，保存工作记录失败");
//        }
//        String id = crmWorkRecordServiceClient.insert(crmWorkRecord);
//
//        crmWorkRecord.setId(id);
//        AlpsThreadPool.getInstance().submit(() -> saveRecordWorkload(crmWorkRecord));
//        return MapMessage.successMessage().add("id", id);
//    }


    @Getter
    @Setter
    public class TeacherData {
        private Long teacherId;
        private String teacherName;
        private Boolean checked;
        private Subject subject;
        private List<Map<String, Object>> subTeacherList;
        private Date createTime;
        private List<ClazzLevel> gradeList;
        private String origin;  //“teacher”：注册老师   “outerResource”：上层资源中的未注册老师
        private Boolean unRegTeacher;
        private Long mainTeacherId;//老师主账号ID
    }


    public CrmWorkRecord getWorkInfo(String workRecordId) {
        if (StringUtils.isBlank(workRecordId)) {
            return null;
        }

        return crmWorkRecordLoaderClient.load(workRecordId);
    }

    // 查询工作记录的时候按照用户直属下级进行查询,防止出现一个地区2个人员的情况
    public List<CrmWorkRecord> loadRegionWorkRecords(AuthCurrentUser user, List<CrmWorkRecordType> crmWorkRecordTypes, Date startTime, Date endTime) {
        if (CollectionUtils.isEmpty(crmWorkRecordTypes)) {
            return Collections.emptyList();
        }
        if (user == null || user.getUserId() == null) {
            return Collections.emptyList();
        }

        List<Long> memberIds = getUserIds(user);

        List<CrmWorkRecord> crmWorkRecords = new ArrayList<>();
        crmWorkRecordTypes.forEach(item -> {
            List<CrmWorkRecord> tempWorkRecords = crmWorkRecordLoaderClient.listByWorkersAndType(memberIds, item, startTime, endTime);
            crmWorkRecords.addAll(tempWorkRecords);
        });
        crmWorkRecords.sort((o1, o2) -> {
            return o1.getWorkTime().compareTo(o2.getWorkTime());
        });
        return crmWorkRecords;
    }

    public List<WorkRecordData> loadRegionWorkRecordsNew(AuthCurrentUser user, List<AgentWorkRecordType> crmWorkRecordTypes, Date startTime, Date endTime) {
        if (CollectionUtils.isEmpty(crmWorkRecordTypes)) {
            return Collections.emptyList();
        }
        if (user == null || user.getUserId() == null) {
            return Collections.emptyList();
        }

        List<Long> memberIds = getUserIds(user);

        List<WorkRecordData> crmWorkRecords = new ArrayList<>();
        crmWorkRecordTypes.forEach(item -> {
            List<WorkRecordData> tempWorkRecords = workRecordDataCompatibilityService.getWorkRecordDataListByUserTypeTime(memberIds, item, startTime, endTime);
            crmWorkRecords.addAll(tempWorkRecords);
        });
        crmWorkRecords.sort((o1, o2) -> {
            return o1.getWorkTime().compareTo(o2.getWorkTime());
        });
        return crmWorkRecords;
    }

    private List<Long> getUserIds(AuthCurrentUser user) {
        AgentUser agentUser = baseUserService.getUser(user.getUserId());
        if (agentUser == null) {
            return Collections.emptyList();
        }
        List<AgentUser> memberList = new ArrayList<>();
        if (user.isBusinessDeveloper()) {
            memberList.add(agentUser);
        } else {
            List<AgentGroup> groups = new ArrayList<>();
            List<Long> groupIdList = baseOrgService.getManagedGroupIdListByUserId(user.getUserId());
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                groupIdList.forEach(p -> baseOrgService.getAllSubGroupList(groups, p));
            }

            Set<Long> groupIds = groups.stream().map(AgentGroup::getId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(groups)) {
                groupIds = new HashSet<>();
            }
            List<AgentGroupUser> agentGroupUsers = agentGroupUserLoaderClient.findByUserId(user.getUserId());
            if (CollectionUtils.isNotEmpty(agentGroupUsers)) {
                groupIds.add(agentGroupUsers.get(0).getGroupId());
            }
            agentGroupUsers = baseOrgService.getGroupUserByGroups(groupIds);
            Set<Long> userIds = agentGroupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            memberList = baseOrgService.getUsers(userIds);
        }

        if (CollectionUtils.isEmpty(memberList)) {
            return Collections.emptyList();
        }
        // 获取所有的用户ID
        List<Long> memberIds = new ArrayList<>();
        memberList.forEach(p -> memberIds.add(p.getId()));
        return memberIds;
    }

    public CrmWorkRecord addTaskDetailWorkRecord(String takDetailId, String title, String content, Long executorId) {
        CrmWorkRecord crmWorkRecord = new CrmWorkRecord();
        AgentUser agentUser = baseUserService.getUser(executorId);
        String executorName = "Agent." + agentUser.getRealName();//TODO 添加执行人名称
        return crmWorkRecord;
    }


    public List<VisitSchoolResultDetailData> getVisitSchoolResultDetailData(Long userId, Date startTime, Date endTime) {
        Date monthFirstDay = DayUtils.getFirstDayOfMonth(startTime);
        Date monthLastDay = DayUtils.getLastDayOfMonth(startTime);
        List<CrmWorkRecord> monthWorkRecordList = listByWorkerAndType(userId, CrmWorkRecordType.SCHOOL, monthFirstDay, DateUtils.addDays(monthLastDay, 1));
        if (CollectionUtils.isEmpty(monthWorkRecordList)) {
            return Collections.emptyList();
        }
        Map<Long, List<CrmWorkRecord>> monthSchoolWorkRecordMap = monthWorkRecordList.stream().collect(Collectors.groupingBy(CrmWorkRecord::getSchoolId));
        List<CrmWorkRecord> workRecordList = monthWorkRecordList.stream().filter(p -> p.getWorkTime() != null && p.getWorkTime().before(endTime) && p.getWorkTime().after(startTime)).collect(Collectors.toList());

        Set<Long> schoolIds = workRecordList.stream().map(CrmWorkRecord::getSchoolId).collect(Collectors.toSet());

        Map<Long, CrmSchoolSummary> schoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(schoolIds);
        Set<Long> missSchoolIds = schoolIds.stream().filter(p -> !schoolSummaryMap.containsKey(p)).collect(Collectors.toSet());
        Map<Long, School> schoolMap = raikouSystem.loadSchools(missSchoolIds);
        Map<Long, SchoolExtInfo> schoolExtInfoMap = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolsExtInfoAsMap(schoolIds)
                .getUninterruptibly();
        List<VisitSchoolResultDetailData> retList = new ArrayList<>();
        for (CrmWorkRecord workRecord : workRecordList) {
            if (workRecord.getSchoolId() == null) {
                continue;
            }
            VisitSchoolResultDetailData detailData = new VisitSchoolResultDetailData();
            detailData.setSchoolId(workRecord.getSchoolId());
            CrmSchoolSummary schoolSummary = schoolSummaryMap.get(workRecord.getSchoolId());
            if (schoolSummary != null) {
                detailData.setSchoolName(schoolSummary.getSchoolName());
                detailData.setSchoolLevel(schoolSummary.getSchoolLevel());
            }
            School school = schoolMap.get(workRecord.getSchoolId());
            if (school != null) {
                detailData.setSchoolName(school.getShortName());
                detailData.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
            }
            SchoolExtInfo schoolExtInfo = schoolExtInfoMap.get(workRecord.getSchoolId());
            if (schoolExtInfo != null) {
                detailData.setSchoolSize(schoolExtInfo.getSchoolSize());
            }
            detailData.setDay(ConversionUtils.toInt(DateUtils.dateToString(workRecord.getWorkTime(), "yyyyMMdd")));
            List<CrmWorkRecord> schoolWorkRecordList = monthSchoolWorkRecordMap.get(workRecord.getSchoolId());
            if (CollectionUtils.isNotEmpty(schoolWorkRecordList)) {
                detailData.setVisitSchoolMonthCount(schoolWorkRecordList.size());
                List<Integer> visitDayList = schoolWorkRecordList.stream().filter(p -> p.getWorkTime() != null).map(k -> ConversionUtils.toInt(DateUtils.dateToString(k.getWorkTime(), "yyyyMMdd"))).sorted().collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(visitDayList)) {
                    detailData.setVisitDayList(visitDayList);
                }
            }

            detailData.setPreStuRegNum(workRecord.getStudentTotalCount()); // 进校前注册学生数
            detailData.setPreStuAuthNum(workRecord.getStudentAuthedCount());// 进校前认证学生数

            detailData.setForecastStuRegNum(workRecord.getForecastStuRegNum());// 预测注册增长
            detailData.setForecastStuAuthNum(workRecord.getForecastStuAuthNum());// 预测认证增长
            detailData.setForecastSascData(workRecord.getForecastSascData());// 预测单活增长
            detailData.setForecastDascData(workRecord.getForecastDascData());// 预测双活增长

            Integer preDay = ConversionUtils.toInt(DateUtils.dateToString(DateUtils.calculateDateDay(workRecord.getWorkTime(), -1), "yyyyMMdd"));
            Integer nextDay = getNextPerformanceDay(schoolWorkRecordList, workRecord);
            setPerformanceChange(detailData, workRecord, preDay, nextDay);
            Date nextDate = getNextVisitDate(schoolWorkRecordList, workRecord);
            setVisitedTeachersData(detailData, workRecord, nextDate);
            retList.add(detailData);
        }
        return retList;
    }

    // 获取下次进校的日期， 如果没有下次进校 ：  本月未结束时取前一天时间， 本月已结束去月底时间
    public Integer getNextPerformanceDay(List<CrmWorkRecord> schoolWorkRecordList, CrmWorkRecord workRecord) {
        CrmWorkRecord nextWorkRecord = schoolWorkRecordList.stream().filter(p -> p.getWorkTime().getTime() > workRecord.getWorkTime().getTime())
                .sorted((o1, o2) -> ((Long) o1.getWorkTime().getTime()).compareTo(o2.getWorkTime().getTime()))
                .findFirst().orElse(null);
        if (nextWorkRecord != null) {// 在指定进校后，还有进校， 取下次进校的前一天
            return ConversionUtils.toInt(DateUtils.dateToString(DateUtils.calculateDateDay(nextWorkRecord.getWorkTime(), -1), "yyyyMMdd"));
        }
        Date currentDate = new Date();
        Date lastDayOfMonth = DayUtils.getLastDayOfMonth(workRecord.getWorkTime());

        Date performanceDate = performanceService.lastSuccessDataDate();
        Date targetDate = performanceDate;
        // 当月还未结束, 则取最新的业绩日期， 若本月已经结束了， 则最新业绩日期和月底日期比较，避免每月1号早上大数据生成以前的问题
        if (DayUtils.getMonth(currentDate) != DayUtils.getMonth(lastDayOfMonth) && performanceDate.after(lastDayOfMonth)) {
            targetDate = lastDayOfMonth;
        }
        return ConversionUtils.toInt(DateUtils.dateToString(targetDate, "yyyyMMdd"));
    }


    public void setPerformanceChange(VisitSchoolResultDetailData detailData, CrmWorkRecord workRecord, Integer preDay, Integer nextDay) {
        List<SchoolDayIncreaseData> preDayIncreaseDataList = schoolDayIncreaseDataDao.findSchoolData(workRecord.getSchoolId(), Collections.singleton(preDay));
        SchoolDayIncreaseData preDayData = null;// 进校前一天的业绩数据
        if (CollectionUtils.isNotEmpty(preDayIncreaseDataList)) {
            preDayData = preDayIncreaseDataList.get(0);
        }
        Integer preDayMonthSasc = (preDayData == null || preDayData.getEngMauc() == null) ? 0 : preDayData.getEngMauc();
        Integer preDayMonthDasc = (preDayData == null || preDayData.getMathMauc() == null) ? 0 : preDayData.getMathMauc();
        Long preMonthStuRegNum = (preDayData == null || preDayData.getMonthStuRegNum() == null) ? 0L : preDayData.getMonthStuRegNum(); // 本月新增学生注册数
        Long preMonthStuAuthNum = (preDayData == null || preDayData.getMonthStuAuthNum() == null) ? 0L : preDayData.getMonthStuAuthNum();// 本月新增学生认证数

        List<SchoolDayIncreaseData> nextDayIncreaseDataList = schoolDayIncreaseDataDao.findSchoolData(workRecord.getSchoolId(), Collections.singleton(nextDay));
        SchoolDayIncreaseData currentDayData = null; // 下次进校前一天的业绩数据
        if (CollectionUtils.isNotEmpty(nextDayIncreaseDataList)) {
            currentDayData = nextDayIncreaseDataList.get(0);
        }

        Integer currentDayMonthSasc = (currentDayData == null || currentDayData.getEngMauc() == null) ? 0 : currentDayData.getEngMauc();
        Integer currentDayMonthDasc = (currentDayData == null || currentDayData.getMathMauc() == null) ? 0 : currentDayData.getMathMauc();
        Long currentMonthStuRegNum = (currentDayData == null || currentDayData.getMonthStuRegNum() == null) ? 0L : currentDayData.getMonthStuRegNum(); // 本月新增学生注册数
        Long currentMonthStuAuthNum = (currentDayData == null || currentDayData.getMonthStuAuthNum() == null) ? 0L : currentDayData.getMonthStuAuthNum();// 本月新增学生认证数

        detailData.setPreSascData(preDayMonthSasc); // 设置拜访前单活
        detailData.setPreDascData(preDayMonthDasc);// 设置拜访前双活


        detailData.setAddStuRegNum(currentMonthStuRegNum - preMonthStuRegNum); // 设置注册增长数
        detailData.setAddStuAuthNum(currentMonthStuAuthNum - preMonthStuAuthNum);// 设置认证增长数

        detailData.setAddSascData(currentDayMonthSasc - preDayMonthSasc); // 设置单活增长数
        detailData.setAddDascData(currentDayMonthDasc - preDayMonthDasc); // 设置双活增长数

    }

    // 获取下次进校的日期， 如果没有下次进校 ：  本月未结束时取当天时间， 本月已结束去月底时间
    private Date getNextVisitDate(List<CrmWorkRecord> schoolWorkRecordList, CrmWorkRecord workRecord) {
        CrmWorkRecord nextWorkRecord = schoolWorkRecordList.stream().filter(p -> p.getWorkTime().getTime() > workRecord.getWorkTime().getTime())
                .sorted((o1, o2) -> ((Long) o1.getWorkTime().getTime()).compareTo(o2.getWorkTime().getTime()))
                .findFirst().orElse(null);
        if (nextWorkRecord != null) { // 在指定进校后，多次进校， 取下次进校的前一天
            return DateUtils.stringToDate(DateUtils.dateToString(nextWorkRecord.getWorkTime(), "yyyyMMdd"), "yyyyMMdd");
        }
        Date currentDate = new Date();
        Date lastDayOfMonth = DayUtils.getLastDayOfMonth(workRecord.getWorkTime());
        // 本月还未结束
        if (DayUtils.getMonth(currentDate) == DayUtils.getMonth(lastDayOfMonth)) {
            return new Date();
        }
        // 换月了
        return DateUtils.addDays(lastDayOfMonth, 1);

    }

    private void setVisitedTeachersData(VisitSchoolResultDetailData detailData, CrmWorkRecord workRecord, Date endTime) {
        if (CollectionUtils.isEmpty(workRecord.getVisitTeacherList())) {
            return;
        }
        Map<Long, CrmTeacherVisitInfo> visitTeacherMap = workRecord.getVisitTeacherList().stream().collect(Collectors.toMap(CrmTeacherVisitInfo::getTeacherId, Function.identity()));
        List<CrmTeacherVisitInfo> realTeacherList = workRecord.getVisitTeacherList().stream().filter(CrmTeacherVisitInfo::isRealTeacher).collect(Collectors.toList());
        List<CrmTeacherVisitInfo> otherVisitList = workRecord.getVisitTeacherList().stream().filter(p -> !p.isRealTeacher()).collect(Collectors.toList());
        List<VisitSchoolResultDetailData.TeacherInfo> teacherInfoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(realTeacherList)) {
            Set<Long> teacherIds = realTeacherList.stream().map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toSet());
            // 过滤掉假老师
            Map<Long, TeacherExtAttribute> extAttributeMap = teacherLoaderClient.loadTeacherExtAttributes(teacherIds);
            teacherIds = teacherIds.stream()
                    .filter(t -> extAttributeMap.get(t) == null || !extAttributeMap.get(t).isFakeTeacher()).collect(Collectors.toSet());

            if (CollectionUtils.isNotEmpty(teacherIds)) {
                Date startTime = DateUtils.stringToDate(DateUtils.dateToString(workRecord.getWorkTime(), "yyyyMMdd"), "yyyyMMdd");
                VisitSchoolResultDetailData.TeacherInfo teacherInfo;
                for (Long teacherId : teacherIds) {
                    CrmTeacherVisitInfo visitInfo = visitTeacherMap.get(teacherId);
                    if (visitInfo != null) {
                        teacherInfo = detailData.new TeacherInfo();
                        teacherInfo.setTeacherId(teacherId);
                        teacherInfo.setTeacherName(visitInfo.getTeacherName());
                        Collection<NewHomework.Location> homeworkDataList = newHomeworkCrmLoaderClient.findIdsByTeacherIdAndCreateAt(teacherId, startTime, endTime);
                        if (CollectionUtils.isNotEmpty(homeworkDataList)) {
                            teacherInfo.setUsedFlg(true);
                        } else {
                            teacherInfo.setUsedFlg(false);
                        }
                        teacherInfoList.add(teacherInfo);
                    }
                }
            }
        }
        sortTeacherList(teacherInfoList);
        if (CollectionUtils.isNotEmpty(otherVisitList)) {
            otherVisitList.forEach(p -> {
                VisitSchoolResultDetailData.TeacherInfo teacherInfo = detailData.new TeacherInfo();
                teacherInfo.setTeacherId(p.getTeacherId());
                teacherInfo.setTeacherName(p.getTeacherName());
                teacherInfo.setUsedFlg(false);
                teacherInfoList.add(teacherInfo);
            });
        }
        detailData.setTeacherInfoList(teacherInfoList);
    }

    private void sortTeacherList(List<VisitSchoolResultDetailData.TeacherInfo> unusedTeacherList) {
        if (CollectionUtils.isEmpty(unusedTeacherList)) {
            return;
        }
        unusedTeacherList.sort((o1, o2) -> {
            if (o1.getUsedFlg()) {
                if (o2.getUsedFlg()) {
                    return 0;
                }
                return 1;
            } else {
                if (o2.getUsedFlg()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    public Set<Long> getUnVisitSchools(Long userId, CrmWorkRecordType recordType, Date startDate, Date endDate) {
        List<CrmWorkRecord> workRecordList = listByWorkerAndType(userId, recordType, startDate, endDate);
        if (CollectionUtils.isEmpty(workRecordList)) {
            return Collections.emptySet();
        }
        Set<Long> visitSchoolList = workRecordList.stream().map(CrmWorkRecord::getSchoolId).collect(Collectors.toSet());
        List<Long> managedSchools = baseOrgService.getManagedSchoolList(userId);
        return managedSchools.stream().filter(p -> !visitSchoolList.contains(p)).collect(Collectors.toSet());
    }


    public List<CrmWorkRecord> listByWorkerAndType(Long worker, CrmWorkRecordType recordType,
                                                   Date startDate, Date endDate) {
        if (worker == null || startDate == null || endDate == null || endDate.before(startDate) || recordType == null) {
            return Collections.emptyList();
        }
        return crmWorkRecordLoaderClient.listByWorkerAndType(worker, recordType, startDate, endDate);
    }

    public Map<Long, List<CrmWorkRecord>> listByWorkersAndType(Collection<Long> workers, CrmWorkRecordType recordType,
                                                               Date startDate, Date endDate) {
        if (CollectionUtils.isEmpty(workers) || startDate == null || endDate == null || endDate.before(startDate) || recordType == null) {
            return Collections.emptyMap();
        }
        List<CrmWorkRecord> workRecords = crmWorkRecordLoaderClient.listByWorkersAndType(workers, recordType, startDate, endDate);
        if (CollectionUtils.isEmpty(workRecords)) {
            return Collections.emptyMap();
        }
        return workRecords.stream().collect(Collectors.groupingBy(CrmWorkRecord::getWorkerId));
    }

    // 计算工作日天数
    private int calWorkDays(Date startDate, Date endDate) {
        int start = SafeConverter.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        int end = SafeConverter.toInt(DateUtils.dateToString(endDate, "yyyyMMdd"));
        return DayUtils.getWorkdayList(start, end).size();
    }

    /**
     * 工作记录列表
     *
     * @param workerId
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordMiddleSortData> workRecordList(Long workerId, Date startDate, Date endDate) {
        List<WorkRecordData> workRecordList = workRecordDataCompatibilityService.getWorkRecordDataListByUserTypeTime(Collections.singletonList(workerId), null, startDate, endDate);

        List<WorkRecordMiddleSortData> wrldResult = new ArrayList();
        if (CollectionUtils.isNotEmpty(workRecordList)) {

            //学校新注册老师
            Map<Long, List<TeacherSummaryEsInfo>> schoolTeacherSummaryMap = new HashMap<>();
            Set<Long> schoolIds = workRecordList.stream().filter(p -> p.getWorkType() == AgentWorkRecordType.SCHOOL).map(WorkRecordData::getSchoolId).collect(Collectors.toSet());
            String regStartTime = DateUtils.dateToString(startDate, "yyyyMMddHHmmss");
            String regEndTime = DateUtils.dateToString(endDate, "yyyyMMddHHmmss");
            Page<TeacherSummaryEsInfo> teacherSummaryEsInfos = searchService.queryNewRegTeacherFromEsInSchools(schoolIds, regStartTime, regEndTime, 0, 500);
            if (CollectionUtils.isNotEmpty(teacherSummaryEsInfos.getContent())) {
                schoolTeacherSummaryMap.putAll(teacherSummaryEsInfos.getContent().stream().filter(p -> p.getSchoolId() != null).collect(Collectors.groupingBy(TeacherSummaryEsInfo::getSchoolId)));
            }

            Map<String, List<WorkRecordData>> crmWorkRecordDayMap = new LinkedHashMap<>();
            for (WorkRecordData p : workRecordList) {
                if (p.getWorkTime() != null) {
                    String dayStr = DateUtils.dateToString(p.getWorkTime(), "yyyy-MM-dd");
                    List<WorkRecordData> tempList = crmWorkRecordDayMap.get(dayStr);
                    if (tempList != null) {
                        tempList.add(p);
                        crmWorkRecordDayMap.put(dayStr, tempList);
                    } else {
                        List<WorkRecordData> dataList = new ArrayList<>();
                        dataList.add(p);
                        crmWorkRecordDayMap.put(dayStr, dataList);
                    }
                }
            }

            //key:拜访时间,value:从拜访时间算起（不包含拜访当天）近30天进校记录
            Map<String, Map<Long, List<WorkRecordData>>> workTimeStrNearly30DaysIntoSchoolWorkRecordListMap = new HashMap<>();
            crmWorkRecordDayMap.forEach((day, crmWorkRecordList) -> {
                //工作时间
                Date workTime = DateUtils.stringToDate(day, DateUtils.FORMAT_SQL_DATE);
                //近30天进校记录（不包含拜访当天）
                Date startTime = DayRange.newInstance(DateUtils.addDays(workTime, -30).getTime()).getStartDate();
                Date endTime = DayRange.newInstance(DateUtils.addDays(workTime, -1).getTime()).getEndDate();
                List<WorkRecordData> intoSchoolWorkRecordList = workRecordDataCompatibilityService.getWorkRecordDataListByUserTypeTime(Collections.singletonList(workerId), AgentWorkRecordType.SCHOOL, startTime, endTime);

                Map<Long, List<WorkRecordData>> schoolIntoSchoolWorkRecordListMap = intoSchoolWorkRecordList.stream().filter(p -> p.getSchoolId() != null && !Objects.equals(p.getVisitSchoolType(), 3)).collect(Collectors.groupingBy(WorkRecordData::getSchoolId));

                workTimeStrNearly30DaysIntoSchoolWorkRecordListMap.put(day, schoolIntoSchoolWorkRecordListMap);
            });

            crmWorkRecordDayMap.forEach((day, crmWorkRecordList) -> {
                List<WorkRecordListData> resultList = new ArrayList<>();
                for (WorkRecordData p : crmWorkRecordList) {
                    WorkRecordListData wrld = new WorkRecordListData();
                    wrld.setWorkRecordId(p.getId());
                    wrld.setWorkRecordType(p.getWorkType());
                    wrld.setWorkRecordTime(DateUtils.dateToString(p.getWorkTime(), "HH:mm"));
                    //进校
                    if (p.getWorkType() == AgentWorkRecordType.SCHOOL) {
                        Set<Long> visitTeacherIds = new HashSet<>();
                        Set<Long> visitChiTeacherIds = new HashSet<>();
                        Set<Long> visitMathTeacherIds = new HashSet<>();
                        Set<Long> visitEngTeacherIds = new HashSet<>();


                        int newRegTeacherCount = 0;     //新注册老师数
                        int newRegChiTeacherCount = 0;  //新注册老师数（语文）
                        int newRegMathTeacherCount = 0; //新注册老师数（数学）
                        int newRegEngTeacherCount = 0;  //新注册老师数（英语）

                        List<WorkRecordVisitUserInfo> visitUserInfoList = p.getVisitUserInfoList();
                        generateVisitTeacherInfo(visitUserInfoList, visitEngTeacherIds, visitMathTeacherIds, visitChiTeacherIds, visitTeacherIds);

                        List<TeacherSummaryEsInfo> teacherSummaryEsInfoList = schoolTeacherSummaryMap.get(p.getSchoolId());
                        if (CollectionUtils.isNotEmpty(teacherSummaryEsInfoList)) {
                            for (TeacherSummaryEsInfo teacherSummaryEsInfo : teacherSummaryEsInfoList) {
                                if (Objects.equals(Subject.ofWithUnknown(teacherSummaryEsInfo.getSubject()), Subject.ENGLISH)) {
                                    newRegEngTeacherCount++;
                                } else if (Objects.equals(Subject.ofWithUnknown(teacherSummaryEsInfo.getSubject()), Subject.MATH)) {
                                    newRegMathTeacherCount++;
                                } else if (Objects.equals(Subject.ofWithUnknown(teacherSummaryEsInfo.getSubject()), Subject.CHINESE)) {
                                    newRegChiTeacherCount++;
                                }
                                newRegTeacherCount++;
                            }
                        }
                        wrld.setVisitTeacherCount(visitTeacherIds.size());
                        wrld.setVisitEngTeacherCount(visitEngTeacherIds.size());
                        wrld.setVisitMathTeacherCount(visitMathTeacherIds.size());
                        wrld.setVisitChiTeacherCount(visitChiTeacherIds.size());

                        wrld.setNewRegTeacherCount(newRegTeacherCount);
                        wrld.setNewRegEngTeacherCount(newRegEngTeacherCount);
                        wrld.setNewRegMathTeacherCount(newRegMathTeacherCount);
                        wrld.setNewRegChiTeacherCount(newRegChiTeacherCount);

                        Long schoolId = p.getSchoolId();

                        if (!Objects.equals(p.getVisitSchoolType(), 3)) {
                            //拜访学校近30天拜访过2天及以上（不含今天这次拜访）
                            Map<Long, List<WorkRecordData>> nearly30DaysIntoSchoolWorkRecordListMap = workTimeStrNearly30DaysIntoSchoolWorkRecordListMap.get(day);
                            //频繁拜访
                            wrld.setFrequentlyVisit(intoSchoolFrequentlyVisit(nearly30DaysIntoSchoolWorkRecordListMap, schoolId));
                        }

                        //进校类型
                        wrld.setVisitSchoolType(p.getVisitSchoolType());

                        wrld.setWorkRecordRemarks(p.getSchoolName());
                        Map<Subject, List<Long>> subjectTeacherMap = agentWorkRecordStatisticsService.getSubjectTeacherMapNew(p);
                        subjectTeacherMap.remove(Subject.UNKNOWN);
                        wrld.setIntoSchoolMultiSubject(subjectTeacherMap.size() > 1);

                        //进校拜访KP
                        if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
                            long visitKpCount = visitUserInfoList.stream().filter(item -> !Objects.equals(item.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)).count();
                            wrld.setIntoSchoolVisitKp(visitKpCount > 0);
                        } else {
                            wrld.setIntoSchoolVisitKp(false);
                        }

                        //组会
                    } else if (p.getWorkType() == AgentWorkRecordType.MEETING) {
                        wrld.setWorkRecordRemarks(p.getWorkTitle() == null ? "" : p.getWorkTitle());
                        wrld.setMeetingType(p.getMeetingType());
                        wrld.setMeetingLength(p.getMeetingTime());
                        wrld.setMeetingPersonCount(p.getMeetingCount());
                        //陪访
                    } else if (p.getWorkType() == AgentWorkRecordType.ACCOMPANY) {
                        WorkRecordData workRecordData = null;
                        if (p.getIsOldBusinessRecordId()) {
                            CrmWorkRecord crmWorkRecord = crmWorkRecordLoaderClient.load(p.getBusinessRecordId());
                            workRecordData = workRecordDataCompatibilityService.transformOldToWorkRecordDataList(Collections.singletonList(crmWorkRecord)).stream().findFirst().orElse(null);
                        } else {
                            workRecordData = workRecordDataCompatibilityService.getWorkRecordDataByIdAndType(p.getBusinessRecordId(), AgentWorkRecordType.nameOf(p.getBusinessType().name()));
                        }
                        if (workRecordData != null) {
                            //进校
                            if (workRecordData.getWorkType() == AgentWorkRecordType.SCHOOL) {
                                if (workRecordData.getVisitSchoolType() != null && workRecordData.getVisitSchoolType() == 1) {
                                    wrld.setWorkRecordRemarks("进校-" + workRecordData.getSchoolName() + "-校级会议");
                                } else if (workRecordData.getVisitSchoolType() != null && workRecordData.getVisitSchoolType() == 3) {
                                    wrld.setWorkRecordRemarks("进校-" + workRecordData.getSchoolName() + "-直播展位推广");
                                } else {
                                    wrld.setWorkRecordRemarks("进校-" + workRecordData.getSchoolName());
                                }
                            }
                            //组会
                            if (workRecordData.getWorkType() == AgentWorkRecordType.MEETING) {
                                //组会主题
                                wrld.setWorkRecordRemarks("组会-" + workRecordData.getWorkTitle());
                            }
                            //资源拓维
                            if (workRecordData.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION) {
                                //资源拓维主题
                                List<String> titleList = generateResourceExtensionTitleList(workRecordData);
                                wrld.setWorkRecordRemarks(StringUtils.join(titleList, ","));
                            }
                            wrld.setAccompanyVisitPerson(p.getAccompanyUserName());
                        }
                        //资源拓维
                    } else if (p.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION) {
                        List<String> titleList = generateResourceExtensionTitleList(p);
                        wrld.setWorkRecordRemarks(StringUtils.join(titleList, ","));
                        List<String> manageRegionList = generateResourceExtensionManageRegionList(p);
                        wrld.setVisitResearcherManageRegion(StringUtils.join(manageRegionList, ","));
                    }

                    //工作量T
                    AgentRecordWorkload workload = workRecordDataCompatibilityService.getWorkload(p.getId(), p.getWorkType());
                    if (workload != null) {
                        wrld.setWorkload(workload.getWorkload());
                    }

                    resultList.add(wrld);
                }
                WorkRecordMiddleSortData wrmsd = new WorkRecordMiddleSortData();
                wrmsd.setSortDate(day);
                wrmsd.setWorkRecordListData(resultList);
                wrldResult.add(wrmsd);
            });
        }
        return wrldResult;
    }


//    /**
//     * 获取教研员级别
//     * @param level
//     * @return
//     */
//    public String getResearcherLevelStr(Integer level){
//        String levelStr = "";
//        if (level == 1){
//            levelStr = "省级";
//        }
//        if (level == 2){
//            levelStr = "市级";
//        }
//        if (level == 3){
//            levelStr = "区级";
//        }
//        return levelStr;
//    }

//    /**
//     * 获取拜访教研员显示标题
//     * @param crmWorkRecord
//     * @return
//     */
//    public String getVisitResearcherTitle(CrmWorkRecord crmWorkRecord){
//        List<CrmVisitResearcherInfo> visitedResearcherList = crmWorkRecord.getVisitedResearcherList();
//        String visitResearcherTitle = "";
//        if (CollectionUtils.isNotEmpty(visitedResearcherList)){
//            CrmVisitResearcherInfo crmVisitResearcherInfo = visitedResearcherList.get(0);
//            AgentResearchers agentResearchers = agentResearchersService.loadResearchers(crmVisitResearcherInfo.getResearcherId());
//
//            visitResearcherTitle += getResearcherLevelStr(agentResearchers.getLevel()) + (( null != agentResearchers.getSubject() && agentResearchers.getSubject() != Subject.UNKNOWN) ? agentResearchers.getSubject().getValue() : "" )+ "教研员" + agentResearchers.getName();
//            if (visitedResearcherList.size() >= 2){
//                CrmVisitResearcherInfo visitResearcherInfo = visitedResearcherList.get(1);
//                visitResearcherTitle += "、" + visitResearcherInfo.getResearcherName() + "等" + visitedResearcherList.size() + "位";
//            }
//            //兼容历史拜访记录
//        }else {
//            AgentResearchers agentResearchers = agentResearchersService.loadResearchers(crmWorkRecord.getResearchersId());
//            visitResearcherTitle += getResearcherLevelStr(agentResearchers.getLevel()) + (( null != agentResearchers.getSubject() && agentResearchers.getSubject() != Subject.UNKNOWN) ? agentResearchers.getSubject().getValue() : "" ) + "教研员" + agentResearchers.getName();
//        }
//        return visitResearcherTitle;
//    }

    /**
     * 通过工作记录id 查找工作记录详情
     *
     * @param workRecordId
     * @return
     */
    public Map<String, Object> workRecordDetailsByWorkRecordId(String workRecordId) {
        CrmWorkRecord cwr = crmWorkRecordLoaderClient.load(workRecordId);
        Map<String, Object> resultMap = null;
        if (cwr != null) {//判断数据是否为空
            resultMap = new HashMap();
            //数据转换 使其更具有可读性， 简单性
            resultMap.put("workRecordType", cwr.getWorkType());//工作记录类型
            resultMap.put("workRecordTime", DateUtils.calculateDateDayToSqlDate(cwr.getWorkTime(), 0));//工作记录时间  #格式化日期
            resultMap.put("workRecordCreator", cwr.getInterviewerName());//工作记录创建者
            resultMap.put("workRecordTitle", cwr.getWorkTitle());//工作记录标题
            resultMap.put("meeteeCount", cwr.getMeeteeCount());//参加会议人数
            resultMap.put("instructor", cwr.getMeetingNote());//讲师
            resultMap.put("meetingLevel", cwr.getMeetingType());//会议级别
            resultMap.put("meetingTime", cwr.getMeetingTime());//会议时长
            resultMap.put("conferenceType", cwr.getShowFrom());//会议类型
            resultMap.put("conferenceAddress", cwr.getCityName() + " " + cwr.getCountyName());//会议地点
            resultMap.put("conferenceAgent", cwr.getAgencyName());//会议代理人
            resultMap.put("instructorName", cwr.getInstructorName());//教研员-组会中教研员
            resultMap.put("researchers", cwr.getResearchersName());//教研员
            resultMap.put("mobile", cwr.getInstructorMobile());     //电话
            resultMap.put("instructorAttend", cwr.getInstructorAttend());//教研员是否在场
            resultMap.put("scenePhotoUrl", cwr.getScenePhotoUrl());//会议现场照片
            resultMap.put("conferenceContent", cwr.getWorkContent());//会议内容
            resultMap.put("researchersVisit", cwr.getResearchersName());//教研员拜访人员
            resultMap.put("researchersVisitedIntention", cwr.getVisitedIntention());//教研员拜访目的
            resultMap.put("researchersVisitContent", cwr.getVisitedFlow());//教研员拜访内容
            resultMap.put("researchersVisitResult", cwr.getVisitedConclusion());//教研员拜访结果
            resultMap.put("researchersVisitAddress", cwr.getVisitedPlace());//工作记录地点 ---教研员拜访地点
            resultMap.put("intoSchoolName", cwr.getSchoolName());//进校学校名称
            resultMap.put("intoSchoolTitle", cwr.getWorkTitle());//进校拜访主题
            resultMap.put("intoSchoolTeacher", cwr.getTeacherName());//进校拜访老师
            resultMap.put("intoSchoolVisit", cwr.getPartnerName());//进校陪访人
            resultMap.put("intoSchoolAgent", cwr.getAgencyName());//进校代理人
            resultMap.put("intoSchoolRemarks", cwr.getWorkContent());//进校效果及详情
            resultMap.put("partnerSuggest", cwr.getPartnerSuggest());//进校陪访建议
            resultMap.put("intoSchooAddress", cwr.getAddress());//进校地址
            //进校拜访的老师需要特殊处理
            resultMap.put("intoSchoolTeacher", cwr.getVisitTeacherList());//进校拜访老师
            resultMap.put("crmWorkRecord", cwr);
        }
        return resultMap;
    }

    public Map<String, Object> resourceExtensionDetailToMap(WorkRecordData workRecordData) {
        Map<String, Object> dataMap = new HashMap<>();
        List<WorkRecordVisitUserInfo> visitUserInfoList = workRecordData.getVisitUserInfoList();

        dataMap.put("visitNameStr", StringUtils.join(visitUserInfoList.stream().map(WorkRecordVisitUserInfo::getName).collect(Collectors.toList()), "、"));  //拜访人员
        dataMap.put("visitIntention", workRecordData.getVisitIntention());                              //拜访目的
        dataMap.put("visitAddress", workRecordData.getAddress());        //拜访地点
        dataMap.put("visitContent", workRecordData.getContent());                                     //拜访过程
        dataMap.put("visitUserInfoList", visitUserInfoList);                                //教研员拜访结果
        //陪同人信息
        List<Map<String, Object>> visitWorkRecordList = getVisitorList(workRecordData.getId());
        dataMap.put("accompanyWorkRecordList", visitWorkRecordList);                                         //陪同人列表
        return dataMap;
    }

    //获取拜访人姓名
    public List<Map<String, Object>> getVisitorList(String wordRecordId) {
        //陪同人信息
        List<Map<String, Object>> visitWorkRecordList = new ArrayList<>();
        List<WorkRecordData> workRecordDataList = getAccompanyRecordsByBusinessRecordId(wordRecordId);
        workRecordDataList.forEach(item -> {
            Map<String, Object> visitWorkRecordMap = new HashMap<>();
            visitWorkRecordMap.put("id", item.getId());
            visitWorkRecordMap.put("userId", item.getUserId());
            visitWorkRecordMap.put("userName", item.getUserName());
            visitWorkRecordList.add(visitWorkRecordMap);
        });
        return visitWorkRecordList;
    }

    public List<CrmWorkRecord> getAllWorkerWorkRecords(Long worker, CrmWorkRecordType recordType) {
        if (worker == null) {
            return Collections.emptyList();
        }
        List<CrmWorkRecord> workRecords = crmWorkRecordLoaderClient.findAllByWorker(worker, recordType);
        workRecords = workRecords.stream()
                .filter(p -> !p.getDisabled())
                .collect(Collectors.toList());
        return workRecords;
    }

    /**
     * 获取需要填写陪访的当天的拜访记录
     *
     * @param userName
     * @param currentUser
     * @return
     */
    public List<WorkRecordData> searchNeedAccompanyWorkRecord(String userName, AuthCurrentUser currentUser) {
        List<AgentGroupUser> agentGroupUserList = baseOrgService.getGroupUserByUser(currentUser.getUserId());
        if (CollectionUtils.isNotEmpty(agentGroupUserList)) {
            //部门及其子部门下所有用户
            List<AgentGroupUser> managedUserIdsUsers = baseOrgService.getAllGroupUsersByGroupId(agentGroupUserList.get(0).getGroupId());
            Set<Long> managedUserIds = managedUserIdsUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            //移除自己的ID
            managedUserIds.remove(currentUser.getUserId());

            //如果查询更关键词不为空，则需要按关键词结果过滤
            if (StringUtils.isNotEmpty(userName)) {
                List<AgentUser> searchedAgentUserList = agentUserLoaderClient.findByRealName(userName);
                Map<Long, AgentUser> searchedAgentUserMap = searchedAgentUserList.stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
                managedUserIds = managedUserIds.stream().filter(item -> searchedAgentUserMap.containsKey(item)).collect(Collectors.toSet());
            }
            Date startDate = DayRange.current().getStartDate();
            Date endDate = DayRange.current().getEndDate();
            //获取当天直属部门及其所有子部门人员拜访记录
            List<WorkRecordData> workRecordDataList = workRecordDataCompatibilityService.getWorkRecordDataListByUserTypeTime(managedUserIds, null, startDate, endDate);
            //当前用户当天填写的陪同记录
            List<WorkRecordData> visitWorkRecordDataList = workRecordDataCompatibilityService.getWorkRecordDataListByUserTypeTime(Collections.singleton(currentUser.getUserId()), AgentWorkRecordType.ACCOMPANY, startDate, endDate);
            Map<String, WorkRecordData> visitWorkRecordMap = visitWorkRecordDataList.stream().filter(item -> StringUtils.isNotBlank(item.getBusinessRecordId())).collect(Collectors.toMap(WorkRecordData::getBusinessRecordId, Function.identity(), (o1, o2) -> o2));
            //过滤出当天直属部门及其所有子部门人员已拜访但是本人未陪同的拜访记录
            List<WorkRecordData> effectiveWorkRecords = workRecordDataList.stream().filter(item -> item.getWorkType() != AgentWorkRecordType.ACCOMPANY && !visitWorkRecordMap.containsKey(item.getId())).collect(Collectors.toList());
            if (effectiveWorkRecords.size() > 30) {
                effectiveWorkRecords = effectiveWorkRecords.subList(0, 30);
            }
            return effectiveWorkRecords;
        }
        return new ArrayList<>();
    }

    /**
     * 获取需要填写陪访的当天的拜访记录
     *
     * @param userName
     * @param currentUser
     * @return
     */
    public List<Map<String, Object>> searchNeedAccompanyWorkRecordMap(String userName, AuthCurrentUser currentUser) {
        List<WorkRecordData> workRecordList = searchNeedAccompanyWorkRecord(userName, currentUser);
        if (CollectionUtils.isEmpty(workRecordList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        workRecordList.forEach(item -> {
            if (null != item) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("id", item.getId());
                dataMap.put("workerName", item.getUserName());
                dataMap.put("workType", item.getWorkType());
                dataMap.put("workTime", DateUtils.dateToString(item.getWorkTime(), "HH:mm"));
                //进校
                if (item.getWorkType() == AgentWorkRecordType.SCHOOL) {
                    if (item.getVisitSchoolType() == 1) {
                        dataMap.put("schoolName", item.getSchoolName() + "-校级会议");
                    } else if (item.getVisitSchoolType() == 3) {
                        dataMap.put("schoolName", item.getSchoolName() + "-直播展位推广");
                    } else {
                        dataMap.put("schoolName", item.getSchoolName());
                    }
                }
                //组会
                if (item.getWorkType() == AgentWorkRecordType.MEETING) {
                    //组会主题
                    dataMap.put("workTitle", item.getWorkTitle());
                }
                //资源拓维
                if (item.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION) {
                    //资源拓维主题
                    dataMap.put("resourceExtensionTitle", StringUtils.join(generateResourceExtensionTitleList(item), "、"));
                    //资源拓维负责区域
                    dataMap.put("resourceExtensionManageRegion", StringUtils.join(generateResourceExtensionManageRegionList(item), "、"));
                }
                dataList.add(dataMap);
            }
        });
        return dataList;
    }

    /**
     * 获取可以参与的组会记录
     * 当天创建的组会且当前参与人未填写过参与记录的
     * 1、专员：分区内所有专员、所在分区市经理、所在区域经理、所在大区大区经理的数据
     * 2、市经理：下属所有人员、所在区域经理、大区经理
     * 3、区域经理：下属所有人员、大区经理
     * 4、大区经理：下属所有人员
     * 5、业务部负责人、全国总监：下属所有人员
     *
     * @param currentUser
     * @return
     */
    public List<CrmWorkRecord> getCanJoinMeetingRecords(AuthCurrentUser currentUser) {
        if (null == currentUser) {
            return Collections.emptyList();
        }
        List<AgentGroupUser> agentGroupUserList = baseOrgService.getGroupUserByUser(currentUser.getUserId());
        if (CollectionUtils.isNotEmpty(agentGroupUserList)) {
            Long groupId = agentGroupUserList.get(0).getGroupId();
            //部门下所有用户
            List<AgentGroupUser> managedUserIdsUsers = baseOrgService.getAllGroupUsersByGroupId(groupId);
            //部门下所有用户ID
            Set<Long> managedUserIds = managedUserIdsUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            Set<Long> allGroupManagers = baseOrgService.getAllGroupManagers(groupId);
            managedUserIds.addAll(allGroupManagers);
            //移除自己的ID
            managedUserIds.remove(currentUser.getUserId());
            Date startDate = DayRange.current().getStartDate();
            Date endDate = DayRange.current().getEndDate();
            //获取当天的组会记录
            List<CrmWorkRecord> meetingWorkRecords = crmWorkRecordLoaderClient.listByWorkersAndType(managedUserIds, CrmWorkRecordType.MEETING, startDate, endDate);
            //当前用户当天填写的参与组会记录
            List<CrmWorkRecord> joinMeetingWorkRecords = crmWorkRecordLoaderClient.listByWorkerAndType(currentUser.getUserId(), CrmWorkRecordType.JOIN_MEETING, startDate, endDate);

            Map<String, CrmWorkRecord> joinMeetingWorkRecordMap = joinMeetingWorkRecords.stream().filter(item -> null != item.getSchoolWorkRecordId()).collect(Collectors.toMap(CrmWorkRecord::getSchoolWorkRecordId, Function.identity(), (o1, o2) -> o2));
            //本人未填写参与组会的记录
            List<CrmWorkRecord> effectiveMeetingRecords = meetingWorkRecords.stream().filter(item -> !joinMeetingWorkRecordMap.containsKey(item.getId())).collect(Collectors.toList());

            return effectiveMeetingRecords;
        }
        return new ArrayList<>();

    }

    /**
     * 根据进校ID查找陪访记录
     *
     * @param intoSchoolRecordId
     * @return
     */
    public List<CrmWorkRecord> getVisitRecordsByIntoRecordId(String intoSchoolRecordId) {
        return crmWorkRecordLoaderClient.getVisitRecordsByIntoRecordId(intoSchoolRecordId);
    }


    /**
     * 根据组会ID查找参与组会记录
     *
     * @param recordId
     * @return
     */
    public List<CrmWorkRecord> getJoinMeetingRecordsByIntoRecordId(String recordId) {
        return crmWorkRecordLoaderClient.getJoinMeetingRecordsByIntoRecordId(recordId);
    }

    public void saveRecordWorkload(WorkRecordData workRecord) {
        if (workRecord == null || StringUtils.isBlank(workRecord.getId())) {
            return;
        }
        //计算工作量T
        Double workload = workRecord.getWorkload();
        if (workload == null) {
            workload = calRecordWorkload(workRecord);
        }
        AgentRecordWorkload recordWorkload = agentRecordWorkloadDao.loadByWorkRecordIdAndType(workRecord.getId(), workRecord.getWorkType());
        if (recordWorkload == null) {
            recordWorkload = new AgentRecordWorkload();
            recordWorkload.setWorkRecordId(workRecord.getId());
            recordWorkload.setWorkRecordType(workRecord.getWorkType());
            recordWorkload.setWorkload(workload);
            agentRecordWorkloadDao.insert(recordWorkload);
        } else {
            recordWorkload.setWorkload(workload);
            agentRecordWorkloadDao.replace(recordWorkload);
        }
    }


    /**
     * 工作量T计算方法
     * <p>
     * 工作量T计算规则
     * 类别                                                                  	专员       市经理       区域经理      	    大区经理
     * 单科进校（除跨科外进校外的进校）                                      	0.5	        0.5	            0.5            	   0.5
     * 跨科进校（勾选了大于等于2个科目的老师，科目包含：语数英政史地理化生信）	 1	        1	            1	                 1
     * 拜访KP（校内KP：校长/主任等，不包含未注册老师）进校，有1KP以上即可       2	        2	            2	                2
     * 陪访单科进校	                                                         /	        0.5	            0.5	              0.5
     * 陪访跨科进校	                                                         /      	1	            1	                1
     * 陪访进校（拜访KP）	                                                     /	        2	            2	                2
     * 组织校级组会（≥6位老师）	                                             2  	    2	            2	                2
     * 组织省市区级组会（≥30位老师）											 5			5				5					5
     * 参与或陪访校级组会（≥6位老师）											 1			1				1					1
     * 参与或陪访省市区级组会（≥30位老师）                           			 2			2				2					2
     * 拜访教研员																 2			2				2					2
     *
     * @param workRecord
     * @return 工作量T
     */
    public double calRecordWorkload(WorkRecordData workRecord) {
        double result = 0d;
        if (workRecord == null) {
            return result;
        }
        AgentWorkRecordType type = workRecord.getWorkType();
        if (type == AgentWorkRecordType.SCHOOL) {
            List<WorkRecordVisitUserInfo> visitUserInfoList = workRecord.getVisitUserInfoList();
            // 校级会议
            if (SafeConverter.toInt(workRecord.getVisitSchoolType()) == 1) {
                if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
                    int visitCount = visitUserInfoList.size();
                    if (visitCount >= 6) {
                        result = MathUtils.doubleAdd(result, 2);
                    }
                }
                //直播展位推广
            } else if (SafeConverter.toInt(workRecord.getVisitSchoolType()) == 3) {
                result = MathUtils.doubleAdd(result, 1);
            } else {
                if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
                    //拜访KP
                    long visitKpCount = visitUserInfoList.stream().filter(p -> !Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB) && !Objects.equals(p.getJob(), ResearchersJobType.UNREGISTERED_TEACHER.getJobId())).count();
                    if (visitKpCount >= 1) {
                        result = MathUtils.doubleAdd(result, 2);
                    } else {
                        //单科进校，跨科进校
                        boolean isMultiSubject = false;
                        List<WorkRecordVisitUserInfo> visitTeacherInfoList = visitUserInfoList.stream().filter(p -> Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB) || Objects.equals(p.getJob(), ResearchersJobType.UNREGISTERED_TEACHER.getJobId())).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(visitTeacherInfoList)) {
                            Set<Subject> subjects = new HashSet<>();
                            Set<Long> noSubjectIds = new HashSet<>();
                            for (WorkRecordVisitUserInfo teacherVisitInfo : visitTeacherInfoList) {
                                Subject subject = teacherVisitInfo.getSubject();
                                if (subject != null) {
                                    subjects.add(subject);
                                } else {
                                    if (!SCHOOL_MASTER_INFO.containsKey(teacherVisitInfo.getId())) {
                                        noSubjectIds.add(teacherVisitInfo.getId());
                                    }
                                }
                            }
                            if (CollectionUtils.isNotEmpty(noSubjectIds)) {
                                Map<Long, CrmTeacherSummary> teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(noSubjectIds);
                                if (MapUtils.isNotEmpty(teacherSummaryMap)) {
                                    for (CrmTeacherSummary summary : teacherSummaryMap.values()) {
                                        Subject subject = Subject.safeParse(summary.getSubject());
                                        if (subject != null) {
                                            subjects.add(subject);
                                        }
                                    }
                                }
                            }
                            if (subjects.size() > 1) {
                                isMultiSubject = true;
                            }
                        }
                        if (isMultiSubject) {
                            result = MathUtils.doubleAdd(result, 1);
                        } else {
                            result = MathUtils.doubleAdd(result, 0.5);
                        }
                    }
                }
            }
        } else if (type == AgentWorkRecordType.ACCOMPANY) {
            WorkRecordData relatedRecord = getAccompanyWorkRecord(workRecord);
            if (relatedRecord != null) {
                AgentWorkRecordType relatedType = relatedRecord.getWorkType();
                if (relatedType == AgentWorkRecordType.SCHOOL) {
                    // 校级会议
                    if (SafeConverter.toInt(relatedRecord.getVisitSchoolType()) == 1) {
                        result = MathUtils.doubleAdd(result, MathUtils.doubleDivide(calRecordWorkload(relatedRecord), 2));
                    } else {
                        result = MathUtils.doubleAdd(result, calRecordWorkload(relatedRecord));
                    }
                } else if (relatedType == AgentWorkRecordType.MEETING) {
                    result = MathUtils.doubleAdd(result, MathUtils.doubleDivide(calRecordWorkload(relatedRecord), 2, 0, BigDecimal.ROUND_FLOOR));
                } else if (relatedType == AgentWorkRecordType.RESOURCE_EXTENSION) {
                    result = MathUtils.doubleAdd(result, MathUtils.doubleDivide(calRecordWorkload(relatedRecord), 2));
                }
            }

        } else if (type == AgentWorkRecordType.MEETING) {
            CrmMeetingType meetingType = workRecord.getMeetingType();
            int visitCount = workRecord.getMeetingCount();
            if (meetingType == CrmMeetingType.SCHOOL_LEVEL && visitCount >= 6) {
                result = MathUtils.doubleAdd(result, 2);
            }
            if ((meetingType == CrmMeetingType.PROVINCE_LEVEL || meetingType == CrmMeetingType.CITY_LEVEL || meetingType == CrmMeetingType.COUNTY_LEVEL) && visitCount >= 30) {
                result = MathUtils.doubleAdd(result, 5);
            }
        } else if (type == AgentWorkRecordType.RESOURCE_EXTENSION) {
            List<WorkRecordVisitUserInfo> visitUserInfoList = workRecord.getVisitUserInfoList();
            if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
                //拜访KP
                List<WorkRecordVisitUserInfo> visitKpInfoList = visitUserInfoList.stream().filter(p -> !Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB) && !Objects.equals(p.getJob(), ResearchersJobType.UNREGISTERED_TEACHER.getJobId())).collect(Collectors.toList());
                int count = visitKpInfoList.size();
                if (count > 1) {
                    result = MathUtils.doubleAdd(result, 4);
                } else {
                    result = MathUtils.doubleAdd(result, 2);
                }
            }
        }
        return result;
    }

    public Map<String, String> loadWorkRecordLocation(String workRecordId) {
        CrmWorkRecord workRecord = crmWorkRecordLoaderClient.load(workRecordId);
        if (workRecord != null && StringUtils.isNotBlank(workRecord.getCoordinateType()) && StringUtils.isNotBlank(workRecord.getLatitude()) && StringUtils.isNotBlank(workRecord.getLongitude())) {
            Map<String, String> info = new HashMap<>();
            info.put("latitude", workRecord.getLatitude());
            info.put("longitude", workRecord.getLongitude());
            info.put("coordinateType", workRecord.getCoordinateType());
            return info;
        }
        return null;
    }

    // 获取专员一段时间范围内的进校记录
    public List<CrmWorkRecord> loadCrmWorkRecordByUsers(Long bdId, Date startDate, Date endDate) {
        List<Long> schoolIds = baseOrgService.getUserSchools(bdId, 0);
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        // 本月的工作记录
        return loadBdJuniorIntoSchoolRecord(bdId, schoolIds, startDate, endDate);
    }


    /**
     * 首页-我的进校统计
     *
     * @param userId
     * @return
     */
    public Map<String, Object> getMyIntoSchoolStatistics(Long userId, Date date) {
        Map<String, Object> dataMap = new HashMap<>();

        int monthIntoSchoolSum = 0;     //月进校数
        int dayIntoSchoolSum = 0;       //今日进校数
        int monthVisitTeacherSum = 0;   //月见师量
        int dayVisitTeacherSum = 0;     //今日见师量

        int englishVisitTeacherSum = 0; //英语见师量
        int mathVisitTeacherSum = 0;    //数学见师量
        int otherVisitTeacherSum = 0;   //其他见师量
        //月工作量
        Map<Long, AgentWorkRecordStatistics> monthStatisticsMap = agentWorkRecordStatisticsService.getUserStatistics(Collections.singleton(userId), date, 3);
        if (MapUtils.isNotEmpty(monthStatisticsMap)) {
            AgentWorkRecordStatistics monthWorkRecordStatistics = monthStatisticsMap.get(userId);
            if (monthWorkRecordStatistics != null) {
                monthIntoSchoolSum = monthWorkRecordStatistics.getBdIntoSchoolCount() != null ? monthWorkRecordStatistics.getBdIntoSchoolCount() : 0;
                monthVisitTeacherSum = monthWorkRecordStatistics.getBdVisitTeacherCount() != null ? monthWorkRecordStatistics.getBdVisitTeacherCount() : 0;
                englishVisitTeacherSum = monthWorkRecordStatistics.getBdVisitEngTeacherCount() != null ? monthWorkRecordStatistics.getBdVisitEngTeacherCount() : 0;
                mathVisitTeacherSum = monthWorkRecordStatistics.getBdVisitMathTeacherCount() != null ? monthWorkRecordStatistics.getBdVisitMathTeacherCount() : 0;
                otherVisitTeacherSum = monthWorkRecordStatistics.getBdVisitOtherTeacherCount() != null ? monthWorkRecordStatistics.getBdVisitOtherTeacherCount() : 0;
            }
        }

        //当日工作量
        Map<Long, AgentWorkRecordStatistics> dayStatisticsMap = agentWorkRecordStatisticsService.getUserStatistics(Collections.singleton(userId), date, 1);
        if (MapUtils.isNotEmpty(dayStatisticsMap)) {
            AgentWorkRecordStatistics dayWorkRecordStatistics = dayStatisticsMap.get(userId);
            if (dayWorkRecordStatistics != null) {
                dayIntoSchoolSum = dayWorkRecordStatistics.getBdIntoSchoolCount() != null ? dayWorkRecordStatistics.getBdIntoSchoolCount() : 0;
                dayVisitTeacherSum = dayWorkRecordStatistics.getBdVisitTeacherCount() != null ? dayWorkRecordStatistics.getBdVisitTeacherCount() : 0;
            }
        }

        //月进校数
        dataMap.put("monthIntoSchoolSum", monthIntoSchoolSum);
        //今日进校数
        dataMap.put("dayIntoSchoolSum", dayIntoSchoolSum);
        //月见师量
        dataMap.put("monthVisitTeacherSum", monthVisitTeacherSum);
        //今日见师量
        dataMap.put("dayVisitTeacherSum", dayVisitTeacherSum);

        //英语见师量
        dataMap.put("englishVisitTeacherSum", englishVisitTeacherSum);
        //英语见师比例
        dataMap.put("englishVisitTeacherRate", MathUtils.doubleDivide(englishVisitTeacherSum, monthVisitTeacherSum));
        //数学见师量
        dataMap.put("mathVisitTeacherSum", mathVisitTeacherSum);
        //数学见师比例
        dataMap.put("mathVisitTeacherRate", MathUtils.doubleDivide(mathVisitTeacherSum, monthVisitTeacherSum));
        //其他见师量
        dataMap.put("otherVisitTeacherSum", otherVisitTeacherSum);
        //其他见师比例
        dataMap.put("otherVisitTeacherRate", MathUtils.doubleDivide(otherVisitTeacherSum, monthVisitTeacherSum));

        return dataMap;
    }


    /**
     * 首页-过程月榜统计
     *
     * @param user
     * @param searchType
     * @return
     */
    public MapMessage processMonthlyRankingStatistics(AuthCurrentUser user, Integer searchType, Date date) {
        MapMessage mapMessage = MapMessage.successMessage();
        // 获取用户所在的部门
        Long groupId = 0L;
        List<Long> userGroupIds = baseOrgService.getGroupIdListByUserId(user.getUserId());
        if (CollectionUtils.isNotEmpty(userGroupIds)) {
            groupId = userGroupIds.get(0);
        }

        List<Long> cityGroupIds = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();
        //获取所有部门
        List<AgentGroup> allGroups = baseOrgService.findAllGroups();

        AgentGroup businessUnit = new AgentGroup();
        //全国角色及管理员
        if (user.isAdmin() || user.isCountryManager()) {
            if (searchType == 1) {
                //级别为分区、业务类型为小学的部门
                cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).map(AgentGroup::getId).collect(Collectors.toList()));
            } else if (searchType == 2) {
                //级别为分区、业务类型为初中和高中的部门
                cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).map(AgentGroup::getId).collect(Collectors.toList()));
            }
        } else {
            // 获取用户所在的业务部
            businessUnit = baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.BusinessUnit);
            if (businessUnit != null) {
                //小学业务部
                if (StringUtils.contains(businessUnit.getGroupName(), "小学")) {
                    //级别为分区、业务类型为小学的部门
                    cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).map(AgentGroup::getId).collect(Collectors.toList()));
                    //中学业务部
                } else if (StringUtils.contains(businessUnit.getGroupName(), "中学")) {
                    //级别为分区、业务类型为初中和高中的部门
                    cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).map(AgentGroup::getId).collect(Collectors.toList()));
                }
            }
        }

        //获取指定部门（多个）中，指定角色的用户
        userIds.addAll(baseOrgService.getUserByGroupIdsAndRole(cityGroupIds, AgentRoleType.BusinessDeveloper));

        List<AgentProcessMonthlyRanking> processMonthlyRankingList = new ArrayList<>();

        List<AgentWorkRecordStatistics> statisticsList = new ArrayList<>();
        Map<Long, AgentWorkRecordStatistics> statisticsMap = agentWorkRecordStatisticsService.getUserStatistics(userIds, date, 3);
        if (MapUtils.isNotEmpty(statisticsMap)) {
            statisticsList.addAll(statisticsMap.values());
        }
        statisticsList.forEach(item -> {
            if (item != null) {
                //工作量
                AgentProcessMonthlyRanking workload = new AgentProcessMonthlyRanking();
                workload.setUserId(item.getUserId());
                workload.setWorkType(1);
                workload.setWorkNum(item.getUserWorkload());
                processMonthlyRankingList.add(workload);
                //进校量
                AgentProcessMonthlyRanking intoSchool = new AgentProcessMonthlyRanking();
                intoSchool.setUserId(item.getUserId());
                intoSchool.setWorkType(2);
                intoSchool.setWorkNum(SafeConverter.toDouble(item.getBdIntoSchoolCount()));
                processMonthlyRankingList.add(intoSchool);
                //见师量
                AgentProcessMonthlyRanking visitTeacher = new AgentProcessMonthlyRanking();
                visitTeacher.setUserId(item.getUserId());
                visitTeacher.setWorkType(3);
                visitTeacher.setWorkNum(SafeConverter.toDouble(item.getBdVisitTeacherCount()));
                processMonthlyRankingList.add(visitTeacher);

            }
        });

        //工作量
        AgentUser agentUser = new AgentUser();
        AgentProcessMonthlyRanking workloadRanking = processMonthlyRankingList.stream().filter(p -> p.getWorkType() == 1 && p.getWorkNum() > 0).sorted(Comparator.comparing(AgentProcessMonthlyRanking::getWorkNum).reversed()).findFirst().orElse(null);
        if (null != workloadRanking) {
            agentUser = baseOrgService.getUser(workloadRanking.getUserId());
            workloadRanking.setUserName(agentUser.getRealName());
            workloadRanking.setUserAvatar(agentUser.getAvatar());
        }

        //进校量
        AgentProcessMonthlyRanking intoSchoolRanking = processMonthlyRankingList.stream().filter(p -> p.getWorkType() == 2 && p.getWorkNum() > 0).sorted(Comparator.comparing(AgentProcessMonthlyRanking::getWorkNum).reversed()).findFirst().orElse(null);
        if (null != intoSchoolRanking) {
            agentUser = baseOrgService.getUser(intoSchoolRanking.getUserId());
            intoSchoolRanking.setUserName(agentUser.getRealName());
            intoSchoolRanking.setUserAvatar(agentUser.getAvatar());
        }

        //见师量
        AgentProcessMonthlyRanking visitTeacherRanking = processMonthlyRankingList.stream().filter(p -> p.getWorkType() == 3 && p.getWorkNum() > 0).sorted(Comparator.comparing(AgentProcessMonthlyRanking::getWorkNum).reversed()).findFirst().orElse(null);
        if (null != visitTeacherRanking) {
            agentUser = baseOrgService.getUser(visitTeacherRanking.getUserId());
            visitTeacherRanking.setUserName(agentUser.getRealName());
            visitTeacherRanking.setUserAvatar(agentUser.getAvatar());
        }

        mapMessage.add("workload", workloadRanking);
        mapMessage.add("intoSchoolRanking", intoSchoolRanking);
        mapMessage.add("visitTeacherRanking", visitTeacherRanking);
        return mapMessage;
    }


    /**
     * 过程月榜明细
     *
     * @param user
     * @param searchType
     * @param date
     * @return
     */
    public MapMessage processMonthlyRankingDetail(AuthCurrentUser user, Integer searchType, Date date) {
        MapMessage mapMessage = MapMessage.successMessage();
        // 获取用户所在的部门
        Long groupId = 0L;
        List<Long> userGroupIds = baseOrgService.getGroupIdListByUserId(user.getUserId());
        if (CollectionUtils.isNotEmpty(userGroupIds)) {
            groupId = userGroupIds.get(0);
        }

        List<Long> cityGroupIds = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        //获取所有部门
        List<AgentGroup> allGroups = baseOrgService.findAllGroups();

        Boolean isJunior = true;//区分中小学（默认为小学）
        AgentGroup businessUnit = new AgentGroup();
        //全国角色及管理员
        if (user.isAdmin() || user.isCountryManager()) {
            if (searchType == 1) {
                //级别为分区、业务类型为小学的部门
                cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).map(AgentGroup::getId).collect(Collectors.toList()));
                isJunior = true;
            } else if (searchType == 2) {
                //级别为分区、业务类型为初中和高中的部门
                cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).map(AgentGroup::getId).collect(Collectors.toList()));
                isJunior = false;
            }
        } else {
            // 获取用户所在的业务部
            businessUnit = baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.BusinessUnit);
            if (businessUnit != null) {
                //小学业务部
                if (StringUtils.contains(businessUnit.getGroupName(), "小学")) {
                    //级别为分区、业务类型为小学的部门
                    cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).map(AgentGroup::getId).collect(Collectors.toList()));
                    isJunior = true;
                    //中学业务部
                } else if (StringUtils.contains(businessUnit.getGroupName(), "中学")) {
                    //级别为分区、业务类型为初中和高中的部门
                    cityGroupIds.addAll(allGroups.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).map(AgentGroup::getId).collect(Collectors.toList()));
                    isJunior = false;
                }
            }
        }

        //获取指定部门（多个）中，指定角色的用户
        userIds.addAll(baseOrgService.getUserByGroupIdsAndRole(cityGroupIds, AgentRoleType.BusinessDeveloper));
        //当月工作量
        List<AgentProcessMonthlyRanking> processMonthlyRankingList = new ArrayList<>();

        List<AgentWorkRecordStatistics> statisticsList = new ArrayList<>();
        Map<Long, AgentWorkRecordStatistics> statisticsMap = agentWorkRecordStatisticsService.getUserStatistics(userIds, date, 3);
        if (MapUtils.isNotEmpty(statisticsMap)) {
            statisticsList.addAll(statisticsMap.values());
        }
        statisticsList.forEach(item -> {
            if (item != null) {
                //工作量
                AgentProcessMonthlyRanking workload = new AgentProcessMonthlyRanking();
                workload.setUserId(item.getUserId());
                workload.setWorkType(1);
                workload.setWorkNum(item.getUserWorkload());
                processMonthlyRankingList.add(workload);
                //进校量
                AgentProcessMonthlyRanking intoSchool = new AgentProcessMonthlyRanking();
                intoSchool.setUserId(item.getUserId());
                intoSchool.setWorkType(2);
                intoSchool.setWorkNum(SafeConverter.toDouble(item.getBdIntoSchoolCount()));
                processMonthlyRankingList.add(intoSchool);
                //见师量
                AgentProcessMonthlyRanking visitTeacher = new AgentProcessMonthlyRanking();
                visitTeacher.setUserId(item.getUserId());
                visitTeacher.setWorkType(3);
                visitTeacher.setWorkNum(SafeConverter.toDouble(item.getBdVisitTeacherCount()));
                processMonthlyRankingList.add(visitTeacher);

            }
        });


        //人员
        Map<Long, AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
        //人员与部门关系
        Map<Long, List<AgentGroup>> userGroupsMap = baseOrgService.getUserGroups(userIds);
        processMonthlyRankingList.forEach(item -> {
            AgentUser agentUser = userMap.get(item.getUserId());
            List<AgentGroup> groupList = userGroupsMap.get(item.getUserId());
            if (CollectionUtils.isNotEmpty(groupList)) {
                AgentGroup group = groupList.get(0);
                item.setGroupId(group.getId());
                item.setGroupName(group.getGroupName());
            }
            item.setUserName(agentUser.getRealName());
            item.setUserAvatar(agentUser.getAvatar());
        });

        List<AgentProcessMonthlyRanking> workloadProcessMonthlyRankingList = new ArrayList<>();
        List<AgentProcessMonthlyRanking> intoSchoolProcessMonthlyRankingList = new ArrayList<>();
        List<AgentProcessMonthlyRanking> visitTeacherProcessMonthlyRankingList = new ArrayList<>();
        //工作量排名
        workloadProcessMonthlyRankingList = processMonthlyRankingList.stream().filter(p -> p.getWorkType() == 1 && p.getWorkNum() > 0).sorted(Comparator.comparing(AgentProcessMonthlyRanking::getWorkNum).reversed()).collect(Collectors.toList());
        //进校量排名
        intoSchoolProcessMonthlyRankingList = processMonthlyRankingList.stream().filter(p -> p.getWorkType() == 2 && p.getWorkNum() > 0).sorted(Comparator.comparing(AgentProcessMonthlyRanking::getWorkNum).reversed()).collect(Collectors.toList());
        //见师量排名
        visitTeacherProcessMonthlyRankingList = processMonthlyRankingList.stream().filter(p -> p.getWorkType() == 3 && p.getWorkNum() > 0).sorted(Comparator.comparing(AgentProcessMonthlyRanking::getWorkNum).reversed()).collect(Collectors.toList());
        //小学显示前50名，中学显示前20名
        if (isJunior) {
            workloadProcessMonthlyRankingList = workloadProcessMonthlyRankingList.stream().limit(50).collect(Collectors.toList());
            intoSchoolProcessMonthlyRankingList = intoSchoolProcessMonthlyRankingList.stream().limit(50).collect(Collectors.toList());
            visitTeacherProcessMonthlyRankingList = visitTeacherProcessMonthlyRankingList.stream().limit(50).collect(Collectors.toList());
        } else {
            workloadProcessMonthlyRankingList = workloadProcessMonthlyRankingList.stream().limit(20).collect(Collectors.toList());
            intoSchoolProcessMonthlyRankingList = intoSchoolProcessMonthlyRankingList.stream().limit(20).collect(Collectors.toList());
            visitTeacherProcessMonthlyRankingList = visitTeacherProcessMonthlyRankingList.stream().limit(20).collect(Collectors.toList());
        }
        mapMessage.put("workloadProcessMonthlyRankingList", workloadProcessMonthlyRankingList);
        mapMessage.put("intoSchoolProcessMonthlyRankingList", intoSchoolProcessMonthlyRankingList);
        mapMessage.put("visitTeacherProcessMonthlyRankingList", visitTeacherProcessMonthlyRankingList);
        //如果是专员,获取专员排名
        if (user.isBusinessDeveloper()) {
            Integer workloadRanking = 0;
            Integer intoSchoolRanking = 0;
            Integer visitTeacherRanking = 0;

            mapMessage.put("workloadBdProcessMonthlyRanking", getBusinessDevProcessMonthlyRanking(user, workloadRanking, workloadProcessMonthlyRankingList));
            mapMessage.put("intoSchoolBdProcessMonthlyRanking", getBusinessDevProcessMonthlyRanking(user, intoSchoolRanking, intoSchoolProcessMonthlyRankingList));
            mapMessage.put("visitTeacherBdProcessMonthlyRanking", getBusinessDevProcessMonthlyRanking(user, visitTeacherRanking, visitTeacherProcessMonthlyRankingList));
        }
        //用户角色
        AgentRoleType userRole = baseOrgService.getUserRole(user.getUserId());
        mapMessage.put("userRole", userRole);
        return mapMessage;
    }

    /**
     * 获取专员的排名信息
     *
     * @param user
     * @param ranking
     * @param processMonthlyRankingList
     * @return
     */
    public AgentProcessMonthlyRanking getBusinessDevProcessMonthlyRanking(AuthCurrentUser user, Integer ranking, List<AgentProcessMonthlyRanking> processMonthlyRankingList) {
        if (ranking == null) {
            ranking = 0;
        }
        AgentProcessMonthlyRanking businessDevProcessMonthlyRanking = null;
        for (AgentProcessMonthlyRanking processMonthlyRanking : processMonthlyRankingList) {
            ranking++;
            if (Objects.equals(processMonthlyRanking.getUserId(), user.getUserId())) {
                businessDevProcessMonthlyRanking = new AgentProcessMonthlyRanking();
                try {
                    BeanUtils.copyProperties(businessDevProcessMonthlyRanking, processMonthlyRanking);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                businessDevProcessMonthlyRanking.setRanking(ranking);
                break;
            }
        }
        return businessDevProcessMonthlyRanking;
    }

    /**
     * 部门及用户角色列表
     *
     * @param currentUser
     * @return
     */
    public List<Map<String, Object>> groupUserRoleTypeList(AuthCurrentUser currentUser) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        //当是全国总监或者大区经理或者区域经理或者市经理身份时，获取所在部门统计
        if (currentUser.isCountryManager() || currentUser.isRegionManager() || currentUser.isCityManager() || currentUser.isAreaManager()) {
            List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
            if (CollectionUtils.isEmpty(groupUserByUser)) {
                return Collections.emptyList();
            }
            List<Long> groupIds = new ArrayList<>();
            Long groupId = groupUserByUser.get(0).getGroupId();
            if (currentUser.isCountryManager() || currentUser.isAdmin()) {
                List<AgentGroup> groupList = baseOrgService.getSubGroupList(groupId);
                groupIds.addAll(groupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing).map(AgentGroup::getId).collect(Collectors.toList()));
            } else {
                groupIds.add(groupId);
            }

            List<AgentGroup> groupList = baseOrgService.getGroupByIds(groupIds);
            if (CollectionUtils.isEmpty(groupList)) {
                return Collections.emptyList();
            }
            groupList.forEach(group -> {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("groupId", group.getId());
                dataMap.put("groupName", group.getGroupName());
                List<AgentRoleType> userRoleTypeList = new ArrayList<>();
                userRoleTypeList.add(AgentRoleType.BusinessDeveloper);
                if (group.fetchGroupRoleType() == AgentGroupRoleType.Marketing) {
                    userRoleTypeList.add(AgentRoleType.CityManager);
                    if (StringUtils.contains(group.getGroupName(), "小学")) {
                        userRoleTypeList.add(AgentRoleType.AreaManager);
                    }
                    userRoleTypeList.add(AgentRoleType.Region);
                } else if (group.fetchGroupRoleType() == AgentGroupRoleType.Region) {
                    userRoleTypeList.add(AgentRoleType.CityManager);
                    AgentGroup businessUnit = baseOrgService.getParentGroupByRole(group.getId(), AgentGroupRoleType.Marketing);
                    if (businessUnit != null && StringUtils.contains(businessUnit.getGroupName(), "小学")) {
                        userRoleTypeList.add(AgentRoleType.AreaManager);
                    }
                } else if (group.fetchGroupRoleType() == AgentGroupRoleType.Area) {
                    userRoleTypeList.add(AgentRoleType.CityManager);
                }
                dataMap.put("userRoleTypeList", userRoleTypeList);
                dataList.add(dataMap);
            });
        }
        return dataList;
    }

    /**
     * 首页-团队工作量统计
     *
     * @param currentUser
     * @param date
     * @param groupId
     * @param userRoleType
     * @return
     */
    public Map<String, Object> teamWorkloadStatistics(AuthCurrentUser currentUser, Date date, Long groupId, String userRoleType) {
        Map<String, Object> dataMap = new HashMap<>();
        //当是全国总监或者大区经理或者区域经理或者市经理身份时，获取所在部门统计
        if (currentUser.isCountryManager() || currentUser.isRegionManager() || currentUser.isCityManager() || currentUser.isAreaManager()) {
            List<Long> groupIds = new ArrayList<>();
            //如果没有选择部门，全国总监或者管理员默认显示小学市场统计数据；其他角色默认显示本部门统计数据
            if (groupId == 0L) {
                List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
                if (CollectionUtils.isEmpty(groupUserByUser)) {
                    return MapMessage.errorMessage();
                }
                Long currentGroupId = groupUserByUser.get(0).getGroupId();
                if (currentUser.isCountryManager() || currentUser.isAdmin()) {
                    List<AgentGroup> subGroupIdList = baseOrgService.getSubGroupList(currentGroupId);
                    groupIds.addAll(subGroupIdList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).map(AgentGroup::getId).collect(Collectors.toList()));
                } else {
                    groupIds.add(currentGroupId);
                }
            } else {
                groupIds.add(groupId);
            }

            //如果没有选择角色，默认显示专员的统计数据
            if (StringUtils.isBlank(userRoleType)) {
                userRoleType = "BusinessDeveloper";
            }
            AgentRoleType agentRoleType = AgentRoleType.valueOf(userRoleType);
            //当日工作量统计数据
            Map<Long, AgentWorkRecordStatistics> dayStatisticsMap = agentWorkRecordStatisticsService.getGroupStatistics(groupIds, date, 1);
            AgentWorkRecordStatistics dayWorkRecordStatistics = null;
            if (groupIds.size() > 0) {
                dayWorkRecordStatistics = dayStatisticsMap.get(groupIds.get(0));
            }
            WrStatisticsOverviewRoleData dayOverviewRoleData = new WrStatisticsOverviewRoleData();
            if (null != agentRoleType) {
                dayOverviewRoleData.setRoleId(agentRoleType.getId());
                dayOverviewRoleData.setRoleName(agentRoleType.getRoleName());
            }
            if (null != dayWorkRecordStatistics) {
                //获取该部门该角色的工作量统计数据
                AgentWorkRecordStatisticsRoleData dayWorkRecordStatisticsRoleData = dayWorkRecordStatistics.getRoleDataMap().get(agentRoleType.getId());
                if (null != dayWorkRecordStatisticsRoleData) {
                    dayOverviewRoleData.setUserCount(dayWorkRecordStatisticsRoleData.getUserCount() != null ? dayWorkRecordStatisticsRoleData.getUserCount() : 0);
                    dayOverviewRoleData.setFillRecordUserCount(dayWorkRecordStatisticsRoleData.getFillRecordUserCount() != null ? dayWorkRecordStatisticsRoleData.getFillRecordUserCount() : 0);
                    dayOverviewRoleData.setRecordUnreachedUserCount(dayWorkRecordStatisticsRoleData.getRecordUnreachedUserCount() != null ? dayWorkRecordStatisticsRoleData.getRecordUnreachedUserCount() : 0);
                    dayOverviewRoleData.setPerCapitaWorkload(dayWorkRecordStatisticsRoleData.getPerCapitaWorkload() != null ? dayWorkRecordStatisticsRoleData.getPerCapitaWorkload() : 0.0);
                }
            }
            dataMap.put("dayOverviewRoleData", dayOverviewRoleData);
            //当月工作量统计数据
            Map<Long, AgentWorkRecordStatistics> monthStatisticsMap = agentWorkRecordStatisticsService.getGroupStatistics(groupIds, date, 3);
            AgentWorkRecordStatistics monthWorkRecordStatistics = null;
            if (groupIds.size() > 0) {
                monthWorkRecordStatistics = monthStatisticsMap.get(groupIds.get(0));
            }
            WrStatisticsOverviewRoleData monthOverviewRoleData = new WrStatisticsOverviewRoleData();
            if (null != agentRoleType) {
                monthOverviewRoleData.setRoleId(agentRoleType.getId());
                monthOverviewRoleData.setRoleName(agentRoleType.getRoleName());
            }
            if (null != monthWorkRecordStatistics) {
                //获取该部门该角色的工作量统计数据
                AgentWorkRecordStatisticsRoleData monthWorkRecordStatisticsRoleData = monthWorkRecordStatistics.getRoleDataMap().get(agentRoleType.getId());
                if (null != monthWorkRecordStatisticsRoleData) {
                    monthOverviewRoleData.setUserCount(monthWorkRecordStatisticsRoleData.getUserCount() != null ? monthWorkRecordStatisticsRoleData.getUserCount() : 0);
                    monthOverviewRoleData.setFillRecordUserCount(monthWorkRecordStatisticsRoleData.getFillRecordUserCount() != null ? monthWorkRecordStatisticsRoleData.getFillRecordUserCount() : 0);
                    monthOverviewRoleData.setRecordUnreachedUserCount(monthWorkRecordStatisticsRoleData.getRecordUnreachedUserCount() != null ? monthWorkRecordStatisticsRoleData.getRecordUnreachedUserCount() : 0);
                    monthOverviewRoleData.setPerCapitaWorkload(monthWorkRecordStatisticsRoleData.getPerCapitaWorkload() != null ? monthWorkRecordStatisticsRoleData.getPerCapitaWorkload() : 0.0);
                }
            }
            dataMap.put("monthOverviewRoleData", monthOverviewRoleData);
        }
        return dataMap;
    }


    /**
     * 首页-团队专员进校
     *
     * @param currentUser
     * @param date
     * @param searchType
     * @return
     */
    public Map<String, Object> teamBdIntoSchoolStatistics(AuthCurrentUser currentUser, Date date, Integer searchType) {
        Map<String, Object> dataMap = new HashMap<>();
        //非专员
        if (!currentUser.isBusinessDeveloper()) {
            List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
            if (CollectionUtils.isEmpty(groupUserByUser)) {
                return MapMessage.errorMessage();
            }
            Long groupId = groupUserByUser.get(0).getGroupId();
            //全国总监或管理员
            if (currentUser.isCountryManager() || currentUser.isAdmin()) {
                List<AgentGroup> subGroupIdList = baseOrgService.getSubGroupList(groupId);
                //小学市场
                if (searchType == 1) {
                    groupId = subGroupIdList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && p.getGroupName().contains("小学")).map(AgentGroup::getId).findFirst().orElse(null);
                    //中学市场
                } else {
                    groupId = subGroupIdList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Marketing && p.getGroupName().contains("中学")).map(AgentGroup::getId).findFirst().orElse(null);
                }
            }

            //获取所有子部门，级别为分区的部门
            List<Long> cityGroupIds = baseOrgService.getSubGroupList(groupId).stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City).map(AgentGroup::getId).collect(Collectors.toList());
            //如果本部门是分区，也要拼装
            AgentGroup group = baseOrgService.getGroupById(groupId);
            if (null != group && group.fetchGroupRoleType() == AgentGroupRoleType.City) {
                cityGroupIds.add(groupId);
            }
            //获取指定部门（多个）中，指定角色的用户
            List<Long> userIds = baseOrgService.getUserByGroupIdsAndRole(cityGroupIds, AgentRoleType.BusinessDeveloper);

            Integer monthUserNum = 0;
            Integer monthIntoSchoolNum = 0;
            Integer monthVisitTeacherNum = 0;

            Map<Long, AgentWorkRecordStatistics> monthStatisticsMap = agentWorkRecordStatisticsService.getUserStatistics(userIds, date, 3);
            if (MapUtils.isNotEmpty(monthStatisticsMap)) {
                for (Long userId : userIds) {
                    AgentWorkRecordStatistics workRecordStatistics = monthStatisticsMap.get(userId);
                    if (workRecordStatistics != null) {
                        //专员数量
                        monthUserNum++;
                        //进校数
                        monthIntoSchoolNum += (workRecordStatistics.getBdIntoSchoolCount() != null ? workRecordStatistics.getBdIntoSchoolCount() : 0);
                        //见师数量
                        monthVisitTeacherNum += (workRecordStatistics.getBdVisitTeacherCount() != null ? workRecordStatistics.getBdVisitTeacherCount() : 0);
                    }
                }
            }

            //月人均进校数
            dataMap.put("monthPerPersonIntoSchoolNum", MathUtils.doubleDivide(monthIntoSchoolNum, monthUserNum));
            //月人均见师数
            dataMap.put("monthPerPersonVisitTeacherNum", MathUtils.doubleDivide(monthVisitTeacherNum, monthUserNum));

            Integer dayUserNum = 0;
            Integer dayIntoSchoolNum = 0;
            Integer dayVisitTeacherNum = 0;
            Map<Long, AgentWorkRecordStatistics> dayStatisticsMap = agentWorkRecordStatisticsService.getUserStatistics(userIds, date, 3);
            if (MapUtils.isNotEmpty(dayStatisticsMap)) {
                for (Long userId : userIds) {
                    AgentWorkRecordStatistics workRecordStatistics = dayStatisticsMap.get(userId);
                    if (workRecordStatistics != null) {
                        //专员数量
                        monthUserNum++;
                        //进校数
                        monthIntoSchoolNum += (workRecordStatistics.getBdIntoSchoolCount() != null ? workRecordStatistics.getBdIntoSchoolCount() : 0);
                        //见师数量
                        monthVisitTeacherNum += (workRecordStatistics.getBdVisitTeacherCount() != null ? workRecordStatistics.getBdVisitTeacherCount() : 0);
                    }
                }
            }
            //当日人均进校数
            dataMap.put("dayPerPersonIntoSchoolNum", MathUtils.doubleDivide(dayIntoSchoolNum, dayUserNum));
            //当日人均见师数
            dataMap.put("dayPerPersonVisitTeacherNum", MathUtils.doubleDivide(dayVisitTeacherNum, dayUserNum));

            dataMap.put("groupId", groupId);
        }
        return dataMap;
    }

    /**
     * 团队专员进校明细
     *
     * @param date
     * @param groupId
     * @param groupRoleType
     * @param dimension
     * @param dateType
     * @return
     */
    public Map<Long, AgentWorkRecordStatistics> teamBdIntoSchoolDetail(Date date, Long groupId, AgentGroupRoleType groupRoleType, Integer dimension, Integer dateType) {
        Map<Long, AgentWorkRecordStatistics> intoSchoolStatistics = new HashMap<>();
        // 专员统计列表
        if (dimension == 5 || (groupRoleType == AgentGroupRoleType.City && dimension == 1)) {
            //获取该部门及子部门中专员的信息
            List<Long> userIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());
            intoSchoolStatistics = agentWorkRecordStatisticsService.getUserIntoSchoolStatistics(userIds, date, dateType);
            //部门统计列表
        } else {
            //获取符合条件的部门
            Collection<Long> groupIds = fetchGroupList(groupId, dimension);
            intoSchoolStatistics = agentWorkRecordStatisticsService.getGroupIntoSchoolStatistics(groupIds, date, dateType);
        }
        return intoSchoolStatistics;
    }


    /**
     * 获取符合的部门
     *
     * @param groupId
     * @param dimension
     * @return
     */
    public Collection<Long> fetchGroupList(Long groupId, Integer dimension) {
        List<AgentGroup> groupList = new ArrayList<>();
        // 默认情况下
        if (dimension == 1) {
            // 获取直接子部门
            List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(groupId);
            groupList.addAll(subGroupList);
        } else {
            List<AgentGroup> allSubGroupList = new ArrayList<>();
            // 获取指定部门下面的所有子部门
            allSubGroupList.addAll(baseOrgService.getSubGroupList(groupId));
            AgentGroupRoleType targetGroupRole = null;
            if (dimension == 2) {
                targetGroupRole = AgentGroupRoleType.Region;
            } else if (dimension == 3) {
                targetGroupRole = AgentGroupRoleType.Area;
            } else if (dimension == 4) {
                targetGroupRole = AgentGroupRoleType.City;
            }
            //过滤出指定级别部门
            for (AgentGroup p : allSubGroupList) {
                if (p.fetchGroupRoleType() == targetGroupRole) {
                    groupList.add(p);
                }
            }
        }
        return groupList.stream().map(AgentGroup::getId).collect(Collectors.toSet());
    }

    public List<CrmWorkRecord> findResearcherVisitRecord(Long researcherId) {
        List<CrmWorkRecord> workRecords = crmWorkRecordLoaderClient.findByResearcherId(researcherId, CrmWorkRecordType.TEACHING);
        return workRecords;
    }

    /**
     * 人员工作统计
     *
     * @param userId
     * @param date
     * @param dateType
     * @return
     */
    public Map<String, Object> userWorkStatistics(Long userId, Date date, Integer dateType) {
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        Map<String, Object> dataMap = new HashMap<>();
        double workload = 0;
        int intoSchoolNum = 0;
        int visitTeaNum = 0;
        int visitChiTeaNum = 0;
        int visitMathTeaNum = 0;
        int visitEngTeaNum = 0;

        int newRegTeaNum = 0;
        int newRegChiTeaNum = 0;
        int newRegMathTeaNum = 0;
        int newRegEngTeaNum = 0;

        double accompanyVisitWorkload = 0;
        int visitResearchersNum = 0;

        int userWorkDays = 0;
        int userNeedWorkDays = 0;
        //工作记录
        Map<Long, AgentWorkRecordStatistics> statisticsMap = agentWorkRecordStatisticsService.getUserStatistics(Collections.singleton(userId), date, dateType);
        AgentWorkRecordStatistics workRecordStatistics = statisticsMap.get(userId);
        if (workRecordStatistics != null) {
            workload = workRecordStatistics.getUserWorkload() != null ? workRecordStatistics.getUserWorkload() : 0;
            intoSchoolNum = workRecordStatistics.getUserIntoSchoolNum() != null ? workRecordStatistics.getUserIntoSchoolNum() : 0;
            visitTeaNum = workRecordStatistics.getUserVisitTeacherNum() != null ? workRecordStatistics.getUserVisitTeacherNum() : 0;
            visitChiTeaNum = workRecordStatistics.getUserVisitChiTeacherNum() != null ? workRecordStatistics.getUserVisitChiTeacherNum() : 0;
            visitMathTeaNum = workRecordStatistics.getUserVisitMathTeacherNum() != null ? workRecordStatistics.getUserVisitMathTeacherNum() : 0;
            visitEngTeaNum = workRecordStatistics.getUserVisitEngTeacherNum() != null ? workRecordStatistics.getUserVisitEngTeacherNum() : 0;

            accompanyVisitWorkload = workRecordStatistics.getUserVisitWorkload() != null ? workRecordStatistics.getUserVisitWorkload() : 0;
            visitResearchersNum = workRecordStatistics.getUserVisitResearcherNum() != null ? workRecordStatistics.getUserVisitResearcherNum() : 0;

            userWorkDays = workRecordStatistics.getUserWorkDays() != null ? workRecordStatistics.getUserWorkDays() : 0;
            userNeedWorkDays = workRecordStatistics.getUserNeedWordDays() != null ? workRecordStatistics.getUserNeedWordDays() : 0;

        }

        Map<Long, AgentRegisterTeacherStatistics> registerTeacherStatisticsMap = agentRegisterTeacherStatisticsService.getUserRegisterTeacherStatistics(Collections.singleton(userId), date, dateType);
        if (MapUtils.isNotEmpty(registerTeacherStatisticsMap)) {
            AgentRegisterTeacherStatistics userRegisterTeacherStatistics = registerTeacherStatisticsMap.get(userId);
            if (userRegisterTeacherStatistics != null) {
                newRegChiTeaNum = userRegisterTeacherStatistics.getUserRegisterChnTeacherCount() == null ? 0 : userRegisterTeacherStatistics.getUserRegisterChnTeacherCount();
                newRegMathTeaNum = userRegisterTeacherStatistics.getUserRegisterMathTeacherCount() == null ? 0 : userRegisterTeacherStatistics.getUserRegisterMathTeacherCount();
                newRegEngTeaNum = userRegisterTeacherStatistics.getUserRegisterEngTeacherCount() == null ? 0 : userRegisterTeacherStatistics.getUserRegisterEngTeacherCount();
                newRegTeaNum = userRegisterTeacherStatistics.getUserRegisterTeacherCount() == null ? 0 : userRegisterTeacherStatistics.getUserRegisterTeacherCount();
            }
        }

        dataMap.put("workload", workload);
        //工作日
        if (dateType == 2 || dateType == 3) {
            dataMap.put("workDays", userWorkDays);
            dataMap.put("needWorkDays", userNeedWorkDays);
        }
        if (userRole == AgentRoleType.BusinessDeveloper) {
            //日报得分
            Map<Long, AgentDailyScoreStatistics> userDailyStatistics = agentDailyScoreStatisticsService.getUserStatistics(Collections.singleton(userId), date, dateType);
            AgentDailyScoreStatistics dailyScoreStatistics = userDailyStatistics.get(userId);
            if (dailyScoreStatistics != null && dailyScoreStatistics.getDailyScore() != null) {
                dataMap.put("dailyScore", dailyScoreStatistics.getDailyScore());
            }

            dataMap.put("intoSchoolNum", intoSchoolNum);
            dataMap.put("visitTeaNum", visitTeaNum);
            dataMap.put("visitChiTeaNum", visitChiTeaNum);
            dataMap.put("visitMathTeaNum", visitMathTeaNum);
            dataMap.put("visitEngTeaNum", visitEngTeaNum);
            dataMap.put("newRegTeaNum", newRegTeaNum);
            dataMap.put("newRegChiTeaNum", newRegChiTeaNum);
            dataMap.put("newRegMathTeaNum", newRegMathTeaNum);
            dataMap.put("newRegEngTeaNum", newRegEngTeaNum);
        } else {
            dataMap.put("accompanyVisitWorkload", accompanyVisitWorkload);
            dataMap.put("visitResearchersNum", visitResearchersNum);
        }
        return dataMap;
    }


    /**
     * 部门工作统计
     *
     * @param groupId
     * @param date
     * @param dateType
     * @return
     */
    public Map<String, Object> groupWorkStatistics(Long groupId, Date date, Integer dateType, AgentRoleType agentRoleType) {
        Map<String, Object> dataMap = new HashMap<>();
        double perCapitaWorkload = 0;
        double perCapitaIntoSchoolNum = 0;
        double perCapitaVisitTeaNum = 0;
        double perCapitaVisitChiTeaNum = 0;
        double perCapitaVisitMathTeaNum = 0;
        double perCapitaVisitEngTeaNum = 0;
        double perCapitalVisitWorkload = 0;//人均陪访数市经理及以上有
        double perCapitalVisitResearcherNum = 0; //资源拓维数


        double perCapitaNewRegTeaNum = 0;
        double perCapitaNewRegChiTeaNum = 0;
        double perCapitaNewRegMathTeaNum = 0;
        double perCapitaNewRegEngTeaNum = 0;

        //工作记录
        Map<Long, AgentWorkRecordStatistics> statisticsMap = agentWorkRecordStatisticsService.getGroupStatistics(Collections.singleton(groupId), date, dateType);
        AgentWorkRecordStatistics workRecordStatistics = statisticsMap.get(groupId);
        if (workRecordStatistics != null) {
            //获取部门专员工作统计
            AgentWorkRecordStatisticsRoleData workRecordStatisticsRoleData = workRecordStatistics.getRoleDataMap() == null ? new AgentWorkRecordStatisticsRoleData() : workRecordStatistics.getRoleDataMap().get(agentRoleType.getId());
            if (workRecordStatisticsRoleData != null) {
                perCapitaWorkload = workRecordStatisticsRoleData.getPerCapitaWorkload() != null ? workRecordStatisticsRoleData.getPerCapitaWorkload() : 0;
                perCapitaIntoSchoolNum = workRecordStatisticsRoleData.getPerCapitaIntoSchoolNum() != null ? workRecordStatisticsRoleData.getPerCapitaIntoSchoolNum() : 0;
                perCapitaVisitTeaNum = workRecordStatisticsRoleData.getPerCapitaVisitTeaNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitTeaNum() : 0;
                perCapitaVisitChiTeaNum = workRecordStatisticsRoleData.getPerCapitaVisitChiTeaNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitChiTeaNum() : 0;
                perCapitaVisitMathTeaNum = workRecordStatisticsRoleData.getPerCapitaVisitMathTeaNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitMathTeaNum() : 0;
                perCapitaVisitEngTeaNum = workRecordStatisticsRoleData.getPerCapitaVisitEngTeaNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitEngTeaNum() : 0;
                perCapitalVisitWorkload = workRecordStatisticsRoleData.getPerCapitalVisitWorkload() != null ? workRecordStatisticsRoleData.getPerCapitalVisitWorkload() : 0;
                perCapitalVisitResearcherNum = workRecordStatisticsRoleData.getPerCapitaVisitResearcherNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitResearcherNum() : 0;
            }
        }
        Map<Long, AgentRegisterTeacherStatistics> registerTeacherStatisticsMap = agentRegisterTeacherStatisticsService.getGroupRegisterTeacherStatistics(Collections.singleton(groupId), date, dateType);
        if (MapUtils.isNotEmpty(registerTeacherStatisticsMap)) {
            AgentRegisterTeacherStatistics agentRegisterTeacherStatistics = registerTeacherStatisticsMap.get(groupId);
            if (agentRegisterTeacherStatistics != null) {
                perCapitaNewRegChiTeaNum = agentRegisterTeacherStatistics.getPerPersonRegisterChnTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getPerPersonRegisterChnTeacherCount();
                perCapitaNewRegMathTeaNum = agentRegisterTeacherStatistics.getPerPersonRegisterMathTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getPerPersonRegisterMathTeacherCount();
                perCapitaNewRegEngTeaNum = agentRegisterTeacherStatistics.getPerPersonRegisterEngTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getPerPersonRegisterEngTeacherCount();
                perCapitaNewRegTeaNum = agentRegisterTeacherStatistics.getPerPersonRegisterTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getPerPersonRegisterTeacherCount();//部门人均
            }
        }
        //日报得分
        Map<Long, AgentDailyScoreStatistics> groupDailyStatisticsMap = agentDailyScoreStatisticsService.getGroupStatistics(Collections.singleton(groupId), date, dateType);
        AgentDailyScoreStatistics dailyScoreStatistics = groupDailyStatisticsMap.get(groupId);
        if (dailyScoreStatistics != null && dailyScoreStatistics.getDailyScore() != null) {
            dataMap.put("perCapitaDailyScore", dailyScoreStatistics.getDailyScore());
        }
        dataMap.put("perCapitaWorkload", perCapitaWorkload);
        dataMap.put("perCapitaIntoSchoolNum", perCapitaIntoSchoolNum);
        dataMap.put("perCapitaVisitTeaNum", MathUtils.doubleToInt(perCapitaVisitTeaNum, BigDecimal.ROUND_DOWN));
        dataMap.put("perCapitaVisitChiTeaNum", MathUtils.doubleToInt(perCapitaVisitChiTeaNum, BigDecimal.ROUND_DOWN));
        dataMap.put("perCapitaVisitMathTeaNum", MathUtils.doubleToInt(perCapitaVisitMathTeaNum, BigDecimal.ROUND_DOWN));
        dataMap.put("perCapitaVisitEngTeaNum", MathUtils.doubleToInt(perCapitaVisitEngTeaNum, BigDecimal.ROUND_DOWN));
        dataMap.put("perCapitaNewRegTeaNum", MathUtils.doubleToInt(perCapitaNewRegTeaNum, BigDecimal.ROUND_DOWN));
        dataMap.put("perCapitaNewRegChiTeaNum", MathUtils.doubleToInt(perCapitaNewRegChiTeaNum, BigDecimal.ROUND_DOWN));
        dataMap.put("perCapitaNewRegMathTeaNum", MathUtils.doubleToInt(perCapitaNewRegMathTeaNum, BigDecimal.ROUND_DOWN));
        dataMap.put("perCapitaNewRegEngTeaNum", MathUtils.doubleToInt(perCapitaNewRegEngTeaNum, BigDecimal.ROUND_DOWN));
        dataMap.put("perCapitalVisitWorkload", perCapitalVisitWorkload);
        dataMap.put("perCapitalVisitResearcherNum", perCapitalVisitResearcherNum);
        return dataMap;
    }

    /**
     * 部门工作统计
     *
     * @param groupIds
     * @param date
     * @param dateType
     * @return
     */
    public List<Map<String, Object>> groupWorkStatisticsList(Collection<Long> groupIds, Date date, Integer dateType) {
        List<Map<String, Object>> result = new ArrayList<>();

        Map<Long, AgentGroup> groupMap = baseOrgService.getGroupByIds(groupIds).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
        //工作记录
        Map<Long, AgentWorkRecordStatistics> statisticsMap = agentWorkRecordStatisticsService.getGroupStatistics(groupIds, date, dateType);
        //新注册老师
        Map<Long, AgentRegisterTeacherStatistics> registerTeacherStatisticsMap = agentRegisterTeacherStatisticsService.getGroupRegisterTeacherStatistics(groupIds, date, dateType);
        //日报得分
        Map<Long, AgentDailyScoreStatistics> groupDailyStatisticsMap = agentDailyScoreStatisticsService.getGroupStatistics(groupIds, date, dateType);
        groupMap.values().forEach(ag -> {
            Map<String, Object> dataMap = new HashMap<>();
            double perCapitaWorkload = 0; //人均工作量
            double perCapitaIntoSchoolNum = 0;//人均进校人均进校
            double perCapitaVisitTeaNum = 0; //人均见师量
            double perCapitaVisitChiTeaNum = 0;//人均见师量 语文
            double perCapitaVisitMathTeaNum = 0;//人均见师量 数学
            double perCapitaVisitEngTeaNum = 0;//人均见师量 英语

            //暂未提供
            double perCapitaNewRegTeaNum = 0; //人均注册
            double perCapitaNewRegChiTeaNum = 0;//人均注册 语文
            double perCapitaNewRegMathTeaNum = 0;//人均注册 数学
            double perCapitaNewRegEngTeaNum = 0;//人均注册 英语
            double perCapitaWorkDayNum = 0; //人均工作天数
            double perCapitaAccompanyVisitNum = 0;//人均陪访
            double perCapitaVisitResearcherNum = 0; //人均拜访教研员工作量
            double perCapitaMeetingNum = 0;//人均组会数
            AgentWorkRecordStatistics workRecordStatistics = statisticsMap.get(ag.getId());
            if (workRecordStatistics != null) {
                //获取部门专员工作统计
                AgentWorkRecordStatisticsRoleData workRecordStatisticsRoleData = workRecordStatistics.getRoleDataMap().get(AgentRoleType.BusinessDeveloper.getId());
                if (workRecordStatisticsRoleData != null) {
                    perCapitaWorkload = workRecordStatisticsRoleData.getPerCapitaWorkload() != null ? workRecordStatisticsRoleData.getPerCapitaWorkload() : 0;
                    perCapitaIntoSchoolNum = workRecordStatisticsRoleData.getPerCapitaIntoSchoolNum() != null ? workRecordStatisticsRoleData.getPerCapitaIntoSchoolNum() : 0;
                    perCapitaVisitTeaNum = workRecordStatisticsRoleData.getPerCapitaVisitTeaNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitTeaNum() : 0;
                    perCapitaVisitChiTeaNum = workRecordStatisticsRoleData.getPerCapitaVisitChiTeaNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitChiTeaNum() : 0;
                    perCapitaVisitMathTeaNum = workRecordStatisticsRoleData.getPerCapitaVisitMathTeaNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitMathTeaNum() : 0;
                    perCapitaVisitEngTeaNum = workRecordStatisticsRoleData.getPerCapitaVisitEngTeaNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitEngTeaNum() : 0;

                    perCapitaWorkDayNum = workRecordStatisticsRoleData.getPerCapitaWorkDayNum() != null ? workRecordStatisticsRoleData.getPerCapitaWorkDayNum() : 0;
                    perCapitaAccompanyVisitNum = workRecordStatisticsRoleData.getPerCapitaAccompanyVisitNum() != null ? workRecordStatisticsRoleData.getPerCapitaAccompanyVisitNum() : 0;
                    perCapitaVisitResearcherNum = workRecordStatisticsRoleData.getPerCapitaVisitResearcherNum() != null ? workRecordStatisticsRoleData.getPerCapitaVisitResearcherNum() : 0;
                    perCapitaMeetingNum = workRecordStatisticsRoleData.getPerCapitaMeetingNum() != null ? workRecordStatisticsRoleData.getPerCapitaMeetingNum() : 0;
                }

            }

            if (MapUtils.isNotEmpty(registerTeacherStatisticsMap)) {
                AgentRegisterTeacherStatistics agentRegisterTeacherStatistics = registerTeacherStatisticsMap.get(ag.getId());
                if (agentRegisterTeacherStatistics != null) {
                    perCapitaNewRegChiTeaNum = agentRegisterTeacherStatistics.getPerPersonRegisterChnTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getPerPersonRegisterChnTeacherCount();
                    perCapitaNewRegMathTeaNum = agentRegisterTeacherStatistics.getPerPersonRegisterMathTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getPerPersonRegisterMathTeacherCount();
                    perCapitaNewRegEngTeaNum = agentRegisterTeacherStatistics.getPerPersonRegisterEngTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getPerPersonRegisterEngTeacherCount();
                    perCapitaNewRegTeaNum = agentRegisterTeacherStatistics.getPerPersonRegisterTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getPerPersonRegisterTeacherCount();//部门人均
                }
            }

            AgentDailyScoreStatistics dailyScoreStatistics = groupDailyStatisticsMap.get(ag.getId());
            if (dailyScoreStatistics != null && dailyScoreStatistics.getDailyScore() != null) {
                dataMap.put("perCapitaDailyScore", dailyScoreStatistics.getDailyScore());
            }
            dataMap.put("perCapitaWorkload", perCapitaWorkload);
            dataMap.put("perCapitaIntoSchoolNum", perCapitaIntoSchoolNum);
            dataMap.put("perCapitaVisitTeaNum", MathUtils.doubleToInt(perCapitaVisitTeaNum, BigDecimal.ROUND_DOWN));
            dataMap.put("perCapitaVisitChiTeaNum", MathUtils.doubleToInt(perCapitaVisitChiTeaNum, BigDecimal.ROUND_DOWN));
            dataMap.put("perCapitaVisitMathTeaNum", MathUtils.doubleToInt(perCapitaVisitMathTeaNum, BigDecimal.ROUND_DOWN));
            dataMap.put("perCapitaVisitEngTeaNum", MathUtils.doubleToInt(perCapitaVisitEngTeaNum, BigDecimal.ROUND_DOWN));
            dataMap.put("perCapitaNewRegTeaNum", MathUtils.doubleToInt(perCapitaNewRegTeaNum, BigDecimal.ROUND_DOWN));
            dataMap.put("perCapitaNewRegChiTeaNum", MathUtils.doubleToInt(perCapitaNewRegChiTeaNum, BigDecimal.ROUND_DOWN));
            dataMap.put("perCapitaNewRegMathTeaNum", MathUtils.doubleToInt(perCapitaNewRegMathTeaNum, BigDecimal.ROUND_DOWN));
            dataMap.put("perCapitaNewRegEngTeaNum", MathUtils.doubleToInt(perCapitaNewRegEngTeaNum, BigDecimal.ROUND_DOWN));
            dataMap.put("groupId", ag.getId());
            dataMap.put("groupName", ag.getGroupName());
            if (dateType == 2 || dateType == 3) {
                dataMap.put("perCapitaWorkDayNum", perCapitaWorkDayNum);
            }
            dataMap.put("perCapitaAccompanyVisitNum", perCapitaAccompanyVisitNum);
            dataMap.put("perCapitaVisitResearcherNum", perCapitaVisitResearcherNum);
            dataMap.put("perCapitaMeetingNum", perCapitaMeetingNum);
            result.add(dataMap);
        });

        return result;

    }

    /**
     * 用户工作统计
     *
     * @param userIds
     * @param date
     * @param dateType
     * @return
     */
    public List<Map<String, Object>> userWorkStatisticsList(Collection<Long> userIds, Date date, Integer dateType) {
        List<Map<String, Object>> result = new ArrayList<>();

        Map<Long, AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
        //工作记录
        Map<Long, AgentWorkRecordStatistics> statisticsMap = agentWorkRecordStatisticsService.getUserStatistics(userIds, date, dateType);
        //新注册老师
        Map<Long, AgentRegisterTeacherStatistics> registerTeacherStatisticsMap = agentRegisterTeacherStatisticsService.getUserRegisterTeacherStatistics(userIds, date, dateType);
        //日报得分
        Map<Long, AgentDailyScoreStatistics> groupDailyStatisticsMap = agentDailyScoreStatisticsService.getUserStatistics(userIds, date, dateType);
        userMap.values().forEach(ag -> {
            Map<String, Object> dataMap = new HashMap<>();
            double userIntoSchoolWorkload = 0; //进校工作量
            double userWorkload = 0; //工作量
            double bdIntoSchoolCount = 0;//进校数
            double bdVisitTeacherCount = 0; //见师量
            double bdVisitChiTeacherCount = 0;//见师量 语文
            double bdVisitMathTeacherCount = 0;//见师量 数学
            double bdVisitEngTeacherCount = 0;//见师量 英语

            double userRegisterTeacherCount = 0; //专员注册
            double userRegisterChnTeacherCount = 0;//注册 语文
            double userRegisterMathTeacherCount = 0;//注册 数学
            double userRegisterEngTeacherCount = 0;//注册 英语
            int userWorkDays = 0; //工作天数
            int userNeedWordDays = 0; //需要工作天数
            double userVisitSchoolCount = 0;//陪访数
            double userVisitResearcherNum = 0; //拜访教研员数量
            double userMeetingNum = 0;//组会数量
            AgentWorkRecordStatistics workRecordStatistics = statisticsMap.get(ag.getId());
            if (workRecordStatistics != null) {
                userWorkload = workRecordStatistics.getUserWorkload() != null ? workRecordStatistics.getUserWorkload() : 0;
                userWorkDays = workRecordStatistics.getUserWorkDays() != null ? workRecordStatistics.getUserWorkDays() : 0;
                userNeedWordDays = workRecordStatistics.getUserNeedWordDays() != null ? workRecordStatistics.getUserNeedWordDays() : 0;
                userIntoSchoolWorkload = workRecordStatistics.getUserIntoSchoolWorkload() != null ? workRecordStatistics.getUserIntoSchoolWorkload() : 0;
                bdIntoSchoolCount = workRecordStatistics.getUserIntoSchoolNum() != null ? workRecordStatistics.getUserIntoSchoolNum() : 0;
                bdVisitTeacherCount = workRecordStatistics.getUserVisitTeacherNum() != null ? workRecordStatistics.getUserVisitTeacherNum() : 0;

                bdVisitChiTeacherCount = workRecordStatistics.getUserVisitChiTeacherNum() != null ? workRecordStatistics.getUserVisitChiTeacherNum() : 0;
                bdVisitMathTeacherCount = workRecordStatistics.getUserVisitMathTeacherNum() != null ? workRecordStatistics.getUserVisitMathTeacherNum() : 0;
                bdVisitEngTeacherCount = workRecordStatistics.getUserVisitEngTeacherNum() != null ? workRecordStatistics.getUserVisitEngTeacherNum() : 0;

                userVisitSchoolCount = workRecordStatistics.getUserAccompanyVisitNum() != null ? workRecordStatistics.getUserAccompanyVisitNum() : 0;
                userVisitResearcherNum = workRecordStatistics.getUserVisitResearcherNum() != null ? workRecordStatistics.getUserVisitResearcherNum() : 0;
                userMeetingNum = workRecordStatistics.getUserMeetingNum() == null ? 0 : workRecordStatistics.getUserMeetingNum();
            }

            if (MapUtils.isNotEmpty(registerTeacherStatisticsMap)) {
                AgentRegisterTeacherStatistics agentRegisterTeacherStatistics = registerTeacherStatisticsMap.get(ag.getId());
                if (agentRegisterTeacherStatistics != null) {
                    userRegisterChnTeacherCount = agentRegisterTeacherStatistics.getUserRegisterChnTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getUserRegisterChnTeacherCount();
                    userRegisterMathTeacherCount = agentRegisterTeacherStatistics.getUserRegisterMathTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getUserRegisterMathTeacherCount();
                    userRegisterEngTeacherCount = agentRegisterTeacherStatistics.getUserRegisterEngTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getUserRegisterEngTeacherCount();
                    userRegisterTeacherCount = agentRegisterTeacherStatistics.getUserRegisterTeacherCount() == null ? 0 : agentRegisterTeacherStatistics.getUserRegisterTeacherCount();//部门人均
                }
            }

            AgentDailyScoreStatistics dailyScoreStatistics = groupDailyStatisticsMap.get(ag.getId());
            if (dailyScoreStatistics != null && dailyScoreStatistics.getDailyScore() != null) {
                dataMap.put("dailyScore", dailyScoreStatistics.getDailyScore());
            }

            dataMap.put("userWorkload", userWorkload);
            dataMap.put("userIntoSchoolWorkload", userIntoSchoolWorkload);
            dataMap.put("bdIntoSchoolCount", bdIntoSchoolCount);
            dataMap.put("bdVisitTeacherCount", bdVisitTeacherCount);
            dataMap.put("bdVisitChiTeacherCount", bdVisitChiTeacherCount);
            dataMap.put("bdVisitMathTeacherCount", bdVisitMathTeacherCount);
            dataMap.put("bdVisitEngTeacherCount", bdVisitEngTeacherCount);
            dataMap.put("userRegisterTeacherCount", userRegisterTeacherCount);
            dataMap.put("userRegisterChnTeacherCount", userRegisterChnTeacherCount);
            dataMap.put("userRegisterMathTeacherCount", userRegisterMathTeacherCount);
            dataMap.put("userRegisterEngTeacherCount", userRegisterEngTeacherCount);
            dataMap.put("userId", ag.getId());
            dataMap.put("userName", ag.getRealName());
            dataMap.put("userWorkDays", userWorkDays);
            dataMap.put("userNeedWordDays", userNeedWordDays);
            dataMap.put("userVisitSchoolCount", userVisitSchoolCount);
            dataMap.put("userVisitResearcherNum", userVisitResearcherNum);
            dataMap.put("userMeetingNum", userMeetingNum);
            result.add(dataMap);
        });

        return result;

    }

    //统计部门趋势图
    public MapMessage workStatisticsView(Long id, Integer groupOrUser, Date date, Integer dateType, String userRoleType) {
        AgentRoleType agentRoleType = AgentRoleType.nameOf(userRoleType);
        if (agentRoleType == null) {
            agentRoleType = AgentRoleType.BusinessDeveloper;
        }
        MapMessage mapMessage = MapMessage.successMessage();
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Integer> dayList = new ArrayList<>();
        if (dateType == 1) {
            dayList.add(SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd")));
            for (int i = 0; i < 5; i++) {
                dayList.add(SafeConverter.toInt(DateUtils.dateToString(DayUtils.addDay(date, -(i + 1)), "yyyyMMdd")));
            }
            mapMessage.add("startDate", DateUtils.dateToString(DayUtils.addDay(date, -6), "yyyyMMdd"));
            for (Integer day : dayList) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("day", day);
                Date dayDate = DateUtils.stringToDate(day.toString(), "yyyyMMdd");
                //部门
                if (Objects.equals(groupOrUser, 1)) {
                    itemMap.put("data", groupWorkStatistics(id, dayDate, 1, agentRoleType));
                    //人员
                } else if (Objects.equals(groupOrUser, 2)) {
                    itemMap.put("data", userWorkStatistics(id, dayDate, 1));
                }
                itemMap.put("startDate", day);
                resultList.add(itemMap);
            }
        } else if (dateType == 2) {
            WeekRange tmpWeekRange = WeekRange.newInstance(date.getTime());
            List<WeekRange> weekRangeList = new ArrayList<>();
            weekRangeList.add(tmpWeekRange);
            for (int i = 0; i < 3; i++) {
                tmpWeekRange = tmpWeekRange.previous();
                weekRangeList.add(tmpWeekRange);
            }
            tmpWeekRange = tmpWeekRange.previous();
            mapMessage.add("startDate", DateUtils.dateToString(tmpWeekRange.getStartDate(), "yyyyMMdd"));
            for (WeekRange wr : weekRangeList) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("day", DateUtils.dateToString(wr.getStartDate(), "MM/dd") + "-" + DateUtils.dateToString(wr.getEndDate(), "MM/dd"));
                //部门
                if (Objects.equals(groupOrUser, 1)) {
                    itemMap.put("data", groupWorkStatistics(id, wr.getStartDate(), 2, agentRoleType));
                    //人员
                } else if (Objects.equals(groupOrUser, 2)) {
                    itemMap.put("data", userWorkStatistics(id, wr.getStartDate(), 2));
                }
                itemMap.put("startDate", DateUtils.dateToString(wr.getStartDate(), "yyyyMMdd"));
                resultList.add(itemMap);
            }
        } else if (dateType == 3) {
            dayList.add(SafeConverter.toInt(DateUtils.dateToString(DayUtils.getFirstDayOfMonth(date), "yyyyMMdd")));
            for (int i = 0; i < 3; i++) {
                dayList.add(SafeConverter.toInt(DateUtils.dateToString(DayUtils.getFirstDayOfMonth(DayUtils.addMonth(date, -(i + 1))), "yyyyMMdd")));
            }
            mapMessage.add("startDate", DateUtils.dateToString(DayUtils.getFirstDayOfMonth(DayUtils.addMonth(date, -4)), "yyyyMMdd"));
            for (Integer day : dayList) {
                Map<String, Object> itemMap = new HashMap<>();
                Date monthDate = DateUtils.stringToDate(day.toString(), "yyyyMMdd");
                itemMap.put("day", monthDate.getMonth() + 1);
                //部门
                if (Objects.equals(groupOrUser, 1)) {
                    itemMap.put("data", groupWorkStatistics(id, monthDate, 3, agentRoleType));
                    //人员
                } else if (Objects.equals(groupOrUser, 2)) {
                    itemMap.put("data", userWorkStatistics(id, monthDate, 3));
                }
                itemMap.put("startDate", day);
                resultList.add(itemMap);
            }
        }

        return mapMessage.add("dataList", resultList);
    }

    /**
     * 组装见师数据
     *
     * @param visitUserInfoList
     * @param visitEngTeacherIds
     * @param visitMathTeacherIds
     * @param visitChiTeacherIds
     * @param visitTeacherIds
     */
    public void generateVisitTeacherInfo(List<WorkRecordVisitUserInfo> visitUserInfoList, Set<Long> visitEngTeacherIds, Set<Long> visitMathTeacherIds, Set<Long> visitChiTeacherIds, Set<Long> visitTeacherIds) {
        //过滤出有科目的拜访老师
        if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
            List<WorkRecordVisitUserInfo> visitTeacherList = visitUserInfoList.stream().filter(p -> p != null && p.getJob() != null && (Objects.equals(p.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB))
                    && p.getSubject() != null && p.getSubject() != Subject.UNKNOWN).collect(Collectors.toList());
            for (WorkRecordVisitUserInfo visitTeacherInfo : visitTeacherList) {
                if (visitTeacherInfo.getSubject() == Subject.ENGLISH) {
                    visitEngTeacherIds.add(visitTeacherInfo.getId());
                } else if (visitTeacherInfo.getSubject() == Subject.MATH) {
                    visitMathTeacherIds.add(visitTeacherInfo.getId());
                } else if (visitTeacherInfo.getSubject() == Subject.CHINESE) {
                    visitChiTeacherIds.add(visitTeacherInfo.getId());
                }
                visitTeacherIds.add(visitTeacherInfo.getId());
            }
        }
    }

    /**
     * 进校频繁拜访：拜访学校近30天拜访过2天及以上（不含今天这次拜访）
     *
     * @param nearly30DaysIntoSchoolWorkRecordListMap
     * @param schoolId
     * @return
     */
    public boolean intoSchoolFrequentlyVisit(Map<Long, List<WorkRecordData>> nearly30DaysIntoSchoolWorkRecordListMap, Long schoolId) {
        Boolean frequentlyVisit = false;
        if (MapUtils.isNotEmpty(nearly30DaysIntoSchoolWorkRecordListMap)) {
            List<WorkRecordData> nearly30DaysIntoSchoolWorkRecordList = nearly30DaysIntoSchoolWorkRecordListMap.get(schoolId);
            if (CollectionUtils.isNotEmpty(nearly30DaysIntoSchoolWorkRecordList)) {
                Map<String, List<WorkRecordData>> workTimeWorkRecordListMap = nearly30DaysIntoSchoolWorkRecordList.stream().collect(Collectors.groupingBy(item -> DateUtils.dateToString(item.getWorkTime(), DateUtils.FORMAT_SQL_DATE)));
                if (workTimeWorkRecordListMap.keySet().size() >= 2) {
                    frequentlyVisit = true;
                }
            }
        }
        return frequentlyVisit;
    }

    private List<WorkRecordTeacher> generateTeacherWorkRecord(Map<Long, School> teacherSchoolMap, String content, FollowUpType followUpType, Map<Long, String> teacherDataMap, Long userId, String userName) {
        if (MapUtils.isEmpty(teacherSchoolMap) || MapUtils.isEmpty(teacherDataMap)) {
            return Collections.emptyList();
        }
        Map<Long, List<Long>> mainSubTeachers = teacherResourceService.getMainSubTeachers(teacherDataMap.keySet());
        Set<Long> teacherIds = new HashSet<>();
        mainSubTeachers.forEach((k, v) -> {
            teacherIds.add(k);
            teacherIds.addAll(v);
        });
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);

        List<WorkRecordTeacher> workRecordTeachers = new ArrayList<>();
        mainSubTeachers.forEach((k, v) -> {
            WorkRecordTeacher record = new WorkRecordTeacher();
            School school = teacherSchoolMap.get(k);
            if (school != null) {
                record.setSchoolId(school.getId());
                record.setSchoolName(school.getCname());
            }
            record.setTeacherId(k);

            Set<Subject> subjects = new HashSet<>();
            Teacher teacher = teacherMap.get(k);
            if (teacher != null) {
                record.setTeacherName(teacher.getProfile() == null ? "" : teacher.getProfile().getRealname());
                if (teacher.getSubject() != null) {
                    subjects.add(teacher.getSubject());
                }
            }

            if (CollectionUtils.isNotEmpty(v)) {
                v.forEach(t -> {
                    Teacher subTeacher = teacherMap.get(t);
                    if (subTeacher != null && subTeacher.getSubject() != null) {
                        subjects.add(subTeacher.getSubject());
                    }
                });
            }
            record.setSubjects(new ArrayList<>(subjects));

            record.setFollowUpType(followUpType);
            record.setContent(content);
            String visitInfo = "";
            if (teacherDataMap.containsKey(k)) {
                visitInfo = teacherDataMap.get(k);
            } else {
                Long targetTeacherId = v.stream().filter(teacherDataMap::containsKey).findFirst().orElse(null);
                if (targetTeacherId != null) {
                    visitInfo = teacherDataMap.get(targetTeacherId);
                }
            }
            record.setResult(visitInfo);
            record.setUserId(userId);
            record.setUserName(userName);
            record.setWorkTime(new Date());
            workRecordTeachers.add(record);
        });
        return workRecordTeachers;
    }


    private List<WorkRecordOuterResource> generateOuterResourceWorkRecord(String content, Map<Long, String> outerResourceDataMap, Long userId, String userName) {
        if (MapUtils.isEmpty(outerResourceDataMap)) {
            return Collections.emptyList();
        }

        Map<Long, AgentResearchers> researchersMap = agentResearchersService.loadResearchers(outerResourceDataMap.keySet());

        List<AgentOuterResource> outerResourceList = agentOuterResourcePersistence.findListByIdsAndName(outerResourceDataMap.keySet(), "");
        if (MapUtils.isEmpty(researchersMap) && CollectionUtils.isEmpty(outerResourceList)) {
            return Collections.emptyList();
        }

        Map<Long, AgentOuterResource> outerResourceMap = outerResourceList.stream().collect(Collectors.toMap(AgentOuterResource::getId, Function.identity(), (o1, o2) -> o1));

        List<WorkRecordOuterResource> workRecordOuterResourceList = new ArrayList<>();
        outerResourceDataMap.forEach((k, v) -> {
            WorkRecordOuterResource record = new WorkRecordOuterResource();
            record.setOuterResourceId(k);
            AgentOuterResource agentOuterResource = outerResourceMap.get(k);
            if (agentOuterResource != null) {
                record.setOuterResourceName(agentOuterResource.getName());
            } else {
                AgentResearchers agentResearchers = researchersMap.get(k);
                if (agentResearchers != null) {
                    record.setOuterResourceName(agentResearchers.getName());
                }
            }
            record.setContent(content);
            record.setResult(v);
            record.setUserId(userId);
            record.setUserName(userName);
            record.setWorkTime(new Date());
            workRecordOuterResourceList.add(record);
        });
        return workRecordOuterResourceList;
    }


    /**
     * 添加组会工作记录
     *
     * @param meetingType
     * @param signInRecordId
     * @param meetingTitle
     * @param attendances
     * @param supporterDataList
     * @param isPresent
     * @param lecturerName
     * @param preachingTime
     * @param form
     * @param photoUrls
     * @param result
     * @param userId
     * @param userName
     * @return
     */
    public MapMessage saveMeetingWorkRecord(CrmMeetingType meetingType,
                                            String signInRecordId,
                                            String meetingTitle,
                                            Integer attendances,
                                            List<Map<String, Object>> supporterDataList,
                                            Boolean isPresent,
                                            String lecturerName,
                                            Integer preachingTime,
                                            Integer form,
                                            List<String> photoUrls,
                                            String result,
                                            Long userId,
                                            String userName
    ) {

        if (meetingType == null || StringUtils.isBlank(signInRecordId) || StringUtils.isBlank(lecturerName)
                || (preachingTime != 1 && preachingTime != 2 && preachingTime != 3)
                || (form != 1 && form != 2)
                || CollectionUtils.isEmpty(photoUrls)
        ) {
            return MapMessage.errorMessage("信息不全，请填写完整");
        }

        if (StringUtils.isBlank(meetingTitle) || attendances == null || CollectionUtils.isEmpty(supporterDataList)) {
            return MapMessage.errorMessage("信息不全，请填写完整");
        }
        if (!signInService.checkSignIn(signInRecordId)) {
            return MapMessage.errorMessage("没有签到信息，请签到");
        }


        WorkRecordMeeting record = new WorkRecordMeeting();
        record.setMeetingType(meetingType);

        record.setTitle(meetingTitle);
        record.setAttendances(attendances);

        List<WorkSupporter> supporterList = generateWorkSupporter(supporterDataList, isPresent, userId, userName);
        List<String> supporterRecordIds = workSupporterServiceClient.inserts(supporterList);
        record.setSupporterRecordList(supporterRecordIds);

        record.setSignInRecordId(signInRecordId);
        record.setLecturerName(lecturerName);
        record.setPreachingTime(preachingTime);
        record.setForm(form);
        record.setPhotoUrls(photoUrls);
        record.setContent(meetingTitle);
        record.setResult(result);

        record.setUserId(userId);
        record.setUserName(userName);
        record.setWorkTime(new Date());
        return saveWorkRecordResourceMeeting(record);
    }

    /**
     * 保存组会记录以及工作量记录
     *
     * @param workRecord
     * @return
     */
    public MapMessage saveWorkRecordResourceMeeting(WorkRecordMeeting workRecord) {
        if (workRecord == null) {
            return MapMessage.errorMessage("工作记录为空，保存工作记录失败！");
        }
        String id = workRecordMeetingServiceClient.insert(workRecord);
        workRecord.setId(id);

        WorkRecordData workRecordData = workRecordDataCompatibilityService.transformNewMeetingToWorkRecordDataList(Collections.singletonList(workRecord)).stream().findFirst().orElse(null);
        if (workRecordData != null) {
            //保存工作量记录
            AlpsThreadPool.getInstance().submit(() -> saveRecordWorkload(workRecordData));
        }
        return MapMessage.successMessage();
    }

    private List<WorkSupporter> generateWorkSupporter(List<Map<String, Object>> supporterDataList, Boolean isPresent, Long userId, String userName) {
        if (CollectionUtils.isEmpty(supporterDataList)) {
            return Collections.emptyList();
        }

        List<WorkSupporter> dataList = new ArrayList<>();
        supporterDataList.forEach(p -> {
            WorkSupporter workSupporter = new WorkSupporter();
            workSupporter.setSupporterId(SafeConverter.toLong(p.get("supportId")));
            workSupporter.setSupporterName(SafeConverter.toString(p.get("supportName")));
            workSupporter.setIsPresent(SafeConverter.toBoolean(isPresent));
            workSupporter.setContent(SafeConverter.toString(p.get("content")));
            workSupporter.setResult(SafeConverter.toString(p.get("result")));
            workSupporter.setUserId(userId);
            workSupporter.setUserName(userName);
            workSupporter.setWorkTime(new Date());
            dataList.add(workSupporter);
        });

        return dataList;
    }

    /**
     * 添加陪同之前校验接口
     *
     * @param userId
     * @param workRecordData
     * @return
     */
    public Map<String, Object> beforeAddAccompanyVisitRecord(Long userId, WorkRecordData workRecordData) {
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        //市经理陪同进校时，需确认学校的基础数据
        boolean needConfirmSchoolBaseInfo = false;
        Long schoolId = 0l;
        if (userRole == AgentRoleType.CityManager && workRecordData.getWorkType() == AgentWorkRecordType.SCHOOL) {
            needConfirmSchoolBaseInfo = true;
            schoolId = workRecordData.getSchoolId();
        }
        //进校中的校级组会、省市区级组会、拜访教研员类型，需上传照片
        boolean needUploadImg = false;
        if ((null != workRecordData.getVisitSchoolType() && workRecordData.getVisitSchoolType() == 1) || workRecordData.getWorkType() == AgentWorkRecordType.MEETING || workRecordData.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION) {
            needUploadImg = true;
        }
        //打分评价及发现的问题、建议，上级对下级显示，平级或下级对上级不显示
        boolean needShowAppraiseAndSuggest = false;
        AgentGroupUser agentGroupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
        boolean groupManager = baseOrgService.isGroupManager(userId, agentGroupUser.getGroupId());
        if (groupManager) {
            needShowAppraiseAndSuggest = true;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("needConfirmSchoolBaseInfo", needConfirmSchoolBaseInfo);
        dataMap.put("schoolId", schoolId);
        dataMap.put("needUploadImg", needUploadImg);
        dataMap.put("needShowAppraiseAndSuggest", needShowAppraiseAndSuggest);
        return dataMap;
    }

    /**
     * 添加陪同工作记录
     *
     * @param businessType
     * @param businessId
     * @param signInRecordId
     * @param purpose
     * @param photoUrls
     * @param result
     * @param evaluationMap
     * @param userId
     * @param userName
     * @return
     */
    public MapMessage saveAccompanyRecord(AccompanyBusinessType businessType, String businessId, String signInRecordId, String purpose, List<String> photoUrls, String result, Map<EvaluationIndicator, Integer> evaluationMap, Long userId, String userName) {

        if (!signInService.checkSignIn(signInRecordId)) {
            return MapMessage.errorMessage("没有签到信息，请签到");
        }

        //当前用户当天填写的陪同记录
        Date startDate = DayRange.current().getStartDate();
        Date endDate = DayRange.current().getEndDate();
        List<WorkRecordData> accompanyWorkRecordDataList = workRecordDataCompatibilityService.getWorkRecordDataListByUserTypeTime(Collections.singleton(userId), AgentWorkRecordType.ACCOMPANY, startDate, endDate);
        List<String> businessRecordIds = accompanyWorkRecordDataList.stream().filter(item -> StringUtils.isNotBlank(item.getBusinessRecordId())).map(WorkRecordData::getBusinessRecordId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(businessRecordIds) && businessRecordIds.contains(businessId)) {
            return MapMessage.errorMessage("当天已陪同过该记录！");
        }

        Long targetUserId = null;
        String targetUserName = "";
        if (businessType == AccompanyBusinessType.SCHOOL) {
            WorkRecordSchool workRecordSchool = workRecordSchoolLoaderClient.load(businessId);
            if (workRecordSchool == null) {
                return MapMessage.errorMessage("请选择陪同信息");
            }
            targetUserId = workRecordSchool.getUserId();
            targetUserName = workRecordSchool.getUserName();
        } else if (businessType == AccompanyBusinessType.MEETING) {
            WorkRecordMeeting meeting = workRecordMeetingLoaderClient.load(businessId);
            if (meeting == null) {
                return MapMessage.errorMessage("请选择陪同信息");
            }
            targetUserId = meeting.getUserId();
            targetUserName = meeting.getUserName();
        } else if (businessType == AccompanyBusinessType.RESOURCE_EXTENSION) {
            WorkRecordResourceExtension resourceExtension = workRecordResourceExtensionLoaderClient.load(businessId);
            if (resourceExtension != null) {
                targetUserId = resourceExtension.getUserId();
                targetUserName = resourceExtension.getUserName();
            }
        } else {
            return MapMessage.errorMessage("请选择陪同信息");
        }

        WorkRecordAccompany record = new WorkRecordAccompany();
        record.setBusinessType(businessType);
        record.setBusinessRecordId(businessId);
        record.setSignInRecordId(signInRecordId);
        record.setPhotoUrls(photoUrls);
        record.setPurpose(purpose);
        record.setResult(result);
        record.setUserId(userId);
        record.setUserName(userName);
        record.setWorkTime(new Date());

        if (MapUtils.isNotEmpty(evaluationMap)) {
            List<EvaluationRecord> evaluationList = new ArrayList<>();
            EvaluationBusinessType evaluationBusinessType = null;
            if (businessType == AccompanyBusinessType.SCHOOL) {
                evaluationBusinessType = EvaluationBusinessType.SCHOOL;
            } else if (businessType == AccompanyBusinessType.MEETING) {
                evaluationBusinessType = EvaluationBusinessType.MEETING;
            } else if (businessType == AccompanyBusinessType.RESOURCE_EXTENSION) {
                evaluationBusinessType = EvaluationBusinessType.RESOURCE_EXTENSION;
            }
            if (evaluationBusinessType != null) {
                for (EvaluationIndicator indicator : evaluationMap.keySet()) {
                    EvaluationRecord evaluation = new EvaluationRecord();
                    evaluation.setBusinessType(evaluationBusinessType);
                    evaluation.setBusinessRecordId(businessId);
                    evaluation.setIndicator(indicator);
                    evaluation.setResult(SafeConverter.toInt(evaluationMap.get(indicator)));

                    evaluation.setTargetUserId(targetUserId);
                    evaluation.setTargetUserName(targetUserName);

                    evaluation.setUserId(userId);
                    evaluation.setUserName(userName);
                    evaluation.setEvaluateTime(new Date());
                    evaluationList.add(evaluation);
                }
            }

            if (CollectionUtils.isNotEmpty(evaluationList)) {
                List<String> evaluationIds = evaluationRecordServiceClient.inserts(evaluationList);
                record.setEvaluationRecordList(evaluationIds);
            }
        }

        return saveWorkRecordResourceAccompany(record);
    }

    /**
     * 保存陪同记录以及工作量记录
     *
     * @param workRecord
     * @return
     */
    public MapMessage saveWorkRecordResourceAccompany(WorkRecordAccompany workRecord) {
        if (workRecord == null) {
            return MapMessage.errorMessage("工作记录为空，保存工作记录失败！");
        }
        String id = workRecordAccompanyServiceClient.insert(workRecord);
        workRecord.setId(id);

        WorkRecordData workRecordData = workRecordDataCompatibilityService.transformNewAccompanyToWorkRecordDataList(Collections.singletonList(workRecord)).stream().findFirst().orElse(null);
        if (workRecordData != null) {
            //保存工作量记录
            AlpsThreadPool.getInstance().submit(() -> saveRecordWorkload(workRecordData));
        }
        return MapMessage.successMessage();
    }

    /**
     * 获取上层资源
     *
     * @param schoolId
     * @return
     */
    public Map<String, Object> getOuterResourceList(Long schoolId) {
        //上层资源中未注册老师列表
        List<TeacherData> unRegTeaList = new ArrayList<>();
        //其他上层资源列表
        List<Map<String, Object>> otherList = new ArrayList<>();
        //上层资源列表
        List<Map<String, Object>> outerResourceList = agentOuterResourceService.getOuterResourceListBySchoolId(schoolId);
        outerResourceList.forEach(item -> {
            Integer job = (Integer) item.get("job");
            //未注册老师
            if (job == ResearchersJobType.UNREGISTERED_TEACHER.getJobId()) {
                TeacherData teacherData = new TeacherData();
                teacherData.setTeacherId((Long) item.get("id"));
                teacherData.setTeacherName((String) item.get("name"));
                teacherData.setSubject((Subject) item.get("subject"));

                //年级转换
                List<Integer> middleList = new ArrayList();
                List<Integer> highList = new ArrayList();
                List<Integer> juniorList = new ArrayList();
                List<Integer> gradeList = new ArrayList<>();
                List<ClazzLevel> gradeFinalList = new ArrayList<>();
                String gradeStr = (String) item.get("gradeStr");
                Map<String, Object> map = JsonUtils.convertJsonObjectToMap(gradeStr);
                if (map != null) {
                    middleList = (List) map.get("middle");
                    highList = (List) map.get("high");
                    juniorList = (List) map.get("junior");
                }
                if (CollectionUtils.isNotEmpty(juniorList)) {
                    gradeList.addAll(juniorList);
                }
                if (CollectionUtils.isNotEmpty(middleList)) {
                    gradeList.addAll(middleList);
                }
                if (CollectionUtils.isNotEmpty(highList)) {
                    highList.forEach(p -> gradeList.add(p + 1));
                }

                gradeList.forEach(p -> gradeFinalList.add(ClazzLevel.of(p)));

                teacherData.setGradeList(gradeFinalList);
                teacherData.setOrigin("outerResource");
                teacherData.setUnRegTeacher(true);
                unRegTeaList.add(teacherData);

                //其他上层资源
            } else {
                item.put("origin", "outerResource");
                otherList.add(item);
            }
        });
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("unRegTeaList", unRegTeaList);
        dataMap.put("otherList", otherList);
        return dataMap;
    }


    /**
     * 添加进校记录
     *
     * @param schoolId
     * @param signInRecordId
     * @param visitType
     * @param lecturerName
     * @param preachingTime
     * @param meetingForm
     * @param photoUrls
     * @param visitTheme
     * @param teacherDataMap
     * @param outerResourceDataMap
     * @param result
     * @param userId
     * @param userName
     * @param userPhone
     * @return
     */
    public MapMessage addIntoSchoolWorkRecord(Long schoolId, String signInRecordId, Integer visitType, String lecturerName, Integer preachingTime, Integer meetingForm,
                                              List<String> photoUrls, String visitTheme, Map<Long, String> teacherDataMap, Map<Long, String> outerResourceDataMap, String result, Long userId, String userName, String userPhone) {

        School school = raikouSystem.loadSchool(schoolId);
        if (school == null || (school.getSchoolAuthenticationState() != AuthenticationState.WAITING && school.getSchoolAuthenticationState() != AuthenticationState.SUCCESS)) {
            return MapMessage.errorMessage("学校信息有误！");
        }

        SignInRecord signInRecord = signInRecordLoaderClient.load(signInRecordId);
        if (signInRecord == null) {
            return MapMessage.errorMessage("没有签到信息，请签到");
        }
        if (signInRecord.getSignInType() == SignInType.PHOTO) {
            //新建学校审核信息
            MapMessage msg = schoolClueService.upsertSchoolClueBySchoolId(schoolId, signInRecord.getLatitude(), signInRecord.getLongitude(), userId
                    , userName, userPhone, signInRecord.getPhotoUrl(), signInRecord.getCoordinateType(), signInRecord.getAddress());
            if (!msg.isSuccess()) {
                return msg;
            }
        }

        WorkRecordSchool record = new WorkRecordSchool();
        record.setSchoolId(schoolId);
        record.setSchoolName(school.getCname());

        //校级会议
        if (visitType == 1) {
            record.setLecturerName(lecturerName);
            record.setPreachingTime(preachingTime);
            record.setMeetingForm(meetingForm);
            record.setPhotoUrls(photoUrls);
            //拜访老师进校
        } else if (visitType == 2) {
            record.setTitle(visitTheme);
        } else {
            record.setPhotoUrls(photoUrls);
        }

        //老师记录
        List<String> teacherRecordIds = new ArrayList<>();
        Map<Long, School> teacherSchoolMap = new HashMap<>();
        Map<Long, List<Long>> mainSubTeachers = teacherResourceService.getMainSubTeachers(teacherDataMap.keySet());
        mainSubTeachers.forEach((k, v) -> {
            teacherSchoolMap.put(k, school);
        });
        List<WorkRecordTeacher> workRecordTeachers = generateTeacherWorkRecord(teacherSchoolMap, "", FollowUpType.SCHOOL, teacherDataMap, userId, userName);
        if (CollectionUtils.isNotEmpty(workRecordTeachers)) {
            teacherRecordIds.addAll(workRecordTeacherServiceClient.inserts(workRecordTeachers));
        }

        //上层资源记录
        List<String> outerResourceRecordIds = new ArrayList<>();
        List<WorkRecordOuterResource> workRecordOuterResources = generateOuterResourceWorkRecord("", outerResourceDataMap, userId, userName);
        if (CollectionUtils.isNotEmpty(workRecordOuterResources)) {
            outerResourceRecordIds.addAll(workRecordOuterResourceServiceClient.inserts(workRecordOuterResources));
        }

        // 判断该进校人30天内进校次数
        Date startDate = DayRange.current().getStartDate();
        List<WorkRecordData> workRecordDataList = getWorkerWorkDataListByUserSchoolTypeTime(userId, schoolId, AgentWorkRecordType.SCHOOL, DateUtils.calculateDateDay(startDate, -30), new Date());
        record.setVisitCountLte30(workRecordDataList.size() + 1);

        Boolean sendNotify = false;
        SchoolLevel schoolLevel = SchoolLevel.safeParse(school.getLevel());
        //学校阶段为“初中、高中”时，30天以内第6次及以上拜访才发送消息
        if (schoolLevel == SchoolLevel.MIDDLE || schoolLevel == SchoolLevel.HIGH) {
            if (record.getVisitCountLte30() >= 6) {
                sendNotify = true;
            }
            //学校阶段为“小学、学前”的，保持不变
        } else if (schoolLevel == SchoolLevel.JUNIOR || schoolLevel == SchoolLevel.INFANT) {
            if (record.getVisitCountLte30() >= 3) {
                sendNotify = true;
            }
        }
        //发送预警信息
        if (sendNotify) {
            AgentUser cityManager = findGroupCityManager(userId);
            if (cityManager != null) {
                agentNotifyService.sendNotify(AgentNotifyType.INTO_SCHOOL_WARNING.getType(), "学校连续拜访",
                        StringUtils.formatMessage("{}近30天内被{}专员第{}次拜访，请您及时关注学校情况", record.getSchoolName(), record.getUserName(), record.getVisitCountLte30()), Collections.singletonList(cityManager.getId()), null);
            }
        }

        record.setContent("");
        record.setResult(result);

        record.setSignInRecordId(signInRecordId);
        record.setVisitType(visitType);
        record.setTeacherRecordList(teacherRecordIds);
        record.setOuterResourceRecordList(outerResourceRecordIds);
        record.setWorkTime(new Date());

        record.setUserId(userId);
        record.setUserName(userName);
        return saveWorkRecordSchool(record);
    }

    public AgentUser findGroupCityManager(Long userId) {
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        if (userRole == AgentRoleType.CityManager) {
            return baseOrgService.getUser(userId);
        }
        if (userRole == AgentRoleType.BusinessDeveloper) {
            AgentGroup group = baseOrgService.getGroupFirstOne(userId, AgentRoleType.BusinessDeveloper);
            AgentGroupUser groupUser = baseOrgService.getGroupUserByGroup(group.getId()).stream().filter(p -> p.getUserRoleType() == AgentRoleType.CityManager).findFirst().orElse(null);
            if (groupUser != null) {
                return baseOrgService.getUser(groupUser.getUserId());
            }
        }
        return null;
    }

    public void saveTeacherMemorandum(Map<Long, String> teacherDataMap, Long schoolId, String workRecordId, Long userId) {
        if (MapUtils.isEmpty(teacherDataMap)) {
            return;
        }
        teacherDataMap.forEach((k, v) -> {
            if (StringUtils.isNotBlank(v)) {
                agentMemorandumService.addMemorandum(userId, schoolId, k, v, MemorandumType.TEXT, workRecordId, null);
            }
        });
    }

    public String saveSchoolMemorandum(Long school, String schoolMemorandumInfo, String workRecordId, Long userId) {
        if (StringUtils.isNotBlank(schoolMemorandumInfo)) {
            MapMessage msg = agentMemorandumService.addMemorandum(userId, school, 0L, schoolMemorandumInfo, MemorandumType.TEXT, workRecordId, null);
            if (msg.isSuccess()) {
                return SafeConverter.toString(msg.get("id"));
            }
        }
        return null;
    }


    /**
     * 保存进校记录以及工作量记录
     *
     * @param workRecord
     * @return
     */
    public MapMessage saveWorkRecordSchool(WorkRecordSchool workRecord) {
        if (workRecord == null) {
            return MapMessage.errorMessage("工作记录为空，保存工作记录失败！");
        }
        String id = workRecordSchoolServiceClient.insert(workRecord);
        workRecord.setId(id);

        //更新上层资源记录中的拜访记录和类型
        List<String> outerResourceRecordList = workRecord.getOuterResourceRecordList();
        if (CollectionUtils.isNotEmpty(outerResourceRecordList)) {
            AlpsThreadPool.getInstance().submit(() -> workRecordOuterResourceServiceClient.updateWorkRecordIdAndType(outerResourceRecordList, id, AgentWorkRecordType.SCHOOL));
        }

        WorkRecordData workRecordData = workRecordDataCompatibilityService.transformNewIntoSchoolToWorkRecordDataList(Collections.singletonList(workRecord)).stream().findFirst().orElse(null);
        if (workRecordData != null) {
            //保存工作量记录
            AlpsThreadPool.getInstance().submit(() -> saveRecordWorkload(workRecordData));
        }
        return MapMessage.successMessage().add("id", id);
    }

    /**
     * 资源拓维保存
     *
     * @param intention
     * @param signInRecordId
     * @param visitPhotoUrls
     * @param content
     * @param teacherResultMap
     * @param outerResourceResultMap
     * @param result
     * @param userId
     * @param userName
     * @return
     */
    public MapMessage addResourceExtensionWorkRecord(Integer intention, String signInRecordId, List<String> visitPhotoUrls, String content, Map<Long, String> teacherResultMap, Map<Long, String> outerResourceResultMap,
                                                     String result, Long userId, String userName) {

        //当天不可重复拜访同一资源
        Date currentDate = new Date();
        Date startDate = DayRange.newInstance(currentDate.getTime()).getStartDate();
        Date endDate = DayRange.newInstance(currentDate.getTime()).getEndDate();
        List<Map<Long, String>> visitedUserList = getVisitedUserList(userId, startDate, endDate, teacherResultMap, outerResourceResultMap);
        if (CollectionUtils.isNotEmpty(visitedUserList)) {
            List<String> visitedUserNameList = new ArrayList<>();
            visitedUserList.forEach(item -> {
                visitedUserNameList.addAll(item.values());
            });
            String visitedUserNameStr = StringUtils.join(visitedUserNameList, "、");
            return MapMessage.errorMessage("今天已拜访过：" + visitedUserNameStr + "，请重新选择！");
        }
        //老师记录
        List<String> teacherRecordIds = new ArrayList<>();
        Map<Long, School> teacherSchoolMap = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchools(teacherResultMap.keySet()).getUninterruptibly();
        List<WorkRecordTeacher> workRecordTeachers = generateTeacherWorkRecord(teacherSchoolMap, "", FollowUpType.RESOURCE_EXTENSION, teacherResultMap, userId, userName);
        if (CollectionUtils.isNotEmpty(workRecordTeachers)) {
            teacherRecordIds.addAll(workRecordTeacherServiceClient.inserts(workRecordTeachers));
        }

        //上层资源记录
        List<String> outerResourceRecordIds = new ArrayList<>();
        List<WorkRecordOuterResource> workRecordOuterResources = generateOuterResourceWorkRecord("", outerResourceResultMap, userId, userName);
        if (CollectionUtils.isNotEmpty(workRecordOuterResources)) {
            outerResourceRecordIds.addAll(workRecordOuterResourceServiceClient.inserts(workRecordOuterResources));
        }

        WorkRecordResourceExtension workRecord = new WorkRecordResourceExtension();
        workRecord.setVisitIntention(intention);
        workRecord.setSignInRecordId(signInRecordId);
        workRecord.setPhotoUrls(visitPhotoUrls);
        workRecord.setContent(content);
        workRecord.setResult(result);
        workRecord.setTeacherRecordIds(teacherRecordIds);
        workRecord.setOuterResourceRecordIds(outerResourceRecordIds);
        workRecord.setWorkTime(new Date());
        workRecord.setUserId(userId);
        workRecord.setUserName(userName);
        return saveWorkRecordResourceExtension(workRecord);
    }

    /**
     * 保存资源拓维记录以及工作量记录
     *
     * @param workRecord
     * @return
     */
    public MapMessage saveWorkRecordResourceExtension(WorkRecordResourceExtension workRecord) {
        if (workRecord == null) {
            return MapMessage.errorMessage("工作记录为空，保存工作记录失败！");
        }

        WorkRecordData workRecordData = workRecordDataCompatibilityService.transformNewResourceExtensionToWorkRecordDataList(Collections.singletonList(workRecord)).stream().findFirst().orElse(null);
        if (workRecordData != null) {
            double maxWorkload = 6;
            //计算工作量T
            double workload = calRecordWorkload(workRecordData);
            if (workload > 0) {
                //获取当天该人员资源拓维工作量T
                Date currentDate = new Date();
                Date startDate = DayRange.newInstance(currentDate.getTime()).getStartDate();
                Date endDate = DayRange.newInstance(currentDate.getTime()).getEndDate();
                Double workloadSum = getResourceExtensionWorkload(workRecord.getUserId(), startDate, endDate);
                //设置上限，每天教研员拜访≤6T
                Double workloadSumFinal = MathUtils.doubleAdd(workloadSum, workload);
                if (workloadSumFinal > maxWorkload) {
                    workRecordData.setWorkload((maxWorkload - workloadSum) >= 0 ? (maxWorkload - workloadSum) : 0);
                } else {
                    workRecordData.setWorkload(workload);
                }
            }

            String id = workRecordResourceExtensionServiceClient.insert(workRecord);
            workRecordData.setId(id);

            //更新上层资源记录中的拜访记录和类型
            List<String> outerResourceRecordIds = workRecord.getOuterResourceRecordIds();
            if (CollectionUtils.isNotEmpty(outerResourceRecordIds)) {
                AlpsThreadPool.getInstance().submit(() -> workRecordOuterResourceServiceClient.updateWorkRecordIdAndType(outerResourceRecordIds, id, AgentWorkRecordType.RESOURCE_EXTENSION));
            }

            //保存工作量记录
            AlpsThreadPool.getInstance().submit(() -> saveRecordWorkload(workRecordData));
        }
        return MapMessage.successMessage();
    }

    /**
     * 获取资源拓维工作量T
     *
     * @param userId
     * @param startDate
     * @param endDate
     * @return
     */
    public Double getResourceExtensionWorkload(Long userId, Date startDate, Date endDate) {
        double workloadSum = 0d;
        List<AgentRecordWorkload> recordWorkloadList = new ArrayList<>();
        List<WorkRecordResourceExtension> workRecordList = workRecordResourceExtensionLoaderClient.loadByWorkersAndTime(Collections.singletonList(userId), startDate, endDate);
        if (CollectionUtils.isNotEmpty(workRecordList)) {
            Set<String> workRecordIds = workRecordList.stream().filter(Objects::nonNull).map(WorkRecordResourceExtension::getId).collect(Collectors.toSet());
            Map<String, AgentRecordWorkload> recordWorkLoadMap = agentRecordWorkloadDao.loadByWorkRecordIdsAndType(workRecordIds, AgentWorkRecordType.RESOURCE_EXTENSION);
            if (MapUtils.isNotEmpty(recordWorkLoadMap)) {
                recordWorkloadList.addAll(new ArrayList<>(recordWorkLoadMap.values()));
            }

        }
        for (AgentRecordWorkload workload : recordWorkloadList) {
            workloadSum = MathUtils.doubleAdd(workloadSum, SafeConverter.toDouble(workload.getWorkload()));
        }
        return workloadSum;
    }

    /**
     * 获取已拜访人员
     *
     * @param userId
     * @param startDate
     * @param endDate
     * @param teacherResultMap
     * @param outerResourceResultMap
     * @return
     */
    List<Map<Long, String>> getVisitedUserList(Long userId, Date startDate, Date endDate, Map<Long, String> teacherResultMap, Map<Long, String> outerResourceResultMap) {
        List<WorkRecordResourceExtension> todayWorkRecordResourceExtensionList = workRecordResourceExtensionLoaderClient.loadByWorkersAndTime(Collections.singletonList(userId), startDate, endDate);
        List<String> todayTeacherRecordIds = new ArrayList<>();
        List<String> todayOuterResourceRecordIds = new ArrayList<>();
        todayWorkRecordResourceExtensionList.forEach(item -> {
            todayTeacherRecordIds.addAll(item.getTeacherRecordIds());
            todayOuterResourceRecordIds.addAll(item.getOuterResourceRecordIds());
        });
        List<Map<Long, String>> visitedUserList = new ArrayList<>();
        Map<String, WorkRecordTeacher> workRecordTeacherMap = workRecordTeacherLoaderClient.loads(todayTeacherRecordIds);
        workRecordTeacherMap.forEach((k, v) -> {
            if (teacherResultMap.containsKey(v.getTeacherId())) {
                Map<Long, String> itemMap = new HashMap<>();
                itemMap.put(v.getTeacherId(), v.getTeacherName());
                visitedUserList.add(itemMap);
            }
        });
        Map<String, WorkRecordOuterResource> workRecordOuterResourceMap = workRecordOuterResourceLoaderClient.loads(todayOuterResourceRecordIds);
        workRecordOuterResourceMap.forEach((k, v) -> {
            if (outerResourceResultMap.containsKey(v.getOuterResourceId())) {
                Map<Long, String> itemMap = new HashMap<>();
                itemMap.put(v.getOuterResourceId(), v.getOuterResourceName());
                visitedUserList.add(itemMap);
            }
        });
        return visitedUserList;
    }

    public Map<Long, String> generateMapFormJson(String jsonStr) {
        Map<Long, String> resultMap = new HashMap<>();
        if (StringUtils.isNotBlank(jsonStr)) {
            List<Map> resultList = JsonUtils.fromJsonToList(jsonStr, Map.class);
            if (CollectionUtils.isNotEmpty(resultList)) {
                resultList.forEach(item -> {
                    resultMap.put(SafeConverter.toLong(item.get("id")), SafeConverter.toString(item.get("result")));
                });
            }
        }
        return resultMap;
    }


    /**
     * 生成资源拓维拜访人员数据
     *
     * @param visitResearcherMap
     * @return
     */
    public List<WorkRecordVisitUserInfo> generateResourceExtensionVisitUserInfo(Map<Long, String> visitResearcherMap) {
        List<WorkRecordVisitUserInfo> visitUserInfoList = new ArrayList<>();
        if (MapUtils.isEmpty(visitResearcherMap)) {
            return visitUserInfoList;
        }
        Map<Long, AgentResearchers> researchersMap = agentResearchersService.loadResearchers(visitResearcherMap.keySet());
        if (MapUtils.isNotEmpty(researchersMap)) {
            researchersMap.forEach((k, v) -> {
                WorkRecordVisitUserInfo visitUserInfo = new WorkRecordVisitUserInfo();
                visitUserInfo.setId(k);
                visitUserInfo.setName(v.getName());
                visitUserInfo.setResult(visitResearcherMap.get(k));
                visitUserInfo.setJob(ResearchersJobType.RESEARCHER.getJobId());
                String regionName = "";
                String subjectName = "";
                Integer level = v.getLevel();
                Integer regionCode = null;
                if (level == 1) {
                    regionCode = v.getProvince();
                } else if (level == 2) {
                    regionCode = v.getCity();
                } else if (level == 3) {
                    regionCode = v.getCounty();
                }
                ExRegion exRegion = raikouSystem.loadRegion(regionCode);
                if (exRegion != null) {
                    if (level == 1) {
                        regionName = exRegion.getProvinceName();
                    } else if (level == 2) {
                        regionName = exRegion.getCityName();
                    } else if (level == 3) {
                        regionName = exRegion.getCountyName();
                    }
                }
                subjectName = v.getSubject() != null && v.getSubject() != Subject.UNKNOWN ? v.getSubject().getValue() : "";
                visitUserInfo.setRegionName(regionName);
                visitUserInfo.setSubjectName(subjectName);
                visitUserInfoList.add(visitUserInfo);
            });
        }
        return visitUserInfoList;
    }


    /**
     * 拼装资源拓维标题
     *
     * @param workRecordData
     * @return
     */
    public List<String> generateResourceExtensionTitleList(WorkRecordData workRecordData) {
        List<String> resourceExtensionTitleList = new ArrayList<>();
        if (workRecordData == null) {
            return resourceExtensionTitleList;
        }
        List<WorkRecordVisitUserInfo> visitUserInfoList = workRecordData.getVisitUserInfoList();
        if (CollectionUtils.isNotEmpty(visitUserInfoList)) {
            List<WorkRecordVisitUserInfo> visitKpInfoList = new ArrayList<>();
            List<WorkRecordVisitUserInfo> visitTeaInfoList = new ArrayList<>();
            visitUserInfoList.forEach(item -> {
                if (!Objects.equals(item.getJob(), WorkRecordVisitUserInfo.TEACHER_JOB)) {
                    visitKpInfoList.add(item);
                } else {
                    visitTeaInfoList.add(item);
                }
            });
            visitKpInfoList.forEach(p -> {
                resourceExtensionTitleList.add((p.getName() != null ? p.getName() : "") + (p.getJobName() != null ? "(" + p.getJobName() + ")" : ""));
            });
            visitTeaInfoList.forEach(p -> {
                resourceExtensionTitleList.add((p.getName() != null ? p.getName() : "") + (p.getJobName() != null ? "(" + p.getJobName() + ")" : ""));
            });
        }
        return resourceExtensionTitleList;
    }

    /**
     * 根据ids拼装资源拓维标题
     *
     * @param ids
     * @return
     */
    public List<String> generateResourceExtensionTitleByIds(Collection<Long> ids) {
        List<String> resourceExtensionTitleList = new ArrayList<>();
        Map<Long, Map<String, Object>> resourceInfoMap = agentOuterResourceService.getResourceInfoByIds(ids);
        resourceInfoMap.forEach((k, v) -> {
            if (MapUtils.isNotEmpty(v)) {
                resourceExtensionTitleList.add((v.get("resourceName") != null ? SafeConverter.toString(v.get("resourceName")) : "") + (v.get("jobName") != null ? "(" + SafeConverter.toString(v.get("jobName")) + ")" : ""));
            }
        });
        return resourceExtensionTitleList;
    }

    /**
     * 拼装资源拓维负责区域
     *
     * @param workRecordData
     * @return
     */
    public List<String> generateResourceExtensionManageRegionList(WorkRecordData workRecordData) {
        List<String> manageRegionList = new ArrayList<>();
        if (workRecordData == null) {
            return manageRegionList;
        }
        List<WorkRecordVisitUserInfo> visitUserInfoList = workRecordData.getVisitUserInfoList();
        visitUserInfoList.forEach(p -> {
            if (ResearchersJobType.typeOf(p.getJob()) == ResearchersJobType.RESEARCHER) {
                manageRegionList.add(p.getRegionName() + "(" + p.getSubjectName() + ")");
            } else {
                manageRegionList.add(p.getRegionName());
            }
        });
        return manageRegionList;
    }

    /**
     * 陪同详情
     *
     * @param workRecordData
     * @return
     */
    public MapMessage accompanyWorkRecordDetail(WorkRecordData workRecordData) {
        MapMessage mapMessage = MapMessage.successMessage();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("workTime", DateUtils.dateToString(workRecordData.getWorkTime(), "yyyy/MM/dd HH:mm:ss"));
        dataMap.put("accompanyUserName", workRecordData.getAccompanyUserName());
        dataMap.put("purpose", workRecordData.getPurpose());
        //获取被陪同拜访记录ID
        String businessRecordId = workRecordData.getBusinessRecordId();
        AccompanyBusinessType businessType = workRecordData.getBusinessType();
        WorkRecordData accompanyWorkRecordData = workRecordDataCompatibilityService.getWorkRecordDataByIdAndType(businessRecordId, AgentWorkRecordType.nameOf(businessType.name()));
        if (accompanyWorkRecordData == null) {
            return MapMessage.errorMessage("拜访信息不存在");
        }
        //陪同主题
        String accompanyTheme = "";
        //进校
        if (accompanyWorkRecordData.getWorkType() == AgentWorkRecordType.SCHOOL) {
            if (accompanyWorkRecordData.getVisitSchoolType() == 1) {
                accompanyTheme = accompanyWorkRecordData.getSchoolName() + "-校级会议";
            } else if (accompanyWorkRecordData.getVisitSchoolType() == 3) {
                accompanyTheme = accompanyWorkRecordData.getSchoolName() + "-直播展位推广";
            } else {
                accompanyTheme = accompanyWorkRecordData.getSchoolName();
            }
        }
        //组会
        if (accompanyWorkRecordData.getWorkType() == AgentWorkRecordType.MEETING) {
            //组会主题
            accompanyTheme = accompanyWorkRecordData.getWorkTitle();
        }
        //资源拓维
        if (accompanyWorkRecordData.getWorkType() == AgentWorkRecordType.RESOURCE_EXTENSION) {
            List<String> titleList = generateResourceExtensionTitleList(accompanyWorkRecordData);
            accompanyTheme = StringUtils.join(titleList, "、");
        }
        dataMap.put("workType", workRecordData.getWorkType());
        dataMap.put("accompanyTheme", accompanyTheme);
        dataMap.put("address", workRecordData.getAddress());
        List<String> photoUrls = workRecordData.getPhotoUrls();
        if (CollectionUtils.isNotEmpty(photoUrls)) {
            dataMap.put("imgUrl", photoUrls.get(0));
        }
        Map<EvaluationIndicator, Integer> evaluationMap = workRecordData.getEvaluationMap();
        if (MapUtils.isNotEmpty(evaluationMap)) {
            dataMap.put("preparationScore", evaluationMap.get(EvaluationIndicator.PREPARATION_SCORE));
            dataMap.put("productProficiencyScore", evaluationMap.get(EvaluationIndicator.PRODUCT_PROFICIENCY_SCORE));
            dataMap.put("resultMeetExpectedResultScore", evaluationMap.get(EvaluationIndicator.RESULT_MEET_EXPECTED_RESULT_SCORE));
        }
        dataMap.put("accompanySuggest", workRecordData.getResult());
        mapMessage.put("dataMap", dataMap);
        return mapMessage;
    }

    public List<WorkRecordVisitUserInfo> getVisitUserList(List<Teacher> teacherList, Map<Long, WorkRecordTeacher> teacherWorkRecordMap, Map<Long, Long> subMainTeacherIdMap) {
        if (CollectionUtils.isEmpty(teacherList)) {
            return Collections.emptyList();
        }

        List<WorkRecordVisitUserInfo> res = new ArrayList<>();
        teacherList.forEach(p -> CollectionUtils.addNonNullElement(res, getVisitUserInfo(p, teacherWorkRecordMap, subMainTeacherIdMap)));
        return res;
    }

    private WorkRecordVisitUserInfo getVisitUserInfo(Teacher teacher, Map<Long, WorkRecordTeacher> teacherWorkRecordMap, Map<Long, Long> subMainTeacherIdMap) {
        WorkRecordVisitUserInfo visitUserInfo = new WorkRecordVisitUserInfo();
        if (teacher == null) {
            return visitUserInfo;
        }
        if (SCHOOL_MASTER_INFO.containsKey(teacher.getId())) {
            visitUserInfo.setId(teacher.getId());
            visitUserInfo.setName(SCHOOL_MASTER_INFO.get(teacher.getId()));
        } else {
            visitUserInfo.setId(teacher.getId());
            UserProfile profile = teacher.getProfile();
            visitUserInfo.setName(profile == null ? "" : profile.getRealname());
            visitUserInfo.setSubject(teacher.getSubject());
            visitUserInfo.setSubjectName(teacher.getSubject() != null ? teacher.getSubject().getValue() : "");
        }
        visitUserInfo.setJob(WorkRecordVisitUserInfo.TEACHER_JOB);
        visitUserInfo.setJobName(WorkRecordVisitUserInfo.TEACHER_JOB_NAME);

        WorkRecordTeacher workRecordTeacher = teacherWorkRecordMap.get(teacher.getId());
        //主账号
        if (workRecordTeacher != null) {
            visitUserInfo.setResult(workRecordTeacher.getResult());
        } else {
            Long mainTeacherId = subMainTeacherIdMap.get(teacher.getId());
            WorkRecordTeacher workRecordTeacher1 = teacherWorkRecordMap.get(mainTeacherId);
            if (workRecordTeacher1 != null) {
                visitUserInfo.setResult(workRecordTeacher1.getResult());
            }
        }
        return visitUserInfo;
    }

    /**
     * 新旧工作类型转化
     *
     * @param workRecordType
     * @return
     */
    public CrmWorkRecordType convertWorkRecordType(AgentWorkRecordType workRecordType) {
        CrmWorkRecordType crmWorkRecordType = null;
        if (workRecordType == AgentWorkRecordType.SCHOOL) {
            crmWorkRecordType = CrmWorkRecordType.SCHOOL;
        } else if (workRecordType == AgentWorkRecordType.MEETING) {
            crmWorkRecordType = CrmWorkRecordType.MEETING;
        } else if (workRecordType == AgentWorkRecordType.RESOURCE_EXTENSION) {
            crmWorkRecordType = CrmWorkRecordType.TEACHING;
        } else if (workRecordType == AgentWorkRecordType.ACCOMPANY) {
            crmWorkRecordType = CrmWorkRecordType.VISIT;
        }
        return crmWorkRecordType;
    }

    /**
     * 获取被陪访工作记录
     *
     * @param workRecordData
     * @return
     */
    public WorkRecordData getAccompanyWorkRecord(WorkRecordData workRecordData) {
        WorkRecordData accompanyWorkRecordData = null;
        if (workRecordData != null && workRecordData.getWorkType() == AgentWorkRecordType.ACCOMPANY) {
            AccompanyBusinessType businessType = workRecordData.getBusinessType();
            if (businessType != null) {
                accompanyWorkRecordData = workRecordDataCompatibilityService.getWorkRecordDataByIdAndType(workRecordData.getBusinessRecordId(), AgentWorkRecordType.nameOf(workRecordData.getBusinessType().name()));
            }
        }
        return accompanyWorkRecordData;
    }

    public Map<String, Object> generateWorkRecordMeetingDetail(WorkRecordData workRecordData) {
        Map<String, Object> workRecordMap = new HashMap<>();
        if (workRecordData == null) {
            return workRecordMap;
        }
        workRecordMap.putAll(BeanMapUtils.tansBean2Map(workRecordData));
        workRecordMap.put("visitUserStr", StringUtils.join(generateResourceExtensionTitleList(workRecordData), "、"));
        return workRecordMap;
    }

    /**
     * 获取工作记录（兼容新旧数据）
     *
     * @param workRecordId
     * @param workRecordType
     * @return
     */
    public WorkRecordData getWorkRecordDataByIdAndType(String workRecordId, AgentWorkRecordType workRecordType) {
        return workRecordDataCompatibilityService.getWorkRecordDataByIdAndType(workRecordId, workRecordType);
    }

    /**
     * 获取工作记录列表（兼容新旧数据）
     *
     * @param userIds
     * @param workRecordType
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordData> getWorkRecordDataListByUserTypeTime(Collection<Long> userIds, AgentWorkRecordType workRecordType, Date startDate, Date endDate) {
        return workRecordDataCompatibilityService.getWorkRecordDataListByUserTypeTime(userIds, workRecordType, startDate, endDate);
    }

    /**
     * 获取工作记录的陪访记录
     *
     * @param businessRecordId
     * @return
     */
    public List<WorkRecordData> getAccompanyRecordsByBusinessRecordId(String businessRecordId) {
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        List<CrmWorkRecord> workRecordList = getVisitRecordsByIntoRecordId(businessRecordId);
        if (CollectionUtils.isNotEmpty(workRecordList)) {
            workRecordDataList.addAll(workRecordDataCompatibilityService.transformOldToWorkRecordDataList(workRecordList));
        } else {
            List<WorkRecordAccompany> workRecordAccompanyList = workRecordAccompanyLoaderClient.loadByBusinessRecordId(businessRecordId);
            workRecordDataList.addAll(workRecordDataCompatibilityService.transformNewAccompanyToWorkRecordDataList(workRecordAccompanyList));
        }
        return workRecordDataList;
    }

    /**
     * 获取工作数据列表
     *
     * @param userId
     * @param schoolId
     * @param workType
     * @param startDate
     * @param endDate
     * @return
     */
    public List<WorkRecordData> getWorkerWorkDataListByUserSchoolTypeTime(Long userId, Long schoolId, AgentWorkRecordType workType, Date startDate, Date endDate) {
        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        if (userId == null || startDate == null || endDate == null || endDate.before(startDate)) {
            return Collections.emptyList();
        }
        workRecordDataList.addAll(workRecordDataCompatibilityService.getWorkRecordDataListByUserTypeTime(Collections.singleton(userId), workType, startDate, endDate));
        if (CollectionUtils.isNotEmpty(workRecordDataList)) {
            workRecordDataList = workRecordDataList.stream()
                    .filter(p -> schoolId != null && Objects.equals(schoolId, p.getSchoolId()))
                    .collect(Collectors.toList());
        }
        return workRecordDataList;
    }

    public List<Map<String, Object>> convertMap(List<Map> originMapList) {
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(originMapList)) {
            for (Map<String, Object> originMap : originMapList) {
                Map<String, Object> resultMap = new HashMap<>();
                Iterator<String> originMapIt = originMap.keySet().iterator();
                while (originMapIt.hasNext()) {
                    String key = originMapIt.next();
                    Object value = originMap.get(key);
                    resultMap.put(key, value);
                }
                resultMapList.add(resultMap);
            }
        }
        return resultMapList;
    }

    /**
     * 获取学校拜访记录
     *
     * @param schoolId
     * @return
     */
    public List<WorkRecordData> getSchoolWorkRecords(Long schoolId) {
        if (schoolId == null || schoolId.equals(0L)) {
            return Collections.emptyList();
        }

        List<CrmWorkRecord> oldSchoolWorkRecord = crmWorkRecordLoaderClient.findBySchool(schoolId).stream()
                .filter(p -> p.getWorkType() == CrmWorkRecordType.SCHOOL)
                .collect(Collectors.toList());

        List<WorkRecordSchool> newSchoolWorkRecord = workRecordSchoolLoaderClient.findBySchoolId(schoolId);

        List<WorkRecordData> workRecordDataList = new ArrayList<>();
        workRecordDataList.addAll(workRecordDataCompatibilityService.transformOldToWorkRecordDataList(oldSchoolWorkRecord));
        workRecordDataList.addAll(workRecordDataCompatibilityService.transformNewIntoSchoolToWorkRecordDataList(newSchoolWorkRecord));
        return workRecordDataList.stream().sorted((o1, o2) -> o2.getWorkTime().compareTo(o1.getWorkTime())).collect(Collectors.toList());
    }

    /**
     * 获取最近一次学校拜访记录
     *
     * @param schoolId
     * @return
     */
    public WorkRecordData getFirstSchoolWorkRecord(Long schoolId) {
        List<WorkRecordData> tempList = getSchoolWorkRecords(schoolId);
        return tempList.stream().findFirst().orElse(null);
    }
}

