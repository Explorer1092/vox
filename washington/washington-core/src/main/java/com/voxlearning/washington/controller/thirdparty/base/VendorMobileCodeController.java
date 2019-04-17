package com.voxlearning.washington.controller.thirdparty.base;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/auth/vendor")
@Slf4j
public class VendorMobileCodeController extends AbstractController {
    // 发送验证码
    @RequestMapping(value = "sendmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendMobileCode () {
        String mobile = getRequest().getParameter("mobile");
        if (StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("请输入正确的手机号码");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.APP_VERIFY_REST_PASSWORD_BY_USER_ID_MOBILE.name(), false);
    }
}
