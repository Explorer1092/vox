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


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: xinqiang.wang
 * Date: 13-12-25
 * Time: 下午4:12
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/teacher/report")
public class TeacherReportController extends AbstractTeacherController {

    // 汇总报告首页
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String reportIndex(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    // 查看报告详情
    @RequestMapping(value = "details.vpage", method = RequestMethod.GET)
    public String reportDetailsList(Model model) {
        return "redirect:/teacher/index.vpage";
    }

    // 批量发学豆
    @RequestMapping(value = "batchsendintegral.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchSendIntegral(@RequestBody Map<String, Object> jsonMap) {
        Long teacherId = getSubjectSpecifiedTeacherId(Subject.valueOf(SafeConverter.toString(jsonMap.get("subject"))));
        try {
            return atomicLockManager.wrapAtomic(newHomeworkServiceClient)
                    .keys("batchSendIntegral", teacherId)
                    .proxy()
                    .batchRewardStudentIntegral(teacherId, jsonMap);
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.errorMessage("处理中，请不要重复点击");
            }
            logger.error(getClass().getName() + ex.getMessage(), ex);
            return MapMessage.errorMessage("操作失败，请重试");
        }
    }
}
