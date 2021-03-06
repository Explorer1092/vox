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

package com.voxlearning.washington.controller.mobile.api;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/mobileApi/learning")
@NoArgsConstructor
public class MobileLearningApiController extends AbstractMobileController {

    /**
     * 学生所在班级指定教材
     */
    @RequestMapping(value = "book.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learning() throws Exception {
        return MapMessage.errorMessage("系统升级中");
    }

    /**
     * 教材的单元查询
     */
    @RequestMapping(value = "unit.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningUnit() {
        return MapMessage.errorMessage("系统升级中");
    }

    /**
     * 加载lesson及对应所有Practice
     */
    @RequestMapping(value = "loadLesson.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningLesson() {
        return MapMessage.errorMessage("系统升级中");
    }
}
