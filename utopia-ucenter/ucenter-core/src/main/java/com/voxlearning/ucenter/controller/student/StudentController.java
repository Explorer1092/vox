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

package com.voxlearning.ucenter.controller.student;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.conversation.client.ConversationLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author changyuan.liu
 * @since 2015.12.16
 */
@Controller
@RequestMapping("/student")
public class StudentController extends AbstractWebController {

    @Inject private ConversationLoaderClient conversationLoaderClient;
    @Inject private MessageServiceClient messageServiceClient;

    @ImportService(interfaceClass = VerificationService.class)
    private VerificationService verificationService;

    // 2014暑期改版 -- 学生端首页 -- 气泡信息
    @RequestMapping(value = "bubbles.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage indexBubble() {
        User student = currentUser();
        // UNREAD SYSTEM MESSAGE
        int unreadMessageCount = messageServiceClient.getMessageService().getUnreadMessageCount(student.narrow());
        //int unreadLetterCount = conversationLoaderClient.getConversationLoader().getUnreadLetterCount(student.getId());
        return MapMessage.successMessage().add("unreadTotalCount", unreadMessageCount);
    }

    // 2014暑期改版 -- 新手任务卡 -- 送手机验证码
    @RequestMapping(value = "sendmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map sendMobileCode() {
        try {
            String mobile = getRequest().getParameter("mobile");
            if (!MobileRule.isMobile(mobile)) {
                return MapMessage.errorMessage("非法的手机号");
            }

            List<ClazzTeacher> teachers = userAggregationLoaderClient.loadStudentTeachers(currentUserId());
            if (CollectionUtils.isNotEmpty(teachers)) {
                Map<Long, UserAuthentication> userAuthentications = userLoaderClient.loadUserAuthentications(teachers
                        .stream()
                        .map(teacher -> teacher.getTeacher().getId())
                        .collect(Collectors.toSet())
                );

                boolean isMyTeacherMobile = false;
                for (UserAuthentication ua : userAuthentications.values()) {
                    if (ua.isMobileAuthenticated()) {
                        if (sensitiveUserDataServiceClient.mobileEquals(ua.getSensitiveMobile(), mobile))
                            isMyTeacherMobile = true;
                    }
                }
                if (isMyTeacherMobile) {
                    return MapMessage.errorMessage("不能绑定老师手机号！");
                }
            }
            return smsServiceHelper.sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.STUDNET_VERIFY_MOBILE_NONAME_POPUP);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    // 2014暑期改版 -- 新手任务卡 -- 验证手机
    @RequestMapping(value = "nonameverifymobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage noNameVeriyMobile() {
        String code = getRequestParameter("code", "");
        if (StringUtils.isBlank(code)) {
            return MapMessage.errorMessage("信息不全");
        }

        return verificationService.verifyMobile(currentUserId(), code, SmsType.STUDNET_VERIFY_MOBILE_NONAME_POPUP.name());
    }

    @RequestMapping(value = "freezestudent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage freezestudent() {
        Long studentId = currentUser().getId();
        if (studentId == null) {
            return MapMessage.errorMessage("用户id缺失");
        }
        return studentServiceClient.freezeStudent(studentId, true);
    }

    @RequestMapping(value = "forbidstudent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage forbidstudent() {
        Long studentId = currentUser().getId();
        if (studentId == null) {
            return MapMessage.errorMessage("用户id缺失");
        }
        return studentServiceClient.forbidStudent(studentId, true);
    }


}
