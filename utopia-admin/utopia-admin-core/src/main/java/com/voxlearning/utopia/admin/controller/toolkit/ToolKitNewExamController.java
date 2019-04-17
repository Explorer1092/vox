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

package com.voxlearning.utopia.admin.controller.toolkit;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamServiceClient;
import com.voxlearning.utopia.service.question.api.DPContentLibService;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.consumer.NewExamLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/5/31.
 */
@Controller
@Slf4j
@RequestMapping("/toolkit/newexam")
@NoArgsConstructor
public class ToolKitNewExamController extends ToolKitAbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private NewExamLoaderClient newExamLoaderClient;
    @Inject private NewExamServiceClient newExamServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @ImportService(interfaceClass = DPContentLibService.class)
    private DPContentLibService dpContentLibService;

    @RequestMapping(value = "change/directional.vpage", method = RequestMethod.POST)
    public String changeDirectional(@RequestParam(value = "newExamId", required = true) String newExamId) {

        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            getAlertMessageManager().addMessageError("考试不存在");
            return "toolkit/toolkit";
        }
        try {
            newExam.setDirectional(true);
            newExamLoaderClient.save(newExam);
            dpContentLibService.cleanBuffer("NewExam");
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("设置考试为定向考试失败");
            return "toolkit/toolkit";
        }
        getAlertMessageManager().addMessageSuccess("设置考试为定向考试成功");
        return "toolkit/toolkit";
    }

    @RequestMapping(value = "unregister.vpage", method = RequestMethod.POST)
    public String unregister(@RequestParam(value = "newExamId") String newExamId, @RequestParam(value = "userIds") String userIds) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            getAlertMessageManager().addMessageError("考试不存在");
            return "toolkit/toolkit";
        }
        try {
            List<Long> uids = Arrays.stream(StringUtils.split(userIds, ","))
                    .filter(StringUtils::isNotBlank)
                    .map(SafeConverter::toLong)
                    .collect(Collectors.toList());
            Map<Long, StudentDetail> longStudentDetailMap = studentLoaderClient.loadStudentDetails(uids);
            for (StudentDetail studentDetail : longStudentDetailMap.values()) {
                if (studentDetail != null && studentDetail.getClazz() != null) {
                    Clazz clazz = studentDetail.getClazz();
                    School school = schoolLoaderClient.getSchoolLoader()
                            .loadSchool(clazz.getSchoolId())
                            .getUninterruptibly();
                    if (school != null && school.getRegionCode() != null) {
                        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
                        newExamServiceClient.unRegisterNewExam(studentDetail, school, exRegion, newExamId, "pc", "crm");
                    }
                }
            }
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("批量取消报名失败");
            return "toolkit/toolkit";
        }
        getAlertMessageManager().addMessageSuccess("批量取消报名成功");
        return "toolkit/toolkit";
    }


    @RequestMapping(value = "register.vpage", method = RequestMethod.POST)
    public String register(@RequestParam(value = "newExamId", required = true) String newExamId, @RequestParam(value = "userIds", required = true) String userIds) {

        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            getAlertMessageManager().addMessageError("考试不存在");
            return "toolkit/toolkit";
        }
        try {
            for (String uid : StringUtils.split(userIds, ",")) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(SafeConverter.toLong(uid));
                if (studentDetail != null && studentDetail.getClazz() != null) {
                    Clazz clazz = studentDetail.getClazz();
                    School school = schoolLoaderClient.getSchoolLoader()
                            .loadSchool(clazz.getSchoolId())
                            .getUninterruptibly();
                    if (school != null && school.getRegionCode() != null) {
                        ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
                        newExamServiceClient.registerNewExam(studentDetail, school, exRegion, newExamId, "pc", "crm");
                    }
                }
            }
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("批量报名失败");
            return "toolkit/toolkit";
        }
        getAlertMessageManager().addMessageSuccess("批量报名成功");
        return "toolkit/toolkit";
    }
}
