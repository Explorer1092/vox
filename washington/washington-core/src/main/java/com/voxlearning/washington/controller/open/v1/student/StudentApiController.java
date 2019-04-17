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

package com.voxlearning.washington.controller.open.v1.student;

import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.entity.like.UserLikedSummary;
import com.voxlearning.utopia.service.action.api.document.*;
import com.voxlearning.utopia.service.action.client.ActionLoaderClient;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.RefStatus;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.utopia.service.userlevel.api.entity.UserActivationHomeLevel;
import com.voxlearning.utopia.service.userlevel.api.mapper.UserActivationLevel;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.utopia.service.vendor.client.ThirdPartLoginServiceClient;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.client.UserLikeServiceClient;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_STUDENT;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.REQ_CLAZZ_LEVEL;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_GROUP_NAME;
import static com.voxlearning.washington.controller.open.v1.student.StudentApiConstants.*;

/**
 * Student API for App
 * Created by Shuai Huan on 2015/05/08.
 */
@Controller
@RequestMapping(value = "/v1/student")
@Slf4j
public class StudentApiController extends AbstractStudentApiController {

    private static String JPUSH_TAG_BY_SERVICE_VERSION = "2.4.0";
    private static String JPUSH_EXCLUDE_TAG_BY_SERVICE_VERSION = "2.8.2";

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;
    @Inject
    private AsyncVendorServiceClient asyncVendorServiceClient;
    @Inject
    private ActionLoaderClient actionLoaderClient;
    @Inject
    private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject
    private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private UserLikeServiceClient userLikeServiceClient;

    @Inject
    private ThirdPartLoginServiceClient thirdPartLoginServiceClient;

    // Only for Mobile App Login
    @RequestMapping(value = "/applogin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage appLogin() {
        // FIXME 对于非法请求的拦截
        if (StringUtils.isBlank(getRequestString(REQ_APP_NATIVE_VERSION))) {
            log.warn("unknown app version request found. {}, {}, {}",
                    getRequestString(REQ_APP_KEY),
                    getRequestString(REQ_USER_CODE),
                    getWebRequestContext().getRealRemoteAddress());

            MapMessage resultMap = new MapMessage();
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            return resultMap;
        }

        // 学生端APP低于2.8.4版本时不允许登录学前
        if (isAndroidRequest(getRequest()) && loginVersionCheck("2.8.4.0", Ktwelve.INFANT, getRequestString(REQ_USER_CODE), UserType.STUDENT)) {
            MapMessage resultMap = new MapMessage();
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_KETWELVE_INFANT);
            return resultMap;
        }

        MapMessage loginResult = internalUserLogin();
        if (!RES_RESULT_SUCCESS.equals(loginResult.get(RES_RESULT))) {
            return loginResult;
        }

        MapMessage resultMap = new MapMessage();

        // 只允许移动端调用
        VendorApps apps = getApiRequestApp();
        if (!("17Student".equals(apps.getAppKey()) || "17JuniorStu".equals(apps.getAppKey()))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_NO_ACCESS_RIGHT_MSG);
            return resultMap;
        }

        // 判断返回的用户只有一个
        List retUserList = (List) loginResult.get(RES_USER_LIST);
        if (retUserList != null && retUserList.size() != 1) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_APP_ERROR_MSG);
            return resultMap;
        }

        Map<String, Object> loginUserInfo = (Map<String, Object>) retUserList.get(0);
        Long userId = (Long) loginUserInfo.get(RES_USER_ID);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);

        // FIXME 西安交通大学附属中学（30636，属于名校的初中部）, 临时禁止登陆和注册
        if (studentDetail.getClazz() != null && AppAuditAccounts.isForbiddenStudentRegisterSchool(studentDetail.getClazz().getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "暂不支持登录和注册");
            return resultMap;
        }

        // 如果中学学生账户登录2.0版本之前的学生端，给出提示不支持中学学生登录
        if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") < 0 && studentDetail.isJuniorStudent()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_SUPPORT_JUNIOR_MSG);
            return resultMap;
        }

        if (!getStudentResult(resultMap, studentDetail, getApiRequestApp())) {
            return resultMap;
        }

        // 获取用户的登录信息
        resultMap.add(RES_USER_ID, loginUserInfo.get(RES_USER_ID));
        resultMap.add(RES_SESSION_KEY, loginUserInfo.get(RES_SESSION_KEY));

        //如果账号异常，返回异常信息
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(userId);
        if (studentExtAttribute != null) {
            if (studentExtAttribute.isFreezing() || studentExtAttribute.isForbidden()) {
                resultMap.add(RES_USER_ACCOUNT_STATUS, studentExtAttribute.getAccountStatus().name());
            }

            if (studentExtAttribute.getAppUserTimeLimit() != null) {
                resultMap.add(RES_USE_TIME_LIMIT, studentExtAttribute.getAppUserTimeLimit());
            }
        }

        // 获取用户的其他个人信息
        processExtraInfo(studentDetail, resultMap);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }


    @RequestMapping(value = "/appSourceLogin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage appDaiteLogin() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_LOGIN_TOKEN, "验证登录的token");
            validateRequired(REQ_LOGIN_SOURCE, "token的所属source");
            validateRequestNoSessionKey(REQ_LOGIN_TOKEN, REQ_LOGIN_SOURCE);
        } catch (IllegalArgumentException e) {
            logger.error("login_source:{} , login_token:{}", getRequestString(REQ_LOGIN_SOURCE), getRequestString(REQ_LOGIN_TOKEN));
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long userId = null;
        MapMessage message = thirdPartLoginServiceClient.getThirdPartLoginService().checkLogin(getRequestString(REQ_LOGIN_SOURCE), getRequestString(REQ_LOGIN_TOKEN));
        if (!message.isSuccess()) {//api调用失败
            logger.error("login_source:{} , login_token:{}, message:{}", getRequestString(REQ_LOGIN_SOURCE), getRequestString(REQ_LOGIN_TOKEN), message);
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, "token校验用户登录失败");
            return resultMap;
        }
        if (null == message.get("userId")) {//如果用户ID为空，表示用户未登录
            logger.error("login_source:{} , login_token:{}", getRequestString(REQ_LOGIN_SOURCE), getRequestString(REQ_LOGIN_TOKEN));
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "token校验用户登录失败");
            return resultMap;
        }
        userId = SafeConverter.toLong(message.get("userId"));

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);

        if (!getStudentResult(resultMap, studentDetail, getApiRequestApp())) {//组装学生信息
            logger.error("login_source:{} , login_token:{}", getRequestString(REQ_LOGIN_SOURCE), getRequestString(REQ_LOGIN_TOKEN));
            logger.error("getStudentResult failt: {}", resultMap);
            return resultMap;
        }

        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        if (studentExtAttribute != null) {
            if (studentExtAttribute.isForbidden() || studentExtAttribute.isFreezing()) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_FORBIDDEN);
                return resultMap;
            }
            if (studentExtAttribute.getAppUserTimeLimit() != null) {
                resultMap.add(RES_USE_TIME_LIMIT, studentExtAttribute.getAppUserTimeLimit());
            }
        }

        //设置App的session
        String sessionKey = attachUser2RequestApp(userId);

        //设置H5的Session
        User user = raikouSystem.loadUser(userId);
        RoleType roleType = RoleType.of(user.getUserType());
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        getWebRequestContext().saveAuthenticationStates(-1, user.getId(), ua.getPassword(), roleType);

        Set<String> tagSet = getUserMessageTagList(studentDetail.getId());
        resultMap.add(RES_JPUSH_TAGS, tagSet);
        resultMap.put("mustBindMobile", false);//前端要求写死
        resultMap.put("showLogoutEntrance", false);//前端要求写死
        resultMap.add("is_show_help", !studentDetail.isInPaymentBlackListRegion());
        resultMap.add(RES_USER_ID, userId);
        resultMap.add(RES_SESSION_KEY, sessionKey);
        processExtraInfo(studentDetail, resultMap);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 通过sessionKey 登录
     * 第三方登录的时候调用
     * cookie
     *
     * @return
     */
    @RequestMapping(value = "/appSessionKeyLogin.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage appSessionKeyLogin() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail == null) {
            return failMessage("请重新登录");
        }
        if (!getStudentResult(resultMap, studentDetail, getApiRequestApp())) {//组装学生信息
            logger.error("getStudentResult failt: {}", resultMap);
            return resultMap;
        }
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        if (studentExtAttribute != null) {
            if (studentExtAttribute.getAppUserTimeLimit() != null) {
                resultMap.add(RES_USE_TIME_LIMIT, studentExtAttribute.getAppUserTimeLimit());
            }
        }
        //设置H5的cookie
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(studentDetail.getId());
        getWebRequestContext().saveAuthenticationStates(-1, ua.getId(), ua.getPassword(), RoleType.of(studentDetail.getUserType()));
        Set<String> tagSet = getUserMessageTagList(studentDetail.getId());
        resultMap.add(RES_JPUSH_TAGS, tagSet);
        resultMap.add("is_show_help", !studentDetail.isInPaymentBlackListRegion());
        resultMap.add(RES_USER_ID, ua.getId());
        resultMap.put("mustBindMobile", false);//前端要求写死
        resultMap.put("showLogoutEntrance", studentDetail.getWebSource());//前端要求写死
        if (StringUtils.isBlank(getRequestString("vendor"))) {
            resultMap.put("isVendor", true);// 标识第三方登录：目前接入 懂你
            resultMap.put("vendor", true);// 兼容ios 坑死了
        } else {
            resultMap.put("vendor", getRequestString("vendor"));
        }
        resultMap.add(RES_SESSION_KEY, getRequestString(RES_SESSION_KEY));
        processExtraInfo(studentDetail, resultMap);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    /**
     * 手机号+验证码登录
     */
    @RequestMapping(value = "/apploginbymobile.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage loginByMobileAndVerifyCode() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_CODE, "手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequestNoSessionKey(REQ_USER_CODE, REQ_VERIFY_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String userMobile = getRequestString(REQ_USER_CODE);
        String code = getRequestString(REQ_VERIFY_CODE);

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(userMobile, code, SmsType.APP_STUDENT_VERIFY_MOBILE_LOGIN_MOBILE.name());
        if (!validateResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
            return resultMap;
        }

        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(userMobile, UserType.STUDENT);
        if (ua == null || !ua.isMobileAuthenticated()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_NOT_EXIST_ERROR_MSG);
            return resultMap;
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(ua.getId());
        if (studentDetail == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
            return resultMap;
        }

        // FIXME 西安交通大学附属中学（30636，属于名校的初中部）, 临时禁止登陆和注册
        if (studentDetail.getClazz() != null && AppAuditAccounts.isForbiddenStudentRegisterSchool(studentDetail.getClazz().getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "暂不支持登录和注册");
            return resultMap;
        }

        if (!getStudentResult(resultMap, studentDetail, getApiRequestApp())) {
            return resultMap;
        }

        // 如果中学学生账户登录2.0版本之前的学生端，给出提示不支持中学学生登录
        if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") < 0 && studentDetail.isJuniorStudent()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_SUPPORT_JUNIOR_MSG);
            return resultMap;
        }

        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        if (studentExtAttribute != null && studentExtAttribute.isFreezing()) {
            studentServiceClient.freezeStudent(studentDetail.getId(), false);
        }

        // 生成vendorAppsUserRef
        String sessionKey = attachUser2RequestApp(ua.getId());

        // 获取用户的登录信息

        resultMap.add(RES_USER_ID, ua.getId());
        resultMap.add(RES_SESSION_KEY, sessionKey);

        processExtraInfo(studentDetail, resultMap);

        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(ua.getId(),
                getWebRequestContext().getRealRemoteAddress(),
                UserRecordMode.LOGIN,
                OperationSourceType.app,
                false,
                getAppType());

        getWebRequestContext().saveAuthenticationStates(-1, studentDetail.getId(), ua.getPassword(), RoleType.ROLE_STUDENT);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;

    }

    @RequestMapping(value = "/unfreezestudent.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage unFreezeStudent() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_VERIFY_CODE, "验证码");
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String userMobile = getParentMobileByStudentId(getRequestLong(REQ_USER_ID));
        if (StringUtils.isBlank(userMobile)) {
            String mobile = sensitiveUserDataServiceClient.showUserMobile(getRequestLong(REQ_USER_ID), "verify freeze student", SafeConverter.toString(getRequestLong(REQ_USER_ID)));
            if (mobile != null) {
                userMobile = mobile;
            }
        }
        String code = getRequestString(REQ_VERIFY_CODE);

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(userMobile, code, SmsType.APP_STUDENT_VERIFY_ACCOUNT_FREEZE_MOBILE.name());
        if (!validateResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
            return resultMap;
        }

        // 如果学生没绑定手机，就将此手机号绑定学生
        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(userMobile, UserType.STUDENT);
        if (ua == null || StringUtils.isBlank(ua.getSensitiveMobile())) {
            userServiceClient.activateUserMobile(getRequestLong(REQ_USER_ID), userMobile);
        }

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(getRequestLong(REQ_USER_ID));
        if (studentDetail == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
            return resultMap;
        }

        // 如果中学学生账户登录2.0版本之前的学生端，给出提示不支持中学学生登录
        if (VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") < 0 && studentDetail.isJuniorStudent()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_NOT_SUPPORT_JUNIOR_MSG);
            return resultMap;
        }

        studentServiceClient.freezeStudent(getRequestLong(REQ_USER_ID), false);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;

    }

    @RequestMapping(value = "/profile.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage profile() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        // 套壳验证
        if (!disguiseCheck()) {
            resultMap.add(RES_RESULT, RES_RESULT_STUDENT_DISGUISE);
            resultMap.add(RES_MESSAGE, RES_RESULT_DISGUISE_CHECK_ERROR);
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();

        // 2018-10-01 00:00:00 新版本APP更新提示 不可以跳过
        VendorApps apps = getApiRequestApp();
        if (!juniorAppLimitTime() && "17Student".equals(apps.getAppKey())) {
            if (studentDetail.isJuniorStudent() || studentDetail.isSeniorStudent()) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, "请重新登录");
                return resultMap;
            }
        }

//        if (studentDetail.getClazz() == null && VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") >= 0) {
//            resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
//            resultMap.add(RES_MESSAGE, RES_STUDENT_NO_CLAZZ_MSG);
//            return resultMap;
//        }

        // FIXME 西安交通大学附属中学（30636，属于名校的初中部）, 临时禁止登陆和注册
        if (studentDetail.getClazz() != null && AppAuditAccounts.isForbiddenStudentRegisterSchool(studentDetail.getClazz().getSchoolId())) {
            resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
            resultMap.add(RES_MESSAGE, "暂时禁止使用");
            return resultMap;
        }

        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        if (studentExtAttribute != null) {
            if (studentExtAttribute.isForbidden() || studentExtAttribute.isFreezing()) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_FORBIDDEN);
                return resultMap;
            }

            if (studentExtAttribute.getAppUserTimeLimit() != null) {
                resultMap.add(RES_USE_TIME_LIMIT, studentExtAttribute.getAppUserTimeLimit());
            }

        }
        resultMap.add(RES_USER_ID, studentDetail.getId());

        processExtraInfo(studentDetail, resultMap);

        //2.4以上的版本服务器端返回JPushTag信息
        //2.8.2以上（含)不返回JpushTag信息，返回UmengPushTag信息
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        if (VersionUtil.compareVersion(version, JPUSH_TAG_BY_SERVICE_VERSION) >= 0) {
            Set<String> tagSet = getUserMessageTagList(studentDetail.getId());
            resultMap.add(RES_JPUSH_TAGS, tagSet);
        }

        //是否显示自学乐园右上角的帮助
        resultMap.add("is_show_help", !studentDetail.isInPaymentBlackListRegion());

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        userLevelService.studentLogin(currentUserId());
        return resultMap;
    }

    /**
     * since v2.9.1
     * 获取成长等级、当月集赞数、获得成就数
     */
    @RequestMapping(value = "/level_profile.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage levelProfile() {
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                return failMessage(e);
            }
            return failMessage(e.getMessage());
        }

        // 套壳验证
        if (!disguiseCheck()) {
            return failMessage(RES_RESULT_STUDENT_DISGUISE, RES_RESULT_DISGUISE_CHECK_ERROR);
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        if (studentDetail.getClazz() == null && VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.0.0.0") >= 0) {
            return failMessage(RES_RESULT_STUDENT_ACCOUNT_EXCEPTION_CODE, RES_STUDENT_NO_CLAZZ_MSG);
        }

        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        if (studentExtAttribute != null) {
            if (studentExtAttribute.isForbidden() || studentExtAttribute.isFreezing()) {
                return failMessage(RES_RESULT_NEED_RELOGIN_CODE, RES_RESULT_FORBIDDEN);
            }
        }

        MapMessage resultMap = successMessage();
        resultMap.add(RES_USER_ID, studentDetail.getId());

        wrapStudentLevelInfo(studentDetail, resultMap);

        wrapStudentActivationInfo(studentDetail, resultMap);

        return resultMap;
    }

    private void wrapStudentActivationInfo(StudentDetail studentDetail, MapMessage resultMap) {
        if (null == studentDetail) {
            return;
        }

        UserActivationHomeLevel userHomeLevel = userLevelLoader.getUserHomeLevel(studentDetail.getId());
        if (null == userHomeLevel) {
            resultMap.put("homeLevel", 1);
            resultMap.put(RES_GROWTH_LEVEL, 1);
        } else {
            resultMap.put("homeLevel", userHomeLevel.getLevel());
            resultMap.put(RES_GROWTH_LEVEL, userHomeLevel.getLevel());
        }

        UserActivationLevel studentLevel = userLevelLoader.getStudentLevel(studentDetail.getId());
        if (null == studentLevel) {
            return;
        }

        resultMap.put("activation", studentLevel.getValue());
        resultMap.put("level", studentLevel.getLevel());
        resultMap.put("maxActivation", studentLevel.getLevel() == 6 ? 9999 : studentLevel.getLevelEndValue() + 1);
        resultMap.put("minActivation", studentLevel.getLevelStartValue());
        resultMap.put("levelName", studentLevel.getName());
    }

    /**
     * 根据学生id，英语作业id查询作业完成情况
     */
    @RequestMapping(value = "enhomeworkresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage enHomeworkResult() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest(REQ_EN_HOMEWORK_ID);
            validateRequired(REQ_EN_HOMEWORK_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, "功能已下线");
        return resultMap;

    }

    /**
     * 根据手机号来判断注册流程走向
     */
    @RequestMapping(value = "mobilebindingcheck.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage mobileBindingCheck() {
        MapMessage resultMap = new MapMessage();

        try {
            // FIXME 要做一个恶心的兼容。新版本需要判定手机号是否绑定了该老师
            if (getRequest().getParameter(REQ_TEACHER_ID) != null) {
                validateRequestNoSessionKey(REQ_USER_CODE, REQ_TEACHER_ID);
            } else {
                validateRequestNoSessionKey(REQ_USER_CODE);
            }
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String userMobile = getRequestString(REQ_USER_CODE);
        Long teacherId = getRequestLong(REQ_TEACHER_ID);
        if (!MobileRule.isMobile(userMobile)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, VALIDATE_ERROR_MOBILE_MSG);
            return resultMap;
        }
        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(userMobile, UserType.TEACHER);
        if (ua != null && Objects.equals(ua.getId(), teacherId)) {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(ua.getId());
            if (teacher != null && teacher.isPrimarySchool()) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "该号码已用于注册老师号，请更换号码");
                return resultMap;
            }
        }

        // 看学生是否绑定手机，已绑定，直接返回已绑定，提示去登录
        ua = userLoaderClient.loadMobileAuthentication(userMobile, UserType.STUDENT);
        if (ua != null) {
            resultMap.add(RES_STUDENT_MOBILE_BINDING, true);
            resultMap.add(RES_PARENT_MOBILE_BINDING, false);
            resultMap.add(RES_USER_LIST, Collections.emptyList());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

            // FIXME 中学还需要单独加一个数据，标记小升初
            if ("17JuniorStu".equals(getRequestString(REQ_APP_KEY))) {
                StudentDetail detail = studentLoaderClient.loadStudentDetail(ua.getId());
                if (detail != null && ((detail.getClazz() == null) || (detail.isPrimaryStudent() && detail.getClazz().isTerminalClazz()))) {
                    resultMap.add(RES_JUNIOR_TARGET_STUDENT, true);
                }
            }

            return resultMap;
        }

        // 如果学生没绑定手机，看有没有家长绑定此手机了，如果家长没有绑定，就直接继续原来注册流程
        ua = userLoaderClient.loadMobileAuthentication(userMobile, UserType.PARENT);
        if (ua == null) {
            resultMap.add(RES_STUDENT_MOBILE_BINDING, false);
            resultMap.add(RES_PARENT_MOBILE_BINDING, false);
            resultMap.add(RES_USER_LIST, Collections.emptyList());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        // 如果学生没绑定手机，但有家长绑定了这个手机号，则把家长的孩子全部返回（只有孩子姓名）
        // 如果家长没有绑定任何孩子，则让其继续注册流程。
        List<User> children = studentLoaderClient.loadParentStudents(ua.getId());
        if (CollectionUtils.isEmpty(children)) {
            resultMap.add(RES_STUDENT_MOBILE_BINDING, false);
            resultMap.add(RES_PARENT_MOBILE_BINDING, true);
            resultMap.add(RES_USER_LIST, Collections.emptyList());
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

        // 如果家长有绑定孩子，则返回绑定孩子列表。
        List<Map<String, Object>> studentList = new LinkedList<>();
        children.stream().forEach(e -> {
            Map<String, Object> childrenMap = new HashMap<>();
            childrenMap.put(RES_NICK_NAME, e.getProfile().getRealname());
            childrenMap.put(RES_AVATAR_URL, getUserAvatarImgUrl(e.getProfile().getImgUrl()));
            studentList.add(childrenMap);
        });

        resultMap.add(RES_STUDENT_MOBILE_BINDING, false);
        resultMap.add(RES_PARENT_MOBILE_BINDING, true);
        resultMap.add(RES_USER_LIST, studentList);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    /**
     * 记录学生每天做作业
     *
     * @return
     */
    @RequestMapping(value = "recordhomework.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage recordHomework() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                .StudentAppDoHomeworkRecordCacheManager_doHomework(getCurrentStudent().getId())
                .awaitUninterruptibly();
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    // 这个接口专门提供给初中学生端用
    // 目的是走统一配置
    // 包括img_domain，打分系数，各网络环境下的加载超时时间
    @RequestMapping(value = "getmessparams.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getMessParams() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        resultMap.add(RES_TIMEOUT_PARAM, getTimeoutParam());
        resultMap.add(RES_VOICE_RATIO, getVoiceRatio(studentDetail.getClazzLevel()));
        resultMap.add(RES_IMG_DOMAIN, getCdnBaseUrlStaticSharedWithSep());
        resultMap.add(RES_ORAL_SCORE_INTERVAL, getOralScoreInterval());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    /**
     * 获取客户端访问的schema
     *
     * @return
     */
    @RequestMapping(value = "fetchschema.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchSchema() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getCurrentStudentDetail();
        // schema灰度之内，用https
//        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "17Student", "Schema")) {
//            resultMap.add(RES_SCHEMA, "https");
//        } else {
//            resultMap.add(RES_SCHEMA, "http");
//        }

        resultMap.add(RES_SCHEMA, "https");

//        // maa灰度之内，用maa
//        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "17Student", "Maa")) {
//            resultMap.add(RES_MAA_FLAG, true);
//        } else {
//            resultMap.add(RES_MAA_FLAG, false);
//        }
        resultMap.add(RES_MAA_FLAG, true);  //已经全量

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    @RequestMapping(value = "/getpossibleaccount.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getPossibleAccount() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_NAME);
            validateRequired(REQ_CLAZZ_LEVEL);
            validateRequired(REQ_SCHOOL_ID);
            validateRequestNoSessionKey(REQ_STUDENT_NAME, REQ_CLAZZ_LEVEL, REQ_SCHOOL_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            return resultMap;
        }

        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        String studentName = getRequestString(REQ_STUDENT_NAME);
        int clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL);

        List<Map<String, Object>> studentList = getAccountInfoList(schoolId, studentName, clazzLevel);
        resultMap.put("student_list", studentList);
        resultMap.put(RES_RESULT, RES_RESULT_SUCCESS);

        return resultMap;
    }

    @RequestMapping(value = "/modifyscannumber.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyKlxScanNumber() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SCAN_NUMBER);
            validateDigitNumber(REQ_SCAN_NUMBER, "考试填涂号");
            validateRequest(REQ_SCAN_NUMBER);
        } catch (IllegalArgumentException e) {
            return failMessage(RES_RESULT_BAD_REQUEST_MSG);
        }

        String scanNumber = getRequestString(REQ_SCAN_NUMBER);

        StudentDetail student = getCurrentStudentDetail();
        if (student == null) {
            return failMessage(RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
        }
        if (student.getClazz() == null) {
            return failMessage(RES_STUDENT_CLAZZ_ERROR_MSG);
        }

        Long studentId = student.getId();
        Long schoolId = student.getClazz().getSchoolId();
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        int digit = 5; // 默认五位填涂号
        if (schoolExtInfo != null && schoolExtInfo.getScanNumberDigit() != null) {
            digit = Integer.max(digit, schoolExtInfo.getScanNumberDigit());
        }
        if (StringUtils.isBlank(scanNumber) || scanNumber.length() > digit) {
            return failMessage("填涂号位数不正确，请填写" + digit + "位数字");
        }

        List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(Collections.singletonList(studentId), false)
                .get(studentId);

        if (CollectionUtils.isEmpty(groups)) {
            return failMessage(RES_STUDENT_CLAZZ_ERROR_MSG);
        }

        // 非OTO也不管了，万一找到了不就绑定上了，岂不美哉
//        boolean requireScanNumber = requireScanNumber(student.getClazz().getEduSystem().getKtwelve(), groups);
//        if (!requireScanNumber) {
//            resultMap.put(RES_RESULT, RES_RESULT_SUCCESS);
//            return resultMap;
//        }

        MapMessage message = newKuailexueServiceClient.modifyKlxScanNumber(schoolId, studentId, scanNumber);
        if (!message.isSuccess()) {
            return failMessage(message.getInfo());
        }

        resultMap.put(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/requestaccesskey.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage requestAccessKey() {
        try {
            validateRequired(REQ_APPLY_APP_KEY);
            validateRequest(REQ_APPLY_APP_KEY);
        } catch (IllegalArgumentException e) {
            return failMessage(RES_RESULT_BAD_REQUEST_MSG);
        }

        Student student = getCurrentStudent();
        if (student == null) {
            return failMessage(RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
        }

        Long userId = student.getId();
        String applyAppKey = getRequestString(REQ_APPLY_APP_KEY);
        // 根据appKey查询
        VendorApps vendorApp = vendorLoaderClient.loadVendor(applyAppKey);
        if (vendorApp == null) {
            return failMessage(RES_RESULT_APP_ERROR_MSG);
        }

        // appKey有效，去查询用户的信息
        VendorAppsUserRef vendorUserRef = vendorLoaderClient.loadVendorAppUserRef(applyAppKey, userId);
        if (vendorUserRef != null) {
            return successMessage(RES_SESSION_KEY, vendorUserRef.getSessionKey());
        }

        // 如果没有的话，就注册一个新的
        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(applyAppKey, userId)
                .getUninterruptibly();

        // 注册失败
        if (!message.isSuccess()) {
            return failMessage(message.getInfo());
        }

        vendorUserRef = (VendorAppsUserRef) message.get("ref");

        return successMessage(RES_SESSION_KEY, vendorUserRef.getSessionKey());
    }

    @RequestMapping(value = "groups.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage groups() {
        MapMessage resultMap = new MapMessage();

        Student student = getCurrentStudent();
        if (student == null) {
            return failMessage(RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
        }

        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(student.getId(), false);

        List<Map<String, Object>> returnList = new ArrayList<>();

        if (groupMappers != null) {
            Map<Long, GroupMapper> idGroup = groupMappers.stream().collect(Collectors.toMap(GroupMapper::getId, group -> group));

            List<Long> clazzId = idGroup.values().stream().map(item -> item.getClazzId()).distinct().collect(Collectors.toList());
            Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazzsIncludeDisabled(clazzId)
                    .stream()
                    .collect(Collectors.toMap(Clazz::getId, Function.identity()));

            List<Long> groupIds = groupMappers.stream().map(GroupMapper::getId).collect(Collectors.toList());
            Map<Long, List<Teacher>> groupIdTeacherRefs = teacherLoaderClient.loadGroupTeacher(groupIds, RefStatus.VALID);

            for (Map.Entry<Long, List<Teacher>> entry : groupIdTeacherRefs.entrySet()) {
                GroupMapper groupMapper = idGroup.get(entry.getKey());
                List<Teacher> teachers = entry.getValue();

                if (groupMapper != null && CollectionUtils.isNotEmpty(teachers)) {
                    if (teachers.size() > 1) {
                        logger.error("group {} 归属两个老师", groupMapper.getId());
                    }
                    Teacher teacher = teachers.get(0);

                    Clazz clazz = clazzMap.get(groupMapper.getClazzId());

                    Map<String, Object> item = new HashMap<>();
                    item.put(RES_CLAZZ_ID, groupMapper.getClazzId());
                    item.put(RES_GROUP_ID, groupMapper.getId());
                    item.put(RES_GROUP_NAME, groupMapper.getGroupName());
                    item.put(RES_TEACHER_ID, teacher.getId());
                    item.put(RES_TEACHER_NAME, teacher.fetchRealname());
                    item.put(RES_CLAZZ_LEVEL_NAME, clazz != null ? clazz.getClazzLevel().getDescription() : "");
                    item.put(RES_CLAZZ_LEVEL, clazz != null ? clazz.getClazzLevel().getLevel() : "");
                    item.put(RES_CLAZZ_NAME, clazz != null ? clazz.getClassName() : "");
                    item.put(RES_CLAZZ_TYPE, clazz != null ? groupMapper.getGroupType() : "");
                    item.put(RES_SUBJECT, groupMapper.getSubject() != null ? groupMapper.getSubject().name() : "");
                    returnList.add(item);
                }
            }
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add("data", returnList);
        return resultMap;
    }

    private List<Map<String, Object>> getAccountInfoList(Long schoolId, String studentName, int clazzLevel) {
        String studentDefaultUrl = "/public/skin/parentMobile/images/new_icon/avatar_child_default.png";
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Long, Clazz> clazzMap = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled()
                .nature()
                .filter(p -> ClassJieHelper.toClazzLevel(p.getJie(), p.getEduSystemType()).getLevel() == clazzLevel)
                .toList()
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        Map<Long, List<Long>> clazzStudentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzIds(clazzMap.keySet());
        Set<Long> studentIds = clazzStudentIds.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userLoaderClient.loadUsers(studentIds);

        Map<User, Long> sameNameStudentIdClazzIdMap = new HashMap<>();

        clazzStudentIds.forEach((cid, sids) -> sids.forEach(sid -> {
            User student = userMap.get(sid);
            if (student != null && Objects.equals(studentName, student.fetchRealname())) {
                sameNameStudentIdClazzIdMap.put(student, cid);
            }
        }));

        sameNameStudentIdClazzIdMap.forEach((student, clazzId) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            Clazz clazz = clazzMap.get(clazzId);
            if (clazz != null && (clazz.getClazzLevel().getLevel() == clazzLevel || isContainGraduatedClazz(clazz, clazzLevel))) {
                String imgUrl = "";
                if (StringUtils.isBlank(student.fetchImageUrl())) {
                    imgUrl = getCdnBaseUrlStaticSharedWithSep() + studentDefaultUrl;
                } else {
                    imgUrl = getUserAvatarImgUrl(student.fetchImageUrl());
                }
                map.put("img_url", imgUrl);
                map.put("student_name", studentName);
                map.put("user_id", student.getId());
                map.put("class_name", clazz.formalizeClazzName());
                String mobile = SafeConverter.toString(sensitiveUserDataServiceClient.loadUserMobileObscured(student.getId()), "");
                map.put("mobile", mobile);
                result.add(map);
            }
        });

        return result;
    }

    private boolean isContainGraduatedClazz(Clazz clazz, int clazzLevel) {
        if (clazz.isTerminalClazz()) {
            if (clazz.getEduSystem() == EduSystemType.P6 && clazzLevel == ClazzLevel.SIXTH_GRADE.getLevel()) {
                return true;
            }
            if (clazz.getEduSystem() == EduSystemType.P5 && clazzLevel == ClazzLevel.FIFTH_GRADE.getLevel()) {
                return true;
            }
            if ((clazz.getEduSystem() == EduSystemType.J3 || clazz.getEduSystem() == EduSystemType.J4) && clazzLevel == ClazzLevel.NINTH_GRADE.getLevel()) {
                return true;
            }
        }
        return false;
    }

    private void processExtraInfo(StudentDetail studentDetail, MapMessage resultMap) {
        // 获取用户认证信息

        String studentMobile = sensitiveUserDataServiceClient.loadUserMobileObscured(studentDetail.getId());
        if (StringUtils.isNotBlank(studentMobile)) {
            resultMap.add(RES_USER_MOBILE, studentMobile);
        }

        //FIXME: 目前移动端没有用到email，所以这里直接用Obscured
        String ae = sensitiveUserDataServiceClient.loadUserEmailObscured(studentDetail.getId());
        if (StringUtils.isNotEmpty(ae)) {
            resultMap.add(RES_USER_EMAIL, ae);
        }

        resultMap.add(RES_USER_TYPE, studentDetail.getUserType());
        resultMap.add(RES_REAL_NAME, studentDetail.getProfile().getRealname());
        resultMap.add(RES_AVATAR_URL, getUserAvatarImgUrl(studentDetail));
        resultMap.add(RES_USER_GENDER, studentDetail.fetchGender());

        // 生日
        String birthday = studentDetail.fetchBirthdayFormat("%s年%s月%s日");
        if (StringUtils.isNoneBlank(birthday)) {
            resultMap.add(RES_USER_BIRTHDAY, birthday);
        }

        Ktwelve ktwelve = null;
        // 中学学生APP 是否小学毕业班
        boolean isTerminal = false;
        if (Objects.equals(getRequestString(REQ_APP_KEY), "17JuniorStu") && studentDetail.getClazz() != null && studentDetail.getClazz().getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED) {
            isTerminal = true;
        }
        if (studentDetail.getClazz() != null && !isTerminal) {

            resultMap.add(RES_CLAZZ_ID, studentDetail.getClazz().getId());
            resultMap.add(RES_CLAZZ_NAME, studentDetail.getClazz().formalizeClazzName());
            resultMap.add(RES_CLAZZ_LEVEL, studentDetail.getClazz().getClassLevel());

            resultMap.add(RES_SCHOOL_ID, studentDetail.getClazz().getSchoolId());
            resultMap.add(RES_SCHOOL_NAME, studentDetail.getStudentSchoolName());

            // 中小学标记
            ktwelve = studentDetail.getClazz().getEduSystem().getKtwelve();
            resultMap.add(RES_KTWELVE, ktwelve);

            ExRegion region = raikouSystem.loadRegion(studentDetail.getStudentSchoolRegionCode());
            if (region != null) {
                resultMap.add(RES_PROVINCE_CODE, region.getProvinceCode());
                resultMap.add(RES_PROVINCE_NAME, region.getProvinceName());
                resultMap.add(RES_CITY_CODE, region.getCityCode());
                resultMap.add(RES_CITY_NAME, region.getCityName());
                resultMap.add(RES_COUNTRY_CODE, region.getCountyCode());
                resultMap.add(RES_COUNTRY_NAME, region.getCountyName());
                resultMap.add(RES_REGION_CODE, region.getCountyCode());
            }
        }

        // 读取分组信息
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        if ((CollectionUtils.isNotEmpty(groupMappers) && !isTerminal)) {
            resultMap.add(RES_USER_GROUP_ID, GroupMapper.filter(groupMappers).idList());
            Set<String> subjects = groupMappers.stream().filter(p -> p.getSubject() != null).map(p -> p.getSubject().name()).collect(Collectors.toSet());
            resultMap.add(RES_USER_SUBJECTS, subjects);
        }
        resultMap.add(RES_INTEGRAL, studentDetail.getUserIntegral().getUsable());

        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(studentDetail.getId());
        resultMap.add(RES_BINDING_PARENT, CollectionUtils.isNotEmpty(studentParents));

        //since v2.7.0 增加成长等级、当月集赞数、获得成就数、头饰
        // Enhancement #53657
        // 2.9.1.0 及以上版本不读RES_ACHIEVEMENT_COUNT，RES_LIKE_COUNT，RES_GROWTH_LEVEL 三个字段
        boolean readFlag = VersionUtil.compareVersion(getRequestString(REQ_APP_NATIVE_VERSION), "2.9.1.0") < 0;
        if (readFlag) {
            wrapStudentLevelInfo(studentDetail, resultMap);
        }

        StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(currentUserId());
        if (null != studentInfo) {
            if (StringUtils.isBlank(studentInfo.getHeadWearId())) {
                resultMap.put(RES_HEAD_WEAR, "");
            } else {
                Privilege headWearPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(studentInfo.getHeadWearId());
                if (null != headWearPrivilege) {
                    resultMap.put(RES_HEAD_WEAR, getUserAvatarImgUrl(headWearPrivilege.getImg()));
                }
            }
        }
        // 是否第三方账号
        Set<String> landingSources = thirdPartyLoaderClient.loadLandingSource(studentDetail.getId(), "DongNi").stream().map(LandingSource::getSourceName).collect(Collectors.toSet());
        resultMap.add("landing_sources", landingSources);
        // 如果学生分组有O2O业务, 给个填涂号标志
        if (Ktwelve.SENIOR_SCHOOL == ktwelve || Ktwelve.JUNIOR_SCHOOL == ktwelve) {
            boolean requireScanNumber = requireScanNumber(ktwelve, groupMappers);
            // 懂你的不校验填涂号
            if (landingSources.contains("DongNi")) {
                requireScanNumber = false;
            }
            String scanNumber = "";
//            if (requireScanNumber) {
            // 即使不需要填涂号，如果有的话也返回
            KlxStudent klxStudent = newKuailexueLoaderClient.loadKlxStudentBy17Id(studentDetail.getId());
            scanNumber = klxStudent == null ? "" : StringUtils.defaultString(klxStudent.getScanNumber());
//            }
            Long schoolId = studentDetail.getClazz() == null ? null : studentDetail.getClazz().getSchoolId();
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
            boolean needScanNumber = schoolExtInfo != null && schoolExtInfo.isScanMachineFlag();
            resultMap.add(RES_REQUIRE_KLX_SCAN_NUMBER, requireScanNumber && needScanNumber);
            resultMap.add(RES_KLX_SCAN_NUMBER, scanNumber);
        } else {
            resultMap.add(RES_REQUIRE_KLX_SCAN_NUMBER, false);
            resultMap.add(RES_KLX_SCAN_NUMBER, "");
        }
    }

    // 获取中学语音打分档级区间
    private List getOralScoreInterval() {
        String regStr = getPageBlockContentGenerator().getPageBlockContentHtml("middle_school_homework", "oral_score_interval");
        regStr = regStr.replace("\r", "").replace("\n", "").replace("\t", "");
        List<Map> list = JsonUtils.fromJsonToList(regStr, Map.class);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    // 套壳检测， true：正常， false：异常, Android only
    private boolean disguiseCheck() {
        try {
            // FIXME 第一期为空时返回正常,避免意外, 如果有反馈,服务端及时修正
            if (!isAndroidRequest(getRequest()) || StringUtils.isBlank(getRequestString("app_sha"))) {
                return true;
            }

            boolean disguiseCheck = SafeConverter.toBoolean(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_STUDENT.name(), "disguise_check_use"));
            if (disguiseCheck) {
                BigInteger d = new BigInteger(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_STUDENT.name(), "disguise_check_d"));
                BigInteger n1 = new BigInteger(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_STUDENT.name(), "disguise_check_n1"));
                String appSha = getRequestString("app_sha");
                BigInteger c = new BigInteger(new String(org.apache.commons.codec.binary.Base64.decodeBase64(appSha), "UTF-8"));
                BigInteger j = c.modPow(d, n1);
                byte[] mt = j.toByteArray();
                String decodeS = new String(mt);
                String sig_hash = decodeS.substring(0, decodeS.length() - 10);
                String disguiseResult = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_STUDENT.name(), "disguise_check_result");
                return Objects.equals(disguiseResult, sig_hash);
            }
            return true;
        } catch (Exception e) {
            logger.error("disguise check error", e);
            return true;
        }
    }

    private boolean requireScanNumber(Ktwelve ktwelve, List<GroupMapper> groupMappers) {
        if (ktwelve == null || CollectionUtils.isEmpty(groupMappers)) {
            return false;
        }
        // 高中全科
        if (Ktwelve.SENIOR_SCHOOL == ktwelve) {
            return true;
        }
        // 初中非英语和语文
        if (Ktwelve.JUNIOR_SCHOOL == ktwelve) {
            return groupMappers.stream().filter(g -> g.getSubject() != null).anyMatch(g -> g.getSubject() != Subject.ENGLISH && g.getSubject() != Subject.CHINESE);
        }
        return false;
    }

    private void wrapStudentLevelInfo(StudentDetail studentDetail, MapMessage resultMap) {
        int growthLevel = 1; // 默认1级
        int studentLevel = 1; // 默认1级
        UserGrowth userGrowth = actionLoaderClient.getRemoteReference().loadUserGrowth(studentDetail.getId());
        if (null != userGrowth) {
            growthLevel = userGrowth.toLevel();
        }
        UserActivationLevel userActivationLevel = userLevelLoader.getStudentLevel(studentDetail.getId());
        if (null != userActivationLevel) {
            studentLevel = userActivationLevel.getLevel();
        }
        resultMap.add(RES_GROWTH_LEVEL, growthLevel);

        int achievementCount = 0;
        List<UserAchievementRecord> uarList = actionLoaderClient.getRemoteReference().loadUserAchievementRecords(studentDetail.getId());
        if (CollectionUtils.isNotEmpty(uarList)) {
            for (UserAchievementRecord uar : uarList) {
                Achievement achievement = AchievementBuilder.build(uar);
                if (null != achievement && null != achievement.getType() && achievement.getRank() > 0) {
                    achievementCount += achievement.getRank();
                }
            }
        }
        resultMap.put(RES_ACHIEVEMENT_COUNT, achievementCount);

        int likedCount = 0;
        UserLikedSummary likedSummary = userLikeServiceClient.loadUserLikedSummary(studentDetail.getId(), new Date());
        if (likedSummary != null) {
            for (Integer n : likedSummary.getDailyCount().values()) {
                likedCount += n;
            }
        }

        resultMap.put(RES_LIKE_COUNT, likedCount);

        // 再加上 我的 tab里面的3个子tab名
        resultMap.add(RES_STUDENT_LEVEL_TAB_KEY, MapUtils.m("name", RES_STUDENT_LEVEL_TAB_NAME, "linkUrl", RES_STUDENT_LEVEL_TAB_URL, "value", studentLevel));
        resultMap.add(RES_LIKE_TAB_KEY, MapUtils.m("name", RES_LIKE_TAB_NAME, "linkUrl", RES_LIKE_TAB_URL, "value", likedCount));
        resultMap.add(RES_ACHIEVEMENT_TAB_KEY, MapUtils.m("name", RES_ACHIEVEMENT_TAB_NAME, "linkUrl", RES_ACHIEVEMENT_TAB_URL, "value", achievementCount));
    }

    /**
     * 组装 中学登录小学, 小学登录中学APP 返回值
     *
     * @param resultMap
     * @param studentDetail
     * @param apps
     * @return 是否通过校验, 如果没有通过检验则直接返回
     */
    private boolean getStudentResult(MapMessage resultMap, StudentDetail studentDetail, VendorApps apps) {
        // 在这里做一下是否封禁的判断
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        if (studentExtAttribute != null) {
            if (studentExtAttribute.isForbidden() || studentExtAttribute.isFreezing()) {
                resultMap.add(RES_RESULT, RES_RESULT_TOAST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_FORBIDDEN);
                return false;
            }
        }
        if (apps != null) {
            // 中学老师登录小学APP提示
            if (!juniorAppLimitTime() && "17Student".equals(apps.getAppKey())) {
                if (studentDetail.isJuniorStudent()) {
                    resultMap.add(RES_RESULT, RES_RESULT_BUTTON_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_JUNIOR_STUDENT);
                    resultMap.add(RES_OK_BUTTON, RES_RESULT_OK_BUTTON_TEXT);
                    resultMap.add(RES_OK_BUTTON_ACTION, RES_RESULT_JUNIOR_LINK);
                    resultMap.add(RES_CANCEL_BUTTON, RES_RESULT_CANCEL_BUTTON_TEXT);
                    return false;
                }
            } else if ("17JuniorStu".equals(apps.getAppKey())) {
                if (studentDetail.getClazz() != null && studentDetail.isPrimaryStudent() && studentDetail.getClazz().getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED) {
                    resultMap.add(RES_RESULT, RES_RESULT_BUTTON_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_PRIMARY_MESSAGE);
                    resultMap.add(RES_OK_BUTTON, RES_RESULT_OK_BUTTON_TEXT);
                    resultMap.add(RES_OK_BUTTON_ACTION, RES_RESULT_PRIMARY_LINK);
                    resultMap.add(RES_CANCEL_BUTTON, RES_RESULT_CANCEL_BUTTON_TEXT);
                    return false;
                }
            }
        }
        return true;
    }
}
