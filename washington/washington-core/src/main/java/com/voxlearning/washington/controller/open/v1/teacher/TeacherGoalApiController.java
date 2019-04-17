/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guoqiang.li
 * @since 2017/3/1
 */
@RestController
@RequestMapping(value = "/v1/teacher/goal")
public class TeacherGoalApiController extends AbstractTeacherApiController {
    /**
     * 班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loadClazzList() {
        return failMessage("功能已下线");
    }

    /**
     * 知识点概要
     */
    @RequestMapping(value = "summary.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage summaryInfo() {
        return failMessage("功能已下线");
    }

    /**
     * 保存教学目标设置
     */
    @RequestMapping(value = "saveteachingobjective.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage saveTeachingObjective() {
        return failMessage("功能已下线");
    }
}