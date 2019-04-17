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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/teacher/message")
public class TeacherMessageController extends AbstractTeacherController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/index.vpage#/teacher/message/index.vpage";
    }

    /**
     * 显示系统消息列表页面--分页
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/index.vpage#/teacher/message/list.vpage";
    }
}
