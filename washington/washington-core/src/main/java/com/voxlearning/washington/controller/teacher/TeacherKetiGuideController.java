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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.VerificationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/teacher/keti/guide")
public class TeacherKetiGuideController extends AbstractTeacherController {

    @ImportService(interfaceClass = VerificationService.class)
    private VerificationService verificationService;

    /**
     * 课题老师引导流程--修改姓名，验证手机
     */
    @RequestMapping(value = "addnameandmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addKetiTeacherNameAndCellNum(@RequestBody Map<String, Object> mapper) {
        try {
            Long teacherId = currentUserId();
            String code = (String) mapper.get("latestCode");
            String name = (String) mapper.get("name");
            return verificationService.ketiVerifyMobile(teacherId, code, name);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("手机验证失败");
        }
    }

    /**
     * 课题教师发送手机验证码
     */
    @RequestMapping(value = "sendmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map ketiSendMobileCode() {
        try {
            String mobile = getRequest().getParameter("mobile");
            return getSmsServiceHelper().sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.TEACHER_VERIFY_MOBILE_REGISTER_KETI);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }
}
