package com.voxlearning.utopia.agent.service.mobile.resource;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.authentication.StuAuthConditionInEsService;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.group.GroupOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.group.GroupOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.teacher.TeacherOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.teacher.TeacherOnlineIndicator;
import com.voxlearning.utopia.agent.bean.outerresource.AgentOuterResourceView;
import com.voxlearning.utopia.agent.constants.AgentTagTargetType;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTag;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.mobile.TeacherFakeService;
import com.voxlearning.utopia.agent.service.platform.AgentPlatformUserInfoService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.view.teacher.TeacherBasicInfo;
import com.voxlearning.utopia.agent.view.teacher.TeacherGroupInfo;
import com.voxlearning.utopia.agent.view.teacher.TeacherStatisticsInfo;
import com.voxlearning.utopia.agent.view.teacher.TeacherSubject;
import com.voxlearning.utopia.entity.TeacherRoles;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.business.consumer.CertificationManagementClient;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.BasicReviewHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.VacationHomeworkPackageLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.constants.TeacherRolesType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.TeacherRolesServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 教师资源Service
 *
 * @author chunlin.yu
 * @create 2017-11-10 14:46
 **/
@Named
public class TeacherResourceService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;

    @Inject
    private PerformanceService performanceService;
    @Inject
    private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject
    private CertificationManagementClient certificationManagementClient;
    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private TeacherFakeService teacherFakeService;
    @Inject
    private SearchService searchService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private VacationHomeworkPackageLoaderClient vacationHomeworkPackageLoaderClient; // 假期作业接口
    @Inject
    private BasicReviewHomeworkLoaderClient basicReviewHomeworkLoaderClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private TeacherRolesServiceClient teacherRolesServiceClient;

    @Inject private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    @Inject
    private SchoolResourceService schoolResourceService;
    @Inject
    private AgentPlatformUserInfoService platformUserInfoService;


    @Inject
    private AgentTagService agentTagService;

    @ImportService(interfaceClass = StuAuthConditionInEsService.class)
    private StuAuthConditionInEsService stuAuthConditionInEsService;
//    /**
//     * 加载学下不活跃老师
//     * @param schoolId
//     * @return
//     */
//    public List<TeacherListCard> loadUnActiveTeachers(Long schoolId){
//        Set<Long> schoolTeacherIds = teacherLoaderClient.loadSchoolTeacherIds(schoolId);
//        Integer lastSuccessDataDay = performanceService.lastSuccessDataDay();
//        Date previousMonthEndDate = MonthRange.newInstance(DateUtils.stringToDate(String.valueOf(lastSuccessDataDay), "yyyyMMdd").getTime()).previous().getEndDate();
//        Integer previousMonthEndDay = Integer.valueOf(DateUtils.dateToString(previousMonthEndDate, "yyyyMMdd"));
//        if (CollectionUtils.isNotEmpty(schoolTeacherIds)){
////            Map<Long, AgentTeacher17PerformanceData> lastSuccessDataDayDataMap = new HashMap<>();
////            Map<Long, AgentTeacher17PerformanceData> previousMonthEndDayDataMap = new HashMap<>();
//            Map<Long, TeacherOnlineIndicator> lastSuccessDataDayDataMap = new HashMap<>();
//            Map<Long, TeacherOnlineIndicator> previousMonthEndDayDataMap = new HashMap<>();
//            //分批获取
//            AgentResourceService.batchIds(schoolTeacherIds,500).forEach((k,v) -> {
//                Map<Long, TeacherOnlineIndicator> tempMap1 = loadNewSchoolServiceClient.loadTeacherOnlineIndicator(v,lastSuccessDataDay);
////                Map<Long, AgentTeacher17PerformanceData> tempMap1 = loadPerformanceServiceClient.loadTeacher17PerformanceData(v,lastSuccessDataDay);
//                if (MapUtils.isNotEmpty(tempMap1)){
//                    lastSuccessDataDayDataMap.putAll(tempMap1);
//                }
////                Map<Long, AgentTeacher17PerformanceData> tempMap2= loadPerformanceServiceClient.loadTeacher17PerformanceData(v,previousMonthEndDay);
//                Map<Long, TeacherOnlineIndicator> tempMap2= loadNewSchoolServiceClient.loadTeacherOnlineIndicator(v,previousMonthEndDay);
//                if (MapUtils.isNotEmpty(tempMap2)){
//                    previousMonthEndDayDataMap.putAll(tempMap2);
//                }
//            });
//            List<Long> unActiveTeacherIds = new ArrayList<>();
//            schoolTeacherIds.stream().forEach(key -> {
////                AgentTeacher17PerformanceData lastSuccessPerformanceData = lastSuccessDataDayDataMap.get(key);
////                AgentTeacher17PerformanceData previousMonthEndDayPerformanceData = previousMonthEndDayDataMap.get(key);
//                TeacherOnlineIndicator lastSuccessPerformanceData = lastSuccessDataDayDataMap.get(key);
//                TeacherOnlineIndicator previousMonthEndDayPerformanceData = previousMonthEndDayDataMap.get(key);
//                if (previousMonthEndDayPerformanceData != null && previousMonthEndDayPerformanceData.getIndicatorMap().get(previousMonthEndDayPerformanceData.getDay()) != null && previousMonthEndDayPerformanceData.getIndicatorMap().get(previousMonthEndDayPerformanceData.getDay()).getTmHwSc() > 0){
//                    int startMonthHwSc = previousMonthEndDayPerformanceData.getIndicatorMap().get(previousMonthEndDayPerformanceData.getDay()).getTmHwSc();
//                    //本月布置数量
//                    int endMonthHwsc = 0;
//                    if (null != lastSuccessPerformanceData && null != lastSuccessPerformanceData.getIndicatorMap().get(lastSuccessPerformanceData.getDay()) && lastSuccessPerformanceData.getIndicatorMap().get(lastSuccessPerformanceData.getDay()).getTmHwSc() > 0){
//                        endMonthHwsc = lastSuccessPerformanceData.getIndicatorMap().get(lastSuccessPerformanceData.getDay()).getTmHwSc();
//                    }
//                    //本月布置小于上月布置并且本月布置小于3
//                    if (endMonthHwsc < startMonthHwSc && endMonthHwsc < 3){
//                        unActiveTeacherIds.add(key);
//                    }
//                }
//
//            });
//            if (CollectionUtils.isNotEmpty(unActiveTeacherIds)){
//                Map<Long, CrmTeacherSummary> teacherSummaryMap = new HashMap<>();
//                //分批获取
//                AgentResourceService.batchIds(unActiveTeacherIds,500).forEach((k,v) -> {
//                    Map<Long, CrmTeacherSummary> tempMap = crmSummaryLoaderClient.loadTeacherSummary(v);
//                    teacherSummaryMap.putAll(tempMap);
//                });
//                return agentResourceMapperService.generateTeacherList(unActiveTeacherIds, teacherSummaryMap, false);
//            }
//        }
//        return new ArrayList<>();
//    }


    // 获取老师主账号及关联的子账号
    public Map<Long, List<Long>> getMainSubTeachers(Collection<Long> teacherIds) {
        Set<Long> mainAccounts = getMainTeachers(teacherIds);
        Map<Long, List<Long>> retList = new HashMap<>();
        Map<Long, List<Long>> mainSubMap = teacherLoaderClient.loadSubTeacherIds(mainAccounts);
        mainAccounts.forEach(p -> {
            List<Long> subList = mainSubMap.get(p);
            if (CollectionUtils.isNotEmpty(subList)) {
                retList.put(p, subList);
            } else {
                retList.put(p, new ArrayList<>());
            }
        });
        return retList;
    }

    // 获取老师的主账号
    private Set<Long> getMainTeachers(Collection<Long> teacherIds) {
        Map<Long, Long> subMainTeacherMap = teacherLoaderClient.loadMainTeacherIds(teacherIds);
        Set<Long> mainAccounts = new HashSet<>();
        if (MapUtils.isNotEmpty(subMainTeacherMap)) {
            teacherIds.forEach(p -> {
                Long mainAccount = subMainTeacherMap.get(p);
                if (mainAccount == null) {
                    mainAccounts.add(p);
                } else {
                    mainAccounts.add(mainAccount);
                }
            });
        } else {
            mainAccounts.addAll(teacherIds);
        }
        return mainAccounts;
    }


    /**
     * 返回老师主账号的基本信息
     *
     * @param teacherIds           老师ids
     * @param includeHiddenTeacher 是否包含隐藏的老师，true:包含    false:不包含
     * @param includeFakeTeacher   是否排除掉假老师    true: 包含假老师，    false： 不包含假老师
     * @param includeKpiData       返回记录里面是否包含基本的指标数据   true: 包含    false: 不包含
     * @return
     */
    public List<TeacherBasicInfo> generateTeacherBasicInfo(Collection<Long> teacherIds, boolean includeHiddenTeacher, boolean includeFakeTeacher, boolean includeKpiData, boolean multithread) {
        Map<Long, List<Long>> mainSubMap = getMainSubTeachers(teacherIds);

        Map<Long, Boolean> hiddenTeacherMap = isHiddenTeacher(mainSubMap.keySet());   // 获取老师的隐藏状态
        Map<Long, Boolean> realTeacherMap = isRealTeacher(mainSubMap.keySet());       // 获取老师是否是真老师

        Iterator<Map.Entry<Long, List<Long>>> iterator = mainSubMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, List<Long>> entry = iterator.next();
            Long key = entry.getKey();
            if (!includeHiddenTeacher && hiddenTeacherMap.get(key)) {          // 过滤掉隐藏老师
                iterator.remove();
            } else if (!includeFakeTeacher && !realTeacherMap.get(key)) {        // 过滤掉假老师
                iterator.remove();
            }
        }

        Set<Long> allTeacherIds = new HashSet<>();
        mainSubMap.forEach((k, v) -> {
            allTeacherIds.add(k);
            allTeacherIds.addAll(v);
        });
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(allTeacherIds);

        Map<Long, School> schoolMap = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchools(mainSubMap.keySet()).getUninterruptibly();

        Map<Long, TeacherOfflineIndicator> teacherOfflineIndicatorMap = new HashMap<>();
        Map<Long, TeacherOfflineIndicator> lmTeacherOfflineIndicatorMap = new HashMap<>();
        Map<Long, TeacherOnlineIndicator> teacherOnlineIndicatorMap = new HashMap<>();
        Map<Long, TeacherOnlineIndicator> lmTeacherOnlineIndicatorMap = new HashMap<>();
        if (includeKpiData) {
            Integer day = performanceService.lastSuccessDataDay();
            teacherOfflineIndicatorMap = loadNewSchoolServiceClient.loadTeacherOfflineIndicator(allTeacherIds, day);
            teacherOnlineIndicatorMap = loadNewSchoolServiceClient.loadTeacherOnlineIndicator(allTeacherIds, day);

            Date lastDayDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
            Date lastMonthLastDayDate = MonthRange.newInstance(lastDayDate.getTime()).previous().getEndDate();
            Integer lastMonthLastDay = Integer.valueOf(DateUtils.dateToString(lastMonthLastDayDate, "yyyyMMdd"));
            lmTeacherOfflineIndicatorMap = loadNewSchoolServiceClient.loadTeacherOfflineIndicator(teacherIds, lastMonthLastDay);
            lmTeacherOnlineIndicatorMap = loadNewSchoolServiceClient.loadTeacherOnlineIndicator(allTeacherIds, lastMonthLastDay);
        }

        //老师标签
        List<String> teacherIdList = new ArrayList<>();
        allTeacherIds.forEach(p -> teacherIdList.add(SafeConverter.toString(p)));
        Map<String, List<AgentTag>> teacherTagMap = agentTagService.getTagListByTargetIdsAndType(teacherIdList, AgentTagTargetType.TEACHER, true);

        List<TeacherBasicInfo> retList = new ArrayList<>();
        for (Long teacherId : mainSubMap.keySet()) {
            Teacher teacher = teacherMap.get(teacherId);
            if (teacher == null) {
                continue;
            }
//            String phone = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "agent:generateTeacherExtStateInfo", "");
            TeacherBasicInfo teacherBasicInfo = new TeacherBasicInfo();
            teacherBasicInfo.setTeacherId(teacher.getId());
//            teacherBasicInfo.setMobile(phone);
            teacherBasicInfo.setTeacherName(teacher.fetchRealname());
            teacherBasicInfo.setAuthState(teacher.getAuthenticationState());
            teacherBasicInfo.setIsRealTeacher(realTeacherMap.get(teacherId));
            teacherBasicInfo.setIsHidden(hiddenTeacherMap.get(teacherId));
            teacherBasicInfo.setIsNewTeacher(DateUtils.addDays(teacher.getCreateTime(), 15).after(new Date()));
            if (!multithread) {
                teacherBasicInfo.setAvatarImgUrl(getUserAvatarImgUrl(teacher));
            }

            teacherBasicInfo.setCreateTime(teacher.getCreateTime());
            teacherBasicInfo.setAuthTime(teacher.getLastAuthDate());
            School school = schoolMap.get(teacherId);
            if (school != null) {
                teacherBasicInfo.setSchoolId(school.getId());
                teacherBasicInfo.setSchoolName(school.getCname());
                teacherBasicInfo.setSchoolLevel(SchoolLevel.safeParse(school.getLevel()));
            }

            if (teacher.isJuniorTeacher() || teacher.isSeniorTeacher()) { // 初高中老师的情况下, 设置校本题库管理员和学科组长
                List<TeacherRoles> teacherRoles = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacher.getId());

                TeacherRoles bankManager = teacherRoles.stream()
                        .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SCHOOL_BANK_MANAGER.name()))
                        .findAny().orElse(null);
                if (bankManager != null) {
                    teacherBasicInfo.setIsSchoolQuizBankAdmin(true);
                }

                TeacherRoles subjectLeader = teacherRoles.stream()
                        .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SUBJECT_LEADER.name()))
                        .findAny().orElse(null);
                if (subjectLeader != null) {
                    teacherBasicInfo.setIsSubjectLeader(true);
                }
            }


            Set<Long> relatedIds = new LinkedHashSet<>();
            relatedIds.add(teacherId);
            List<Long> subList = mainSubMap.get(teacherId);
            if (CollectionUtils.isNotEmpty(subList)) {
                relatedIds.addAll(subList);
            }

            for (Long relatedId : relatedIds) {
                Teacher relatedTeacher = teacherMap.get(relatedId);
                if (relatedTeacher == null) {
                    continue;
                }

                TeacherSubject teacherSubject = new TeacherSubject();
                teacherSubject.setTeacherId(relatedId);
                teacherSubject.setSubject(relatedTeacher.getSubject());
                teacherSubject.setSubjectName(relatedTeacher.getSubject() == null ? "" : relatedTeacher.getSubject().getValue());
                if (includeKpiData && realTeacherMap.get(teacherId)) { // 真老师的情况下设置指标数据
//                    AgentTeacher17PerformanceData teacher17PerformanceData = teacher17PerformanceDataMap.get(relatedId);
                    TeacherOnlineIndicator teacherOnlineIndicator = teacherOnlineIndicatorMap.get(relatedId);
                    TeacherOnlineIndicator lmTeacherOnlineIndicator = lmTeacherOnlineIndicatorMap.get(relatedId);
                    if (teacherOnlineIndicator != null && teacherOnlineIndicator.getIndicatorMap() != null) {
                        //本月
                        OnlineIndicator onlineIndicator = teacherOnlineIndicator.fetchMonthData();
                        //汇总
                        OnlineIndicator sumOnlineIndicator = teacherOnlineIndicator.fetchSumData();
                        //上月
                        OnlineIndicator lmOnlineIndicator = lmTeacherOnlineIndicator.fetchMonthData();
                        teacherSubject.getKpiData().put("regStuCount", sumOnlineIndicator != null ? (sumOnlineIndicator.getRegStuCount() != null ? sumOnlineIndicator.getRegStuCount() : 0) : 0);
                        teacherSubject.getKpiData().put("auStuCount", sumOnlineIndicator != null ? (sumOnlineIndicator.getRegStuCount() != null ? sumOnlineIndicator.getAuStuCount() : 0) : 0);
                        if (lmOnlineIndicator != null) {
                            teacherSubject.getKpiData().put("lmHwSc", lmOnlineIndicator.getTmHwSc() != null ? lmOnlineIndicator.getTmHwSc() : 0);//老师上月布置作业套数

                            teacherSubject.getKpiData().put("lmFinCsHwGte3AuStuCount", SafeConverter.toInt(lmOnlineIndicator.getIncSettlementSglSubjStuCount()) + SafeConverter.toInt(lmOnlineIndicator.getReturnSettleNum()));//上月当前科目月活
                        } else {
                            teacherSubject.getKpiData().put("lmHwSc", 0);//老师上月布置作业套数
                            teacherSubject.getKpiData().put("lmFinCsHwGte3AuStuCount", 0);//上月当前科目月活
                        }

                        if (onlineIndicator != null) {
                            teacherSubject.getKpiData().put("classCount", sumOnlineIndicator != null ? SafeConverter.toInt(sumOnlineIndicator.getGroupCount()) : 0);//老师带班数teacher17PerformanceData.getClazzCount()
                            teacherSubject.getKpiData().put("tmGroupMaxHwSc", onlineIndicator.getMaxHwSuitCount());//所有班组最大作业套数
                            teacherSubject.getKpiData().put("tmGroupMinHwSc", onlineIndicator.getMinHwSuitCount());//所有班组最小作业套数
                            teacherSubject.getKpiData().put("tmFinCsHwGte3AuStuCount", SafeConverter.toInt(onlineIndicator.getIncSettlementSglSubjStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNum()));//月活teacher17PerformanceData.getFinCsHwGte3AuStuCount()
                            teacherSubject.getKpiData().put("tmHwSc", onlineIndicator.getTmHwSc() != null ? onlineIndicator.getTmHwSc() : 0);//布置所有作业套数


                            teacherSubject.getKpiData().put("potentialInc1StuCount", onlineIndicator.getFinSglSubjHwEq1UnSettleStuCount());//新增1套
                            teacherSubject.getKpiData().put("potentialInc2StuCount", onlineIndicator.getFinSglSubjHwEq2UnSettleStuCount());//新增2套
                            Integer finSglSubjHwEq1UnSettleStuCount = onlineIndicator.getFinSglSubjHwEq1UnSettleStuCount();
                            Integer finSglSubjHwEq2UnSettleStuCount = onlineIndicator.getFinSglSubjHwEq2UnSettleStuCount();
                            teacherSubject.getKpiData().put("potentialIncStuCount", (finSglSubjHwEq1UnSettleStuCount == null ? 0 : finSglSubjHwEq1UnSettleStuCount) + (finSglSubjHwEq2UnSettleStuCount == null ? 0 : finSglSubjHwEq2UnSettleStuCount));//新增潜力值
                            teacherSubject.getKpiData().put("potentialBack1StuCount", onlineIndicator.getFinSglSubjHwEq1SettleStuCount());//回流1套
                            teacherSubject.getKpiData().put("potentialBack2StuCount", onlineIndicator.getFinSglSubjHwEq2SettleStuCount());//回流2套
                            Integer finSglSubjHwEq1SettleStuCount = onlineIndicator.getFinSglSubjHwEq1SettleStuCount();
                            Integer finSglSubjHwEq2SettleStuCount = onlineIndicator.getFinSglSubjHwEq2SettleStuCount();
                            teacherSubject.getKpiData().put("potentialBackStuCount", (finSglSubjHwEq1SettleStuCount == null ? 0 : finSglSubjHwEq1SettleStuCount) + (finSglSubjHwEq2SettleStuCount == null ? 0 : finSglSubjHwEq2SettleStuCount));//回流潜力值
                            teacherSubject.getKpiData().put("latestHwTime", onlineIndicator.getLatestHwTime());//最后布置作业时间
                        }
                    }
                    if (teacherBasicInfo.getSchoolLevel() == SchoolLevel.MIDDLE || teacherBasicInfo.getSchoolLevel() == SchoolLevel.HIGH) {
                        TeacherOfflineIndicator teacherOfflineIndicator = teacherOfflineIndicatorMap.get(relatedId);
                        if (teacherOfflineIndicator != null && teacherOfflineIndicator.fetchSumData() != null) {
                            OfflineIndicator offlineIndicator = teacherOfflineIndicator.fetchSumData();
                            teacherSubject.getKpiData().put("klxTnCount", offlineIndicator.getKlxTotalNum());
                        }
                        if (teacherOfflineIndicator != null && teacherOfflineIndicator.fetchMonthData() != null) {
                            OfflineIndicator offlineIndicator = teacherOfflineIndicator.fetchMonthData();
                            teacherSubject.getKpiData().put("tmGte2Num", SafeConverter.toInt(offlineIndicator.getSettlementGte2Num()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2Num()));
                            teacherSubject.getKpiData().put("tmScanTpCount", SafeConverter.toInt(offlineIndicator.getScanPaperNum()));
                        }
                        TeacherOfflineIndicator lmTeacherOfflineIndicator = lmTeacherOfflineIndicatorMap.get(relatedId);
                        if (lmTeacherOfflineIndicator != null && lmTeacherOfflineIndicator.fetchMonthData() != null) {
                            OfflineIndicator lmOfflineIndicator = lmTeacherOfflineIndicator.fetchMonthData();
                            teacherSubject.getKpiData().put("lmGte2Num", SafeConverter.toInt(lmOfflineIndicator.getSettlementGte2Num()) + SafeConverter.toInt(lmOfflineIndicator.getUnsettlementGte2Num()));
                            teacherSubject.getKpiData().put("lmScanTpCount", SafeConverter.toInt(lmOfflineIndicator.getScanPaperNum()));
                        }
                    }
                }
                teacherBasicInfo.getSubjects().add(teacherSubject);

                if (teacherOnlineIndicatorMap.containsKey(teacherId)) {
                    TeacherOnlineIndicator teacherOnlineIndicator = teacherOnlineIndicatorMap.get(teacherId);
                    if (null != teacherOnlineIndicator) {
                        OnlineIndicator onlineIndicator = teacherOnlineIndicator.fetchSumData();
                        //布置假期作业的班组数
                        teacherBasicInfo.setVacnHwGroupCount(onlineIndicator.getVacnHwGroupCount() != null ? onlineIndicator.getVacnHwGroupCount() : 0);
                        //布置期末作业的班组数
                        teacherBasicInfo.setTermReviewGroupCount(onlineIndicator.getTermReviewGroupCount() != null ? onlineIndicator.getTermReviewGroupCount() : 0);
                    }
                }
            }

            teacherBasicInfo.setIsParent(platformUserInfoService.isParent(teacherId));

            teacherBasicInfo.setTagList(teacherTagMap.get(SafeConverter.toString(teacherId)));

            retList.add(teacherBasicInfo);
        }
        return retList;
    }

    public TeacherBasicInfo generateTeacherBasicInfo(Long teacherId, boolean includeKpiData) {
        List<TeacherBasicInfo> teacherBasicInfoList = generateTeacherBasicInfo(Collections.singleton(teacherId), true, true, includeKpiData, false);
        return teacherBasicInfoList.stream().filter(p -> {
            if (Objects.equals(p.getTeacherId(), teacherId)) {
                return true;
            } else {
                return p.getSubjects().stream().anyMatch(s -> Objects.equals(s.getTeacherId(), teacherId));
            }
        }).findFirst().orElse(null);
    }

    public Teacher getMainTeacher(Long teacherId) {
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId == null) {
            mainTeacherId = teacherId;
        }
        return teacherLoaderClient.loadTeacher(mainTeacherId);
    }

    public Map<String, Object> generateTeacherExtStateInfo(Long teacherId) {
        Teacher teacher = getMainTeacher(teacherId);
        if (teacher == null) {
            return Collections.emptyMap();
        }
        List<GroupTeacherMapper> groupTeacherMapperList = groupLoaderClient.loadTeacherGroups(teacherId, false);
        groupTeacherMapperList = groupTeacherMapperList.stream().filter(p -> p.isTeacherGroupRefStatusValid(teacherId)).collect(Collectors.toList()); // 过滤出有效的组

        Map<String, Object> retMap = new HashMap<>();
        retMap.put("registerTime", teacher.getCreateTime());
//        String phone = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "agent:generateTeacherExtStateInfo", getCurrentUser().getUserName());
//        retMap.put("mobile", phone);
        if (CollectionUtils.isEmpty(groupTeacherMapperList)) {
            retMap.put("classCount", 0);
        } else {
            Set<Long> clazzIdList = groupTeacherMapperList.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toSet());
            //通过班级id 标识 获取 班级信息
            Map<Long, Clazz> clazzMap = raikouSystem.loadClazzesIncludeDisabled(clazzIdList);
            // 过滤掉毕业班
            clazzMap = clazzMap.values().stream()
                    .filter(p -> !p.isDisabledTrue() && p.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && p.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED && p.getClazzLevel() != ClazzLevel.INFANT_GRADUATED)
                    .collect(Collectors.toMap(Clazz::getId, Function.identity(), (o1, o2) -> o1));
            retMap.put("classCount", clazzMap.keySet().size());
        }


        if (!isRealTeacher(teacherId)) {
            List<CrmTeacherFake> ctfList = teacherFakeService.findFakedTeacher(teacher.getId());
            if (CollectionUtils.isNotEmpty(ctfList)) {//过滤空数据
                Collections.sort(ctfList, (o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
                CrmTeacherFake ctf = ctfList.get(0);
                retMap.put("fakeTime", ctf.getCreateTime());
                retMap.put("fakeCreatorName", ctf.getFakerName());
                retMap.put("fakeDesc", ctf.getFakeNote());
            }
        } else {
            retMap.put("authState", teacher.getAuthenticationState());
            retMap.put("authTime", teacher.getLastAuthDate());
            // 小学老师，并且未认证的情况下，设置认证进度     初高中不显示认证进度
            if (teacher.isPrimarySchool() && teacher.fetchCertificationState() != AuthenticationState.SUCCESS) { // 老师认证进度：未认证的情况下，设置各个认证条件的达标情况
                UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(teacher.getId());
                if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
                    retMap.put("auth1Achieved", true);
                } else {
                    retMap.put("auth1Achieved", false);
                }
                if (certificationManagementClient.getRemoteReference().hasEnoughStudentsBindParentMobileOrBindSelfMobile(teacher.getId())) {
                    retMap.put("auth2Achieved", true);
                } else {
                    retMap.put("auth2Achieved", false);
                }
                boolean auth3Achieved = false;
                try {
                    auth3Achieved = certificationManagementClient.getRemoteReference().hasEnoughStudentsFinishedHomework(teacher.getId());
                } catch (Exception e) {
                    logger.error("调用 hasEnoughStudentsFinishedHomework 异常 ： teacherId:", teacherId, e);
                }
                retMap.put("auth3Achieved", auth3Achieved);
            }
        }
        return retMap;
    }

    // 获取老师所在的班组信息
    public List<TeacherGroupInfo> generateTeacherGroupList(Long teacherId) {
        if (!isRealTeacher(teacherId)) {
            return Collections.emptyList();
        }

        List<GroupTeacherMapper> groupTeacherMapperList = groupLoaderClient.loadTeacherGroups(teacherId, false);
        groupTeacherMapperList = groupTeacherMapperList.stream().filter(p -> p.isTeacherGroupRefStatusValid(teacherId)).collect(Collectors.toList()); // 过滤出有效的组
        if (CollectionUtils.isEmpty(groupTeacherMapperList)) {
            return Collections.emptyList();
        }

        Set<Long> clazzIdList = groupTeacherMapperList.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toSet());
        //通过班级id 标识 获取 班级信息
        Map<Long, Clazz> clazzMap = raikouSystem.loadClazzesIncludeDisabled(clazzIdList);

        // 过滤掉毕业班
        clazzMap = clazzMap.values().stream()
                .filter(p -> !p.isDisabledTrue() && p.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && p.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED && p.getClazzLevel() != ClazzLevel.INFANT_GRADUATED)
                .collect(Collectors.toMap(Clazz::getId, Function.identity(), (o1, o2) -> o1));
        Set<Long> validClazzIdList = clazzMap.keySet();
        // 获得有效的班组
        List<GroupTeacherMapper> validTeacherGroupList = groupTeacherMapperList.stream().filter(p -> validClazzIdList.contains(p.getClazzId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(validTeacherGroupList)) {
            return Collections.emptyList();
        }

        List<TeacherGroupInfo> resultList = new ArrayList<>();
        for (GroupTeacherMapper groupMapper : validTeacherGroupList) {
            Clazz clazz = clazzMap.get(groupMapper.getClazzId());
            if (clazz == null || clazz.isDisabledTrue()) {
                continue;
            }
            TeacherGroupInfo groupInfo = new TeacherGroupInfo();
            groupInfo.setGrade(clazz.getClazzLevel().getLevel());
            groupInfo.setClassId(clazz.getId());
            groupInfo.setClassName(clazz.getClassName());
            groupInfo.setClassFullName(clazz.formalizeClazzName());
            groupInfo.setGroupId(groupMapper.getId());
            groupInfo.setUpdateTime(clazz.getUpdateTime());
            resultList.add(groupInfo);
        }
        resultList.sort((o1, o2) -> {
            if (!Objects.equals(o1.getGrade(), o2.getGrade())) {
                return Integer.compare(o1.getGrade(), o2.getGrade());
            } else {
                String className1 = o1.getClassName();
                String className2 = o2.getClassName();
                int classNo1 = 99;
                int classNo2 = 99;
                if (StringUtils.isNotBlank(className1)) {
                    classNo1 = SafeConverter.toInt(className1.replaceAll("班", ""), 99);
                }
                if (StringUtils.isNotBlank(className2)) {
                    classNo2 = SafeConverter.toInt(className2.replaceAll("班", ""), 99);
                }
                return Integer.compare(classNo1, classNo2);
            }
        });
        return resultList;
    }


    // mode：  1:online   2:offline
    public List<TeacherGroupInfo> generateTeacherGroupListWithKpiData(Long teacherId, int mode) {
        List<TeacherGroupInfo> teacherGroupInfoList = generateTeacherGroupList(teacherId);
        if (CollectionUtils.isEmpty(teacherGroupInfoList)) {
            return Collections.emptyList();
        }
        Set<Long> groupIds = teacherGroupInfoList.stream().map(TeacherGroupInfo::getGroupId).collect(Collectors.toSet());
        Integer day = performanceService.lastSuccessDataDay();
        if (mode == 1) {

            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (null == teacher) {
                return Collections.emptyList();
            }
//            boolean isJuniorTeacher = teacher.isPrimarySchool() || teacher.isInfantTeacher();
//            Map<Long, Boolean> groupJuniorVacationHwMap = new HashMap<>();
//            Map<Long, Boolean> groupMiddleVacationHwMap = new HashMap<>();
//            Map<Long, Boolean> groupJuniorReviewHwMap = new HashMap<>();
//            //如果是小学老师
//            if (isJuniorTeacher){
//                //班组与是否布置小学假期作业关系
//                Map<Long, List<VacationHomeworkPackage.Location>> groupVacationHwMap = vacationHomeworkPackageLoaderClient.loadVacationHomeworkPackageByClazzGroupIds(groupIds);
//                groupVacationHwMap.forEach((k, v) -> groupJuniorVacationHwMap.put(k, CollectionUtils.isNotEmpty(v)));
//
//                //班组与是否布置小学期末作业关系
//                Map<Long, List<BasicReviewHomeworkPackage>> groupReviewHwMap = basicReviewHomeworkLoaderClient.loadBasicReviewHomeworkPackageByClazzGroupIds(groupIds);
//                groupReviewHwMap.forEach((k, v) -> groupJuniorReviewHwMap.put(k, CollectionUtils.isNotEmpty(v)));
//                //如果是中学老师
//            }else {
//                //班组与是否布置中学期末作业关系
//                String url = "http://zx.17zuoye.com";
//                if(RuntimeMode.isDevelopment() || RuntimeMode.isTest()){
//                    url = "http://zx.test.17zuoye.net";
//                }else if(RuntimeMode.isStaging()){
//                    url = "http://zx.staging.17zuoye.net";
//                }
//                url += "/rpc/crm/getVacationAssignStatus";
//                Map<Object, Object> parameterMap = new HashMap<>();
//                parameterMap.put("groupIds", JsonUtils.toJson(groupIds));
//                parameterMap.put("year", SafeConverter.toInt(DateUtils.dateToString(new Date(),"yyyy")));
//                parameterMap.put("type", "SUMMER");
//                try {
//                    AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).addParameter(parameterMap).execute();
//                    if (response.getStatusCode() == 200) {
//                        Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
//                        if (MapUtils.isNotEmpty(resultMap)) {
//                            Object result = resultMap.get("success");
//                            Boolean bResult = (Boolean) result;
//                            if (bResult) {
//                                Map<String, Boolean> dataMap = (Map<String, Boolean>) resultMap.get("data");
//                                dataMap.forEach((k, v) -> groupMiddleVacationHwMap.put(SafeConverter.toLong(k), SafeConverter.toBoolean(v)));
//                            }
//                        }
//                    }
//                }catch (Exception e){
//                    logger.error("http request error :  url= " + url , e);
//                    emailServiceClient.createPlainEmail()
//                            .body("http request error :  url= " + url+ "\r\n参数：" + JsonUtils.toJson(parameterMap))
//                            .subject("中学是否布置寒假作业接口调用失败【" + RuntimeMode.current().getStageMode() + "】")
//                            .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
//                            .send();
//                }
//            }


//            Map<Long, AgentGroup17PerformanceData> agentGroup17PerformanceDataMap = loadPerformanceServiceClient.loadGroup17PerformanceData(groupIds, day);
            Map<Long, GroupOnlineIndicator> groupOnlineIndicatorMap = loadNewSchoolServiceClient.loadGroupOnlineIndicator(groupIds, day);
            for (TeacherGroupInfo groupInfo : teacherGroupInfoList) {
//                AgentGroup17PerformanceData groupPerformance = agentGroup17PerformanceDataMap.get(groupInfo.getGroupId());
                GroupOnlineIndicator groupOnlineIndicator = groupOnlineIndicatorMap.get(groupInfo.getGroupId());
                if (groupOnlineIndicator != null && groupOnlineIndicator.getIndicatorMap() != null) {
                    OnlineIndicator onlineIndicator = groupOnlineIndicator.fetchMonthData();
                    OnlineIndicator sumOnlineIndicator = groupOnlineIndicator.fetchSumData();
                    if (onlineIndicator != null) {
                        groupInfo.getGroupKpiData().put("regStuCount", sumOnlineIndicator != null ? (sumOnlineIndicator.getRegStuCount() != null ? sumOnlineIndicator.getRegStuCount() : 0) : 0);//注册数(至今)
                        groupInfo.getGroupKpiData().put("auStuCount", sumOnlineIndicator != null ? (sumOnlineIndicator.getRegStuCount() != null ? sumOnlineIndicator.getAuStuCount() : 0) : 0);//认证数（至今
                        groupInfo.getGroupKpiData().put("tmFinCsHwEq1StuCount", 0);//groupPerformance.getIndicatorData().getFinCsHwEq1StuCount() 班组一套（暂时可能不用了）
                        groupInfo.getGroupKpiData().put("tmFinCsHwEq2StuCount", 0);//groupPerformance.getIndicatorData().getFinCsHwEq2StuCount() 班组二套（暂时可能不用了）
                        groupInfo.getGroupKpiData().put("tmFinCsHwGte3AuStuCount", SafeConverter.toInt(onlineIndicator.getIncSettlementSglSubjStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNum()));//月活teacher17PerformanceData.getFinCsHwGte3AuStuCount()
                        groupInfo.getGroupKpiData().put("tmHwSc", onlineIndicator.getTmHwSc());//布置所有作业套数
                        groupInfo.getGroupKpiData().put("newMonthActive", SafeConverter.toInt(onlineIndicator.getIncSettlementSglSubjStuCount()));//新增月活
                        groupInfo.getGroupKpiData().put("backStuCount", SafeConverter.toInt(onlineIndicator.getReturnSettleNum()));//回流人数
                        Integer finSglSubjHwEq1UnSettleStuCount = onlineIndicator.getFinSglSubjHwEq1UnSettleStuCount();
                        Integer finSglSubjHwEq2UnSettleStuCount = onlineIndicator.getFinSglSubjHwEq2UnSettleStuCount();
                        groupInfo.getGroupKpiData().put("potentialIncStuCount", (finSglSubjHwEq1UnSettleStuCount == null ? 0 : finSglSubjHwEq1UnSettleStuCount) + (finSglSubjHwEq2UnSettleStuCount == null ? 0 : finSglSubjHwEq2UnSettleStuCount));//新增潜力人数
                        Integer finSglSubjHwEq1SettleStuCount = onlineIndicator.getFinSglSubjHwEq1SettleStuCount();
                        Integer finSglSubjHwEq2SettleStuCount = onlineIndicator.getFinSglSubjHwEq2SettleStuCount();
                        groupInfo.getGroupKpiData().put("potentialBackStuCount", (finSglSubjHwEq1SettleStuCount == null ? 0 : finSglSubjHwEq1SettleStuCount) + (finSglSubjHwEq2SettleStuCount == null ? 0 : finSglSubjHwEq2SettleStuCount));//回流潜力人数
//                        //如果是小学老师
//                        if (isJuniorTeacher){
//                            //是否已布置假期作业
//                            groupInfo.getGroupKpiData().put("vacnHwFlag", SafeConverter.toBoolean(groupJuniorVacationHwMap.get(groupInfo.getGroupId())));
//                            //是否已布置期末作业
//                            groupInfo.getGroupKpiData().put("termReviewFlag", SafeConverter.toBoolean(groupJuniorReviewHwMap.get(groupInfo.getGroupId())));
//                            //如果是中学老师
//                        }else {
//                            //是否已布置假期作业
//                            groupInfo.getGroupKpiData().put("vacnHwFlag", SafeConverter.toBoolean(groupMiddleVacationHwMap.get(groupInfo.getGroupId())));
//                            //是否已布置期末作业
//                            groupInfo.getGroupKpiData().put("termReviewFlag", false);
//                        }
                        //是否已布置假期作业
                        groupInfo.getGroupKpiData().put("vacnHwFlag", sumOnlineIndicator.getVacnHwFlag());
                        //是否已布置期末作业
                        groupInfo.getGroupKpiData().put("termReviewFlag", sumOnlineIndicator.getTermReviewFlag());
                    }

                }
            }
        } else if (mode == 2) {
            Map<Long, GroupOfflineIndicator> groupOfflineIndicatorMap = loadNewSchoolServiceClient.loadGroupOfflineIndicator(groupIds, day);
            for (TeacherGroupInfo groupInfo : teacherGroupInfoList) {
                GroupOfflineIndicator groupOfflineIndicator = groupOfflineIndicatorMap.get(groupInfo.getGroupId());
                if (groupOfflineIndicator != null && groupOfflineIndicator.fetchSumData() != null) {
                    OfflineIndicator offlineIndicator = groupOfflineIndicator.fetchSumData();
                    groupInfo.getGroupKpiData().put("klxTnCount", SafeConverter.toInt(offlineIndicator.getKlxTotalNum()));
                }
                if (groupOfflineIndicator != null && groupOfflineIndicator.fetchMonthData() != null) {
                    OfflineIndicator offlineIndicator = groupOfflineIndicator.fetchMonthData();
                    // 周测1套
                    groupInfo.getGroupKpiData().put("tmGte1Num", SafeConverter.toInt(offlineIndicator.getSettlementNum()) + SafeConverter.toInt(offlineIndicator.getUnsettlementNum()));
                    groupInfo.getGroupKpiData().put("tmSettlementGte1Num", SafeConverter.toInt(offlineIndicator.getSettlementNum()));     //有线上作业
                    groupInfo.getGroupKpiData().put("tmUnSettlementGte1Num", SafeConverter.toInt(offlineIndicator.getUnsettlementNum())); //无线上作业
                    // 周测2套
                    groupInfo.getGroupKpiData().put("tmGte2Num", SafeConverter.toInt(offlineIndicator.getSettlementGte2Num()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2Num()));
                    groupInfo.getGroupKpiData().put("tmSettlementGte2Num", SafeConverter.toInt(offlineIndicator.getSettlementGte2Num()));     //有线上作业
                    groupInfo.getGroupKpiData().put("tmUnSettlementGte2Num", SafeConverter.toInt(offlineIndicator.getUnsettlementGte2Num())); //无线上作业
                }
            }
        }
        return teacherGroupInfoList;
    }

    // mode：  1:online   2:offline
    public Map<String, Object> generateTeacherKpiData(Long teacherId, int mode) {
        if (!isRealTeacher(teacherId)) {
            return Collections.emptyMap();
        }

        Map<String, Object> retMap = new HashMap<>();
        Integer day = performanceService.lastSuccessDataDay();
        if (mode == 1) {
//            AgentTeacher17PerformanceData teacherPerformance = loadPerformanceServiceClient.loadTeacher17PerformanceData(Collections.singleton(teacherId), day).get(teacherId);
            TeacherOnlineIndicator teacherOnlineIndicator = loadNewSchoolServiceClient.loadTeacherOnlineIndicator(Collections.singleton(teacherId), day).get(teacherId);
            //logger.info(JsonUtils.toJson(teacherOnlineIndicator));
            if (teacherOnlineIndicator != null && teacherOnlineIndicator.getIndicatorMap() != null) {
                OnlineIndicator onlineIndicator = teacherOnlineIndicator.fetchMonthData();
                OnlineIndicator sumOnlineIndicator = teacherOnlineIndicator.fetchSumData();
                retMap.put("regStuCount", sumOnlineIndicator != null ? (sumOnlineIndicator.getRegStuCount() != null ? sumOnlineIndicator.getRegStuCount() : 0) : 0);//注册数(至今)
                retMap.put("auStuCount", sumOnlineIndicator != null ? (sumOnlineIndicator.getRegStuCount() != null ? sumOnlineIndicator.getAuStuCount() : 0) : 0);//认证数（至今）
                retMap.put("tmFinCsHwGte3AuStuCount", SafeConverter.toInt(onlineIndicator.getIncSettlementSglSubjStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNum()));//月活
                retMap.put("latestHwTime", onlineIndicator.getLatestHwTime());//最后布置作业时间
                retMap.put("tmHwSc", onlineIndicator.getTmHwSc());//本月布置所有作业套数
                retMap.put("tmFinCsHwGte3IncAuStuCount", onlineIndicator.getIncSettlementSglSubjStuCount());//本月新增人数总人数
                retMap.put("backStuCount", SafeConverter.toInt(onlineIndicator.getReturnSettleNum()));//本月回流人数总人数
                Integer finSglSubjHwEq1UnSettleStuCount = onlineIndicator.getFinSglSubjHwEq1UnSettleStuCount();
                Integer finSglSubjHwEq2UnSettleStuCount = onlineIndicator.getFinSglSubjHwEq2UnSettleStuCount();
                retMap.put("potentialIncStuCount", (finSglSubjHwEq1UnSettleStuCount == null ? 0 : finSglSubjHwEq1UnSettleStuCount) + (finSglSubjHwEq2UnSettleStuCount == null ? 0 : finSglSubjHwEq2UnSettleStuCount));//新增潜力人数
                Integer finSglSubjHwEq1SettleStuCount = onlineIndicator.getFinSglSubjHwEq1SettleStuCount();
                Integer finSglSubjHwEq2SettleStuCount = onlineIndicator.getFinSglSubjHwEq2SettleStuCount();
                retMap.put("potentialBackStuCount", (finSglSubjHwEq1SettleStuCount == null ? 0 : finSglSubjHwEq1SettleStuCount) + (finSglSubjHwEq2SettleStuCount == null ? 0 : finSglSubjHwEq2SettleStuCount));//潜力回流人数
                retMap.put("latestHw120", onlineIndicator.getLatestHwTime() != null ? DateUtils.dayDiff(new Date(), onlineIndicator.getLatestHwTime()) : null);//上次布置作业至今天数
                retMap.put("latestUpdateTime", day);//最后更新时间
            }
        } else if (mode == 2) {
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//            AgentTeacherKlxPerformanceData teacherPerformance = loadPerformanceServiceClient.loadTeacherKlxPerformanceData(Collections.singleton(teacherId), day).get(teacherId);
            Map<Long, TeacherOfflineIndicator> teacherOfflineIndicatorMap = loadNewSchoolServiceClient.loadTeacherOfflineIndicator(Collections.singleton(teacherId), day);
            TeacherOfflineIndicator teacherOfflineIndicator = teacherOfflineIndicatorMap.get(teacherId);
            if (teacherOfflineIndicator != null && teacherOfflineIndicator.fetchSumData() != null) {
                OfflineIndicator offlineIndicator = teacherOfflineIndicator.fetchSumData();
                retMap.put("klxTnCount", offlineIndicator.getKlxTotalNum());//快乐学账号数
            }
            if (teacherOfflineIndicator != null && teacherOfflineIndicator.fetchMonthData() != null) {
                OfflineIndicator offlineIndicator = teacherOfflineIndicator.fetchMonthData();
                retMap.put("tmGte1Num", SafeConverter.toInt(offlineIndicator.getSettlementNum()) + SafeConverter.toInt(offlineIndicator.getUnsettlementNum()));
                retMap.put("tmSettlementGte1Num", SafeConverter.toInt(offlineIndicator.getSettlementNum()));
                retMap.put("tmUnSettlementGte1Num", SafeConverter.toInt(offlineIndicator.getUnsettlementNum()));
                retMap.put("tmGte2Num", SafeConverter.toInt(offlineIndicator.getSettlementGte2Num()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2Num()));
                retMap.put("tmSettlementGte2Num", SafeConverter.toInt(offlineIndicator.getSettlementGte2Num()));
                retMap.put("tmUnSettlementGte2Num", SafeConverter.toInt(offlineIndicator.getUnsettlementGte2Num()));
            }
            CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(teacherId);
            if (teacherSummary != null && teacherSummary.getLatestScanTpTime() != null) {
                retMap.put("latestScanTpDate", teacherSummary.getLatestScanTpTime());
            }
        }
        return retMap;
    }

    /**
     * 根据三种场景判断是否对该老师有操作权限，若无权限判断，获取老师负责人员
     *
     * @param userId
     * @param teacherId
     * @param scene     三种场景（1：责任区域  2：范围区域  3：公私海）
     * @return
     */
    public MapMessage teacherAuthorityMessage(Long userId, Long teacherId, Integer scene) {
        String teacherManager = "";
        //判断是否对该老师有操作权限
        boolean hasTeacherPermission = searchService.hasTeacherPermission(userId, teacherId, scene);
        if (!hasTeacherPermission) {
            //获取该老师所属学校
            School school = asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchool(teacherId).getUninterruptibly();
            if (null != school) {
                //获取负责该学校的专员和代理
                teacherManager = StringUtils.join(baseOrgService.getSchoolManager(school.getId()).stream().map(AgentUser::getRealName).collect(toList()), "、");
            }
            return MapMessage.errorMessage().add("teacherManager", teacherManager);
        } else {
            return MapMessage.successMessage();
        }
    }

    /**
     * 查询潜力值老师
     *
     * @param schoolId
     * @return
     */
    public List<TeacherBasicInfo> loadPotentialTeachers(Long schoolId, int potentialType) {
        Set<Long> schoolTeacherIds = teacherLoaderClient.loadSchoolTeacherIds(schoolId);
        List<TeacherBasicInfo> teacherBasicInfos = generateTeacherBasicInfo(schoolTeacherIds, true, true, true, false);
        String valueKey;
        if (potentialType == 1) {
            valueKey = "potentialIncStuCount";
        } else {
            valueKey = "potentialBackStuCount";
        }
        String finalValueKey = valueKey;
        return teacherBasicInfos.stream().filter(p -> {
            if (p.getSubjects().size() < 1) {
                return false;
            } else {
                for (TeacherSubject subject : p.getSubjects()) {
                    if (SafeConverter.toInt(subject.getKpiData().get(finalValueKey)) > 0) {
                        return true;
                    }
                }
                return false;
            }

        }).collect(Collectors.toList());
    }

    public MapMessage getStudentUnAuthConditions(Long studentId) {
        try {
            return stuAuthConditionInEsService.loadStudentUnAuthConditions(studentId);
        } catch (Exception e) {
            logger.error("学生Id :" + studentId + " 查询认证条件异常！", e);
            return MapMessage.errorMessage();
        }

    }


    /**
     * 生成老师资源数据
     *
     * @param teacherIds
     * @return
     */
    public List<AgentOuterResourceView> generateTeacherInfo(Collection<Long> teacherIds) {
        Map<Long, List<Long>> mainSubMap = getMainSubTeachers(teacherIds);
        // 获取老师的隐藏状态
        Map<Long, Boolean> hiddenTeacherMap = isHiddenTeacher(mainSubMap.keySet());
        // 获取老师是否是真老师
        Map<Long, Boolean> realTeacherMap = isRealTeacher(mainSubMap.keySet());

        Map<Long, List<Long>> mainSubMapNew = new HashMap<>();
        //过滤掉隐藏老师与假老师
        mainSubMap.forEach((k, v) -> {
            if (!hiddenTeacherMap.get(k) && realTeacherMap.get(k)) {
                mainSubMapNew.put(k, v);
            }
        });

        Set<Long> allTeacherIds = new HashSet<>();
        mainSubMapNew.forEach((k, v) -> {
            allTeacherIds.add(k);
            allTeacherIds.addAll(v);
        });
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(allTeacherIds);

        Map<Long, School> schoolMap = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchools(mainSubMapNew.keySet()).getUninterruptibly();

        List<AgentOuterResourceView> retList = new ArrayList<>();
        for (Long teacherId : mainSubMapNew.keySet()) {
            Teacher teacher = teacherMap.get(teacherId);
            if (teacher == null) {
                continue;
            }
            AgentOuterResourceView outerResourceView = new AgentOuterResourceView();
            outerResourceView.setId(teacher.getId());
            outerResourceView.setName(teacher.fetchRealname());
            School school = schoolMap.get(teacherId);
            if (school != null) {
                outerResourceView.setOrganizationName(school.getCname());
                ExRegion region = raikouSystem.loadRegion(school.getRegionCode());
                if (region != null) {
                    outerResourceView.setProvinceName(region.getProvinceName());
                    outerResourceView.setCityName(region.getCityName());
                    outerResourceView.setCountyName(region.getCountyName());
                }
            }
            outerResourceView.setSubjectName(teacher.getSubject() != null ? teacher.getSubject().getValue() : "");
            retList.add(outerResourceView);
        }
        return retList;
    }

    /**
     * 学校老师数量统计
     *
     * @param schoolId
     * @return
     */
    public TeacherStatisticsInfo schoolTeacherStatistics(Long schoolId) {
        TeacherStatisticsInfo teacherStatisticsInfo = new TeacherStatisticsInfo();
        Set<Long> teacherIds = teacherLoaderClient.loadSchoolTeacherIds(schoolId);
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);

        Map<Long, Boolean> hiddenTeacherMap = isHiddenTeacher(teacherIds);   // 获取老师的隐藏状态
        Map<Long, Boolean> realTeacherMap = isRealTeacher(teacherIds);       // 获取老师是否是真老师

        //过滤掉隐藏老师与假老师
        List<Teacher> teacherList = teacherMap.values().stream().filter(p -> !hiddenTeacherMap.get(p.getId()) && realTeacherMap.get(p.getId())).collect(toList());

        Integer day = performanceService.lastSuccessDataDay();
        Map<Long, TeacherOnlineIndicator> teacherOnlineIndicatorMap = loadNewSchoolServiceClient.loadTeacherOnlineIndicator(teacherIds, day);

        //注册
        Set<Long> regTeaIds = new HashSet<>();
        Set<Long> regEngTeaIds = new HashSet<>();
        Set<Long> regMathTeaIds = new HashSet<>();
        Set<Long> regChiTeaIds = new HashSet<>();
        //认证
        Set<Long> authTeaIds = new HashSet<>();
        Set<Long> authEngTeaIds = new HashSet<>();
        Set<Long> authMathTeaIds = new HashSet<>();
        Set<Long> authChiTeaIds = new HashSet<>();
        //本月布置
        Set<Long> tmHwTeaIds = new HashSet<>();
        Set<Long> tmHwEngTeaIds = new HashSet<>();
        Set<Long> tmHwMathTeaIds = new HashSet<>();
        Set<Long> tmHwChiTeaIds = new HashSet<>();
        teacherList.forEach(teacher -> {
            //认证
            if (Objects.equals(teacher.getAuthenticationState(), 1)) {
                generateTeacherSubjectInfo(teacher, authTeaIds, authEngTeaIds, authMathTeaIds, authChiTeaIds);
            }
            //注册
            generateTeacherSubjectInfo(teacher, regTeaIds, regEngTeaIds, regMathTeaIds, regChiTeaIds);
            //本月布置
            TeacherOnlineIndicator teacherOnlineIndicator = teacherOnlineIndicatorMap.get(teacher.getId());
            if (teacherOnlineIndicator != null) {
                OnlineIndicator onlineIndicator = teacherOnlineIndicator.fetchMonthData();
                if (onlineIndicator != null) {
                    int tmHwSc = onlineIndicator.getTmHwSc() != null ? onlineIndicator.getTmHwSc() : 0;
                    if (tmHwSc > 0) {
                        generateTeacherSubjectInfo(teacher, tmHwTeaIds, tmHwEngTeaIds, tmHwMathTeaIds, tmHwChiTeaIds);
                    }
                }
            }
        });
        teacherStatisticsInfo.setSchoolId(schoolId);
        teacherStatisticsInfo.setRegTeaNum(regTeaIds.size());
        teacherStatisticsInfo.setRegEngTeaNum(regEngTeaIds.size());
        teacherStatisticsInfo.setRegMathTeaNum(regMathTeaIds.size());
        teacherStatisticsInfo.setRegChiTeaNum(regChiTeaIds.size());

        teacherStatisticsInfo.setAuthTeaNum(authTeaIds.size());
        teacherStatisticsInfo.setAuthEngTeaNum(authEngTeaIds.size());
        teacherStatisticsInfo.setAuthMathTeaNum(authMathTeaIds.size());
        teacherStatisticsInfo.setAuthChiTeaNum(authChiTeaIds.size());

        teacherStatisticsInfo.setTmHwTeaNum(tmHwTeaIds.size());
        teacherStatisticsInfo.setTmHwEngTeaNum(tmHwEngTeaIds.size());
        teacherStatisticsInfo.setTmHwMathTeaNum(tmHwMathTeaIds.size());
        teacherStatisticsInfo.setTmHwChiTeaNum(tmHwChiTeaIds.size());
        return teacherStatisticsInfo;
    }

    /**
     * 老师科目信息
     *
     * @param teacher
     * @param teaIds
     * @param engTeaIds
     * @param mathTeaIds
     * @param chiTeaIds
     */
    void generateTeacherSubjectInfo(Teacher teacher, Set<Long> teaIds, Set<Long> engTeaIds, Set<Long> mathTeaIds, Set<Long> chiTeaIds) {
        teaIds.add(teacher.getId());
        if (teacher.getSubject() == Subject.ENGLISH) {
            engTeaIds.add(teacher.getId());
        } else if (teacher.getSubject() == Subject.MATH) {
            mathTeaIds.add(teacher.getId());
        } else if (teacher.getSubject() == Subject.CHINESE) {
            chiTeaIds.add(teacher.getId());
        }
    }

    public MapMessage getTeacherMobile(Long teacherId) {
        String phone = sensitiveUserDataServiceClient.showUserMobile(teacherId, "agent:generateTeacherExtStateInfo", getCurrentUser().getUserName());
        return MapMessage.successMessage().add("mobile", phone);
    }
}
