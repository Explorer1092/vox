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

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.lang.util.StringHelper;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * User verify related API controller class.
 *
 * @author Zhilong Hu
 * @since 2014-06-6
 */
@Controller
@RequestMapping(value = "/v1/user")
@Slf4j
public class VerificationApiController extends AbstractApiController {

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    @RequestMapping(value = "/register/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendRegisterVerifyCode() {
        MapMessage resultMap = new MapMessage();

        // FIXME temp log verification code request
        String pickUpLog = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "pick_up_log");
        if (StringUtils.isNoneBlank(pickUpLog) && "1".equals(pickUpLog)) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", getRequestString(REQ_USER_CODE),
                    "appKey", getRequestString(REQ_APP_KEY),
                    "clientIp", getWebRequestContext().getRealRemoteAddress(),
                    "User-Agent", getRequest().getHeader("User-Agent"),
                    "op", "sendRegisterVerifyCodeV1Before"
            ));
        }

        // 反扒处理
        String userAgent = getRequest().getHeader("User-Agent");
        if (StringUtils.isNoneBlank(userAgent) && userAgent.contains("Java")) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            return resultMap;
        }

        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            if (getRequest().getParameter(REQ_TEACHER_ID) != null) {
                validateRequestNoSessionKey(REQ_USER_TYPE, REQ_USER_CODE, REQ_IMEI, RES_TEACHER_ID);
            } else {
                validateRequestNoSessionKey(REQ_USER_TYPE, REQ_USER_CODE, REQ_IMEI);
            }
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        // 根据UserType发送验证码
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userMobile = getRequestString(REQ_USER_CODE);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        UserAuthentication userAuth = userLoaderClient.loadMobileAuthentication(userMobile, userType);
        if (userAuth != null) {

            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "该手机号码已经注册，请直接登录");

            // FIXME 中学学生，小升初，重新设个code
            if (UserType.STUDENT == userAuth.getUserType() && "17JuniorStu".equals(getRequestString(REQ_APP_KEY))) {
                StudentDetail detail = studentLoaderClient.loadStudentDetail(userAuth.getId());
                if (detail != null && ((detail.getClazz() == null) || (detail.isPrimaryStudent() && detail.getClazz().isTerminalClazz()))) {
                    resultMap.set(RES_RESULT, RES_RESULT_REDIRECT_LOGIN);
                }
            }

            return resultMap;
        }

        // FIXME 20150902 临时关闭一个手机号码注册多个用户身份的功能
        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(userMobile, UserType.TEACHER);
        if (ua != null) {
            // 只对小学起作用
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(ua.getId());
            if (teacher != null && teacher.isPrimarySchool()) {
                // 1.teacherId == 0 : 老版本未传teacherId。如果手机号绑定了老师，直接给出提示
                // 2.teacherId > 0 : 新版本传递了teacherId，如果手机号绑定的老师id就是teacherId，也给出提示
                // 2的条件比1要松。为了解决老师的孩子无法加入的问题
                if (teacherId == 0 || (teacherId > 0 && Objects.equals(ua.getId(), teacherId))) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, "该号码已用于注册老师号，请更换号码");
                    return resultMap;
                }
            }
        }


        MapMessage sendVerifyCodeResult;
        if (userType == UserType.STUDENT) {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_STUDENT_VERIFY_MOBILE_REGISTER_MOBILE,
                    UserType.STUDENT,
                    false);
        } else if (userType == UserType.TEACHER) {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_TEACHER_VERIFY_MOBILE_REGISTER_MOBILE,
                    UserType.TEACHER,
                    false);
        } else {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_PARENT_VERIFY_MOBILE_REGISTER_MOBILE,
                    UserType.PARENT,
                    false);
        }

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 注册时获取语音验证码
     *
     * @return
     */
    @RequestMapping(value = "/register/verifyvoicecode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendRegisterVerifyVoideCode() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequestNoSessionKey(REQ_USER_TYPE, REQ_USER_CODE, REQ_IMEI);
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        // 根据UserType发送验证码
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userMobile = getRequestString(REQ_USER_CODE);

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        if (userLoaderClient.loadMobileAuthentication(userMobile, userType) != null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "该手机号码已经注册，请直接登录");
            return resultMap;
        }

        MapMessage sendVerifyCodeResult;
        if (userType == UserType.STUDENT) {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_STUDENT_VERIFY_MOBILE_REGISTER_MOBILE,
                    UserType.STUDENT,
                    true);
        } else if (userType == UserType.TEACHER) {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_TEACHER_VERIFY_MOBILE_REGISTER_MOBILE,
                    UserType.TEACHER,
                    true);
        } else {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_PARENT_VERIFY_MOBILE_REGISTER_MOBILE,
                    UserType.PARENT,
                    true);
        }

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 家长APP绑定手机发送验证码
     */
    @RequestMapping(value = "/moblie/parent/bindMoblie.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bindMobile() {
        MapMessage resultMap = new MapMessage();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequestNoSessionKey(REQ_USER_CODE, REQ_IMEI);
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        // 根据UserType发送验证码
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        if (userType == UserType.TEACHER) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_PARENT_LOGIN_USERTYPE_ERROR_MSG);
            return resultMap;
        }

        String userMobile = getRequestString(REQ_USER_CODE);

        if (CollectionUtils.isNotEmpty(userLoaderClient.loadMobileAuthentications(userMobile))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_IS_BIND_ERROR_MSG);
            return resultMap;
        }

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        SmsType smsType = SmsType.PARENT_VERIFY_MOBILE_LOGIN;
        MapMessage sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                userMobile,
                smsType.name(),
                false);

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/delete/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendDeleteUserVerifyCode() {
        MapMessage resultMap = new MapMessage();
        User user = getApiRequestUser();
        if (user == null) {
            resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_RELOGIN);
            return resultMap;
        }
        String userMobile = sensitiveUserDataServiceClient.showUserMobile(user.getId(), "open api", SafeConverter.toString(user.getId()));
        if (StringUtils.isBlank(userMobile)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_PARENT_MOBLIE_STUDENT_NOT_BIND_MOBLIE_MSG);
            return resultMap;
        }
        try {
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo(userMobile);
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            } else {
                return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
            }
        }

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        SmsType smsType = SmsType.NO_CATEGORY;
        if (user.isStudent()) {
            smsType = SmsType.APP_STUDENT_VERIFY_MOBILE_DELETE_ACCOUNT;
        } else if (user.isParent()) {
            smsType = SmsType.APP_PARENT_VERIFY_MOBILE_DELETE_ACCOUNT;
        } else if (user.isTeacher()) {
            smsType = SmsType.APP_TEACHER_VERIFY_MOBILE_DELETE_ACCOUNT;
        }

        if (smsType == SmsType.NO_CATEGORY) {
            resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_RELOGIN);
            return resultMap;
        }
//        MapMessage sendResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
//                userMobile,
//                smsType.name(),
//                false);
        MapMessage sendResult = MapMessage.successMessage();
        // generate response message
        if (!sendResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo(userMobile);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/moblie/parent/identity/bindMoblie.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendParentBindIdentityVerifyCode() {
        MapMessage resultMap = new MapMessage();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_IMEI, "IMEI");
            if (!hasSessionKey()) {
                validateRequestNoSessionKey(REQ_STUDENT_ID, REQ_PARENT_ID, REQ_CONTACT_MOBILE, REQ_IMEI);
            } else {
                validateRequest(REQ_STUDENT_ID, REQ_PARENT_ID, REQ_CONTACT_MOBILE, REQ_IMEI);
            }

        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long parentId = getRequestLong(REQ_PARENT_ID);
        String userMobile = getRequestString(REQ_CONTACT_MOBILE);
        Long wantToBindMobileUserId = 0L;

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        if (StringUtils.isBlank(userMobile) || userMobile.contains("*")) {
            if (parentId > 0) {
                List<StudentParentRef> refList = parentLoaderClient.loadParentStudentRefs(parentId);
                if (CollectionUtils.isEmpty(refList)) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, "当前家长未关联任何学生");
                    return resultMap;
                }
                boolean parentStudentMatch = refList.stream().anyMatch(e -> Objects.equals(e.getStudentId(), studentId));
                if (!parentStudentMatch) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, "当前家长未关联学生" + studentId);
                    return resultMap;
                }
                wantToBindMobileUserId = parentId;
            } else if (studentId > 0) {
                wantToBindMobileUserId = studentId;
            }
            //根据userId取手机号
            String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(wantToBindMobileUserId);
            if (StringUtils.isBlank(authenticatedMobile)) {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, "获取手机号码失败");
                return resultMap;
            }
            userMobile = authenticatedMobile;
        }
        try {
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo(userMobile);
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            } else {
                return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
            }
        }
        SmsType smsType = SmsType.APP_PARENT_BIND_IDENTITY_VERIFY_MOBILE_LOGIN_MOBILE;
        MapMessage sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                userMobile,
                smsType.name(),
                false);

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo(userMobile);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/forgotpassword/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendForgotPasswordVerifyCode() {
        MapMessage resultMap = new MapMessage();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            if (StringUtils.isEmpty(getRequestString(RES_SESSION_KEY)))
                validateRequestNoSessionKey(REQ_USER_TYPE, REQ_USER_CODE, REQ_IMEI);
            else
                validateRequest(REQ_USER_TYPE, REQ_USER_CODE, REQ_IMEI);
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userCode = getRequestString(REQ_USER_CODE);
        VendorApps vendorApps = getApiRequestApp();

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userCode, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        try {
            List<User> userList = userLoaderClient.loadUsers(userCode, null);
            if (CollectionUtils.isEmpty(userList)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                if (userType.equals(UserType.TEACHER)) {
                    resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_NOT_REGIST_ERROR_MSG);
                } else
                    resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_NOT_EXIST_ERROR_MSG);
                return resultMap;
            } else {
                //为的是当在老师端输入学生手机号时,判断用户合法性.
                //此处当发验证码为老师时,不影响原来逻辑.如果可以确认上面  List<User> userList = userLoaderClient.loadUsers(userCode, null) 这行代码可以传userType进来,就无需这样处理
                if (userType.equals(UserType.TEACHER)) {
                    Boolean isTeacher = false;
                    for (User user : userList) {
                        if (user.getUserType().equals(userType.getType())) {
                            isTeacher = true;
                            break;
                        }
                    }
                    if (!isTeacher) {
                        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                        resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_NOT_REGIST_ERROR_MSG);
                        return resultMap;
                    }
                }

            }

            // FIXME 临时措施，开学学生拿到老师手机号码注册很多，造成验证码误发，这里检查一下手机号码是否同时绑定了老师号，
            // FIXME 如果绑定了，不允许发送，防止骚扰老师

            // FIXME 2015-12-11 commit
            // FIXME 暂时注掉，否则某些注册用户（比如，老师的孩子）无法发送验证码并找回密码，这个接口也不是老师被骚扰的来源
//            if (userType == UserType.STUDENT && userLoaderClient.loadMobileAuthenticatedUser(userCode, UserType.TEACHER) != null) {
//                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//                resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_ERROR_MSG);
//                return resultMap;
//            }
            if (!MobileRule.isMobile(userCode)) {
                String sendForgotPasswordVerifyCode = sensitiveUserDataServiceClient.showUserMobile(SafeConverter.toLong(userCode), "sendForgotPasswordVerifyCode", userCode);
                if (sendForgotPasswordVerifyCode == null) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
                    return resultMap;
                }
                userCode = sendForgotPasswordVerifyCode;
            }
            // 区分发送验证码的类型，家长的单独使用，因为家长APP做了蛋疼的位数限制。。。
            SmsType smsType = SmsType.FORGOT_PASSWORD_SEND_VERIFY_CODE;
            MapMessage sendResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(userCode, smsType.name(), false);
            if (!sendResult.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, sendResult.getInfo());
                return resultMap;
            }
            updateMemCacheInfo();
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        } catch (Exception e) {
            logger.error("Error happened while send verify code for forgot password!", e);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }
    }


    @RequestMapping(value = "/bindmobile/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendMobileCode() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequest(REQ_USER_TYPE, REQ_USER_CODE, REQ_IMEI);
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        User curUser = getApiRequestUser();
        // 根据UserType发送验证码
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userMobile = getRequestString(REQ_USER_CODE);

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        MapMessage sendVerifyCodeResult;
        if (userType == UserType.STUDENT) {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    curUser.getId(),
                    userMobile,
                    SmsType.APP_STUDENT_VERIFY_MOBILE_CENTER);
        } else if (userType == UserType.TEACHER) {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    curUser.getId(),
                    userMobile,
                    SmsType.APP_TEACHER_VERIFY_MOBILE_CENTER);
        } else {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    curUser.getId(),
                    userMobile,
                    SmsType.APP_PARENT_VERIFY_MOBILE_CENTER);
        }

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/login/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendMobileCodeForLogin() {
        MapMessage resultMap = new MapMessage();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);

        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequestNoSessionKey(REQ_USER_TYPE, REQ_USER_CODE, REQ_IMEI);
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        // 根据UserType发送验证码
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userMobile = getRequestString(REQ_USER_CODE);

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        MapMessage sendVerifyCodeResult;
        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(userMobile, userType);
        if (userType == UserType.STUDENT) {
            //发送验证码之前先判断用户帐号状态，如果是封禁，则返回，否则发送验证码

            if (ua != null) {
                StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(ua.getId());
                if (studentExtAttribute != null && studentExtAttribute.isForbidden()) {
                    resultMap.add(RES_USER_ACCOUNT_STATUS, studentExtAttribute.getAccountStatus().name());
                    resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                    return resultMap;
                }
            }

            sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_STUDENT_VERIFY_MOBILE_LOGIN_MOBILE.name(),
                    false);
        } else if (userType == UserType.TEACHER) {
            if (ua == null)
                return failMessage(RES_RESULT_MOBILE_NOT_REGIST_ERROR_MSG);
            sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_TEACHER_VERIFY_MOBILE_LOGIN_MOBILE.name(),
                    false);
        } else {
            SmsType smsType = SmsType.APP_PARENT_VERIFY_MOBILE_LOGIN_MOBILE;
            sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    userMobile,
                    smsType.name(),
                    false);
        }

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    @RequestMapping(value = "/changemobile/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changeMobileSendCode() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequest(REQ_USER_TYPE, REQ_USER_CODE, REQ_IMEI);
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        // 根据UserType发送验证码
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userMobile = getRequestString(REQ_USER_CODE);

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        MapMessage sendVerifyCodeResult;
        if (userType == UserType.STUDENT) {
            sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.STUDENT_VERIFY_MOBILE_CENTER.name(),
                    false);
        } else if (userType == UserType.TEACHER) {
            sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.TEACHER_VERIFY_MOBILE_CENTER.name(),
                    false);
        } else {
            sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.PARENT_VERIFY_MOBILE_CENTER.name(),
                    false);
        }

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;

    }

    @RequestMapping(value = "/changeclazz/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendMobileCodeForChangeClazz() {
        MapMessage resultMap = new MapMessage();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_IMEI, "IMEI");
            if (VersionUtil.compareVersion(ver, "2.0.0.0") < 0) {
                validateRequest(REQ_USER_CODE, REQ_IMEI);
            } else {
                validateRequest(REQ_IMEI);
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        User user = getApiRequestUser();
        // 原逻辑：
        // 查询绑定的家长手机号：如果有关键家长并且有手机号，直接发验证码；如果没有关键家长，用第一个有手机号的家长手机号发送验证码；如果都没有手机号，则查询当前学生的手机号，不为空，发送验证码。
        // 新逻辑：
        // 查询当前学生的手机号，不为空，发送验证码；否则查询绑定的家长手机号：如果有关键家长并且有手机号，直接发验证码；如果没有关键家长，用第一个有手机号的家长手机号发送验证码；redmine 31437
        String userMobile = null;
        String mobile = sensitiveUserDataServiceClient.showUserMobile(user.getId(), "open api", SafeConverter.toString(user.getId()));
        if (mobile != null) {
            userMobile = mobile;
        }
        if (StringUtils.isBlank(userMobile)) {
            userMobile = getParentMobileByStudentId(user.getId());
        }

        if (StringUtils.isBlank(userMobile)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_PARENT_MOBLIE_STUDENT_NOT_BIND_MOBLIE_MSG);
            return resultMap;
        }
        try {
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo(userMobile);
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            } else {
                return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
            }
        }

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        MapMessage sendResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                userMobile,
                SmsType.APP_STUDENT_CHANGE_CLAZZ_VERIFY_MOBILE.name(),
                false);

        // generate response message
        if (!sendResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo(userMobile);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_MOBILE, StringHelper.mobileObscure(userMobile));
        return resultMap;
    }

    @RequestMapping(value = "freezestudent/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendCodeForFreezeStudent() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_ID, "用户id");
            validateRequired(REQ_IMEI, "IMEI");
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        String userMobile = getParentMobileByStudentId(getRequestLong(REQ_USER_ID));
        if (StringUtils.isBlank(userMobile)) {
            String mobile = sensitiveUserDataServiceClient.loadUserMobile(getRequestLong(REQ_USER_ID));
            if (StringUtils.isNoneBlank(mobile)) {
                userMobile = mobile;
            }
        }

        if (StringUtils.isBlank(userMobile)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_PARENT_MOBLIE_STUDENT_NOT_BIND_MOBLIE_MSG);
            return resultMap;
        }

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        MapMessage sendResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                userMobile,
                SmsType.APP_STUDENT_VERIFY_ACCOUNT_FREEZE_MOBILE.name(),
                false);
        if (!sendResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendResult.getInfo());
            return resultMap;
        }

        updateMemCacheInfo(userMobile);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_MOBILE, StringHelper.mobileObscure(userMobile));
        return resultMap;
    }

    @RequestMapping(value = "/mobile/binded.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage mobileBinded() {

        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequestNoSessionKey(REQ_USER_TYPE, REQ_USER_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userMobile = getRequestString(REQ_USER_CODE);

        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(userMobile, userType);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_MOBILE_BINDING, userAuthentication != null && userAuthentication.isMobileAuthenticated());
        return resultMap;
    }


    // FIXME 这个方法跟上边那个很类似。
    // 但是上边方法的判定加入了以下逻辑：
    // 当需要验证学生手机是否绑定时，同时验证此手机号码是否也被老师绑定了，如果也绑定了，同样算作手机号已绑定
    // 这个方法完全是按照roletype和mobile来判定的，不掺杂任何其他逻辑
    @RequestMapping(value = "/mobile/bindedbyrole.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage mobileBindedByRole() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequestNoSessionKey(REQ_USER_TYPE, REQ_USER_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userMobile = getRequestString(REQ_USER_CODE);

        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(userMobile, userType);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_MOBILE_BINDING, userAuthentication != null && userAuthentication.isMobileAuthenticated());
        return resultMap;
    }

    @RequestMapping(value = "/register/channel_c_student/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendRegisterChannelCStudentVerifyCode() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequest(REQ_USER_TYPE, REQ_USER_CODE, REQ_IMEI);
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        // 根据UserType发送验证码
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        String userMobile = getRequestString(REQ_USER_CODE);

        if (userLoaderClient.loadMobileAuthentication(userMobile, userType) != null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "该手机号码已经注册，请直接登录");
            return resultMap;
        }

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        MapMessage sendVerifyCodeResult;
        if (userType == UserType.STUDENT) {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_STUDENT_VERIFY_MOBILE_REGISTER_MOBILE,
                    UserType.STUDENT,
                    false);
        } else if (userType == UserType.TEACHER) {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_TEACHER_VERIFY_MOBILE_REGISTER_MOBILE,
                    UserType.TEACHER,
                    false);
        } else {
            sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    userMobile,
                    SmsType.APP_PARENT_VERIFY_MOBILE_REGISTER_MOBILE,
                    UserType.PARENT,
                    false);
        }

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    /**
     * 更换手机号之前验证原有手机号的验证码发送
     *
     * @return
     */
    @RequestMapping(value = "/changeMobile/original/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changeMobileOriginalMobileCodeGet() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "1", "2", "3");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequest(REQ_USER_TYPE, REQ_IMEI);

        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        User apiRequestUser = getApiRequestUser();
        if (apiRequestUser == null)
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        // 根据UserType发送验证码
        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
        if (userType != UserType.PARENT) // 目前只支持家长,没加其他的SmsType
            return failMessage(RES_RESULT_USER_TYPE_ERROR_MSG);
        String userMobile = sensitiveUserDataServiceClient.loadUserMobile(apiRequestUser.getId());
        if (StringUtils.isBlank(userMobile))
            return failMessage("该用户没有绑定手机");


        if (!RuntimeMode.isTest()) {
            try {
                validateVerifyCodeInfo(userMobile);
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    return failMessage(e);
                } else {
                    return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
                }
            }
        }

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        MapMessage sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                userMobile,
                SmsType.APP_PARENT_VERIFY_MOBILE_BEFORE_CHANGE_MOBILE.name(),
                false);

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 在家长端给学生绑定手机号验证码发送
     *
     * @return
     */
    @RequestMapping(value = "/parent/student/bindmobile/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage parentBindChildMobileCodeGet() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_USER_CODE, "手机号");
            validateRequired(REQ_IMEI, "IMEI");
            validateRequest(REQ_USER_TYPE, REQ_IMEI);
            if (!RuntimeMode.isTest()) {
                validateVerifyCodeInfo();
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        String userMobile = getRequestString(REQ_USER_CODE);

        // FIXME 审查盗刷的接口
        if (!validateLimitation(getRequestString(REQ_APP_KEY), userMobile, getWebRequestContext().getRealRemoteAddress())) {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        MapMessage sendVerifyCodeResult = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                userMobile,
                SmsType.APP_STUDENT_VERIFY_MOBILE_CENTER,
                UserType.STUDENT,
                false);

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;

    }


    private void validateVerifyCodeInfo() throws Exception {
        String userMobile = getRequestString(REQ_USER_CODE);
        String imei = getRequestString(REQ_IMEI);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String sys = getRequestString(REQ_SYS);

        // 防止骚扰用户，每个IMEI每天至多给3个不同的手机号码发验证
        // FIXME 他大爷的。。iOS1.9.9把IMEI写死了。。导致每天只能发送成功3个机器的验证码
        // FIXME 这个接口还有第三方在调用。。所以ver为空也得校验
        // FIXME 目前需要校验的是：
        // FIXME 1.所有来自第三方的请求（ver为空）
        // FIXME 2.来自客户端的请求，不包括iOS1.9.9.156
        String sentMobiles = washingtonCacheSystem.CBS.flushable.load(getImeiSentMobileListKey(imei));
        if ((StringUtils.isBlank(ver) ||
                (StringUtils.equalsIgnoreCase(sys, "ios") && StringUtils.isNotBlank(ver) && VersionUtil.compareVersion(ver, "1.9.9.156") != 0))
                && !StringUtils.isEmpty(sentMobiles) && !sentMobiles.contains(userMobile) && sentMobiles.split(",").length >= 4) {
            log.warn("用户手机号码(" + userMobile + "," + imei + ")请求接受的验证码超过上限!==单个imei发送的手机号码个数超过限制");
            throw new IllegalArgumentException("请求发送的验证码超过上限!");
        }

        // 防止骚扰用户，每个手机号至多接受3个IMEI的验证码
        String mobileRecvImeis = washingtonCacheSystem.CBS.flushable.load(getMobileRecvImeiListKey(userMobile));
        if (!StringUtils.isEmpty(mobileRecvImeis) && !mobileRecvImeis.contains(imei) && mobileRecvImeis.split(",").length >= 3) {
            log.warn("用户手机号码(" + userMobile + "," + imei + ")请求接受的验证码超过上限!==单个手机号使用的imei个数超过限制");
            throw new IllegalArgumentException("用户手机号码(" + userMobile + ")请求接受的验证码超过上限");
        }

        // 防止骚扰用户，每个IEMI给同一个手机号码至多发5次
        Integer sentCount = washingtonCacheSystem.CBS.flushable.load(getMobileRecvCountKey(imei, userMobile));
        if (sentCount != null && sentCount > 10) {
            log.warn("用户手机号码(" + userMobile + "," + imei + ")发送验证码的次数超过上限!===单个imei给单个手机号发送超过限制");
            throw new IllegalArgumentException("用户手机号码(" + userMobile + ")发送验证码的次数超过上限!");
        }
    }

    //TODO 临时修复
    private void validateVerifyCodeInfo(String userMobile) throws Exception {
        String imei = getRequestString(REQ_IMEI);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String sys = getRequestString(REQ_SYS);
        // 防止骚扰用户，每个IMEI每天至多给3个不同的手机号码发验证
        // FIXME 他大爷的。。iOS1.9.9把IMEI写死了。。导致每天只能发送成功3个机器的验证码
        // FIXME 这个接口还有第三方在调用。。所以ver为空也得校验
        // FIXME 目前需要校验的是：
        // FIXME 1.所有来自第三方的请求（ver为空）
        // FIXME 2.来自客户端的请求，不包括iOS1.9.9.156
        String sentMobiles = washingtonCacheSystem.CBS.flushable.load(getImeiSentMobileListKey(imei));
        if ((StringUtils.isBlank(ver) ||
                (StringUtils.equalsIgnoreCase(sys, "ios") && StringUtils.isNotBlank(ver) && VersionUtil.compareVersion(ver, "1.9.9.156") != 0))
                && !StringUtils.isEmpty(sentMobiles) && !sentMobiles.contains(userMobile) && sentMobiles.split(",").length >= 4) {
            log.warn("用户手机号码(" + userMobile + "," + imei + ")请求接受的验证码超过上限!==单个imei发送的手机号码个数超过限制");
            throw new IllegalArgumentException("请求发送的验证码超过上限!");
        }

        // 防止骚扰用户，每个手机号至多接受3个IMEI的验证码
        String mobileRecvImeis = washingtonCacheSystem.CBS.flushable.load(getMobileRecvImeiListKey(userMobile));
        if (!StringUtils.isEmpty(mobileRecvImeis) && !mobileRecvImeis.contains(imei) && mobileRecvImeis.split(",").length >= 3) {
            log.warn("用户手机号码(" + userMobile + "," + imei + ")请求接受的验证码超过上限!==单个手机号使用的imei个数超过限制");
            throw new IllegalArgumentException("用户手机号码(" + userMobile + ")请求接受的验证码超过上限");
        }

        // 防止骚扰用户，每个IEMI给同一个手机号码至多发10次
        Integer sentCount = washingtonCacheSystem.CBS.flushable.load(getMobileRecvCountKey(imei, userMobile));
        if (sentCount != null && sentCount > 10) {
            log.warn("用户手机号码(" + userMobile + "," + imei + ")发送验证码的次数超过上限!===单个imei给单个手机号发送超过限制");
            throw new IllegalArgumentException("用户手机号码(" + userMobile + ")发送验证码的次数超过上限!");
        }
    }

    private String getImeiSentMobileListKey(String imei) throws Exception {
        return MEMCACHE_KEY_PREFIX_IMEI_SENT_MOBILE + imei;
    }

    private String getMobileRecvImeiListKey(String mobile) throws Exception {
        return MEMCACHE_KEY_PREFIX_MOBILE_RECV_IMEI + mobile;
    }

    private String getMobileRecvCountKey(String imei, String mobile) throws Exception {
        return MEMCACHE_KEY_PREFIX_IMEI_SENT_MOBILE + imei + "_" + mobile;
    }

    private void updateMemCacheInfo() {
        try {
            String userMobile = getRequestString(REQ_USER_CODE);
            String imei = getRequestString(REQ_IMEI);
            Integer liveTime = DateUtils.getCurrentToDayEndSecond(); // 这里从24小时时长有效改成了当天有效

            final String sentMobilesKey = getImeiSentMobileListKey(imei);
            String sentMobiles = washingtonCacheSystem.CBS.flushable.load(sentMobilesKey);
            washingtonCacheSystem.CBS.flushable.set(sentMobilesKey, liveTime, addValue(sentMobiles, userMobile));

            final String mobileRecvImeiKey = getMobileRecvImeiListKey(userMobile);
            String mobileRecvImeis = washingtonCacheSystem.CBS.flushable.load(mobileRecvImeiKey);
            washingtonCacheSystem.CBS.flushable.set(mobileRecvImeiKey, liveTime, addValue(mobileRecvImeis, imei));

            final String sentCountKey = getMobileRecvCountKey(imei, userMobile);
            Integer sentCount = washingtonCacheSystem.CBS.flushable.load(sentCountKey);
            if (sentCount == null) {
                sentCount = 1;
            } else {
                sentCount++;
            }
            washingtonCacheSystem.CBS.flushable.set(sentCountKey, liveTime, sentCount);

        } catch (Exception e) {
            logger.warn("更新缓存时出现错误!", e);
        }
    }


    //TODO 临时修复接口没有user_code参数和user_code参数获取到的userMobile实际是带星号的。无法解决验证次数
    private void updateMemCacheInfo(String userMobile) {
        String imei = getRequestString(REQ_IMEI);
        try {
            Integer liveTime = DateUtils.getCurrentToDayEndSecond(); // 这里从24小时时长有效改成了当天有效

            final String sentMobilesKey = getImeiSentMobileListKey(imei);
            String sentMobiles = washingtonCacheSystem.CBS.flushable.load(sentMobilesKey);
            washingtonCacheSystem.CBS.flushable.set(sentMobilesKey, liveTime, addValue(sentMobiles, userMobile));

            final String mobileRecvImeiKey = getMobileRecvImeiListKey(userMobile);
            String mobileRecvImeis = washingtonCacheSystem.CBS.flushable.load(mobileRecvImeiKey);
            washingtonCacheSystem.CBS.flushable.set(mobileRecvImeiKey, liveTime, addValue(mobileRecvImeis, imei));

            final String sentCountKey = getMobileRecvCountKey(imei, userMobile);
            Integer sentCount = washingtonCacheSystem.CBS.flushable.load(sentCountKey);
            if (sentCount == null) {
                sentCount = 1;
            } else {
                sentCount++;
            }
            washingtonCacheSystem.CBS.flushable.set(sentCountKey, liveTime, sentCount);

        } catch (Exception e) {
            logger.warn("更新缓存时出现错误!", e);
        }
    }


    private static String getForgotPasswordVerifyCodeMemKey(UserType userType, String userCode) {
        return SmsType.FORGOT_PASSWORD_SEND_VERIFY_CODE + "_" + userType.getType() + "_" + userCode;
    }

    @RequestMapping(value = "/parent/login/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendSidCodeForLogin() {
        MapMessage resultMap = new MapMessage();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequiredNumber(REQ_PARENT_ID, "家长id");
            validateRequired(REQ_IMEI, "IMEI");
            if (!hasSessionKey()) {
                validateRequestNoSessionKey(REQ_PARENT_ID, REQ_IMEI);
            } else {
                validateRequest(REQ_PARENT_ID, REQ_IMEI);
            }
//            if (!RuntimeMode.isTest()) {
//                validateVerifyCodeInfo();
//            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }

        // 根据parentId获取手机号，再发送验证码
        Long parentId = getRequestLong(REQ_PARENT_ID);
        MapMessage sendVerifyCodeResult = new MapMessage();

        String mobile = sensitiveUserDataServiceClient.showUserMobile(parentId, "open api", SafeConverter.toString(parentId));

        if (mobile != null) {
            try {
                if (!RuntimeMode.isTest()) {
                    validateVerifyCodeInfo(mobile);
                }
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    return failMessage(e);
                } else {
                    return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
                }
            }
            SmsType smsType = SmsType.APP_PARENT_VERIFY_MOBILE_LOGIN_MOBILE;
            sendVerifyCodeResult = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    mobile,
                    smsType.name(),
                    false);
        }

        // generate response message
        if (!sendVerifyCodeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, sendVerifyCodeResult.getInfo());
            return resultMap;
        }

        // 操作成功，更新MEMCACHE信息
        updateMemCacheInfo(mobile);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    private static String addValue(String orgValue, String addedValue) {
        if (StringUtils.isEmpty(orgValue)) {
            return addedValue;
        }

        if (StringUtils.isEmpty(addedValue) || orgValue.contains(addedValue)) {
            return orgValue;
        }

        return orgValue + "," + addedValue;
    }

    private boolean validateLimitation(String appKey, String userMobile, String clientIp) {
        return true;
//        // FIXME IP白名单
//        String smsWhiteIps = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "sms_white_ip");
//        if (StringUtils.isNoneBlank(smsWhiteIps) && smsWhiteIps.contains(clientIp)) {
//            return true;
//        }
//
//        String pickUpLog = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "pick_up_log");
//        if (StringUtils.isNoneBlank(pickUpLog) && "1".equals(pickUpLog)) {
//            LogCollector.info("backend-general", MiscUtils.map(
//                    "env", RuntimeMode.getCurrentStage(),
//                    "usertoken", userMobile,
//                    "appKey", appKey,
//                    "clientIp", clientIp,
//                    "op", "sendRegisterVerifyCodeV1"
//            ));
//        }
//
//        // FIXME 有人盗刷验证码接口，临时按照IP进行计数，超过20就不发了
//        String cacheKey = "SMS_IP_COUNT:" + clientIp;
//        long count = washingtonCacheSystem.CBS.flushable.incr(cacheKey, 1, 1, 3600);
//
//        return count <= 10;
    }

}

