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

package com.voxlearning.utopia.service.guest.impl.service;

import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.runtime.ProductDevelopment;
import com.voxlearning.utopia.data.NeonatalClazz;
import com.voxlearning.utopia.service.guest.api.mapper.XuebaStudentMapper;
import com.voxlearning.utopia.service.guest.api.mapper.XuebaTeacherMapper;
import com.voxlearning.utopia.service.guest.api.service.XuebaService;
import com.voxlearning.utopia.service.guest.impl.utils.GuestUtils;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClassMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author changyuan
 * @since 2017/2/28
 */
@Named
@ExposeService(interfaceClass = XuebaService.class)
public class XuebaServiceImpl implements XuebaService {

    private final Logger logger = LoggerFactory.getLogger(XuebaServiceImpl.class);

    private final static String DEFAULT_TEACHER_NAME = "老师";
    private final static Subject DEFAULT_SUBJECT = Subject.ENGLISH;
    private final static Ktwelve DEFAULT_KTWELVE = Ktwelve.PRIMARY_SCHOOL;
    private final static ClazzLevel DEFAULT_CLAZZLEVEL = ClazzLevel.FIRST_GRADE;
    private final static String DEFAULT_INIT_PASSWORD = "123456";

    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserServiceClient userServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private TeacherServiceClient teacherServiceClient;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private RaikouSDK raikouSDK;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Override
    public MapMessage bulkCreateTeachers(Collection<XuebaTeacherMapper> teacherMappers) {
        if (CollectionUtils.isEmpty(teacherMappers)) {
            return MapMessage.errorMessage();
        }

        Map<Long, String> resultMap = new HashMap<>();
        for (XuebaTeacherMapper teacherMapper : teacherMappers) {
            String teacherName = GuestUtils.isValidChineseName(teacherMapper.getName()) ? teacherMapper.getName() : DEFAULT_TEACHER_NAME;
            String teacherMobile = MobileRule.isMobile(teacherMapper.getMobile()) ? teacherMapper.getMobile() : generateRandomMobile();

            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
            neonatalUser.setUserType(UserType.TEACHER);
            neonatalUser.setMobile(teacherMobile);
            neonatalUser.setPassword(DEFAULT_INIT_PASSWORD);
            neonatalUser.setRealname(teacherName);
            neonatalUser.setWebSource(UserWebSource.xueba.getSource());
            neonatalUser.attachPasswordState(PasswordState.AUTO_GEN);

            MapMessage regResult = userServiceClient.registerUser(neonatalUser);
            if (!regResult.isSuccess()) {
                return regResult;
            }

            User user = (User) regResult.get("user");

            // 如果手机号码尚未绑定，那么根据参数绑定手机号码
            if (userLoaderClient.loadMobileAuthentication(teacherMobile, UserType.TEACHER) == null) {
                userServiceClient.activateUserMobile(user.getId(), teacherMobile, false);
            }

            // 设置老师学校
            teacherServiceClient.setTeacherSubjectSchool(user, DEFAULT_SUBJECT, DEFAULT_KTWELVE, getSchoolId());

            resultMap.put(user.getId(), teacherName);
        }

        return MapMessage.successMessage().add("users", resultMap);
    }

    @Override
    public MapMessage bulkAddStudents(Collection<XuebaStudentMapper> studentMappers, Long teacherId) {
        if (CollectionUtils.isEmpty(studentMappers) || teacherId == null) {
            return MapMessage.errorMessage();
        }

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher == null) {
            return MapMessage.errorMessage("老师不存在");
        }

        // 设置学生要加入的班级
        setClazzIds(studentMappers, teacher);

        Map<Long, Integer> autoNameMap = new HashMap<>();

        Map<Long, String> resultMap = new HashMap<>();
        for (XuebaStudentMapper studentMapper : studentMappers) {
            Long clazzId = studentMapper.getClazzId();
            if (clazzId == null) {
                return MapMessage.errorMessage("找不到学生" + studentMapper.getName() + "对应的班级");
            }

            // 注册学生
            String studentName = getStudentName(studentMapper, autoNameMap);
            if (studentName == null) {
                return MapMessage.errorMessage("学生名字" + studentMapper.getName() + "不合法");
            }

            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_STUDENT);
            neonatalUser.setUserType(UserType.STUDENT);
            neonatalUser.setPassword(DEFAULT_INIT_PASSWORD);
            neonatalUser.setRealname(studentName);
            neonatalUser.setWebSource(UserWebSource.xueba.getSource());
            neonatalUser.attachPasswordState(PasswordState.AUTO_GEN);
            neonatalUser.setTeacherId(teacherId);

            MapMessage regResult = userServiceClient.registerUser(neonatalUser);
            if (!regResult.isSuccess()) {
                return regResult;
            }

            User user = (User) regResult.get("user");

            if (SafeConverter.toBoolean(studentMapper.getNeedBindMobile())) {
                MapMessage message = userServiceClient.activateUserMobile(user.getId(), studentMapper.getMobile());
                if (!message.isSuccess()) {
                    logger.error("Active student mobile failed. userId {}", user.getId());
                }
            }

            // 加入班级
            MapMessage message = clazzServiceClient.studentJoinSystemClazz(user.getId(), studentMapper.getClazzId(), teacherId, true, OperationSourceType.xueba);
            if (!message.isSuccess()) {
                return message;
            }

            resultMap.put(user.getId(), user.fetchRealname());
        }

        return MapMessage.successMessage().add("users", resultMap);
    }

    // 默认的翻转课堂学校
    private long getSchoolId() {
        if (ProductDevelopment.isTestEnv() || ProductDevelopment.isDevEnv()) {
            return 414474L;
        } else {
            return 406084L;
        }
    }

    /**
     * 设置班级id
     * 如果班级id存在，且属于该老师，则使用班级id
     * 否则，根据clazz name查找或创建班级。将找到的clazz id设回到mapper的clazz id中
     *
     * @param studentMappers
     * @param teacher
     * @return
     */
    @SuppressWarnings("unchecked")
    private MapMessage setClazzIds(Collection<XuebaStudentMapper> studentMappers, Teacher teacher) {

        AlpsFuture<School> schoolAlpsFuture = asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchool(teacher.getId());

        List<Clazz> clazzes = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());
        Set<Long> clazzIds = clazzes.stream().map(Clazz::getId).collect(Collectors.toSet());
        Map<String, Long> clazzNameIdMap = clazzes.stream().collect(Collectors.toMap(Clazz::formalizeClazzName, Clazz::getId));

        School school = schoolAlpsFuture.getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("找不到老师对应学校");
        }

        Map<String, Clazz> schoolClazzNameMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .enabled()
                .toMap(Clazz::formalizeClazzName);

        for (XuebaStudentMapper studentMapper : studentMappers) {
            if (clazzIds.contains(studentMapper.getClazzId())) {
                continue;
            }

            String clazzName = studentMapper.getClazzName();
            if (clazzNameIdMap.containsKey(clazzName)) {
                studentMapper.setClazzId(clazzNameIdMap.get(clazzName));
                continue;
            }

            Long clazzId;
            Clazz clazz = schoolClazzNameMap.get(clazzName);
            if (clazz == null) {
                // 此时需要创建班级
                ClassMapper classMapper = new ClassMapper();
                classMapper.setClazzName(clazzName);
                classMapper.setClassLevel(SafeConverter.toString(DEFAULT_CLAZZLEVEL.getLevel()));
                classMapper.setSchoolId(school.getId());
                classMapper.setEduSystem(EduSystemType.P6.name());
                MapMessage result = clazzServiceClient.createSystemClazz(Collections.singletonList(classMapper));
                if (!result.isSuccess()) {
                    return MapMessage.errorMessage("创建班级" + clazzName + "失败!");
                }

                Collection<NeonatalClazz> neonatals = (Collection<NeonatalClazz>) result.get("neonatals");
                NeonatalClazz neonatalClazz = MiscUtils.firstElement(neonatals);
                clazzId = neonatalClazz.getClazzId();

                Clazz c = new Clazz();
                c.setId(clazzId);
                schoolClazzNameMap.put(neonatalClazz.getClazzName(), c);
            } else {
                clazzId = clazz.getId();
            }

            // 老师加入班级
            MapMessage result = clazzServiceClient.teacherJoinSystemClazzForce(teacher.getId(), clazzId);
            if (!result.isSuccess()) {
                return MapMessage.errorMessage("老师加入班级" + clazzId + "失败!");
            }

            // 更新对应集合
            clazzIds.add(clazzId);
            clazzNameIdMap.put(clazzName, clazzId);
            studentMapper.setClazzId(clazzId);
        }
        return MapMessage.successMessage();
    }

    private String getStudentName(XuebaStudentMapper studentMapper, Map<Long, Integer> autoNameMap) {
        if (GuestUtils.isValidChineseName(studentMapper.getName())) {
            return studentMapper.getName();
        }

        if (studentMapper.getName() == null) {
            Integer index = autoNameMap.computeIfAbsent(studentMapper.getClazzId(), k -> 0);
            index++;
            autoNameMap.put(studentMapper.getClazzId(), index);
            return SafeConverter.toString(studentMapper.getAutoNamePrefix(), "")
                    + GuestUtils.fromNumberToCNNumber(SafeConverter.toString(index));
        }

        return null;
    }

    private String generateRandomMobile() {
        int i = 0;
        while (i < 10) {
            String r = "1" + RandomUtils.randomNumeric(10);
            if (MobileRule.isMobile(r) && userLoaderClient.loadMobileAuthentication(r, UserType.TEACHER) == null) {
                return r;
            }
            i++;
        }
        return "";
    }


}
