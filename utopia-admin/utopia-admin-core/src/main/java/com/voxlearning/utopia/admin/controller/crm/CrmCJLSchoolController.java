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

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLClass;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLStudent;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacherCourse;
import com.voxlearning.utopia.service.mizar.consumer.service.CJLSyncDataServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alex
 * @version 0.1
 * @since 2015/7/3
 */
@Controller
@RequestMapping("/crm/cjlschool")
public class CrmCJLSchoolController extends CrmAbstractController {

    @Inject private CJLSyncDataServiceClient cjlSyncDataServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Map<String, List<CJLTeacherCourse>> allCourse = cjlSyncDataServiceClient.getSyncDataService()
                .findAllTeacherCourseForJob()
                .stream()
                .filter(CJLTeacherCourse::isValid)
                .collect(Collectors.groupingBy(CJLTeacherCourse::getTeacherId));

        Map<String, CJLTeacher> allTeacher = cjlSyncDataServiceClient.getSyncDataService()
                .findAllTeacher()
                .stream()
                .collect(Collectors.toMap(CJLTeacher::getId, Function.identity()));

        Map<String, CJLClass> allClass = cjlSyncDataServiceClient.getSyncDataService()
                .findAllClass()
                .stream()
                .collect(Collectors.toMap(CJLClass::getId, Function.identity()));

        Map<String, CJLStudent> allStudent = cjlSyncDataServiceClient.getSyncDataService()
                .findAllStudent()
                .stream()
                .collect(Collectors.toMap(CJLStudent::getId, Function.identity()));


        List<Map<String, Object>> data = new LinkedList<>();
        // 组装数据
        for (Map.Entry<String, CJLTeacher> entry : allTeacher.entrySet()) {
            CJLTeacher teacher = entry.getValue();
            String teacherId = entry.getKey();

            Map<String, Object> teacherInfo = new HashMap<>();
            teacherInfo.put("teacherId", teacherId);
            teacherInfo.put("teacherName", teacher.getName());
            teacherInfo.put("loginName", teacher.getLoginName());
            teacherInfo.put("subjects", teacher.getSubjectNames());
            teacherInfo.put("teacherGender", teacher.getGender());

            if (allCourse.get(teacherId) != null) {
                List<Map<String, Object>> clazzInfo = new LinkedList<>();
                Map<String, CJLTeacherCourse> courses = allCourse.get(teacherId)
                        .stream()
                        .collect(Collectors.toMap(CJLTeacherCourse::getClassId, Function.identity()));

                for (Map.Entry<String, CJLTeacherCourse> e : courses.entrySet()) {
                    Map<String, Object> courseInfo = new HashMap<>();

                    String clazzId = e.getKey();
                    CJLClass clazz = allClass.get(clazzId);
                    courseInfo.put("clazzId", clazzId);
                    courseInfo.put("clazzName", clazz.getGradeName() + clazz.getName());
                    courseInfo.put("type", clazz.getType() + clazz.getType1() + clazz.getType2());

                    CJLTeacherCourse course = e.getValue();
                    List<Map<String, Object>> students = course.getStudentList()
                            .stream()
                            .map(sid -> {
                                CJLStudent student = allStudent.get(sid);
                                if (student == null) {
                                    return null;
                                }
                                Map<String, Object> studentInfo = new HashMap<>();
                                studentInfo.put("studentId", student.getId());
                                studentInfo.put("studentName", student.getName());
                                studentInfo.put("studentGender", student.getGender());
                                studentInfo.put("studentNumber", student.getStudentNumber());
                                studentInfo.put("klxStudentId", student.getKlxStudentId());
                                return studentInfo;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    courseInfo.put("students", students);
                    clazzInfo.add(courseInfo);
                }
                teacherInfo.put("clazzInfo", clazzInfo);
            }
            data.add(teacherInfo);
        }
        System.out.println(JsonUtils.toJsonPretty(data));
        model.addAttribute("data", data);
        return "crm/cjlschool/index";
    }

}