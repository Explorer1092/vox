/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author tanguohong
 * @since 2015-8-26
 */
@Controller
@RequestMapping("/student/workbook/homework")
public class StudentWorkbookHomeworkController extends AbstractController {

    /**
     * 学生首页->开始教辅作业
     */
    @RequestMapping(value = "go.vpage", method = RequestMethod.GET)
    public String go(Model model) {
        return "redirect:/student/index.vpage";
    }


    /**
     * 上传教辅作业
     */
    @RequestMapping(value = "process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage process(@RequestBody Map<String, Object> map) {
        return MapMessage.successMessage("此功能已下线，如有疑问请联系客服");
    }

    /**
     * 教辅B模式，扫描全品上的二维码，跳转，然后呢？
     *
     * @since 2015-9-23
     * xuesong.zhang
     */
    @RequestMapping(value = "qr/bmode.vpage", method = RequestMethod.GET)
    public String qrBmode(Model model) {
        return "redirect:/student/index.vpage";
    }
}
