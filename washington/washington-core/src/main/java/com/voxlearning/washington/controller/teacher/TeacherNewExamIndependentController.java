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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/5/12
 */
@Controller
@RequestMapping("/teacher/newexam")
public class TeacherNewExamIndependentController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    /**
     * 期末专项测试
     */
    @RequestMapping(value = "independent/index.vpage", method = RequestMethod.GET)
    public String independentIndex(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return "redirect:/teacher/showtip.vpage";
        }
        Map<Long, List<GroupTeacherMapper>> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(Collections.singleton(teacher.getId()), true);
        Map<Long, List<Long>> clazzIdGroupIdsMap = new LinkedHashMap<>();
        teacherGroups.forEach((tId, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(tId) && CollectionUtils.isNotEmpty(group.getStudents())) {
                clazzIdGroupIdsMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>()).add(group.getId());
            }
        }));
        List<Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdGroupIdsMap.keySet())
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        Map<Integer, List<Map<String, Object>>> batchClazzs = new LinkedHashMap<>();
        clazzs.forEach(clazz -> {
            List<Long> groupIds = clazzIdGroupIdsMap.get(clazz.getId());
            if (CollectionUtils.isNotEmpty(groupIds)) {
                for (Long groupId : groupIds) {
                    Map<String, Object> clzaaMapper = MapUtils.m(
                            "classId", clazz.getId(),
                            "className", clazz.getClassName(),
                            "groupId", groupId,
                            "canBeAssigned", true
                    );
                    batchClazzs.computeIfAbsent(clazz.getClazzLevel().getLevel(), k -> new ArrayList<>()).add(clzaaMapper);
                }
            }
        });
        // 生成各年级信息
        List<Map<String, Object>> batchClazzsList = new ArrayList<>();
        // 1~6年级
        for (int i = 1; i <= 6; i++) {
            List<Map<String, Object>> clazzList = batchClazzs.getOrDefault(i, Collections.emptyList());
            if (CollectionUtils.isNotEmpty(clazzList)) {
                Map<String, Object> batchClazzsMap = new LinkedHashMap<>();
                boolean canBeAssigned = clazzList.stream().anyMatch(c -> c.get("canBeAssigned").equals(true));
                batchClazzsMap.put("canBeAssigned", canBeAssigned);
                batchClazzsMap.put("clazzs", clazzList);
                batchClazzsMap.put("classLevel", i);
                batchClazzsList.add(batchClazzsMap);
            }
        }
        model.addAttribute("batchclazzs", JsonUtils.toJson(batchClazzsList));
        model.addAttribute("subject", teacher.getSubject());
        model.addAttribute("currentDateTime", DateUtils.dateToString(DayRange.current().getStartDate(), DateUtils.FORMAT_SQL_DATETIME));
        Date endDate = new Date(System.currentTimeMillis() + 300000);
        endDate = DateUtils.nextDay(endDate, 1);
        model.addAttribute("endDate", DateUtils.dateToString(endDate, "yyyy-MM-dd"));
        model.addAttribute("endTime", DateUtils.dateToString(endDate, "HH:mm"));
        SchoolYear schoolYear = SchoolYear.newInstance();
        model.addAttribute("term", schoolYear.currentTerm().getKey());
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        model.addAttribute("domain", getWebRequestContext().getWebAppBaseUrl());
        model.addAttribute("env", RuntimeMode.current());
        return "teacherv3/newexam/independent";
    }

    /**
     * 期末专项测试报告
     */
    @RequestMapping(value = "independent/report.vpage", method = RequestMethod.GET)
    public String independentReport(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return "redirect:/teacher/showtip.vpage";
        }
        model.addAttribute("subject", teacher.getSubject());
        return "teacherv3/newexam/independentreport";
    }

    /**
     * 试卷列表
     */
    @RequestMapping(value = "paperlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadPaperList() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 预览试卷
     */
    @RequestMapping(value = "preview.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage previewPaper() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 保存考试
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveNewExam() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 班级列表
     */
    @RequestMapping(value = "report/independent/clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadTeacherClazzList() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
        }
        return newExamServiceClient.loadTeacherClazzList(Collections.singleton(teacher.getId()));
    }

    /**
     * 自主考试列表
     */
    @RequestMapping(value = "report/independent/list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reportIndependentList(@RequestParam("groupId") Long groupId) {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 删除考试
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteNewExam() {
        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 自主考试详情-学生成绩单
     */
    @RequestMapping(value = "report/independent/detail/users.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage independentStudentDetail() {
        String newExamId = getRequestString("newExamId");
        if (StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("考试id不能为空");
        }
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
        }
        return newExamReportLoaderClient.independentExamDetailForStudent(teacher, newExamId);
    }

    /**
     * 自主考试详情-查看试卷
     */
    @RequestMapping(value = "report/independent/detail/clazz.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage independentClazzDetail() {
        String newExamId = getRequestString("newExamId");
        if (StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("考试id不能为空");
        }
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
        }
        return newExamReportLoaderClient.independentExamDetailForClazz(teacher, newExamId);
    }

    /**
     * 自主考试报告分享
     */
    @RequestMapping(value = "report/independent/share.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage shareIndependentReport() {
        String newExamId = getRequestString("newExamId");
        if (StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("考试id不能为空");
        }
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null || teacher.getSubject() == null) {
            return MapMessage.errorMessage("您还没有设置学科，请完成设置后再登录！");
        }
        return newExamServiceClient.shareIndependentReport(teacher, newExamId);
    }

    /**
     * 自主考试报告详情
     */
    @RequestMapping(value = "report/independent/detail.vpage", method = RequestMethod.GET)
    public String independentHistoryDetail(Model model) {
        String newExamId = getRequestString("newExamId");
        Subject subject = null;
        if (StringUtils.isNotBlank(newExamId)) {
            NewExam newExam = newExamLoaderClient.load(newExamId);
            if (newExam != null) {
                subject = newExam.getSubject();
            }
        }
        model.addAttribute("subject", subject);
        model.addAttribute("imgDomain", getCdnBaseUrlStaticSharedWithSep());
        return "teacherv3/newexam/report/independentdetail";
    }

    @RequestMapping(value = "report/independent/studentanswer.vpage", method = RequestMethod.GET)
    public String historyStudentanswer(Model model) {
        Long studentId = getRequestLong("userId");

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return "redirect:/index.vpage";
        }

        String id = getRequestString("newexamId");
        if (StringUtils.isBlank(id)) {
            return "redirect:/index.vpage";
        }
        NewExam newExam = newExamLoaderClient.load(id);
        if (newExam == null) {
            return "redirect:/index.vpage";
        }

        Long clazzId = null;
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        for (GroupMapper gm : groupMappers) {
            if (gm.getGroupType().equals(GroupType.TEACHER_GROUP) && gm.getSubject().equals(newExam.processSubject())) {
                clazzId = gm.getClazzId();
                break;
            }
        }

        Teacher teacher = getSubjectSpecifiedTeacher(newExam.getSubject());
        if (teacher == null) {
            return "redirect:/index.vpage";
        }

        if (!hasClazzTeachingPermission(teacher.getId(), clazzId)) {
            return "redirect:/index.vpage";
        }

        model.addAttribute("subject", newExam.getSubject());
        model.addAttribute("userId", studentId);
        model.addAttribute("id", id);
        model.addAttribute("clazzId", clazzId);

        return "teacherv3/newexam/report/independentstudentanswer";
    }
}
