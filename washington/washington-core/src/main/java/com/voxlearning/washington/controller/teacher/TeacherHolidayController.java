/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.lang.util.MapMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/teacher/holiday")
@Deprecated
public class TeacherHolidayController extends AbstractTeacherController {
    // 点击布置假期作业，选择开始结束时间以及年级班级
    @RequestMapping(value = "bavhstac.vpage", method = RequestMethod.GET)
    public String batchAssignVacationHomework_SelectDateAndClazz(Model model, HttpServletRequest request) {
        return "redirect:/teacher/index.vpage";
    }

    // 点击删除假期作业
    @RequestMapping(value = "removevh.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeVacationHomework() {
        return MapMessage.errorMessage("功能已下线");
    }

    // 寒假作业列表
    @RequestMapping(value = "vhindex.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    // 查看某个班级的成绩评语
    @RequestMapping(value = "clazzvhscore.vpage", method = RequestMethod.GET)
    public String clazzVacationHomeworkScore(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    // 写评语
    @RequestMapping(value = "wvhc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage writeVacationHomeworkComment(@RequestBody Map<String, Object> mapper) {
        return MapMessage.errorMessage("功能已下线");
    }

    // 教师发送学豆
    @RequestMapping(value = "abts.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage awardBeanToStudent() {
        return MapMessage.errorMessage("功能已下线");
    }

    // 查看某个学生的成绩
    @RequestMapping(value = "svhreport.vpage", method = RequestMethod.GET)
    public String studentVacationHomeworkReport(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    // 回看假期作业某个package的应试部分试题ids
    @RequestMapping(value = "replay.vpage", method = RequestMethod.GET)
    public String replay(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    // 获取假期作业某个package的应试部分的学生结果
    @RequestMapping(value = "paperanswer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map paperAnswer() {
        return MapMessage.errorMessage("功能已下线");
    }
}
