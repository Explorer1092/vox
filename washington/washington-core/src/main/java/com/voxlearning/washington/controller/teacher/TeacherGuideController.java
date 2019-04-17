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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.LatestType;
import com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.latest.Latest_NewRegisterTeacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-12-18
 */

@Controller
@RequestMapping("/teacher/guide")
public class TeacherGuideController extends AbstractTeacherController {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    // 选择学校
    @RequestMapping(value = "selectschoolinner.vpage", method = RequestMethod.GET)
    public String selectSchoolInner() {
        return "teacherv3/guide/selectschoolinner";
    }

    // 选择学校学科
    @RequestMapping(value = "selectschoolsubject.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage selectSchool() {
        Teacher teacher = currentTeacher();
        if (teacher.hasValidSubject() && asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacher.getId()).getUninterruptibly() != null) {
            return MapMessage.errorMessage("您已经选择了学校和学科");
        }
        Long schoolId = getRequestLong("schoolId");
        String subjectText = getRequestParameter("subject", "");
        Subject subject = Subject.valueOf(subjectText);
        // FIXME hardcode primary school here
        MapMessage mesg = teacherServiceClient.setTeacherSubjectSchool(teacher, subject, Ktwelve.PRIMARY_SCHOOL, schoolId);
        // 发送动态
        if (mesg.isSuccess()) {
            final Latest_NewRegisterTeacher detail = new Latest_NewRegisterTeacher();
            detail.setUserId(teacher.getId());
            detail.setUserName(teacher.fetchRealname());
            detail.setUserImg(teacher.fetchImageUrl());
            detail.setUserSubject(subject.getValue());
            userServiceClient.createSchoolLatest(schoolId, LatestType.NEW_REGISTER_TEACHER)
                    .withDetail(detail).send();
        }
        return mesg;
    }

    // 参与十二五课题
    @RequestMapping(value = "participateinketi.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage participateInKeTi() {
        try {
            userAttributeServiceClient.setExtensionAttribute(currentUserId(), UserExtensionAttributeKeyType.KETI_125);
            return MapMessage.successMessage("参与成功");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return MapMessage.errorMessage("请重试");
        }
    }

}
