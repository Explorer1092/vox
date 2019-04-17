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

package com.voxlearning.washington.controller.open.v2.parent;

import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.meta.PasswordState;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.cache.UserCache;
import com.voxlearning.utopia.service.vendor.api.constant.ParentChannelCLoginResult;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.helpers.ValidateStudentIdHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwe.liao
 * @since 2016/7/7
 */
@Controller
@Slf4j
@RequestMapping(value = "/v2/parent/")
public class ParentApiV2Controller extends AbstractParentApiController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = VerificationService.class)
    private VerificationService verificationService;
    @Inject private ValidateStudentIdHelper validateStudentIdHelper;

    /**
     * 注册C端家长
     */
    @RequestMapping(value = "/register.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage registerParent() {
        try {
            validateRequired(REQ_CONTACT_MOBILE, "手机号码");
            validateRequestNoSessionKey(REQ_CONTACT_MOBILE, REQ_UUID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        String mobile = getRequestString(REQ_CONTACT_MOBILE);
        String uuid = getRequestString(REQ_UUID);
        RoleType roleType = RoleType.ROLE_PARENT;
        //先判断该手机是否已经绑定家长
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
        if (userAuthentication != null) {
            return failMessage(RES_RESULT_MOBILE_EXIST_MSG);
        }
        MapMessage mapMessage = parentRegisterHelper.registerChannelCParent(mobile, roleType, uuid, "17Parent-c");
        if (!mapMessage.isSuccess()) {
            return failMessage(mapMessage.getInfo());
        }
        User newUser = (User) mapMessage.get("user");
        return doParentLogin(newUser);
    }

    /**
     * 更新家长头像
     */
    @RequestMapping(value = "/uploadavatar.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage uploadParentAvatar() {
        Long userId = getRequestLong(REQ_USER_ID);
        String avatarDat = getRequestString(REQ_AVATAR_DAT);
        try {
            validateRequired(REQ_USER_ID, "用户ID");
            validateRequired(REQ_AVATAR_DAT, "头像");
            validateRequest(REQ_USER_ID, REQ_AVATAR_DAT);
        }  catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        User user = raikouSystem.loadUser(userId);
        //上传
        MapMessage changeResult = updateUserAvatar(user, avatarDat);
        if (!changeResult.isSuccess()) {
            return failMessage(RES_RESULT_UPDATE_AVATAR_ERROR_MSG);
        }
        String fileName = SafeConverter.toString(changeResult.get("row"));
        return successMessage().add(RES_USER_IMG_URL, getUserAvatarImgUrl(fileName));
    }

    /**
     * 更新用户头像。可能是家长、学生
     */
    @RequestMapping(value = "update_user_avatar.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUserAvatar() {
        try {
            validateRequired(REQ_USER_ID, "用户ID");
            validateRequired(REQ_USER_IMG_URL, "头像地址");
            validateRequired(REQ_AVATAR_GFS_ID, "头像ID");
            validateRequest(REQ_USER_ID, REQ_USER_IMG_URL, REQ_AVATAR_GFS_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long userId = getRequestLong(REQ_USER_ID);
        String fileName = getRequestString(REQ_USER_IMG_URL);
        String gfsId = getRequestString(REQ_AVATAR_GFS_ID);
        User user = raikouSystem.loadUser(userId);
        if (user == null) {
            return failMessage(RES_RESULT_LOAD_USER_ERROR);
        }
        MapMessage message = onlyUpdateUserAvatar(user, fileName, gfsId);
        if (!message.isSuccess()) {
            return failMessage(message.getInfo());
        }
        return successMessage();
    }

    //仅针对ParentChannelCLoginResult.CHOOSE_ADD_STUDENT这种情况下使用这个接口
    //sid可能从这俩接口返回回来的。
    ///v1/parent/verifypassword.vpage
    //v1/parent/verify_code_channel_c.vpage
    @RequestMapping(value = "bind_identity_and_login_channel_c.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindIdentityAndLoginForChannelC() {
        String mobile = getRequestString(REQ_CONTACT_MOBILE);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Integer callNameId = getRequestInt(REQ_CALL_NAME);
        try {
            validateRequired(REQ_CONTACT_MOBILE, "手机号");
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_CALL_NAME, "身份信息");
            validateRequestNoSessionKey(REQ_CONTACT_MOBILE, REQ_STUDENT_ID, REQ_CALL_NAME);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        CallName callName = CallName.of(callNameId);
        if (callName == null) {
            return failMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }
        //防止调用这个接口的StudentId被篡改。先校验一次
        String uuid = getRequestString(REQ_UUID);
        MapMessage message = validateStudentIdHelper.validateBindRequestStudentIdWithUUID(uuid, studentId);
        if (!message.isSuccess()) {
            return failMessage("学生ID错误");
        }
        MapMessage validateMapMessage = getWantToBindParentIdentityMobileBySid(studentId, callNameId, "");
        if (!isSuccess(validateMapMessage)) {
            //验证失败。直接返回
            return validateMapMessage;
        }
        boolean hadSameCallNameParent = SafeConverter.toBoolean(validateMapMessage.get("hadSameCallNameParent"));
        String bindingMobile = SafeConverter.toString(validateMapMessage.get(RES_BINDING_MOBILE));
        Long parentId = SafeConverter.toLong(validateMapMessage.get(RES_PARENT_ID));
        if (hadSameCallNameParent) {
            if (StringUtils.isNotBlank(bindingMobile)) {
                //已经有这个身份的家长且手机号不为空。直接返回前端处理
                return validateMapMessage.add(RES_VERIFY_CHANNEL_C_RESULT, ParentChannelCLoginResult.VERIFY_IDENTITY_CHOOSE.getType());
            } else if (parentId > 0) {
                //有这个身份的家长，但家长没有绑定手机好
                MapMessage mapMessage = userServiceClient.activateUserMobile(parentId, mobile);
                if (!mapMessage.isSuccess()) {
                    return failMessage(message.getInfo());
                }
                return doParentLogin(raikouSystem.loadUser(parentId)).add(RES_VERIFY_CHANNEL_C_RESULT, ParentChannelCLoginResult.LOGIN_SUCCESS.getType());
            }
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail.isJuniorStudent()) {
            return failMessage(RES_RESULT_NOT_SUPPORT_JUNIOR_STUDENT_ERROR_MESSAGE);
        }
        //没有请求身份的家长。直接创建一个家长号并绑定身份
        //创建家长号
        User student = raikouSystem.loadUser(studentId);
        MapMessage createMapMessage = createParent(student, mobile);
        Object newUser = createMapMessage.get("newParent");
        if (newUser == null) {
            return failMessage(RES_RESULT_FAILED_CREATE_PARENT);
        }
        User parent = (User) newUser;
        List<StudentParent> studentParents = parentLoaderClient.loadStudentParents(student.getId());
        boolean isKeyParent = studentParents.stream().noneMatch(StudentParent::isKeyParent);
        //绑定创建的家长和学生
        MapMessage mapMessage = parentServiceClient.bindExistingParent(student.getId(), parent.getId(), isKeyParent, callName.name());
        if (!mapMessage.isSuccess()) {
            return failMessage(mapMessage.getInfo());
        }
        return doParentLogin(parent).add(RES_VERIFY_CHANNEL_C_RESULT, ParentChannelCLoginResult.LOGIN_SUCCESS.getType());
    }

    //添加C端孩子之前的验证
    @RequestMapping(value = "validate_mobile_channel_c_student.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage validateParentMobileForChannelCStudent() {
        try {
            validateRequest();
        }  catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long parentId = getCurrentParentId();
        //家长手机号
        String parentMobile = sensitiveUserDataServiceClient.loadUserMobile(parentId);
        if (StringUtils.isBlank(parentMobile)) {
            return failMessage(RES_RESULT_PARENT_ERROR_MSG).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //手机是否绑定了学生
        UserAuthentication studentAuthentication = userLoaderClient.loadMobileAuthentication(parentMobile, UserType.STUDENT);
        if (studentAuthentication == null) {
            return successMessage().add(RES_RESULT_SHOW_EXIST_STUDENT, false).add(RES_CHANNEL_C_STUDENT_INPUT_PHONE, Boolean.FALSE);
        }
        Long studentId = studentAuthentication.getId();
        //已绑定的学生与家长是否有关系
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
        if (CollectionUtils.isNotEmpty(studentParentRefs) && studentParentRefs.stream().anyMatch(p -> parentId.equals(p.getParentId()))) {
            //与当前登录的ParentId有关联
            return successMessage().add(RES_RESULT_SHOW_EXIST_STUDENT, false).add(RES_CHANNEL_C_STUDENT_INPUT_PHONE, Boolean.TRUE);
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null || !studentDetail.isPrimaryStudent()) {
            return successMessage().add(RES_RESULT_SHOW_EXIST_STUDENT, false).add(RES_CHANNEL_C_STUDENT_INPUT_PHONE, Boolean.TRUE);
        }
        validateStudentIdHelper.storeBindStudentIdWithParentId(parentId, studentId);
        return successMessage()
                .add(RES_RESULT_SHOW_EXIST_STUDENT, true)
                .add(RES_STUDENT_ID, studentId)
                .add(RES_STUDENT_NAME, studentDetail.fetchRealname())
                .add(RES_USER_IMG_URL, getUserAvatarImgUrl(studentDetail.fetchImageUrl()))
                .add(RES_CHANNEL_C_STUDENT_INPUT_PHONE, Boolean.TRUE);
    }

    @RequestMapping(value = "register_channel_c_student.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage registerChannelCStudent() {
        Integer clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL_PARENT);
        Long schoolId = getRequestLong(REQ_SCHOOL_ID);
        String schoolName = getRequestString(REQ_SCHOOL_NAME);
        String birthDay = getRequestString(REQ_BIRTHDAY);
        String gender = getRequestString(REQ_USER_GENDER);
        String studentName = getRequestString(REQ_STUDENT_NAME);
        String studentPassword = getRequestString(REQ_PASSWORD);
        String mobile = getRequestString(REQ_CONTACT_MOBILE);
        String code = getRequestString(REQ_VERIFY_CODE);
        String uuid = getRequestString(REQ_UUID);
        Integer regionCode = getRequestInt(REQ_REGION_CODE);
        String imgUrl = getRequestString(REQ_USER_IMG_URL);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String gfsId = getRequestString(REQ_AVATAR_GFS_ID);

        try {
            validateRequired(REQ_CLAZZ_LEVEL_PARENT, "年级");
            validateRequired(REQ_BIRTHDAY, "年龄");
            validateRequired(REQ_USER_GENDER, "性别");
            validateRequired(REQ_STUDENT_NAME, "姓名");
            if (VersionUtil.compareVersion(ver, "1.9.5") > 0) {
                validateRequest(REQ_CLAZZ_LEVEL_PARENT, REQ_BIRTHDAY, REQ_USER_GENDER, REQ_STUDENT_NAME, REQ_CONTACT_MOBILE, REQ_VERIFY_CODE, REQ_USER_IMG_URL, REQ_UUID, REQ_AVATAR_GFS_ID);
            } else {
                validateRequest(REQ_CLAZZ_LEVEL_PARENT, REQ_SCHOOL_ID, REQ_SCHOOL_NAME, REQ_BIRTHDAY, REQ_USER_GENDER, REQ_STUDENT_NAME, REQ_PASSWORD, REQ_CONTACT_MOBILE, REQ_VERIFY_CODE, REQ_REGION_CODE, REQ_UUID);
            }
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }

        Long parentId = getCurrentParentId();
        if (StringUtils.isNotBlank(mobile)) {
            //手机号不为空。说明是填入了新手机号。需要验证验证码
            ///v1/user/register/channel_c_student/verifycode/get.vpage
            MapMessage mapMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.APP_STUDENT_VERIFY_MOBILE_REGISTER_MOBILE.name());
            if (!mapMessage.isSuccess()) {
                return failMessage(mapMessage.getInfo());
            }
        } else {
            //否则直接把家长手机号码拿出来绑定给新创建的学生
            //家长手机号
            String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(parentId);
            if (StringUtils.isBlank(authenticatedMobile)) {
                return failMessage(RES_RESULT_PARENT_ERROR_MSG).setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }
            mobile = authenticatedMobile;
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage();
        }

        // Enhancement #48844 学生注册姓名字数限制
        if (studentName != null && studentName.length() > 10) {
            return failMessage("学生姓名需在10位汉字以内");
        }

        // schoolId!=0时存schoolId。schoolId==0.存schoolName
        if (VersionUtil.compareVersion(ver, "1.9.5") < 0 && schoolId == 0 && StringUtils.isBlank(schoolName)) {
            return failMessage(RES_RESULT_SCHOOL_INFO_ERROR_MSG);
        }
        ChannelCUserAttribute.ClazzCLevel clazzCLevel = ChannelCUserAttribute.ClazzCLevel.parse(clazzLevel);
        if (clazzCLevel == null) {
            return failMessage(RES_RESULT_CLAZZ_LEVEL_ERROR);
        }

        Gender genderEnum = Gender.fromCode(gender);

        NeonatalUser source = new NeonatalUser();
        source.setGender(genderEnum.getCode());
        source.setRealname(StringUtils.cleanXSS(studentName));
        source.setMobile(mobile);
        source.setWebSource(CHANNEL_C_USER_WEB_SOURCE);
        source.setUserType(UserType.STUDENT);

        if (StringUtils.isNotBlank(studentPassword)) {
            source.setPassword(studentPassword);
        } else {
            source.setPassword(RandomGenerator.generateUserPassword().getPassword());
        }
        source.attachPasswordState(PasswordState.AUTO_GEN);

        try {
            //设置生日
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(birthDay));
            source.getExtensionAttributes().put("year", calendar.get(Calendar.YEAR));
            source.getExtensionAttributes().put("month", calendar.get(Calendar.MONTH) + 1);
            source.getExtensionAttributes().put("day", calendar.get(Calendar.DAY_OF_MONTH));

        } catch (Exception e) {
            return failMessage(RES_RESULT_BIRTHDAY_ERROR_MSG);
        }

        if (StringUtils.isNotBlank(imgUrl) && StringUtils.isNotBlank(gfsId)) {
            source.getExtensionAttributes().put("imgUrl", imgUrl);
            source.getExtensionAttributes().put("gfsId", gfsId);
        }

        ChannelCUserAttribute channelCUserAttribute = new ChannelCUserAttribute(null, UserType.STUDENT, schoolId, schoolName, clazzLevel, regionCode);
        channelCUserAttribute.setUuid(uuid);
        try {
            MapMessage mapMessage = AtomicLockManager.getInstance().wrapAtomic(studentServiceClient)
                    .keyPrefix("registerChannelCStudent")
                    .keys(mobile)// use mobile as key
                    .proxy()
                    .registerChannelCStudent(source, channelCUserAttribute);
//          MapMessage mapMessage = studentServiceClient.registerChannelCStudent(user, channelCUserAttribute);
            if (!mapMessage.isSuccess()) {
                return failMessage(RES_RESULT_REGISTER_STUDENT_FAILED_MSG);
            }
            Long studentId = SafeConverter.toLong(mapMessage.get("student_id"));
            validateStudentIdHelper.storeBindStudentIdWithParentId(parentId, studentId);
            return successMessage().add(RES_STUDENT_ID, studentId);
        } catch (CannotAcquireLockException ignored) {
            return failMessage(RES_RESULT_DUPLICATE_DEAL);
        }
    }

    /**
     * 家长更换手机,检查原手机号验证码
     *
     */
    @RequestMapping(value = "/changeMobile/original/check.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCodeChangeMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequest(REQ_VERIFY_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String userMobile = sensitiveUserDataServiceClient.loadUserMobile(currentUserId());
        if (StringUtils.isBlank(userMobile))
            return failMessage("该用户没有绑定手机");

        String verifyCode = getRequestString(REQ_VERIFY_CODE);

        MapMessage mapMessage = verificationService.verifyMobile(
                currentUserId(), userMobile,
                verifyCode,
                SmsType.APP_PARENT_VERIFY_MOBILE_BEFORE_CHANGE_MOBILE.name());

        return convert2NativeMessage(mapMessage);
    }


    /**
     * 更换手机号
     *
     */
    @RequestMapping(value = "/mobile/change.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequired(REQ_MOBILE, "手机号");
            validateRequest(REQ_VERIFY_CODE, REQ_MOBILE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String mobile = getRequestString(REQ_MOBILE);
        String verifyCode = getRequestString(REQ_VERIFY_CODE);

        MapMessage mapMessage = verificationService.verifyMobile(
                currentUserId(), mobile,
                verifyCode, SmsType.APP_PARENT_VERIFY_MOBILE_CENTER.name());
        if (!mapMessage.isSuccess())
            return failMessage(mapMessage.getInfo());

        // 去掉老手机号验证，应该是没必要验证是否给老手机号发送验证码, 中学家长端设计的时候没有给老手机号发验证码 -- chongfeng.qi
        if (!Objects.equals(getRequestString(REQ_APP_KEY), "17JuniorPar")) {
            CacheObject<String> cacheObject = UserCache.getUserCache().get(SmsType.APP_PARENT_VERIFY_MOBILE_BEFORE_CHANGE_MOBILE.name() + getCurrentParentId());
            if (cacheObject == null || StringUtils.isBlank(cacheObject.getValue()))
                return failMessage(RES_RESULT_PARENT_CHANGE_MOBILE_TIMEOUT, RES_RESULT_VERIFY_CODE_TIMEOUT);
        }

        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
        if (ua != null)
            return failMessage(RES_RESULT_MOBILE_EXIST_PARENT_MSG);


        MapMessage mapMessage1 = userServiceClient.activateUserMobile(getCurrentParentId(), mobile);
        return convert2NativeMessage(mapMessage1);

    }


    /**
     * 设置是否允许孩子使用一起作业端支付
     *
     */
    @RequestMapping(value = "/child/pay_limit/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updatePayLimit() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_ALLOW, "是否允许");
            validateRequest(REQ_STUDENT_ID, REQ_ALLOW);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long sid = getRequestLong(REQ_STUDENT_ID);
        Boolean allow = getRequestBool(REQ_ALLOW);

        MapMessage mapMessage = studentServiceClient.upsertStudentExtAttributePayFree(sid, allow, getCurrentParentId());
        return convert2NativeMessage(mapMessage);
    }

    /**
     * 设置是否允许孩子使用课外乐园
     *
     */
    @RequestMapping(value = "/child/fairyland_close/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateFairyLandClose() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_ALLOW, "是否允许"); //传 true 是允许。。。这里跟实际传到接口里的反的
            validateRequest(REQ_STUDENT_ID, REQ_ALLOW);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }


        Long sid = getRequestLong(REQ_STUDENT_ID);
        Boolean allow = getRequestBool(REQ_ALLOW);

        MapMessage mapMessage = studentServiceClient.updateFairylandClosed(sid, !allow, getCurrentParentId()); //传 true 是关闭
        return convert2NativeMessage(mapMessage);
    }

    /**
     * 设置是否允许孩子使用自学产品
     *
     */
    @RequestMapping(value = "/child/vap_close/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateVapClose() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_ALLOW, "是否允许"); //传 true 是允许。。。这里跟实际传到接口里的反的
            validateRequest(REQ_STUDENT_ID, REQ_ALLOW);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }


        Long sid = getRequestLong(REQ_STUDENT_ID);
        Boolean allow = getRequestBool(REQ_ALLOW);

        MapMessage mapMessage = studentServiceClient.updateVapClosed(sid, !allow, getCurrentParentId()); //传 true 是关闭
        return convert2NativeMessage(mapMessage);
    }


    /**
     * 设置使用时间
     *
     */
    @RequestMapping(value = "/child/app_use_time/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateAppUseTime() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_USE_TIME, "使用时长"); //分钟
            validateRequest(REQ_STUDENT_ID, REQ_USE_TIME);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long sid = getRequestLong(REQ_STUDENT_ID);
        Integer useTime = getRequestInt(REQ_USE_TIME);

        MapMessage mapMessage = studentServiceClient.updateAppUseTimeLimit(sid, useTime, getCurrentParentId()); //传 true 是关闭
        return convert2NativeMessage(mapMessage);
    }

    /**
     * 设置使用单次使用时间
     *
     */
    @RequestMapping(value = "/child/app_use_once_time/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateAppUseOnceTime() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_USE_TIME, "使用时长"); //分钟
            validateRequest(REQ_STUDENT_ID, REQ_USE_TIME);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long sid = getRequestLong(REQ_STUDENT_ID);
        Integer useTime = getRequestInt(REQ_USE_TIME);
        MapMessage mapMessage = studentServiceClient.updateAppUseOnceTimeLimit(sid, useTime, getCurrentParentId());
        return convert2NativeMessage(mapMessage);
    }

    /**
     * 设置是否关闭学豆乐园
     *
     */
    @RequestMapping(value = "/child/close_integral_fairyland/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateCloseIntegralFairyland() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_ALLOW, "是否允许"); // true 标识允许。。反着来的
            validateRequest(REQ_STUDENT_ID, REQ_ALLOW);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long sid = getRequestLong(REQ_STUDENT_ID);
        Boolean allow = getRequestBool(REQ_ALLOW);
        MapMessage mapMessage = studentServiceClient.updateCloseIntegralFairyland(sid, !allow, getCurrentParentId());
        return convert2NativeMessage(mapMessage);
    }


    /**
     * 直接给学生绑定自己的手机号
     *
     */
    @RequestMapping(value = "/child/bindparentmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindParentMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String bindChildMobile = sensitiveUserDataServiceClient.loadUserMobile(getCurrentParentId());
        if (StringUtils.isBlank(bindChildMobile))
            return failMessage(RES_RESULT_USER_NOT_BIND_MOBILE_MSG);
        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(bindChildMobile, UserType.STUDENT);
        if (ua != null)
            return failMessage(RES_RESULT_MOBILE_EXIST_STUDENT_MSG);
        String childBindMobile = sensitiveUserDataServiceClient.loadUserMobile(studentId);
        if (StringUtils.isNotBlank(childBindMobile))
            return failMessage(RES_RESULT_STUDENT_MOBILE_BINDED_MSG);
        MapMessage mapMessage = userServiceClient.activateUserMobile(studentId, bindChildMobile);
        return convert2NativeMessage(mapMessage);
    }


    @RequestMapping(value = "/child/bindmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_VERIFY_CODE, "验证码");
            validateRequired(REQ_MOBILE, "手机号");
            validateRequest(REQ_VERIFY_CODE, REQ_MOBILE, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String mobile = getRequestString(REQ_MOBILE);
        String verifyCode = getRequestString(REQ_VERIFY_CODE);
        Long studentId = getRequestLong(REQ_STUDENT_ID);

        MapMessage mapMessage = verificationService.verifyMobile(
                studentId, mobile,
                verifyCode, SmsType.APP_STUDENT_VERIFY_MOBILE_CENTER.name());
        if (!mapMessage.isSuccess())
            return failMessage(mapMessage.getInfo());
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
        if (userAuthentication != null)
            return failMessage(RES_RESULT_MOBILE_EXIST_MSG);

        MapMessage mapMessage1 = userServiceClient.activateUserMobile(studentId, mobile);
        return convert2NativeMessage(mapMessage1);
    }


}