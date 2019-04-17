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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/teacher/workbookhomework")
public class TeacherWorkbookHomeworkController extends AbstractTeacherController {

    /**
     * 布置教辅作业
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.GET)
    public String batchAssignHomework(Model model, HttpServletRequest request) {
        return "redirect:teacher/index.vpage";
    }

    /**
     * ?level=2&f=h&step=&workbookIds=1,2,3
     * 布置教辅作业--更换教辅
     */
    @RequestMapping(value = "changeworkbook.vpage", method = RequestMethod.GET)
    public String changeBook(Model model) {
        return "redirect:teacher/index.vpage";
    }

    /**
     * 教辅作业 -- 作业列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String workbookHomeworkList(Model model) {
        return "redirect:teacher/index.vpage";
    }

    /**
     * 调整作业
     */
    @RequestMapping(value = "adjustment.vpage", method = RequestMethod.GET)
    public String adjustHomework(Model model) {
        return "redirect:teacher/index.vpage";
    }
}
