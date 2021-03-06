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
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/mobileApi/homework")
@NoArgsConstructor
public class MobileHomeworkApiController extends AbstractMobileController {
    /**
     * 移动端英语作业，不包括测验
     */
    @RequestMapping(value = "english/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage englishHomeworkList() {
        return MapMessage.errorMessage("系统升级中");
    }

    /**
     * 移动端数学作业
     */
    @RequestMapping(value = "math/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public MapMessage mathList() {
        return MapMessage.errorMessage("系统升级中");
    }

    /**
     * 返回未完成作业数量，因为基础作业在mysql中。应试作业在mongo，
     * 计算当次作业已完成和未完成作业逻辑较复杂，所以直接利用取当次作业接口。
     * 可以尝试优化？
     */
    @RequestMapping(value = "count.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkCount() {
        return MapMessage.errorMessage("系统升级中");
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homeworkList() {
        return MapMessage.errorMessage("系统升级中");
    }

    @RequestMapping(value = "{flashGameName}/process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String process(@PathVariable("flashGameName") String flashGameName) {
        return JsonUtils.toJson(MapMessage.errorMessage("系统升级中"));
    }
}
