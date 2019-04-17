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

@Controller
@RequestMapping("/teacher/clazzwork")
public class TeacherClazzworkController extends AbstractTeacherController {

    /**
     * 测试 列表
     * 数学测验 OK
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String clazzworklist(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 批量布置测验
     */
    @RequestMapping(value = "batch.vpage", method = RequestMethod.GET)
    public String clickBatchAssignEnglishClazzwork(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 测验首页
     * --调整测验
     */
    @RequestMapping(value = "quizadjustment.vpage", method = RequestMethod.GET)
    public String quizAdjustment(Model model) {
        return "redirect:/teacher/index.vpage";
    }


    /**
     * 批量布置口语测验
     * 口语测验对1-6年级都可以布置
     */
    @RequestMapping(value = "oralbatch.vpage", method = RequestMethod.GET)
    public String oralBatch(Model model) {
        //redmine #24269
        return "redirect:/teacher/index.vpage";
    }
}
