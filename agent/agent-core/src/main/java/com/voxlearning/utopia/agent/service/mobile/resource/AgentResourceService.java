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

package com.voxlearning.utopia.agent.service.mobile.resource;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.PropertyUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.teacher.TeacherOnlineIndicator;
import com.voxlearning.utopia.agent.bean.resource.HomeWorkInfo;
import com.voxlearning.utopia.agent.bean.resource.HomeWorkListInfo;
import com.voxlearning.utopia.agent.constants.AgentFollowType;
import com.voxlearning.utopia.agent.dao.CrmMainSubAccountApplyDao;
import com.voxlearning.utopia.agent.dao.CrmUserFollowDao;
import com.voxlearning.utopia.agent.mapper.ClazzAlterMapper;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.view.CrmHomeworkHistory;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.data.CrmMainSubApplyStatus;
import com.voxlearning.utopia.entity.crm.CrmMainSubAccountApply;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.CrmUserFollow;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewVacationHomeworkHistory;
import com.voxlearning.utopia.service.newhomework.consumer.*;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationState;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzTeacherAlteration;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.ClazzTeacherAlterationLoaderClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherAlterationServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherSystemClazzServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.calculateDateDay;
import static java.util.stream.Collectors.toList;

/**
 * @author Yuechen.Wang
 * @since 2016-07-20
 */
@Named
public class AgentResourceService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private UserPopupServiceClient userPopupServiceClient;

    @Inject
    private CrmMainSubAccountApplyDao internalCrmMainSubAccountApplyDao;

    @Inject
    private CrmUserFollowDao crmUserFollowDao;


    @Inject
    private BaseUserService baseUserService;
    @Inject
    private AgentDictSchoolService agentDictSchoolService;
    @Inject
    private AgentResourceMapperService agentResourceMapperService;

    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private TeacherAlterationServiceClient teacherAlterationServiceClient;
    @Inject
    private TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;

    @Inject
    private NewHomeworkCrmLoaderClient newHomeworkCrmLoaderClient;
    @Inject
    private NewAccomplishmentLoaderClient newAccomplishmentLoaderClient;
    @Inject
    private MiddleSchoolHomeworkLoaderClient middleSchoolHomeworkLoader;

    @ImportService(interfaceClass = CrmSummaryService.class)
    private CrmSummaryService crmSummaryService;

    @Inject
    private ClazzTeacherAlterationLoaderClient clazzTeacherAlterationLoaderClient;

    @Inject
    private VacationHomeworkPackageLoaderClient vacationHomeworkPackageLoaderClient; // 寒假作业接口

    @Inject
    private VacationHomeworkReportLoaderClient vacationHomeworkReportLoaderClient; // 寒假作业包接口

    @Inject
    private BasicReviewHomeworkLoaderClient basicReviewHomeworkLoaderClient;  // 期末基础复习接口

    @Inject
    private EmailServiceClient emailServiceClient;


    @Inject
    private PerformanceService performanceService;
    @Inject
    private BasicReviewHomeworkReportLoaderClient basicReviewHomeworkReportLoaderClient;
    @Inject
    private TeacherResourceService teacherResourceService;

    @Inject
    private NewHomeworkLoaderClient newHomeworkLoaderClient;

    @Inject private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    // 换班详情里查看学校近一个月所有的换班记录
    private static final String QUERY_CLAZZ_ALTERATION_BY_SCHOOL = "SELECT " +
            " ID, ALTERATION_TYPE, APPLICANT_ID, RESPONDENT_ID, CLAZZ_ID, UPDATE_DATETIME, ALTERATION_STATE, CC_PROCESS_STATE" +
            " FROM VOX_CLAZZ_TEACHER_ALTERATION " +
            " WHERE DISABLED=0 AND SCHOOL_ID=:schoolId AND UPDATE_DATETIME BETWEEN :startDate AND :endDate ";

    private static final Integer HOMEWORK_DAY = 31;


    // ------------------------------------------------------------------------------------------------
    // ---------------------                     公共方法                              --------------
    //  ------------------------------------------------------------------------------------------------
    public Set<Long> loadRelatedTeachersByTeacherId(Long teacherId) {
        Map<Long, Long> subMainTeacherMap = teacherLoaderClient.loadMainTeacherIds(Collections.singletonList(teacherId));
        Long mainAccount = teacherId;
        if (MapUtils.isNotEmpty(subMainTeacherMap)) {
            Long mainAccountTemp = subMainTeacherMap.get(teacherId);
            if (mainAccountTemp != null) {
                mainAccount = mainAccountTemp;
            }
        }
        Set<Long> subAccountList = new HashSet<>();
        List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(mainAccount);
        if (CollectionUtils.isNotEmpty(subTeacherIds)) {
            subAccountList.addAll(subTeacherIds);
        }
        subAccountList.add(mainAccount);
        Map<Long, CrmTeacherSummary> teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(subAccountList);
        return subAccountList.stream()
                .filter(t -> teacherSummaryMap.get(t) == null || teacherSummaryMap.get(t).getManualFakeTeacher()).collect(Collectors.toSet());
    }


    // ------------------------------------------------------------------------------------------------
    // ---------------------                     老师资源相关                             --------------
    // ------------------------------------------------------------------------------------------------
    public MapMessage searchTeacherHwList(Long teacherId) {
        MapMessage msg = searchTeacherHwListByTeacherId(teacherId);
        if (!msg.isSuccess()) {
            return msg;
        }
        List<HomeWorkInfo> homeWorkInfoList = (List<HomeWorkInfo>) msg.get("homeWorkList");
        return MapMessage.successMessage().add("homeWorkInfoList", groupTeacherHwListByTime(homeWorkInfoList));
    }

    // 按月分组老师作业信息数据
    private List<HomeWorkListInfo> groupTeacherHwListByTime(List<HomeWorkInfo> homeWorkInfoList) {
        if (CollectionUtils.isEmpty(homeWorkInfoList)) {
            return Collections.emptyList();
        }
        Map<String, List<HomeWorkInfo>> homeWorkGroupByMonth = homeWorkInfoList.stream().collect(Collectors.groupingBy(p ->
                        DateUtils.dateToString(p.getAssignDate(), DateUtils.FORMAT_SQL_DATE),
                Collectors.toList()));
        List<HomeWorkListInfo> result = new ArrayList<>();
        homeWorkGroupByMonth.forEach((k, v) -> {
            HomeWorkListInfo info = new HomeWorkListInfo();
            info.setMonthGroupName(k);
            v.sort((p1, p2) -> p2.getAssignDate().compareTo(p1.getAssignDate()));
            info.setHwList(v);
            result.add(info);
        });
        result.sort((p1, p2) -> p2.getMonthGroupName().compareTo(p1.getMonthGroupName()));
        return result;
    }

    // 查询老师的日常作业信息
    private MapMessage searchTeacherHwListByTeacherId(Long teacherId) {
        if (teacherId == null || teacherId == 0L) {
            return MapMessage.errorMessage("老师ID为空或不存在");
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(SafeConverter.toLong(teacherId));
        if (teacher == null) {
            return MapMessage.errorMessage("老师信息不存在");
        }

        //实时获取老师班级情况
        List<GroupTeacherMapper> groupTeacherList = new ArrayList<>();
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(Collections.singleton(teacher.getId()), true);
        teacherGroups.forEach((tid, groupList) -> groupList.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(tid)) { // 过滤出有效的组
                // 分组id
                groupTeacherList.add(group);
            }
        }));

        Map<Long, GroupTeacherMapper> groups = groupTeacherList.stream().collect(Collectors.toMap(GroupTeacherMapper::getId, Function.identity()));

        Map<Long, Clazz> clazzs = raikouSystem.loadClazzes(groupTeacherList.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toSet()));
        // 过滤掉毕业班
        clazzs = clazzs.values().stream()
                .filter(p -> !p.isDisabledTrue() && p.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && p.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED && p.getClazzLevel() != ClazzLevel.INFANT_GRADUATED)
                .collect(Collectors.toMap(Clazz::getId, Function.identity(), (o1, o2) -> o1));
        Map<Long, Clazz> finalClazzs = clazzs;
        List<HomeWorkInfo> result = new ArrayList<>();
        if (teacher.getKtwelve() == Ktwelve.JUNIOR_SCHOOL) {
            if (teacher.getSubject() == Subject.MATH) {//中学数学作业时
                StringBuffer url = new StringBuffer(getMiddleMATHSchoolBaseURL());
                url.append("/api/3/teacher_paper/get_group_paper_list");
                Map<Object, Object> homeworkMap = new HashMap<>();
                homeworkMap.put("groupIds", groupTeacherList.stream().filter(p -> finalClazzs.keySet().contains(p.getClazzId())).map(GroupTeacherMapper::getId).collect(Collectors.toList()));
                homeworkMap.put("page", 1);
                homeworkMap.put("size", 50);
                Date now = new Date();
                homeworkMap.put("startTime", DateUtils.calculateDateDay(now, -HOMEWORK_DAY).getTime());
                homeworkMap.put("endTime", now.getTime());
                homeworkMap.put("teacherId", teacherId);
                try {
                    AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url.toString()).json(JsonUtils.toJson(homeworkMap)).execute();
                    if (response.getStatusCode() == 200) {
                        Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
                        if (MapUtils.isNotEmpty(resultMap)) {
                            Integer code = SafeConverter.toInt(resultMap.get("code"));
                            if (code == 0) {
                                List<List<Map<String, Object>>> listMap = (List<List<Map<String, Object>>>) resultMap.get("data");
                                listMap.forEach(p -> {
                                    List<Map<String, Object>> list = p.stream().filter(item -> SafeConverter.toInt(item.get("type")) != 4).collect(toList());
                                    list.forEach(g -> {
                                        HomeWorkInfo homeWorkInfo = getHomeWorkInfo(2, DateUtils.stringToDate((String) g.get("create_time"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
                                                SafeConverter.toInt(g.get("status")) == 1, SafeConverter.toInt(g.get("group_count")), SafeConverter.toInt(g.get("doCount")),
                                                SafeConverter.toInt(g.get("finish_upload_auto_corrects")) == 1, groups.get(SafeConverter.toLong(g.get("group_id"))), finalClazzs);
                                        homeWorkInfo.setTypes(Collections.emptyList());
                                        homeWorkInfo.setSpecifiedType(true);
                                        result.add(homeWorkInfo);
                                    });
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("http request error :  url= " + url.toString(), e);
                    emailServiceClient.createPlainEmail()
                            .body("http request error :  url= " + url + "\r\n参数：" + JsonUtils.toJson(homeworkMap))
                            .subject("中学数学作业接口调用失败【" + RuntimeMode.current().getStageMode() + "】")
                            .to("xianlong.zhang@17zuoye.com")
                            .send();
                }
            } else {//中学英语作业时
                List<GroupMapper> groupMapperList = new ArrayList<>(groupTeacherList);
                if (CollectionUtils.isEmpty(groupMapperList)) {
                    return MapMessage.errorMessage("该老师无班级信息");
                }
                juniorSchoolHomeWork(result, groupTeacherList, clazzs);
                /*List<MiddleSchoolHomeworkCrmHistory> midSchoolHwHistories = middleSchoolHomeworkLoader.loadGroupRecentHomeworkList(groupMapperList, HOMEWORK_DAY);
                if (CollectionUtils.isEmpty(midSchoolHwHistories)) {
                    return MapMessage.errorMessage("未找到作业信息");
                }
                midSchoolHwHistories.forEach(p -> {
                    //中学英语作业没有老师检查状态默认false
                    HomeWorkInfo homeWorkInfo =getHomeWorkInfo(2,p.getStartTime(),p.getDisabled(),SafeConverter.toInt(p.getStudentCount()),SafeConverter.toInt(p.getFinishedCount()),
                            false,p.getGroup(),clazzs);
                    result.add(homeWorkInfo);
                });*/
            }

        } else if (teacher.getKtwelve() == Ktwelve.PRIMARY_SCHOOL) {
            // 分页 每页200个
            PageRequest pageable = new PageRequest(0, 200);
            Page<NewHomework.Location> page = newHomeworkCrmLoaderClient.loadGroupNewHomeworks(groups.keySet(),
                    DateUtils.getDayStart(DateUtils.nextDay(new Date(), -1 * HOMEWORK_DAY)), new Date(), pageable, true);
            if (page == null || !page.hasContent()) {
                return MapMessage.errorMessage("未找到作业信息");
            }
            List<String> homeworkIds = page.getContent().stream().map(p -> p.getId()).collect(Collectors.toList());
            //作业内容
            Map<String, NewHomework> newHomeworkMap = newHomeworkLoaderClient.loads(homeworkIds);
            List<String> newAccIds = page.getContent().stream().map(p -> NewAccomplishment.ID.build(p.getCreateTime(), p.getSubject(), p.getId()).toString()).collect(Collectors.toList());
            //作业结果
            Map<String, NewAccomplishment> newAccomplishmentMap = newAccomplishmentLoaderClient.loadNewAccomplishments(newAccIds);
            page.getContent().forEach(v -> {
                if (v.isDisabled()) {
                    return;
                }
                GroupTeacherMapper group = groups.get(v.getClazzGroupId());
                HomeWorkInfo homeWorkInfo = getHomeWorkInfo(1, new Date(v.getCreateTime()), v.isDisabled(), 0, 0,
                        v.isChecked(), group, finalClazzs);
                List<GroupMapper.GroupUser> students = new ArrayList<>();
                if (group != null) {
                    students = group.getStudents();
                    homeWorkInfo.setSumClassStuNum(students.size());
                }

                if (!Objects.equals(teacherId, v.getTeacherId())) {
                    homeWorkInfo.setOldTeacherId(v.getTeacherId());
                    Teacher homeworkTeacher = teacherLoaderClient.loadTeacher(v.getTeacherId());
                    homeWorkInfo.setOldTeacherName(homeworkTeacher.getProfile().getRealname());
                }
                String accId = NewAccomplishment.ID.build(v.getCreateTime(), v.getSubject(), v.getId()).toString();
//                NewAccomplishment acc = newAccomplishmentLoaderClient.loadNewAccomplishment(v);
                NewAccomplishment acc = newAccomplishmentMap.get(accId);
                Set<Long> studentIds = students.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
                if (acc != null && acc.size() > 0) {
                    int completeCount = 0;
                    for (String studentId : acc.getDetails().keySet()) {
                        if (studentIds.contains(SafeConverter.toLong(studentId))) {
                            completeCount++;
                        }
                    }
                    homeWorkInfo.setFinishHwStuNum(completeCount);
                }
                //取出作业类别list
                NewHomework homework = newHomeworkMap.get(v.getId());
                if (homework != null) {
                    List<ObjectiveConfigType> typeList = new ArrayList<>();
                    homework.getPractices().forEach(c -> typeList.add(c.getType()));
                    List<String> types = new ArrayList<>();
                    boolean specifiedType = false;
                    if (typeList.size() > 0) {
                        for (ObjectiveConfigType t : typeList) {
                            types.add(t.getValue());
                            if (homework.getNewHomeworkType() == NewHomeworkType.Normal) {
                                if (specifiedTypeNormalList().contains(t)) {
                                    specifiedType = true;
                                }
                            } else if (homework.getNewHomeworkType() == NewHomeworkType.TermReview) {
                                if (specifiedTypeTermReviewList().contains(t)) {
                                    specifiedType = true;
                                }
                            }
                        }
                    }
                    homeWorkInfo.setSpecifiedType(specifiedType);
                    homeWorkInfo.setTypes(types);
                }

                result.add(homeWorkInfo);
            });
        }
        return MapMessage.successMessage().add("homeWorkList", result);
    }

    public MapMessage getTeacherHwListByTeacherId(Long teacherId) {
        return searchTeacherHwListByTeacherId(teacherId);
    }

    private HomeWorkInfo getHomeWorkInfo(Integer schoolLevel, Date assignDate, Boolean isDisabled, Integer classStuNum, Integer finishHwStuNum, Boolean hwCheckStatus, GroupMapper group, Map<Long, Clazz> clazzs) {
        HomeWorkInfo homeWorkInfo = new HomeWorkInfo();
        homeWorkInfo.setSchoolLevel(schoolLevel);
        homeWorkInfo.setAssignDate(assignDate);
        homeWorkInfo.setDisabled(isDisabled);
        homeWorkInfo.setSumClassStuNum(classStuNum);
        homeWorkInfo.setFinishHwStuNum(finishHwStuNum);
        if (group != null) {
            Clazz clazz = clazzs.get(group.getClazzId());
            if (clazz != null) {
                homeWorkInfo.setClazzName(clazz.formalizeClazzName());
                homeWorkInfo.setClazzId(clazz.getId());
            }
            homeWorkInfo.setGroupId(group.getId());
        }
        homeWorkInfo.setHwCheckStatus(hwCheckStatus);
        return homeWorkInfo;
    }

    //中学英语作业
    private MapMessage juniorSchoolHomeWork(List<HomeWorkInfo> result, List<GroupTeacherMapper> groupTeacherList, Map<Long, Clazz> clazzs) {
        List<GroupMapper> groupMapperList = new ArrayList<>(groupTeacherList);
        if (CollectionUtils.isEmpty(groupMapperList)) {
            return MapMessage.errorMessage("该老师无班级信息");
        }
        Map<Long, GroupTeacherMapper> groups = groupTeacherList.stream().collect(Collectors.toMap(GroupTeacherMapper::getId, Function.identity()));
        StringBuffer url = new StringBuffer(getMiddleENSchoolBaseURL());
        url.append("/rpc/crm/loadGroupRecentHomeworkList");
        Map<Object, Object> homeworkMap = new HashMap<>();
        homeworkMap.put("groupIds", groupTeacherList.stream().map(GroupTeacherMapper::getId).collect(Collectors.toList()));
        homeworkMap.put("day", HOMEWORK_DAY);
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url.toString()).addParameter(homeworkMap).execute();
            if (response.getStatusCode() == 200) {
                Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
                if (MapUtils.isNotEmpty(resultMap)) {
                    Integer code = SafeConverter.toInt(resultMap.get("error_code"));
                    if (code == 0) {
                        List<Map<String, Object>> listMap = (List<Map<String, Object>>) resultMap.get("data");
                        listMap.forEach(p -> {
                            Long groupId = SafeConverter.toLong(p.get("group"));
                            HomeWorkInfo homeWorkInfo = getHomeWorkInfo(2, new Date(SafeConverter.toLong(p.get("create_time"))), SafeConverter.toBoolean(p.get("is_delete")),
                                    SafeConverter.toInt(p.get("student_count")), SafeConverter.toInt(p.get("finished_count")),
                                    false, groups.get(groupId), clazzs);
                            homeWorkInfo.setSpecifiedType(true);
                            homeWorkInfo.setTypes((List<String>) p.get("practice_types"));
                            result.add(homeWorkInfo);
                        });
                    }
                }
            }
        } catch (Exception e) {
            logger.error("http request error :  url= " + url.toString(), e);
            emailServiceClient.createPlainEmail()
                    .body("http request error :  url= " + url + "\r\n参数：" + JsonUtils.toJson(homeworkMap))
                    .subject("中学数学作业接口调用失败【" + RuntimeMode.current().getStageMode() + "】")
                    .to("xianlong.zhang@17zuoye.com")
                    .send();
        }
//        List<MiddleSchoolHomeworkCrmHistory> midSchoolHwHistories = middleSchoolHomeworkLoader.loadGroupRecentHomeworkList(groupMapperList, HOMEWORK_DAY);
//        if (CollectionUtils.isEmpty(midSchoolHwHistories)) {
//            return MapMessage.errorMessage("未找到作业信息");
//        }
//        midSchoolHwHistories.forEach(p -> {
//            HomeWorkInfo homeWorkInfo = new HomeWorkInfo();
//            homeWorkInfo.setSchoolLevel(2);
//            homeWorkInfo.setAssignDate(p.getStartTime());
//            homeWorkInfo.setDisabled(p.getDisabled());
//            homeWorkInfo.setSumClassStuNum(SafeConverter.toInt(p.getStudentCount()));
//            homeWorkInfo.setFinishHwStuNum(SafeConverter.toInt(p.getFinishedCount()));
//            GroupMapper group = p.getGroup();
//            if (group != null) {
//                Clazz clazz = clazzs.get(group.getClazzId());
//                if (clazz != null) {
//                    homeWorkInfo.setClazzName(clazz.formalizeClazzName());
//                    homeWorkInfo.setClazzId(clazz.getId());
//                }
//            }
//            result.add(homeWorkInfo);
//        });
        return MapMessage.successMessage().add("homeWorkList", result);
    }

    //小学作业
    private MapMessage primarySchoolHomeWork(List<HomeWorkInfo> result, Map<Long, Clazz> clazzs, Map<Long, GroupTeacherMapper> groups) {

        // 分页 每页200个
        PageRequest pageable = new PageRequest(0, 200);
        Page<NewHomework.Location> page = newHomeworkCrmLoaderClient.loadGroupNewHomeworks(groups.keySet(),
                DateUtils.getDayStart(DateUtils.nextDay(new Date(), -1 * HOMEWORK_DAY)), new Date(), pageable, true);
        if (page == null || !page.hasContent()) {
            return MapMessage.errorMessage("未找到作业信息");
        }
        List<String> homeworkIds = page.getContent().stream().map(p -> p.getId()).collect(Collectors.toList());
        //作业内容
        Map<String, NewHomework> newHomeworkMap = newHomeworkLoaderClient.loads(homeworkIds);
        List<String> newAccIds = page.getContent().stream().map(p -> NewAccomplishment.ID.build(p.getCreateTime(), p.getSubject(), p.getId()).toString()).collect(Collectors.toList());
        //作业结果
        Map<String, NewAccomplishment> newAccomplishmentMap = newAccomplishmentLoaderClient.loadNewAccomplishments(newAccIds);
        page.getContent().forEach(v -> {
            HomeWorkInfo homeWorkInfo = new HomeWorkInfo();
            homeWorkInfo.setSchoolLevel(1);
            homeWorkInfo.setDisabled(v.isDisabled());
            homeWorkInfo.setAssignDate(new Date(v.getCreateTime()));
            homeWorkInfo.setHwCheckStatus(v.isChecked());
            GroupTeacherMapper group = groups.get(v.getClazzGroupId());
            List<GroupMapper.GroupUser> students = new ArrayList<>();
            if (group != null) {
                students = group.getStudents();
                homeWorkInfo.setSumClassStuNum(students.size());
                Clazz clazz = clazzs.get(group.getClazzId());
                if (clazz != null) {
                    homeWorkInfo.setClazzName(clazz.formalizeClazzName());
                    homeWorkInfo.setClazzId(clazz.getId());
                }
            }
           /* if (!Objects.equals(teacherId, v.getTeacherId())) {
                homeWorkInfo.setOldTeacherId(v.getTeacherId());
                Teacher homeworkTeacher = teacherLoaderClient.loadTeacher(v.getTeacherId());
                homeWorkInfo.setOldTeacherName(homeworkTeacher.getProfile().getRealname());
            }*/
            String accId = NewAccomplishment.ID.build(v.getCreateTime(), v.getSubject(), v.getId()).toString();
//                NewAccomplishment acc = newAccomplishmentLoaderClient.loadNewAccomplishment(v);
            NewAccomplishment acc = newAccomplishmentMap.get(accId);
            Set<Long> studentIds = students.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
            if (acc != null && acc.size() > 0) {
                int completeCount = 0;
                for (String studentId : acc.getDetails().keySet()) {
                    if (studentIds.contains(SafeConverter.toLong(studentId))) {
                        completeCount++;
                    }
                }
                homeWorkInfo.setFinishHwStuNum(completeCount);
            }
            //取出作业类别list
            NewHomework homework = newHomeworkMap.get(v.getId());
            if (homework != null) {
                List<ObjectiveConfigType> typeList = new ArrayList<>();
                homework.getPractices().forEach(c -> typeList.add(c.getType()));
                List<String> types = new ArrayList<>();
                boolean specifiedType = false;
                if (typeList.size() > 0) {
                    for (ObjectiveConfigType t : typeList) {
                        types.add(t.getValue());
                        if (homework.getNewHomeworkType() == NewHomeworkType.Normal) {
                            if (specifiedTypeNormalList().contains(t)) {
                                specifiedType = true;
                            }
                        } else if (homework.getNewHomeworkType() == NewHomeworkType.TermReview) {
                            if (specifiedTypeTermReviewList().contains(t)) {
                                specifiedType = true;
                            }
                        }
                    }
                }
                homeWorkInfo.setSpecifiedType(specifiedType);
                homeWorkInfo.setTypes(types);
            }

            result.add(homeWorkInfo);
        });
        return MapMessage.successMessage().add("homeWorkList", result);
    }

    public List<ObjectiveConfigType> specifiedTypeNormalList() {
        return Arrays.asList(ObjectiveConfigType.BASIC_APP, ObjectiveConfigType.MENTAL, ObjectiveConfigType.UNIT_QUIZ, ObjectiveConfigType.READ_RECITE, ObjectiveConfigType.KEY_POINTS,
                ObjectiveConfigType.BASIC_KNOWLEDGE, ObjectiveConfigType.CHINESE_READING, ObjectiveConfigType.INTELLIGENCE_EXAM, ObjectiveConfigType.NEW_READ_RECITE, ObjectiveConfigType.NATURAL_SPELLING,
                ObjectiveConfigType.MENTAL_ARITHMETIC, ObjectiveConfigType.READ_RECITE_WITH_SCORE, ObjectiveConfigType.INTELLIGENT_TEACHING, ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING, ObjectiveConfigType.WORD_RECOGNITION_AND_READING);
    }

    public List<ObjectiveConfigType> specifiedTypeTermReviewList() {
        return Arrays.asList(ObjectiveConfigType.BASIC_APP, ObjectiveConfigType.EXAM, ObjectiveConfigType.LISTEN_PRACTICE, ObjectiveConfigType.MENTAL, ObjectiveConfigType.UNIT_QUIZ,
                ObjectiveConfigType.READ_RECITE, ObjectiveConfigType.KEY_POINTS, ObjectiveConfigType.BASIC_KNOWLEDGE, ObjectiveConfigType.CHINESE_READING, ObjectiveConfigType.INTELLIGENCE_EXAM,
                ObjectiveConfigType.NEW_READ_RECITE, ObjectiveConfigType.NATURAL_SPELLING, ObjectiveConfigType.MENTAL_ARITHMETIC, ObjectiveConfigType.READ_RECITE_WITH_SCORE, ObjectiveConfigType.INTELLIGENT_TEACHING,
                ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING, ObjectiveConfigType.WORD_RECOGNITION_AND_READING);
    }
    // ------------------------------------------------------------------------------------------------
    // ---------------------                    用户关注相关                              --------------
    // ------------------------------------------------------------------------------------------------

    public MapMessage followOrUnfollowSchool(Long userId, Long schoolId, boolean followed) {
        if (userId == null || userId == 0L || schoolId == null || schoolId == 0L) {
            return MapMessage.errorMessage("参数异常");
        }
        AgentUser user = baseUserService.getUser(userId);
        if (user == null || !user.isValidUser()) {
            return MapMessage.errorMessage("无效的用户信息");
        }
        try {
            CrmUserFollow follow = crmUserFollowDao.findByFollowerAndTarget(userId, schoolId, AgentFollowType.SCHOOL.name());
            if (follow == null) {
                follow = new CrmUserFollow();
                follow.setFollowerId(userId);
                follow.setFollowerName(user.getRealName());
                follow.setTarget(schoolId.toString());
                follow.setFollowType(AgentFollowType.SCHOOL.name());
                follow.setIsFollowed(followed);
                follow.setDisabled(false);
                crmUserFollowDao.upsert(follow);
            } else {
                follow.setIsFollowed(followed);
                crmUserFollowDao.replace(follow);
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed process follow school, user={}, school={}, followed={}, ex={}", userId, schoolId, followed, ex.getMessage(), ex);
            return MapMessage.errorMessage("关注/取消关注更新失败: {}", ex.getMessage());
        }
    }

    // ------------------------------------------------------------------------------------------------
    // ---------------------                    换班处理相关                              --------------
    // ------------------------------------------------------------------------------------------------
    public long countPendingClazzAlterationBySchool(Collection<Long> schoolIds, Integer startDay, Integer endDay) {
        Date startDate = calculateDateDay(DayRange.current().getStartDate(), -startDay);
        Date endDate = calculateDateDay(DayRange.current().getEndDate(), -endDay);
        Set<ClazzTeacherAlterationType> alterationTypes = new HashSet<>();
        alterationTypes.add(ClazzTeacherAlterationType.TRANSFER);
        alterationTypes.add(ClazzTeacherAlterationType.REPLACE);
        alterationTypes.add(ClazzTeacherAlterationType.LINK);
        long result = batchIds(schoolIds, 500).values().stream().
                mapToLong(p -> clazzTeacherAlterationLoaderClient.loadClazzTeacherAlterationCount(p, startDate, endDate, alterationTypes, ClazzTeacherAlterationState.PENDING)).sum();
        return result;
    }


    /**
     * 将ID进行分组
     *
     * @param ids       待分组的ID
     * @param batchSize 每组数量
     * @return
     */
    public static <T> Map<Integer, Set<T>> batchIds(Collection<T> ids, int batchSize) {
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        List<T> tempIds = ids.stream().collect(Collectors.toList());
        //不使用lamda原因是速度相差百倍
        //Map<Integer, Set<Long>> integerSetMap = tempSchoolIds.stream().collect(Collectors.groupingBy(p -> tempSchoolIds.indexOf(p) / batchSize, Collectors.toSet()));
        Map<Integer, Set<T>> integerSetMap = new HashMap<>();
        for (int i = 0; i < tempIds.size(); i++) {
            int key = i / batchSize;
            if (!integerSetMap.containsKey(key)) {
                integerSetMap.put(key, new HashSet<>());
            }
            Set<T> temp = integerSetMap.get(key);
            temp.add(tempIds.get(i));
            integerSetMap.put(key, temp);

        }
        return integerSetMap;
    }

    /**
     * 获取当前学校下所有的换班记录
     */
    public List<ClazzAlterMapper> getClazzAlterationBySchool(Collection<Long> schoolIds, Integer startDay, Integer endDay) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        Date startDate = calculateDateDay(DayRange.current().getStartDate(), -startDay);
        Date endDate = calculateDateDay(DayRange.current().getEndDate(), -endDay);
        Set<ClazzTeacherAlterationType> alterationTypes = new HashSet<>();
        alterationTypes.add(ClazzTeacherAlterationType.TRANSFER);
        alterationTypes.add(ClazzTeacherAlterationType.REPLACE);
        alterationTypes.add(ClazzTeacherAlterationType.LINK);
        Set<ClazzTeacherAlterationState> alterationStates = new HashSet<>();
        alterationStates.add(ClazzTeacherAlterationState.PENDING);
        alterationStates.add(ClazzTeacherAlterationState.SUCCESS);
        alterationStates.add(ClazzTeacherAlterationState.REJECT);

        List<ClazzTeacherAlteration> clazzTeacherAlterationLists = new ArrayList<>();
        batchIds(schoolIds, 500).forEach((k, v) -> {
            List<ClazzTeacherAlteration> clazzTeacherAlterationTemps = clazzTeacherAlterationLoaderClient.loadClazzTeacherAlterations(v, startDate, endDate, alterationTypes, alterationStates);
            if (CollectionUtils.isNotEmpty(clazzTeacherAlterationTemps)) {
                clazzTeacherAlterationLists.addAll(clazzTeacherAlterationTemps);
            }
        });


        List<ClazzAlterMapper> alterations = clazzTeacherAlterationLists
                .stream()
                .map(this::clazzTeacherAlterationMap).collect(toList());
        if (CollectionUtils.isEmpty(alterations)) {
            return Collections.emptyList();
        }
        Set<Long> teacherIdSet = new HashSet<>(); // 老师Id集合
        alterations.forEach(record -> {
            teacherIdSet.add(SafeConverter.toLong(record.getApplicantId()));
            teacherIdSet.add(SafeConverter.toLong(record.getRespondentId()));
        });
        Map<Long, CrmTeacherSummary> teacherSummaryMap = new HashMap<>();
        batchIds(teacherIdSet, 500).forEach((k, v) -> {
            Map<Long, CrmTeacherSummary> teacherSummaryMapTemp = crmSummaryLoaderClient.loadTeacherSummary(v);
            if (MapUtils.isNotEmpty(teacherSummaryMapTemp)) {
                teacherSummaryMapTemp.forEach((k1, v1) -> {
                    if (v1.getRealName() != null) {
                        teacherSummaryMap.put(k1, v1);
                    }
                });
            }
        });
        return agentResourceMapperService.mapClazzAlteration(alterations, teacherSummaryMap);

    }

   /* Map<Long, Teacher> teacherMap = new HashMap<>();
    batchIds(teacherIdSet,2000).forEach((k,v) -> {
        Map<Long, Teacher> teacherMapTemp = teacherLoaderClient.loadTeachers(v);
        teacherMap.putAll(teacherMapTemp);
    });*/

    public MapMessage approveApplication(Long userId, Long recordId, Long respondent, ClazzTeacherAlterationType alterationType) {
        if (userId == null || respondent == null || recordId == null || alterationType == null) {
            return MapMessage.errorMessage("参数异常！");
        }
        try {
            AgentUser user = baseUserService.getUser(userId);
            if (user == null || !user.isValidUser()) {
                return MapMessage.errorMessage("无效的用户信息");
            }
            // 操作之前先看一下是否已经被处理了
            ClazzTeacherAlteration alteration = teacherLoaderClient.loadClazzTeacherAlteration(recordId);
            if (alteration == null) {
                logger.error("Unknown clazz alteration id:{} received!", recordId);
                return MapMessage.errorMessage("无效的换班申请:" + recordId);
            }
            if (alteration.isDisabledTrue() || alteration.getState() != ClazzTeacherAlterationState.PENDING) {
                return MapMessage.errorMessage("该换班请求已经处理完毕，请勿重复操作");
            }
            Long applicantId = alteration.getApplicantId();
            Teacher applicant = teacherLoaderClient.loadTeacher(applicantId);
            if (applicant == null) {
                return MapMessage.errorMessage("老师不存在");
            }
            // 判断是否是假老师
            if (teacherLoaderClient.isFakeTeacher(applicantId)) {
                return MapMessage.errorMessage("申请老师为假老师，请先将老师取消判假后再处理");
            }
            MapMessage message = teacherAlterationServiceClient.approveApplication(respondent, recordId, alterationType, OperationSourceType.marketing);
            // 成功之后发送通知消息 以及 保存用户记录
            if (message.isSuccess()) {
                sendTeacherNotify("同意", alterationType, message);
                saveAlterationUserRecord(user, message, "同意");
            }
            return message;
        } catch (Exception ex) {
            logger.error("Failed approve clazz alteration, id={}, tid={}, type={}", recordId, respondent, alterationType, ex);
            return MapMessage.errorMessage("同意换班请求失败:" + ex.getMessage());
        }
    }

    public MapMessage rejectApplication(Long userId, Long recordId, Long respondent, ClazzTeacherAlterationType alterationType) {
        if (userId == null || respondent == null || recordId == null || alterationType == null) {
            return MapMessage.errorMessage("参数异常！");
        }
        try {
            AgentUser user = baseUserService.getUser(userId);
            if (user == null || !user.isValidUser()) {
                return MapMessage.errorMessage("无效的用户信息");
            }
            // 操作之前先看一下是否已经被处理了
            ClazzTeacherAlteration alteration = teacherLoaderClient.loadClazzTeacherAlteration(recordId);
            if (alteration == null) {
                logger.error("Unknown clazz alteration id:{} received!", recordId);
                return MapMessage.errorMessage("无效的换班申请:" + recordId);
            }
            if (alteration.isDisabledTrue() || alteration.getState() != ClazzTeacherAlterationState.PENDING) {
                return MapMessage.errorMessage("该换班请求已经处理完毕，请勿重复操作");
            }
            Long applicantId = alteration.getApplicantId();
            Teacher applicant = teacherLoaderClient.loadTeacher(applicantId);
            if (applicant == null) {
                return MapMessage.errorMessage("老师不存在");
            }
            // 判断是否是假老师
            if (teacherLoaderClient.isFakeTeacher(applicantId)) {
                return MapMessage.errorMessage("申请老师为假老师，请先将老师取消判假后再处理");
            }
            MapMessage message = teacherAlterationServiceClient.rejectApplication(respondent, recordId, alterationType, OperationSourceType.marketing);
            // 成功之后发送通知消息 以及 保存用户记录
            if (message.isSuccess()) {
                sendTeacherNotify("拒绝", alterationType, message);
                saveAlterationUserRecord(user, message, "拒绝");
            }
            return message;
        } catch (Exception ex) {
            logger.error("Failed reject clazz alteration, id={}, tid={}, type={}", recordId, respondent, alterationType, ex);
            return MapMessage.errorMessage("拒绝换班请求失败:" + ex.getMessage());
        }
    }

    private void saveAlterationUserRecord(AgentUser user, MapMessage message, String op) {
        try {
            Teacher applicant = (Teacher) message.get("applicant");
            Teacher respondent = (Teacher) message.get("respondent");
            Clazz clazz = (Clazz) message.get("clazz");
            UserServiceRecord applicantRecord = UserServiceRecord.newInstance(
                    applicant.getId(), applicant.fetchRealname(),
                    user.getId().toString(), user.getRealName(),
                    UserServiceRecordOperationType.老师换班.name(), StringUtils.formatMessage("{}换班申请", op),
                    StringUtils.formatMessage("市场{}操作请求,{}换班申请,申请人ID:{},被申请人ID:{},操作班级ID:{}", user.getRealName(), op, applicant.getId(), respondent.getId(), clazz == null ? "" : clazz.getId()), null
            );
            userServiceClient.saveUserServiceRecord(applicantRecord);
            UserServiceRecord respondentRecord = UserServiceRecord.newInstance(
                    respondent.getId(), respondent.fetchRealname(),
                    user.getId().toString(), user.getRealName(),
                    UserServiceRecordOperationType.老师换班.name(), StringUtils.formatMessage("{}换班申请", op),
                    StringUtils.formatMessage("市场{}操作请求,{}换班申请,申请人ID:{},被申请人ID:{},操作班级ID:{}", user.getRealName(), op, applicant.getId(), respondent.getId(), clazz == null ? "" : clazz.getId()), null
            );
            userServiceClient.saveUserServiceRecord(respondentRecord);
        } catch (Exception ex) {
            logger.error("Failed save user record of clazz_alteration, reason:{}", ex.getMessage(), ex);
        }
    }

    private void sendTeacherNotify(String op, ClazzTeacherAlterationType type, MapMessage message) {
        Teacher applicant = (Teacher) message.get("applicant");
        Teacher respondent = (Teacher) message.get("respondent");
        Clazz clazz = (Clazz) message.get("clazz");
        String content = "";
        if (type != ClazzTeacherAlterationType.TRANSFER && type != ClazzTeacherAlterationType.REPLACE) {
            return;
        }
        if (null != respondent && null != respondent.getProfile() && null != clazz) {
            if (type == ClazzTeacherAlterationType.TRANSFER) {
                content = StringUtils.formatMessage("{}老师{}了您转让{}的申请。",
                        respondent.getProfile().getRealname(), op, clazz.formalizeClazzName());
            } else {
                content = StringUtils.formatMessage("{}老师{}了您接管{}的申请。",
                        respondent.getProfile().getRealname(), op, clazz.formalizeClazzName());
            }
        }
        userPopupServiceClient.createPopup(applicant.getId())
                .content(content)
                .type(PopupType.TEACHER_ALTERATION_FOR_RESPONDENT)
                .category(PopupCategory.LOWER_RIGHT)
                .create();
    }

    // ------------------------------------------------------------------------------------------------
    // ---------------------                    包班申请相关                              --------------
    // ------------------------------------------------------------------------------------------------
    public MapMessage applyMainSubAccount(Long userId, Long teacherId, Long clazzId, Subject subject) {
        if (userId == null || teacherId == null || clazzId == null || subject == null) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            // 稍微严格校验每项输入
            AgentUser user = baseUserService.getUser(userId);
            if (user == null || !user.isValidUser()) {
                return MapMessage.errorMessage("没有申请包班的权限");
            }
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacher == null || teacher.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的老师信息：ID=" + teacherId);
            }

            // 中学不支持包班制
            if (!teacher.isPrimarySchool()) {
                return MapMessage.errorMessage("只有小学老师才可以开通包班");
            }

            if (subject.equals(teacher.getSubject())) {
                return MapMessage.errorMessage("老师已经执教了该班的" + subject.getValue());
            }
            Clazz clazz = raikouSystem.loadClazz(clazzId);
            if (clazz == null || clazz.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的班级信息：ID=" + clazzId);
            }
            CrmMainSubAccountApply apply = CrmMainSubAccountApply.newInstance();
            apply.setApplicantId(user.getId());
            apply.setApplicantName(user.getRealName());
            apply.setTeacherId(teacher.getId());
            apply.setTeacherName(teacher.getProfile().getRealname());
            apply.setSchoolId(teacher.getTeacherSchoolId());
            apply.setSchoolName(teacher.getTeacherSchoolName());
            apply.setCurrentSubject(teacher.getSubject());
            apply.setApplySubject(subject);
            apply.setClazzId(clazz.getId());
            apply.setClazzName(clazz.formalizeClazzName());
            apply = internalCrmMainSubAccountApplyDao.upsert(apply);
            MapMessage result = this.autoApproveMainSubApply(user, apply);
            //自动审核通过
            if (result.isSuccess()) {
                apply.setAuditStatus(CrmMainSubApplyStatus.APPROVED);
                apply.setAuditor(userId);
                apply.setAuditorName(user.getRealName());
                apply.setAuditNote("同意");
                apply.setAuditTime(new Date());
                internalCrmMainSubAccountApplyDao.replace(apply);
            }
            return result;
        } catch (Exception ex) {
            logger.error("Failed to applyMainSubAccount，teacherId={}, user={}", teacherId, userId, ex);
            return MapMessage.errorMessage("开通包班失败，原因：" + ex.getMessage());
        }
    }

    public List<CrmMainSubAccountApply> getUserApplyRecord(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        Map<Long, List<CrmMainSubAccountApply>> map = internalCrmMainSubAccountApplyDao.findByApplicant(userIds);
        return map.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public List<CrmMainSubAccountApply> getTeacherApplyRecord(Long teacherId) {
        if (teacherId == null || teacherId == 0L) {
            return Collections.emptyList();
        }
        return internalCrmMainSubAccountApplyDao.findByTeacherId(teacherId);
    }

    public MapMessage autoApproveMainSubApply(AgentUser user, CrmMainSubAccountApply apply) {
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(apply.getTeacherId());
            if (teacher == null || teacher.isDisabledTrue()) {
                return MapMessage.errorMessage("无效的老师信息：ID=" + apply.getTeacherId());
            }
            // 中学不支持包班制
            if (!teacher.isPrimarySchool()) {
                return MapMessage.errorMessage("只有小学老师才可以开通包班制");
            }
            MapMessage message = teacherSystemClazzServiceClient.createSubTeacherForTeacherAndClazz(
                    apply.getTeacherId(),
                    apply.getClazzId(),
                    apply.getApplySubject(),
                    OperationSourceType.marketing
            );
            if (message.isSuccess()) {
                // FIXME 记得接口实现方法里需要保存一条用户备注，这里使用用户记录实现不知是否满足要求
                saveClazzApplyUserRecord(user, apply);
            }
            return message;
        } catch (Exception ex) {
            logger.error("Failed to autoApproveMainSubApply，teacherId={}, user={}", apply.getTeacherId(), apply.getApplicantId(), ex);
            return MapMessage.errorMessage("包班开通失败:" + ex.getMessage());
        }
    }

    private void saveClazzApplyUserRecord(AgentUser user, CrmMainSubAccountApply apply) {
        try {
            UserServiceRecord applicantRecord = UserServiceRecord.newInstance(
                    apply.getTeacherId(), apply.getTeacherName(),
                    user.getId().toString(), user.getRealName(),
                    UserServiceRecordOperationType.开通包班.name(),
                    StringUtils.formatMessage("班级:{}({}),开通{}科目", apply.getClazzName(), apply.getClazzId(), apply.getApplySubject().getValue()),
                    "开通包班", null
            );
            userServiceClient.saveUserServiceRecord(applicantRecord);
        } catch (Exception ex) {
            logger.error("Failed save user record of clazz_apply, reason:{}", ex.getMessage(), ex);
        }
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


    /**
     * 功能：假期作业包
     * 描述:
     * 1.小学,假期作业、期末复习
     * 2.中学,假期作业
     *
     * @param teacherId
     * @return
     */
    public List<Map<String, Object>> loadVacationHomeworkPackageList(Long teacherId) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (null != teacher) {
            boolean isJuniorTeacher = teacher.isPrimarySchool() || teacher.isInfantTeacher();
            //如果是小学老师
            if (isJuniorTeacher) {
                //小学假期
                List<NewVacationHomeworkHistory> vacationHomeworkHistoryList = vacationHomeworkReportLoaderClient.newVacationHomeworkHistory(teacher);
                if (CollectionUtils.isNotEmpty(vacationHomeworkHistoryList)) {
                    vacationHomeworkHistoryList.forEach(item -> {
                        if (null != item) {
                            Map<String, Object> vacationHomeworkMap = new HashMap<>();
                            item.setStartTime(DateUtils.dateToString(DateUtils.stringToDate(item.getStartTime()), "MM月dd日 HH:mm"));
                            item.setEndTime(DateUtils.dateToString(DateUtils.stringToDate(item.getEndTime()), "MM月dd日 HH:mm"));
                            vacationHomeworkMap = BeanMapUtils.tansBean2Map(item);
                            vacationHomeworkMap.put("homeworkFlag", "vacnHw");
                            vacationHomeworkMap.put("assignTime", item.getCreateTime());
                            dataList.add(vacationHomeworkMap);
                        }
                    });
                }
//                //小学期末
//                List<BasicReviewHomeworkHistory> basicReviewHomeworkHistoryList = basicReviewHomeworkReportLoaderClient.basicReviewHomeworkHistory(teacher);
//                if (CollectionUtils.isNotEmpty(basicReviewHomeworkHistoryList)){
//                    basicReviewHomeworkHistoryList.forEach(item -> {
//                        item.setStartTime(DateUtils.dateToString(DateUtils.stringToDate(item.getStartTime()),"MM月dd日 HH:mm"));
//                        item.setEndTime(DateUtils.dateToString(DateUtils.stringToDate(item.getEndTime()),"MM月dd日 HH:mm"));
//                        Map<String, Object> reviewHomeworkMap = new HashMap<>();
//                        reviewHomeworkMap = BeanMapUtils.tansBean2Map(item);
//                        reviewHomeworkMap.put("homeworkFlag","termReview");
//                        resultList.add(reviewHomeworkMap);
//                    });
//                }
                //如果是中学老师
            } else {
                Subject subject = teacher.getSubject();
                String url = "";
                Integer year = SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyy"));
                //中学数学
                if (subject == Subject.MATH) {
                    url += getMiddleMATHSchoolBaseURL() + "/api/1/learn/get_vacation_paper";
                    //中学英语
                } else if (subject == Subject.ENGLISH) {
                    url += getMiddleENSchoolBaseURL() + "/rpc/winterVacation/middleEnglish";
                }
                Map<Object, Object> parameterMap = new HashMap<>();
                parameterMap.put("teacherId", teacherId);
                parameterMap.put("year", year);
                try {
                    if (StringUtils.isBlank(url)) {
                        return resultList;
                    }
                    url = UrlUtils.buildUrlQuery(url, parameterMap);
                    AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
                    if (response.getStatusCode() == 200) {
                        Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
                        if (MapUtils.isNotEmpty(resultMap)) {
                            Boolean bResult = (subject == Subject.MATH && Objects.equals(resultMap.get("msg"), "成功")) || (subject == Subject.ENGLISH && (Boolean) resultMap.get("success"));
                            if (bResult) {
                                List<Map<String, Object>> data = (List<Map<String, Object>>) resultMap.get("data");
                                if (CollectionUtils.isNotEmpty(data)) {
                                    data.forEach(item -> {
                                        if (item != null) {
                                            Map<String, Object> vMap = (Map<String, Object>) item;
                                            Map<String, Object> vacationHomeworkMap = new HashMap<>();

                                            vacationHomeworkMap.put("className", vMap.get("className"));
                                            vacationHomeworkMap.put("startTime", DateUtils.dateToString(new Date(SafeConverter.toLong(vMap.get("startTime")) * 1000), "MM月dd日 HH:mm"));
                                            vacationHomeworkMap.put("endTime", DateUtils.dateToString(new Date(SafeConverter.toLong(vMap.get("endTime")) * 1000), "MM月dd日 HH:mm"));
                                            vacationHomeworkMap.put("totalNum", vMap.get("studentCount"));
                                            vacationHomeworkMap.put("beginNum", vMap.get("beginCount"));
                                            vacationHomeworkMap.put("finishNum", vMap.get("finishCount"));
                                            vacationHomeworkMap.put("homeworkFlag", "vacnHw");
                                            vacationHomeworkMap.put("assignTime", SafeConverter.toString(vMap.get("assignTime")));
                                            dataList.add(vacationHomeworkMap);
                                        }
                                    });
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("http request error :  url= " + url, e);
                    emailServiceClient.createPlainEmail()
                            .body("http request error :  url= " + url + "\r\n参数：" + JsonUtils.toJson(parameterMap))
                            .subject("中学假期作业包接口调用失败【" + RuntimeMode.current().getStageMode() + "】")
                            .to("song.wang@17zuoye.com;deliang.che@17zuoye.com")
                            .send();
                }
            }

            List<CrmHomeworkHistory> crmHomeworkHistories = new ArrayList<>();
            dataList.forEach(p -> {
                CrmHomeworkHistory crmHomeworkHistory = new CrmHomeworkHistory();
                try {
                    PropertyUtils.copyProperties(crmHomeworkHistory, p);
                    crmHomeworkHistory.setFinishNum(SafeConverter.toInt(p.get("finishNum")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                crmHomeworkHistories.add(crmHomeworkHistory);
            });

            Map<String, List<CrmHomeworkHistory>> map = new HashMap<>();
            //小学
            if (isJuniorTeacher) {
                map.putAll(crmHomeworkHistories.stream().collect(Collectors.groupingBy(p -> p.getAssignTime())));
            } else {
                map.putAll(crmHomeworkHistories.stream().collect(Collectors.groupingBy(p -> DateUtils.dateToString(new Date(SafeConverter.toLong(p.getAssignTime()) * 1000), "yyyy-MM-dd HH:mm"))));
            }
            List<String> keys = map.keySet().stream().sorted((o1, o2) -> o2.compareTo(o1)).collect(Collectors.toList());
            keys.forEach(k -> {
                List<CrmHomeworkHistory> v = map.get(k);
                if (v != null) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("assignDateStr", k);
                    m.put("list", v);
                    resultList.add(m);
                }
            });
        }
        return resultList;
    }


    public List<Map<String, Object>> generateTeacherChartInfo(Long teacherId, Integer mode, MapMessage msg) {
        List<Integer> dayList = new ArrayList<>();
        Date date = performanceService.lastSuccessDataDate();
        dayList.add(SafeConverter.toInt(DateUtils.dateToString(date, "yyyyMMdd")));
        for (int i = 0; i < 5; i++) {
            dayList.add(0, SafeConverter.toInt(DateUtils.dateToString(DayUtils.getLastDayOfMonth(DayUtils.addMonth(date, -(i + 1))), "yyyyMMdd")));
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Integer day : dayList) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("month", day / 100);
            if (mode == 1) {
//                Map<Long, AgentTeacher17PerformanceData> teacher17PerformanceDataMap = loadPerformanceServiceClient.loadTeacher17PerformanceData(Collections.singletonList(teacherId), day);
//                AgentTeacher17PerformanceData teacher17PerformanceData = teacher17PerformanceDataMap.get(teacherId);
                TeacherOnlineIndicator teacherOnlineIndicator = loadNewSchoolServiceClient.loadTeacherOnlineIndicator(Collections.singleton(teacherId), day).get(teacherId);
                if (teacherOnlineIndicator != null && teacherOnlineIndicator.getIndicatorMap() != null) {
                    OnlineIndicator onlineIndicator = teacherOnlineIndicator.fetchMonthData();
                    if (onlineIndicator != null) {
                        if (onlineIndicator.getLatestHwTime() != null) {
                            msg.put("latestHwTime", onlineIndicator.getLatestHwTime());
                        }
                        itemMap.put("tmHwSc", onlineIndicator.getTmHwSc());//本月布置所有作业套数
                        itemMap.put("finCsHwGte3AuStuCount", SafeConverter.toInt(onlineIndicator.getIncSettlementSglSubjStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNum()));      // 月活
                    } else {
                        msg.put("latestHwTime", null);
                        itemMap.put("tmHwSc", 0);      // 本月作业套数
                        itemMap.put("finCsHwGte3AuStuCount", 0);      // 月活
                    }
                } else {
                    msg.put("latestHwTime", null);
                    itemMap.put("tmHwSc", 0);      // 本月作业套数
                    itemMap.put("finCsHwGte3AuStuCount", 0);      // 月活
                }
                if (!msg.containsKey("latestHwTime")) {
                    msg.put("latestHwTime", null);
                }
            } else {
            }
            resultList.add(itemMap);
        }
        return resultList;
    }


    //数学作业域名
    public String getMiddleMATHSchoolBaseURL() {
        String url = "http://shensz-api.17zuoye.com";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            url = "http://shensz-api.test.17zuoye.net";
        } else if (RuntimeMode.isStaging()) {
            url = "http://se-math-service.staging.17zuoye.net";
        }
        return url;
    }

    //英语作业域名
    public String getMiddleENSchoolBaseURL() {
        String url = "http://zx.17zuoye.com";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            url = "http://zx.test.17zuoye.net";
        } else if (RuntimeMode.isStaging()) {
            url = "http://zx.staging.17zuoye.net";
        }
        return url;
    }

    public static void main(String[] args) {
        StringBuffer url = new StringBuffer("http://zx.test.17zuoye.net");
        url.append("/rpc/crm/loadGroupRecentHomeworkList");
        Map<Object, Object> homeworkMap = new HashMap<>();
        List<Long> ids = new ArrayList<>();
        ids.add(113129l);
        ids.add(113130l);
        ids.add(113131l);
        homeworkMap.put("groupIds", ids);
        homeworkMap.put("day", 15);
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url.toString()).addParameter(homeworkMap).execute();
            if (response.getStatusCode() == 200) {
                Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
                if (MapUtils.isNotEmpty(resultMap)) {
                    Integer code = SafeConverter.toInt(resultMap.get("error_code"));
                    if (code == 0) {
                        System.out.println(resultMap.get("data"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取老师头像
    public String getUserPhoto(Teacher teacher) {
        return getUserAvatarImgUrl(teacher);
    }

    /**
     * 中学英语作业
     *
     * @param teacherIds
     * @param homeWorkDay
     * @return
     */
    public List<HomeWorkInfo> juniorSchoolEngHomeWorkList(List<Long> teacherIds, Integer homeWorkDay) {
        List<HomeWorkInfo> result = new ArrayList<>();

        List<GroupTeacherMapper> groupTeacherList = new ArrayList<>();
        Map<Long, GroupTeacherMapper> groupMap = new HashMap<>();
        Map<Long, Clazz> clazzMap = new HashMap<>();
        bindGroupClazzData(teacherIds, groupTeacherList, groupMap, clazzMap);

        StringBuffer url = new StringBuffer(getMiddleENSchoolBaseURL());
        url.append("/rpc/crm/loadGroupRecentHomeworkList");
        Map<Object, Object> homeworkMap = new HashMap<>();
        homeworkMap.put("groupIds", groupTeacherList.stream().map(GroupTeacherMapper::getId).collect(Collectors.toList()));
        homeworkMap.put("day", homeWorkDay);
        try {
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url.toString()).addParameter(homeworkMap).execute();
            if (response.getStatusCode() == 200) {
                Map<String, Object> resultMap = JsonUtils.fromJson(response.getResponseString());
                if (MapUtils.isNotEmpty(resultMap)) {
                    Integer code = SafeConverter.toInt(resultMap.get("error_code"));
                    if (code == 0) {
                        List<Map<String, Object>> listMap = (List<Map<String, Object>>) resultMap.get("data");
                        listMap.forEach(p -> {
                            Long groupId = SafeConverter.toLong(p.get("group"));
                            HomeWorkInfo homeWorkInfo = getHomeWorkInfo(2, new Date(SafeConverter.toLong(p.get("create_time"))), SafeConverter.toBoolean(p.get("is_delete")),
                                    SafeConverter.toInt(p.get("student_count")), SafeConverter.toInt(p.get("finished_count")),
                                    false, groupMap.get(groupId), clazzMap);
                            homeWorkInfo.setSpecifiedType(true);
                            homeWorkInfo.setTypes((List<String>) p.get("practice_types"));
                            result.add(homeWorkInfo);
                        });
                    }
                }
            }
        } catch (Exception e) {
            logger.error("http request error :  url= " + url.toString(), e);
            emailServiceClient.createPlainEmail()
                    .body("http request error :  url= " + url + "\r\n参数：" + JsonUtils.toJson(homeworkMap))
                    .subject("中学英语作业接口调用失败【" + RuntimeMode.current().getStageMode() + "】")
                    .to("xianlong.zhang@17zuoye.com;deliang.che@17zuoye.com")
                    .send();
        }
        return result;
    }


    /**
     * 小学作业列表
     *
     * @param teacherIds
     * @param homeWorkDay
     * @return
     */
    public List<HomeWorkInfo> primarySchoolHomeWorkList(List<Long> teacherIds, Integer homeWorkDay) {
        List<HomeWorkInfo> result = new ArrayList<>();

        List<GroupTeacherMapper> groupTeacherList = new ArrayList<>();
        Map<Long, GroupTeacherMapper> groupMap = new HashMap<>();
        Map<Long, Clazz> clazzMap = new HashMap<>();
        bindGroupClazzData(teacherIds, groupTeacherList, groupMap, clazzMap);

        // 分页 每页200个
        PageRequest pageable = new PageRequest(0, 200);
        Page<NewHomework.Location> page = newHomeworkCrmLoaderClient.loadGroupNewHomeworks(groupMap.keySet(),
                DateUtils.getDayStart(DateUtils.nextDay(new Date(), -1 * homeWorkDay)), new Date(), pageable, true);
        if (page == null || !page.hasContent()) {
            return result;
        }
        page.getContent().forEach(v -> {
            GroupTeacherMapper group = groupMap.get(v.getClazzGroupId());
            HomeWorkInfo homeWorkInfo = getHomeWorkInfo(1, new Date(v.getCreateTime()), v.isDisabled(), 0, 0,
                    v.isChecked(), group, clazzMap);

            result.add(homeWorkInfo);
        });
        return result;
    }

    public void bindGroupClazzData(List<Long> teacherIds, List<GroupTeacherMapper> groupTeacherList, Map<Long, GroupTeacherMapper> groupMap, Map<Long, Clazz> clazzMap) {
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(teacherIds, true);
        teacherGroups.forEach((tid, groupList) -> groupList.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(tid)) { // 过滤出有效的组
                // 防止加入重复班组ID的班组信息
                Set<Long> groupIds = groupTeacherList.stream().map(GroupTeacherMapper::getId).collect(Collectors.toSet());
                if (!groupIds.contains(group.getId())) {
                    groupTeacherList.add(group);
                }
            }
        }));
        groupMap.putAll(groupTeacherList.stream().collect(Collectors.toMap(GroupTeacherMapper::getId, Function.identity())));
        clazzMap.putAll(raikouSystem.loadClazzes(groupTeacherList.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toSet())));
    }

}
