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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-8-13
 */
@Controller
@RequestMapping("/teacher/clazz/alteration")
public class ClazzTeacherAlterationController extends AbstractTeacherController {

    /**
     * 5.教师查看成功的操作记录
     */
    @RequestMapping(value = "checkhistory.vpage", method = RequestMethod.GET)
    public String checkHistory() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/systemclazz/clazzindex.vpage";
    }

    /**
     * 6.教师查看未处理的申请记录
     */
    @RequestMapping(value = "unprocessedapplication.vpage", method = RequestMethod.GET)
    public String pengdingList() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/systemclazz/clazzindex.vpage";
    }

}
