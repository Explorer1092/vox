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

package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.EmailRule;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.MapUtils;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilege;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilegeTpl;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskPrivilegeServiceClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.TeacherAgentLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.support.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * Created by jiangpeng on 16/3/14.
 */

@Controller
@RequestMapping(value = "/v1/teacher/user")
@Slf4j
public class TeacherUserApiController extends AbstractTeacherApiController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @ImportService(interfaceClass = VerificationService.class)
    private VerificationService verificationService;
    @Inject private TeacherAgentLoaderClient teacherAgentLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private TeacherTaskPrivilegeServiceClient teacherTaskPrivilegeServiceClient;


    /**
     * 通过手机号+验证码登录接口
     *
     * @return MapMessage
     */
    @RequestMapping(value = "login_by_code.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage loginByCode() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_MOBILE, "手机号");
            validateRequired(REQ_CODE, "验证码");
            if (getRequest().getParameter(REQ_USER_ID) != null) {
                // 需求wiki: http://wiki.17zuoye.net/pages/viewpage.action?pageId=44845258
                // 换设备验证的时候，传过来的是掩码手机号，所以需要传uid，此时将uid加入到签名校验中
                validateRequestNoSessionKey(REQ_MOBILE, REQ_CODE, REQ_USER_ID);
            } else {
                validateRequestNoSessionKey(REQ_MOBILE, REQ_CODE);
            }

        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String phoneNumber = getRequestString(REQ_MOBILE);
        if (phoneNumber.contains("*")) {
            // 需求wiki: http://wiki.17zuoye.net/pages/viewpage.action?pageId=44845258
            // 换设备验证的时候，传过来的是掩码手机号，此时通过uid拿手机号
            long userId = getRequestLong(REQ_USER_ID);
            phoneNumber = sensitiveUserDataServiceClient.loadUserMobile(userId);
        }

        boolean isMobile = MobileRule.isMobile(phoneNumber);
        if (!isMobile) {
            return failMessage(RES_RESULT_MOBILE_RULE_ERROR);
        }

        String code = getRequestString(REQ_CODE);
        MapMessage mapMessage = smsServiceClient.getSmsService().verifyValidateCode(phoneNumber, code, SmsType.APP_TEACHER_VERIFY_MOBILE_LOGIN_MOBILE.name());
        if (!mapMessage.isSuccess()) {
            return failMessage(mapMessage.getInfo());
        }
        List<UserAuthentication> authenticationList = userLoaderClient.loadMobileAuthentications(phoneNumber);
        if (CollectionUtils.isEmpty(authenticationList))
            return failMessage(RES_RESULT_MOBILE_NOT_REGIST_ERROR_MSG);
        UserAuthentication userAuthentication = authenticationList.stream().filter(u -> UserType.TEACHER == u.getUserType()).findFirst().orElse(null);
        if (userAuthentication == null)
            return failMessage(RES_RESULT_MOBILE_NOT_REGIST_ERROR_MSG);

        // 1.5.8 以下版本不允许INFANT老师登录
        if (loginVersionCheck("1.5.8.0", Ktwelve.INFANT, String.valueOf(userAuthentication.getId()), UserType.TEACHER)) {
            return failMessage(RES_RESULT_UNSUPPORT_KETWELVE_INFANT);
        }
        // 在这里做一下是否封禁的判断
        TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(userAuthentication.getId());
        if (teacherExtAttribute != null && (teacherExtAttribute.isForbidden() || teacherExtAttribute.isFreezing())) {
            resultMap.add(RES_RESULT, RES_RESULT_TOAST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_FORBIDDEN);
            return resultMap;
        }
        resultMap.add(RES_SESSION_KEY, attachUser2RequestApp(userAuthentication.getId()));
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(userAuthentication.getId());
        if (teacherDetail == null) {
            return failMessage(RES_TEACHER_NUMBER_ERROR_MSG);
        }

        if (!getTeacherResult(resultMap, teacherDetail, getApiRequestApp())) {
            return resultMap;
        }
        this.processTeacherProfile(teacherDetail, resultMap);
        afterUserLoginSuccess(teacherDetail.getId(), userAuthentication.getPassword(), RoleType.ROLE_TEACHER, false);
        return resultMap;
    }


    /**
     * 检查手机号是否已经注册
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/phone_number/check.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage checkPhoneNumber() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_CODE, "手机号");
            validateRequestNoSessionKey(REQ_USER_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String phoneNumber = getRequestString(REQ_USER_CODE);
        UserType userType = UserType.TEACHER;

        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(phoneNumber, userType);


        if (userAuthentication != null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_EXIST_MSG);
            return resultMap;
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        }

    }


    /**
     * 检查验证码是否正确
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/register/phone_code/check.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage checkRegistMobileCode() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_MOBILE, "手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequestNoSessionKey(RES_USER_MOBILE, REQ_VERIFY_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String phoneNumber = getRequestString(RES_USER_MOBILE);
        String verifyCode = getRequestString(REQ_VERIFY_CODE);

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(phoneNumber, verifyCode, SmsType.APP_TEACHER_VERIFY_MOBILE_REGISTER_MOBILE.name(), false);
        if (!validateResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    /**
     * 检查找回密码验证码是否正确
     * <p>
     * //todo 注意 这里是只检查验证码的正确性，真正的消费验证码在业务的下一步
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/forgotpassword/phone_code/check.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage checkRestpwdMobileCode() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_MOBILE, "手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequestNoSessionKey(RES_USER_MOBILE, REQ_VERIFY_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String phoneNumber = getRequestString(RES_USER_MOBILE);
        String verifyCode = getRequestString(REQ_VERIFY_CODE);

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(phoneNumber, verifyCode, SmsType.FORGOT_PASSWORD_SEND_VERIFY_CODE.name(), false);
        if (!validateResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }


    /**
     * 老师用户登录
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/login.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage teacherLogin() {

        //此处恶心了点,为了防止小学老师登陆直接通过internalUserLogin后会生成对应的sessionkey,所以在调用internalUserLogin方法前先拦截一次.
        // 等待小学老师支持登陆后,即可去掉此段代码.
        // 去不掉这段代码了,1.1.0.0版本之前的版本仍然不允许小学老师登陆 谁的锅?产品让写她的,songjiayuan啊哈哈
        if (loginVersionCheck("1.1.0.0", Ktwelve.PRIMARY_SCHOOL, getRequestString(REQ_USER_CODE), UserType.TEACHER)) {
            MapMessage resultMap = new MapMessage();
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_TEACHER_KETWELVE);
            return resultMap;
        }

        // 继续脏下去.... 学前老师不允许登录1.5.8以下版本
        if (loginVersionCheck("1.5.8.0", Ktwelve.INFANT, getRequestString(REQ_USER_CODE), UserType.TEACHER)) {
            MapMessage resultMap = new MapMessage();
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_KETWELVE_INFANT);
            return resultMap;
        }

        MapMessage loginResult = internalUserLogin();
        if (!RES_RESULT_SUCCESS.equals(loginResult.get(RES_RESULT))
                || Objects.equals(loginResult.get(RES_NEW_DEVICE), Boolean.TRUE)) {
            // 这里有一个是否为新设备的登录校验
            return loginResult;
        }

        MapMessage resultMap = new MapMessage();
        // 判断返回的用户只有一个
        List retUserList = (List) loginResult.get(RES_USER_LIST);
        if (retUserList != null && retUserList.size() != 1) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_APP_ERROR_MSG);
            return resultMap;
        }
        Map<String, Object> loginUserInfo = (Map<String, Object>) retUserList.get(0);
        Long userId = (Long) loginUserInfo.get(RES_USER_ID);

        resultMap.add(RES_SESSION_KEY, loginUserInfo.get(RES_SESSION_KEY));
        resultMap.add(RES_NEW_DEVICE, loginUserInfo.getOrDefault(RES_NEW_DEVICE, false));
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(userId);
        if (teacherDetail == null) {
            resultMap.clear();
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_TEACHER_KETWELVE);
            return resultMap;
        }

        if (!getTeacherResult(resultMap, teacherDetail, getApiRequestApp())) {
            return resultMap;
        }
        //初中数学/物理/化学/生物老师 初中理科老师APP已暂停使用哦 高中老师APP已暂停使用
        //#60109 改成只能初中英语老师能正常登陆了
        VendorApps app = getApiRequestApp();
        if (teacherDetail.isKLXTeacher() && (app == null || !Objects.equals(app.getAppKey(), "17JuniorTea"))) {
            resultMap.clear();
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_JUNIOR_MATH_TEACHER);
            return resultMap;
        }

        //限制只允许中学老师登陆.
//        if (teacherDetail.getKtwelve() != null && !Ktwelve.JUNIOR_SCHOOL.equals(teacherDetail.getKtwelve())) {
//            resultMap.clear();
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_TEACHER_KETWELVE);
//            return resultMap;
//        }
        this.processTeacherProfile(teacherDetail, resultMap);
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
        TeacherDetail teacherDetail = getCurrentTeacher();
        if (teacherDetail == null) {
            return failMessage("请重新登录");
        }
        this.processTeacherProfile(teacherDetail, resultMap);
        //设置H5的cookie
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacherDetail.getId());
        getWebRequestContext().saveAuthenticationStates(-1, ua.getId(), ua.getPassword(), RoleType.of(teacherDetail.getUserType()));
        if (StringUtils.isBlank(getRequestString("vendor"))) {
            resultMap.put("isVendor", true);// 标识第三方登录：目前接入 懂你
            resultMap.put("vendor", true);// 兼容ios
        } else {
            resultMap.put("vendor", getRequestString("vendor"));
        }
        resultMap.add(RES_SESSION_KEY, getRequestString(RES_SESSION_KEY));
        return resultMap;
    }

    /**
     * 生成老师详细信息结果
     *
     * @param teacherDetail
     * @param resultMap
     */
    private void processTeacherProfile(TeacherDetail teacherDetail, MapMessage resultMap) {


        TeacherDetail curUser = teacherDetail;
        resultMap.add(RES_USER_ID, curUser.getId());
        resultMap.add(RES_USER_TYPE, curUser.getUserType());
        resultMap.add(RES_REAL_NAME, curUser.getProfile().getRealname());
        resultMap.add(RES_AVATAR_URL, getUserAvatarImgUrl(curUser));
        resultMap.add(RES_KTWELVE, teacherDetail.getKtwelve() == null ? "" : teacherDetail.getKtwelve().name());
        resultMap.add(RES_SUBJECT, teacherDetail.getSubject() == null ? "" : teacherDetail.getSubject().name());
        resultMap.add(RES_SUBJECT_NAME, teacherDetail.getSubject() == null ? "" : teacherDetail.getSubject().getValue());

        resultMap.add(RES_AUTH_STATE, curUser.fetchCertificationState() == AuthenticationState.SUCCESS);
        // 获取用户认证信息

        String am = sensitiveUserDataServiceClient.loadUserMobileObscured(curUser.getId());
        if (!StringUtils.isEmpty(am)) {
            resultMap.add(RES_USER_MOBILE, am);
        }
        //生成用户tag列表
        Set<String> tagList = getUserMessageTagList(teacherDetail.getId());
        resultMap.add(RES_JPUSH_TAGS, tagList);

        // 性别
        if (Gender.FEMALE == curUser.fetchGender() || Gender.MALE == curUser.fetchGender()) {
            resultMap.add(RES_USER_GENDER, curUser.fetchGender().getCode());
        }

        // 生日
        if (StringUtils.isNoneBlank(curUser.fetchBirthday())) {
            Integer year = curUser.getProfile().getYear();
            Integer month = curUser.getProfile().getMonth();
            Integer day = curUser.getProfile().getDay();
            if (null == year || null == month || null == day || 0 == year || 0 == month || 0 == day) {
                resultMap.add(RES_USER_BIRTHDAY, "");
            } else {
                String birthday = SafeConverter.toString(year);
                birthday = month < 10 ? birthday + "0" + month : birthday + month;
                birthday = day < 10 ? birthday + "0" + day : birthday + day;
                resultMap.add(RES_USER_BIRTHDAY, birthday);
            }
        }
        // 职务
        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(curUser.getId());
        if (extAttribute != null) {
            if (StringUtils.isNoneBlank(extAttribute.getDuty())) {
                resultMap.add(RES_TEACHER_DUTY, extAttribute.getDuty());
            }
            if (extAttribute.getTeachingYears() != null) {
                resultMap.add(RES_TEACHING_YEARS, extAttribute.fetchTeachingYears().getDesc());
            }
        }

        // 获取用户的SHIPPING ADDRESS
        try {
            MapMessage message = userServiceClient.generateUserShippingAddress(curUser.getId());
            if (message.isSuccess()) {
                String receiverPhone = MapUtils.getString(message, "receiverPhone");

                UserShippingAddress address = (UserShippingAddress) message.get("address");
                addIntoMap(resultMap, RES_SCHOOL_ID, address.getSchoolId() == null ? 0 : address.getSchoolId());
                addIntoMap(resultMap, RES_SCHOOL_NAME, address.getSchoolName());
                addIntoMap(resultMap, RES_PROVINCE_CODE, address.getProvinceCode() == null ? 0 : address.getProvinceCode());
                addIntoMap(resultMap, RES_PROVINCE_NAME, address.getProvinceName());
                addIntoMap(resultMap, RES_CITY_CODE, address.getCityCode() == null ? 0 : address.getCityCode());
                addIntoMap(resultMap, RES_CITY_NAME, address.getCityName());
                addIntoMap(resultMap, RES_COUNTRY_CODE, address.getCountyCode() == null ? 0 : address.getCountyCode());
                addIntoMap(resultMap, RES_COUNTRY_NAME, address.getCountyName());
                addIntoMap(resultMap, RES_DETAIL_ADDRESS, address.getDetailAddress());
                addIntoMap(resultMap, RES_RECEIVER_PHONE, receiverPhone);
                addIntoMap(resultMap, RES_RECEIVER, address.getReceiver());
                addIntoMap(resultMap, RES_POST_CODE, address.getPostCode());
                addIntoMap(resultMap, RES_SHIPPING_TYPE, address.getLogisticType());
            }
        } catch (Exception e) {
            // ignore
        }

        // 班级数量 + 包班制
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(teacherDetail.getTeacherSchoolId()).getUninterruptibly();
        EduSystemType eduSystem = EduSystemType.of(schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly());

        Set<Clazz> teacherClazzs = loadTeacherClazzIncludeMainSub(curUser.getId());
        teacherClazzs = teacherClazzs.stream()
                .filter(p -> p.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && p.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED)
                .filter(p -> p.matchEduSystem(eduSystem))
                .collect(Collectors.toSet());

        addIntoMap(resultMap, RES_RESULT_CLAZZ_COUNT, teacherClazzs.size());

        // 学校所在区域
        if (teacherDetail.getRegionCode() != null) {
            addIntoMap(resultMap, RES_SCHOOL_COUNTRY_CODE, teacherDetail.getRegionCode());
        }

        // 一起作业为您服务
        addIntoMap(resultMap, RES_TEACHER_AGENT, teacherAgentLoaderClient.getSchoolManager(teacherDetail.getTeacherSchoolId()));

        addIntoMap(resultMap, RES_INTEGRAL, teacherDetail.getUserIntegral().getUsable());
        addIntoMap(resultMap, RES_INTEGRAL_UNIT, (teacherDetail.isPrimarySchool() || teacherDetail.isInfantTeacher()) ? "园丁豆" : "学豆");
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        //是否是假老师
        resultMap.add(RES_RESULT_TEACHER_IS_FAKE, teacherLoaderClient.isFakeTeacher(curUser.getId()));
        resultMap.add(RES_JXT_HIT, teacherDetail.isPrimarySchool() && !AppAuditAccounts.isTeacherAuditAccount(teacherDetail.getId()));
        resultMap.add(RES_SUBJECT_LIST, toSubjectList(teacherDetail.getSubjects(), false));
    }


    private Boolean checkPasswordContainsChinese(String password) {
        String regEx = "[\\u4e00-\\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(password);
        return m.find();
    }


    /**
     * 注册老师账号
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/register.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage registerTeacher() {

        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_CODE, "手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequired(REQ_PASSWORD, "密码");
            if (StringUtils.isNoneBlank(getRequestString(REQ_INVITER_ID))) {
                validateRequestNoSessionKey(REQ_USER_CODE, REQ_VERIFY_CODE, REQ_PASSWORD, REQ_INVITER_ID);
            } else
                validateRequestNoSessionKey(REQ_USER_CODE, REQ_VERIFY_CODE, REQ_PASSWORD);
            validateUserRegisterInfo();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        if (checkPasswordContainsChinese(getRequestString(REQ_PASSWORD))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_PASSWORD_INVALID);
            return resultMap;
        }

        String teacherMobile = getRequestString(REQ_USER_CODE);

        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
        neonatalUser.setUserType(UserType.TEACHER);
        neonatalUser.setMobile(teacherMobile);
        neonatalUser.setPassword(getRequestString(RES_PASSWORD));
        neonatalUser.setWebSource(getApiRequestApp().getAppKey());
        String inviterId = getRequestString(REQ_INVITER_ID);
        if (StringUtils.isNotBlank(inviterId))
            neonatalUser.setInviter(inviterId);

        MapMessage message;

        try {
            message = AtomicLockManager.instance().wrapAtomic(userServiceClient)
                    .keyPrefix("TEACHER_REGISTER")
                    .keys(teacherMobile)
                    .proxy()
                    .registerUserAndSendMessage(neonatalUser);
        } catch (CannotAcquireLockException ex) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "正在处理，请不要重复提交");
            return resultMap;
        }

        // MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);

        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
            return resultMap;
        }
        User teacher = (User) message.get("user");

        //绑手机
        MapMessage activeResult = userServiceClient.activateUserMobile(teacher.getId(), teacherMobile);
        if (!activeResult.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_BIND_MOBILE_ERROR_MSG);
            return resultMap;
        }

        // 从缓存中删除验证码
        smsServiceClient.getSmsService()
                .deleteMobileValidationStatus(SmsType.APP_TEACHER_VERIFY_MOBILE_REGISTER_MOBILE.name(), getRequestString(REQ_USER_CODE))
                .awaitUninterruptibly();

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
        addMoreInfo(resultMap, teacher.getId(), ua.getPassword(), RoleType.ROLE_TEACHER);
        return resultMap;
    }


    private void addMoreInfo(MapMessage resultMap, Long userId, String password, RoleType roleType) {
        String sessionKey = attachUser2RequestApp(userId);

//        userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddress(), OperationSourceType.app);
//        userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId,
                getWebRequestContext().getRealRemoteAddress(),
                UserRecordMode.LOGIN,
                OperationSourceType.app,
                false,
                getAppType());

        getWebRequestContext().saveAuthenticationStates(-1, userId, password, roleType);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_ID, userId);
        resultMap.add(RES_SESSION_KEY, sessionKey);
    }


    private void validateUserRegisterInfo() {
        // 判断是否已经注册过
        UserType userType = UserType.TEACHER;
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

        String userCode = getRequestString(REQ_USER_CODE);
        String verifyCode = getRequestString(REQ_VERIFY_CODE);

        // 从缓存中获取验证码
        SmsType smsType = SmsType.APP_TEACHER_VERIFY_MOBILE_REGISTER_MOBILE;
        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(userCode, verifyCode, smsType.name(), false);
        if (!validateResult.isSuccess()) {
            throw new IllegalArgumentException("验证码无效或者已过期!");
        }
    }


    /**
     * 老师通过手机验证码重置密码
     * <p>
     * !!!!!!!!!
     * 这个api客户端木有用@!!!!!!
     * !!!!!!!!!!!!!
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/password/reset_by_code.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage resetPasswordByMobileCode() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_MOBILE, "手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequired(REQ_PASSWORD, "密码");
            validateRequestNoSessionKey(RES_USER_MOBILE, REQ_VERIFY_CODE, RES_PASSWORD);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String userMobile = getRequestString(REQ_USER_MOBILE);
        String code = getRequestString(REQ_VERIFY_CODE);
        String newPassword = getRequestString(REQ_PASSWORD);

        UserAuthentication teacher = userLoaderClient.loadMobileAuthentication(userMobile, UserType.TEACHER);
        if (teacher == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_MOBILE_NOT_EXIST_ERROR_MSG);
            return resultMap;
        }

        MapMessage codeMessage = smsServiceClient.getSmsService().verifyValidateCode(userMobile, code, SmsType.FORGOT_PASSWORD_SEND_VERIFY_CODE.name());
        if (!codeMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
            return resultMap;
        }

        User user = raikouSystem.loadUser(teacher.getId());

        MapMessage message = userServiceClient.setPassword(user, newPassword);
        if (message.isSuccess()) {
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacher.getId());
            userServiceRecord.setOperatorId(teacher.getId().toString());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("修改密码");
            userServiceRecord.setComments("用户通过手机验证码重置密码");
            userServiceRecord.setAdditions("refer:TeacherUserApiControllermobile.resetPasswordByMobileCode");
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            String sessionKey = updateAppSessionKeyForTeacher(user.getId());
            getWebRequestContext().saveAuthenticationStates(-1, user.getId(), newPassword, RoleType.ROLE_TEACHER);
            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_SESSION_KEY, sessionKey);
        } else {
            return failMessage("修改失败");
        }

    }

    /**
     * 修改密码后更新老师App的session key
     *
     * @param tid
     * @author changyuan.liu
     */
    private String updateAppSessionKeyForTeacher(Long tid) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Teacher", tid);
        String newSessionKey = "";
        if (vendorAppsUserRef != null) {
            newSessionKey = SessionUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), tid);
            vendorServiceClient.expireSessionKey("17Teacher", tid, newSessionKey);
        }
        return newSessionKey;
    }

    public TeacherUserApiController() {
    }

    /**
     * 老师完善资料
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/profile/complete.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage completeTeacherProfile() {

        MapMessage resultMap = new MapMessage();


        try {
            validateRequired(REQ_TEACHER_NAME, "教师姓名");
            validateRequired(REQ_SUBJECT, "学科");
            validateRequired(REQ_KTWELVE, "学段");
            validateRequired(REQ_SCHOOL_ID, "学校id");
            validateEnum(REQ_KTWELVE, "学段", Ktwelve.INFANT.name(), Ktwelve.PRIMARY_SCHOOL.name(), Ktwelve.JUNIOR_SCHOOL.name());
            validateRequest(REQ_TEACHER_NAME, REQ_SUBJECT, REQ_KTWELVE, REQ_SCHOOL_ID);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Teacher teacher = getCurrentTeacher();

        String teacherName = getRequestString(REQ_TEACHER_NAME);
        if (badWordCheckerClient.containsUserNameBadWord(teacherName)) {
            return failMessage("输入的姓名信息不合适哦\n有疑问请联系客服：\n400-160-1717");
        }

        Subject subject = Subject.of(getRequestString(REQ_SUBJECT));
        Ktwelve ktwelve = Ktwelve.of(getRequestString(REQ_KTWELVE));

        // 初中数学老师暂不支持APP注册哦(神算子除外)
        // 排除中学APP 17JuniorTea
        String appKey = getRequestString(REQ_APP_KEY);
        if (!(Objects.equals(appKey, UserWebSource.Shensz.getSource()) || Objects.equals(appKey, "17JuniorTea"))) {
            if (ktwelve == Ktwelve.JUNIOR_SCHOOL && subject == Subject.MATH) {
                return failMessage(RES_RESULT_UNSUPPORT_JUNIOR_MATH_TEACHER_REGISTER);
            }
        }

        // 学前只支持英语老师APP注册哦
        if (ktwelve == Ktwelve.INFANT && subject != Subject.ENGLISH) {
            return failMessage(RES_RESULT_UNSUPPORT_INFANT_TEACHER_REGISTER);
        }

        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null)
            return failMessage(RES_RESULT_SCHOOL_INFO_ERROR_MSG);

        if (StringRegexUtils.isNotRealName(teacherName)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_NAME_ERROR);
            return resultMap;
        }

        MapMessage result = teacherServiceClient.setTeacherSubjectSchool(teacher.getId(), subject, ktwelve, schoolId);
        if (!result.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_TEACHER_SET_SCHOOL_FAILED);
            return resultMap;
        }
        // Feature #54929
//        if (ForbidModifyNameAndPortrait.check()) {
//            return ForbidModifyNameAndPortrait.errorMessage;
//        }
        result = userServiceClient.changeName(teacher.getId(), teacherName);
        if (!result.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UPDATE_REALNAME_ERROR_MSG);
            return resultMap;
        }

        LogCollector.info("backend-general", MiscUtils.map("usertoken", teacher.getId(),
                "usertype", teacher.getUserType(),
                "platform", getApiRequestApp().getAppKey(),
                "version", getRequestString(REQ_APP_NATIVE_VERSION),
                "op", "change user name",
                "mod1", teacher.fetchRealname(),
                "mod2", teacherName,
                "mod3", teacher.getAuthenticationState()));

        addMResultWrapper(resultMap, subject, ktwelve, schoolId);
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;

    }

    @RequestMapping(value = "/profile.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getProfile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
        // 检查老师账号状态
        TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherDetail.getId());
        if (teacherExtAttribute != null) {
            if (teacherExtAttribute.isForbidden() || teacherExtAttribute.isFreezing()) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_FORBIDDEN);
                return resultMap;
            }
        }

        VendorApps app = getApiRequestApp();
        // 老师APP 10月1号之后强制弹窗, 中学老师进入小学APP
        if (!juniorAppLimitTime() && app != null && "17Teacher".equals(app.getAppKey())) {
            if (teacherDetail.getKtwelve() != null && teacherDetail.isJuniorTeacher()) {
                resultMap.add(RES_RESULT, RES_RESULT_NEED_RELOGIN_CODE);
                resultMap.add(RES_MESSAGE, "请重新登录");
                return resultMap;
            }
        }

        this.processTeacherProfile(teacherDetail, resultMap);

        if (teacher.isJuniorTeacher()) {
            Boolean integralhHit = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "MSIntegral", "Mall");
            resultMap.add(RES_INTEGRAL_HIT, integralhHit);
            if (integralhHit) {
                resultMap.add(RES_INTEGRAL_URL, fetchMainsiteUrlByCurrentSchema() + integralUrl);
                resultMap.add(RES_INTEGRAL_EXPLAIN_URL, fetchMainsiteUrlByCurrentSchema() + integralNoteUrl);
            }

            Boolean authRewardHit = juniorAuthRewardGrey(teacherDetail);
            resultMap.add(RES_AUTH_REWARD_HIT, authRewardHit);
            if (authRewardHit) {
                resultMap.add(RES_AUTH_URL, juniorAuthRewardUrl);
                resultMap.add(RES_AUTH_REWARD, "30元话费");
            }
        } else if (teacher.isPrimarySchool() || teacher.isInfantTeacher()) {
            resultMap.add(RES_INTEGRAL_HIT, true);
            resultMap.add(RES_INTEGRAL_URL, fetchMainsiteUrlByCurrentSchema() + integralUrl);
            resultMap.add(RES_INTEGRAL_EXPLAIN_URL, fetchMainsiteUrlByCurrentSchema() + integralNoteUrl);

        }

        setPrivilegeInfo(resultMap, teacherDetail, teacherExtAttribute);

        resultMap.add(RES_MODIFY_AVATAR_H5_URL, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/teacher_avatar");//修改头像H5
        resultMap.add(RES_LEVEL_H5_URL, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/activity2018/primary/level_system/index");//等级H5
        addMResultWrapper(resultMap, teacherDetail.getSubject(), teacherDetail.getKtwelve(), teacherDetail.getTeacherSchoolId());
        return resultMap;
    }

    private void setPrivilegeInfo(MapMessage resultMap, TeacherDetail teacherDetail, TeacherExtAttribute teacherExtAttribute) {
        //用户的等级信息
        Map<String, Object> userExp = new HashMap<>();//用户等级的基本信息
        Integer level = 1;
        if (teacherExtAttribute != null && teacherExtAttribute.getNewLevel() != null && teacherExtAttribute.getNewLevel() > 0
                && null != TeacherExtAttribute.NewLevel.getNewLevelByLevel(teacherExtAttribute.getNewLevel())) {
            level = teacherExtAttribute.getNewLevel();
        }
        Integer exp = 0;
        if (null != teacherExtAttribute && teacherExtAttribute.getExp() != null && teacherExtAttribute.getExp() > 0) {
            exp = teacherExtAttribute.getExp();
        }
        Integer expNeed = 0;
        Integer expType = 0;
        TeacherExtAttribute.NewLevel levelEnum = TeacherExtAttribute.NewLevel.getNewLevelByLevel(level);
        if (levelEnum == TeacherExtAttribute.NewLevel.SUPER) {//如果是特技，则已经是顶级，只需要保级，否则就是升级
            expNeed = TeacherExtAttribute.NewLevel.SUPER.getMinExp();
            expType = 2;
        } else {
            expNeed = levelEnum.getUpExp();
            expType = 1;
        }

        //用户的特权信息
        MapMessage mapMessagePrivilege = teacherTaskPrivilegeServiceClient.getPrivilege(teacherDetail.getId());
        if (mapMessagePrivilege.isSuccess()) {
            Map<Long, TeacherTaskPrivilegeTpl> allTplsMap = teacherTaskPrivilegeServiceClient.getAllTplsMap();
            TeacherTaskPrivilege teacherTaskPrivilege = (TeacherTaskPrivilege) mapMessagePrivilege.get("teacherTaskPrivilege");
            List<TeacherTaskPrivilege.Privilege> privileges = teacherTaskPrivilege.getPrivileges();
            List<String> privilegeName = new ArrayList<>();
            for (TeacherTaskPrivilege.Privilege privilege : privileges) {
                TeacherTaskPrivilegeTpl teacherTaskPrivilegeTpl = allTplsMap.get(privilege.getId());
                if (teacherTaskPrivilegeTpl == null) {
                    continue;
                }
                privilegeName.add(teacherTaskPrivilegeTpl.getName() + (teacherTaskPrivilegeTpl.getSubName() == null ? "" : teacherTaskPrivilegeTpl.getSubName()));
            }
            Integer type = (Integer) CacheSystem.CBS.getCache("persistence").get("teacher_new_change_level_app_" + teacherDetail.getId()).getValue();
            userExp.put("type", type);//级别调整类型1升级，-1降级，0保级
            userExp.put("is_pop", type == null ? false : true);
            userExp.put("privilege_name", privilegeName);
        }

        //升级或者积分增加，是否有扫光动画
        Boolean isUpLevel = (Boolean) CacheSystem.CBS.getCache("persistence").get("teacher_app_personal_uplevel_" + teacherDetail.getId()).getValue();
        Boolean expIsAdd = (Boolean) CacheSystem.CBS.getCache("persistence").get("exp_teacher_is_add_" + teacherDetail.getId()).getValue();
        userExp.put("level_id", level);
        userExp.put("level_name", levelEnum.getValue());
        userExp.put("exp_now", exp);
        userExp.put("exp_need", expNeed);
        userExp.put("exp_type", expType);
        userExp.put("is_up_level", isUpLevel == null ? false : isUpLevel);//是否升级了
        userExp.put("exp_is_add", expIsAdd == null ? false : expIsAdd);//积分是否增加了
        resultMap.add("userExp", userExp);
    }

    @RequestMapping(value = "/deleteLevelKey.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deletePrivilegeKey() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Teacher teacher = getCurrentTeacher();
        CacheSystem.CBS.getCache("persistence").delete("teacher_app_personal_uplevel_" + teacher.getId());
        CacheSystem.CBS.getCache("persistence").delete("exp_teacher_is_add_" + teacher.getId());
        CacheSystem.CBS.getCache("persistence").delete("teacher_new_change_level_app_" + teacher.getId());
        return MapMessage.successMessage();
    }

    private final static String integralNoteUrl = "/view/mobile/teacher/integral_note";


    /**
     * 老师通过手机验证码修改手机
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/mobile/change.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage changeMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_USER_MOBILE, "手机号");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequest(REQ_USER_MOBILE, REQ_VERIFY_CODE);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        User teacher = getCurrentTeacher();
        String userMobile = getRequestString(RES_USER_MOBILE);
        String code = getRequestString(REQ_VERIFY_CODE);


        MapMessage mapMessage = verificationService.verifyMobile(
                teacher.getId(), userMobile,
                code,
                SmsType.APP_TEACHER_VERIFY_MOBILE_CENTER.name());

        if (mapMessage.isSuccess()) {
            MapMessage mapMessage1 = userServiceClient.activateUserMobile(teacher.getId(), userMobile);
            if (mapMessage1.isSuccess()) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, mapMessage1.getInfo());
            }

        } else {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, mapMessage.getInfo());
        }

        return resultMap;

//        MobileValidationStatus status = smsCacheSystem.getMobileValidationStatusCache().load(SmsType.TEACHER_VERIFY_MOBILE_CENTER, userMobile);
//        if (null == status || StringUtils.isEmpty(status.getMobile()) || StringUtils.isEmpty(status.getValidationCode())) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
//            return resultMap;
//        }
//
//        if (status.getValidationCode().equals(code)) {
//            // 验证成功, 追踪，记录验证码已经消费掉
//            String trackId = status.getTrackId();
//            if (StringUtils.isNotBlank(trackId)) {
//                smsServiceClient.consumeVerificationCode(trackId);
//            }
//            //验证成功了，清掉缓存
//            smsCacheSystem.getMobileValidationStatusCache().delete(SmsType.TEACHER_VERIFY_MOBILE_CENTER, userMobile);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_VERIFY_CODE_ERROR_MSG);
//            return resultMap;
//        }

        //User user = userLoaderClient.loadUser(teacher.getUserId());

//        MapMessage message = userServiceClient.setPassword(user, newPassword);
//        if (message.isSuccess()) {
//            LogCollector.instance().info("password_change_track",
//                    passwordChangeTrackMap(user.getId(), user.getId(),
//                            "TeacherUserApiController_/v1/teacher/user//password_reset/mobile_code.vpage"));
//            return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        } else {
//            return failMessage("修改失败");
//        }


    }

    /**
     * 通过区id获取学校
     *
     * @return MapMessage
     */
    @RequestMapping(value = "/school/get_by_region_ktwelve.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getSchoolByCounty() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_REGION_PCODE, "区域code");
            validateRequired(REQ_KTWELVE, "学段");
            validateEnum(REQ_KTWELVE, "学段", Ktwelve.INFANT.name(), Ktwelve.PRIMARY_SCHOOL.name(), Ktwelve.JUNIOR_SCHOOL.name());
            validateRequest(REQ_REGION_PCODE, REQ_KTWELVE);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Integer pcode = getRequestInt(REQ_REGION_PCODE);
        Ktwelve ktwelve = Ktwelve.of(getRequestString(REQ_KTWELVE));

        List<School> schoolKtwelveList = loadAreaSchools(Collections.singleton(pcode),
                SchoolLevel.safeParse(ktwelve.getLevel()));
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        if (CollectionUtils.isEmpty(schoolKtwelveList)) {
            resultMap.add(RES_SCHOOL_LIST, new ArrayList<>());
            return resultMap;
        } else {
            if (CollectionUtils.isEmpty(schoolKtwelveList)) {
                resultMap.add(RES_SCHOOL_LIST, new ArrayList<>());
                return resultMap;
            }
            List<Map<String, Object>> resSchoolList = new ArrayList<>(schoolKtwelveList.size());
            for (School school : schoolKtwelveList) {
                if (StringUtils.isEmpty(school.getShortName()))
                    continue;
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(RES_SCHOOL_ID, school.getId());
                map.put(RES_SCHOOL_NAME, school.getShortName());
                map.put(RES_SCHOOL_LEVEL, school.getLevel());
                resSchoolList.add(map);
            }
            resultMap.add(RES_SCHOOL_LIST, resSchoolList);
        }
        return resultMap;
    }

    private List<School> loadAreaSchools(Collection<Integer> regionCodes, SchoolLevel schoolLevel) {
        return raikouSystem.querySchoolLocations(regionCodes)
                .enabled()
                .waitingSuccess()
                .level(schoolLevel)
                .filter(s -> s.getType() != SchoolType.TRAINING.getType() && s.getType() != SchoolType.CONFIDENTIAL.getType())
                .transform()
                .asList();
    }

    /**
     * 组装 中学登录小学, 小学登录中学APP 返回值
     *
     * @param resultMap
     * @param teacherDetail
     * @param apps
     * @return 是否通过校验, 如果没有通过检验则直接返回
     */
    private boolean getTeacherResult(MapMessage resultMap, TeacherDetail teacherDetail, VendorApps apps) {
        if (apps != null) {
            // 中学老师登录小学APP提示
            if ("17JuniorTea".equals(apps.getAppKey())) {
                // 没有选择学段的时候，直接返回
                if (teacherDetail.getKtwelve() == null || teacherDetail.getKtwelve() == Ktwelve.UNKNOWN) {
                    return true;
                }

                if (teacherDetail.isPrimarySchool()) {
                    // 如果小学老师登录中学APP
                    resultMap.add(RES_RESULT, RES_RESULT_BUTTON_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_PRIMARY_TEACHER_MESSAGE);
                    resultMap.add(RES_OK_BUTTON, RES_RESULT_OK_BUTTON_TEXT);
                    resultMap.add(RES_OK_BUTTON_ACTION, RES_RESULT_PRIMARY_TEACHER_BUTTON);
                    resultMap.add(RES_CANCEL_BUTTON, RES_RESULT_CANCEL_BUTTON_TEXT);
                    return false;
                }

                if (VersionUtil.compareVersion(getClientVersion(), "2.3.0") >= 0) {
                    addMResultWrapper(resultMap, teacherDetail.getSubject(), teacherDetail.getKtwelve(), teacherDetail.getTeacherSchoolId());
                } else if (!(teacherDetail.isJuniorTeacher() && (teacherDetail.isEnglishTeacher() || teacherDetail.isMathTeacher())) || teacherDetail.isSeniorTeacher()) {
                    // 若为初中非英语和数学老师or高中老师
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_UNSUPPORT_JUNIOR_MATH_TEACHER);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 中学增加两个字段 M_ENGLISH,   M_MATH, M_O2O_PAPER
     *
     * @param resultMap
     * @param subject
     * @param ktwelve
     * @param teacherSchoolId
     */
    private void addMResultWrapper(MapMessage resultMap, Subject subject, Ktwelve ktwelve, Long teacherSchoolId) {
        if (ktwelve != Ktwelve.JUNIOR_SCHOOL && ktwelve != Ktwelve.SENIOR_SCHOOL) {
            return;
        }

        SchoolExtInfo schoolExtInfo;
        if (teacherSchoolId != null && (schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(teacherSchoolId).getUninterruptibly()) != null) {
            resultMap.add(RES_IS_SCAN_FLAG, schoolExtInfo.isScanMachineFlag());
        } else {
            resultMap.add(RES_IS_SCAN_FLAG, false);
        }

        if (ktwelve == Ktwelve.JUNIOR_SCHOOL && subject == Subject.ENGLISH) { // 初中英语
            resultMap.add(RES_REDIRECT_MODULE, "M_ENGLISH");
        } else if (ktwelve == Ktwelve.JUNIOR_SCHOOL && subject == Subject.MATH) { // 初中数学
            resultMap.add(RES_REDIRECT_MODULE, "M_MATH");
        } else {
            resultMap.add(RES_REDIRECT_MODULE, "M_O2O_PAPER");
        }
        resultMap.add(RES_M_O2O_PAPER_URL, ProductConfig.getKuailexueUrl() + "/m/exam/tasks");
    }
}
