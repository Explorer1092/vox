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

package com.voxlearning.luffy.controller.piclisten;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.luffy.controller.AbstractXcxController;
import com.voxlearning.luffy.controller.ApiConstants;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.enums.MiniProgramApi;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.luffy.controller.ApiConstants.RES_RESULT_NEED_RELOGIN_CODE;

/**
 * @author Xin Xin
 * @since 10/15/15
 */

@Controller
@Slf4j
@RequestMapping(value = "/xcx/signup")
public class SignUpController extends AbstractXcxController {
    private final static Integer sessionKey_expirationInSeconds = 86400 * 25;

    // 通过wx.login获取code 换取openid sessionKey 缓存
    @RequestMapping(value = "/auth.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage auth() {
        String code = getRequestString("code");
        if (StringUtils.isBlank(code))
            return MapMessage.errorMessage("没有 code");
        String openId = null;
        String sessionKey = null;
        try {
            FlightRecorder.dot("beforeGetOpenIdByCode");
            Map<String, String> data = getOpenIdAndSessionKeyByCode(code, MiniProgramType.PICLISTEN);
            openId = data.get("openId");
            sessionKey = data.get("sessionKey");
            FlightRecorder.dot("afterGetOpenIdByCode");
            if (null == openId || null == sessionKey) {
                return MapMessage.errorMessage("获取数据失败，请重试");
            }
            // 更新sessionKey  暂定25天(有资料说微信有效期是30天)
            luffyWebCacheSystem.CBS.persistence.set(openId, sessionKey_expirationInSeconds, sessionKey);
            // 设置登录cookie
            getRequestContext().setAuthenticatedOpenId(openId, 24 * 60 * 60);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("Piclisten MiniProgram auth error,openId:{} code:{}", openId, code, ex);
        }
        return MapMessage.errorMessage("认证失败，请稍候重试");
    }

    // 通过授权手机号进行登录
    @RequestMapping(value = "/authlogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage authLogin() {
        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("openId is not exist!").setErrorCode(ApiConstants.RES_RESULT_NO_OPEN_ID_CODE);
        }
        String iv = getRequestString("iv");
        String encryptedData = getRequestString("encryptedData");
        String nickName = getRequestString("nickName");  // 微信昵称
        String avatar = getRequestString("avatar");      // 微信头像
        if (StringUtils.isBlank(iv) || StringUtils.isBlank(encryptedData))
            return MapMessage.errorMessage("参数错误");
        String sessionKey = getSessionKeyByOpenId(openId);
        if (StringUtils.isBlank(sessionKey)) {
            return MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NO_SESSION_KEY_CODE).setInfo("no sessionKey!");
        }
        try {
            // 解密
            String phoneNo = getDecryptData(iv, encryptedData, sessionKey, "purePhoneNumber");
            if (StringUtils.isBlank(phoneNo) || !MobileRule.isMobile(phoneNo)) {
                log.error("PicListen MiniProgram decode mobile faild, please check, {}, {}, {}, {}", sessionKey, iv, encryptedData, phoneNo);
                return MapMessage.errorMessage("手机号授权失败");
            }
            return doLogin(phoneNo, openId, nickName, avatar, "one_key");
        } catch (Exception ex) {
            log.error("Piclisten MiniProgram authlogin error,openId:{}", openId, ex);
        }
        return MapMessage.errorMessage("登录失败，请稍候重试");
    }

    @RequestMapping(value = "/verifycode/getcid.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCid() {
        return MapMessage.successMessage().add("cid", tokenHelper.generateContextId(getRequestContext()));
    }

    @RequestMapping(value = "/verifycode/get.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getVerifyCode() {
        String mobile = getRequestString("mobile");
        String contextId = getRequestString("cid");
        try {
            if (!MobileRule.isMobile(mobile)) {
                return MapMessage.errorMessage("参数错误");
            }
            if (!tokenHelper.verifyAndConsumeContextId(contextId)) {
                return MapMessage.errorMessage("您发送的太频繁了,请稍侯再试~");
            }
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.PARENT_VERIFY_MOBILE_XCX_REGISTER.name(),
                    false);
        } catch (Exception ex) {
            log.error("Send sms code for parent mobile bind failed,mobile:{},contextId:{}", mobile, ex);
            return MapMessage.errorMessage("发送失败");
        }
    }


    @RequestMapping(value = "/loadchilds.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadChilds() {
        User parent = currentUser();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        // 获取家长callName 手机号
        List<StudentParentRef> refs = parentLoaderClient.loadParentStudentRefs(parent.getId());
        String callName = "";
        if (CollectionUtils.isNotEmpty(refs)) {
            callName = refs.get(0).getCallName();
        }
        String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(parent.getId());
        return MapMessage.successMessage().add("childs", studentLoaderClient.loadParentStudents(parent.getId()))
                .add("callName", callName)
                .add("mobile", mobile);
    }

    @RequestMapping(value = "/verifycode/login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifiedLoginPost() {
        String mobileNumber = getRequestString("mobile");
        String verifyCode = getRequestString("code");
        String nickName = getRequestString("nickName");  // 微信昵称
        String avatar = getRequestString("avatar");      // 微信头像

        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("openId无效").setErrorCode(ApiConstants.RES_RESULT_NO_OPEN_ID_CODE);
        }

        if (!MobileRule.isMobile(mobileNumber)) {
            return MapMessage.errorMessage("请输入正确的手机号");
        }

        if (StringUtils.isBlank(verifyCode))
            return MapMessage.errorMessage("请输入正确验证码");
        try {
            // 验证短信验证码
            MapMessage verifySmsMessage = smsServiceClient.getSmsService().verifyValidateCode(mobileNumber, verifyCode, SmsType.PARENT_VERIFY_MOBILE_XCX_REGISTER.name());
            if (!verifySmsMessage.isSuccess()) {
                return verifySmsMessage;
            }

            return doLogin(mobileNumber, openId, nickName, avatar, "verify");
        } catch (Exception ex) {
            log.error("Verified Login Failed, mobile:{},code:{}", mobileNumber, verifyCode, ex);
            return MapMessage.errorMessage("登录失败");
        }
    }

    // 执行登录逻辑 根据手机号
    private MapMessage doLogin(String mobileNumber, String openId, String nickName, String avatar, String refer) {
        String source = "parent_piclisten_miniProgram" + "_" + refer;
        User parent;
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobileNumber, UserType.PARENT);
        if (userAuthentication == null) {
            parent = newChannelCParentLogin(mobileNumber);
            if (parent == null)
                return MapMessage.errorMessage("创建新用户失败");
            userAuthentication = userLoaderClient.loadMobileAuthentication(mobileNumber, UserType.PARENT);
        } else {
            parent = userLoaderClient.loadUser(userAuthentication.getId());
        }
        if (parent == null || userAuthentication == null)
            return MapMessage.errorMessage("获取用户信息失败");

        wechatServiceClient.bindUserAndMiniProgramOrRelogin(parent.getId(), openId, source, MiniProgramType.PICLISTEN);
        // check child
        List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
        if (CollectionUtils.isEmpty(students)) {
            // register child and bind
            MapMessage message = registerStudentAndBind(parent, mobileNumber);
            if (!message.isSuccess()) {
                return message;
            }
        }
        // 更新用户头像昵称
        updateParentWechatInfo(nickName, avatar, parent.getId());
        //设置登录cookie
        getRequestContext().saveAuthenticateState(24 * 60 * 60, parent.getId(), userAuthentication.getPassword(), openId, RoleType.ROLE_PARENT);
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(parent.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wechat);
        return MapMessage.successMessage();
    }

    private void updateParentWechatInfo(String nickName, String avatar, Long userId) {
        ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(userId);
        if (parentExtAttribute == null) {
            parentExtAttribute = new ParentExtAttribute(userId);
        }
        if (StringUtils.isNotBlank(nickName)) {
            parentExtAttribute.setWechatNick(nickName);
        }
        if (StringUtils.isNotBlank(avatar)) {
            parentExtAttribute.setWechatImage(avatar);
        }
        parentServiceClient.updateParentExtAttribute(parentExtAttribute);
    }


    private Map<String, String> getOpenIdAndSessionKeyByCode(String code, MiniProgramType miniProgramType) {
        Map<String, String> data = new HashMap<>();


        String url = MiniProgramApi.JSCODE2SESSION.url(ProductConfig.get(miniProgramType.getAppId()), ProductConfig.get(miniProgramType.getAppSecret()), code);

        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).get(url).execute();
        if (null == response.getResponseString()) {
            logger.warn("Get openId by code from weixin failed, response nothing.");
            return data;
        }

        Map<String, Object> result = JsonUtils.fromJson(response.getResponseString());
        if (!MapUtils.isEmpty(result) && !result.keySet().contains("errcode")) {
            data.put("openId", (String) result.get("openid"));
            data.put("sessionKey", (String) result.get("session_key"));
            return data;
        } else if (!MapUtils.isEmpty(result) && result.keySet().contains("errcode") && "40029".equals(result.get("errcode").toString())) {
            //微信有bug,菜单点击后会发出两次请求,第一次请求会被微信终止进程,导致第二次接收到的是已经接收过一次的,此时code已经失效
            //do nothing
        } else {
            logger.error("Get openId by code from oauth failed,code:{},response:{}", code, response.getResponseString());
        }
        return data;
    }

    private User newChannelCParentLogin(String mobile) {
        MapMessage mapMessage = registerChannelCParent(mobile);

        Object user = mapMessage.get("user");
        if (user == null || !(user instanceof User))
            return null;
        return (User) user;
    }

    private MapMessage registerChannelCParent(String mobile) {
        //初始化要注册的用户
        NeonatalUser neonatalUser = initChannelCUser(UserType.PARENT, RoleType.ROLE_PARENT);
        MapMessage message = userServiceClient.registerUser(neonatalUser);
        if (!message.isSuccess()) {
            return message;
        }
        User newUser = (User) message.get("user");
        //绑定手机号
        MapMessage activeResult = userServiceClient.activateUserMobile(newUser.getId(), mobile, true);
        if (!activeResult.isSuccess()) {
            return activeResult;
        }

        ParentExtAttribute parentExtAttribute = new ParentExtAttribute(newUser.getId());
        parentExtAttribute.setBrandFlag(true);//默认c端家长同意:家长通产品商业区隔协议书
        MapMessage extResult = parentServiceClient.generateParentExtAttribute(parentExtAttribute);
        if (!extResult.isSuccess()) {
            logger.error("插入c端家长附加信息失败");
            return extResult;
        }
        return message;
    }

    private MapMessage registerStudentAndBind(User parent, String phoneNo) {
        // FIXME
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(phoneNo, UserType.STUDENT);
        if (userAuthentication != null) {
            return MapMessage.errorMessage("用户已经存在");
        }
        //初始化要注册的用户
        NeonatalUser neonatalUser = initChannelCUser(UserType.STUDENT, RoleType.ROLE_STUDENT);
        MapMessage message = userServiceClient.registerUser(neonatalUser);
        if (!message.isSuccess()) {
            return message;
        }
        User newUser = (User) message.get("user");
        //绑定手机号
        MapMessage activeResult = userServiceClient.activateUserMobile(newUser.getId(), phoneNo, false);
        if (!activeResult.isSuccess()) {
            return activeResult;
        }
        //绑定创建的家长和学生
        MapMessage mapMessage = parentServiceClient.bindExistingParent(newUser.getId(), parent.getId(), true, CallName.妈妈.name());
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        return message;
    }

    //初始化用户
    private NeonatalUser initChannelCUser(UserType userType, RoleType roleType) {
        //用户名和昵称为空字符串,密码为随机数,
        //下列数据为空时是否违背底层数据库设计
        // Save User Info
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(roleType);
        neonatalUser.setUserType(userType);
        neonatalUser.setRealname("");
        neonatalUser.setNickName("");
        neonatalUser.setPassword(RandomGenerator.generatePlainPassword());
        neonatalUser.setInviter(null);
        neonatalUser.setWebSource(UserWebSource.miniProgramPicListen.name());
        return neonatalUser;
    }
}
