package com.voxlearning.utopia.mizar.controller.auth;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarUserServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Login controller
 * Created by wangshichao on 16/9/6.
 */
@Controller
@RequestMapping(value = "/auth")
public class MizarAuthController extends AbstractMizarController {

    @Inject private SmsServiceClient smsServiceClient;
    @Inject private MizarUserLoaderClient mizarUserLoaderClient;
    @Inject private MizarUserServiceClient mizarUserServiceClient;

    @RequestMapping(value = "getSMSCode.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSMSCode() {

        String mobile = getRequestString("mobile");
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("请填写正确的手机号码");
        }
        MizarUser mizarUser = mizarUserLoaderClient.getRemoteReference().loadUserByMobile(mobile);
        if (mizarUser == null) {
            return MapMessage.errorMessage("手机号未注册");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.APP_CRM_MOBILE_LOGIN.name(), false);
    }


    @RequestMapping(value = "logout.vpage", method = RequestMethod.GET)
    public String logout() {
        MizarAuthUser curUser = getCurrentUser();
        if (curUser != null) {
            String cacheKey = MizarAuthUser.ck_user(curUser.getUserId());
            CacheSystem.CBS.getCache("unflushable").delete(cacheKey);
        }

        removeUserFromCookie();
        return "redirect: /index.vpage";
    }


    @RequestMapping(value = "modifyPass.vpage", method = RequestMethod.GET)
    public String modifyPass() {
        return "changepassword";
    }

    @RequestMapping(value = "getBackPass.vpage", method = RequestMethod.GET)
    public String getBackPass() {
        return "getbackpassword";
    }

    @RequestMapping(value = "fa.vpage", method = RequestMethod.GET)
    public String forbiddenAccess() {
        return "forbiddenaccess";
    }

    @RequestMapping(value = "login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Object loginPost() {

        String userName = getRequestString("username");
        String password = getRequestString("password");
        if (StringUtils.isEmpty(userName)) {
            return MapMessage.errorMessage("用户名不能为空");
        }
        if (StringUtils.isEmpty(password)) {
            return MapMessage.errorMessage("密码不能为空");
        }
        MizarUser mizarUser = mizarUserServiceClient.getRemoteReference().login(userName, password);

        if (mizarUser == null) {
            return MapMessage.errorMessage("用户名或密码错误");
        }
        if (mizarUser.getStatus() == 0) {
            setUserToCookie(mizarUser.getId());
            Map<String, Object> map = genMap(false, 1001, "请修改密码");
            return MapMessage.of(map);
        } else if (mizarUser.getStatus() == 9) {
            return MapMessage.errorMessage("该账户已经冻结");
        } else {
            setUserToCookie(mizarUser.getId());
            return MapMessage.successMessage();
        }
    }

    private Map<String, Object> genMap(boolean isSuccess, Integer code, String info) {

        Map<String, Object> map = new HashMap<>();
        map.put("success", isSuccess);
        if (null != code) {
            map.put("code", 1001);
        }
        if (null != info) {
            map.put("info", info);
        }
        return map;
    }


    @RequestMapping(value = "isLogin.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage isLogin() {

        String userId = currentUserId();
        if (userId == null) {
            MapMessage.errorMessage();
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "resetPassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPassWord() {

        String userId = currentUserId();
        String mobile = getRequestString("mobile");
        if (!MobileRule.isMobile(mobile)) {

            Map<String, Object> map = genMap(false, 2001, "请填写正确的手机号码");
            return MapMessage.of(map);
        }
        String password = getRequestString("password");
        if (StringUtils.isEmpty(password)) {
            Map<String, Object> map = genMap(false, 2003, "请填写密码");
            return MapMessage.of(map);
        }
        String passwordConfirm = getRequestString("passwordConfirm");
        if (StringUtils.isEmpty(passwordConfirm)) {
            Map<String, Object> map = genMap(false, 2004, "请填写确认密码密码");
            return MapMessage.of(map);
        }
        String code = getRequestString("captchaCode");
        if (StringUtils.isEmpty(code)) {
            Map<String, Object> map = genMap(false, 2002, "请填写验证码");
            return MapMessage.of(map);
        }
        if (!password.equals(passwordConfirm)) {
            Map<String, Object> map = genMap(false, 2004, "两次填写密码不同");
            return MapMessage.of(map);
        }
        MapMessage mapMessage = verifySmsCode(mobile, code);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }

        // 如果未传入用户id，则通过手机号找用户，拿到userId
        if(StringUtils.isBlank(userId)){
            userId = Optional.ofNullable(mizarUserLoaderClient.loadUserByMobile(mobile))
                    .map(MizarUser::getId)
                    .orElse(null);
        }

        if(StringUtil.isBlank(userId)){
            return MapMessage.errorMessage("手机号未绑定!");
        }

        MapMessage message = mizarUserServiceClient.getRemoteReference().editMizarUserPassWord(userId, password);
        // 密码修改成功之后退出重新登录
        if (message.isSuccess()) {
            MizarAuthUser curUser = getCurrentUser();
            if (curUser != null) {
                String cacheKey = MizarAuthUser.ck_user(curUser.getUserId());
                CacheSystem.CBS.getCache("unflushable").delete(cacheKey);
            }
            removeUserFromCookie();
        }
        return message;
    }


    private MapMessage verifySmsCode(String mobile, String code) {

        SmsType smsType = SmsType.APP_CRM_MOBILE_LOGIN;
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效手机号");
        }

        return smsServiceClient.getSmsService().verifyValidateCode(mobile, code, smsType.name());
    }

}
