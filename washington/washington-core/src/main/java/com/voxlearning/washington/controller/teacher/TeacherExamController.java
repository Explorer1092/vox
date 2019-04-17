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
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/teacher/exam")
public class TeacherExamController extends AbstractController {

    @Inject private AsyncUserServiceClient asyncUserServiceClient;

    @RequestMapping(value = "deleteexampaper.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteTeacherExamPaper(@RequestParam("paperId") String paperId) {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "deletemathexampaper.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteMathTeacherExamPaper(@RequestParam("paperId") String paperId) {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "getknowledge/{unitId}.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String getKnoledgePoingByUint(@PathVariable("unitId") Long unitId) {
        return "null"; // FIXME: why string "null" ?
    }

    @RequestMapping(value = "previewresource.vpage", method = RequestMethod.GET)
    public String previewResource(@RequestParam("examId") String examId, @RequestParam("owener") String owener, Model model, HttpServletRequest request) {
        return "redirect:/teacher/index.vpage";
    }


    @RequestMapping(value = "previewmathresource.vpage", method = RequestMethod.GET)
    public String previewMathResource(@RequestParam("examId") String examId, @RequestParam("owner") String owner, Model model, HttpServletRequest request) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 测验菜单下
     * NEW--获得多单元知识点
     */
    @RequestMapping(value = "getMoreUnitknowledge.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String getMoreKnoledgePoingByUints(HttpServletRequest request) {
        return "null"; //string "null" 是与flash的之间规定
    }

    /**
     * 随堂练题目预览
     */
    @RequestMapping(value = "viewpaper.vpage", method = RequestMethod.GET)
    public String viewPaper(Model model, HttpServletRequest request) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 老师查看某个学生做某个作业的情况
     */
    @RequestMapping(value = "paperanswer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map paperAnswer(HttpServletRequest request) {

        return MapMessage.errorMessage("功能已下线");
    }

    /**
     * 组卷大赛活动
     */
    @RequestMapping(value = "activity/{type}.vpage", method = RequestMethod.GET)
    public String activityIndex(HttpServletRequest request, Model model, @PathVariable("type") String type) {
        return "redirect:/teacher/index.vpage";
    }


    /**
     * 普通测验试卷预览
     */
    @RequestMapping(value = "viewquizpaper.vpage", method = RequestMethod.GET)
    public String viewQuizPaper(Model model, HttpServletRequest request) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 老师查看某个学生做某个测验的情况
     */
    @RequestMapping(value = "quizpaperanswer.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map quizPaperAnswer(HttpServletRequest request) {

        return MapMessage.errorMessage("功能已下线");
    }
}
