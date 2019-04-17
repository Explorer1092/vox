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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.api.constant.ClazzConstants;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * @author changyuan.liu
 * @since 2015/6/18
 */
@Slf4j
@Controller
@RequestMapping("/student/systemclazz")
public class StudentSystemClazzController extends AbstractController {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private GlobalTagServiceClient globalTagServiceClient;
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

    /**
     * 学生关联老师
     *
     * @return
     * @author changyuan.liu
     */
    @RequestMapping(value = "linkteacher.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage linkTeacher() {
        long teacherId = getRequestLong("teacherId");
        if (teacherId == 0) {
            return MapMessage.errorMessage("No teacherId specified.");
        }

        long clazzId = getRequestLong("clazzId");
        String clazzName = getRequestString("clazzName");
        if (clazzId == 0) {
            return MapMessage.errorMessage("No clazzId specified.");
        }

        if (String.valueOf(teacherId).length() == 11) {
            // 手机号情况
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(String.valueOf(teacherId), UserType.TEACHER);
            teacherId = ua == null ? 0 : ua.getId();
        }

        User student = currentStudent();
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();

        //TODO 各种验证
        Integer maxCapacity = globalTagServiceClient.getGlobalTagBuffer()
                .loadSchoolMaxClassCapacity(school.getId(), ClazzConstants.MAX_CLAZZ_CAPACITY);
        MapMessage message = groupServiceClient.linkStudentTeacher(student.getId(), teacherId, clazzId, false, maxCapacity, OperationSourceType.pc);

        // 发送消息
        if (message.isSuccess()) {
            String content = StringUtils.formatMessage("{}同学于{}加入了{} ",
                    StringUtils.defaultString(student.fetchRealname()),
                    DateUtils.dateToString(new Date(), "yyyy年MM月dd日"),
                    clazzName);
            String append = ";不想新学生加入班级？ <a href=\"http://help.17zuoye.com/?p=509\" class=\"w-blue\" target=\"_blank\">【点这里】</a>";

            teacherLoaderClient.sendTeacherMessage(teacherId, content + append);

            // 通知所有共享关联关系的老师
            List<Teacher> teachers = teacherLoaderClient.loadSharedTeachers(teacherId, clazzId, false);
            for (Teacher teacher : teachers) {
                teacherLoaderClient.sendTeacherMessage(teacher.getId(), content + append);
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
