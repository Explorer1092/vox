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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.meta.PasswordState;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.UserAvatar;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementPositionType;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedback;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import com.voxlearning.utopia.service.parent.api.StudyTogetherLoader;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.ChannelCUserAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.AdMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.helpers.ParentRegisterHelper;
import com.voxlearning.washington.helpers.ParentStudentCallNameHelper;
import com.voxlearning.washington.helpers.ValidateStudentIdHelper;
import com.voxlearning.washington.support.WashingtonRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Hailong Yang
 * @version 0.1
 * @since 09/14/2015
 */
@Slf4j
@Controller
@RequestMapping(value = "/parentMobile/parent")
public class MobileParentController extends AbstractMobileParentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private FeedbackServiceClient feedbackServiceClient;
    @Inject
    private UserAdvertisementServiceClient userAdvertisementServiceClient;
    @ImportService(interfaceClass = StudyTogetherLoader.class)
    private StudyTogetherLoader studyTogetherLoader;
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @Inject
    private ParentRegisterHelper parentRegisterHelper;
    @Inject
    private ValidateStudentIdHelper validateStudentIdHelper;
    @Inject
    private ParentStudentCallNameHelper parentStudentCallNameHelper;

    /**
     * 查看首页轮播广告             *
     */
    // TODO 这个路由 只要不叫  advertisement; ad; advert; 跟广告相关的 都可以.  这里保持跟 主站一样 使用 be
    @RequestMapping(value = "be.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage advertisement() {
        Long studentId = getRequestLong("sid");
        try {
            if (studentId <= 0) {
                return MapMessage.errorMessage("无效的参数").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            if (currentParent() == null) {
                return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            List<AdMapper> data = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadAdvertisementData(studentId, Collections.singletonList(AdvertisementPositionType.REWARD_PARENT_HOME_POLL.getType()));
            return MapMessage.successMessage().add("advertisements", data);
        } catch (Exception ex) {
            log.error("find parent advertisements failed.", ex);
            return MapMessage.errorMessage("查询失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    /**
     * 生成二维码
     */
    @RequestMapping(value = "createTwoDimensionCode.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage createTwoDimensionCode() {
        try {
            String userId = getRequestString("uid");
            if (currentUser() == null || StringUtils.isBlank(userId)) {
                return MapMessage.errorMessage("当前未登陆学生账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            Long time = new Date().getTime();
            Map<String, Object> result = new HashMap<>();
            result.put("uid", userId);
            result.put("time", time.toString());
            return MapMessage.successMessage().add("secret", AesUtils.encryptBase64String("1234567890123456", JsonUtils.toJson(result)));
        } catch (Exception ex) {
            log.error("find parent advertisements failed.", ex);
            return MapMessage.errorMessage("获取信息失败").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    //保存用户提交的问题
    @RequestMapping(value = "submitquestion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitQuestion() {
        User parent = currentParent();
        String question = getRequestString("q");
        String questionType = getRequestString("type");

        if (StringUtils.isBlank(question)) {
            return MapMessage.errorMessage("无效的参数～").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        if (question.length() < 5) {
            return MapMessage.errorMessage("反馈内容最少5个字哦～").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        if (question.length() > 250) {
            return MapMessage.errorMessage("最多输入250字符，已经超出最大字数限制了哦～").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        if (parent == null) {
            return MapMessage.errorMessage("请登录家长号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        String cacheKey = CacheKeyGenerator.generateCacheKey(getClass(), "FB", DayRange.current().toString() + "_" + parent.getId());
        Long count = washingtonCacheSystem.CBS.persistence.incr(cacheKey, 1, 1, DateUtils.getCurrentToDayEndSecond());
        if (count > 1) {
            return MapMessage.errorMessage("今日已留言，明日再来吧～").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        try {
            UserFeedback userFeedback = new UserFeedback();
            userFeedback.setUserId(parent.getId());   //所有技术支持公众号的粉丝都绑在这一个学号上
            userFeedback.setUserType(UserType.PARENT.getType());
            userFeedback.setContent(question);
            userFeedback.setFeedbackSubType1(questionType);
            userFeedback.setIp(getWebRequestContext().getRealRemoteAddress());
            userFeedback.setRealName(parent.fetchRealname());
            userFeedback.setFeedbackType("移动家长通问题");
            feedbackServiceClient.getFeedbackService().saveFeedback(userFeedback);
            return MapMessage.successMessage().setInfo("留言成功，感谢您的反馈。");
        } catch (Exception ex) {
            log.error("提交问题失败，[openid:{},question:{},msg:{}]", parent.getId(), question, ex.getMessage(), ex);
            return MapMessage.errorMessage("留言发送失败，请稍后再试。").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
    }

    /**
     * @deprecated 这个接口已经迁移到galaxy-webapp了
     */
    @Deprecated
    @RequestMapping(value = "code.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCode() {
        WashingtonRequestContext context = getWebRequestContext();
        if (RuntimeModeLoader.getInstance().isProduction()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://x.17zuoye.com");
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://x.staging.17zuoye.net");
        } else if (RuntimeModeLoader.getInstance().isTest()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://x.test.17zuoye.net");
        }
        context.getResponse().addHeader("Access-Control-Allow-Methods", "GET");
        context.getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with");
        context.getResponse().addHeader("Access-Control-Max-Age", "1800");
        context.getResponse().addHeader("Access-Control-Allow-Credentials", "true");

        String mobile = getRequestString("mobile");
        if (StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("手机号不能为空");
        }
        //检查次数
        //h5的登录最早是一起学用的。所以简单的次数校验写在一起学里了。
        boolean canSendMobileCode = studyTogetherLoader.canSendMobileCode(mobile);
        if (!canSendMobileCode) {
            return MapMessage.errorMessage("对不起，手机号验证码次数超过限制");
        }
        //发验证码
        MapMessage message = smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.PARENT_VERIFY_MOBILE_YIQIXUE_REGISTER.name(), false);
        if (message.isSuccess()) {
            //累计次数
            studyTogetherServiceClient.incrMobileCodeCount(mobile);
        }
        return message;
    }

    /**
     * @deprecated 这个接口已经迁移到了galaxy-webapp
     */
    @Deprecated
    @RequestMapping(value = "verify_code.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCode() {
        addCrossHeaderForXdomain();

        String mobile = getRequestString("mobile");
        String code = getRequestString("code");
        String userWebSource = getRequestString("user_web_source");
        if (StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("手机号不能为空");
        }
        if (StringUtils.isBlank(code)) {
            return MapMessage.errorMessage("验证码不能为空");
        }

        if (StringUtils.isBlank(userWebSource)) {
            userWebSource = UserWebSource.parent_17xue.getSource();
        }

        MapMessage mapMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.PARENT_VERIFY_MOBILE_YIQIXUE_REGISTER.name());
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        // 已注册。直接登录
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
        if (userAuthentication != null && StringUtils.isNotBlank(userAuthentication.getSensitiveMobile())) {
            User parent = raikouSystem.loadUser(userAuthentication.getId());
            //跟原生登录一样有一堆处理要做
            doExtThingForLogin(parent);
            return MapMessage.successMessage().add("is_new_parent", false);
        }
        String lock = "YIQIXUE_REGISTER_" + mobile;
        try {
            AtomicLockManager.getInstance().acquireLock(lock);
            //注册C端家长
            MapMessage message = parentRegisterHelper.registerChannelCParent(mobile, RoleType.ROLE_PARENT, "", userWebSource);
            if (!message.isSuccess()) {
                return MapMessage.errorMessage("注册家长号失败");
            }
            if (!message.containsKey("user")) {
                return MapMessage.errorMessage("注册家长号失败");
            }
            User parent = (User) message.get("user");
            //跟原生登录一样有一堆处理要做
            doExtThingForLogin(parent);
            return MapMessage.successMessage().add("is_new_parent", true);
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("正在处理中。请稍后");
        } finally {
            AtomicLockManager.getInstance().releaseLock(lock);
        }
    }

    //H5统一接口。注册前验证
    @RequestMapping(value = "register_pre_validate.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage validateBeforeRegisterStudent() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //家长手机号
        String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(parent.getId());
        if (StringUtils.isBlank(authenticatedMobile)) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //手机是否绑定了学生
        UserAuthentication studentAuthentication = userLoaderClient.loadMobileAuthentication(authenticatedMobile, UserType.STUDENT);
        if (studentAuthentication == null || StringUtils.isBlank(studentAuthentication.getSensitiveMobile())) {
            return MapMessage.successMessage().add("show_exist_student", false).add("channel_c_input_phone", Boolean.FALSE);
        }
        Long studentId = studentAuthentication.getId();
        //已绑定的学生与家长是否有关系
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
        if (CollectionUtils.isNotEmpty(studentParentRefs) && studentParentRefs.stream().anyMatch(p -> parent.getId().equals(p.getParentId()))) {
            //与当前登录的ParentId有关联
            return MapMessage.successMessage().add("show_exist_student", false).add("channel_c_input_phone", Boolean.TRUE);
        }
        //是否是小学
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null || !studentDetail.isPrimaryStudent()) {
            return MapMessage.successMessage().add("show_exist_student", false).add("channel_c_input_phone", Boolean.TRUE);
        }
        User user = raikouSystem.loadUser(studentAuthentication.getId());
        validateStudentIdHelper.storeBindStudentIdWithParentId(parent.getId(), studentAuthentication.getId());
        return MapMessage.successMessage()
                .add("show_exist_student", true)
                .add("student_id", studentAuthentication.getId())
                .add("student_name", user == null ? "" : user.fetchRealname())
                .add("img_url", getUserAvatarImgUrl(user))
                .add("channel_c_input_phone", Boolean.TRUE);
    }

    //H5统一接口。新注册一个C端孩子并绑定
    @RequestMapping(value = "register_channel_c_student.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage registerChannelCStudent() {
        Integer clazzLevel = getRequestInt("clazz_level", -1);
        String gender = getRequestString("gender");
        String studentName = getRequestString("student_name");
        String uuid = getRequestString("uuid");
        Integer callNameId = getRequestInt("call_name_id");
        Integer avatarKey = getRequestInt("avatar_key");
        String userWebSource = getRequestString("user_web_source");
        //需要新输入手机号的情况
        String mobile = getRequestString("mobile");
        String code = getRequestString("code");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //验证身份
        CallName callName = CallName.of(callNameId);
        if (callName == null) {
            return MapMessage.errorMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }
        UserAvatar userAvatar = UserAvatar.parsePrimaryStudentKey(avatarKey);
        if (userAvatar == null) {
            return MapMessage.errorMessage("头像选择错误");
        }
        try {
            UserWebSource source = UserWebSource.valueOf(userWebSource);
            userWebSource = source.getSource();
        } catch (Exception e) {
            userWebSource = UserWebSource.parent_17xue.getSource();
        }
        //家长手机号
        String studentMobile;
        if (StringUtils.isNotBlank(mobile)) {
            //家长手机号已绑给其他学生。新注册需要输入手机号
            MapMessage message = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.PARENT_VERIFY_MOBILE_YIQIXUE_REGISTER.name());
            if (!message.isSuccess()) {
                return message;
            }
            studentMobile = mobile;
        } else {
            //家长手机号未注册过学生
            String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(parent.getId());
            if (StringUtils.isBlank(authenticatedMobile)) {
                return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
            }
            studentMobile = authenticatedMobile;
        }
        if (!MobileRule.isMobile(studentMobile)) {
            return MapMessage.errorMessage();
        }
        // Enhancement #48844 学生注册姓名字数限制
        if (StringUtils.isBlank(studentName)) {
            return MapMessage.errorMessage("学生姓名不能为空");
        }
        if (studentName.length() > 10) {
            return MapMessage.errorMessage("学生姓名需在10位汉字以内");
        }

        ChannelCUserAttribute.ClazzCLevel clazzCLevel = ChannelCUserAttribute.ClazzCLevel.parse(clazzLevel);
        if (clazzCLevel == null) {
            return MapMessage.errorMessage("错误的学生年级!");
        }

        Gender genderEnum = Gender.fromCode(gender);
        NeonatalUser source = new NeonatalUser();
        source.setGender(genderEnum.getCode());
        source.setRealname(StringUtils.cleanXSS(studentName));
        source.setMobile(studentMobile);
        source.setWebSource(userWebSource);
        source.setUserType(UserType.STUDENT);
        source.setPassword(RandomGenerator.generateUserPassword().getPassword());
        source.attachPasswordState(PasswordState.AUTO_GEN);
        source.getExtensionAttributes().put("imgUrl", userAvatar.getUrl());

        ChannelCUserAttribute channelCUserAttribute = new ChannelCUserAttribute(null, UserType.STUDENT, null, "", clazzLevel, null);
        channelCUserAttribute.setUuid(uuid);
        try {
            MapMessage mapMessage = AtomicLockManager.getInstance().wrapAtomic(studentServiceClient)
                    .keyPrefix("registerChannelCStudent")
                    .keys(studentMobile)
                    .proxy()
                    .registerChannelCStudent(source, channelCUserAttribute);
            if (!mapMessage.isSuccess()) {
                return MapMessage.errorMessage("注册学生失败");
            }
            Long studentId = SafeConverter.toLong(mapMessage.get("student_id"));
            //绑定身份
            parentServiceClient.bindExistingParent(studentId, parent.getId(), true, callName.name());
            return MapMessage.successMessage().add("student_id", studentId);
        } catch (CannotAcquireLockException ignored) {
            return MapMessage.errorMessage("正在处理中，请稍后");
        }
    }

    @RequestMapping(value = "bind_identity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindIdentity() {
        Long studentId = getRequestLong("sid");
        Integer callNameId = getRequestInt("call_name_id");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //验证身份
        CallName callName = CallName.of(callNameId);
        if (callName == null) {
            return MapMessage.errorMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }
        //验证传入的sid是否是前面的后端接口返回的
        MapMessage mapMessage = validateStudentIdHelper.validateBindRequestStudentIdWithParentId(parent.getId(), studentId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        //验证选择的身份是否合法
        MapMessage message = parentStudentCallNameHelper.validateStudentParentRef(studentId, parent, callName);
        if (!message.isSuccess()) {
            return message;
        }
        boolean isKeyParent = SafeConverter.toBoolean(message.get("keyParent"));
        return parentServiceClient.bindExistingParent(studentId, parent.getId(), isKeyParent, callName.name());
    }

    @RequestMapping(value = "validate_call_name.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage validateCallName() {
        Integer callNameId = getRequestInt("call_name_id");
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage().setErrorCode(RES_RESULT_NEED_RELOGIN_CODE);
        }
        //验证身份
        CallName callName = CallName.of(callNameId);
        if (callName == null) {
            return MapMessage.errorMessage(RES_RESULT_CALLNAME_ERROR_MSG);
        }
        return parentStudentCallNameHelper.validateParentWithCallName(parent.getId(), callName);
    }

    @RequestMapping(value = "validate_student_mobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage validateVerifyCode() {
        String mobile = getRequestString("mobile");
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
        return userAuthentication == null || StringUtils.isBlank(userAuthentication.getSensitiveMobile()) ? MapMessage.successMessage() : MapMessage.errorMessage(RES_RESULT_MOBILE_EXIST_STUDENT_MSG);
    }
}
