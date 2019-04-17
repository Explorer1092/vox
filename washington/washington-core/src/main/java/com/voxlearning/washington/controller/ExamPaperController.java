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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;


@Controller
@RequestMapping("/exam/paper")
@Slf4j
@NoArgsConstructor
public class ExamPaperController extends AbstractController {

    /**
     * 组卷首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/teacher/index.vpage";
    }


    /**
     * Math组卷首页
     */
    @RequestMapping(value = "math/index.vpage", method = RequestMethod.GET)
    public String mathIndex(Model model) {

        return "redirect:/teacher/index.vpage";
    }


    /**
     * 我的全部试卷页面
     */
    @RequestMapping(value = "mypaperindex.vpage", method = RequestMethod.GET)
    public String myPaperIndex(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 试题预览
     * paramMap 的KEY取值
     * <pre>
     *     {
     *         examIds : 试题ID,逗号分隔
     *         bookId : 课本ID
     *         userId : 当前用户ID
     *     }
     * </pre>
     */
    @RequestMapping(value = "questionpreview.vpage", method = RequestMethod.POST)
    public String findQuestionQuickPreview(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    /**
     * 试卷预览
     * paramMap 的KEY取值
     * <pre>
     *     {
     *         paperId : 试卷ID
     *         property: 试卷属性，取值：standard | teacher,含义：标准试卷 | 老师试卷
     *         userId : 当前用户ID
     *     }
     * </pre>
     */
    @RequestMapping(value = "exampaperpreview.vpage", method = RequestMethod.GET)
    public String preview(Model model) {
        return "redirect:/teacher/index.vpage";
    }
}
