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

package com.voxlearning.ucenter.controller.student;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.ucenter.support.SessionUtils;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * @author changyuan.liu
 * @since 2015/6/18
 */
@Slf4j
@Controller
@RequestMapping("/student/systemclazz")
public class StudentSystemClazzController extends AbstractWebController {

    @Inject private SmsServiceClient smsServiceClient;

    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;

    /**
     * 学生关联老师并加入班级
     *
     * @return
     * @author changyuan.liu
     */
    @RequestMapping(value = "joinclazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage joinSystemClazz() {
        long teacherId = getRequestLong("teacherId");
        if (teacherId == 0) {
            return MapMessage.errorMessage("No teacherId specified.");
        }

        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("No clazzId specified.");
        }

        Long studentId = currentUserId();

        if (MobileRule.isMobile(SafeConverter.toString(teacherId))) {
            // 手机号情况
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(String.valueOf(teacherId), UserType.TEACHER);
            teacherId = ua == null ? 0 : ua.getId();
        }

        MapMessage message = studentSystemClazzServiceClient.studentJoinClazz(studentId, teacherId, clazzId, getRequestBool("forceLink"), OperationSourceType.pc);
        if (message.isSuccess()) {
            //加入快乐学
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacherDetail != null && (teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher())) {
                newKuailexueServiceClient.joinKlxClazz(clazzId, teacherId, studentId);
            }

            boolean jumpMSPage = SafeConverter.toBoolean(message.get("jumpMSPage"));
            if (jumpMSPage) {
                VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Student", studentId);
                if (vendorAppsUserRef != null) {
                    vendorServiceClient.expireSessionKey(
                            "17Student",
                            studentId,
                            SessionUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), studentId));
                }
            }
        }
        return message;
    }


    @RequestMapping(value = "sendSCTCode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendStudentChangeTeacherCode() {
        String mobile = getRequestString("mobile");
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("错误的手机号");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.STUDENT_CHANGE_TEACHER.name());
    }

    @RequestMapping(value = "verifySCTCode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyStudentChangeTeacherCode() {
        String code = getRequestString("code");
        if (code == null) {
            return MapMessage.errorMessage("验证码错误");
        }

        return smsServiceClient.getSmsService().verifyValidateCode(currentUserId(), code, SmsType.STUDENT_CHANGE_TEACHER.name());
    }


}
