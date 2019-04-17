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

package com.voxlearning.utopia.service.business.impl.service.user;

import com.voxlearning.alps.annotation.meta.PasswordState;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.business.impl.service.teacher.TeacherCertificationServiceImpl;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkStat;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.StudentNewUserTaskMapper;
import com.voxlearning.utopia.service.user.api.mappers.TeacherNewUserTaskMapper;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.user.consumer.cache.StudentNewUserTaskMapperGenerator;
import com.voxlearning.utopia.service.user.consumer.cache.TeacherNewUserTaskMapperGenerator;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class UserTaskService extends SpringContainerSupport {

    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;

    @Inject private TeacherCertificationServiceImpl teacherCertificationService;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UserLoginServiceClient userLoginServiceClient;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject private UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    public TeacherNewUserTaskMapper getTeacherNewUserTaskMapper(final User user) {
        if (user == null || user.getId() == null || user.fetchUserType() != UserType.TEACHER) {
            logger.warn("No available student user specified");
            return null;
        }

        TeacherNewUserTaskMapperGenerator generator = () -> {
            TeacherNewUserTaskMapper mapper = new TeacherNewUserTaskMapper("", "", 0L, 0L, 0L, 0L);
            try {
                List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(user.getId());

                // 设置姓名
                mapper.setName(user.fetchRealname());
                // 设置绑定的手机号码
                String phone = sensitiveUserDataServiceClient.showUserMobile(user.getId(), "be:getTeacherNewUserTaskMapper", SafeConverter.toString(user.getId()));
                mapper.setMobile(StringUtils.defaultString(phone));

                if (CollectionUtils.isNotEmpty(clazzs)) {
                    List<Long> clazzIds = clazzs.stream()
                            .filter(e -> e != null && e.getId() != null)
                            .map(Clazz::getId)
                            .collect(Collectors.toList());
                    List<StudentHomeworkStat.DataMapper> stats = newHomeworkLoaderClient.getStudentHomeworkStatByTeacherId(user.getId());
                    // 设置班级中登录过的学生数量
                    long count = userAggregationLoaderClient.loadTeacherStudentsByClazzIds(clazzIds, user.getId())
                            .values().stream()
                            .filter(CollectionUtils::isNotEmpty)
                            .flatMap(List::stream)
//                            .filter(e -> userLoaderClient.findUserLastLoginTime(e) != null)
                            .filter(e -> e != null && userLoginServiceClient.findUserLastLoginTime(e.getId()) != null)
                            .count();
                    mapper.setStudentCount(count);
                    // 设置曾经完成过作业或者测验或者假期作业任务包的学生数量
                    mapper.setFinishOnePlusCount(stats.stream().filter(source -> source.getNormalHomeworkCount() >= 1).count());
                    // 设置曾经完成过3次作业或者测验或者假期作业任务包的学生数量
                    mapper.setFinishThreePlusCount((long) stats.stream().filter(source -> source.getNormalHomeworkCount() >= 3).map(StudentHomeworkStat.DataMapper::getStudentId).collect(Collectors.toSet()).size());
                    // 设置绑定了手机或者家长绑定手机的学生数量
                    List<Long> bindStudents = teacherCertificationService.studentsBindParentMobileCountPlusStudentsBindSelfMobileCount(user.getId(), clazzIds);
                    mapper.setBindMobileCount(ConversionUtils.toLong(bindStudents.size()));
                }
            } catch (Exception ex) {
                logger.error("Teacher {} get new user task mapper error.", user.getId(), ex);
                return null;
            }
            return mapper;
        };

        TeacherNewUserTaskMapper mapper = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .NewUserTaskCacheManager_pureLoadTeacherNewUserTaskMapper(user.getId())
                .getUninterruptibly();
        if (mapper == null) {
            mapper = generator.generate();
            mapper = asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .NewUserTaskCacheManager_pureAddTeacherNewUserTaskMapper(user.getId(), mapper)
                    .getUninterruptibly();
        }
        return mapper;
    }

    public StudentNewUserTaskMapper getStudentNewUserTaskMapper(final User user) {
        if (user == null || user.getId() == null || user.fetchUserType() != UserType.STUDENT) {
            logger.warn("No available student user specified");
            return null;
        }

        StudentNewUserTaskMapperGenerator generator = () -> {
            StudentNewUserTaskMapper mapper = new StudentNewUserTaskMapper();
            mapper.setNameSetted(StringUtils.isNotBlank(user.fetchRealname()));

            try {
                UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(user.getId());
                mapper.setMobileVerfied(userAuthentication != null && userAuthentication.isMobileAuthenticated());
            } catch (Exception ex) {
                logger.warn("Failed to load authenticated mobile");
                return null;
            }

            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            PasswordState passwordState = ua.fetchPasswordState();
            mapper.setPasswordModified(passwordState == PasswordState.USER_SET);
            // 这个字段显现表示学生是否有家长（因为家长只能来源于微信或者家长app）
            mapper.setParentWechatBinded(CollectionUtils.isNotEmpty(studentLoaderClient.loadStudentParentRefs(user.getId())));
            return mapper;
        };

        StudentNewUserTaskMapper mapper = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .NewUserTaskCacheManager_pureLoadStudentNewUserTaskMapper(user.getId())
                .getUninterruptibly();
        if (mapper == null) {
            mapper = generator.generate();
            mapper = asyncUserCacheServiceClient.getAsyncUserCacheService()
                    .NewUserTaskCacheManager_pureAddStudentNewUserTaskMapper(user.getId(), mapper)
                    .getUninterruptibly();
        }
        return mapper;
    }
}
