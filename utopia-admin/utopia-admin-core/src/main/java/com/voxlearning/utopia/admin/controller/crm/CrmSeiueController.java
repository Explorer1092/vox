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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.entity.seiue.SeiueClass;
import com.voxlearning.utopia.entity.seiue.SeiueTerm;
import com.voxlearning.utopia.entity.seiue.SeiueUser;
import com.voxlearning.utopia.service.user.api.service.SeiueSyncDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alex
 * @version 0.1
 * @since 2015/7/3
 */
@Controller
@RequestMapping("/crm/seiue")
public class CrmSeiueController extends CrmAbstractController {

    @ImportService(interfaceClass = SeiueSyncDataService.class)
    private SeiueSyncDataService syncDataService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        SeiueTerm term = syncDataService.loadCurrentTerm();

        List<SeiueClass> seiueClasses = syncDataService.loadTermClass(term.getId());

        Map<String, SeiueUser> allTeacher = new HashMap<>();
        Map<String, List<SeiueClass>> allCourse = new HashMap<>();
        Map<Long, List<SeiueUser>> allStudents = new HashMap<>();

        for (SeiueClass seiueClass : seiueClasses) {
            Long classId = seiueClass.getId();
            List<SeiueUser> seiueUsers = syncDataService.loadClassUsersIncludeDisabled(classId);

            List<SeiueUser> classStudents = seiueUsers.stream().filter(SeiueUser::student).collect(Collectors.toList());

            seiueUsers.stream().filter(SeiueUser::teacher)
                    .forEach(teacher -> {
                        String teacherId = teacher.getId();

                        allTeacher.put(teacherId, teacher);

                        allCourse.computeIfAbsent(teacherId, k -> new ArrayList<>());
                        allCourse.get(teacherId).add(seiueClass);
                    });

            allStudents.put(classId, classStudents);
        }

        List<Map<String, Object>> data = new LinkedList<>();
        // 组装数据
        for (Map.Entry<String, SeiueUser> entry : allTeacher.entrySet()) {
            SeiueUser teacher = entry.getValue();
            String teacherId = entry.getKey();

            Map<String, Object> teacherInfo = new HashMap<>();
            teacherInfo.put("teacherName", teacher.getUserName());
            teacherInfo.put("loginName", teacher.getUserCode());
            teacherInfo.put("status", teacher.getUserStatus());

            if (allCourse.get(teacherId) != null) {
                List<Map<String, Object>> teacherClazz = new LinkedList<>();
                Map<Long, SeiueClass> courses = allCourse.get(teacherId)
                        .stream()
                        .collect(Collectors.toMap(SeiueClass::getId, Function.identity()));

                for (Map.Entry<Long, SeiueClass> e : courses.entrySet()) {
                    Map<String, Object> clazzInfo = new HashMap<>();

                    SeiueClass seiueClass = e.getValue();
                    clazzInfo.put("subject", seiueClass.subject().getValue());
                    clazzInfo.put("grade", seiueClass.clazzLevel().getDescription());
                    clazzInfo.put("clazzName", seiueClass.realClassName());
                    clazzInfo.put("courseName", seiueClass.getCourseName());
                    clazzInfo.put("id", seiueClass.getId());

                    List<Map<String, Object>> students = allStudents.get(e.getKey())
                            .stream()
                            .filter(Objects::nonNull)
                            .map(student -> {
                                Map<String, Object> studentInfo = new HashMap<>();
                                studentInfo.put("studentName", student.getUserName());
                                studentInfo.put("studentNumber", student.getUserCode());
                                return studentInfo;
                            })
                            .collect(Collectors.toList());

                    clazzInfo.put("students", students);
                    teacherClazz.add(clazzInfo);
                }
                teacherInfo.put("clazzInfo", teacherClazz);
            }
            data.add(teacherInfo);
        }
        model.addAttribute("data", data);
        model.addAttribute("schoolId", syncDataService.loadSchoolId());
        return "crm/seiue/index";
    }

}