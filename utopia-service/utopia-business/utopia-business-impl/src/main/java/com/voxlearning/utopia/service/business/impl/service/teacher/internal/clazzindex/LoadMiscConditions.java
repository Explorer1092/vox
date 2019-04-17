/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.clazzindex;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.service.business.impl.service.user.UserTaskService;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.TeacherNewUserTaskMapper;
import com.voxlearning.utopia.service.user.consumer.TeacherAlterationServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;

/**
 * @author Rui.Bao
 * @since 2014-09-11 2:05 PM
 */
@Named
public class LoadMiscConditions extends AbstractTeacherClazzIndexDataLoader {
    @Inject private TeacherAlterationServiceClient teacherAlterationServiceClient;
    @Inject private UserTaskService userTaskService;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;

    @Override
    protected TeacherClazzIndexDataContext doProcess(TeacherClazzIndexDataContext context) {
        Teacher teacher = context.getTeacher();
        if (!context.isSkipNextAll()) {
            // 未处理的换班请求数量
            context.getParam().put("pendingApplicationCount", teacherAlterationServiceClient.countPendingApplication(teacher.getId()));
            // 如果当前教师是未认证教师，并且认证任务之'三个学生绑定手机'还没有完成，显示认证帮助浮窗
            if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
                // 这里用教师新手任务的方法，虽然会得到许多无用内容，但是会命中缓存
                TeacherNewUserTaskMapper mapper = userTaskService.getTeacherNewUserTaskMapper(teacher);
                context.getParam().put("showHelp", mapper != null && !mapper.isEnoughStudentsBindParentMobile());
            }
            // 教师首页下载学生名单
            List<Clazz> clazzs = context.getClazzs().stream()
                    .filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .collect(Collectors.toList());
            context.getParam().put("downloadClazzs", getBatchNameClazz(teacher, clazzs));
        }
        return context;
    }

    // 新建无名单班级后导入学生姓名
    private List<Map<String, Object>> getBatchNameClazz(Teacher teacher, List<Clazz> clazzs) {
        // 如果老师是认证的，查询所有班级中最近一个月创建的并且登录学生小于等于1人
        if (teacher.fetchCertificationState() == SUCCESS) {
            clazzs = clazzs.stream().filter(source -> DateUtils.dayDiff(new Date(), source.getCreateTime()) < 30)
                    .collect(Collectors.toList());
        } else { // 如果老师没有认证，查询所有该老师创建的班级中最近一个月创建的并且登录学生小于等于1人
            List<Long> teacherClazzIds = teacherLoaderClient.loadTeacherClazzIds(teacher.getId());
            final List<Long> createClazzIds = new ArrayList<>();
            for (Long teacherClazzId : teacherClazzIds) {
                createClazzIds.add(teacherClazzId);
            }
            clazzs = clazzs.stream().filter(source -> createClazzIds.contains(source.getId()) && DateUtils.dayDiff(new Date(), source.getCreateTime()) < 30)
                    .collect(Collectors.toList());
        }

        if (clazzs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        List<Long> clazzIdList = clazzs.stream()
                .filter(e -> e != null && e.getId() != null)
                .map(Clazz::getId)
                .collect(Collectors.toList());

        Map<Long, List<Long>> clazzStudentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzIds(clazzIdList);
        Set<Long> studentIds = clazzStudentIds.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userLoaderClient.loadUsers(studentIds);

        for (Clazz clazz : clazzs) {
            boolean add = true;
            if (clazzStudentIds.containsKey(clazz.getId())) {
                List<User> students = clazzStudentIds.get(clazz.getId())
                        .stream()
                        .map(userMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
//                add = !userLoaderClient.validateLoginUserCount(students, 1);
                add = !userLoginServiceClient.validateLoginUserCount(
                        students.stream().map(User::getId).collect(Collectors.toList()), 1);
            }
            if (add) {
                Map<String, Object> map = new HashMap<>();
                map.put("clazzId", clazz.getId());
                map.put("clazzName", clazz.formalizeClazzName());
                result.add(map);
            }
        }
        return result;
    }
}
