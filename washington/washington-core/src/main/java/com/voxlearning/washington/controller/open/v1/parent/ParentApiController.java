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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.vendor.api.constant.ParentChannelCLoginResult;
import com.voxlearning.utopia.service.vendor.api.constant.ParentChannelCLoginSource;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.wechat.api.WechatLoader;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.controller.open.v1.teacher.TeacherConfigApiController;
import com.voxlearning.washington.helpers.ValidateStudentIdHelper;
import com.voxlearning.washington.helpers.ValidateWechatOpenIdHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Parent API for App
 * Created by Hailong yang on 2015/09/09.
 */
@Controller
@RequestMapping(value = "/v1/parent")
@Slf4j
public class ParentApiController extends AbstractParentApiController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    private static String APP_KEY_PARENT = "17Parent";
    @Inject
    private ValidateStudentIdHelper validateStudentIdHelper;
    @Inject
    private ValidateWechatOpenIdHelper validateWechatOpenIdHelper;

    @ImportService(interfaceClass = WechatLoader.class)
    private WechatLoader wechatLoader;

    @RequestMapping(value = "creatparentshorturl.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage createParentShortUrl() {
        String url = getRequestString("url");
        if (StringUtils.isBlank(url)) {
            return MapMessage.errorMessage("url不能为空");
        }
        String shortUrl = TeacherConfigApiController.i7TinyUrl(url);
        return MapMessage.successMessage().add("shortUrl", shortUrl);
    }

    //免密码登录
    //针对版本>=1.3.6作特殊处理.需要有关键家长且有身份才能登录
    @RequestMapping(value = "/verifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCode() {
        Long parentId = getRequestLong(REQ_PARENT_ID);
        String mobile = getRequestString(REQ_USER_CODE);
        String code = getRequestString(REQ_VERIFY_CODE);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_VERIFY_CODE, "验证码");
            if (!hasSessionKey()) {
                validateRequestNoSessionKey(REQ_PARENT_ID, REQ_USER_CODE, REQ_VERIFY_CODE);
            } else {
                validateRequest(REQ_PARENT_ID, REQ_USER_CODE, REQ_VERIFY_CODE);
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            }
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        }

        try {
            // 只允许移动端调用
            VendorApps apps = getApiRequestApp();
            if (!APP_KEY_PARENT.equals(apps.getAppKey())) {
                return failMessage(RES_RESULT_NO_ACCESS_RIGHT_MSG);
            }

            if (StringUtils.isEmpty(mobile) || mobile.contains("*")) {
                String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(parentId);
                if (StringUtils.isNoneBlank(authenticatedMobile)) {
                    mobile = authenticatedMobile;
                }
            }

            SmsType smsType = SmsType.APP_PARENT_VERIFY_MOBILE_LOGIN_MOBILE;
            MapMessage validateMessage = validateMobileVerifyCode(smsType, mobile, code, true);
            if (!isSuccess(validateMessage)) {
                return validateMessage;
            }
            Map<UserType, User> userTypeUserMap = internalUserLogin(mobile, null, true);
            User loginResult = userTypeUserMap.containsKey(UserType.PARENT) ? userTypeUserMap.get(UserType.PARENT) : userTypeUserMap.get(UserType.STUDENT);
            if (loginResult == null) {
                return failMessage("用户不存在");
            }

            boolean isJunior = isJuniorLogin(loginResult);
            if (isJunior) {
                return failMessage(RES_RESULT_NOT_SUPPORT_JUNIOR_STUDENT_ERROR_MESSAGE);
            }
            if (UserType.of(loginResult.getUserType()) == UserType.STUDENT) {
                //登录的学生。就要去选身份
                return successMessage().add(RES_DEED_LOAD_IDENTITY, true).add(RES_STUDENT_ID, loginResult.getId());
            } else {
                //登录的是家长。直接往下走了。
                return doParentLogin(loginResult);
            }
        } catch (Exception ex) {
            String message;
            if (ex instanceof UtopiaRuntimeException || ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else {
                log.error("verifycode failed. mobile:{}, code:{}, ver:{}", mobile, code, ver, ex);
                message = "登录失败";
            }
            return failMessage(message);
        }
    }

    //找回密码登录
    //这里只做验证码校验。实际重置密码还是去用户中心重置的。
    @RequestMapping(value = "/forgotpassword/verifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage forgotpasswordVerifyCode() {
        String mobile = getRequestString(REQ_USER_CODE);
        String code = getRequestString(REQ_VERIFY_CODE);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);

        try {
            validateRequired(REQ_USER_CODE, "手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequestNoSessionKey(REQ_USER_CODE, REQ_VERIFY_CODE);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            }
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        }

        try {
            // 只允许移动端调用
            VendorApps apps = getApiRequestApp();
            if (!APP_KEY_PARENT.equals(apps.getAppKey())) {
                return failMessage(RES_RESULT_NO_ACCESS_RIGHT_MSG);
            }

            if (!MobileRule.isMobile(mobile)) {
                log.error("forgotpassword verifycode failed. mobile:{}, code:{}, ver:{}", mobile, code, ver);
                return failMessage(VALIDATE_ERROR_MOBILE_MSG);
            }

            User user;
            //如果手机号只是家长手机号。这里就不知道是绑定的那个学生。所以这里要做校验。
            UserAuthentication studentAuth = userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
            user = studentAuth == null ? null : raikouSystem.loadUser(studentAuth.getId());
            if (user == null) {
                UserAuthentication parentAuth = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
                user = parentAuth == null ? null : raikouSystem.loadUser(parentAuth.getId());
                if (user == null) {
                    return failMessage(RES_RESULT_MOBILE_NOT_EXIST_ERROR_MSG);
                } else {
                    return failMessage("该手机号未被学生号绑定，请用免密码登录");
                }
            }
            //客户端调重置密码的接口还需要验证码。所以最后一个参数是false.不从缓存删除验证码
            //TODO 已经沟通。新版本直接不请求这个接口了。没有意义。修改密码自己带着验证码去改密码去。
            SmsType smsType = SmsType.FORGOT_PASSWORD_SEND_VERIFY_CODE_PARENT_APP;
            return validateMobileVerifyCode(smsType, mobile, code, false);
        } catch (Exception ex) {
            String message;
            if (ex instanceof UtopiaRuntimeException || ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else {
                log.error("forgotpassword verifycode failed. mobile:{}, code:{}, ver:{}", mobile, code, ver, ex);
                message = "验证码验证失败";
            }
            return failMessage(message);
        }
    }


    /**
     * 用户信息
     * <p><a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=20447800#id-%E5%AE%B6%E9%95%BF%E9%80%9AAPP%E6%8E%A5%E5%8F%A3-%E5%AE%9E%E6%97%B6%E8%8E%B7%E5%8F%96%E4%B8%AA%E4%BA%BA%E4%BF%A1%E6%81%AF">interface wiki</a></a></p>
     */
    @RequestMapping(value = "/getUserInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUserInfo() {
        try {
            String version = getRequestString(REQ_APP_NATIVE_VERSION);
            // 只允许移动端调用
            VendorApps apps = getApiRequestApp();
            if (!APP_KEY_PARENT.equals(apps.getAppKey())) {
                return failMessage(RES_RESULT_NO_ACCESS_RIGHT_MSG);
            }

            User curUser = getApiRequestUser();
            if (UserType.PARENT != UserType.of(curUser.getUserType())) {
                return failMessage(RES_RESULT_USER_TYPE_ERROR_MSG);
            }

            //记录当天登录行为加活跃值
            if (curUser.fetchUserType() == UserType.PARENT) {
                userLevelService.parentLogin(curUser.getId());
            }

            // 获取用户的登录信息
            return parentUserBasicInfo(curUser, version);
        } catch (Exception ex) {
            String message;
            if (ex instanceof UtopiaRuntimeException || ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else {
                message = "获取用户信息失败";
            }
            return failMessage(message);
        }
    }

    //只验证验证码是否正确
    //1.3.6版本之后解耦原来的方法，
    @RequestMapping(value = "onlyverifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCodeOnly() {
        Long sid = getRequestLong(REQ_STUDENT_ID);
        String mobile = getRequestString(REQ_CONTACT_MOBILE);
        String code = getRequestString(REQ_VERIFY_CODE);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequest(REQ_STUDENT_ID, REQ_CONTACT_MOBILE, REQ_VERIFY_CODE);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            }
            return failMessage(e.getMessage());
        }
        try {
            if (StringUtils.isBlank(mobile)) {
                String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(sid);
                if (StringUtils.isBlank(authenticatedMobile)) {
                    return failMessage("获取手机号码失败");
                }
                mobile = authenticatedMobile;
            }
            SmsType smsType = SmsType.APP_PARENT_BIND_IDENTITY_VERIFY_MOBILE_LOGIN_MOBILE;
            return validateMobileVerifyCode(smsType, mobile, code, true);
        } catch (Exception ex) {
            String message;
            if (ex instanceof UtopiaRuntimeException || ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else {
                log.error("bind mobile failure,[mobile:{}, studentId:{}, code:{},  verstion:{}]", mobile, sid, code, ver, ex);
                message = "验证码验证失败";
            }
            return failMessage(message);
        }
    }


    //绑定家长身份并登录
    //1.3.6版本之后启用
    //1、在使用手机号+验证码 免密码登录过后，选择完身份调用这个接口===1.5.3去掉免密码登录了。
    //2、家长端注册学生号过后也调用这个接口
    @RequestMapping(value = "bindidentityandlogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindIdentityAndLogin() {
        return failMessage(RES_RESULT_UNSUPPORT_ANSWER_EXAM);
    }

    //验证验证码、绑定家长身份、登录
    //1.3.6版本之后启用
    //使用学生用户名密码登录选择完身份后全都调的这个接口
    @RequestMapping(value = "verifyandbindandlogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCodeAndBindIdentityAndLogin() {
       return failMessage(RES_RESULT_UNSUPPORT_ANSWER_EXAM);
    }


    //只验证用户名和密码是否正确，不登录
    //1.3.6需要家长身份的版本开始启用
    @RequestMapping(value = "/verifypassword.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage verifyPassword() {
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_USER_CODE, "用户名");
            validateRequired(REQ_PASSWD, "密码");
            if (!hasSessionKey()) {
                validateRequestNoSessionKey(REQ_USER_CODE, REQ_PASSWD);
            } else {
                validateRequest(REQ_USER_CODE, REQ_PASSWD);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        try {

            String userCode = getRequestString(REQ_USER_CODE);
            String userPassword = getRequestString(REQ_PASSWD);
            Map<UserType, User> userTypeUserMap = internalUserLogin(userCode, userPassword, false);
            User student = userTypeUserMap.get(UserType.STUDENT);
            User parent = userTypeUserMap.get(UserType.PARENT);
            MapMessage resultMap = new MapMessage();
            if (student != null) {
                boolean isJunior = isJuniorLogin(student);
                if (isJunior) {
                    return failMessage(RES_RESULT_NOT_SUPPORT_JUNIOR_STUDENT_ERROR_MESSAGE);
                }
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_DEED_LOAD_IDENTITY, true);
                resultMap.add(RES_STUDENT_ID, student.getId());

                String uuid = getRequestString(REQ_UUID);
                validateStudentIdHelper.storeBindStudentIdWithUUID(uuid, student.getId());
                return resultMap;
            } else if (parent != null) {
                //1.5.3以上只有在添加孩子有这个接口调用了。这时候如果输入的信息只能查到家长。就报错。
                //1.5.3以前可能在登录界面调用这个接口。能通过。
                if (VersionUtil.compareVersion(ver, "1.5.3") > 0) {
                    return failMessage("学生账号不存在或密码错误");
                }
                boolean tempMatch = false;
                if (StringUtils.isNotBlank(userPassword) && StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(parent.getId()), userPassword)) {
                    tempMatch = true;
                }
                if (parent.getId() == 20001L || tempMatch || parent.getId() == 29547620L) {
                    return doParentLogin(parent);
                }
            }
            return failMessage("学生账号不存在或密码错误");
        } catch (Exception ex) {
            String message;
            if (ex instanceof UtopiaRuntimeException || ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else {
                log.error("login parent app failed. ver:{}", ver, ex);
                message = "登录错误未知";
            }
            return failMessage(message);
        }
    }

    //1.3.6后添加孩子
    //1.判断要添加的孩子是否是家长的孩子，2.用户名密码校验
    //历史原因。。我已经不知道什么原因了。这个接口跟verifypassword.vpage的接口干的事情一模一样。。。。。
    @RequestMapping(value = "/addchild.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addChild() {
        long studentId = getRequestLong(REQ_STUDENT_ID);
        String password = getRequestString(REQ_PASSWD);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_PASSWD, "密码");
            validateRequest(REQ_STUDENT_ID, REQ_PASSWD);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        try {
            if (studentId <= 0) {
                return failMessage("用户不存在");
            }
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            if (clazz != null && clazz.isTerminalClazz()) {
                return failMessage("暂不支持小学毕业账号").add("isGraduate", Boolean.TRUE);
            }
            Long parentId = getCurrentParentId();
            if (studentIsParentChildren(parentId, studentId)) {
                return failMessage("此学生和家长已关联");
            }

            Map<UserType, User> userTypeUserMap = internalUserLogin(String.valueOf(studentId), password, false);
            User loginResult = userTypeUserMap.containsKey(UserType.PARENT) ? userTypeUserMap.get(UserType.PARENT) : userTypeUserMap.get(UserType.STUDENT);
            if (loginResult == null) {
                return failMessage("用户不存在或密码错误");
            }

            MapMessage resultMap = new MapMessage();
            if (UserType.of(loginResult.getUserType()) == UserType.STUDENT) {

                boolean isJunior = isJuniorLogin(loginResult);
                if (isJunior) {
                    return failMessage(RES_RESULT_NOT_SUPPORT_JUNIOR_STUDENT_ERROR_MESSAGE);
                }

                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_MESSAGE, "用户名密码验证通过");
                resultMap.add(RES_DEED_LOAD_IDENTITY, true);
                validateStudentIdHelper.storeBindStudentIdWithParentId(parentId, loginResult.getId());
                resultMap.add(RES_STUDENT_ID, loginResult.getId());
            } else if (UserType.of(loginResult.getUserType()) == UserType.PARENT) {
                List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(loginResult.getId());
                if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                    resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                    resultMap.add(RES_MESSAGE, "用户名密码验证通过");
                    resultMap.add(RES_DEED_LOAD_IDENTITY, true);
                    validateStudentIdHelper.storeBindStudentIdWithParentId(loginResult.getId(), studentParentRefs.get(0).getStudentId());
                    resultMap.add(RES_STUDENT_ID, studentParentRefs.get(0).getStudentId());
                } else {
                    return failMessage("该家长未关联任何学生");
                }
            } else {
                return failMessage("请使用家长或学生号登录");
            }
            return resultMap;
        } catch (Exception ex) {
            String message;
            if (ex instanceof UtopiaRuntimeException || ex instanceof IllegalArgumentException) {
                message = ex.getMessage();
            } else {
                log.error("login parent app failed. ver:{}", ver, ex);
                message = "登录错误未知";
            }
            return failMessage(message);
        }
    }

    /**
     * 注册C端时用户验证码及后续逻辑接口
     * 1.5.3
     */
    @RequestMapping(value = "verify_code_channel_c.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCodeWithChannelC() {
        String mobile = getRequestString(REQ_CONTACT_MOBILE);
        String code = getRequestString(REQ_VERIFY_CODE);
        Integer source = getRequestInt(REQ_LOGIN_SOURCE_CHANNEL_C);
        String uuid = getRequestString(REQ_UUID);
        try {
            validateRequired(REQ_CONTACT_MOBILE, "手机号码");
            validateRequired(REQ_VERIFY_CODE, "验证码");
                validateRequestNoSessionKey(REQ_CONTACT_MOBILE, REQ_VERIFY_CODE, REQ_LOGIN_SOURCE_CHANNEL_C, REQ_UUID, REQ_AGREE_OR_NOT);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        mobile = StringUtils.trim(mobile);
        if (!NumberUtils.isNumber(mobile) || mobile.length() > 11)
            return failMessage(VALIDATE_ERROR_MOBILE_MSG);
        ParentChannelCLoginSource loginSource = ParentChannelCLoginSource.parseWithUnknown(source);
        if (loginSource == ParentChannelCLoginSource.UNKNOWN) {
            return failMessage(RES_RESULT_LOGIN_SOURCE_ERROR);
        }

        Map<UserType, User> userTypeUserMap = internalUserLogin(mobile, code, false);
        User parent = userTypeUserMap.getOrDefault(UserType.PARENT, null);
        // 支持临时密码登录
        if (parent != null) {
            if (StringUtils.isNotBlank(code) && StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(parent.getId()), code)) {
                return doParentLogin(parent).add(RES_VERIFY_CHANNEL_C_RESULT, ParentChannelCLoginResult.LOGIN_SUCCESS.getType());
            }
        }
        // 银座九号小学支持id+pwd登录
        if (isMatchIdPwdLoginCondition(parent) || (parent != null && (parent.getId().equals(20001L) || parent.getId().equals(214303936L)))) {
            return doParentLogin(parent).add(RES_VERIFY_CHANNEL_C_RESULT, ParentChannelCLoginResult.LOGIN_SUCCESS.getType());
        }

        //这里开始就是真实用户的逻辑了。
        //验证验证码
        SmsType smsType = SmsType.APP_PARENT_VERIFY_MOBILE_LOGIN_MOBILE;
        MapMessage mapMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, smsType.name());
        if (!mapMessage.isSuccess()) {
            return failMessage(mapMessage.getInfo());
        }
        //这个手机号绑定的所有用户
        List<UserAuthentication> authenticationList = userLoaderClient.loadMobileAuthentications(mobile);
        //绑定家长号，直接登录
        if (CollectionUtils.isNotEmpty(authenticationList) && authenticationList.stream().anyMatch(p -> p.getUserType() == UserType.PARENT)) {
            Long userId = authenticationList.stream().filter(p -> p.getUserType() == UserType.PARENT).findFirst().get().getId();
            parent = raikouSystem.loadUser(userId);
            Integer nextStep;
            List<User> parentStudents = studentLoaderClient.loadParentStudents(userId);
            if (CollectionUtils.isNotEmpty(parentStudents)) {
                nextStep = ParentChannelCLoginResult.LOGIN_SUCCESS.getType();
            } else {
                nextStep = loginSource.getLoginResult().getType();
            }
            return doParentLogin(parent).add(RES_VERIFY_CHANNEL_C_RESULT, nextStep);
        }
        //有可能不走学生相关校验逻辑。没有家长直接去注册C端家长
        if (!loginSource.getCheckStudent()) {
            MapMessage registerMessage = parentRegisterHelper.registerChannelCParent(mobile, RoleType.ROLE_PARENT, uuid, "17Parent-c");
            if (!registerMessage.isSuccess()) {
                return failMessage(registerMessage.getInfo());
            }
            User newUser = (User) registerMessage.get("user");
            return doParentLogin(newUser).add(RES_VERIFY_CHANNEL_C_RESULT, loginSource.getLoginResult().getType());
        }
        //绑定学生号。选择身份
        if (CollectionUtils.isNotEmpty(authenticationList) && authenticationList.stream().anyMatch(p -> p.getUserType() == UserType.STUDENT)) {
            Long userId = authenticationList.stream().filter(p -> p.getUserType() == UserType.STUDENT).findFirst().get().getId();
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
//           当且仅当是小学学生时。才允许绑定。否则直接跳过。
            if (studentDetail != null && studentDetail.isPrimaryStudent()) {
                MapMessage resultMap = new MapMessage();
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                validateStudentIdHelper.storeBindStudentIdWithUUID(uuid, userId);
                resultMap.add(RES_STUDENT_ID, userId);
                resultMap.add(RES_VERIFY_CHANNEL_C_RESULT, ParentChannelCLoginResult.LOAD_IDENTITY.getType());
                return resultMap;
            }
        }
        return successMessage().add(RES_VERIFY_CHANNEL_C_RESULT, ParentChannelCLoginResult.CLIENT_TELL_USER_CHOOSE_ADD_STUDENT.getType());
    }

    //下面2个是兼容1.3.6之前的版本。由于客户端Bug没有做未登录用户的升级处理。
    //所以这里先保留这两个接口。提示用户去升级新版本
    //学生没绑定手机号时绑定手机号码并登录
    @RequestMapping(value = "/verifycodeBindMobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifycodeBindMobile() {
        try {
            validateRequired(REQ_CONTACT_MOBILE, "手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequired(REQ_STUDENT_ID, "学生号");
            validateRequestNoSessionKey(REQ_CONTACT_MOBILE, REQ_VERIFY_CODE, REQ_STUDENT_ID);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            }
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        }
        return failMessage(RES_RESULT_VERSION_OLD_ERROR_MSG);
    }

    //首页正常用户名密码登录
    @RequestMapping(value = "/applogin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage appLogin() {
        try {
            validateRequired(REQ_USER_CODE, "用户名");
            validateRequired(REQ_PASSWD, "密码");
            validateRequestNoSessionKey(REQ_USER_CODE, REQ_PASSWD);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            }
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        }
        return failMessage(RES_RESULT_VERSION_OLD_ERROR_MSG);
    }


    /**
     * 同意条款记录
     *
     * @return
     */
    @RequestMapping(value = "/provisions/agree.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage agreeBrandProvisions() {
        try {
            validateRequired(REQ_AGREE_OR_NOT, "是否同意");
            validateRequest(REQ_AGREE_OR_NOT);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                return failMessage(e);
            }
            return failMessage(RES_RESULT_INTERNAL_ERROR_MSG);
        }
        User parent = getCurrentParent();
        if (parent == null) {
            return failMessage(RES_RESULT_USER_ERROR_MSG);
        }
        Boolean isAgree = getRequestBool(REQ_AGREE_OR_NOT);
        if (isAgree)
            parentServiceClient.agreeParentBrandFlag(parent.getId());
        else
            parentServiceClient.notAgreeParentBrandFlag(parent.getId());
        return successMessage();
    }

    @RequestMapping(value = "getTopNotice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getTopNotice() {
        User parent = getCurrentParent();
        if (parent == null)
            return failMessage(RES_RESULT_PARENT_ERROR_MSG);
        String slotId = "220103";
        List<NewAdMapper> data = userAdvertisementServiceClient.getUserAdvertisementService().loadNewAdvertisementData(getCurrentParentId(), slotId, getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));
        Map<String, Object> noticeMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(data)) {
            generateNoticeInfo(noticeMap, data.get(0));
            // 实时数据打点
            for (int i = 0; i < data.size(); i++) {
                if (Boolean.FALSE.equals(data.get(i).getLogCollected())) {
                    continue;
                }
                LogCollector.info("sys_new_ad_show_logs",
                        MiscUtils.map(
                                "user_id", getCurrentParentId(),
                                "env", RuntimeMode.getCurrentStage(),
                                "version", getRequestString("version"),
                                "aid", data.get(i).getId(),
                                "acode", data.get(i).getCode(),
                                "index", i,
                                "slotId", slotId,
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", com.voxlearning.alps.calendar.DateUtils.dateToString(new Date()),
                                "agent", getRequest().getHeader("User-Agent"),
                                "uuid", UUID.randomUUID().toString(),
                                "system", getRequestString("sys"),
                                "system_version", getRequestString("sysVer")
                        ));
            }
        }
        return successMessage().add("notice", noticeMap);
    }

    private void generateNoticeInfo(Map<String, Object> noticeMap, NewAdMapper newAdMapper) {
        if (newAdMapper.getShowStartTime() != null) {
            Date startDate = new Date(newAdMapper.getShowStartTime());
            noticeMap.put("start_time", DateUtils.dateToString(startDate, "yyyy-MM-dd HH:mm:ss"));
        }
        if (newAdMapper.getShowEndTime() != null) {
            Date endDate = new Date((newAdMapper.getShowEndTime()));
            noticeMap.put("end_time", DateUtils.dateToString(endDate, "yyyy-MM-dd HH:mm:ss"));
        }
        noticeMap.put("name", newAdMapper.getName());
        noticeMap.put("content", newAdMapper.getContent());
        if (newAdMapper.getHasUrl()) {
            String linkUrl = AdvertiseRedirectUtils.redirectUrl(newAdMapper.getId(), 0, getRequestString(REQ_APP_NATIVE_VERSION), getRequestString(REQ_SYS), "", 0L);
            noticeMap.put("url", linkUrl);
        }
    }

}
