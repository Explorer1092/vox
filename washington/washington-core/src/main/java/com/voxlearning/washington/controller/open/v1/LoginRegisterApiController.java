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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.*;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.helpers.ValidateStudentIdHelper;
import com.voxlearning.washington.support.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * User register related API controller class.
 *
 * @author Zhilong Hu
 * @since 2014-06-6
 */
@Controller
@RequestMapping(value = "/v1/user")
@Slf4j
public class LoginRegisterApiController extends AbstractApiController {

    @Inject private EmailServiceClient emailServiceClient;

    @Inject private CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject private ValidateStudentIdHelper validateStudentIdHelper;
    @Inject private AsyncFootprintServiceClient asyncFootprintServiceClient;

    @RequestMapping(value = "/login.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage userLogin() {
        return internalUserLogin();
    }

    @RequestMapping(value = "/register.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage registerUser() {
        MapMessage resultMap = new MapMessage();
        Long clazzId = getRequestLong(REQ_CLAZZ_ID);
        String userGender = getRequestParameter(REQ_USER_GENDER, null);
        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequired(REQ_USER_CODE, "手机号码或者邮箱");
            validateRequired(REQ_PASSWD, "密码");
            // validateRequiredAny(REQ_NICK_NAME, REQ_REAL_NAME, "真实姓名或者昵称");
            validateNumber(REQ_INVITOR_ID, "邀请人ID");

            //这种形式是为了兼容老版本不传某些值的情况下也能校验通过
            List<String> reqParams = new ArrayList<>();
            reqParams.add(REQ_USER_TYPE);
            reqParams.add(REQ_USER_CODE);
            reqParams.add(REQ_NICK_NAME);
            reqParams.add(REQ_REAL_NAME);
            reqParams.add(REQ_VERIFY_CODE);
            reqParams.add(REQ_PASSWD);
            reqParams.add(REQ_AVATAR_DAT);
            reqParams.add(REQ_INVITOR_ID);
            if (clazzId > 0) {
                reqParams.add(REQ_CLAZZ_ID);
            } else {
                if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.7.0.0") >= 0) {
                    log.error("No clazz id found when user register, user code " + getRequestString(REQ_USER_CODE));
                }
            }
            if (userGender != null) {
                reqParams.add(REQ_USER_GENDER);
            }

            validateRequestNoSessionKey(reqParams.toArray(new String[reqParams.size()]));
            validateUserRegisterInfo();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        try {
            PasswordRule.validatePassword(getRequestString(REQ_PASSWD));
        } catch (Exception ex) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_PASSWORD_INVALID);
            return resultMap;
        }

        String userCode = getRequestString(REQ_USER_CODE);
        String realName = StringUtils.filterEmojiForMysql(getRequestString(REQ_REAL_NAME));
        RoleType roleType = RoleType.of(getRequestInt(REQ_USER_TYPE));
        String password = getRequestString(REQ_PASSWD);

        if (StringRegexUtils.isNotRealName(realName)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_REALNAME_INVALID);
            return resultMap;
        }
        // Enhancement #48844 学生APP注册检查学生姓名
        if (clazzId > 0) {
            List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazzId);
            if (CollectionUtils.isNotEmpty(studentIds)) {
                List<User> sameNameList = userLoaderClient.loadUsers(studentIds)
                        .values()
                        .stream()
                        .filter(s -> StringUtils.equals(s.fetchRealname(), realName.trim()))
                        .sorted((o1, o2) -> Long.compare(o2.getCreateTime().getTime(), o1.getCreateTime().getTime()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sameNameList)) {
                    User user = userLoginServiceClient.getUserLoginService().findSameNameNeverLoginUser(sameNameList).getUninterruptibly();

                    if (user != null) {
                        MapMessage message = userServiceClient.setPassword(user, password);
                        if (!message.isSuccess()) {
                            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
                            return resultMap;
                        } else {
                            // 绑定手机号
                            userServiceClient.activateUserMobile(user.getId(), userCode, true);
                            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
                            addMoreInfo(resultMap, user.getId(), ua.getPassword(), roleType);
                            return resultMap;
                        }
                    } else {
                        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                        resultMap.add(RES_MESSAGE, RES_RESULT_ACCOUNT_EXISTS_MSG);
                        return resultMap;
                    }
                }
            }
        }

        // Save User Info
        NeonatalUser neonatalUser = new NeonatalUser();
        if (StringUtils.isNotBlank(userGender)) {
            Gender gender = Gender.fromCode(userGender);
            neonatalUser.setGender(gender.getCode());
        }
        neonatalUser.setRoleType(roleType);
        neonatalUser.setUserType(UserType.of(getRequestInt(REQ_USER_TYPE)));
        if (userCode.contains("@")) {
            neonatalUser.setEmail(userCode);
        }
//        } else {
////            neonatalUser.setMobile(userCode);
//        }
        neonatalUser.setRealname(realName);
        neonatalUser.setNickName(StringUtils.filterEmojiForMysql(getRequestString(REQ_NICK_NAME)));
        neonatalUser.setPassword(password);
        neonatalUser.setInviter(getRequestString(REQ_INVITOR_ID));
        neonatalUser.setWebSource(getRequestString(REQ_APP_KEY));
        //fixme 注意 这个地方如果传了验证码进去， 底层会做二次校验 上面已经做了校验了， 所以这里就没必要传了。 xiaopeng.yang 2015-09-17
//        neonatalUser.setCode(getRequestString(REQ_VERIFY_CODE));

        MapMessage message;
        if (neonatalUser.getUserType() == UserType.STUDENT) {
            message = userServiceClient.registerUser(neonatalUser);
        } else {
            message = userServiceClient.registerUserAndSendMessage(neonatalUser);
        }

        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, StringUtils.isBlank(message.getInfo()) ? message.getAttributes().get("none") : message.getInfo());
            return resultMap;
        }

        // 更新用户头像
        User newUser = (User) message.get("user");
        String avatarDat = getRequestString(REQ_AVATAR_DAT);
        if (!StringUtils.isEmpty(avatarDat)) {
            MapMessage changeResult = updateUserAvatar(newUser, avatarDat);
            if (!changeResult.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UPDATE_AVATAR_ERROR_MSG + changeResult.getInfo());
                return resultMap;
            }
        }

        //注册的时候同步shippingaddress 电话号码信息
        if (!userCode.contains("@")) {
            MapMessage activeResult = userServiceClient.activateUserMobile(newUser.getId(), userCode, true);
            if (!activeResult.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_BIND_MOBILE_ERROR_MSG);
                return resultMap;
            }
            userServiceClient.generateUserShippingAddress(newUser.getId(), userCode);

            // 从缓存中删除验证码
            SmsType smsType = getRegisterVerifyCodeSmsType(UserType.of(getRequestInt(REQ_USER_TYPE)));
            smsServiceClient.getSmsService()
                    .deleteMobileValidationStatus(smsType.name(), userCode)
                    .awaitUninterruptibly();
        } else {
            // 邮箱注册发送激活邮件
            sendActiveEmail(userCode, getRequestString(REQ_REAL_NAME), getRequestString(REQ_NICK_NAME),
                    getRequestString(REQ_PASSWD), UserType.of(getRequestInt(REQ_USER_TYPE)));
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(newUser.getId());
        addMoreInfo(resultMap, newUser.getId(), ua.getPassword(), roleType);

        return resultMap;
    }

    private void addMoreInfo(MapMessage resultMap, Long userId, String password, RoleType roleType) {
        String sessionKey = attachUser2RequestApp(userId);

//        userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddress(), OperationSourceType.app);
//        userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.app);

        getWebRequestContext().saveAuthenticationStates(-1, userId, password, roleType);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_ID, userId);
        resultMap.add(RES_SESSION_KEY, sessionKey);
        //存储一下学生ID用户之后可能绑定家长身份校验用
        if (roleType == RoleType.ROLE_STUDENT) {
            String uuid = getRequestString(REQ_UUID);
            validateStudentIdHelper.storeBindStudentIdWithUUID(uuid, userId);
        }
    }

    private void validateUserRegisterInfo() {
        // 判断是否已经注册过
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userCode = getRequestString(REQ_USER_CODE);

        if (userLoaderClient.loadMobileAuthentication(userCode, userType) != null) {
            throw new IllegalArgumentException("该手机号码已注册，请直接登录!");
        }

        List<User> registeredUserList = userLoaderClient.loadUsers(userCode, userType);
        if (registeredUserList != null && registeredUserList.size() > 0) {
            throw new IllegalArgumentException("该手机号码或邮箱已注册，请直接登录!");
        }

        // 如果是手机注册，则验证码必填并且要和缓存中的验证码一致
        if (!userCode.contains("@")) {
            validateRegisterVerifyCode();
        } else if (!EmailRule.isEmail(userCode)) {
            throw new IllegalArgumentException("无效的手机号码或邮箱!");
        }
    }

    private void validateRegisterVerifyCode() {
        validateRequired(REQ_VERIFY_CODE, "手机注册时验证码");

        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userCode = getRequestString(REQ_USER_CODE);
        String verifyCode = getRequestString(REQ_VERIFY_CODE);

        // 从缓存中获取验证码
        SmsType smsType = getRegisterVerifyCodeSmsType(userType);

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(userCode, verifyCode, smsType.name(), false);
        if (!validateResult.isSuccess()) {
            throw new IllegalArgumentException("验证码无效或者已过期!");
        }
    }

    private void sendActiveEmail(String email, String realName, String nickName, String password, UserType userType) {
        try {
            // 生成邀请地址
            String userName = realName;
            if (StringUtils.isEmpty(userName)) {
                userName = nickName;
            }

            Map<String, Object> registerInfo = new LinkedHashMap<>();
            registerInfo.put("email", email);
            registerInfo.put("realname", userName);
            registerInfo.put("password", password);
            registerInfo.put("roleType", RoleType.of(userType.getType()));
            registerInfo.put("userType", userType);
            registerInfo.put("expiredTimestamp", System.currentTimeMillis() + 86400000L);

            String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
            if (defaultDesKey == null) {
                throw new ConfigurationException("No 'default_des_key' configured");
            }
            String link = ProductConfig.getMainSiteBaseUrl() + "/signup/tesignactivation/"
                    + DesUtils.encryptHexString(defaultDesKey, JsonUtils.toJson(registerInfo)) + ".vpage";

            // 发送邮件
            String subject = "请激活你的一起作业账号，完成注册";
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("url", link);
            content.put("name", userName);
            content.put("date", DateUtils.dateToString(new Date(), "yyyy年MM月dd日 hh:mm"));

            emailServiceClient.createTemplateEmail(EmailTemplate.emailregister)
                    .to(email)
                    .subject(subject)
                    .content(content)
                    .send();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/password/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateUserPassword() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequiredNumber(REQ_USER_TYPE, "用户身份");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequired(REQ_PASSWD, "新密码");
            if (StringUtils.isEmpty(getRequestString(REQ_SESSION_KEY)))
                validateRequestNoSessionKey(REQ_USER_CODE, REQ_USER_TYPE, REQ_VERIFY_CODE, REQ_PASSWD);
            else
                validateRequest(REQ_USER_CODE, REQ_USER_TYPE, REQ_VERIFY_CODE, REQ_PASSWD);
            validateForgotPasswordVerifyCode();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            logger.error("Error happened while update user password", e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        try {
            PasswordRule.validatePassword(getRequestString(REQ_PASSWD));
        } catch (Exception ex) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_PASSWORD_INVALID);
            return resultMap;
        }

        // 获取用户信息
        String userAccount = getRequestString(REQ_USER_CODE);
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        List<User> userList = userLoaderClient.loadUsers(userAccount, userType);
        if (userList == null || userList.size() == 0) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNKNOWN_USER_ACCOUNT);
            return resultMap;
        } else if (userList.size() > 1) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        if (userServiceClient.setPassword(userList.get(0), getRequestString(REQ_PASSWD)).isSuccess()) {
            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(userList.get(0).getId());
            userServiceRecord.setOperatorId(userList.get(0).getId().toString());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("修改密码");
            userServiceRecord.setComments("用户app修改密码");
            userServiceRecord.setAdditions("refer:LoginRegisterApiController.updateUserPassword");
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            // 更新sessionkey
            User user = userList.get(0);
            if (user.getUserType().equals(UserType.TEACHER.getType())) {
                String sessionKey = updateAppSessionKey(getRequestString(REQ_APP_KEY), user.getId());
                // FIXME 临时方案
                if ("17JuniorTea".equals(getRequestString(REQ_APP_KEY))) {
                    updateAppSessionKey("Shensz", user.getId());
                }
                getWebRequestContext().saveAuthenticationStates(-1, user.getId(), getRequestString(REQ_PASSWD), RoleType.ROLE_TEACHER);
                resultMap.add(RES_SESSION_KEY, sessionKey);
            } else if (user.getUserType().equals(UserType.STUDENT.getType())) {
                String sessionKey = updateAppSessionKey(getRequestString(REQ_APP_KEY), user.getId());
                // FIXME 临时方案
                if ("17JuniorStu".equals(getRequestString(REQ_APP_KEY))) {
                    updateAppSessionKey("Shensz", user.getId());
                }
                getWebRequestContext().saveAuthenticationStates(-1, user.getId(), getRequestString(REQ_PASSWD), RoleType.ROLE_STUDENT);
                resultMap.add(RES_SESSION_KEY, sessionKey);
            }
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 修改密码后更新App的session key
     *
     * @param appKey userId
     *
     * @author changyuan.liu
     */
    private String updateAppSessionKey(String appKey, Long userId) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef(appKey, userId);
        String newSessionKey = "";
        if (vendorAppsUserRef != null) {
            newSessionKey = SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), userId);
            vendorServiceClient.expireSessionKey(appKey, userId, newSessionKey);
        }
        return newSessionKey;
    }

    private void validateForgotPasswordVerifyCode() {
        String userCode = getRequestString(REQ_USER_CODE);
        String verifyCode = getRequestString(REQ_VERIFY_CODE);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);

        if (!MobileRule.isMobile(userCode)) {
            String sendForgotPasswordVerifyCode = sensitiveUserDataServiceClient.loadUserMobile(Long.valueOf(userCode));
            if (sendForgotPasswordVerifyCode == null) {
                throw new IllegalArgumentException("用户没有绑定手机号!");
            }
            userCode = sendForgotPasswordVerifyCode;
        }
        // 区分发送验证码的类型，家长的单独使用，因为家长APP做了蛋疼的位数限制。。。
        SmsType smsType = SmsType.FORGOT_PASSWORD_SEND_VERIFY_CODE;
        MapMessage message = smsServiceClient.getSmsService().verifyValidateCode(userCode, verifyCode, smsType.name());
        if (!message.isSuccess()) {
            throw new IllegalArgumentException("验证码无效或者已过期!");
        }
    }

    private static SmsType getRegisterVerifyCodeSmsType(UserType userType) {
        if (userType == UserType.TEACHER) {
            return SmsType.APP_TEACHER_VERIFY_MOBILE_REGISTER_MOBILE;
        } else if (userType == UserType.PARENT) {
            return SmsType.APP_PARENT_VERIFY_MOBILE_REGISTER_MOBILE;
        } else {
            return SmsType.APP_STUDENT_VERIFY_MOBILE_REGISTER_MOBILE;
        }
    }

}
