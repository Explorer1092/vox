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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Maofeng Lu
 * @since 13-12-17 下午8:19
 */
@Controller
@RequestMapping("/teacher/quiz")
public class TeacherQuizHistoryController extends AbstractTeacherController {
    @RequestMapping(value = "history/index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    @RequestMapping(value = "history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getHistoryList() {
        return MapMessage.errorMessage("功能已下线");
    }

    // 2014暑假改版 -- 英语测验历史详情
    @RequestMapping(value = "historydetail.vpage", method = RequestMethod.GET)
    public String historyDetail(@RequestParam(value = "quizId", required = false) Long quizId, @RequestParam(value = "clazzId", required = false) Long clazzId, Model model) {
        return "redirect:/teacher/index.vpage";
    }

    @RequestMapping(value = "mathhistorydetail.vpage", method = RequestMethod.GET)
    public String mathHistoryDetail(@RequestParam(value = "quizId", required = false) Long quizId, @RequestParam(value = "clazzId", required = false) Long clazzId, Model model) {
        return "redirect:/teacher/index.vpage";
    }

    //口语测验报告详情
    @RequestMapping(value = "oralhistorydetail.vpage", method = RequestMethod.GET)
    public String oralHistoryDetail(@RequestParam(value = "quizId", required = false) Long quizId, @RequestParam(value = "clazzId", required = false) Long clazzId, Model model) {
        //redmine #24269 口语下线
        return "redirect:/teacher/index.vpage";
    }
}
