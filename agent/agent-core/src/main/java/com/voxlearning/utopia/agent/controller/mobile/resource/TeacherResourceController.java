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

package com.voxlearning.utopia.agent.controller.mobile.resource;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentErrorCode;
import com.voxlearning.utopia.agent.constants.AgentTagTargetType;
import com.voxlearning.utopia.agent.constants.MemorandumType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.memorandum.AgentMemorandum;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.memorandum.AgentMemorandumService;
import com.voxlearning.utopia.agent.service.mobile.AgentTargetTagService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.mobile.TeacherFakeService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceMapperService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.search.SearchService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.view.teacher.TeacherBasicInfo;
import com.voxlearning.utopia.agent.view.teacher.TeacherGroupInfo;
import com.voxlearning.utopia.agent.view.teacher.TeacherSubject;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.CrmGroupSummary;
import com.voxlearning.utopia.entity.crm.CrmMainSubAccountApply;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.utopia.agent.constants.AgentErrorCode.AUTH_FAILED;
import static java.util.stream.Collectors.toList;

/**
 * 老师资源页面相关请求
 * Created by Yuechen.Wang on 2016/7/11.
 */
@Controller
@RequestMapping("/mobile/resource/teacher")
public class TeacherResourceController extends AbstractAgentController {

    private static final String DATE_FORMAT = "yyyyMM";
    private static final int MONTH_DIFF = 5;        // 此数据指开始月和结束月相差5个月

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private AgentResourceService agentResourceService;
    @Inject private AgentResourceMapperService agentResourceMapperService;
    @Inject private RaikouSDK raikouSDK;
    @Inject private UserManagementClient userManagementClient;
    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject private AgentMemorandumService agentMemorandumService;
    @Inject private AgentTargetTagService agentTargetTagService;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private AgentDictSchoolService agentDictSchoolService;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private TeacherFakeService teacherFakeService;
    @Inject private TeacherResourceService teacherResourceService;
    @Inject private SearchService searchService;
    @Inject private WorkRecordService workRecordService;
    @Inject private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;
    @Inject
    private PerformanceService performanceService;

    @Inject private UserAuthQueryServiceClient userAuthQueryServiceClient;
    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;
    @Inject
    private AgentTagService agentTagService;

    /**
     * 日常作业列表接口
     *
     * @return
     */
    @RequestMapping(value = "homework_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeWorkList() {
        MapMessage mapMessage = MapMessage.successMessage();
        Long teacherId = getRequestLong("teacherId");
        MapMessage msg = agentResourceService.searchTeacherHwList(teacherId);
        if (!msg.isSuccess()) {
            mapMessage.add("homeWorkList", Collections.emptyList());
        } else {
            mapMessage.add("homeWorkList", msg.get("homeWorkInfoList"));
        }
        return mapMessage;
    }

    /**
     * 假期作业包列表
     *
     * @return
     */
    @RequestMapping(value = "vacation_homework_package_List.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage vacationHomeworkPackageList() {
        Long teacherId = getRequestLong("teacherId");
        return MapMessage.successMessage().add("vacationHomeworkPackageList", agentResourceService.loadVacationHomeworkPackageList(teacherId));
    }

    /**
     * 全部老师列表
     * 1.专员展示其下属学校内的老师小学单活可挖Top40
     * 2.市经理以及以上角色只能搜索老师，不显示卡片列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String userTeacherList(Model model) {
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return errorInfoPage(AUTH_FAILED, model);
        }
        return "rebuildViewDir/mobile/resource/teacher_list";
    }

    /**
     * 0. 如果输入的是老师ID or 手机号,不做权限过滤
     * 1. 专员根据下属的学校查询过滤(schoolIds)
     * 2. 市经理根据下属地区(countyCode)中查询出前20条相关数据
     * 3. 大区总根据下属地区(cityCode)中查询出前20条相关数据
     * 4. 全国总监根据下属地区(cityCode)中查询出前20条相关数据
     */
    @RequestMapping(value = "search.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage searchTeacherList() {
        String teacherKey = getRequestString("searchKey");
        if (StringUtils.isBlank(teacherKey)) {
            return MapMessage.errorMessage("请输入查询条件");
        }
        Integer scene = getRequestInt("scene", 1);
        teacherKey = teacherKey.trim();
        Integer pageNo = getRequestInt("pageNo");       //第几页
        Integer pageSize = getRequestInt("pageSize");   //每页数量
        AuthCurrentUser user = getCurrentUser();
        Long userId = user.getUserId();
        if (user.isProductOperator() && user.getShadowId() != null) { // 如果是产品运营角色， 则使用影子账号，以全国总监的身份查看数据
            userId = user.getShadowId();
        }
        try {
            Set<Long> teacherIds = new HashSet<>();
            List<Long> esTeacherList = searchService.searchTeachersForSceneWithPage(userId, teacherKey, scene, pageNo, pageSize);
            if (CollectionUtils.isNotEmpty(esTeacherList)) {
                teacherIds.addAll(esTeacherList);
            }

            boolean isTeacherIdOrMobile = false;
            if (MobileRule.isMobile(teacherKey) || SafeConverter.toLong(teacherKey) != 0L) { // 手机号或者老师ID
                isTeacherIdOrMobile = true;  // 根据手机号和老师ID搜索时要包含假老师数据
            }
            List<TeacherBasicInfo> teacherBasicInfoList = teacherResourceService.generateTeacherBasicInfo(teacherIds, isTeacherIdOrMobile, isTeacherIdOrMobile, true, false);
            Map<String, Object> dataMap = new HashMap<>();
            //判断是否没有更多数据了
            if (esTeacherList.size() < pageSize) {
                dataMap.put("noMoreData", true);
            } else {
                dataMap.put("noMoreData", false);
            }
            dataMap.put("teacherList", teacherBasicInfoList);
            return MapMessage.successMessage().add("dataMap", dataMap);
        } catch (Exception ex) {
            logger.error("Failed searching teacher, user={}, key={}", userId, teacherKey, ex);
            return MapMessage.errorMessage();
        }
    }


    @RequestMapping(value = "check_school.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkSchool() {
        Long teacherId = getRequestLong("teacherId");
        String type = getRequestString("type");
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        return agentResourceMapperService.schoolDirectlyResponsiblePerson(teacherId, school == null ? null : school.getId(), type);
    }

    // 老师的数据看板
    @RequestMapping(value = "data_view.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherDataView() {
        Long teacherId = getRequestLong("teacherId");
        int mode = getRequestInt("mode", 1);   // 1: online   2:offline
        MapMessage msg = MapMessage.successMessage("成功");
        msg.add("teacherId", teacherId);
        Date date = performanceService.lastSuccessDataDate();
        msg.add("lastUpdateTime", DateUtils.dateToString(date, "yyyy/MM/dd"));
        msg.add("dataList", agentResourceService.generateTeacherChartInfo(teacherId, mode, msg));
        return msg;
    }

    // 老师的关联信息
    @RequestMapping(value = "work_view.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherWorkView() {
        Long teacherId = getRequestLong("teacherId");
        MapMessage msg = MapMessage.successMessage();
        //备忘录
//        msg.add("textMemorandum", memorandumInfo(agentMemorandumService.loadMemorandumByTeacherIdFirstOne(teacherId, MemorandumType.TEXT)));
        //照片库
//        msg.add("pictureMemorandum", memorandumInfo(agentMemorandumService.loadMemorandumByTeacherIdFirstOne(teacherId, MemorandumType.PICTURE)));
        List<AgentMemorandum> list = agentMemorandumService.loadMemorandumByTeacherId(teacherId, null, null, null);
        List<Map<String, Object>> resultList = new ArrayList<>();
        list.forEach(p -> {
            if (p.getType().equals(MemorandumType.PICTURE)) {
                p.setUrl(p.getContent());
                p.setContent("");
            }
            Map<String, Object> map = BeanMapUtils.tansBean2Map(p);
            AgentUser agentUser = baseOrgService.getUser(p.getCreateUserId());
            map.put("createUserName", agentUser == null ? "" : agentUser.getRealName());
            resultList.add(map);
        });
        msg.add("memorandumList", resultList);
        return msg;
    }

    private Map<String, String> memorandumInfo(AgentMemorandum agentMemorandum) {
        Map<String, String> result = new HashMap<>();
        if (agentMemorandum == null) {
            return null;
        }
        result.put("content", agentMemorandum.getContent());
        result.put("writeTime", DateUtils.dateToString(agentMemorandum.getWriteTime(), DateUtils.FORMAT_SQL_DATE));
        return result;
    }


    /**
     * 开通包班选择学科和班级接口
     *
     * @return
     */
    @RequestMapping(value = "clazz_apply_select.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage applyClazz2() {
        MapMessage mapMessage = MapMessage.successMessage();
        Long teacherId = getRequestLong("teacherId");
        try {
            // 老师信息
//            CrmTeacherSummary teacher = crmSummaryLoaderClient.loadTeacherSummary(teacherId);
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            if (teacher == null) {
                return MapMessage.errorMessage("无效的老师ID = " + teacherId);
            }
            mapMessage.put("teacherId", teacherId);
//            mapMessage.put("teacher", teacher);

            School school = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(teacherId)
                    .getUninterruptibly();
            mapMessage.put("schoolId", school == null ? null : school.getId());
            Map<String, List<Map<String, Object>>> subjectClazz = getTeacherSubjectClazz(teacherId);
            if (null != subjectClazz) {
                mapMessage.put("subjectClazz", subjectClazz);
            }
            return mapMessage;
        } catch (Exception ex) {
            logger.error("原因:{}", ex.getMessage());
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 获取老师可以申请开通包班的班级信息
     *
     * @param teacherId
     * @return
     */
    private Map<String, List<Map<String, Object>>> getTeacherSubjectClazz(Long teacherId) {

        // 如果该老师不是主账户，取到所有的子账户老师
        // 理论上来说，从列表点击卡片进来的话，所有的老师都是主账号的老师
        // 不排除会有其他入口进来的可能
        Set<Long> relatedTeachers = teacherLoaderClient.loadRelTeacherIds(teacherId);
        // 过滤掉假老师
        Map<Long, TeacherExtAttribute> extAttributeMap = teacherLoaderClient.loadTeacherExtAttributes(relatedTeachers);
        List<Long> teacherIds = relatedTeachers.stream()
                .filter(t -> extAttributeMap.get(t) == null || !extAttributeMap.get(t).isFakeTeacher()).collect(Collectors.toList());

        Map<Long, List<GroupMapper>> teacherGroupList = groupLoaderClient.loadTeacherGroupsByTeacherId(teacherIds, false);
        if (MapUtils.isEmpty(teacherGroupList)) {
            return null;
        }
        Map<Subject, List<Long>> subjectGroupMap = new HashMap<>();
        teacherGroupList.values().stream().flatMap(List::stream).filter(k -> k != null && k.getSubject() != null && k.getSubject() != Subject.UNKNOWN).forEach(t -> {
            List<Long> groupIdList = subjectGroupMap.get(t.getSubject());
            if (groupIdList == null) {
                groupIdList = new ArrayList<>();
                subjectGroupMap.put(t.getSubject(), groupIdList);
            }
            if (!groupIdList.contains(t.getId())) {
                groupIdList.add(t.getId());
            }
        });

        if (MapUtils.isEmpty(subjectGroupMap)) {
            return null;
        }
        // 老师未教学科
        List<Subject> subjects = Stream.of(Subject.CHINESE, Subject.ENGLISH, Subject.MATH)
                .filter(s -> !subjectGroupMap.containsKey(s))
                .collect(Collectors.toList());
        // 过滤语文灰度
        // 测试环境下不校验这个(因为数据匹配不上而导致必然被过滤)
        if (RuntimeMode.current().ge(Mode.STAGING)) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacherDetail == null) {
                //return errorInfoPage(AgentErrorCode.TEACHER_CLAZZ_APPLY_ERROR, StringUtils.formatMessage("老师账号异常(ID:{})", teacherId), model);
                return null;
            }
            // 中学不支持包班制
            if (!teacherDetail.isPrimarySchool() && !teacherDetail.isInfantTeacher()) {
                return null;
            }
            if (subjects.contains(Subject.CHINESE)
                    && !grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "Chinese", "Register")) {
                subjects.remove(Subject.CHINESE);
            }
        }
        // 专员如果已经申请过某个学科的，过滤掉
        // 如果已经驳回了申请，还允许重新申请
        List<Subject> applySubject = agentResourceService.getTeacherApplyRecord(teacherId).stream()
                .filter(CrmMainSubAccountApply::canBeApplied)
                .map(CrmMainSubAccountApply::getApplySubject).collect(toList());
        subjects.removeAll(applySubject);
        Map<String, List<Map<String, Object>>> subjectClazz = new HashMap<>();
        if (CollectionUtils.isNotEmpty(subjects)) {
            // 学科之下的班级
            List<Long> groupIds = subjectGroupMap.values().stream()
                    .flatMap(List::stream)
                    .collect(toList());
            Map<Long, GroupMapper> groupMapperMap = groupLoaderClient.loadGroups(groupIds, false);
            if (MapUtils.isNotEmpty(groupMapperMap)) {
                // 根据clazzId去重
                Map<Long, GroupMapper> clazzMap = groupMapperMap.values().stream().collect(Collectors.toMap(GroupMapper::getClazzId, Function.identity(), (c1, c2) -> c1));
                List<TeacherGroupInfo> resultList = new ArrayList<>();
                clazzMap.values().forEach(p -> {
                    Clazz clazz = raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(p.getClazzId());
                    if (!clazz.isDisabledTrue() && clazz.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && clazz.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED && clazz.getClazzLevel() != ClazzLevel.INFANT_GRADUATED) {
                        TeacherGroupInfo groupInfo = new TeacherGroupInfo();
                        groupInfo.setGrade(clazz.getClazzLevel().getLevel());
                        groupInfo.setClassId(clazz.getId());
                        groupInfo.setClassName(clazz.getClassName());
                        groupInfo.setClassFullName(clazz.formalizeClazzName());
                        groupInfo.setGroupId(p.getId());
                        groupInfo.setUpdateTime(clazz.getUpdateTime());
                        resultList.add(groupInfo);
                    }
                });
                Map<Integer, List<TeacherGroupInfo>> clazzGroupMap = resultList.stream().collect(Collectors.groupingBy(TeacherGroupInfo::getGrade, Collectors.toList()));

              /*  List<Map<String, Object>> clazzInfo = clazzMap.values().stream().map(p -> {
                    Map<String, Object> groupInfo = new HashMap<>();
                    groupInfo.put("gid", p.getId());
                    groupInfo.put("cid", p.getClazzId());
                    Clazz clazz = clazzLoaderClient.getClazzLoader()
                            .loadClazz(p.getClazzId())
                            .getUninterruptibly();
                    groupInfo.put("cname", clazz == null ? "" : clazz.formalizeClazzName());
                    return groupInfo;
                }).collect(Collectors.toList());*/
                List<Map<String, Object>> mapList = new ArrayList<>();
                clazzGroupMap.forEach((key, value) -> {
                    Map<String, Object> clazzGroupMapResult = new HashMap<>();
                    clazzGroupMapResult.put("grade", key);
                    clazzGroupMapResult.put("calzzList", value);
                    mapList.add(clazzGroupMapResult);
                });
                subjects.forEach(t -> subjectClazz.putIfAbsent(t.name(), mapList));
            }
        }
        return subjectClazz;
    }

    @RequestMapping(value = "clazz_apply.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("8b11894c7eaa4c93")
    public MapMessage mainSubAccountApply() {
        AuthCurrentUser user = getCurrentUser();
        if (user.isProductOperator()) { // 产品运营角色 只有查看资源部分数据的功能， 没有操作功能
            return MapMessage.errorMessage("您没有权限操作该功能！");
        }
        Long teacherId = getRequestLong("teacherId");
        Long clazzId = getRequestLong("clazzId");
        Subject subject = Subject.safeParse(getRequestString("subject"));
        if (teacherId == 0L || clazzId == 0L || subject == null) {
            return MapMessage.errorMessage("参数错误");
        }
        Map<Long, Long> subMainTeacherMap = teacherLoaderClient.loadMainTeacherIds(Collections.singleton(teacherId));
        Long mainAccount = subMainTeacherMap.get(teacherId);
        if (mainAccount != null) {
            return MapMessage.errorMessage("请使用主账号老师开通包班 ！ ");
        }
        if (!user.isBusinessDeveloper() && !user.isCityManager() && !user.isRegionManager() && !user.isCountryManager()) {
            return MapMessage.errorMessage("没有开通包班的权限");
        }
        try {
            return agentResourceService.applyMainSubAccount(getCurrentUserId(), teacherId, clazzId, subject);
        } catch (Exception ex) {
            logger.error("Failed to apply, teacher={},subject={},class={}", teacherId, subject, clazzId);
            return MapMessage.errorMessage("开通失败");
        }
    }

    @RequestMapping(value = "clazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage teacherClazz() {
        MapMessage mapMessage = MapMessage.successMessage();
        Long groupId = getRequestLong("groupId");
        Long teacherId = getRequestLong("teacherId");
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage();
        }
        mapMessage.add("teacherId", teacherId);
        if (groupId == 0L) {
            return MapMessage.errorMessage();
        }
        CrmGroupSummary summary = crmSummaryLoaderClient.loadGroupSummary(groupId);
        mapMessage.add("clazzInfo", agentResourceMapperService.mapClazzDetail(summary));
        List<User> studentList = studentLoaderClient.loadGroupStudents(Collections.singleton(groupId)).get(groupId);
        List<Map<String, Object>> studentDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(studentList)) {

            Collator collator = Collator.getInstance(Locale.CHINA);
            Collections.sort(studentList, (o1, o2) -> {
                String name1 = StringUtils.defaultString(o1.getProfile().getRealname());
                String name2 = StringUtils.defaultString(o2.getProfile().getRealname());
                if (StringUtils.isNotBlank(name1) && StringUtils.isNotBlank(name2)) {
                    return collator.compare(name1, name2);
                } else if (StringUtils.isBlank(name1) && StringUtils.isNotBlank(name2)) {
                    return 1;
                } else if (StringUtils.isNotBlank(name1) && StringUtils.isBlank(name2)) {
                    return -1;
                }
                return 0;
            });
            List<Long> allStudentIds = studentList.stream().map(User::getId).collect(toList());
            List<Long> authedStudentIds = userAuthQueryServiceClient.filterAuthedStudents(allStudentIds, teacher.getKtwelve() != null ? SchoolLevel.safeParse(teacher.getKtwelve().getLevel()) : SchoolLevel.JUNIOR);
            studentList.forEach(p -> {
                Map<String, Object> studentMap = new HashMap<>();
                studentMap.put("studentId", p.getId());
                if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                    studentMap.put("authState", p.getAuthenticationState());
                } else if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
                    if (CollectionUtils.isNotEmpty(authedStudentIds)) {
                        studentMap.put("authState", authedStudentIds.contains(p.getId()));
                    } else {
                        studentMap.put("authState", false);
                    }
                }

                studentMap.put("studentName", StringUtils.defaultString(p.getProfile().getRealname()));
                studentDataList.add(studentMap);
            });
        }
        mapMessage.add("studentList", studentDataList);
        return mapMessage;
    }

    /**
     * 解除老师界面跳转
     *
     * @return 解除老师判假页
     */
    @RequestMapping(value = "relieve_faketeacher_view.vpage", method = RequestMethod.GET)
    public String relieve_faketeacher(Model model) {
        Long teacherId = getRequestLong("teacherId");
        model.addAttribute("teacherId", teacherId);
        return "rebuildViewDir/mobile/resource/cancleFake";
    }

    /**
     * 解除老师判假
     * CrmTeacherNewController 迁移
     *
     * @return
     */
    @RequestMapping(value = "relieve_faketeacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage undoFakeTeacher() {
        // 对于假老师的处理，解除客服误操作判假的老师
        // 对于客服手工判假的老师会在VOX_USER_TAG里面有数据，同时还在CRM_TEACHER_SUMMARY里面有数据
        // 两个地方需要同时清理
        Long teacherId = getRequestLong("teacherId");
        String desc = getRequestString("desc");

        Set<Long> teacherMainSubIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        MapMessage message = MapMessage.successMessage();
        for (Long tempteacherId : teacherMainSubIds) {
            boolean flag = false;
            // set teacher fake in teacher ext attribute
            userManagementClient.setTeacherFake(tempteacherId, false, null);

            CrmTeacherSummary teacherSummary = crmSummaryLoaderClient.loadTeacherSummary(tempteacherId);
            AuthCurrentUser currentUser = getCurrentUser();
            if (teacherSummary != null && Boolean.TRUE.equals(teacherSummary.getFakeTeacher())) {
                message = crmSummaryServiceClient.removeTeacherFakeType(tempteacherId);
                flag = true;
            }
            agentResourceMapperService.defakeTeacher(tempteacherId, currentUser);
            // 记录 UserServiceRecord
            if (flag) {
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(teacherId);
                userServiceRecord.setOperatorId(currentUser.getUserId().toString());
                userServiceRecord.setOperatorName(currentUser.getRealName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.市场取消老师判假.name());
                userServiceRecord.setOperationContent("解除老师判假");
                userServiceRecord.setComments("说明[" + desc + "]");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }
        }
        return message;
    }

    /**
     * 校本题库管理员修改
     * 跳转界面
     *
     * @return
     */
    @RequestMapping(value = "change_school_quiz_bank_administrator_view.vpage", method = RequestMethod.GET)
    public String changeSchoolQuizBankAdministratorView(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        MapMessage mapMessage = agentResourceMapperService.permissionSchoolQuizBankAdministrator(teacherId, schoolId);
        model.addAttribute("schoolQuizBankAdministratorType", mapMessage.get("schoolQuizBankAdministratorType"));
        model.addAttribute("originalTeacherId", mapMessage.get("originalTeacherId"));
        model.addAttribute("otherExistenceTeacherName", mapMessage.get("otherExistenceTeacherName"));
        model.addAttribute("schoolQuizBankAdministratorMessage", mapMessage.getInfo());
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("schoolId", schoolId);
        return "rebuildViewDir/mobile/resource/change_school_quiz_bank_administrator_view";
    }

    /**
     * 校本题库管理员变更
     *
     * @return
     */
    @RequestMapping(value = "change_school_quiz_bank_administrator.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeSchoolQuizBankAdministrator() {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        if (teacherId == 0L || schoolId == 0L) {
            MapMessage.errorMessage("数据有误,请刷新后操作");
        }
        return agentResourceMapperService.changeSchoolQuizBankAdministrator(teacherId, schoolId, getCurrentUser());
    }

    /**
     * 学科组长信息
     *
     * @return
     */
    @RequestMapping(value = "change_klx_subject_leader_view.vpage", method = RequestMethod.GET)
    public String changeKlxSubjectLeaderView(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        model.addAttribute("gradeDetailAndIfKlxSubjectLeaderList", agentResourceMapperService.findGradeDetailAndIfKlxSubjectLeader(teacherId, schoolId));
        //既然能跳转说明该学校一定是字典表学校，若是有error 界面地址就将此处的异常按照 error 界面中的格式打印并指向error界面
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("schoolId", schoolId);
        return "rebuildViewDir/mobile/resource/change_klx_subject_leader_view";
    }

    @RequestMapping(value = "change_klx_subject_leader_view_new.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage changeKlxSubjectLeaderViewNew() {
        MapMessage mapMessage = MapMessage.successMessage();
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        mapMessage.put("gradeDetailAndIfKlxSubjectLeaderList", agentResourceMapperService.findGradeDetailAndIfKlxSubjectLeader(teacherId, schoolId));
        //既然能跳转说明该学校一定是字典表学校，若是有error 界面地址就将此处的异常按照 error 界面中的格式打印并指向error界面
        mapMessage.put("teacherId", teacherId);
        mapMessage.put("schoolId", schoolId);
        return mapMessage;
    }

    /**
     * 学科组长变更
     *
     * @return
     */
    @RequestMapping(value = "change_klx_subject_leader.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeKlxSubjectLeader() {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        String desc = getRequestString("desc");
        if (teacherId == 0L || schoolId == 0L) {
            return MapMessage.errorMessage("参数错误");
        }
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("问题描述不能为空");
        }
        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if (agentDictSchool == null) {
            return MapMessage.errorMessage("请先将学校申请加入字典表再开通");
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo == null || !Objects.equals(Boolean.TRUE, schoolExtInfo.getScanMachineFlag())) {
            return MapMessage.errorMessage("请先申请开通阅卷机权限后，再申请开通其它权限");
        }
        String newClazzLevels = getRequestString("newClazzLevels");//可以查看的年级 修改之后的
        String oldClazzLevels = getRequestString("oldClazzLevels");//可以查看的年级 修改之前
        MapMessage checkMessage = agentResourceMapperService.schoolDirectlyResponsiblePerson(teacherId, schoolId, "headMan");
        if (checkMessage.getSuccess()) {
            checkMessage = agentResourceMapperService.changeSubjectLeader(teacherId, newClazzLevels, desc);
        }
        return checkMessage;
    }


    /**
     * 快乐学职务
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "klx_duty.vpage", method = RequestMethod.GET)
    @OperationCode("e248d765aaf844c4")
    public String klxDuty(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        if (teacherId == 0L) {
            return errorInfoPage(AgentErrorCode.TEACHER_RESOURCE_INFO_ERROR, StringUtils.formatMessage("无效的老师ID:{}", teacherId), model);
        }
        if (schoolId == 0L) {
            return errorInfoPage(AgentErrorCode.SCHOOL_RESOURCE_INFO_ERROR, StringUtils.formatMessage("无效的学校ID:{}", schoolId), model);
        }
        if (!searchService.hasTeacherPermission(getCurrentUserId(), teacherId, SearchService.SCENE_DICT)) {
            return errorInfoPage(AgentErrorCode.NO_AUTH_AND_BACK, "您无该老师的操作权限", model);
        }
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("schoolId", schoolId);

        return "rebuildViewDir/mobile/resource/set_klx_duty";
    }

    /**
     * 老师快乐学职务数据
     *
     * @return
     */
    @RequestMapping(value = "klx_duty_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage klxDutyData() {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        return agentResourceMapperService.getKlxDutyData(teacherId, schoolId);
    }

    /**
     * 年级主任设置数据
     *
     * @return
     */
    @RequestMapping(value = "getgrademanagelist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getGradeManagerList() {
        Long teacherId = getRequestLong("teacherId");
        return agentResourceMapperService.getGradeManagerList(teacherId);
    }

    /**
     * 设置年级主任
     *
     * @return
     */
    @RequestMapping(value = "setgrademanagelist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setGradeManagerList() {
        // 检查参数
        String desc = getRequestString("desc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请输入描述");
        }
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");

        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if (agentDictSchool == null) {
            return MapMessage.errorMessage("请先将学校申请加入字典表再开通");
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo == null || !Objects.equals(Boolean.TRUE, schoolExtInfo.getScanMachineFlag())) {
            return MapMessage.errorMessage("请先申请开通阅卷机权限后，再申请开通其它权限");
        }

        String clazzLevels = getRequestString("grades");
        return agentResourceMapperService.setSchoolGradeManager(teacherId, clazzLevels, desc, getCurrentUser());
    }

    /**
     * 班主任
     *
     * @return
     */
    @RequestMapping(value = "classmanagelist.vpage", method = RequestMethod.GET)
    public String classManagelist(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("schoolId", schoolId);
        return "rebuildViewDir/mobile/resource/classmanagelist";
    }

    /**
     * 获取班主任数据
     *
     * @return
     */
    @RequestMapping(value = "getclassmanagelist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getClassManagerList() {
        Long teacherId = getRequestLong("teacherId");
        return agentResourceMapperService.getClassManagerList(teacherId);
    }

    /**
     * 设置班主任
     *
     * @return
     */
    @RequestMapping(value = "setclassmanagelist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setClassManagerList() {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if (agentDictSchool == null) {
            return MapMessage.errorMessage("请先将学校申请加入字典表再开通");
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo == null || !Objects.equals(Boolean.TRUE, schoolExtInfo.getScanMachineFlag())) {
            return MapMessage.errorMessage("请先申请开通阅卷机权限后，再申请开通其它权限");
        }
        String classIds = getRequestString("classIds");
        return agentResourceMapperService.setClassManager(teacherId, classIds, "设置班主任", getCurrentUser());
    }

    /**
     * 创建教务账号
     *
     * @return
     */
    @RequestMapping(value = "createaffairteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createAffairTeacher() {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
        if (agentDictSchool == null) {
            return MapMessage.errorMessage("请先将学校申请加入字典表再开通");
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo == null || !Objects.equals(Boolean.TRUE, schoolExtInfo.getScanMachineFlag())) {
            return MapMessage.errorMessage("请先申请开通阅卷机权限后，再申请开通其它权限");
        }
        return agentResourceMapperService.createAffairTeacher(teacherId, schoolId, getCurrentUser());
    }

    /**
     * 创建教务账号
     */
    @RequestMapping(value = "createaffairteacher.vpage", method = RequestMethod.GET)
    public String createAffairTeacher(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        boolean isAffair = getRequestBool("isAffair");
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("isAffair", isAffair);
        return "rebuildViewDir/mobile/resource/createaffairteacher";
    }


    /**
     * 年级主任
     */
    @RequestMapping(value = "change_klx_grade_manager_view.vpage", method = RequestMethod.GET)
    public String changeKlxGradeManagerView(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("schoolId", schoolId);
        return "rebuildViewDir/mobile/resource/change_klx_grade_manager_view";
    }

    /**
     * 设置快乐学考试管理员页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "set_klx_exam_manager_view.vpage", method = RequestMethod.GET)
    public String setKlxExamManagerView(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        boolean isExamManager = getRequestBool("isExamManager");
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("isExamManager", isExamManager);
        return "rebuildViewDir/mobile/resource/set_klx_exam_manager_view";
    }

    /**
     * 设置或取消快乐学考试管理员请求
     *
     * @return
     */
    @RequestMapping(value = "set_klx_exam_manager_data.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setKlxExamManagerPost() {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        boolean isExamManager = getRequestBool("isExamManager");
        if (isExamManager) {
            AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
            if (agentDictSchool == null) {
                return MapMessage.errorMessage("请先将学校申请加入字典表再开通");
            }
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(schoolId)
                    .getUninterruptibly();
            if (schoolExtInfo == null || !Objects.equals(Boolean.TRUE, schoolExtInfo.getScanMachineFlag())) {
                return MapMessage.errorMessage("请先申请开通阅卷机权限后，再申请开通其它权限");
            }
            return agentResourceMapperService.setExamManager(schoolId, teacherId, getCurrentUser());
        } else {
            return agentResourceMapperService.cancelExamManager(schoolId, teacherId, getCurrentUser());
        }
    }

    /**
     * 设置校长页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "set_school_master_view.vpage", method = RequestMethod.GET)
    public String setSchoolMasterView(Model model) {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        boolean isSchoolMaster = getRequestBool("isSchoolMaster");
        model.addAttribute("schoolId", schoolId);
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("isSchoolMaster", isSchoolMaster);
        return "rebuildViewDir/mobile/resource/set_school_master_view";
    }

    /**
     * 设置或取消校长权限
     *
     * @return
     */
    @RequestMapping(value = "set_school_master_data.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setSchoolMasterPost() {
        Long teacherId = getRequestLong("teacherId");
        Long schoolId = getRequestLong("schoolId");
        boolean isSchoolMaster = getRequestBool("isSchoolMaster");
        //设置为校长，校验条件
        if (isSchoolMaster) {
            AgentDictSchool agentDictSchool = agentDictSchoolLoaderClient.findBySchoolId(schoolId);
            if (agentDictSchool == null) {
                return MapMessage.errorMessage("请先将学校申请加入字典表再开通");
            }
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(schoolId)
                    .getUninterruptibly();
            if (schoolExtInfo == null || !Objects.equals(Boolean.TRUE, schoolExtInfo.getScanMachineFlag())) {
                return MapMessage.errorMessage("请先申请开通阅卷机权限后，再申请开通其它权限");
            }
        }
        //设置或取消校长权限
        return agentResourceMapperService.setSchoolMaster(schoolId, teacherId, isSchoolMaster);
    }


    /**
     * 查询老师基础信息
     *
     * @return
     */
    @RequestMapping(value = "detail.vpage")
    @ResponseBody
    public MapMessage teacherDetail() {
        Long teacherId = getRequestLong("teacherId");
        MapMessage message = MapMessage.successMessage();

        //公私海场景，判断该用户是否有权限操作，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(getCurrentUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))) {
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限", mapMessage.get("teacherManager")));
            } else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        // 老师基本信息
        TeacherBasicInfo teacherBasicInfo = teacherResourceService.generateTeacherBasicInfo(teacherId, true);
        message.add("teacherBasicInfo", teacherBasicInfo);

        // 老师的扩展状态信息
        Map<String, Object> teacherExtStateInfo = teacherResourceService.generateTeacherExtStateInfo(teacherId);
        message.add("teacherExtStateInfo", teacherExtStateInfo);

        Long targetTeacherId = teacherId;
        if (teacherBasicInfo != null) {
            targetTeacherId = teacherBasicInfo.getTeacherId();
        }
        // 老师标签
//        List<AgentTag> tagList = agentTargetTagService.loadTeacherTags(targetTeacherId);   // 从 teacherBasicInfo 中取出的是主账号
        List<Map<String, Object>> teacherTagList = new ArrayList<>();
        Map<String, List<com.voxlearning.utopia.agent.persist.entity.tag.AgentTag>> tagMap = agentTagService.getTagListByTargetIdsAndType(Collections.singleton(SafeConverter.toString(targetTeacherId)), AgentTagTargetType.TEACHER,true);
        List<com.voxlearning.utopia.agent.persist.entity.tag.AgentTag> tagList = tagMap.get(SafeConverter.toString(targetTeacherId));
        if (CollectionUtils.isNotEmpty(tagList)){
            teacherTagList.addAll(agentTagService.convertTagList(tagList));
        }
        message.add("teacherTagList", teacherTagList);

        // 判断老师是否处在判假审核中
        message.add("haveWaitingReviewFakeRecord", teacherFakeService.haveWaitingReviewFakeRecord(targetTeacherId));
        // 判断老师是否是活跃老师
        message.add("isActiveTeacher", teacherFakeService.isActiveTeacher(teacherId));

        return message;
    }


    /**
     * 老师数据概览
     *
     * @return
     */
    @RequestMapping(value = "loadKpiData.vpage")
    @ResponseBody
    public MapMessage teacherKpiData() {
        Long teacherId = getRequestLong("teacherId");
        int mode = getRequestInt("mode", 1);   // 1: online   2:offline
        if (mode != 1 && mode != 2) {
            mode = 1;
        }

        MapMessage message = MapMessage.successMessage();

        // 老师基本指标信息
        Map<String, Object> teacherKpiData = teacherResourceService.generateTeacherKpiData(teacherId, mode);
        message.add("teacherKpiData", teacherKpiData);

        // 老师名校班组列表
        List<TeacherGroupInfo> teacherGroupInfoList = teacherResourceService.generateTeacherGroupListWithKpiData(teacherId, mode);
        message.add("teacherClassList", teacherGroupInfoList);
        return message;
    }

    /**
     * 跳转老师详情页之前，操作权限控制
     *
     * @return
     */
    @RequestMapping(value = "teacher_detail_authority_message.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherDetailAuthorityMessage() {
        Long userId = getRequestLong("userId");
        if (userId <= 0) {
            userId = getCurrentUserId();
        }
        Long teacherId = getRequestLong("teacherId");
        Integer scene = getRequestInt("scene");
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(userId, teacherId, scene);
        if (!mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))) {
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限", mapMessage.get("teacherManager")));
            } else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 查询老师名下班级列表
     */
    @RequestMapping(value = "teacherClassList.vpage")
    @ResponseBody
    public MapMessage teacherClassList() {
        Long teacherId = getRequestLong("teacherId");
        MapMessage message = MapMessage.successMessage();
        Map<Integer, List<TeacherGroupInfo>> resultMap = teacherResourceService.generateTeacherGroupList(teacherId).stream().collect(Collectors.groupingBy(TeacherGroupInfo::getGrade, Collectors.toList()));
        List<Map<String, Object>> list = new ArrayList<>();
        resultMap.forEach((k, v) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("grade", k);
            map.put("classList", v);
            list.add(map);
        });
        message.put("dataList", list);
        return message;
    }

    /**
     * 所在部门下负责的区域
     *
     * @return
     */
    @RequestMapping(value = "getAllRegion.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAllRegion() {
//        Map<Integer, ExRegion> allRegions = regionServiceClient.getExRegionBuffer().loadAllRegions();
//        List<ExRegion> regions = new ArrayList<>(allRegions.values());
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (roleType == AgentRoleType.Country || roleType == AgentRoleType.BUManager || roleType == AgentRoleType.Region || roleType == AgentRoleType.AreaManager
                || roleType == AgentRoleType.CityManager || roleType == AgentRoleType.BusinessDeveloper) {
            Collection<ExRegion> counties = baseOrgService.getCountyRegionByUserId(getCurrentUserId(), roleType);
            if (CollectionUtils.isEmpty(counties)) {
                return MapMessage.errorMessage("该用户下无地区");
            }
            resultList = baseOrgService.createRegionTreeGroupByFLetter(counties);
        }
        return MapMessage.successMessage().add("data", resultList);

    }

    /**
     * 最近拜访老师记录
     */
    @RequestMapping(value = "recentVisitTeacherList.vpage")
    @ResponseBody
    public MapMessage recentVisitTeacherList() {
        MapMessage mapMessage = MapMessage.successMessage();
        //按地区筛选暂时去掉了  regionCode条件用不上了
        Integer regionCode = requestInteger("regionCode");
        Long userId = requestLong("userId");

        List<AgentGroup> userGroups = baseOrgService.getUserGroups(getCurrentUserId());
        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<AgentGroup> subAgentGroup = new ArrayList<>();
        userGroups.forEach(p -> {
            List<AgentGroup> subList = baseOrgService.getSubGroupList(p.getId());
            if (CollectionUtils.isNotEmpty(subList)) {
                subAgentGroup.addAll(subList);
            }
        });
        List<CrmWorkRecord> finalWorkList = new ArrayList<>();
        if (roleType == AgentRoleType.BusinessDeveloper) {//专员时
            List<CrmWorkRecord> userWorkRecord = workRecordService.loadCrmWorkRecordByUsers(getCurrentUserId(), DateUtils.calculateDateDay(new Date(), -7), new Date());
            finalWorkList.addAll(userWorkRecord);
        } else {
            List<CrmWorkRecord> userWorkRecord = null;
            if ((regionCode != null && regionCode <= 0) && (userId != null && userId <= 0)) {//没有选择条件时找一个专员查专员的
                Long deveId = baseOrgService.getUserByGroupIdsAndRole(subAgentGroup.stream().map(AgentGroup::getId).collect(toList()), AgentRoleType.BusinessDeveloper).stream().findFirst().orElse(null);
                userWorkRecord = workRecordService.loadCrmWorkRecordByUsers(deveId, DateUtils.calculateDateDay(new Date(), -7), new Date());
                finalWorkList.addAll(userWorkRecord);
            } else {
                List<CrmWorkRecord> schoolWorkRecordList = new ArrayList<>();
                if (regionCode != null && regionCode > 0) {//选择当前地区包含的人员
                    List<AgentUser> agentUsers = baseOrgService.findUserByReginCode(subAgentGroup.stream().map(AgentGroup::getId).collect(toList()), regionCode);
                    List<CrmWorkRecord> workRecords = crmWorkRecordLoaderClient.listByWorkersAndType(agentUsers.stream().map(AgentUser::getId).collect(toList()), CrmWorkRecordType.SCHOOL, DateUtils.calculateDateDay(new Date(), -7), new Date());
//                    按省市区查出来的理论上就是在
//                    List<Long> schoolIds = workRecords.stream().map(CrmWorkRecord::getSchoolId).collect(toList());
//                    Page<SchoolEsInfo> schoolEsInfos = searchService.searchSchoolInManagedSchools(schoolIds,userId,0,100);
//                    List<String>  effectiveIds = schoolEsInfos.getContent().stream().filter(s -> s.getCountyCode() == regionCode).map(SchoolEsInfo::getId).collect(toList());
//                    schoolWorkRecordList.addAll(workRecords.stream().filter(crmWorkRecord -> effectiveIds.contains(String.valueOf(crmWorkRecord.getSchoolId()))).collect(toList()));
                    schoolWorkRecordList.addAll(workRecords);
                }

                if (userId != null && userId > 0) {
                    userWorkRecord = workRecordService.loadCrmWorkRecordByUsers(userId, DateUtils.calculateDateDay(new Date(), -7), new Date());
                }
                if ((regionCode != null && regionCode > 0) && (userId != null && userId > 0)) {
                    if (CollectionUtils.isNotEmpty(schoolWorkRecordList) && CollectionUtils.isNotEmpty(userWorkRecord)) {//两个条件都有值取交集
                        Map<String, List<CrmWorkRecord>> schoolRecordMap = schoolWorkRecordList.stream().collect(Collectors.groupingBy(CrmWorkRecord::getId));
                        Map<String, List<CrmWorkRecord>> userRecordMap = userWorkRecord.stream().collect(Collectors.groupingBy(CrmWorkRecord::getId));
                        schoolRecordMap.forEach((k, v) -> {
                            if (CollectionUtils.isNotEmpty(userRecordMap.get(k))) {
                                finalWorkList.addAll(userRecordMap.get(k));
                            }
                        });
                    }
                } else {//选择地区 或选择人时
                    if (CollectionUtils.isNotEmpty(userWorkRecord)) {
                        finalWorkList.addAll(userWorkRecord);
                    } else if (CollectionUtils.isNotEmpty(schoolWorkRecordList)) {
                        finalWorkList.addAll(schoolWorkRecordList);
                    }
//                    finalWorkList.addAll(Collections.emptyList());
                }
            }

        }

        Set<Long> teacherIds = new HashSet<>();
        Map<String, Date> teacherMap = new HashMap<>();//每个老师对应的拜访记录
        finalWorkList.forEach(p -> {
            List<CrmTeacherVisitInfo> teachers = p.getVisitTeacherList();
            Set<Long> idSet = new HashSet<>();
            teachers.forEach(t -> {
                idSet.add(t.getTeacherId());
                teacherMap.put(String.valueOf(t.getTeacherId()), p.getCreateTime());
            });
//            Set<Long> idSet = teachers.stream().map(CrmTeacherVisitInfo::getTeacherId).collect(Collectors.toSet());
            teacherIds.addAll(idSet);
        });
        List<TeacherBasicInfo> teacherBasicInfoList = teacherResourceService.generateTeacherBasicInfo(teacherIds, false, false, true, false);

//        MapMessage msg = agentResourceService.searchTeacherHwListByTeacherIds(teacherIds);
//        List<HomeWorkInfo> homeWorkInfoList = (List<HomeWorkInfo>) msg.get("homeWorkList");
//        Map<Long, List<GroupMapper>> teacherGroupList = groupLoaderClient.loadTeacherGroupsByTeacherId(teacherIds, false);
//        List<Map<String,Object>> result = new ArrayList<>();
        int hashkAftVisitNum = 0;
        for (TeacherBasicInfo tb : teacherBasicInfoList) {
//            Map<String,Object> map = BeanMapUtils.tansBean2Map(tb);
            Date visitTime = teacherMap.get(String.valueOf(tb.getTeacherId()));

            List<TeacherSubject> subjects = tb.getSubjects();
            for (TeacherSubject s : subjects) {
                s.getKpiData().put("lastVisitTime", visitTime);
                s.getKpiData().put("hashkAftVisit", false);
                Date latestHwTime = SafeConverter.toDate(s.getKpiData().get("latestHwTime"));
                if (visitTime != null && latestHwTime != null) {
                    if (visitTime.before(latestHwTime)) {
                        s.getKpiData().put("hashkAftVisit", true);
                        hashkAftVisitNum++;
                    }
                }
            }
//            result.add(map);
        }

        mapMessage.add("visitTeacherNum", teacherBasicInfoList.size());
        mapMessage.add("hashkAftVisitNum", hashkAftVisitNum);
        if (CollectionUtils.isNotEmpty(teacherBasicInfoList)) {

            Collator collator = Collator.getInstance(Locale.CHINA);
            Collections.sort(teacherBasicInfoList, (o1, o2) -> {
                Date date1 = SafeConverter.toDate(o1.getSubjects().get(0).getKpiData().get("lastVisitTime"));
                Date date2 = SafeConverter.toDate(o2.getSubjects().get(0).getKpiData().get("lastVisitTime"));
                return date2.compareTo(date1);
            });
        }
        return mapMessage.add("teacherList", teacherBasicInfoList);
    }

    /**
     * 查询潜力值老师列表
     *
     * @return
     */
    @RequestMapping(value = "potentialTeacherList.vpage")
    @ResponseBody
    public MapMessage potentialTeacherList() {
        Integer potentialType = requestInteger("potentialType");
        Long schoolId = requestLong("schoolId");
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("无效的学校ID:{}", schoolId);
        }
        return MapMessage.successMessage().add("teacherList", teacherResourceService.loadPotentialTeachers(schoolId, potentialType));
    }

    /**
     * 查询未认证学生认证待达成条件
     *
     * @return
     */
    @RequestMapping(value = "getStudentUnAuthConditions.vpage")
    @ResponseBody
    public MapMessage getStudentUnAuthConditions() {
        Long studentId = requestLong("studentId");
        return teacherResourceService.getStudentUnAuthConditions(studentId);
    }

    /**
     * 根据老师id获取老师手机号
     *
     * @return
     */
    @RequestMapping(value = "get_teacher_mobile.vpage")
    @ResponseBody
    public MapMessage getTeacherMobile() {
        Long teacherId = requestLong("teacherId");
        //公私海场景，判断该用户是否有权限操作，若无权限，返回老师负责人员
        MapMessage mapMessage = teacherResourceService.teacherAuthorityMessage(getCurrentUserId(), teacherId, SearchService.SCENE_SEA);
        if (!mapMessage.isSuccess()) {
            if (StringUtils.isNotBlank(ConversionUtils.toString(mapMessage.get("teacherManager")))) {
                return MapMessage.errorMessage(StringUtils.formatMessage("该老师由{}负责，暂无操作权限", mapMessage.get("teacherManager")));
            } else {
                return MapMessage.errorMessage(StringUtils.formatMessage("暂无操作权限"));
            }
        }
        return teacherResourceService.getTeacherMobile(teacherId);
    }


}
