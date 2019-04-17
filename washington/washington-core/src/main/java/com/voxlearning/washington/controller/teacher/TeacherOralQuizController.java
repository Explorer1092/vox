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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;

/**
 * @author Maofeng Lu
 * @since 14-11-28 下午10:56
 */
@Controller
@RequestMapping("/teacher/oralquiz")
public class TeacherOralQuizController extends AbstractTeacherController {

    //试卷预览页面
    @RequestMapping(value = "paperpreview.vpage", method = RequestMethod.GET)
    public String previewOralPaper(HttpServletRequest request, Model model) {
        return "redirect:/teacher/index.vpage";
    }


    // 预览口语题
    @RequestMapping(value = "oralpreview.vpage", method = {RequestMethod.GET,RequestMethod.POST})
    public String previewOralQuestion(HttpServletRequest request, Model model) {
        return "redirect:/teacher/index.vpage";
    }

    // flash load口语测试题
    @RequestMapping(value = "loadquestions.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadOralQuestions(HttpServletRequest request){
        return MapMessage.successMessage().add("allQuestions", new LinkedList<>());
    }

}
