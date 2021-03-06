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

import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author RuiBao
 * @since 9/24/2015
 */
@Controller
@RequestMapping("/teacher/unitreport")
public class TeacherUnitReportController extends AbstractController {

    /**
     * 单元报告首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 单元报告详情页
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        return "redirect:/teacher/index.vpage";
    }
}
