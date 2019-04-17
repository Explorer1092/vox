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

/**
 * Teacher homework history controller implementation.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @since 2013-03-28 11:48
 */
@Controller
@RequestMapping("/teacher/homework")
public class TeacherHomeworkHistoryController extends AbstractTeacherController {

    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    /**
     * NEW == 作业管理 -- 作业历史--班级列表
     */
    @RequestMapping(value = "history/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    @RequestMapping(value = "history/detail.vpage", method = RequestMethod.GET)
    public String historyDetail1(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    @RequestMapping(value = "history/student/detail.vpage", method = RequestMethod.GET)
    public String historyStudentDetail1(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    @RequestMapping(value = "history/error/rate.vpage", method = RequestMethod.GET)
    public String historyErrorRate(Model model) {
        return "redirect:/teacher/index.vpage";
    }
}
