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

package com.voxlearning.utopia.agent.service.workspace;

import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.ObjectUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.region.api.buffer.RaikouRegionBufferDelegator;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.*;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.api.constant.ClazzConstants;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.data.NeonatalClazz;
import com.voxlearning.utopia.mapper.ChangeBookMapper;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.crm.api.bean.ImportKLXStudentInfo;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.ImportKLXStudentsRecord;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.ImportKLXStudentsRecordLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.constants.AuthenticationSource;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.api.mappers.NewbieUser;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedSchoolServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2015/9/6.
 */

@Named
public class MarketToolService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private GlobalTagServiceClient globalTagServiceClient;

    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Deprecated
    @Inject private DeprecatedSchoolServiceClient deprecatedSchoolServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentDictSchoolService agentDictSchoolService;
    @Inject private ImportKLXStudentsRecordLoaderClient importKLXStudentsRecordLoaderClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;

    @Inject private RaikouSDK raikouSDK;

    private static final Map<String, String> SUBJECT_MAP = new HashMap<>();

    static {
        SUBJECT_MAP.put("1_英语", "ENGLISH");
        SUBJECT_MAP.put("1_ENGLISH", "ENGLISH");
        SUBJECT_MAP.put("1_数学", "MATH");
        SUBJECT_MAP.put("1_MATH", "MATH");
        SUBJECT_MAP.put("1_语文", "CHINESE");
        SUBJECT_MAP.put("1_CHINESE", "CHINESE");

        SUBJECT_MAP.put("2_英语", "ENGLISH");
        SUBJECT_MAP.put("2_ENGLISH", "ENGLISH");
        SUBJECT_MAP.put("2_数学", "MATH");
        SUBJECT_MAP.put("2_MATH", "MATH");
        SUBJECT_MAP.put("2_物理", "PHYSICS");
        SUBJECT_MAP.put("2_PHYSICS", "PHYSICS");
        SUBJECT_MAP.put("2_化学", "CHEMISTRY");
        SUBJECT_MAP.put("2_CHEMISTRY", "CHEMISTRY");
        SUBJECT_MAP.put("2_生物", "BIOLOGY");
        SUBJECT_MAP.put("2_BIOLOGY", "BIOLOGY");
        SUBJECT_MAP.put("2_语文", "CHINESE");
        SUBJECT_MAP.put("2_CHINESE", "CHINESE");
        SUBJECT_MAP.put("2_政治", "POLITICS");
        SUBJECT_MAP.put("2_POLITICS", "POLITICS");
        SUBJECT_MAP.put("2_地理", "GEOGRAPHY");
        SUBJECT_MAP.put("2_GEOGRAPHY", "GEOGRAPHY");
        SUBJECT_MAP.put("2_历史", "HISTORY");
        SUBJECT_MAP.put("2_HISTORY", "HISTORY");
        SUBJECT_MAP.put("2_信息", "INFORMATION");
        SUBJECT_MAP.put("2_INFORMATION", "INFORMATION");

        SUBJECT_MAP.put("4_英语", "ENGLISH");
        SUBJECT_MAP.put("4_ENGLISH", "ENGLISH");
        SUBJECT_MAP.put("4_数学", "MATH");
        SUBJECT_MAP.put("4_MATH", "MATH");
        SUBJECT_MAP.put("4_物理", "PHYSICS");
        SUBJECT_MAP.put("4_PHYSICS", "PHYSICS");
        SUBJECT_MAP.put("4_化学", "CHEMISTRY");
        SUBJECT_MAP.put("4_CHEMISTRY", "CHEMISTRY");
        SUBJECT_MAP.put("4_生物", "BIOLOGY");
        SUBJECT_MAP.put("4_BIOLOGY", "BIOLOGY");
        SUBJECT_MAP.put("4_语文", "CHINESE");
        SUBJECT_MAP.put("4_CHINESE", "CHINESE");
        SUBJECT_MAP.put("4_政治", "POLITICS");
        SUBJECT_MAP.put("4_POLITICS", "POLITICS");
        SUBJECT_MAP.put("4_地理", "GEOGRAPHY");
        SUBJECT_MAP.put("4_GEOGRAPHY", "GEOGRAPHY");
        SUBJECT_MAP.put("4_历史", "HISTORY");
        SUBJECT_MAP.put("4_HISTORY", "HISTORY");
        SUBJECT_MAP.put("4_信息", "INFORMATION");
        SUBJECT_MAP.put("4_INFORMATION", "INFORMATION");
    }

    public List<School> searchSchool(Integer regionCode, String key, Long userId) {
        if (regionCode == null || userId == null) {
            return null;
        }
        List<School> schools = null;
        if (StringUtils.isBlank(key)) {
            schools = raikouSystem.querySchoolLocations(regionCode)
                    .enabled()
                    .transform()
                    .sort(Comparator.comparing(School::getId))
                    .asList();
        } else if (NumberUtils.isNumber(key)) {
            long schoolId = SafeConverter.toLong(key);
            School school = raikouSystem.loadSchool(schoolId);
            if (school != null && regionCode.equals(school.getRegionCode())) {
                schools = Collections.singletonList(school);
            }
        } else {
            List<School> regionSchools = raikouSystem.querySchoolLocations(regionCode)
                    .enabled()
                    .transform()
                    .sort(Comparator.comparing(School::getId))
                    .asList();
            if (!regionSchools.isEmpty()) {
                schools = new ArrayList<>();
                for (School school : regionSchools) {
                    Integer regCod = school.getRegionCode();
                    String cname = school.getCname();
                    String ename = school.getEname();
                    if (regionCode.equals(regCod)) {
                        if (cname != null && cname.contains(key) || ename != null && ename.contains(key)) {
                            schools.add(school);
                        }
                    }
                }
            }
        }
        return filterSchools(schools, userId);
    }

    public Page<SchoolDetailBean> buildSchoolDetail(List<School> schools, Pageable pageable) {
        if (schools == null || schools.isEmpty()) {
            return null;
        }
        int total = schools.size();
        int size = pageable.getPageSize();
        int idx = size * pageable.getPageNumber();
        List<SchoolDetailBean> content = new ArrayList<>();
        for (int i = 0; i < size && idx < total; i++, idx++) {
            School school = schools.get(idx);
            Long schoolId = school.getId();
            SchoolType type = SchoolType.safeParse(school.getType());
            int teacherCount = crmSummaryLoaderClient.loadSchoolTeachers(schoolId).size();
            Map<Long, Teacher> schoolAmbassadors = null; //teacherLoaderClient.loadSchoolAmbassadors(schoolId); FIXME
            Collection<Teacher> ambassadors = schoolAmbassadors == null ? null : schoolAmbassadors.values();
            SchoolDetailBean detail = new SchoolDetailBean(school, type, teacherCount, ambassadors);
            content.add(detail);
        }
        return new PageImpl<>(content, pageable, total);
    }

    private List<School> filterSchools(List<School> schools, Long userId) {
        if (schools == null || schools.isEmpty()) {
            return schools;
        }
        List<School> buffer = schools.stream().filter(this::isJunior).collect(Collectors.toList());

        List<Long> managedSchoolList = baseOrgService.getManagedSchoolList(userId);
        if (CollectionUtils.isEmpty(managedSchoolList)) {
            return Collections.emptyList();
        }
        return buffer.stream().filter(p -> managedSchoolList.contains(p.getId())).collect(Collectors.toList());
    }

    private boolean isJunior(School school) {
        return school != null && isJunior(SchoolLevel.safeParse(school.getLevel()));
    }

    private boolean isJunior(SchoolLevel schoolLevel) {
        return schoolLevel == null || schoolLevel == SchoolLevel.JUNIOR;
    }

    // ===============================================================================================================

    // 批量往已有班级里面添加学生
    public MapMessage bulkAddClazzStudents(List<JoinClazzStudentInfoMapper> studentList, String webSource) {
        MapMessage validateResult = validateJoinClazzStudents(studentList);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }

        List<String> failedInfoList = new ArrayList<>();
        Map<String, Collection<NewbieUser>> studentData = new LinkedHashMap<>();
        Map<Long, List<Clazz>> teacherClazzData = new HashMap<>();

        // 以学校，班级为单位添加学生
        Map<Long, List<JoinClazzStudentInfoMapper>> schoolGroups = studentList.stream()
                .collect(Collectors.groupingBy(p -> p.getSchoolId(), Collectors.toList()));
        for (Long schoolId : schoolGroups.keySet()) {
            List<Clazz> existClazzList = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId)
                    .toList();
            existClazzList = existClazzList.stream()
                    .filter(p -> p.isSystemClazz() && p.getClazzLevel().getLevel() >= ClazzLevel.FIRST_GRADE.getLevel() && p.getClazzLevel().getLevel() <= ClazzLevel.SENIOR_THREE.getLevel() && !p.isDisabledTrue())
                    .collect(Collectors.toList());
            Map<String, List<Clazz>> existClazzMap = existClazzList.stream()
                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel().getLevel(), "_", p.getClassName()), Collectors.toList()));

            // 重名处理新策略，按照年级处理，同年级重名，不创建，异年级重名，创建, 要按照年级存储学生姓名
            Map<String, User> clazzLevelNameUserMap = new HashMap<>();
            Map<String, Clazz> clazzLevelNameClazzMap = new HashMap<>();

            for (Clazz clazz : existClazzList) {
                List<Long> userIds = asyncGroupServiceClient.getAsyncGroupService()
                        .findStudentIdsByClazzId(clazz.getId());
                userLoaderClient.loadUsers(userIds).values()
                        .forEach(p -> {
                                    clazzLevelNameUserMap.put(StringUtils.join(clazz.getClazzLevel().getLevel(), "_", p.fetchRealname()), p);
                                    clazzLevelNameClazzMap.put(StringUtils.join(clazz.getClazzLevel().getLevel(), "_", p.fetchRealname()), clazz);
                                }
                        );
            }

            List<JoinClazzStudentInfoMapper> schoolData = schoolGroups.get(schoolId);

            // 首先将老师加入班级
            Map<String, List<JoinClazzStudentInfoMapper>> teaherClazzGroups = schoolData.stream()
                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getTeacherMobileOrid(), "_", p.getTeacherName(), "_", p.getClazzLevel(), "_", p.getClazzName()), Collectors.toList()));
            for (String teacherClazzKey : teaherClazzGroups.keySet()) {
                String[] teacherClazzKeys = teacherClazzKey.split("_");
                String teacherMobileOrid = teacherClazzKeys[0];
                TeacherDetail teacherDetail = null;
                // 支持手机号码和用户ID两种模式
                if (MobileRule.isMobile(teacherMobileOrid)) {
                    UserAuthentication ua = userLoaderClient.loadMobileAuthentication(teacherMobileOrid, UserType.TEACHER);
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(ua.getId());
                } else {
                    Long tid = ConversionUtils.toLong(teacherMobileOrid);
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(ConversionUtils.toLong(tid));
                }

                String clazzKey = StringUtils.join(teacherClazzKeys[2], "_", teacherClazzKeys[3]);

                // 如果班级不存在就新建班级
                if (!existClazzMap.containsKey(clazzKey)) {
                    ClassMapper mapper = new ClassMapper();
                    mapper.setClassLevel(teacherClazzKeys[2]);
                    mapper.setClazzName(teacherClazzKeys[3]);
                    mapper.setSchoolId(schoolId);
                    MapMessage result = clazzServiceClient.createSystemClazz(Collections.singletonList(mapper));
                    if (!result.isSuccess()) {
                        failedInfoList.add("创建班级" + clazzKey + "失败!");
                        continue;
                    }
                    List<NeonatalClazz> neonatals = (List<NeonatalClazz>) result.get("neonatals");
                    Clazz clazz = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadClazz(neonatals.get(0).getClazzId());
                    existClazzMap.put(clazzKey, Collections.singletonList(clazz));
                }

                List<Clazz> existClazz = existClazzMap.get(clazzKey);
                Clazz clazz = existClazz.get(0);
                if (existClazz.size() > 1) {
                    for (int i = 1; i < existClazz.size(); i++) {
                        Clazz clazzObject = existClazz.get(i);
                        if (groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherDetail.getId(), clazzObject.getId(), false) != null) {
                            clazz = clazzObject;
                            break;
                        }
                    }
                }

                // 重名处理新策略，按照年级处理，同年级重名，不创建，提示错误的学生信息（姓名、班级、已存在的学号）, 异年级重名，创建
                List<JoinClazzStudentInfoMapper> newStudentList = teaherClazzGroups.get(teacherClazzKey);
                List<String> newStudentNames = new ArrayList<>();
                List<User> neverLoginUsers = new ArrayList<>();
                for (JoinClazzStudentInfoMapper mapper : newStudentList) {
                    String clazzLevelNameKey = StringUtils.join(mapper.getClazzLevel(), "_", mapper.getStudentName());
                    if (clazzLevelNameUserMap.containsKey(clazzLevelNameKey)) {
                        User existUser = clazzLevelNameUserMap.get(clazzLevelNameKey);
                        // FIXME 兼容模式
                        Date lastLoginTime = existUser != null ? userLoginServiceClient.findUserLastLoginTime(existUser.getId()) : null;
                        // 未登录的用户, 退出当前系统班
                        // FIXME 20151028修正：未登录用户退出班级逻辑改成未登录且创建账号时间超过6个月以上
                        if (lastLoginTime == null && existUser.getCreateTime().before(DateUtils.calculateDateDay(new Date(), -180))) {
                            Clazz existUserClazz = clazzLevelNameClazzMap.get(clazzLevelNameKey);
                            clazzServiceClient.studentExitSystemClazz(existUser.getId(), existUserClazz.getId());
                            neverLoginUsers.add(existUser);
                        } else {
                            failedInfoList.add("学生" + mapper.getStudentName() + "由于姓名重复，没有创建账号!, 同名学生信息:"
                                    + "班级:" + clazzLevelNameClazzMap.get(clazzLevelNameKey).formalizeClazzName()
                                    + ",学号:" + clazzLevelNameUserMap.get(clazzLevelNameKey).getId());
                        }
                        continue;
                    }

                    // 如果有手机号，看到是否已经注册过了
                    if (StringUtils.isNoneBlank(mapper.getStudentMobile())) {
                        List<User> mobileUsers = userLoaderClient.loadUsers(mapper.getStudentMobile(), UserType.STUDENT);
                        if (CollectionUtils.isNotEmpty(mobileUsers)) {
                            failedInfoList.add("学生" + mapper.getStudentName() + "手机号" + mapper.getStudentMobile() + "由于手机号已经注册，没有创建账号!, 同名学生信息:"
                                    + "学号:" + mobileUsers.get(0).getId() + ", 姓名:" + mobileUsers.get(0).fetchRealname());
                            continue;
                        }
                    }

                    newStudentNames.add(mapper.getStudentName());
                }

                MapMessage operResult = clazzServiceClient.teacherJoinSystemClazzWithSource(teacherDetail.getId(), clazz.getId(), newStudentNames, webSource);
                if (!operResult.isSuccess()) {
                    failedInfoList.add("往已有班级里面添加学生失败," + operResult.getInfo());
                } else {
                    ExRegion region = userLoaderClient.loadUserRegion(teacherDetail);
                    Long bookId = contentLoaderClient.getExtension().initializeClazzBook(teacherDetail.getSubject(), clazz.getClazzLevel().getLevel(), region.getCode(),
                            new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                    ChangeBookMapper cbm = new ChangeBookMapper();
                    cbm.setType(0);
                    cbm.setBooks(String.valueOf(bookId));
                    cbm.setClazzs(String.valueOf(clazz.getId()));
                    try {
                        contentServiceClient.setClazzBook(teacherDetail, cbm);
                    } catch (Exception e) {
                        logger.error("change book failed", e);
                    }

                    // 处理未登录的用户, 进新班
                    if (CollectionUtils.isNotEmpty(neverLoginUsers)) {
                        for (User user : neverLoginUsers) {
                            clazzServiceClient.studentJoinSystemClazz(user.getId(), clazz.getId(), teacherDetail.getId(), false, OperationSourceType.marketing);
                        }
                    }

                    Collection<NewbieUser> newStudents = (Collection<NewbieUser>) operResult.get("students");
                    if (CollectionUtils.isEmpty(newStudents)) {
                        newStudents = new ArrayList<>();
                    }

                    if (newStudents.size() > 0) {
                        String studentClazzKey = StringUtils.join(schoolId, "_", schoolData.get(0).getSchoolName(), "_", teacherClazzKey);
                        studentData.put(studentClazzKey, newStudents);
                    }

                    // 绑定手机号处理
                    for (NewbieUser newbieUser : newStudents) {
                        String userName = newbieUser.getUsername();
                        JoinClazzStudentInfoMapper mapper = newStudentList.stream()
                                .filter(p -> Objects.equals(teacherClazzKey, StringUtils.join(p.getTeacherMobileOrid(), "_", p.getTeacherName(), "_", p.getClazzLevel(), "_", p.getClazzName())))
                                .filter(p -> Objects.equals(p.getStudentName(), userName))
                                .findFirst().orElse(null);
                        if (StringUtils.isNoneBlank(mapper.getStudentMobile())) {
                            userServiceClient.activateUserMobile(newbieUser.getUserId(), mapper.getStudentMobile());
                        }
                    }

                    if (CollectionUtils.isNotEmpty(neverLoginUsers)) {
                        String studentClazzKey = StringUtils.join(schoolId, "_", schoolData.get(0).getSchoolName(), "_", teacherClazzKey);
                        Collection<NewbieUser> newGenStudents = studentData.get(studentClazzKey);
                        if (CollectionUtils.isEmpty(newGenStudents)) {
                            newGenStudents = new ArrayList<>();
                        }

                        for (User user : neverLoginUsers) {
                            NewbieUser newbieUser = new NewbieUser();
                            newbieUser.setUserId(user.getId());
                            newbieUser.setUsername(user.fetchRealname());
                            newbieUser.setPwd("");
                            newGenStudents.add(newbieUser);
                        }

                        studentData.put(studentClazzKey, newGenStudents);
                    }

                    if (!teacherClazzData.containsKey(teacherDetail.getId())) {
                        teacherClazzData.put(teacherDetail.getId(), new ArrayList<>());
                    }
                    teacherClazzData.get(teacherDetail.getId()).add(clazz);
                }
            }
        }

        return MapMessage.successMessage()
                .add("failedInfo", failedInfoList)
                .add("studentData", studentData)
                .add("teacherClazzData", teacherClazzData);
    }

    public MapMessage validateJoinClazzStudents(List<JoinClazzStudentInfoMapper> studentList) {
        if (CollectionUtils.isEmpty(studentList)) {
            return MapMessage.errorMessage("学生名单为空!");
        }

        // 空检查
        for (JoinClazzStudentInfoMapper student : studentList) {
            if (student.getSchoolId() == null || StringUtils.isBlank(student.getTeacherMobileOrid())
                    || student.getClazzLevel() == null || StringUtils.isBlank(student.getSchoolName())
                    || StringUtils.isBlank(student.getTeacherName()) || StringUtils.isBlank(student.getClazzName())
                    || StringUtils.isBlank(student.getStudentName())) {
                return MapMessage.errorMessage("输入内容有空值!");
            }
        }

        // 老师不可以跨学校, 根据老师ID和老师ID＋学校ID分组的大小是一样的
        Map<String, List<JoinClazzStudentInfoMapper>> teacherIdUniqueGroups = studentList.stream()
                .collect(Collectors.groupingBy(p -> p.getTeacherMobileOrid(), Collectors.toList()));
        Map<String, List<JoinClazzStudentInfoMapper>> teacherIdSchoolUniqueGroups = studentList.stream()
                .collect(Collectors.groupingBy(p -> StringUtils.join(p.getTeacherMobileOrid(), "_", p.getSchoolId()), Collectors.toList()));
        if (teacherIdUniqueGroups.size() != teacherIdSchoolUniqueGroups.size()) {
            return MapMessage.errorMessage("老师不能在两个学校同时任教!");
        }

        // 以学校为单位进行循环
        Map<Long, List<JoinClazzStudentInfoMapper>> schoolGroups = studentList.stream()
                .collect(Collectors.groupingBy(p -> p.getSchoolId(), Collectors.toList()));
        for (Long schoolId : schoolGroups.keySet()) {
            // 验证学校是否存在，并且学校名和数据库中一致
            School school = raikouSystem.loadSchool(schoolId);
            if (school == null) {
                return MapMessage.errorMessage("学校ID：" + schoolId + "不存在!");
            }

            if (!StringUtils.equalsIgnoreCase(StringUtils.deleteWhitespace(school.getCname()), schoolGroups.get(schoolId).get(0).getSchoolName())) {
                return MapMessage.errorMessage("学校ID：" + schoolId + "的名称不正确!");
            }

            List<JoinClazzStudentInfoMapper> schoolGroupData = schoolGroups.get(schoolId);

            Map<Integer, List<JoinClazzStudentInfoMapper>> clazzLevelGroups = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(p -> p.getClazzLevel(), Collectors.toList()));
            for (Integer clazzLevelKey : clazzLevelGroups.keySet()) {
                if (clazzLevelKey < ClazzLevel.FIRST_GRADE.getLevel() || clazzLevelKey > ClazzLevel.SENIOR_THREE.getLevel()) {
                    return MapMessage.errorMessage("非法的年级：" + clazzLevelKey + "!");
                }
            }
            Map<String, List<JoinClazzStudentInfoMapper>> clazzNameGroups = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(p -> p.getClazzName(), Collectors.toList()));
            String clazzNameRegex = "\\d+班";
            for (String clazzNameKey : clazzNameGroups.keySet()) {
                if (!clazzNameKey.matches(clazzNameRegex)) {
                    return MapMessage.errorMessage("非法的班级名：" + clazzNameKey + "!");
                }
            }

            // 以学校内的老师进行循环
            Map<String, List<JoinClazzStudentInfoMapper>> teacherGroups = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(p -> p.getTeacherMobileOrid(), Collectors.toList()));
            for (String teacherMobileOrid : teacherGroups.keySet()) {
                // 同一个ID老师姓名唯一，并且和数据库中一致
                List<JoinClazzStudentInfoMapper> teacherInfoGroup = teacherGroups.get(teacherMobileOrid);
                Map<String, List<JoinClazzStudentInfoMapper>> teacherNameSubGroup = teacherInfoGroup.stream()
                        .collect(Collectors.groupingBy(p -> p.getTeacherName(), Collectors.toList()));
                if (teacherNameSubGroup.size() > 1) {
                    return MapMessage.errorMessage("老师：" + teacherMobileOrid + "有多个姓名!");
                }

                TeacherDetail teacherDetail = null;
                // 支持手机号码和用户ID两种模式
                if (MobileRule.isMobile(teacherMobileOrid)) {
                    UserAuthentication ua = userLoaderClient.loadMobileAuthentication(teacherMobileOrid, UserType.TEACHER);
                    if (ua != null) {
                        teacherDetail = teacherLoaderClient.loadTeacherDetail(ua.getId());
                    }
                } else {
                    Long tid = ConversionUtils.toLong(teacherMobileOrid);
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(ConversionUtils.toLong(tid));
                }

                if (teacherDetail == null) {
                    return MapMessage.errorMessage("老师：" + teacherMobileOrid + "不存在!");
                }

                if (!StringUtils.equalsIgnoreCase(StringUtils.deleteWhitespace(teacherDetail.fetchRealname()), teacherInfoGroup.get(0).getTeacherName())) {
                    return MapMessage.errorMessage("老师：" + teacherMobileOrid + "的姓名不正确!");
                }

                if (!Objects.equals(teacherDetail.getTeacherSchoolId(), schoolId)) {
                    return MapMessage.errorMessage("老师：" + teacherMobileOrid + "的学校不正确!");
                }

                // 老师带的班级是否超过8个
                List<Clazz> sysTeacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherDetail.getId());
                sysTeacherClazzs = sysTeacherClazzs.stream()
                        .filter(p -> p.getClazzLevel().getLevel() >= ClazzLevel.FIRST_GRADE.getLevel() && p.getClazzLevel().getLevel() <= ClazzLevel.SENIOR_THREE.getLevel() && !p.isDisabledTrue())
                        .collect(Collectors.toList());
                Map<String, List<Clazz>> sysTeacherClazzGroups = sysTeacherClazzs.stream()
                        .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel().getLevel(), "年级", p.getClassName()), Collectors.toList()));
                Map<String, List<JoinClazzStudentInfoMapper>> newTeacherClazzGroups = teacherInfoGroup.stream()
                        .filter(p -> !sysTeacherClazzGroups.containsKey(StringUtils.join(p.getClazzLevel(), "年级", p.getClazzName())))
                        .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel(), "年级", p.getClazzName()), Collectors.toList()));
                if (teacherDetail.getSubject() != null
                        && newTeacherClazzGroups.size() > 0 && sysTeacherClazzs.size() + newTeacherClazzGroups.size() > ClazzConstants.MAX_CLAZZ_COUNT.get(teacherDetail.getSubject())) {
                    return MapMessage.errorMessage("老师：" + teacherMobileOrid + "带的班级数超过限制!");
                }

                // 老师带的班内人数是否超过100个
                Map<String, List<JoinClazzStudentInfoMapper>> oldTeacherClazzGroups = teacherInfoGroup.stream()
                        .filter(p -> sysTeacherClazzGroups.containsKey(StringUtils.join(p.getClazzLevel(), "年级", p.getClazzName())))
                        .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel(), "年级", p.getClazzName()), Collectors.toList()));
                for (String clazzKey : oldTeacherClazzGroups.keySet()) {
                    List<Clazz> clazzList = sysTeacherClazzGroups.get(clazzKey);
                    List<JoinClazzStudentInfoMapper> studentData = oldTeacherClazzGroups.get(clazzKey);
                    // 注意：2年级1班可能有多个，蛋疼
                    Clazz clazz = clazzList.get(0);
                    if (clazzList.size() > 1) {
                        for (int i = 1; i < clazzList.size(); i++) {
                            Clazz clazzObject = clazzList.get(i);
                            if (groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherDetail.getId(), clazzObject.getId(), false) != null) {
                                clazz = clazzObject;
                                break;
                            }
                        }
                    }

                    List<String> existClazzStudentNames = new ArrayList<>();
                    List<User> classStudents = userAggregationLoaderClient.loadTeacherStudentsByClazzId(clazz.getId(), teacherDetail.getId());
                    classStudents.stream().forEach(p -> existClazzStudentNames.add(p.fetchRealname()));
                    // 去掉重名的人数
                    List<JoinClazzStudentInfoMapper> newStudentList = oldTeacherClazzGroups.get(clazzKey);
                    newStudentList = newStudentList.stream().filter(p -> !existClazzStudentNames.contains(p.getStudentName())).collect(Collectors.toList());

                    if (existClazzStudentNames.size() + newStudentList.size() > globalTagServiceClient.getGlobalTagBuffer()
                            .loadSchoolMaxClassCapacity(schoolId, ClazzConstants.MAX_CLAZZ_CAPACITY)) {
                        return MapMessage.errorMessage("学校" + schoolId + "的班级" + clazzKey + "人数超上限!");
                    }

                }

                // 重名判断 导入名单的班级内不允许重名
                Map<String, List<JoinClazzStudentInfoMapper>> clazzStudentGroups = teacherInfoGroup.stream()
                        .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel(), "年级", p.getClazzName(), "学生", p.getStudentName()), Collectors.toList()));
                for (String studentKey : clazzStudentGroups.keySet()) {
                    if (clazzStudentGroups.get(studentKey).size() > 1) {
                        return MapMessage.errorMessage(studentKey + "姓名重复!");
                    }
                }
            }

            // 名单内和学校内重名15%判断
            Map<String, Integer> duplicateInfoMap = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(p -> p.getStudentName(), Collectors.summingInt(p -> 1)));
            double duplicateCount = duplicateInfoMap.values().stream().filter(p -> p > 1).reduce(0, (x, y) -> x + y);
            if (duplicateCount / schoolGroupData.size() > 0.15f) {
                return MapMessage.errorMessage("学校ID:" + schoolId + "内学生名有太多重复!");
            }

        }

        // 验证学生姓名是否合法
        List<JoinClazzStudentInfoMapper> errorNameList = studentList.stream().filter(p -> !isValidChineseName(p.getStudentName())).collect(Collectors.toList());
        if (errorNameList.size() > 0) {
            return MapMessage.errorMessage("学生姓名:" + errorNameList.get(0).getStudentName() + "非法!");
        }

        return MapMessage.successMessage();
    }


    // 批量往已有班级里面添加学生
    public MapMessage reformClazzStudents(List<ClazzReformInfoMapper> studentList) {
        MapMessage validateResult = validateClazzReformInput(studentList);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }

        List<String> failedInfoList = new ArrayList<>();
        Map<String, Collection<NewbieUser>> studentData = new LinkedHashMap<>();
        Map<Long, List<Clazz>> teacherClazzData = new HashMap<>();

        // 以学校，老师，班级为单位重新分班
        Map<Long, List<ClazzReformInfoMapper>> schoolGroups = studentList.stream()
                .collect(Collectors.groupingBy(p -> p.getSchoolId(), Collectors.toList()));
        for (Long schoolId : schoolGroups.keySet()) {
            // 学校数据
            List<ClazzReformInfoMapper> schoolData = schoolGroups.get(schoolId);

            List<Clazz> schoolClazzList = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId)
                    .toList();
            schoolClazzList = schoolClazzList.stream()
                    .filter(p -> p.isSystemClazz() && p.getClazzLevel().getLevel() >= ClazzLevel.FIRST_GRADE.getLevel() && p.getClazzLevel().getLevel() <= ClazzLevel.SIXTH_GRADE.getLevel() && !p.isDisabledTrue())
                    .collect(Collectors.toList());

            // 重名处理新策略，按照年级处理，同年级重名，不创建，异年级重名，创建, 要按照年级存储学生姓名
            Map<String, List<User>> clazzLevelNameUserMap = new HashMap<>();
            Map<String, Clazz> clazzLevelNameClazzMap = new HashMap<>();


            for (Clazz clazz : schoolClazzList) {
                List<Long> userIds = asyncGroupServiceClient.getAsyncGroupService()
                        .findStudentIdsByClazzId(clazz.getId());
                userLoaderClient.loadUsers(userIds).values()
                        .forEach(user -> {
                            String userKey = StringUtils.join(clazz.getClazzLevel().getLevel(), "_", user.fetchRealname());
                            if (!clazzLevelNameUserMap.containsKey(userKey)) {
                                clazzLevelNameUserMap.put(userKey, new ArrayList<>());
                            }

                            clazzLevelNameUserMap.get(userKey).add(user);
                            clazzLevelNameClazzMap.put(userKey, clazz);
                        });
            }

            Map<String, List<ClazzReformInfoMapper>> teacherGroups = schoolData.stream()
                    .collect(Collectors.groupingBy(p -> p.getTeacherMobileOrid(), Collectors.toList()));
            for (String teacherKey : teacherGroups.keySet()) {

                TeacherDetail teacherDetail;
                // 支持手机号码和用户ID两种模式
                if (MobileRule.isMobile(teacherKey)) {
                    UserAuthentication ua = userLoaderClient.loadMobileAuthentication(teacherKey, UserType.TEACHER);
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(ua.getId());
                } else {
                    Long tid = ConversionUtils.toLong(teacherKey);
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(ConversionUtils.toLong(tid));
                }

                List<Clazz> sysTeacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherDetail.getId());
                Map<String, List<Clazz>> teacherClazzList = sysTeacherClazzs.stream()
                        .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel().getLevel(), "年级", p.getClassName()), Collectors.toList()));

                Map<String, List<ClazzReformInfoMapper>> teacherClazzGroups = teacherGroups.get(teacherKey).stream()
                        .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel(), "年级", p.getClazzName()), Collectors.toList()));

                for (String clazzKey : teacherClazzGroups.keySet()) {
                    List<ClazzReformInfoMapper> clazzGroups = teacherClazzGroups.get(clazzKey);
                    Clazz clazz = teacherClazzList.get(clazzKey).get(0);

                    List<User> curStudents = userAggregationLoaderClient.loadTeacherStudentsByClazzId(clazz.getId(), teacherDetail.getId());
                    Map<String, List<User>> teacherClazzStudents = curStudents.stream()
                            .collect(Collectors.groupingBy(p -> StringUtils.join(p.getProfile().getRealname()), Collectors.toList()));

                    for (ClazzReformInfoMapper student : clazzGroups) {
                        String studentKey = StringUtils.join(clazz.getClazzLevel().getLevel(), "_", student.getStudentName());
                        if (!clazzLevelNameUserMap.containsKey(studentKey)) {
                            failedInfoList.add("学生" + student.getStudentName() + "不存在!");
                            continue;
                        }

                        List<User> users = clazzLevelNameUserMap.get(studentKey);

                        if (users.size() > 1) {
                            StringBuilder builder = new StringBuilder();
                            builder.append("老师").append(teacherKey).append("的学生").append(student.getStudentName());
                            builder.append("在系统中有多个重名账号存在!账号信息：");
                            for (User user : users) {
                                builder.append("ID:").append(user.getId());
                                builder.append(",");
                            }
                            failedInfoList.add(builder.toString());
                            continue;
                        }

                        // 退出学生原有的系统班级，并加入新班级
                        Clazz oldClazz = clazzLevelNameClazzMap.get(studentKey);
                        User user = users.get(0);
                        // 只处理不在班级里面的学生
                        if (!teacherClazzStudents.containsKey(user.getProfile().getRealname())) {
                            clazzServiceClient.studentExitSystemClazz(user.getId(), oldClazz.getId());
                            clazzServiceClient.studentJoinSystemClazz(user.getId(), clazz.getId(), teacherDetail.getId(), false, OperationSourceType.marketing);
                        }

                        // 构造结果数据，以便生成EXCEL文件
                        String studentClazzKey = StringUtils.join(schoolId, "_", schoolData.get(0).getSchoolName(), "_",
                                teacherDetail.getId(), "_", teacherDetail.getProfile().getRealname(), "_",
                                clazz.getClazzLevel().getLevel(), "_", clazz.getClassName());
                        Collection<NewbieUser> newGenStudents = studentData.get(studentClazzKey);
                        if (CollectionUtils.isEmpty(newGenStudents)) {
                            newGenStudents = new ArrayList<>();
                        }

                        NewbieUser newbieUser = new NewbieUser();
                        newbieUser.setUserId(user.getId());
                        newbieUser.setUsername(user.fetchRealname());
                        newbieUser.setPwd("");
                        newGenStudents.add(newbieUser);

                        studentData.put(studentClazzKey, newGenStudents);
                    }

                    if (!teacherClazzData.containsKey(teacherDetail.getId())) {
                        teacherClazzData.put(teacherDetail.getId(), new ArrayList<>());
                    }

                    teacherClazzData.get(teacherDetail.getId()).add(clazz);
                }
            }
        }

        return MapMessage.successMessage()
                .add("failedInfo", failedInfoList)
                .add("studentData", studentData)
                .add("teacherClazzData", teacherClazzData);
    }

    public MapMessage validateClazzReformInput(List<ClazzReformInfoMapper> studentList) {
        if (CollectionUtils.isEmpty(studentList)) {
            return MapMessage.errorMessage("学生名单为空!");
        }

        // 空检查
        for (ClazzReformInfoMapper student : studentList) {
            if (student.getSchoolId() == null || StringUtils.isBlank(student.getTeacherMobileOrid())
                    || student.getClazzLevel() == null || StringUtils.isBlank(student.getSchoolName())
                    || StringUtils.isBlank(student.getTeacherName()) || StringUtils.isBlank(student.getClazzName())
                    || StringUtils.isBlank(student.getStudentName())) {
                return MapMessage.errorMessage("输入内容有空值!");
            }
        }

        // 以学校为单位进行循环
        Map<Long, List<ClazzReformInfoMapper>> schoolGroups = studentList.stream()
                .collect(Collectors.groupingBy(p -> p.getSchoolId(), Collectors.toList()));
        for (Long schoolId : schoolGroups.keySet()) {
            // 验证学校是否存在，并且学校名和数据库中一致
            School school = raikouSystem.loadSchool(schoolId);
            if (school == null) {
                return MapMessage.errorMessage("学校ID：" + schoolId + "不存在!");
            }

            if (!StringUtils.equalsIgnoreCase(StringUtils.deleteWhitespace(school.getCname()), schoolGroups.get(schoolId).get(0).getSchoolName())) {
                return MapMessage.errorMessage("学校ID：" + schoolId + "的名称不正确!");
            }

            // 学校必须是小学
            if (SchoolLevel.JUNIOR.getLevel() != school.getLevel()) {
                return MapMessage.errorMessage("学校ID：" + schoolId + "不是小学!");
            }

            List<ClazzReformInfoMapper> schoolGroupData = schoolGroups.get(schoolId);

            // 学校中班级必须存在
            List<Clazz> sysSchoolClazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId)
                    .toList();
            sysSchoolClazz = sysSchoolClazz.stream()
                    .filter(p -> p.getClazzLevel().getLevel() >= ClazzLevel.FIRST_GRADE.getLevel() && p.getClazzLevel().getLevel() <= ClazzLevel.SIXTH_GRADE.getLevel() && !p.isDisabledTrue())
                    .collect(Collectors.toList());
            Map<String, List<Clazz>> sysSchoolClazzGroups = sysSchoolClazz.stream()
                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel().getLevel(), "年级", p.getClassName()), Collectors.toList()));
            List<ClazzReformInfoMapper> notExistClazz = schoolGroupData.stream()
                    .filter(p -> !sysSchoolClazzGroups.containsKey(StringUtils.join(p.getClazzLevel(), "年级", p.getClazzName())))
                    .collect(Collectors.toList());
            if (notExistClazz.size() > 0) {
                return MapMessage.errorMessage("学校ID：" + schoolId + "的班级" + StringUtils.join(notExistClazz.get(0).getClazzLevel(), "年级", notExistClazz.get(0).getClazzName()) + "不存在!");
            }

            // 同年级内学生不能有重名，有重名的学生需要市场人员手工处理
            Map<String, List<ClazzReformInfoMapper>> studentGroups = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel(), "_", p.getStudentName()), Collectors.toList()));
            for (String studentKey : studentGroups.keySet()) {
                if (studentGroups.get(studentKey).size() > 1) {
                    return MapMessage.errorMessage("学校ID：" + schoolId + "中有同名的学生存在，学生姓名：" + studentGroups.get(studentKey).get(0).getStudentName());
                }
            }

            // 以学校内的老师进行循环
            Map<String, List<ClazzReformInfoMapper>> teacherGroups = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(p -> p.getTeacherMobileOrid(), Collectors.toList()));
            for (String teacherMobileOrid : teacherGroups.keySet()) {
                List<ClazzReformInfoMapper> teacherInfoGroup = teacherGroups.get(teacherMobileOrid);

                TeacherDetail teacherDetail = null;
                // 支持手机号码和用户ID两种模式
                if (MobileRule.isMobile(teacherMobileOrid)) {
                    UserAuthentication ua = userLoaderClient.loadMobileAuthentication(teacherMobileOrid, UserType.TEACHER);
                    if (ua != null) {
                        teacherDetail = teacherLoaderClient.loadTeacherDetail(ua.getId());
                    }
                } else {
                    Long tid = ConversionUtils.toLong(teacherMobileOrid);
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(ConversionUtils.toLong(tid));
                }

                if (teacherDetail == null) {
                    return MapMessage.errorMessage("老师：" + teacherMobileOrid + "不存在!");
                }

                if (!StringUtils.equalsIgnoreCase(StringUtils.deleteWhitespace(teacherDetail.fetchRealname()), teacherInfoGroup.get(0).getTeacherName())) {
                    return MapMessage.errorMessage("老师：" + teacherMobileOrid + "的姓名不正确!");
                }

                if (!Objects.equals(teacherDetail.getTeacherSchoolId(), schoolId)) {
                    return MapMessage.errorMessage("老师：" + teacherMobileOrid + "的学校不正确!");
                }

                // 以老师班级进行循环
                Map<String, List<ClazzReformInfoMapper>> teacherClazzGroups = teacherInfoGroup.stream()
                        .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel(), "年级", p.getClazzName()), Collectors.toList()));

                List<Clazz> sysTeacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherDetail.getId());
                Map<String, List<Clazz>> teacherClazzList = sysTeacherClazzs.stream()
                        .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel().getLevel(), "年级", p.getClassName()), Collectors.toList()));

                for (String clazzKey : teacherClazzGroups.keySet()) {
                    // 老师必须是班级的任课老师
                    if (!teacherClazzList.containsKey(clazzKey)) {
                        return MapMessage.errorMessage("老师：" + teacherMobileOrid + "尚未加入" + clazzKey);
                    }

                    // 班级大小限制
                    List<ClazzReformInfoMapper> clazzInfoGroup = teacherClazzGroups.get(clazzKey);

                    if (clazzInfoGroup.size() > globalTagServiceClient.getGlobalTagBuffer()
                            .loadSchoolMaxClassCapacity(schoolId, ClazzConstants.MAX_CLAZZ_CAPACITY)) {
                        return MapMessage.errorMessage("老师" + teacherMobileOrid + "的班级" + clazzKey + "人数超上限!");
                    }

                }
            }
        }

        return MapMessage.successMessage();
    }


    private boolean isValidChineseName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return false;
        }

        String tempUserName = userName.trim().replaceAll(" ", "").replaceAll("　", "");
        String regExStudentName = "^[\u4E00-\u9FA5]{0,5}$";

        return tempUserName.matches(regExStudentName);
    }


    // 批量注册老师学生
    public MapMessage bulkCreateAccount(List<BulkAccountInfoMapper> accountList, String webSource, String operatorName) {
        MapMessage validateResult = validateBulkAccounts(accountList);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }

        List<String> failedInfoList = new ArrayList<>();
        List<TeacherDetail> teacherAccountList = new ArrayList<>();
        Map<Long, List<Clazz>> teacherClazzData = new HashMap<>();
        Map<String, Collection<NewbieUser>> studentData = new HashMap<>();
        Map<String, TeacherDetail> teacherIdMap = new HashMap<>();

        // 以学校，班级为单位添加学生
        Map<Long, List<BulkAccountInfoMapper>> schoolGroups = accountList.stream()
                .collect(Collectors.groupingBy(p -> p.getSchoolId(), Collectors.toList()));
        for (Long schoolId : schoolGroups.keySet()) {
            List<BulkAccountInfoMapper> schoolData = schoolGroups.get(schoolId);
            List<Clazz> existClazzList = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadSchoolClazzs(schoolId)
                    .toList();
            School clazzSchool = raikouSystem.loadSchool(schoolId);
            Ktwelve ktwelve = Ktwelve.PRIMARY_SCHOOL;
            //班级过滤增加中学的判断，中学增加7至9年级的判断
            if (SchoolLevel.JUNIOR == SchoolLevel.safeParse(clazzSchool.getLevel())) {
                existClazzList = existClazzList.stream()
                        .filter(p ->
                                p.isSystemClazz() && p.getClazzLevel().getLevel() >= ClazzLevel.FIRST_GRADE.getLevel() && p.getClazzLevel().getLevel() <= ClazzLevel.SIXTH_GRADE.getLevel() && !p.isDisabledTrue())
                        .collect(Collectors.toList());
            } else if (SchoolLevel.MIDDLE == SchoolLevel.safeParse(clazzSchool.getLevel())) {
                existClazzList = existClazzList.stream()
                        .filter(p ->
                                p.isSystemClazz() && p.getClazzLevel().getLevel() >= ClazzLevel.SIXTH_GRADE.getLevel() && p.getClazzLevel().getLevel() <= ClazzLevel.NINTH_GRADE.getLevel() && !p.isDisabledTrue()
                        ).collect(Collectors.toList());
                ktwelve = Ktwelve.JUNIOR_SCHOOL;
            } else if (SchoolLevel.HIGH == SchoolLevel.safeParse(clazzSchool.getLevel())) {
                existClazzList = existClazzList.stream()
                        .filter(p ->
                                p.isSystemClazz() && p.getClazzLevel().getLevel() >= ClazzLevel.SENIOR_ONE.getLevel() && p.getClazzLevel().getLevel() <= ClazzLevel.SENIOR_THREE.getLevel() && !p.isDisabledTrue()
                        ).collect(Collectors.toList());
                ktwelve = Ktwelve.SENIOR_SCHOOL;
            }

            Map<String, List<Clazz>> existClazzMap = existClazzList.stream()
                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel().getLevel(), "_", p.getClassName()), Collectors.toList()));

            // 重名处理新策略，按照年级处理，同年级重名，不创建，异年级重名，创建, 要按照年级存储学生姓名
            Map<String, User> clazzLevelNameUserMap = new HashMap<>();
            Map<String, Clazz> clazzLevelNameClazzMap = new HashMap<>();

            for (Clazz clazz : existClazzList) {
                List<Long> userIds = asyncGroupServiceClient.getAsyncGroupService()
                        .findStudentIdsByClazzId(clazz.getId());
                userLoaderClient.loadUsers(userIds).values()
                        .forEach(p -> {
                            clazzLevelNameUserMap.put(StringUtils.join(clazz.getClazzLevel().getLevel(), "_", p.fetchRealname()), p);
                            clazzLevelNameClazzMap.put(StringUtils.join(clazz.getClazzLevel().getLevel(), "_", p.fetchRealname()), clazz);
                        });
            }

            // 首先处理老师账号
            Map<String, List<BulkAccountInfoMapper>> teacherGroups = schoolData.stream()
                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getSubject(), "_", p.getTeacherMobile(), "_", p.getTeacherName()), Collectors.toList()));
            for (String teacherKey : teacherGroups.keySet()) {
                String[] teacherKeys = teacherKey.split("_");
                String subject = teacherKeys[0];
                Subject subjectObj = Subject.valueOf(SUBJECT_MAP.get(StringUtils.join(clazzSchool.getLevel(), "_", subject)));

                String teacherMobile = teacherKeys[1];
                String teacherName = teacherKeys[2];
                //TODO
                MapMessage createTeacherResult = createTeacherAccount(schoolId, subjectObj, teacherName, teacherMobile, "qsjklx", webSource, ktwelve, operatorName);
                if (!createTeacherResult.isSuccess()) {
                    failedInfoList.add("生成老师账号失败!老师手机：" + teacherMobile + ",老师姓名:" + teacherName + ",错误信息:" + createTeacherResult.getInfo());
                    continue;
                }

                User teacher = (User) createTeacherResult.get("user");
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
                // FIXME 20171001
                // teacherDetail.getProfile().setSensitiveMobile(teacherMobile); //DIRTY HACK FOR DOWNLOAD
                teacherAccountList.add(teacherDetail);
                teacherIdMap.put(teacherMobile, teacherDetail);
            }

            // 如果需要就继续处理班级
            if (schoolData.get(0).getClazzLevel() == null) {
                continue;
            }

            // 首先处理老师
            Map<String, List<BulkAccountInfoMapper>> teaherClazzGroups = schoolData.stream()
                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getTeacherMobile(), "_", p.getClazzLevel(), "_", p.getClazzName()), Collectors.toList()));
            for (String teacherClazzKey : teaherClazzGroups.keySet()) {
                String[] teacherClazzKeys = teacherClazzKey.split("_");
                String teacherMobile = teacherClazzKeys[0];
                String clazzKey = StringUtils.join(teacherClazzKeys[1], "_", teacherClazzKeys[2]);

                if (!teacherIdMap.containsKey(teacherMobile)) {
                    continue;
                }

                // FIXME 没有班级就创建新班级
                if (!existClazzMap.containsKey(clazzKey)) {
                    ClassMapper mapper = new ClassMapper();
                    mapper.setClassLevel(teacherClazzKeys[1]);
                    mapper.setClazzName(teacherClazzKeys[2]);
                    mapper.setSchoolId(schoolId);
                    MapMessage result = clazzServiceClient.createSystemClazz(Collections.singletonList(mapper));
                    if (!result.isSuccess()) {
                        failedInfoList.add("创建班级" + clazzKey + "失败!");
                        continue;
                    }
                    List<NeonatalClazz> neonatals = (List<NeonatalClazz>) result.get("neonatals");
                    Clazz clazz = raikouSDK.getClazzClient()
                            .getClazzLoaderClient()
                            .loadClazz(neonatals.get(0).getClazzId());
                    existClazzMap.put(clazzKey, Collections.singletonList(clazz));
                }

                Clazz clazz = existClazzMap.get(clazzKey).get(0);
                TeacherDetail teacher = teacherIdMap.get(teacherMobile);
                // FIXME 20171001
                //teacher.getProfile().setSensitiveMobile(teacherMobile);

                // 重名处理新策略，按照年级处理，同年级重名，不创建，提示错误的学生信息（姓名、班级、已存在的学号）, 异年级重名，创建

                List<BulkAccountInfoMapper> newStudentList = teaherClazzGroups.get(teacherClazzKey);
                List<String> studentNames = new ArrayList<>();
                List<User> neverLoginUsers = new ArrayList<>();

                for (BulkAccountInfoMapper mapper : newStudentList) {
                    String clazzLevelNameKey = StringUtils.join(mapper.getClazzLevel(), "_", mapper.getStudentName());
                    if (clazzLevelNameUserMap.containsKey(clazzLevelNameKey)) {
                        User existUser = clazzLevelNameUserMap.get(clazzLevelNameKey);
                        // 未登录的用户, 退出当前系统班
                        // FIXME 20151028修正：未登录用户退出班级逻辑改成未登录且创建账号时间超过6个月以上
                        // FIXME 兼容模式
//                        Date lastLoginTime = userLoaderClient.findUserLastLoginTime(existUser);
                        Date lastLoginTime = existUser != null ? userLoginServiceClient.findUserLastLoginTime(existUser.getId()) : null;
                        if (lastLoginTime == null
                                && existUser.getCreateTime().before(DateUtils.calculateDateDay(new Date(), -180))) {
                            Clazz existUserClazz = clazzLevelNameClazzMap.get(clazzLevelNameKey);
                            clazzServiceClient.studentExitSystemClazz(existUser.getId(), existUserClazz.getId());
                            neverLoginUsers.add(existUser);
                        } else {
                            failedInfoList.add("学生" + mapper.getStudentName() + "由于姓名重复，没有创建账号!, 同名学生信息:"
                                    + "班级:" + clazzLevelNameClazzMap.get(clazzLevelNameKey).formalizeClazzName()
                                    + ",学号:" + clazzLevelNameUserMap.get(clazzLevelNameKey).getId());
                        }
                    } else {
                        studentNames.add(mapper.getStudentName());
                    }
                }

                MapMessage operResult = clazzServiceClient.teacherJoinSystemClazzWithSource(teacher.getId(), clazz.getId(), studentNames, webSource);
                if (!operResult.isSuccess()) {
                    failedInfoList.add("生成学生账号失败，但是创建老师成功!老师学号:" + teacher.getId()
                            + "，老师手机号码：" + teacherMobile + ",错误信息：" + operResult.getInfo());
                } else {
                    // 指定班级教材
                    ExRegion region = userLoaderClient.loadUserRegion(teacher);
                    Long bookId = contentLoaderClient.getExtension().initializeClazzBook(
                            teacher.getSubject(), clazz.getClazzLevel().getLevel(), region.getCode(),
                            new RaikouRegionBufferDelegator(raikouSystem.getRegionBuffer()));
                    ChangeBookMapper cbm = new ChangeBookMapper();
                    cbm.setType(0);
                    cbm.setBooks(String.valueOf(bookId));
                    cbm.setClazzs(String.valueOf(clazz.getId()));
                    try {
                        contentServiceClient.setClazzBook(teacher, cbm);
                    } catch (Exception e) {
                        logger.error("change book failed", e);
                    }

                    // 处理未登录的用户, 进新班
                    if (CollectionUtils.isNotEmpty(neverLoginUsers)) {
                        neverLoginUsers.forEach(p -> clazzServiceClient.studentJoinSystemClazz(p.getId(), clazz.getId(), teacher.getId(), false, OperationSourceType.marketing));
                    }

                    Collection<NewbieUser> newStudents = (Collection<NewbieUser>) operResult.get("students");
                    if (newStudents == null) {
                        newStudents = new ArrayList<>();
                    }
                    if (newStudents.size() > 0) {
                        String studentClazzKey = StringUtils.join(teacher.getId(), "_", teacher.fetchRealname(), "_", teacherMobile, "_", clazzKey, "_", clazz.getId());
                        studentData.put(studentClazzKey, newStudents);
                    }
                    if (CollectionUtils.isNotEmpty(neverLoginUsers)) {
                        String studentClazzKey = StringUtils.join(teacher.getId(), "_", teacher.fetchRealname(), "_", teacherMobile, "_", clazzKey, "_", clazz.getId());
                        Collection<NewbieUser> newGenStudents = studentData.get(studentClazzKey);
                        if (CollectionUtils.isEmpty(newGenStudents)) {
                            newGenStudents = new ArrayList<>();
                        }

                        for (User user : neverLoginUsers) {
                            NewbieUser newbieUser = new NewbieUser();
                            newbieUser.setUserId(user.getId());
                            newbieUser.setUsername(user.fetchRealname());
                            newbieUser.setPwd("");
                            newGenStudents.add(newbieUser);
                        }

                        studentData.put(studentClazzKey, newGenStudents);
                    }

                    if (!teacherClazzData.containsKey(teacher.getId())) {
                        teacherClazzData.put(teacher.getId(), new ArrayList<>());
                    }
                    teacherClazzData.get(teacher.getId()).add(clazz);
                }
            }

        }

        return MapMessage.successMessage().add("failedInfo", failedInfoList).add("studentData", studentData).add("teacherData", teacherAccountList)
                .add("teacherClazzData", teacherClazzData);
    }

    public MapMessage validateBulkAccounts(List<BulkAccountInfoMapper> bulkAccounts) {
        if (CollectionUtils.isEmpty(bulkAccounts)) {
            return MapMessage.errorMessage("名单内容为空!");
        }

        // 空检查
        Boolean onlyCreateTeacherAccount = null;
        for (BulkAccountInfoMapper account : bulkAccounts) {
            if (account.getSchoolId() == null || StringUtils.isBlank(account.getSchoolName())
                    || StringUtils.isBlank(account.getTeacherName()) || StringUtils.isBlank(account.getSubject())
                    || StringUtils.isBlank(account.getTeacherMobile())) {
                return MapMessage.errorMessage("输入内容有空值!");
            }

            // 后面三个要么全为空，要么全有值
            if (account.getClazzLevel() != null && StringUtils.isNoneBlank(account.getClazzName())
                    && StringUtils.isNoneBlank(account.getStudentName()) && (onlyCreateTeacherAccount == null || !onlyCreateTeacherAccount)) {
                onlyCreateTeacherAccount = Boolean.FALSE;
                continue;
            }

            if (account.getClazzLevel() == null && StringUtils.isBlank(account.getClazzName())
                    && StringUtils.isBlank(account.getStudentName()) && (onlyCreateTeacherAccount == null || onlyCreateTeacherAccount)) {
                onlyCreateTeacherAccount = Boolean.TRUE;
                continue;
            }

            return MapMessage.errorMessage("输入内容有空值!");
        }

        // 老师不可以跨学校, 根据老师手机号码和老师手机号码＋学校ID分组的大小是一样的
        Map<String, List<BulkAccountInfoMapper>> teacherMobileUniqueGroups = bulkAccounts.stream()
                .collect(Collectors.groupingBy(BulkAccountInfoMapper::getTeacherMobile, Collectors.toList()));
        Map<String, List<BulkAccountInfoMapper>> teacherMobileSchoolUniqueGroups = bulkAccounts.stream()
                .collect(Collectors.groupingBy(p -> StringUtils.join(p.getTeacherMobile(), "_", p.getSchoolId()), Collectors.toList()));
        if (teacherMobileUniqueGroups.size() != teacherMobileSchoolUniqueGroups.size()) {
            return MapMessage.errorMessage("老师不能在两个学校同时任教!");
        }

        // 老师手机号码和姓名匹配检查
        Map<String, List<BulkAccountInfoMapper>> teacherNameUniqueGroups = bulkAccounts.stream()
                .collect(Collectors.groupingBy(BulkAccountInfoMapper::getTeacherName, Collectors.toList()));
        if (teacherMobileUniqueGroups.size() > (teacherNameUniqueGroups.size() + 3)) {
            return MapMessage.errorMessage("重名老师太多，看看是不是手机号码有问题!");
        }

        // 学科必须满足要求 这块放在学校循环判断里,因为现在小学 初中 高中的学科不一样
//        Map<String, List<BulkAccountInfoMapper>> subjectGroups = bulkAccounts.stream()
//                .collect(Collectors.groupingBy(BulkAccountInfoMapper::getSubject, Collectors.toList()));
//
//        for (String subjectKey : subjectGroups.keySet()) {
//            if (!SUBJECT_MAP.containsKey(StringUtils.join("1_" + subjectKey))) {
//                return MapMessage.errorMessage("无效的学科:" + subjectKey);
//            }
//        }

        // 同一个手机号老师姓名唯一
        Map<String, List<BulkAccountInfoMapper>> teacherMobileGroups = bulkAccounts.stream()
                .collect(Collectors.groupingBy(BulkAccountInfoMapper::getTeacherMobile, Collectors.toList()));
        Map<String, List<BulkAccountInfoMapper>> teacherMobileNameGroups = bulkAccounts.stream()
                .collect(Collectors.groupingBy(p -> StringUtils.join(p.getTeacherMobile(), "_", p.getTeacherName()), Collectors.toList()));
        if (teacherMobileGroups.size() != teacherMobileNameGroups.size()) {
            return MapMessage.errorMessage("同一个手机号码有多个老师姓名!");
        }

        // 以学校为单位进行循环
        Map<Long, List<BulkAccountInfoMapper>> schoolGroups = bulkAccounts.stream()
                .collect(Collectors.groupingBy(BulkAccountInfoMapper::getSchoolId, Collectors.toList()));
        for (Long schoolId : schoolGroups.keySet()) {
            // 验证学校是否存在，并且学校名和数据库中一致
            School school = raikouSystem.loadSchool(schoolId);
            if (school == null) {
                return MapMessage.errorMessage("学校ID：" + schoolId + "不存在!");
            }
            if (!StringUtils.equalsIgnoreCase(StringUtils.deleteWhitespace(school.getCname()), schoolGroups.get(schoolId).get(0).getSchoolName())) {
                return MapMessage.errorMessage("学校ID：" + schoolId + "的名称不正确!");
            }

            // 学科必须满足要求
            Map<String, List<BulkAccountInfoMapper>> tempSubjectGroups = schoolGroups.get(schoolId).stream()
                    .collect(Collectors.groupingBy(BulkAccountInfoMapper::getSubject, Collectors.toList()));
            for (String subjectKey : tempSubjectGroups.keySet()) {
                if (!SUBJECT_MAP.containsKey(StringUtils.join(school.getLevel() + "_" + subjectKey))) {
                    return MapMessage.errorMessage("学校ID " + schoolId + "无效的学科:" + subjectKey);
                }
            }

            // 学校必须是小学或者中学
//            if (SchoolLevel.JUNIOR.getLevel() != school.getLevel() && SchoolLevel.MIDDLE.getLevel() != school.getLevel()) {
//                return MapMessage.errorMessage("学校ID：" + schoolId + "的类型为:" + SchoolLevel.safeParse(school.getLevel()).getDescription());
//            }

            if (onlyCreateTeacherAccount) {
                continue;
            }

            List<BulkAccountInfoMapper> schoolGroupData = schoolGroups.get(schoolId);

            // 学校中班级存在
//            List<Clazz> sysSchoolClazz = clazzLoaderClient.loadSchoolClazzs(schoolId).toList();
//            sysSchoolClazz = sysSchoolClazz.stream()
//                    .filter(p -> p.getClazzLevel().getLevel() >= ClazzLevel.FIRST_GRADE.getLevel() && p.getClazzLevel().getLevel() <= ClazzLevel.SIXTH_GRADE.getLevel() && !p.isDisabledTrue())
//                    .collect(Collectors.toList());
//            Map<String, List<Clazz>> sysSchoolClazzGroups = sysSchoolClazz.stream()
//                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel().getLevel(), "年级", p.getClassName()), Collectors.toList()));
//            List<BulkAccountInfoMapper> notExistClazz = schoolGroupData.stream()
//                    .filter(p -> !sysSchoolClazzGroups.containsKey(StringUtils.join(p.getClazzLevel(), "年级", CLAZZ_NAME_MAP.get(p.getClazzName()))))
//                    .collect(Collectors.toList());
//            if (notExistClazz.size() > 0) {
//                return MapMessage.errorMessage("学校ID：" + schoolId + "的班级" + StringUtils.join(notExistClazz.get(0).getClazzLevel(), "年级", notExistClazz.get(0).getClazzName()) + "不存在!");
//            }
            // FIXME 市场要求批量注册的时候能够同时建班，新的班级体系就是一个笑话，啥也不说了
            // 这里简单验一下年级是否是1－6年级，班级名称是否是 数字+班
            Map<Integer, List<BulkAccountInfoMapper>> clazzLevelGroups = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(BulkAccountInfoMapper::getClazzLevel, Collectors.toList()));
            if (SchoolLevel.JUNIOR == SchoolLevel.safeParse(school.getLevel())) {
                for (Integer clazzLevelKey : clazzLevelGroups.keySet()) {
                    if (clazzLevelKey < ClazzLevel.FIRST_GRADE.getLevel() || clazzLevelKey > ClazzLevel.SIXTH_GRADE.getLevel()) {
                        return MapMessage.errorMessage("学校级别为：" + SchoolLevel.safeParse(school.getLevel()).getDescription() + "非法的年级：" + clazzLevelKey + "!");
                    }
                }
            }
            if (SchoolLevel.MIDDLE == SchoolLevel.safeParse(school.getLevel())) {
                for (Integer clazzLevelKey : clazzLevelGroups.keySet()) {
                    if (clazzLevelKey < ClazzLevel.SIXTH_GRADE.getLevel() || clazzLevelKey > ClazzLevel.NINTH_GRADE.getLevel()) {
                        return MapMessage.errorMessage("学校级别为：" + SchoolLevel.safeParse(school.getLevel()).getDescription() + "非法的年级：" + clazzLevelKey + "!");
                    }
                }
            }

            if (SchoolLevel.HIGH == SchoolLevel.safeParse(school.getLevel())) {
                for (Integer clazzLevelKey : clazzLevelGroups.keySet()) {
                    if (clazzLevelKey < ClazzLevel.SENIOR_ONE.getLevel() || clazzLevelKey > ClazzLevel.SENIOR_THREE.getLevel()) {
                        return MapMessage.errorMessage("学校级别为：" + SchoolLevel.safeParse(school.getLevel()).getDescription() + "非法的年级：" + clazzLevelKey + "!");
                    }
                }
            }

            Map<String, List<BulkAccountInfoMapper>> clazzNameGroups = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(BulkAccountInfoMapper::getClazzName, Collectors.toList()));
            String clazzNameRegex = "\\d+班";
            for (String clazzNameKey : clazzNameGroups.keySet()) {
                if (!clazzNameKey.matches(clazzNameRegex)) {
                    return MapMessage.errorMessage("非法的班级名：" + clazzNameKey + "!");
                }
            }

            // 以学校内的老师进行循环
            Map<String, List<BulkAccountInfoMapper>> teacherGroups = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(BulkAccountInfoMapper::getTeacherMobile, Collectors.toList()));
            for (String teacherMobile : teacherGroups.keySet()) {
                List<BulkAccountInfoMapper> teacherInfoGroup = teacherGroups.get(teacherMobile);

                // 老师带的班级是否超过8个
                Map<String, List<BulkAccountInfoMapper>> teacherClazzGroups = teacherInfoGroup.stream()
                        .collect(Collectors.groupingBy(p -> StringUtils.join(p.getClazzLevel(), "年级", p.getClazzName()), Collectors.toList()));
                if (CollectionUtils.isNotEmpty(teacherInfoGroup)
                        && teacherClazzGroups.size() > ClazzConstants.MAX_CLAZZ_COUNT.get(Subject.of(teacherInfoGroup.get(0).getSubject()))) {
                    return MapMessage.errorMessage("老师：" + teacherMobile + "带的班级数超过限制!");
                }

                // 老师带的班级内人数是否超过100个
                for (String clazzKey : teacherClazzGroups.keySet()) {
                    List<BulkAccountInfoMapper> studentList = teacherClazzGroups.get(clazzKey);

                    if (studentList.size() > globalTagServiceClient.getGlobalTagBuffer()
                            .loadSchoolMaxClassCapacity(schoolId, ClazzConstants.MAX_CLAZZ_CAPACITY)) {
                        return MapMessage.errorMessage("学校" + schoolId + "的班级" + clazzKey + "人数超上限!");
                    }

                    // 验证学生姓名是否合法
                    List<BulkAccountInfoMapper> errorNameList = studentList.stream().filter(p -> !isValidChineseName(p.getStudentName())).collect(Collectors.toList());
                    if (errorNameList.size() > 0) {
                        return MapMessage.errorMessage("学生姓名:" + errorNameList.get(0).getStudentName() + "非法!");
                    }

                    Map<String, List<BulkAccountInfoMapper>> errorNameMap = studentList.stream()
                            .collect(Collectors.groupingBy(p -> p.getStudentName(), Collectors.toList()));
                    if (errorNameMap.size() < studentList.size()) {
                        return MapMessage.errorMessage("班级" + clazzKey + "内学生姓名有重复!");
                    }
                }
            }

            // 名单内和学校内重名15%判断
            Map<String, Integer> duplicateInfoMap = schoolGroupData.stream()
                    .collect(Collectors.groupingBy(p -> p.getStudentName(), Collectors.summingInt(p -> 1)));
            double duplicateCount = duplicateInfoMap.values().stream().filter(p -> p > 1).reduce(0, (x, y) -> x + y);
            if (duplicateCount / schoolGroupData.size() > 0.15f) {
                return MapMessage.errorMessage("学校ID:" + schoolId + "内学生名有太多重复!");
            }

//            // 和学校内已有数据重名
//            List<Clazz> clazzList = clazzLoaderClient.loadSchoolClazzs(schoolId).toList();
//            clazzList = clazzList.stream()
//                    .filter(p -> p.getClazzLevel().getLevel() >= ClazzLevel.FIRST_GRADE.getLevel() && p.getClazzLevel().getLevel() <= ClazzLevel.SIXTH_GRADE.getLevel() && !p.isDisabledTrue())
//                    .collect(Collectors.toList());
//            Set<Long> clazzIds = clazzList.stream().map(Clazz::getId).collect(Collectors.toSet());
//            Set<User> existUsers = studentLoaderClient.loadClazzStudents(clazzIds).values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
//            List<String> existNameList = existUsers.stream().map(User::fetchRealname).collect(Collectors.toList());
//            List<BulkAccountInfoMapper> duplicatedList = schoolGroupData.stream()
//                    .filter(p -> existNameList.contains(p.getStudentName()))
//                    .collect(Collectors.toList());
//            if (duplicatedList.size() * 1.0f / schoolGroupData.size() > 0.15f) {
//                return MapMessage.errorMessage("学校ID:"+ schoolId + "内学生名有太多重复!");
//            }
        }

        return MapMessage.successMessage();
    }

    public MapMessage createTeacherAccount(Long schoolId, Subject subject, String teacherName, String teacherMobile, String initialPasswd, String webSource, Ktwelve ktwelve, String operatorName) {
        if (schoolId == null || subject == null || StringUtils.isBlank(teacherName) || StringUtils.isBlank(teacherMobile)) {
            return MapMessage.errorMessage("请求参数错误!");
        }

        // 判断学校是否有效
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("无效的学校ID:" + schoolId);
        }

        // 判断学科是否满足
        if (subject != Subject.ENGLISH && subject != Subject.MATH && subject == Subject.PHYSICS && subject == Subject.CHEMISTRY && subject == Subject.BIOLOGY
                && subject != Subject.CHINESE && subject != Subject.JMATH && subject != Subject.JENGLISH) {
            return MapMessage.errorMessage("无效的学科:" + subject);
        }

        // 判断手机号码
        if (!MobileRule.isMobile(teacherMobile)) {
            return MapMessage.errorMessage("无效的手机号码:" + teacherMobile);
        }

        if (!isValidChineseName(teacherName)) {
            return MapMessage.errorMessage("无效的老师姓名:" + teacherName);
        }

        // 判断初始密码
        if (StringUtils.isBlank(initialPasswd) || initialPasswd.length() > 16) {
            initialPasswd = String.valueOf(RandomUtils.nextInt(100000, 999999));
        }

        if (userLoaderClient.loadMobileAuthentication(teacherMobile, UserType.TEACHER) != null) {
            return MapMessage.errorMessage("手机号码已经被绑定过了!");
        }

        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
        neonatalUser.setUserType(UserType.TEACHER);
        neonatalUser.setMobile(teacherMobile);
        neonatalUser.setPassword(initialPasswd);
        neonatalUser.setRealname(teacherName);
        neonatalUser.setWebSource(webSource);
        neonatalUser.attachPasswordState(PasswordState.AUTO_GEN);

        MapMessage regResult = userServiceClient.registerUserAndSendMessage(neonatalUser);
        if (!regResult.isSuccess()) {
            return regResult;
        }
        User user = (User) regResult.get("user");

        // 如果手机号码尚未绑定，那么根据参数绑定手机号码
        if (userLoaderClient.loadMobileAuthentication(teacherMobile, UserType.TEACHER) == null) {
            userServiceClient.activateUserMobile(user.getId(), teacherMobile, false, operatorName, webSource);
        }

        // 设置老师学校
        teacherServiceClient.setTeacherSubjectSchool(user, subject, ktwelve, schoolId);

        return regResult;
    }


    public MapMessage bulkAddSchool(List<BulkSchoolInfoMapper> schoolList) {
        MapMessage validateResult = validateBulkSchools(schoolList);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }

        List<School> schools = new ArrayList<>();

        for (BulkSchoolInfoMapper item : schoolList) {

            // 判断学校是否存在
            List<Long> existSchools = getExistSchools(item.getRegionCode(), item.getSchoolName());
            if (CollectionUtils.isNotEmpty(existSchools)) {
                School oldSchool = new School();
                oldSchool.setId(existSchools.get(0));
                oldSchool.setCname(item.getSchoolName());
                schools.add(oldSchool);
                continue;
            }

            School school = new School();

            school.setRegionCode(item.getRegionCode());
            school.setCode(String.valueOf(item.getRegionCode()));

            school.setCname(item.getSchoolName());
            school.setShortName(item.getShortName());

            school.setLevel(SchoolLevel.JUNIOR.getLevel());
            if (SchoolLevel.MIDDLE.getDescription().equals(item.getSchoolLevel())) {
                school.setLevel(SchoolLevel.MIDDLE.getLevel());
            }

            school.setType(SchoolType.PUBLIC.getType());

            // 鉴定状态
            if ("鉴定通过".equals(item.getAuthState())) {
                school.setAuthenticationState(AuthenticationState.SUCCESS.getState());
            } else {
                school.setAuthenticationState(AuthenticationState.WAITING.getState());
            }

            // 鉴定来源
            if ("AB类学校".equals(item.getAuthState())) {
                school.setAuthenticationSource(AuthenticationSource.AB_TYPE);
            } else if ("市场".equals(item.getAuthState())) {
                school.setAuthenticationSource(AuthenticationSource.MARKET);
            }

            // 重点非重点
            if ("重点".equals(item.getVip())) {
                school.setVip(1);
            } else {
                school.setVip(2);
            }

            Long schoolId = createSchool(school);
            school.setId(schoolId);
            schools.add(school);
        }

        return MapMessage.successMessage().add("schools", schools);
    }

    public MapMessage validateBulkSchools(List<BulkSchoolInfoMapper> bulkSchools) {
        if (CollectionUtils.isEmpty(bulkSchools)) {
            return MapMessage.errorMessage("名单内容为空!");
        }

        // 内容检查
        for (BulkSchoolInfoMapper item : bulkSchools) {
            if (item.getRegionCode() == null || StringUtils.isBlank(item.getSchoolName())
                    || StringUtils.isBlank(item.getShortName()) || StringUtils.isBlank(item.getSchoolLevel())
                    || StringUtils.isBlank(item.getSchoolType()) || StringUtils.isBlank(item.getAuthState())
                    || StringUtils.isBlank(item.getVip())) {
                return MapMessage.errorMessage("输入内容有空值!");
            }

            // 学校级别必须是小学/中学
            if (!SchoolLevel.JUNIOR.getDescription().equals(item.getSchoolLevel()) && !SchoolLevel.MIDDLE.getDescription().equals(item.getSchoolLevel())) {
                return MapMessage.errorMessage("学校级别必须是小学/中学!");
            }

            // 学校类型必须是公立制学校
            if (!"公立制学校".equals(item.getSchoolType())) {
                return MapMessage.errorMessage("学校类型必须是公立制学校!");
            }

            // 鉴定状态必须是已鉴定/等待鉴定
            if (!"鉴定通过".equals(item.getAuthState()) && !"待鉴定".equals(item.getAuthState())) {
                return MapMessage.errorMessage("鉴定状态必须是已鉴定/待鉴定!");
            }

            // 鉴定来源必须是AB类学校/市场
            if ("鉴定通过".equals(item.getAuthState()) && (!"AB类学校".equals(item.getAuthSource()) && !"市场".equals(item.getAuthSource()))) {
                return MapMessage.errorMessage("鉴定通过学校的鉴定来源必须是AB类学校/市场!");
            }

            // 重点非重点
            if (!"重点".equals(item.getVip()) && !"非重点".equals(item.getVip())) {
                return MapMessage.errorMessage("重点/非重点内容错误!");
            }

            // 不能有同名的学校存在
//            if (hasExistSchool(item.getRegionCode(), item.getSchoolName())) {
//                return MapMessage.errorMessage("系统中已经有名为" + item.getSchoolName() + "的学校!");
//            }
        }

        // REGION CODE必须存在
        Map<Integer, List<BulkSchoolInfoMapper>> regionGroups = bulkSchools.stream()
                .collect(Collectors.groupingBy(p -> p.getRegionCode(), Collectors.toList()));
        for (Integer regionCode : regionGroups.keySet()) {
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            if (exRegion == null) {
                return MapMessage.errorMessage("地区CODE" + regionCode + "无效！");
            }
        }

        // 输入文件里面不能有相同全称/简称的学校
        Map<String, List<BulkSchoolInfoMapper>> schoolNameGroups = bulkSchools.stream()
                .collect(Collectors.groupingBy(p -> p.getSchoolName(), Collectors.toList()));
        if (bulkSchools.size() != schoolNameGroups.size()) {
            return MapMessage.errorMessage("有重复的学校全称!");
        }

        Map<String, List<BulkSchoolInfoMapper>> shortNameGroups = bulkSchools.stream()
                .collect(Collectors.groupingBy(p -> p.getShortName(), Collectors.toList()));
        if (bulkSchools.size() != shortNameGroups.size()) {
            return MapMessage.errorMessage("有重复的学校简称!");
        }

        return MapMessage.successMessage();
    }

    private List<Long> getExistSchools(Integer regionCode, String schoolName) {
        if (regionCode == null || StringUtils.isBlank(schoolName)) {
            Collections.emptyList();
        }

        String querySchool = "SELECT ID FROM VOX_SCHOOL WHERE DISABLED=0 AND CNAME=? AND REGION_CODE=?";
        return utopiaSql.withSql(querySchool).useParamsArgs(schoolName, regionCode).queryColumnValues(Long.class);
    }

    public Long createSchool(final School school) {
        Validate.notBlank(school.getCname());
        Validate.notBlank(school.getShortName());
        Validate.notBlank(school.getCode());

        int regionCode = school.getRegionCode();
        ExRegion region = raikouSystem.loadRegion(regionCode);
        Validate.notNull(region);
        Validate.isTrue(region.fetchRegionType() == RegionType.COUNTY);

        MapMessage message = deprecatedSchoolServiceClient.getRemoteReference().upsertSchool(school, null);

        return message.isSuccess() ? SafeConverter.toLong(message.get("id")) : null;
    }

    /**
     * 批量导入快乐学学生
     * 需要注意：循环操作过程中导致的效率问题，事物问题
     */
    @SuppressWarnings("unchecked")
    public MapMessage batchAddKLXStudendts(List<ImportKLXStudentInfo> importKLXStudentInfoList, AuthCurrentUser authCurrentUser) {
        MapMessage mapMessage = checkImportKLXStudents(importKLXStudentInfoList, authCurrentUser);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        //按照老师id分组
        Map<Long, List<ImportKLXStudentInfo>> klxsMap = importKLXStudentInfoList.stream().collect(Collectors.groupingBy(ImportKLXStudentInfo::getTeacherId));
        Map<String, Long> mapglc = new HashMap<>();//保存 年级班级名称 对应的 班级id
        List<Clazz> existClazzList = (List) mapMessage.get("clazzList");
        Set<Long> groupList = (Set) mapMessage.get("groupList");
        existClazzList.forEach(p -> {
            mapglc.put(p.getClassLevel() + "_" + p.getClassName(), p.getId());
        });
        int updateNum = 0;//更新学号
        int newSignNum = 0;//新注册学生
        for (Long teacherId : klxsMap.keySet()) {
            //转换 clazzId 对应的 导入数据
            Map<Long, List<ImportKLXStudentInfo>> clazzInfoMap = new HashMap<>();
            klxsMap.get(teacherId).forEach(p -> {
                Long cId = mapglc.get(p.getGradeLevel() + "_" + p.getCalzzName());
                List<ImportKLXStudentInfo> ctintfo = clazzInfoMap.get(cId) == null ? new ArrayList() : clazzInfoMap.get(cId);
                ctintfo.add(p);
                clazzInfoMap.put(cId, ctintfo);
            });
            for (Long clazzId : clazzInfoMap.keySet()) {
                Map<String, String> studentInfoMap = new HashMap<>();
                clazzInfoMap.get(clazzId).forEach(p ->
                        studentInfoMap.put(p.getStudentName(), p.getStudentNumber())
                );
                mapMessage = newKuailexueServiceClient.batchImportKlxStudents(teacherId, clazzId, studentInfoMap, KlxStudent.ImportSource.marketing);
                if (!mapMessage.isSuccess()) {//失败则跳出
                    return mapMessage;
                }
                updateNum += SafeConverter.toInt(mapMessage.get("updateNum"));
                newSignNum += SafeConverter.toInt(mapMessage.get("newSignNum"));
            }
        }
        //保存操作日志 若是有需要可以拆除来
//        MapMessage klxs = kuailexueLoaderClient.loadGroupStudents(groupList);
//        //暂时先这样子处理，没有理清楚快乐学模式下关系
//        Map<Long, List<KuailexueUserMapper>> kumm = (Map<Long, List<KuailexueUserMapper>>) klxs.get("data");
        Map<Long, List<KlxStudent>> kumm = newKuailexueLoaderClient.loadKlxGroupStudents(groupList);
        //数据存在情况过于复杂 学生姓名可能不唯一且可能为空 学生学号可能不唯一且可能为空
        Map<String, KlxStudent> svuiMap = new HashMap<>();
        kumm.values().stream().flatMap(Collection::stream).forEach(p -> svuiMap.put(StringUtils.defaultIfBlank(p.getName(), "") + "_" + StringUtils.defaultIfBlank(p.getStudentNumber(), ""), p));
        importKLXStudentInfoList.forEach(p -> {
                    KlxStudent kum = svuiMap.get(StringUtils.defaultIfBlank(p.getStudentName(), "") + "_" + StringUtils.defaultIfBlank(p.getStudentNumber(), ""));
                    if (kum != null) {
                        p.setScanNumber(kum.getScanNumber());
                    }
                }
        );
        ImportKLXStudentsRecord sr = new ImportKLXStudentsRecord();
        sr.setImportKLXStudentInfoList(importKLXStudentInfoList);
        sr.setCreateTime(new Date());
        sr.setOperatorId(authCurrentUser.getUserId());
        sr.setSourceType(SystemPlatformType.AGENT);
        sr.setUpdateTime(new Date());
        sr.setSchoolId(importKLXStudentInfoList.get(0).getSchoolId());
        sr.setSchoolName(importKLXStudentInfoList.get(0).getSchoolName());
        sr.setComments("新注册学生" + newSignNum + "名，更新学号" + updateNum + "名");
        List<ImportKLXStudentsRecord> srList = new ArrayList<>();
        srList.add(sr);
        importKLXStudentsRecordLoaderClient.saveImportKLXStudentsRecords(srList);
        return MapMessage.successMessage();
    }

    /**
     * 校验导入的快乐学生数据
     * 1.校验学校是否存在 是否符合要求  字典表学校 学校id和学校名称需与字典表学校信息保持一致
     * 2.校验老师是否存在 是否符合要求  老师需为快乐学模式老师，老师姓名和老师id 需要和系统保持一致  即时信息
     * 3.校验班级是否存在 是否符合要求  班级是否存在
     * 4.上传学校需为同一所学校 且学生学号不能重复
     * 5.需要标记 哪一行出现了问题
     */
    private MapMessage checkImportKLXStudents(List<ImportKLXStudentInfo> importKLXStudentInfoList, AuthCurrentUser authCurrentUser) {
        MapMessage mapMessage = MapMessage.errorMessage();
        boolean ifError = false;
        //以第一行为基准数据
        Long schoolId = importKLXStudentInfoList.get(0).getSchoolId();
        String schoolName = importKLXStudentInfoList.get(0).getSchoolName();
        //暂时先这样处理
        Map<ImportKLXStudentsErrorType, ImportKLXStudentsErrorMessage> errorMap = new HashMap<>();
        errorMap.put(ImportKLXStudentsErrorType.ADMINISTERERROR, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.ADMINISTERERROR));
        errorMap.put(ImportKLXStudentsErrorType.SCHOOLNOTEXITERROR, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.SCHOOLNOTEXITERROR));
        errorMap.put(ImportKLXStudentsErrorType.SCHOOLNAMEDIFFERENTID, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.SCHOOLNAMEDIFFERENTID));
        errorMap.put(ImportKLXStudentsErrorType.CLASSCORRESPONDENCE, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.CLASSCORRESPONDENCE));
        errorMap.put(ImportKLXStudentsErrorType.TEACHERRELATIONERROR, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.TEACHERRELATIONERROR));
        errorMap.put(ImportKLXStudentsErrorType.MANYSCHOOLS, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.MANYSCHOOLS));
        errorMap.put(ImportKLXStudentsErrorType.STUDENTIDREPEAT, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.STUDENTIDREPEAT));
        errorMap.put(ImportKLXStudentsErrorType.FAKETEACHER, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.FAKETEACHER));
        errorMap.put(ImportKLXStudentsErrorType.NOTKLXTEACHER, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.NOTKLXTEACHER));
        errorMap.put(ImportKLXStudentsErrorType.MANYSTUDENTSSAMENAME, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.MANYSTUDENTSSAMENAME));
        errorMap.put(ImportKLXStudentsErrorType.VACANCDATALINE, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.VACANCDATALINE));
        errorMap.put(ImportKLXStudentsErrorType.TEACHERMISMATCH, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.TEACHERMISMATCH));
        errorMap.put(ImportKLXStudentsErrorType.STUDENTOUTRANGE, new ImportKLXStudentsErrorMessage(ImportKLXStudentsErrorType.STUDENTOUTRANGE));
        List<String> studentNumbers = new ArrayList<>();
        List<String> studentNames = new ArrayList<>();
        //上传学校需为同一所学校且学生姓名不能重复 若是不符合要求则标记数据行并提示
        Set<Long> schoolIds = new HashSet<>();
        for (ImportKLXStudentInfo klxs : importKLXStudentInfoList) {
            if (schoolIds.size() > 0 && (!ObjectUtils.equals(schoolId, klxs.getSchoolId()) || !ObjectUtils.equals(schoolName, klxs.getSchoolName()))) {
                errorMap.get(ImportKLXStudentsErrorType.MANYSCHOOLS).getRows().add(klxs.getRows());
                errorMap.get(ImportKLXStudentsErrorType.MANYSCHOOLS).setExit(true);
                ifError = true;
            }
            if (studentNumbers.contains(klxs.getStudentNumber())) {
                errorMap.get(ImportKLXStudentsErrorType.STUDENTIDREPEAT).getRows().add(klxs.getRows());
                errorMap.get(ImportKLXStudentsErrorType.STUDENTIDREPEAT).setExit(true);
                ifError = true;
            }
            if (studentNames.contains(klxs.getStudentName())) {//校验学生是否用重名
                errorMap.get(ImportKLXStudentsErrorType.MANYSTUDENTSSAMENAME).getRows().add(klxs.getRows());
                errorMap.get(ImportKLXStudentsErrorType.MANYSTUDENTSSAMENAME).setExit(true);
                ifError = true;
            }
            if (!klxs.notEmpty()) {
                errorMap.get(ImportKLXStudentsErrorType.VACANCDATALINE).getRows().add(klxs.getRows());
                errorMap.get(ImportKLXStudentsErrorType.VACANCDATALINE).setExit(true);
                ifError = true;
            }
            studentNumbers.add(klxs.getStudentNumber());
            studentNames.add(klxs.getStudentName());
            schoolIds.add(klxs.getSchoolId());
        }
        //过滤缺失的数据行
        importKLXStudentInfoList.forEach(p -> p.notEmpty());
        //判断学校是否存在
        List<AgentSchoolDictData> asddList = agentDictSchoolService.getWrappedSchoolDictDataBySchool(schoolId);
        if (CollectionUtils.isEmpty(asddList)) {
            errorMap.get(ImportKLXStudentsErrorType.SCHOOLNOTEXITERROR).setExit(true);
            mapMessage.add("errorMessage", errorMap.values().stream().collect(Collectors.toList()));
            return mapMessage;
        }
        //判断该学校是否在该用户管辖范围内
        List<AgentUser> aus = baseOrgService.getSchoolManager(schoolId);
        if ((authCurrentUser.isCityManager() || authCurrentUser.isBusinessDeveloper()) && CollectionUtils.isEmpty(aus)) {
            errorMap.get(ImportKLXStudentsErrorType.ADMINISTERERROR).setExit(true);
            mapMessage.add("errorMessage", errorMap.values().stream().collect(Collectors.toList()));
            return mapMessage;
        }
        if (authCurrentUser.isRegionManager()) {//该学校是否在该大区经理的管辖范围内
            List<AgentGroup> agentGroupList = baseOrgService.getUserGroups(authCurrentUser.getUserId());
            if (CollectionUtils.isNotEmpty(agentGroupList)) {
                List<AgentGroupRegion> agentGroupRegionList = baseOrgService.getGroupRegionByGroup(agentGroupList.get(0).getId());
                ExRegion exRegion = raikouSystem.loadRegion(asddList.get(0).getRegionCode());
                long num = agentGroupRegionList.stream().filter(p -> ObjectUtils.equals(p.getRegionCode(), exRegion.getCityCode())).count();
                if (num == 0) {
                    errorMap.get(ImportKLXStudentsErrorType.ADMINISTERERROR).setExit(true);
                    mapMessage.add("errorMessage", errorMap.values().stream().collect(Collectors.toList()));
                    return mapMessage;
                }
            }
        }
        //当前用户为专员
        if (authCurrentUser.isBusinessDeveloper() && ObjectUtils.notEqual(aus.get(0).getId(), authCurrentUser.getUserId())) {
            errorMap.get(ImportKLXStudentsErrorType.ADMINISTERERROR).setExit(true);
            mapMessage.add("errorMessage", errorMap.values().stream().collect(Collectors.toList()));
            return mapMessage;
        }
        if (ObjectUtils.notEqual(asddList.get(0).getSchoolName(), schoolName)) {
            errorMap.get(ImportKLXStudentsErrorType.SCHOOLNAMEDIFFERENTID).setExit(true);
            ifError = true;
        }
        Set<Long> teacherIdSet = importKLXStudentInfoList.stream().map(ImportKLXStudentInfo::getTeacherId).collect(Collectors.toSet());
        //验证老师是否是在该学校内
        Set<Long> exitTeacherIdSet = teacherLoaderClient.loadSchoolTeacherIds(schoolId);
        teacherIdSet.forEach(p -> exitTeacherIdSet.contains(p));
        if (CollectionUtils.isEmpty(exitTeacherIdSet)) {
            ImportKLXStudentsErrorMessage em4 = errorMap.get(ImportKLXStudentsErrorType.TEACHERRELATIONERROR);
            importKLXStudentInfoList.forEach(p -> em4.getRows().add(p.getRows()));
            em4.setExit(true);
            mapMessage.add("errorMessage", errorMap.values().stream().collect(Collectors.toList()));
            return mapMessage;
        }
        //校验老师是否有效 真老师 且 快乐学老师
        //过滤假老师
        Set<Long> fakeTeacherIdSet = teacherIdSet.stream()
                .filter(t -> teacherLoaderClient.isFakeTeacher(t)).collect(Collectors.toSet());
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIdSet);
        Set<Long> trueKLXTeacherIds = teacherMap.values().stream().filter(Teacher::isKLXTeacher).map(Teacher::getId).collect(Collectors.toSet());

        //获取老师 与 班级 之间的关系
        Map<Long, List<GroupTeacherMapper>> teacherGroups = groupLoaderClient.loadTeacherGroups(trueKLXTeacherIds, false);
        Map<Long, List<Long>> clazzTeacherMap = new HashMap<>();// 保存老师和班级的对应关系   若是存在更加直接的方法请修改2017.4.10
        Set<Long> clazzIds = new HashSet();
        Set<Long> groupList = new HashSet();
        for (Long tid : teacherGroups.keySet()) {
            List<GroupTeacherMapper> groupTeacherMapperList = teacherGroups.get(tid);
            for (GroupTeacherMapper group : groupTeacherMapperList) {
                if (group.isTeacherGroupRefStatusValid(tid)) { // 过滤出有效的组
                    List<Long> cds = CollectionUtils.isNotEmpty(clazzTeacherMap.get(tid)) ? clazzTeacherMap.get(tid) : new ArrayList<>();
                    cds.add(group.getClazzId());
                    clazzIds.add(group.getClazzId());
                    groupList.add(group.getId());
                    clazzTeacherMap.put(tid, cds);
                }
            }
        }
        Map<Long, Clazz> existClazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        Map<Long, List<String>> exitClazzMap = new HashMap();
        for (Long tId : clazzTeacherMap.keySet()) {
            List<String> tclazz = new ArrayList();
            clazzTeacherMap.get(tId).forEach(p -> {
                if (existClazzList.get(p) != null) {
                    tclazz.add(existClazzList.get(p).getClassLevel() + "_" + existClazzList.get(p).getClassName());
                }
            });
            exitClazzMap.put(tId, tclazz);
        }
        //标记假老师所在行 非快乐学老师所在行 非该学校下的老师 所在行
        for (ImportKLXStudentInfo info : importKLXStudentInfoList) {
            if (!exitTeacherIdSet.contains(info.getTeacherId())) {//老师是否存在
                errorMap.get(ImportKLXStudentsErrorType.TEACHERRELATIONERROR).getRows().add(info.getRows());
                errorMap.get(ImportKLXStudentsErrorType.TEACHERRELATIONERROR).setExit(true);
                ifError = true;
                continue;
            }
            if (fakeTeacherIdSet.contains(info.getTeacherId())) {//假老师
                errorMap.get(ImportKLXStudentsErrorType.FAKETEACHER).getRows().add(info.getRows());
                errorMap.get(ImportKLXStudentsErrorType.FAKETEACHER).setExit(true);
                ifError = true;
            }
            if (!trueKLXTeacherIds.contains(info.getTeacherId())) {
                errorMap.get(ImportKLXStudentsErrorType.NOTKLXTEACHER).getRows().add(info.getRows());
                errorMap.get(ImportKLXStudentsErrorType.NOTKLXTEACHER).setExit(true);
                ifError = true;
            }
            //真老师 快乐学身份 班级对应关系不存在
            if (!fakeTeacherIdSet.contains(info.getTeacherId()) && trueKLXTeacherIds.contains(info.getTeacherId()) && (exitClazzMap.get(info.getTeacherId()) == null || !exitClazzMap.get(info.getTeacherId()).contains(info.getGradeLevel() + "_" + info.getCalzzName()))) {
                errorMap.get(ImportKLXStudentsErrorType.CLASSCORRESPONDENCE).getRows().add(info.getRows());
                errorMap.get(ImportKLXStudentsErrorType.CLASSCORRESPONDENCE).setExit(true);
                ifError = true;
            }
            if (teacherMap.get(info.getTeacherId()) != null && ObjectUtils.notEqual(info.getTeacherName(), teacherMap.get(info.getTeacherId()).fetchRealname())) {
                errorMap.get(ImportKLXStudentsErrorType.TEACHERMISMATCH).getRows().add(info.getRows());
                errorMap.get(ImportKLXStudentsErrorType.TEACHERMISMATCH).setExit(true);
                ifError = true;
            }
        }
        Map<String, List<ImportKLXStudentInfo>> clazzGroup = new HashMap();
        importKLXStudentInfoList.forEach(p -> {
            List<ImportKLXStudentInfo> list = clazzGroup.get(p.getCalzzName());
            if (CollectionUtils.isEmpty(list)) {
                list = new ArrayList();
            }
            list.add(p);
            clazzGroup.put(p.getCalzzName(), list);
        });
        for (String calzzName : clazzGroup.keySet()) {
            if (CollectionUtils.isNotEmpty(clazzGroup.get(calzzName)) && clazzGroup.get(calzzName).size() > 150) {
                errorMap.get(ImportKLXStudentsErrorType.STUDENTOUTRANGE).setExit(true);
                ifError = true;
            }
        }
        if (ifError) {
            List<ImportKLXStudentsErrorMessage> list = errorMap.values().stream().collect(Collectors.toList());
            mapMessage.add("errorMessage", list);
        } else {
            mapMessage = MapMessage.successMessage();
            mapMessage.add("clazzList", existClazzList.values().stream().collect(Collectors.toList()));
            mapMessage.add("groupList", groupList);
        }
        return mapMessage;
    }
}
