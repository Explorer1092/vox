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

import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * @author changyuan.liu
 * @since 2015/7/28
 */
@Controller
@RequestMapping("/teacher/subjectivehomework")
public class TeacherSubjectiveHomeworkController extends AbstractTeacherController {

    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    /**
     * 布置主观作业
     *
     * @author changyuan.liu
     * @param model model
     * @param request request
     * @return url
     */
    @RequestMapping(value = "assignhomework.vpage", method = RequestMethod.GET)
    public String clickBatchAssignSubjectiveHomework(Model model, HttpServletRequest request) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 调整作业
     *
     * @author changyuan.liu
     * @param model model
     * @return url
     */
    @RequestMapping(value = "adjustsubjectivehomework.vpage", method = RequestMethod.GET)
    public String adjustSubjectiveHomework(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 主观作业历史--班级列表
     */
    @RequestMapping(value = "history/index.vpage", method = RequestMethod.GET)
    public String historyList(Model model) {
        return "redirect:/teacher/new/homework/report/list.vpage";
    }

    /**
     * 主观作业详情
     *
     * @author changyuan.liu
     * @param model model
     * @return url
     */
    @RequestMapping(value = "history/detail.vpage", method = RequestMethod.GET)
    public String historyDetail(Model model) {
        return "redirect:/teacher/index.vpage";
    }
}
