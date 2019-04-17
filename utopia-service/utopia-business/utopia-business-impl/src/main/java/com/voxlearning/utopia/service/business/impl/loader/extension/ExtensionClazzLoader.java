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

package com.voxlearning.utopia.service.business.impl.loader.extension;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.mapper.ClazzInfoMapper;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.privilege.client.BlackWhiteListManagerClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.ActivityType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzListMapper;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.*;

@Named
public class ExtensionClazzLoader extends BusinessServiceSpringBean {

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private BlackWhiteListManagerClient blackWhiteListManagerClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject private RaikouSDK raikouSDK;

    public List<ClazzListMapper> loadTeacherClazzMappers(Teacher teacher, List<Clazz> clazzs) {
        if (teacher == null || CollectionUtils.isEmpty(clazzs)) {
            return Collections.emptyList();
        }

        // find clazzs that teacher create
        List<Long> teacherClazzIds = teacherLoaderClient.loadTeacherClazzIds(teacher.getId());
        Set<Long> createClazzIds = new LinkedHashSet<>();
        createClazzIds.addAll(teacherClazzIds);

        final Set<Long> allClazzIds = new LinkedHashSet<>();
        for (Clazz clazz : clazzs) {
            allClazzIds.add(clazz.getId());
        }

        // Load all clazzs' teachers
        Map<Long, List<ClazzTeacher>> clazzTeachers = teacherLoaderClient.loadClazzTeachers(allClazzIds);

        // Load all clazzs' students
        Map<Long, Group> groupMap = asyncGroupServiceClient.getAsyncGroupService()
                .loadGroupsByClazzIds(allClazzIds)
                .getUninterruptibly()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Group::getId, Function.identity(), (a, b) -> a));

        Map<Long, List<GroupStudentTuple>> groupStudentRefs = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByGroupIds(groupMap.keySet())
                .stream()
                .collect(Collectors.groupingBy(GroupStudentTuple::getGroupId));
        Set<Long> allStudentIds = groupStudentRefs.values()
                .stream()
                .flatMap(Collection::stream)
                .map(GroupStudentTuple::getStudentId)
                .collect(Collectors.toSet());
        Map<Long, User> allStudents = userLoaderClient.loadUsers(allStudentIds);

        // Arrange clazz students
        Map<Long, Set<Long>> clazzStudents = new HashMap<>();
        groupStudentRefs.values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(ref -> {
                    Group group = groupMap.get(ref.getGroupId());
                    User user = allStudents.get(ref.getStudentId());
                    if (group == null || user == null || user.fetchUserType() != UserType.STUDENT) {
                        return;
                    }

                    if (!clazzStudents.containsKey(group.getClazzId())) {
                        clazzStudents.put(group.getClazzId(), new HashSet<>());
                    }
                    clazzStudents.get(group.getClazzId()).add(user.getId());
                });

        List<ClazzListMapper> result = new LinkedList<>();
        for (Clazz clazz : clazzs) {
            ClazzListMapper mapper = new ClazzListMapper();

            // fillClazzInfo
            mapper.setClazzId(clazz.getId());
            mapper.setClazzLevel(clazz.getClassLevel());
            mapper.setClazzName(clazz.formalizeClazzName());
            mapper.setCreator(createClazzIds.contains(clazz.getId()));

            // fillAllTeachers
            List<Teacher> teachers = ClazzTeacher.toTeacherList(clazzTeachers.get(clazz.getId()));
            Map<String, Teacher> t_s_map = new HashMap<>();
            for (Teacher t : teachers) {
                t_s_map.put(t.getSubject().name(), t);
            }
            List<Teacher> allTeachers = new LinkedList<>();
            allTeachers.add(0, t_s_map.containsKey("MATH") ? t_s_map.get("MATH") : blankTeacher(MATH));

            if (teacher.getSubject() == CHINESE || blackWhiteListManagerClient.hasActivity(teacher.getId(), ActivityType.试用语文教师)) {
                allTeachers.add(1, t_s_map.containsKey("CHINESE") ? t_s_map.get("CHINESE") : blankTeacher(CHINESE));
                allTeachers.add(2, t_s_map.containsKey("ENGLISH") ? t_s_map.get("ENGLISH") : blankTeacher(ENGLISH));
            } else {
                allTeachers.add(1, t_s_map.containsKey("ENGLISH") ? t_s_map.get("ENGLISH") : blankTeacher(ENGLISH));
            }
            mapper.setAllTeachers(allTeachers);

            // fillStudentsInfo
            Set<Long> students = clazzStudents.get(clazz.getId());
            mapper.setStudentCount(0);
            mapper.setLoginFailuredCount(0);
            if (students != null) {
                mapper.setStudentCount(students.size());
//                int loginFailuredCount = 0;
//                for (User student : students) {
//                    if (loginFailed(student)) { // 登录失败学生
//                        loginFailuredCount++;
//                    }
//                }
//                mapper.setLoginFailuredCount(loginFailuredCount);
            }

            result.add(mapper);
        }
        return result;
    }

    private Teacher blankTeacher(Subject subject) {
        Teacher teacher = new Teacher();
        teacher.setSubject(subject);
        return teacher;
    }

    @Deprecated
    public Map<Long, List<ClazzInfoMapper>> loadClazzInfoMappers(Collection<Long> clazzIds) {
        if (CollectionUtils.isEmpty(clazzIds)) {
            return Collections.emptyMap();
        }
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        if (MapUtils.isEmpty(clazzs)) {
            return Collections.emptyMap();
        }
        Collection<Long> schoolIds = new LinkedList<>();
        for (Clazz clazz : clazzs.values()) {
            CollectionUtils.addNonNullElement(schoolIds, clazz.getSchoolId());
        }
        Map<Long, School> schools = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();

        Map<Long, List<Long>> clazzStudentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzIds(clazzIds);
        Set<Long> studentIds = clazzStudentIds.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userLoaderClient.loadUsers(studentIds);

        Map<Long, List<ClazzInfoMapper>> result = new LinkedHashMap<>();
        for (Long clazzId : clazzIds) {
            Clazz clazz = clazzs.get(clazzId);
            if (clazz == null) {
                continue;
            }
            School school = null;
            if (clazz.getSchoolId() != null) {
                school = schools.get(clazz.getSchoolId());
            }
            if (school == null) {
                continue;
            }
            List<ClazzInfoMapper> mappers = new LinkedList<>();
            List<Long> sids = clazzStudentIds.get(clazzId);
            if (CollectionUtils.isNotEmpty(sids)) {
                for (Long sid : sids) {
                    User student = userMap.get(sid);
                    if (student == null) {
                        continue;
                    }

                    int clazzLevel;
                    try {
                        clazzLevel = Integer.parseInt(clazz.getClassLevel());
                    } catch (Exception ex) {
                        clazzLevel = 0;
                    }

                    ClazzInfoMapper mapper = new ClazzInfoMapper();
                    mapper.setUserId(student.getId());
//                    mapper.setPwd(student.getRealCode());
                    mapper.setUserName(student.fetchRealname());
                    mapper.setClazzLevel(clazzLevel);
                    mapper.setClazzName(clazz.getClassName());
                    mapper.setSchoolName(school.getCname());
                    mapper.setClazzId(clazzId);
                    mappers.add(mapper);
                }
            }
            result.put(clazzId, mappers);
        }
        return result;
    }

    @Deprecated
    public List<ClazzInfoMapper> loadClazzInfoMapper(Long clazzId) {
        if (clazzId == null) {
            return Collections.emptyList();
        }
        List<ClazzInfoMapper> result = loadClazzInfoMappers(Collections.singleton(clazzId)).get(clazzId);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

//    private boolean loginFailed(User student) {
//        return (student.getLastLoginDatetime() == null && student.getLastLoginFailureDatetime() != null) ||
//                (student.getLastLoginDatetime() != null && student.getLastLoginFailureDatetime() != null &&
//                        student.getLastLoginFailureDatetime().getTime() > student.getLastLoginDatetime().getTime());
//    }
}
