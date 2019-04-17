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
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.ObjectUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.resource.GradeDetailAndIfKlxSubjectLeaderInfo;
import com.voxlearning.utopia.agent.constants.SchoolQuizBankAdministratorOperationType;
import com.voxlearning.utopia.agent.mapper.ClazzAlterMapper;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.mobile.AgentHiddenTeacherService;
import com.voxlearning.utopia.agent.service.mobile.AgentTargetTagService;
import com.voxlearning.utopia.agent.service.mobile.TeacherFakeService;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.ReviewStatus;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.entity.TeacherRoles;
import com.voxlearning.utopia.entity.crm.CrmGroupSummary;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.business.consumer.CertificationManagementClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.constants.TeacherRoleCategory;
import com.voxlearning.utopia.service.user.api.constants.TeacherRolesType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.TeacherRolesServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * 用于转化实体，包装之用
 *
 * @author Yuechen.Wang
 * @since 2016-09-26
 */
@Named
public class AgentResourceMapperService extends AbstractAgentService {

    private static final String MIDDLE_SCHOOL_HOMEWORK_COUNT_API_TEST = "http://zx2.test.17zuoye.net/extend/teacherHomework";
    private static final String MIDDLE_SCHOOL_HOMEWORK_COUNT_API_STAGING = "http://zx.staging.17zuoye.net/extend/teacherHomework";
    private static final String MIDDLE_SCHOOL_HOMEWORK_COUNT_API_RELEASE = "http://zx.17zuoye.com/extend/teacherHomework";
    private static final String TEACHER_DEFAKE_NOTE = "【管理员解除判假】";

    private static final Map<String, Integer> CharacterMap = new HashMap<String, Integer>() {
        {
            put("零", 0);
            put("一", 1);
            put("二", 2);
            put("三", 3);
            put("四", 4);
            put("五", 5);
            put("六", 6);
            put("七", 7);
            put("八", 8);
            put("九", 9);
        }
    };

    private static final String SCALE = "规模";
    private static final String MONTH_ACTIVE = "单活";
    private static final String MATH_SCAN = "数扫";

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Inject private TeacherFakeService teacherFakeService;

    @Inject private DeprecatedSchoolServiceClient deprecatedSchoolServiceClient;
    @Inject private AgentTargetTagService agentTargetTagService;
    @Inject private CertificationManagementClient certificationManagementClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private SpecialTeacherLoaderClient specialTeacherLoaderClient;
    @Inject private SpecialTeacherServiceClient specialTeacherServiceClient;
    @Inject private ResearchStaffLoaderClient researchStaffLoaderClient;
    @Inject private AgentHiddenTeacherService agentHiddenTeacherService;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private TeacherRolesServiceClient teacherRolesServiceClient;

    @Inject private RaikouSDK raikouSDK;

    private Map<String, Object> mapClazzInfo(CrmGroupSummary group) {
        Map<String, Object> groupInfo = new HashMap<>();
        groupInfo.put("gid", group.getGroupId());
        groupInfo.put("cid", group.getClazzId());
        groupInfo.put("cname", group.formalizeClazzName());
        return groupInfo;
    }

    public Map<String, Object> mapClazzDetail(CrmGroupSummary group) {
        if (group == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> groupInfo = mapClazzInfo(group);
        groupInfo.put("totalStuCnt", SafeConverter.toInt(group.getRegStuCount()));
        return groupInfo;
    }

    public List<ClazzAlterMapper> mapClazzAlteration(List<ClazzAlterMapper> records, Map<Long, CrmTeacherSummary> teacherSummaryMap) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        //=======加载不在summary中的老师
        Set<Long> applicantTeacherIds = records.stream().map(p -> p.getApplicantId()).collect(Collectors.toSet());
        Set<Long> respondentTeacherIds = records.stream().map(p -> p.getRespondentId()).collect(Collectors.toSet());
        Set<Long> notInSummaryTeacherIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(applicantTeacherIds)) {
            applicantTeacherIds.removeAll(teacherSummaryMap.keySet());
            notInSummaryTeacherIds.addAll(applicantTeacherIds);
        }
        if (CollectionUtils.isNotEmpty(respondentTeacherIds)) {
            respondentTeacherIds.removeAll(teacherSummaryMap.keySet());
            notInSummaryTeacherIds.addAll(respondentTeacherIds);
        }
        Map<Long, Teacher> teacherMap = new HashMap<>();
        AgentResourceService.batchIds(notInSummaryTeacherIds, 500).forEach((k, v) -> {
            Map<Long, Teacher> teacherMapTemp = teacherLoaderClient.loadTeachers(v);
            teacherMap.putAll(teacherMapTemp);
        });

        Set<Long> clazzIdSet = records.stream().map(c -> SafeConverter.toLong(c.getClazzId())).collect(Collectors.toSet());
        Map<Long, Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzsIncludeDisabled(clazzIdSet)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Set<Long> schoolIds = records.stream().map(ClazzAlterMapper::getSchoolId).filter(Objects::nonNull).collect(toSet());
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader().loadSchools(schoolIds).getUninterruptibly();
        // 返回值
        List<ClazzAlterMapper> result = new ArrayList<>();

        for (ClazzAlterMapper record : records) {

            Long applicantId = SafeConverter.toLong(record.getApplicantId());
            // Applicant Information
            CrmTeacherSummary applicant = teacherSummaryMap.get(applicantId);
            if (applicant == null) {
                Teacher applicantTeacher = teacherMap.get(applicantId);
                if (null == applicantTeacher) {
                    continue;
                }
                record.setApplicantName(applicantTeacher.fetchRealname());
                record.setApplicantSubject(applicantTeacher.getSubject() == null ? "" : applicantTeacher.getSubject().getValue());
                record.setApplicantAuthState(applicantTeacher.getAuthenticationState());

            } else {
                record.setApplicantName(applicant.getRealName());
                record.setApplicantSubject(applicant.getSubjectValue());
                record.setApplicantAuthState(applicant.getAuthState());
            }


            // Respondent Information
            Long respondentId = SafeConverter.toLong(record.getRespondentId());
            CrmTeacherSummary respondent = teacherSummaryMap.get(respondentId);
            if (respondent == null) {
                Teacher respondentTeacher = teacherMap.get(respondentId);
                if (null == respondentTeacher) {
                    continue;
                }
                record.setRespondentName(respondentTeacher.fetchRealname());
                record.setRespondentSubject(respondentTeacher.getSubject() == null ? "" : respondentTeacher.getSubject().getValue());
                record.setRespondentAuthState(respondentTeacher.getAuthenticationState());
            } else {
                record.setRespondentName(respondent.getRealName());
                record.setRespondentSubject(respondent.getSubjectValue());
                record.setRespondentAuthState(respondent.getAuthState());
            }

            // Clazz Information
            Long clazzId = SafeConverter.toLong(record.getClazzId());
            Clazz clazzInfo = clazzList.get(clazzId);
            // avoid NPE, 如果班级信息为空，跳过该条记录
            if (clazzInfo == null) {
                continue;
            }
            record.setClazzName(clazzInfo.formalizeClazzName());
            if (schoolMap.containsKey(record.getSchoolId())) {
                record.setSchoolName(schoolMap.get(record.getSchoolId()).loadSchoolFullName());
            }
            record.setAvailable(true);
            result.add(record);
        }
        result.sort((o1, o2) -> {
           /* int compare = Integer.compare(o1.getOrderIndex(), o2.getOrderIndex());
            if (compare != 0) return compare;*/
            return Long.compare(o2.getCreateTimeLong(), o1.getCreateTimeLong());
        });
        return result;
    }

    public List<Map<String, Object>> generateTeacherClazzDataFromGroup(Long teacherId) {

        //实时获取老师班级情况
        List<GroupTeacherMapper> groupList = new ArrayList<>();
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(Collections.singletonList(teacherId), false);
        teacherGroups.forEach((tid, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(tid)) { // 过滤出有效的组
                // 分组id
                groupList.add(group);
            }
        }));

        if (CollectionUtils.isEmpty(groupList)) {
            return Collections.emptyList();
        }
        List<Long> clazzIdList = groupList.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toList());
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzsIncludeDisabled(clazzIdList)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupTeacherMapper groupMapper : groupList) {
            Long clazzId = groupMapper.getClazzId();
            Clazz clazz = clazzMap.get(clazzId);
            if (clazz == null) {
                continue;
            }
            ClazzLevel clazzLevel = clazz.getClazzLevel();
            if (clazzLevel == null || clazzLevel == ClazzLevel.PRIMARY_GRADUATED || clazzLevel == ClazzLevel.MIDDLE_GRADUATED) {//过滤 已经毕业的 班级信息
                continue;
            }
            Map<String, Object> clazzInfo = new HashMap<>();

            clazzInfo.put("cid", groupMapper.getClazzId());
            clazzInfo.put("cname", clazz.formalizeClazzName());
            clazzInfo.put("clevel", clazzLevel.getLevel());
            result.add(clazzInfo);
        }
        if (CollectionUtils.isNotEmpty(result)) {
            Collator collator = Collator.getInstance(Locale.CHINA);
            result.sort((o1, o2) -> {
                int lv1 = SafeConverter.toInt(o1.get("clevel"));
                int lv2 = SafeConverter.toInt(o2.get("clevel"));
                if (lv1 != lv2) return Integer.compare(lv1, lv2);
                String name1 = StringUtils.defaultString(SafeConverter.toString(o1.get("cname")));
                String name2 = StringUtils.defaultString(SafeConverter.toString(o2.get("cname")));
                if (StringUtils.isNotBlank(name1) && StringUtils.isNotBlank(name2)) {
                    return collator.compare(name1, name2);
                } else if (StringUtils.isBlank(name1) && StringUtils.isNotBlank(name2)) {
                    return 1;
                } else if (StringUtils.isNotBlank(name1) && StringUtils.isBlank(name2)) {
                    return -1;
                }
                return 0;
            });
        }
        return result;
    }


    /**
     * 取消判假流程
     * CrmTeacherSummaryService 迁移
     *
     * @param teacherId
     * @param currentUser
     * @return
     */
    public CrmTeacherFake defakeTeacher(Long teacherId, AuthCurrentUser currentUser) {
        if (teacherId == null || currentUser == null) {
            return null;
        }
        CrmTeacherFake teacherFake = loadFakedTeacher(teacherId);
        if (teacherFake == null) {
            return null;
        }
        teacherFake.setReviewStatus(ReviewStatus.REJECT);
        teacherFake.setReviewer(currentUser.getUserName());
        teacherFake.setReviewerName(currentUser.getRealName());
        teacherFake.setReviewNote(TEACHER_DEFAKE_NOTE);
        teacherFake.setReviewTime(new Date());
        teacherFakeService.update(teacherFake.getId(), teacherFake);
        return teacherFake;
    }

    public CrmTeacherFake loadFakedTeacher(Long teacherId) {
        if (teacherId == null) {
            return null;
        }
        List<CrmTeacherFake> fakedTeachers = teacherFakeService.findFakedTeacher(teacherId);
        return CollectionUtils.isEmpty(fakedTeachers) ? null : fakedTeachers.get(0);
    }


    /**
     * 校本题库管理员变更
     *
     * @param teacherId
     * @param schoolId
     */
    public MapMessage changeSchoolQuizBankAdministrator(Long teacherId, Long schoolId, AuthCurrentUser authCurrentUser) {
        //数据校验 暂时通过数据校验的方式实现获取必要数据 若果效率有影响可以去掉数据校验 以直接获取数据方式处理
        MapMessage mapMessage = permissionSchoolQuizBankAdministrator(teacherId, schoolId);
        if (!mapMessage.getSuccess()) {
            return mapMessage;
        }
        //校验数据是否为变更操作
        if (mapMessage.get("schoolQuizBankAdministratorType") != null && ObjectUtils.equals(mapMessage.get("schoolQuizBankAdministratorType"), SchoolQuizBankAdministratorOperationType.OTHEREXISTENCE)) {
            Long originalTeacherId = Long.valueOf(mapMessage.get("originalTeacherId").toString());
            mapMessage = deprecatedSchoolServiceClient.getRemoteReference().disableSchoolQuizBankAdministrator(originalTeacherId);
            if (mapMessage.getSuccess()) {//删除相关的操作日志
                saveUserServiceRecordMapper(originalTeacherId, authCurrentUser, UserServiceRecordOperationType.校本题库管理员变更, "DELETE", "老师[" + originalTeacherId + "]撤销校本本题库管理员身份");
            }
        }
        mapMessage = deprecatedSchoolServiceClient.getRemoteReference().addSchoolQuizBankAdministrator(teacherId, schoolId);
        if (mapMessage.getSuccess()) {
            saveUserServiceRecordMapper(teacherId, authCurrentUser, UserServiceRecordOperationType.校本题库管理员变更, "ADD", "添加老师[" + teacherId + "]为校本本题库管理员身份");
        }

        return mapMessage;
    }

    /**
     * 是否可以添加 校本本题库管理员
     *
     * @return
     */
    public MapMessage permissionSchoolQuizBankAdministrator(Long teacherId, Long schoolId) {
        MapMessage mapMessage = schoolDirectlyResponsiblePerson(teacherId, schoolId, "testLibrarian");
        if (!mapMessage.getSuccess()) {
            return mapMessage;
        }
        //以用户操作日志中 添加日期为准
        List<TeacherRoles> bankManagers = teacherRolesServiceClient.getTeacherRolesService().loadSchoolRoleTeachers(schoolId, TeacherRolesType.SCHOOL_BANK_MANAGER.name());
        if (CollectionUtils.isEmpty(bankManagers)) {
            mapMessage.add("schoolQuizBankAdministratorType", SchoolQuizBankAdministratorOperationType.NOTEXIST);
        } else {
            Set<Long> teacherIdSet = bankManagers.stream().map(TeacherRoles::getUserId).collect(Collectors.toSet());
            if (teacherIdSet.contains(teacherId)) {
                mapMessage = MapMessage.errorMessage("当前老师已经是校本题库管理员");
                mapMessage.add("schoolQuizBankAdministratorType", SchoolQuizBankAdministratorOperationType.ALREADY);
                return mapMessage;
            }
            Set<Long> teacherIdAllSet = teacherIdSet;
            teacherIdAllSet.add(teacherId);
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIdAllSet);
            Teacher teacher = teacherMap.get(teacherId);
            if (teacher.getSubject() == null) {
                return MapMessage.errorMessage("老师无学科，暂不能操作").add("schoolQuizBankAdministratorType", SchoolQuizBankAdministratorOperationType.NOTEXISTSUBJECT);//其他错误
            }
            //获取当前学校下所有 校本题库管理员变更 记录
            List<UserServiceRecord> surList = userLoaderClient.loadUserServiceRecordByTypeAndUserIds(UserServiceRecordOperationType.校本题库管理员变更.name(), teacherIdSet);
            Map<Long, List<UserServiceRecord>> surMap = null;
            if (CollectionUtils.isNotEmpty(surList)) {
                surMap = surList.stream().filter(p -> p.getOperationContent().equals("ADD")).collect(groupingBy(UserServiceRecord::getUserId));//待测试 分组之后 排序是否正常
            }
            boolean exitSubject = false;//是否存在此学科
            for (TeacherRoles bankManager : bankManagers) {
                Teacher tempTeacher = teacherMap.get(bankManager.getUserId());
                if (ObjectUtils.equals(teacher.getSubject(), tempTeacher.getSubject())) {
                    exitSubject = true;
                    List<UserServiceRecord> usrList = MapUtils.isEmpty(surMap) ? null : surMap.get(bankManager.getUserId());
                    if (CollectionUtils.isNotEmpty(usrList) && usrList.get(0).getCreateTime().after(DateUtils.addMonths(new Date(), -1))) {
                        mapMessage = MapMessage.errorMessage("该学校30天内已设置校本题库管理员" + tempTeacher.fetchRealname() + "老师，如需变更请联系销运负责人");//暂时这样子处理
                        mapMessage.add("otherExistenceTeacherName", tempTeacher.fetchRealname());
                        mapMessage.add("schoolQuizBankAdministratorType", SchoolQuizBankAdministratorOperationType.ONLYBEMODIFIEDONCEAMONTH);
                    } else {
                        mapMessage.add("otherExistenceTeacherName", tempTeacher.fetchRealname());
                        mapMessage.add("schoolQuizBankAdministratorType", SchoolQuizBankAdministratorOperationType.OTHEREXISTENCE);
                        mapMessage.add("originalTeacherId", bankManager.getUserId());
                    }
                    break;
                }
            }
            if (!exitSubject) {//说明没有此科目的校本题库管理员
                mapMessage.add("schoolQuizBankAdministratorType", SchoolQuizBankAdministratorOperationType.NOTEXIST);
            }

        }
        return mapMessage;
    }

    /**
     * 是否是学校直属负责人
     * 已经没有那个意思了~，现在是检查学校是否可以设置学校题库管理员和学科组长
     * headMan 学科组长
     * testLibrarian 校本题库管理员
     *
     * @return
     */
    public MapMessage schoolDirectlyResponsiblePerson(Long teacherId, Long schoolId, String type) {
        if (schoolId == null || teacherId == 0L) {
            return MapMessage.errorMessage("老师未找到学校");
        }
        if (teacherLoaderClient.isFakeTeacher(teacherId)) {
            return MapMessage.errorMessage("当前为假老师.不能操作");//待补充
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if ("testLibrarian".equals(type) && (schoolExtInfo == null || !(schoolExtInfo.isScanMachineFlag() && schoolExtInfo.isQuestionBankFlag()))) {
            return MapMessage.errorMessage("学校" + schoolId + "必须开通阅卷机权限和校本题库权限");
        }

        if ("headMan".equals(type) && (schoolExtInfo == null || !schoolExtInfo.isScanMachineFlag())) {
            return MapMessage.errorMessage("学校" + schoolId + "必须开通阅卷机权限");
        }
        return MapMessage.successMessage();
    }

    // 更换学科组长
    public MapMessage changeSubjectLeader(Long teacherId, String clazzLevelStr, String comments) {
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }

        List<ClazzLevel> clazzLevels = new ArrayList<>();
        List<ClazzLevel> managedClassLevels = new ArrayList<>();
        if (StringUtils.isNotBlank(clazzLevelStr)) {
            String[] clazzLevelArr = clazzLevelStr.split(",");
            List<ClazzLevel> tmpClazzLevels = Arrays.asList(clazzLevelArr).stream().map(p -> ClazzLevel.parse(Integer.valueOf(p))).collect(toList());
            if (CollectionUtils.isNotEmpty(tmpClazzLevels)) {
                clazzLevels.addAll(tmpClazzLevels);
            }
        }

        TeacherRoles subjectLeader = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherId)
                .stream()
                .filter(p -> Objects.equals(p.fetchTeacherRolesTypeEnum(), TeacherRolesType.SUBJECT_LEADER))
                .findAny()
                .orElse(null);
        if (subjectLeader != null && StringUtils.isNotBlank(subjectLeader.getRoleContent())) {
            managedClassLevels.addAll(JsonUtils.fromJsonToList(subjectLeader.getRoleContent(), ClazzLevel.class));
        }

        boolean isSameData = isSameData(clazzLevels, managedClassLevels);
        if (isSameData) {
            return MapMessage.successMessage();
        }

        MapMessage message;
        if (CollectionUtils.isEmpty(clazzLevels)) {
            message = teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.SUBJECT_LEADER.name());
        } else {
            message = teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.SUBJECT_LEADER.name(),
                    JsonUtils.toJson(clazzLevels));
        }
        if (message.isSuccess()) {
            saveUserServiceRecordMapper(teacherId, getCurrentUser(), UserServiceRecordOperationType.学科组长变更, comments, CollectionUtils.isNotEmpty(clazzLevels) ? "UPDATE" : "ELIMINATE");
        }
        return message;
    }

    private boolean isSameData(List<ClazzLevel> c1, List<ClazzLevel> c2) {
        boolean result = false;
        if (CollectionUtils.isEmpty(c1) && CollectionUtils.isEmpty(c2)) {
            result = true;
        } else if (CollectionUtils.isNotEmpty(c1) && CollectionUtils.isNotEmpty(c2)) {
            if (c1.size() == c2.size()) {
                result = !c1.stream().anyMatch(p -> !c2.contains(p));
            }
        }
        return result;
    }


    /**
     * 保存用户操作日志
     *
     * @param teacherId
     * @param authCurrentUser
     * @param userServiceRecordOperationType
     * @param operationContent
     * @return
     */
    private MapMessage saveUserServiceRecordMapper(Long teacherId, AuthCurrentUser authCurrentUser, UserServiceRecordOperationType userServiceRecordOperationType, String operationContent, String comments) {
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setUserName(teacher.fetchRealname());
        userServiceRecord.setOperatorId(String.valueOf(authCurrentUser.getUserId()));
        userServiceRecord.setOperatorName(authCurrentUser.getRealName());
        userServiceRecord.setOperationType(userServiceRecordOperationType.name());
        userServiceRecord.setOperationContent(operationContent);
        userServiceRecord.setComments(comments);
        return userServiceClient.saveUserServiceRecord(userServiceRecord);
    }

    /**
     * 学校的年级分布及该老师的学科组长身份负责的年级信息
     *
     * @param teacherId
     * @param schoolId
     * @return
     */
    public List<GradeDetailAndIfKlxSubjectLeaderInfo> findGradeDetailAndIfKlxSubjectLeader(Long teacherId, Long schoolId) {
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (null == teacher) {
            return new ArrayList<>();
        }
        EduSystemType eduSystemType = EduSystemType.S4;
        if (teacher.isJuniorTeacher()) {
            eduSystemType = EduSystemType.J4;
        }

        TeacherRoles subjectLeader = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherId).stream()
                .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SUBJECT_LEADER.name()))
                .findAny().orElse(null);
        List<ClazzLevel> subjectClazzLevels = new ArrayList<>();
        if (subjectLeader != null && StringUtils.isNoneBlank(subjectLeader.getRoleContent())) {
            subjectClazzLevels = JsonUtils.fromJsonToList(subjectLeader.getRoleContent(), ClazzLevel.class);
        }

        List<GradeDetailAndIfKlxSubjectLeaderInfo> tgkslList = new ArrayList<>();
        for (String level : eduSystemType.getCandidateClazzLevel().split(",")) {
            ClazzLevel clazzLevel = ClazzLevel.parse(Integer.parseInt(level));
            GradeDetailAndIfKlxSubjectLeaderInfo tgksl = new GradeDetailAndIfKlxSubjectLeaderInfo();
            tgksl.setClazzLevel(clazzLevel);
            tgksl.setClazzDescription(clazzLevel != null ? clazzLevel.getDescription() : "");
            tgksl.setIfKlxSubjectLeader(subjectClazzLevels.contains(clazzLevel));
            tgksl.setLevel(clazzLevel.getLevel());
            tgkslList.add(tgksl);
        }
        return tgkslList;
    }

    /**
     * 获取快乐学老师职责数据
     *
     * @param teacherId
     * @param schoolId
     * @return
     */
    public MapMessage getKlxDutyData(Long teacherId, Long schoolId) {
        if (teacherId == 0L) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的老师ID:{}", teacherId));
        }
        if (schoolId == 0L) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }


        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();

        if (null == school) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的学校ID:{}", schoolId));
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (null == teacherDetail) {
            return MapMessage.errorMessage(StringUtils.formatMessage("无效的老师ID:{}", teacherId));
        }

        Map<String, Object> resultMap = new HashMap<>();

        if (teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher()) {

            // 教务老师
            boolean isAffairTeacher = false;
            if (school.isJuniorSchool() || school.isSeniorSchool()) {
                List<User> schoolAffairTeachers = specialTeacherLoaderClient.findSchoolAffairTeachers(school.getId());
                if (CollectionUtils.isNotEmpty(schoolAffairTeachers)) {
                    String phone = sensitiveUserDataServiceClient.loadUserMobile(teacherId);
                    if (StringUtils.isNoneBlank(phone)) {
                        for (int i = 0; i < schoolAffairTeachers.size(); i++) {
                            User user = schoolAffairTeachers.get(i);
                            String userPhone = sensitiveUserDataServiceClient.loadUserMobile(user.getId());
                            if (StringUtils.isNoneBlank(userPhone) && Objects.equals(userPhone, phone)) {
                                isAffairTeacher = true;
                                break;
                            }
                        }
                    }
                }
            }
            resultMap.put("isAffairTeacher", isAffairTeacher);

            List<TeacherRoles> teacherRoles = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherDetail.getId());

            //校本题库管理员
            TeacherRoles schoolBankManager = teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SCHOOL_BANK_MANAGER.name()))
                    .findAny().orElse(null);
            if (schoolBankManager != null) {
                resultMap.put("schoolQuizBankAdministrator", true);
            }

            //是不是考试管理员
            TeacherRoles examManager = teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.EXAM_MANAGER.name()))
                    .findAny().orElse(null);
            resultMap.put("isExamManager", examManager != null);

            //学科组长
            Map<String, Object> subjectLeaderMap = new HashMap<>();
            TeacherRoles subjectLeader = teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SUBJECT_LEADER.name()))
                    .findAny().orElse(null);
            subjectLeaderMap.put("subjectLeaderFlag", subjectLeader != null);
            if (subjectLeader != null && StringUtils.isNoneBlank(subjectLeader.getRoleContent())) {
                List<ClazzLevel> clazzLevels = JsonUtils.fromJsonToList(subjectLeader.getRoleContent(), ClazzLevel.class);
                if (CollectionUtils.isNotEmpty(clazzLevels)) {
                    List<String> clazzLevelStrList = new HashSet<>(clazzLevels)
                            .stream().sorted(Comparator.comparingInt(o -> o.getLevel()))
                            .map(ClazzLevel::getDescription)
                            .collect(Collectors.toList());
                    subjectLeaderMap.put("clazzLevelStr", StringUtils.join(clazzLevelStrList, ","));
                }
            }
            resultMap.put("subjectLeaderMap", subjectLeaderMap);

            // 班主任
            Map<String, Object> classManagerMap = new HashMap<>();
            TeacherRoles classManager = teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.CLASS_MANAGER.name()))
                    .findAny().orElse(null);
            classManagerMap.put("classManagerFlag", classManager != null);
            if (classManager != null && StringUtils.isNoneBlank(classManager.getRoleContent())) {
                List<Long> managedClassIds = JsonUtils.fromJsonToList(classManager.getRoleContent(), Long.class);
                Collection<Clazz> managedClassList = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazzs(managedClassIds);
                List<String> managedClassNameList = managedClassList.stream().map(Clazz::formalizeClazzName).collect(Collectors.toList());
                classManagerMap.put("managedClassStr", StringUtils.join(managedClassNameList, ","));
            }
            resultMap.put("classManagerMap", classManagerMap);

            // 年级主任
            Map<String, Object> gradeManagerMap = new HashMap<>();
            TeacherRoles gradeManager = teacherRoles.stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.GRADE_MANAGER.name()))
                    .findAny().orElse(null);
            gradeManagerMap.put("gradeManagerFlag", gradeManager != null);
            if (gradeManager != null && StringUtils.isNoneBlank(gradeManager.getRoleContent())) {
                List<Integer> gradeList = JsonUtils.fromJsonToList(gradeManager.getRoleContent(), Integer.class);
                if (CollectionUtils.isNotEmpty(gradeList)) {
                    EduSystemType est = EduSystemType.S4;
                    if (teacherDetail.isJuniorTeacher()) {
                        est = EduSystemType.J4;
                    }
                    List<String> managedGradeList = new ArrayList<>();
                    for (Integer jie : gradeList) {
                        ClazzLevel clazzLevel = ClassJieHelper.toClazzLevel(jie, est);
                        if (clazzLevel.getLevel() >= ClazzLevel.SIXTH_GRADE.getLevel() && clazzLevel.getLevel() <= ClazzLevel.SENIOR_THREE.getLevel()) {
                            managedGradeList.add(clazzLevel.getDescription());
                        }
                    }
                    gradeManagerMap.put("managedGradeStr", StringUtils.join(managedGradeList, ","));
                }
            }
            resultMap.put("gradeManagerMap", gradeManagerMap);

            // 是不是中学校长
            TeacherRoles schoolMaster = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherDetail.getId()).stream()
                    .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.SCHOOL_MASTER.name()))
                    .findAny().orElse(null);
            resultMap.put("isSchoolMaster", schoolMaster != null);
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.putAll(resultMap);
        return mapMessage;
    }

    public MapMessage getGradeManagerList(Long teacherId) {
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }
        EduSystemType eduSystemType = EduSystemType.S4;
        if (teacher.isJuniorTeacher()) {
            eduSystemType = EduSystemType.J4;
        }

        List<Integer> managedGradeJies = new ArrayList<>();

        TeacherRoles gradeManager = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherId)
                .stream()
                .filter(p -> Objects.equals(p.getRoleType(), TeacherRolesType.GRADE_MANAGER.name()))
                .findAny()
                .orElse(null);

        if (gradeManager != null) {
            managedGradeJies = JsonUtils.fromJsonToList(gradeManager.getRoleContent(), Integer.class);
        }

        Set<ClazzLevel> managedGradeList = new HashSet<>();
        for (Integer jie : managedGradeJies) {
            ClazzLevel clazzLevel = ClassJieHelper.toClazzLevel(jie, eduSystemType);
            if (clazzLevel.getLevel() < ClazzLevel.SIXTH_GRADE.getLevel() || clazzLevel.getLevel() > ClazzLevel.SENIOR_THREE.getLevel()) {
                continue;
            }
            managedGradeList.add(clazzLevel);
        }

        List<Map<String, Object>> gradeList = new ArrayList<>();
        for (String level : eduSystemType.getCandidateClazzLevel().split(",")) {
            ClazzLevel clazzLevel = ClazzLevel.parse(Integer.parseInt(level));
            gradeList.add(
                    MapUtils.m("value", clazzLevel.getLevel(),
                            "text", clazzLevel.getDescription(),
                            "selected", managedGradeList.contains(clazzLevel)
                    )
            );
        }
        return MapMessage.successMessage().add("gradeList", gradeList);
    }

    public MapMessage setSchoolGradeManager(Long teacherId, String clazzLevels, String desc, AuthCurrentUser authCurrentUser) {
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }

        EduSystemType eduSystemType = teacher.isJuniorTeacher() ? EduSystemType.J4 : EduSystemType.S4;
        List<ClazzLevel> levels = Stream.of(clazzLevels.split(","))
                .filter(level -> eduSystemType.getCandidateClazzLevel().contains(level))
                .map(t -> ClazzLevel.parse(SafeConverter.toInt(t)))
                .filter(Objects::nonNull)
                .collect(toList());
        Set<Integer> jies = levels.stream()
                .map(ClassJieHelper::fromClazzLevel)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(levels)) {
            teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.GRADE_MANAGER.name(),
                    JsonUtils.toJson(jies));
        } else {
            teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.GRADE_MANAGER.name());
        }


        String comments = "设置年级主任[" + StringUtils.join(levels.stream().map(ClazzLevel::getDescription).collect(toList()), ",") + "]，说明[" + desc + "]";
        saveUserServiceRecordMapper(teacherId, authCurrentUser, UserServiceRecordOperationType.用户信息变更, "设置年级主任", comments);
        return MapMessage.successMessage();
    }

    public MapMessage getClassManagerList(Long teacherId) {
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(teacher.getTeacherSchoolId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("无效的学校");
        }

        Map<ClazzLevel, List<Clazz>> gradeMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .enabled()
                .toList()
                .stream()
                .filter(c -> c.getClazzLevel() != null)
                .collect(Collectors.groupingBy(Clazz::getClazzLevel));

        if (MapUtils.isEmpty(gradeMap)) {
            return MapMessage.errorMessage("该学校还没有创建班级");
        }

        List<Long> clazzIds = new ArrayList<>();

        TeacherRoles classManager = teacherRolesServiceClient.getTeacherRolesService().loadTeacherRoles(teacherId)
                .stream()
                .filter(p -> Objects.equals(p.fetchTeacherRolesTypeEnum(), TeacherRolesType.CLASS_MANAGER))
                .findAny()
                .orElse(null);
        if (classManager != null && !Objects.equals("null", classManager.getRoleContent())) {
            List<Long> managedClazzIds = JsonUtils.fromJsonToList(classManager.getRoleContent(), Long.class);
            if (CollectionUtils.isNotEmpty(managedClazzIds)) {
                clazzIds.addAll(managedClazzIds);
            }
        }

        List<Map<String, Object>> gradeList = new LinkedList<>();
        for (Map.Entry<ClazzLevel, List<Clazz>> entry : gradeMap.entrySet()) {
            ClazzLevel grade = entry.getKey();
            if (grade == null || grade.getLevel() > 80) {
                continue;
            }
            Map<String, Object> gradeInfo = new HashMap<>();
            gradeInfo.put("grade", grade.getDescription());
            gradeInfo.put("level", grade.getLevel());

            List<Map<String, Object>> classList = entry.getValue()
                    .stream()
                    .sorted((c1, c2) -> {
                        Long n1 = c1.getId();
                        Long n2 = c2.getId();
                        return n1.compareTo(n2);
                    })
                    .map(c -> {
                        Map<String, Object> classInfo = new HashMap<>();
                        classInfo.put("classId", c.getId());
                        classInfo.put("className", c.getClassName());
                        classInfo.put("fullName", c.formalizeClazzName());
                        classInfo.put("selected", clazzIds.contains(c.getId()));
                        return classInfo;
                    }).collect(Collectors.toList());
            gradeInfo.put("classList", classList);
            int selectNum = 0;
            for (Map<String, Object> map : classList) {
                if (SafeConverter.toBoolean(map.get("selected"))) {
                    selectNum++;
                }
            }
            gradeInfo.put("selectNum", selectNum);
            gradeList.add(gradeInfo);
        }

        gradeList.sort(Comparator.comparingInt(g -> ClazzLevel.parse(SafeConverter.toInt(g.get("level"))).ordinal()));

        List<Map<String, Object>> teacherClass = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .sorted((c1, c2) -> {
                    int lv1 = c1.getClazzLevel() != null ? c1.getClazzLevel().getLevel() : 100;
                    int lv2 = c2.getClazzLevel() != null ? c2.getClazzLevel().getLevel() : 100;
                    if (lv1 != lv2) {
                        return Integer.compare(lv1, lv2);
                    }
                    Long n1 = c1.getId();
                    Long n2 = c2.getId();
                    return n1.compareTo(n2);
                })
                .map(c -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("classId", c.getId());
                    info.put("fullName", c.formalizeClazzName());
                    return info;
                }).collect(toList());


        return MapMessage.successMessage().add("gradeList", gradeList)
                .add("teacherClass", teacherClass);
    }

    public MapMessage setClassManager(Long teacherId, String classIds, String desc, AuthCurrentUser authCurrentUser) {
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null || teacher.isDisabledTrue()) {
            return MapMessage.errorMessage("无效的老师信息");
        }

        Set<Long> classIdSet = null;
        if (StringUtils.isNotBlank(classIds)) {
            classIdSet = Stream.of(classIds.trim().split(","))
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toSet());
        }

        if (CollectionUtils.isNotEmpty(classIdSet)) {
            teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.CLASS_MANAGER.name(),
                    JsonUtils.toJson(classIdSet));
        } else {
            teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(teacherId,
                    teacher.getTeacherSchoolId(),
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.CLASS_MANAGER.name());
        }
        String comments = "设置班主任[" + classIds + "]，说明[" + desc + "]";
        saveUserServiceRecordMapper(teacherId, authCurrentUser, UserServiceRecordOperationType.用户信息变更, "设置班主任", comments);
        return MapMessage.successMessage();
    }

    public MapMessage createAffairTeacher(Long teacherId, Long schoolId, AuthCurrentUser authCurrentUser) {
        if (null == teacherId || teacherId == 0L) {
            return MapMessage.errorMessage("无效的老师ID");
        }
        if (null == schoolId || schoolId == 0L) {
            return MapMessage.errorMessage("无效的学校ID");
        }
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (null == teacher) {
            return MapMessage.errorMessage("无效的老师ID");
        }
        String phone = sensitiveUserDataServiceClient.loadUserMobile(teacherId);
        if (StringUtils.isBlank(phone)) {
            return MapMessage.errorMessage("该老师没有填写手机号，无法注册教务老师");
        }
        List<UserAuthentication> userList = userLoaderClient.loadMobileAuthentications(phone);
        boolean occupied = userList.stream()
                .filter(ua -> ua.getUserType() != null)
                .anyMatch(ua -> UserType.RESEARCH_STAFF == ua.getUserType());
        if (occupied) {
            return MapMessage.errorMessage("手机号已注册教务老师号");
        }
        List<User> affairTeachers = specialTeacherLoaderClient.findSchoolAffairTeachers(schoolId);
        if (CollectionUtils.isNotEmpty(affairTeachers)) {
            List<String> msgs = new ArrayList<>();
            affairTeachers.forEach(item -> {
                StringBuilder sb = new StringBuilder();
                sb.append(item.getProfile().getRealname());
                String itemPhone = sensitiveUserDataServiceClient.showUserMobile(item.getId(), "agent:createAffairTeacher", getCurrentUser().getUserName());
                if (StringUtils.isNoneBlank(itemPhone)) {
                    sb.append("(手机号：" + itemPhone + ")");
                }
                msgs.add(sb.toString());
            });
            return MapMessage.errorMessage("目前学校已有教务老师 " + StringUtils.join(msgs, "、") + ",暂无法重新设置");
        }
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("请确认添加教务老师的学校ID：" + schoolId);
        }

        // 检查完毕，开始创建账号
        try {
            MapMessage mapMessage = specialTeacherServiceClient.createAffairTeacher(schoolId, teacher.getProfile().getRealname(), phone);
            if (mapMessage.isSuccess()) {
                String passwd = (String) mapMessage.get("defaultPassword");
                smsServiceClient.createSmsMessage(phone)
                        .content("注册成功！用手机号和密码(" + passwd + ")即可登录一起作业教务系统。如有问题，可拨打400-160-1717")
                        .type(SmsType.AFFAIR_TEACHER_ACCOUNT_NOTICE.name())
                        .send();

                saveUserServiceRecordMapper(teacherId, authCurrentUser, UserServiceRecordOperationType.用户信息变更, "设为教务主任", "将该老师设为教务主任");
            }
            return mapMessage;
        } catch (Exception ex) {
            logger.error("Failed create affair teacher account, schoolId={}, name={}, mobile={}", schoolId, teacher.getProfile().getRealname(), phone, ex);
            return MapMessage.errorMessage("创建教务老师账号失败");
        }
    }

    /**
     * 设置考试管理员
     *
     * @param schoolId
     * @param teacherId
     * @return
     */
    public MapMessage setExamManager(Long schoolId, Long teacherId, AuthCurrentUser authCurrentUser) {
        if (schoolId == 0 || teacherId == 0) {
            return MapMessage.errorMessage("参数错误 学校ID:" + schoolId + ",老师ID:" + teacherId);
        }
        User user = userLoaderClient.loadUser(teacherId);
        if (user == null) {
            return MapMessage.errorMessage("该账号" + teacherId + "不是老师或教务老师");
        }
        Long localSchoolId = null;
        if (user.fetchUserType() == UserType.TEACHER) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (teacherDetail != null) {
                localSchoolId = teacherDetail.getTeacherSchoolId();
            }
        } else if (user.fetchUserType() == UserType.RESEARCH_STAFF) {
            ResearchStaff staff = researchStaffLoaderClient.loadResearchStaff(user.getId());
            if (staff != null && staff.isAffairTeacher()) {
                ResearchStaffManagedRegion researchStaffManagedRegion = specialTeacherLoaderClient.findSchoolId(user.getId());
                if (researchStaffManagedRegion != null) {
                    localSchoolId = researchStaffManagedRegion.getManagedRegionCode();
                }
            }
        } else {
            return MapMessage.errorMessage("该账号" + teacherId + "不是老师或教务老师");
        }
        if (localSchoolId == null || !Objects.equals(localSchoolId, schoolId)) {
            return MapMessage.errorMessage("该老师" + teacherId + "学校id为" + localSchoolId + ",不在该" + schoolId + "学校内");
        }

        teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(teacherId,
                schoolId,
                TeacherRoleCategory.O2O.name(),
                TeacherRolesType.EXAM_MANAGER.name(),
                "");

        saveUserServiceRecordMapper(teacherId, authCurrentUser, UserServiceRecordOperationType.用户信息变更, "设为考试管理员", "将该老师设为考试管理员");

        return MapMessage.successMessage().add("teacherId", user.getId()).add("teacherName", user.fetchRealname());
    }

    /**
     * 取消考试管理员
     *
     * @param teacherId
     * @return
     */
    public MapMessage cancelExamManager(Long schoolId, Long teacherId, AuthCurrentUser authCurrentUser) {
        if (teacherId == 0) {
            return MapMessage.errorMessage("老师ID不能为空");
        }

        teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(teacherId,
                schoolId,
                TeacherRoleCategory.O2O.name(),
                TeacherRolesType.EXAM_MANAGER.name());

        saveUserServiceRecordMapper(teacherId, authCurrentUser, UserServiceRecordOperationType.用户信息变更, "取消考试管理员", "将该老师取消考试管理员");

        return MapMessage.successMessage();
    }


    /**
     * 设置或取消校长权限
     *
     * @param schoolId
     * @param teacherId
     * @param setSchoolMaster
     * @return
     */
    public MapMessage setSchoolMaster(Long schoolId, Long teacherId, Boolean setSchoolMaster) {
        if (schoolId == 0 || teacherId == 0) {
            return MapMessage.errorMessage("参数错误 学校ID:" + schoolId + ",老师ID:" + teacherId);
        }
        User user = userLoaderClient.loadUser(teacherId);
        if (user == null) {
            return MapMessage.errorMessage("未找到该老师：" + teacherId);
        }

        Long localSchoolId = null;
        if (user.fetchUserType() == UserType.TEACHER) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (teacherDetail != null) {
                localSchoolId = teacherDetail.getTeacherSchoolId();
            }
        } else {
            return MapMessage.errorMessage("该账号" + teacherId + "不是老师");
        }

        if (localSchoolId == null || !Objects.equals(localSchoolId, schoolId)) {
            return MapMessage.errorMessage("该老师" + teacherId + "学校id为" + localSchoolId + ",不在该" + schoolId + "学校内");
        }

        //设置为校长
        if (setSchoolMaster) {
            teacherRolesServiceClient.getTeacherRolesService().upsertTeacherRoles(teacherId,
                    schoolId,
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.SCHOOL_MASTER.name(),
                    "");
            //取消校长权限
        } else {
            teacherRolesServiceClient.getTeacherRolesService().removeTeacherRoles(teacherId,
                    schoolId,
                    TeacherRoleCategory.O2O.name(),
                    TeacherRolesType.SCHOOL_MASTER.name());
        }

        // 记录一条处理日志
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(ConversionUtils.toString(getCurrentUser().getUserId()));
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("设置中学校长");
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        return MapMessage.successMessage().add("teacherId", user.getId())
                .add("teacherName", user.fetchRealname());
    }
}
