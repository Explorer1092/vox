package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user/verification")
public class MobileVerificationController extends AbstractController {

    /**
     * 家长绑定孩子，发送验证码
     * @return
     */
    @RequestMapping(value = "bindstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindStudentVerification () {
        String captchaToken = getRequestString("captchaToken");
        String captchaCode = getRequestString("captchaCode");
        String studentMobile = getRequestString("studentMobile");
        if (StringUtils.isAnyBlank(captchaToken, captchaCode, studentMobile)) {
            return MapMessage.errorMessage("参数错误");
        }
        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return MapMessage.errorMessage("验证码错误或者失效，请重新输入");
        }
        if (currentUserId() == null) {
            return MapMessage.errorMessage("登录状态失效");
        }
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(studentMobile, UserType.STUDENT);
        if (userAuthentication != null) {
            return MapMessage.errorMessage("该手机号已经绑定了其他学生, 请重新输入");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), studentMobile, SmsType.APP_PARENT_VERIFY_MOBILE_CENTER.name());
    }
}
