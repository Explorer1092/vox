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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.MentorCategory;
import com.voxlearning.utopia.api.constant.MentorType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.WechatUserCampaignServiceClient;
import com.voxlearning.utopia.service.certification.client.TeacherCertificationServiceClient;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.TeacherAgentLoaderClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegralHistoryPagination;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.*;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatCodeServiceClient;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;

/**
 * 微信老师相关
 * Created by Shuai Huan on 2014/11/19.
 */
@Controller
@RequestMapping(value = "/open/wechat/teacher")
@Slf4j
public class WechatTeacherController extends AbstractOpenController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncFootprintServiceClient asyncFootprintServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private TeacherCertificationServiceClient teacherCertificationServiceClient;

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private TeacherAgentLoaderClient teacherAgentLoaderClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private FlowerServiceClient flowerServiceClient;
    @Inject private WechatCodeServiceClient wechatCodeServiceClient;
    @Inject private WechatUserCampaignServiceClient wechatUserCampaignServiceClient;
    @Inject private UserServiceClient userServiceClient;


    //记录user record
    // 本身的user record记录是放在拦截器中的，但是现在微信传过来的uid可能并不是使用本人的
    // 这里用一个特别的接口专门记录用
    @RequestMapping(value = "recorduser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext recordUser(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long userId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        if (userId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid user id");
        } else {
            // record
//            long count = asyncFootprintServiceClient.getAsyncFootprintService()
//                    .increaseCookieLoginCount(userId)
//                    .getUninterruptibly();
//            if (count == 1) {
//                // the first time cookie authentication within today
//                String remoteIp = SafeConverter.toString(openAuthContext.getParams().get("remoteIp"), "");
//                userServiceClient.createUserRecord(userId, remoteIp, OperationSourceType.wechat);
//            }
            String remoteIp = SafeConverter.toString(openAuthContext.getParams().get("remoteIp"), "");
            asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId,
                    remoteIp,
                    UserRecordMode.VALIDATE,
                    OperationSourceType.wechat,
                    true,
                    getAppSystemType().name());
            openAuthContext.setCode("200");
        }
        return openAuthContext;
    }

    //将老师与微信帐号绑定
    @RequestMapping(value = "/bindteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext bindUser(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String openId = SafeConverter.toString(openAuthContext.getParams().get("openId"));
        long userId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        String source = SafeConverter.toString(openAuthContext.getParams().get("s"));
        if (null == openId || Long.MIN_VALUE == userId || StringUtils.isBlank(source)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的openId或userId");
            return openAuthContext;
        }
        try {
            CampaignType campaignType = wechatUserCampaignServiceClient.getWechatUserCampaignService().loadUserCampaign(userId).get();
            if (null != campaignType) {
                source += "_" + campaignType;
            }

            Teacher teacher = teacherLoaderClient.loadTeacher(userId);
            if (teacher == null) {
                openAuthContext.setCode("400");
                openAuthContext.setError("老师不存在！");
                return openAuthContext;
            }

            User user = wechatLoaderClient.loadWechatUser(openId);
            if (null != user) { //如果openId能查到绑定用户,则不能进入绑定流程
                openAuthContext.setCode("400");
                openAuthContext.setError("请不要重复绑定");
                return openAuthContext;
            }

            boolean haveBindBefore = wechatLoaderClient.haveBindBefore(userId, WechatType.TEACHER.getType());
            MapMessage message = wechatServiceClient.bindUserAndWechat(userId, openId, source, WechatType.TEACHER.getType(), null);
            if (!message.isSuccess() || null == message.get("id")) {
                openAuthContext.setCode("400");
                openAuthContext.setError("绑定失败");
                return openAuthContext;
            }
            Long id = (Long) message.get("id");

            if (!haveBindBefore && teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
                //如果老师第一次绑定，奖励老师当月因为检查作业获取的智慧教室学豆，如果没有则送100智慧教室学豆
                //必须认证老师才能获取！！

                List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(userId).stream()
                        .filter(Clazz::isPublicClazz)
                        .filter(e -> !e.isTerminalClazz())
                        .collect(Collectors.toList());
                for (Clazz clazz : clazzList) {
                    String couchBaseKey = MemcachedKeyConstants.CHECK_HOMEWORK_ADD_SMART_CLASS_INTEGRAL + "_" + userId + "_" + clazz.getId();
                    CacheObject<String> cacheObject = washingtonCacheSystem.CBS.persistence.get(couchBaseKey);
                    int integral = 0;
                    if (cacheObject != null) {
                        String response = StringUtils.trim(cacheObject.getValue());
                        if (StringUtils.isNotEmpty(response)) {
                            integral += NumberUtils.toInt(response);
                        }
                    }
                    if (integral > 0) {
                        GroupMapper group = deprecatedGroupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazz.getId(), false);
                        if (group != null) {
                            ClazzIntegralHistory history = new ClazzIntegralHistory();
                            history.setGroupId(group.getId());
                            history.setClazzIntegralType(ClazzIntegralType.老师首次绑定微信奖励.getType());
                            history.setIntegral(integral);
                            history.setComment(ClazzIntegralType.老师首次绑定微信奖励.getDescription());
                            history.setAddIntegralUserId(teacher.getId());
                            MapMessage mapMessage = clazzIntegralServiceClient.getClazzIntegralService()
                                    .changeClazzIntegral(history)
                                    .getUninterruptibly();
                            if (!mapMessage.isSuccess()) {
                                log.warn("teacher bind wechat add clazz integral fail, group {}", group.getId());
                            }
                        } else {
                            log.error("teacher group is null.teacherId:{},clazzId:{}", teacher.getId(), clazz.getId());
                        }
                    }
                    //需要把couchbase里预存的学豆清掉
                    washingtonCacheSystem.CBS.persistence.delete(couchBaseKey);
                }
            }

            openAuthContext.setCode("200");
            openAuthContext.add("rid", id);
        } catch (Exception ex) {
            log.error("bind user and wechat failed,[userId:{},openId:{},msg:{}]", userId, openId, ex.getMessage(), ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("绑定失败");
        }
        return openAuthContext;
    }

    //发送手机注册老师号验证码
    @RequestMapping(value = "/sendregisterteacherverifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendVerifyCode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String mobile = ConversionUtils.toString(openAuthContext.getParams().get("mobile"));
        String contextId = ConversionUtils.toString(openAuthContext.getParams().get("cid"));
        boolean voice = ConversionUtils.toBool(openAuthContext.getParams().get("voice"));
        try {
            if (StringUtils.isBlank(contextId)) {
                openAuthContext.setCode("400");
                openAuthContext.setError("invalid data");
                return openAuthContext;
            }
            if (!MobileRule.isMobile(mobile)) {
                openAuthContext.setCode("400");
                openAuthContext.setError("请输入正确的手机号");
                return openAuthContext;
            }
            if (!verifyContext(contextId)) {
                openAuthContext.setCode("400");
                openAuthContext.setError("刷接口，作弊");
                return openAuthContext;
            }
            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                openAuthContext.setCode("400");
                openAuthContext.setError("该手机号码已经注册，请直接登录");
                return openAuthContext;
            }

            MapMessage mapMessage = getSmsServiceHelper().sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.TEACHER_VERIFY_MOBILE_WEIXIN_REGISTER,
                    UserType.TEACHER,
                    voice
            );
            if (mapMessage.isSuccess()) {
                openAuthContext.setCode("200");
            } else {
                openAuthContext.setCode("400");
                openAuthContext.setError(mapMessage.getInfo());
            }
        } catch (Exception ex) {
            log.error("send verify code failed", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("发送验证码失败，请稍后再试");
        }
        return openAuthContext;
    }

    //发送忘记密码验证码
    @RequestMapping(value = "/sendforgetpasswordverifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendForgetPasswordVerifyCode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String mobile = SafeConverter.toString(openAuthContext.getParams().get("mobile"));
        String contextId = SafeConverter.toString(openAuthContext.getParams().get("cid"));
        try {
            if (StringUtils.isBlank(contextId)) {
                openAuthContext.setCode("400");
                openAuthContext.setError("invalid data");
                return openAuthContext;
            }
            if (!MobileRule.isMobile(mobile)) {
                openAuthContext.setCode("400");
                openAuthContext.setError("请输入正确的手机号");
                return openAuthContext;
            }
            if (!verifyContext(contextId)) {
                openAuthContext.setCode("400");
                openAuthContext.setError("您的操作存在安全风险，请刷新页面重试");
                return openAuthContext;
            }
            UserAuthentication teacher = userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER);
            if (teacher == null) {
                openAuthContext.setCode("400");
                openAuthContext.setError("您输入的手机号不存在，请重新输入");
                return openAuthContext;
            }

            smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.FORGOT_PWD.name(), false);

            openAuthContext.setCode("200");
        } catch (Exception ex) {
            log.error("send verify code failed", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("发送验证码失败，请稍后再试");
        }
        return openAuthContext;
    }

//    private static String getForgotPasswordVerifyCodeMemKey(UserType userType, String userCode) {
//        return SmsType.FORGOT_PASSWORD_SEND_VERIFY_CODE + "_" + userType.getType() + "_" + userCode;
//    }

    private boolean verifyContext(String contextId) {
        String key = "VrfCtxWx_" + contextId;
        String ctxIp = washingtonCacheSystem.CBS.unflushable.load(key);
        boolean valid = !StringUtils.isEmpty(ctxIp);
        if (valid) {
            washingtonCacheSystem.CBS.unflushable.delete(key);
        }
        return valid;
    }


    //忘记密码  验证码验证
    @RequestMapping(value = "verifyforgetpasswordcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext verifyForgetPasswordCode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String mobile = SafeConverter.toString(openAuthContext.getParams().get("mobile"));
        String code = SafeConverter.toString(openAuthContext.getParams().get("code"));                        //验证码
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("信息不全");
            return openAuthContext;
        }
        if (!MobileRule.isMobile(mobile)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("手机号不正确");
            return openAuthContext;
        }

        MapMessage message = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.FORGOT_PWD.name());
        if (!message.isSuccess()) {
            openAuthContext.setCode("400");
            openAuthContext.setError(message.getInfo());
            return openAuthContext;
        }

        openAuthContext.setCode("200");
        return openAuthContext;
    }

    //忘记密码  重置密码
    @RequestMapping(value = "resetpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext resetPassword(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String mobile = SafeConverter.toString(openAuthContext.getParams().get("mobile"));
        String code = SafeConverter.toString(openAuthContext.getParams().get("code"));
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("信息不全");
            return openAuthContext;
        }
        if (!MobileRule.isMobile(mobile)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("手机号不正确");
            return openAuthContext;
        }

        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER);
        User user = ua == null ? null : raikouSystem.loadUser(ua.getId());
        if (user == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("用户不存在，请重试");
            return openAuthContext;
        }

        if (userServiceClient.setPassword(user, code).isSuccess()) {
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(user.getId());
            userServiceRecord.setOperatorId(user.getId().toString());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("修改密码");
            userServiceRecord.setComments(FindPasswordMethod.MOBILE.getDescription() + ",操作端：wechat");
            userServiceRecord.setAdditions("refer:WechatTeacherController.resetPassword");

            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }

        openAuthContext.setCode("200");
        return openAuthContext;
    }

    private MapMessage verifyCode(String mobile, String code) {
        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code)) {
            return MapMessage.errorMessage("信息不全");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号不正确");
        }

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.TEACHER_VERIFY_MOBILE_WEIXIN_REGISTER.name());
        if (!validateResult.isSuccess()) {
            return validateResult;
        }

        return MapMessage.successMessage().add("mobile", mobile);
    }

    /**
     * 注册第一步
     * 注册填写手机号，验证码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "verifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext verifyCode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String mobile = SafeConverter.toString(openAuthContext.getParams().get("mobile"));
        String code = SafeConverter.toString(openAuthContext.getParams().get("code"));                        //验证码
        MapMessage mapMessage = verifyCode(mobile, code);
        if (mapMessage.isSuccess()) {
            openAuthContext.setCode("200");
            openAuthContext.add("mobile", mobile);
        } else {
            openAuthContext.setCode("400");
            openAuthContext.setError(mapMessage.getInfo());
        }
        return openAuthContext;
    }

    /**
     * 注册第二步
     * 真实姓名，密码
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "msignup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext processMobileSignupForm(HttpServletRequest request) {

        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String mobile = SafeConverter.toString(openAuthContext.getParams().get("mobile"));
        String code = SafeConverter.toString(openAuthContext.getParams().get("code"));

        String realName = SafeConverter.toString(openAuthContext.getParams().get("realname"));
        if (StringUtils.isBlank(realName)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("信息不全");
            return openAuthContext;
        }
        if (badWordCheckerClient.containsUserNameBadWord(realName)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("输入的姓名信息不合适哦<br/>有疑问请联系客服：<br/>400-160-1717");
            return openAuthContext;
        }

        // verify mobile again before register a new user
        MapMessage mapMessage = verifyCode(mobile, code);
        if (!mapMessage.isSuccess()) {
            openAuthContext.setCode("400");
            openAuthContext.setError(mapMessage.getInfo());
            return openAuthContext;
        }

        String password = SafeConverter.toString(openAuthContext.getParams().get("password"));
        String inviter = SafeConverter.toString(openAuthContext.getParams().get("inviter"));                  //邀请者id（将来会有手机号？）
        String invitationType = SafeConverter.toString(openAuthContext.getParams().get("invitationtype"));    //邀请类型
        String contextId = SafeConverter.toString(openAuthContext.getParams().get("cid"));
        String webSource = SafeConverter.toString(openAuthContext.getParams().get("websource"));
        String info = ConversionUtils.toString(openAuthContext.getParams().get("info")); // 平台短信链接邀请的邀请人信息

        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(realName) || StringUtils.isBlank(password)
                || StringUtils.isBlank(contextId)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("信息不全");
            return openAuthContext;
        }
        if (!MobileRule.isMobile(mobile)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("手机号不正确");
            return openAuthContext;
        }

        if (!verifyContext(contextId)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("刷接口，作弊");
            return openAuthContext;
        }
        // 创建用户
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
        neonatalUser.setUserType(UserType.TEACHER);
        neonatalUser.setMobile(mobile);
        neonatalUser.setPassword(password);
        neonatalUser.setRealname(realName);
        if (StringUtils.isBlank(webSource)) {
            neonatalUser.setWebSource(UserWebSource.wechat.getSource());
        } else {
            neonatalUser.setWebSource(webSource);
        }
        InvitationType type = InvitationType.safeParse(invitationType);
        if (StringUtils.isNotEmpty(inviter) && InvitationType.UNKNOWN != type) {
            neonatalUser.setInviter(inviter);
            neonatalUser.setInvitationType(type);
        }

        if (StringUtils.isNotBlank(info)) {
            String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
            if (defaultDesKey == null) {
                throw new ConfigurationException("No 'default_des_key' configured");
            }
            try {
                String decryptCode = DesUtils.decryptHexString(defaultDesKey, info);
                Map<String, Object> inviterMap = JsonUtils.fromJson(decryptCode);
                User invit = raikouSystem.loadUser(ConversionUtils.toLong(inviterMap.get("userId")));
                if (invit != null) {
                    if (invit.fetchUserType() == UserType.TEACHER) {
                        neonatalUser.setInviter(invit.getId().toString());
                        neonatalUser.setInvitationType(InvitationType.TEACHER_INVITE_TEACHER_LINK);
                    }
                }
            } catch (Exception ex) {
                logger.error("sms invite teacher decryptkey error, the param info is {}, error is {}", info, ex.getMessage());
            }
        }

        try {
            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
            if (!message.isSuccess()) {
                openAuthContext.setCode("400");
                openAuthContext.setError(JsonUtils.toJson(message.getAttributes()));
                return openAuthContext;
            }
            User user = (User) message.get("user");
            Long userId = user.getId();
            //注册的时候同步shippingaddress 电话号码信息
            MapMessage msg = userServiceClient.activateUserMobile(userId, mobile, true);
            if (!msg.isSuccess()) {
                openAuthContext.setCode("400");
                openAuthContext.setError("绑定用户手机失败");
                return openAuthContext;
            }

            try {
                // 注册成功后，立刻记录用户的登录记录
//                userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddress(), OperationSourceType.wechat);
//                userServiceClient.createUserRecordDaily(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId,
                        getWebRequestContext().getRealRemoteAddress(),
                        UserRecordMode.LOGIN,
                        OperationSourceType.wechat,
                        false,
                        getAppSystemType().name());
            } catch (Exception e) {
                logger.error("after register, record user login failed: " + e.getMessage(), e);
            }


            openAuthContext.setCode("200");
            openAuthContext.add("userId", userId);
            return openAuthContext;
        } catch (Exception ex) {
            logger.error("创建用户失败!realName:{},mobile:{}", realName, mobile, ex.getMessage(), ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("创建用户失败");
            return openAuthContext;
        }
    }


    /**
     * 根据regionCode和query搜索匹配学校
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "searchschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext searchSchool(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String regionCodesStr = SafeConverter.toString(openAuthContext.getParams().get("regionCodes"), "");
        String[] regionCodeArr = regionCodesStr.split(",");
        List<Integer> regionCodes = new ArrayList<>();
        for (String codeStr : regionCodeArr) {
            regionCodes.add(SafeConverter.toInt(codeStr));
        }
        String query = SafeConverter.toString(openAuthContext.getParams().get("query"));
        if (CollectionUtils.isEmpty(regionCodes)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }

        try {
            List<School> schools = searchSchool(regionCodes, query, 20);
            openAuthContext.setCode("200");
            openAuthContext.add("schools", schools);
        } catch (Exception ex) {
            logger.error("搜索学校失败!regionCode:{},query:{}", regionCodesStr, query, ex.getMessage(), ex);
            openAuthContext.setCode("200");
            openAuthContext.setError("搜索学校失败");
        }
        return openAuthContext;
    }

    /**
     * 注册第二步
     * 老师注册选择学校学科
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "selectschoolsubject.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext selectSchool(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        long schoolId = SafeConverter.toLong(openAuthContext.getParams().get("schoolId"), Long.MIN_VALUE);
        String subjectText = SafeConverter.toString(openAuthContext.getParams().get("subject"));

        if (teacherId == Long.MIN_VALUE || schoolId == Long.MIN_VALUE || !Subject.isValidSubject(subjectText)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }

        // 禁止老师通过返回的方式重新选择学校,所以在老师已有学校的情况下,报错
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (school != null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("已选学校");
            return openAuthContext;
        }

        Subject subject = Subject.valueOf(subjectText);

        User teacher = raikouSystem.loadUser(teacherId);
        // FIXME hardcode primary school here
        MapMessage message = teacherServiceClient.setTeacherSubjectSchool(teacher, subject, Ktwelve.PRIMARY_SCHOOL, schoolId);
        if (!message.isSuccess()) {
            logger.error("绑定学校失败!teacherId:{},schoolId:{}", teacherId, schoolId);
            openAuthContext.setCode("400");
            openAuthContext.setError("绑定学校失败");
            return openAuthContext;
        }

        String mobile = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "wechat/selectschoolsubject", SafeConverter.toString(teacher.getId()));

        // #10688 老师注册成功发下一步指导短信
        userSmsServiceClient.buildSms().to(teacher)
                .content("家长你好，我是" + subject.getValue() + "老师" + teacher.getProfile().getRealname()
                        + "，请帮孩子在www.17zuoye.com 注册并填写我的手机号" + mobile + "做作业")
                .type(SmsType.TEACHER_GUIDE_AFTER_REG)
                .send();

        userSmsServiceClient.buildSms().to(teacher)
                .content("注册成功！用“手机号+密码”即可登录一起作业网！您可将收到的上一条短信直接转发给家长，引导家长帮助学生使用！")
                .type(SmsType.TEACHER_GUIDE_AFTER_REG)
                .send();

        openAuthContext.setCode("200");
        openAuthContext.add("schoolId", schoolId);
        return openAuthContext;
    }

    // 统计实际教学班级数量
    @RequestMapping(value = "recordactualteachclazzcount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext recordActualTeachClazzCount(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        int clazzCount = SafeConverter.toInt(openAuthContext.getParams().get("cc"));
        long uid = SafeConverter.toLong(openAuthContext.getParams().get("uid"));
//        if (clazzCount != 0) {
//            teacherServiceClient.setTeacherUGCTeachClazzCount(uid, clazzCount);
//        }
        openAuthContext.setCode("200");
        return openAuthContext;
    }


    // 获取可加入班级列表
    @RequestMapping(value = "coraclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext createClazz_addClazz(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String clazzLevel = ConversionUtils.toString(openAuthContext.getParams().get("clazzLevel"));
        String clazzNameStr = ConversionUtils.toString(openAuthContext.getParams().get("clazzName"));
        Long uid = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
        String[] clazzNames = StringUtils.split(clazzNameStr, ",");
        if (StringUtils.isBlank(clazzLevel) || clazzNames.length == 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("参数错误");
            return openAuthContext;
        }
        // 判断是否有重名班级，如果没有就执行创建班级流程，否则执行加入班级流程
        MapMessage message = teacherClazzServiceClient.findClazzWithSameNameForWechat(teacherLoaderClient.loadTeacher(uid), clazzLevel, clazzNames);
        if (!message.isSuccess()) {
            openAuthContext.setCode("400");
            openAuthContext.setError(message.getInfo());
            return openAuthContext;
        }
        openAuthContext.setCode("200");
        openAuthContext.add("addList", message.get("addList"));
        return openAuthContext;
    }

//    //加入班级
//    @RequestMapping(value = "addinclazz.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public OpenAuthContext addInClazz(HttpServletRequest request) {
//        OpenAuthContext openAuthContext = getOpenAuthContext(request);
//        Long clazzId = ConversionUtils.toLong(openAuthContext.getParams().get("clazzId"));
//        Long uid = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
//        OperationSourceType sourceType = OperationSourceType.ofWithUnknown(ConversionUtils.
//                toString(openAuthContext.getParams().get("sourceType"), OperationSourceType.wechat.name()));
//        Teacher teacher = teacherLoaderClient.loadTeacher(uid);
//        if (teacher == null) {
//            openAuthContext.setCode("400");
//            openAuthContext.setError("参数错误");
//            return openAuthContext;
//        }
//        try {
//            MapMessage message = teacherAlterationServiceClient.addInClazzForWechat(teacher.getId(), clazzId, sourceType);
//
//            if (!message.isSuccess()) {
//                openAuthContext.setCode("400");
//                openAuthContext.setError(message.getInfo());
//                return openAuthContext;
//            }
//            openAuthContext.setCode("200");
//            return openAuthContext;
//        } catch (Exception ex) {
//            if (ex instanceof DuplicatedOperationException) {
//                openAuthContext.setCode("400");
//                openAuthContext.setError("正在处理，请不要重复提交");
//                return openAuthContext;
//            }
//            log.error("ERROR OCCURS WHEN ADD IN CLAZZ WECHAT. TEACHER: {}, CLAZZID: {}", teacher.getId(), clazzId, ex);
//            openAuthContext.setCode("400");
//            openAuthContext.setError("加入班级失败");
//            return openAuthContext;
//        }
//    }

//    @RequestMapping(value = "createclazz.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public OpenAuthContext createClazz(HttpServletRequest request) {
//        OpenAuthContext openAuthContext = getOpenAuthContext(request);
//        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
//        long schoolId = SafeConverter.toLong(openAuthContext.getParams().get("schoolId"), Long.MIN_VALUE);
//        String classLevel = SafeConverter.toString(openAuthContext.getParams().get("classLevel"));
//        String eduSystem = SafeConverter.toString(openAuthContext.getParams().get("eduSystem"));
//        String clazzNames = SafeConverter.toString(openAuthContext.getParams().get("clazzNames"));
//        // 默认1人班级 佳平需求
//        int clazzSize = SafeConverter.toInt(openAuthContext.getParams().get("clazzSize"), 1);
//
//
//        if (teacherId == Long.MIN_VALUE || schoolId == Long.MIN_VALUE
//                || StringUtils.isEmpty(clazzNames) || StringUtils.isEmpty(classLevel) || StringUtils.isEmpty(eduSystem)) {
//            openAuthContext.setCode("400");
//            openAuthContext.setError("invalid parameters");
//            return openAuthContext;
//        }
//
//        try {
//            String[] clazzNameArray = StringUtils.split(clazzNames, ",");
//            List<ClassMapper> mapperList = new ArrayList<>();
//            for (String name : clazzNameArray) {
//                ClassMapper mapper = new ClassMapper();
//                mapper.setClassLevel(classLevel);
//                mapper.setSchoolId(schoolId);
//                mapper.setClazzName(name);
//                mapper.setEduSystem(eduSystem);
//                mapper.setAddStudentType("common");
//                mapper.setClassSize(clazzSize);
//                mapperList.add(mapper);
//            }
//            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//
//            MapMessage message = clazzServiceClient.createPublicClazzs(teacher, mapperList);
//            if (!message.isSuccess()) {
//                openAuthContext.setCode("400");
//                openAuthContext.setError("创建班级失败");
//                return openAuthContext;
//            }
//
//            Collection neonatals = (Collection) message.get("neonatals");
//            NeonatalClazz neonatal = (NeonatalClazz) MiscUtils.firstElement(neonatals);
//            if (neonatal != null && neonatal.isSuccessful()) {
//
//                List<Long> clazzIds = new ArrayList<>();
//                for (Object neonatalClazz : neonatals) {
//                    NeonatalClazz clazz = (NeonatalClazz) neonatalClazz;
//                    clazzIds.add(clazz.getClazzId());
//                }
//
//                // 初始化创建的新班级所需要的课本
//                ExRegion region = userLoaderClient.loadUserRegion(teacher);
//                Long bookId = contentLoaderClient.getExtension().initializeClazzBook(
//                        teacher.getSubject(),
//                        ConversionUtils.toInt(classLevel),
//                        region.getCode(),
//                        regionServiceClient);
//                if (bookId != null) {
//                    ChangeBookMapper cmb = new ChangeBookMapper();
//                    cmb.setBooks(String.valueOf(bookId));
//                    cmb.setClazzs(StringUtils.join(clazzIds, ","));
//                    cmb.setType(0); // 0表示添加/修改课本
//                    try {
//                        contentServiceClient.setClazzBook(teacher, cmb);
//                    } catch (Exception ignored) {
//                        logger.warn("Failed to set book for neonatal clazz {}", neonatal.getClazzId(), ignored);
//                    }
//                }
//
//                //帮助学生登录班级wechat notice
//                Map<String, Object> extension = new HashMap<>();
//                wechatServiceClient.processWechatNotice(
//                        WechatNoticeProcessorType.TeacherRemindStudentLoginNotice,
//                        teacherId,
//                        extension,
//                        WechatType.TEACHER);
//
//                //体验布置学生最爱的作业 wechat notice
//                extension.put("subject", teacher.getSubject().getValue());
//                wechatServiceClient.processWechatNotice(
//                        WechatNoticeProcessorType.TeacherAssignHomeworkExperience,
//                        teacherId,
//                        extension,
//                        WechatType.TEACHER);
//
//
//                openAuthContext.setCode("200");
//                openAuthContext.add("clazzIds", clazzIds);
//                return openAuthContext;
//            }
//
//            openAuthContext.setCode("400");
//            openAuthContext.setError("创建班级失败");
//            return openAuthContext;
//        } catch (Exception ex) {
//            logger.error("创建班级失败!teacherId:{},schoolId:{},clazzName:{}", teacherId, schoolId, clazzNames, ex.getMessage(), ex);
//            openAuthContext.setCode("400");
//            openAuthContext.setError("创建班级失败");
//            return openAuthContext;
//        }
//    }

    @RequestMapping(value = "sendnaminglist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendNamingListToQQMailBox(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        long clazzId = SafeConverter.toLong(openAuthContext.getParams().get("clazzId"), Long.MIN_VALUE);
        String qq = SafeConverter.toString(openAuthContext.getParams().get("qq"));

        if (teacherId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE || StringUtils.isEmpty(qq)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }

        try {
            if (!teacherLoaderClient.isTeachingClazz(teacherId, clazzId)) {
                openAuthContext.setCode("400");
                openAuthContext.setError("老师和班级不匹配，发送邮件失败！");
                return openAuthContext;
            }
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient().loadClazz(clazzId);
            Map<String, Object> content = new HashMap<>();
            content.put("teacher", teacher);
            content.put("clazz", clazz);
            content.put("prefix", ProductConfig.getMainSiteBaseUrl());
            String subject = clazz.getClassLevel() + "年级" + clazz.getClassName() + "学生名单（一起作业网账号密码）";
            emailServiceClient.createTemplateEmail(EmailTemplate.wxstudentlistletter)
                    .to(qq + "@qq.com")
                    .subject(subject)
                    .content(content)
                    .send();
        } catch (Exception ex) {
            logger.error("发送邮件失败!teacherId:{},clazzId:{}", teacherId, clazzId, ex.getMessage(), ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("发送邮件失败");
            return openAuthContext;
        }
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    @RequestMapping(value = "teacherinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getTeacherInfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId).stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .collect(Collectors.toList());

        Collection<Long> clazzIds = clazzs.stream()
                .map(Clazz::getId)
                .filter(t -> t != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, List<Long>> clazzStudentMap = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzIds(clazzIds);
        List<Map<String, Object>> clazzStudentCountList = new LinkedList<>();
        for (Clazz clazz : clazzs) {
            Map<String, Object> clazzMap = new HashMap<>();
            clazzMap.put("clazzId", clazz.getId());
            clazzMap.put("studentCount", clazzStudentMap.get(clazz.getId()) == null ? 0 : clazzStudentMap.get(clazz.getId()).size());
            clazzStudentCountList.add(clazzMap);
        }
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        // 手机号处理
        String authenticatedMobile = sensitiveUserDataServiceClient.showUserMobile(teacherId, "WechatTeacherController.getTeacherInfo", SafeConverter.toString(teacherId));
        openAuthContext.setCode("200");
        openAuthContext.add("teacherDetail", teacherDetail);
        openAuthContext.add("mobile", authenticatedMobile == null ? null : authenticatedMobile);
        openAuthContext.add("school", school);
        openAuthContext.add("clazzs", clazzs);
        openAuthContext.add("clazzStudentCountList", clazzStudentCountList);
        //本月鲜花数量
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        teacherIds.add(teacherId);
        int flowerCount = (int) flowerServiceClient.loadReceiverFlowers(teacherIds).values()
                .stream().flatMap(Collection::stream)
                .filter(t -> t.fetchCreateTimestamp() >= MonthRange.current().getStartTime())
                .count();
        openAuthContext.add("flowerCount", flowerCount);
        return openAuthContext;
    }

    @RequestMapping(value = "changename.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext changeTeacherName(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        // Feature #54929
//        if (ForbidModifyNameAndPortrait.check()) {
//            openAuthContext.setCode("400");
//            openAuthContext.setError(ForbidModifyNameAndPortrait.errorMessage.getInfo());
//            return openAuthContext;
//        }
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        String name = SafeConverter.toString(openAuthContext.getParams().get("name"));

        if (teacherId == Long.MIN_VALUE || StringUtils.isEmpty(name)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (!StringUtils.equals(teacher.fetchRealname(), name)) {
            MapMessage mapMessage = userServiceClient.changeName(teacherId, name);
            if (!mapMessage.isSuccess()) {
                openAuthContext.setCode("400");
                openAuthContext.setError(mapMessage.getInfo());
                return openAuthContext;
            }

            com.voxlearning.alps.spi.bootstrap.LogCollector.info("backend-general", MiscUtils.map("usertoken", teacher.getId(),
                    "usertype", teacher.getUserType(),
                    "platform", "wechat",
                    "version", "",
                    "op", "change user name",
                    "mod1", teacher.fetchRealname(),
                    "mod2", name,
                    "mod3", teacher.getAuthenticationState()));
        }
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    @RequestMapping(value = "qrcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext qrcode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);

        long tmp = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        Long teacherId = (tmp == Long.MIN_VALUE ? null : tmp);
        try {
            if (null == teacherId) {
                openAuthContext.setCode("400");
                openAuthContext.setError("生成二维码失败");
                return openAuthContext;
            }

            Integer campaignId = getRequestInt("campaignId", 0);
            wechatUserCampaignServiceClient.getWechatUserCampaignService().setUserCampaign(teacherId, campaignId).get();

            String url = wechatCodeServiceClient.getWechatCodeService()
                    .generateQRCode(teacherId.toString(), WechatType.TEACHER)
                    .getUninterruptibly();
            openAuthContext.setCode("200");
            openAuthContext.add("qrcode_url", url);
        } catch (UtopiaRuntimeException ex) {
            logger.warn("生成二维码失败，msg:{}", ex.getMessage());
            openAuthContext.setCode("400");
            openAuthContext.setError("生成二维码失败");
        } catch (Exception ex) {
            logger.error("生成二维码失败,msg:{}", ex.getMessage(), ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("生成二维码失败");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "getloginstudentcount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getLoginStudentCount(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        int loginLimitCount = SafeConverter.toInt(openAuthContext.getParams().get("limit"), Integer.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE || loginLimitCount == Integer.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        boolean overQuota = false;
        int loginCount = 0;
        List<Long> clazzIds = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId).stream()
                .filter(t -> t != null)
                .filter(t -> !t.isTerminalClazz())
                .map(Clazz::getId)
                .filter(t -> t != null)
                .collect(Collectors.toList());

        Map<Long, List<Long>> clazzStudentIds = asyncGroupServiceClient.getAsyncGroupService()
                .findStudentIdsByClazzIds(clazzIds);
        Set<Long> studentIds = clazzStudentIds.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userLoaderClient.loadUsers(studentIds);

        for (Long clazzId : clazzStudentIds.keySet()) {
            List<User> studentList = clazzStudentIds.get(clazzId)
                    .stream()
                    .map(userMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(studentList)) {
                continue;
            }
            for (User user : studentList) {
//                Date lastLoginTime = userLoaderClient.findUserLastLoginTime(user);
                Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(user.getId());
                if (lastLoginTime != null) {
                    loginCount++;
                }
                if (loginCount >= loginLimitCount) {
                    overQuota = true;
                    break;
                }
            }
            if (loginCount >= loginLimitCount) {
                overQuota = true;
                break;
            }
        }

        openAuthContext.add("overQuota", overQuota);
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    // 新老师认证卡片相关 -- 对应主站 30- 60 -90 卡片
    @RequestMapping(value = "afterauthcard.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadAfterAuthCard(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
        if (teacherId <= 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        try {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacher == null) {
                openAuthContext.setCode("400");
                openAuthContext.setError("invalid parameters");
                return openAuthContext;
            }
            Map<String, Object> result = new HashMap<>();
            if (teacher.fetchCertificationState() != SUCCESS) {
                //非认证老师 显示新页面
                result.put("showCard", true);
                result.put("isAuth", false);
                //获取我的Mentor
                MapMessage message = businessTeacherServiceClient.findMyMentorOrCandidates(teacherId, MentorCategory.MENTOR_AUTHENTICATION);
                if (message.isSuccess() && message.get("mentor") != null) {
                    result.put("hasMentor", true);
                    result.put("mentor", message.get("mentor"));
                } else {
                    result.put("hasMentor", false);
                }
                openAuthContext.setCode("200");
                openAuthContext.add("result", result);
                return openAuthContext;
            }
            // 如果教师首次认证时间距离现在超过30天，不显示
            TeacherCertificationReward tcr = teacherCertificationServiceClient.getTeacherCertificationService()
                    .loadTeacherCertificationReward(teacher.getId())
                    .getUninterruptibly();
            if (tcr == null || DateUtils.dayDiff(new Date(), tcr.getCreateDatetime()) > 30) {
                result.put("showCard", false);
                result.put("isAuth", true);
                openAuthContext.setCode("200");
                openAuthContext.add("result", result);
                return openAuthContext;
            }
            // 教师带来更多学生卡片，1278月不显示
            if (!Arrays.asList("1", "2", "7", "8").contains(DateUtils.dateToString(tcr.getCreateDatetime(), "M"))) {
                result.put("showCard", true);
                result.put("isAuth", true);
                result.put("phase", tcr.getPhase());
                result.put("pr", tcr.getPostReward());
                result.put("countDown30", Math.max(30 - DateUtils.dayDiff(new Date(), tcr.getCreateDatetime()), 0));
                result.put("studentCount3", businessTeacherServiceClient.getFinishCount(teacher).get("count3"));
                result.put("studentCount6", businessTeacherServiceClient.getFinishCount(teacher).get("count6"));
                Date date = DateUtils.stringToDate("2016-04-01 00:00:00");
                if (RuntimeMode.lt(Mode.STAGING)) {
                    date = DateUtils.stringToDate("2016-03-31 00:00:00");
                }
                result.put("before41", tcr.getCreateDatetime().before(date));
                openAuthContext.setCode("200");
                openAuthContext.add("result", result);
                return openAuthContext;
            } else {
                result.put("showCard", false);
                result.put("isAuth", true);
                openAuthContext.setCode("200");
                openAuthContext.add("result", result);
                return openAuthContext;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            openAuthContext.setCode("400");
        }
        return openAuthContext;
    }

    // 新老师 -- 获取mentor列表
    @RequestMapping(value = "mentorlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext mentorList(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
        if (teacherId <= 0) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        try {
            MapMessage message = businessTeacherServiceClient.findMyMentorOrCandidates(teacherId, MentorCategory.MENTOR_AUTHENTICATION);
            List<Map<String, Object>> mentorList = new ArrayList<>();
            if (message.isSuccess() && message.get("mentor") == null) {
                mentorList = (List<Map<String, Object>>) message.get("mentorList");
            }
            openAuthContext.setCode("200");
            openAuthContext.add("mentorList", mentorList);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            openAuthContext.setCode("400");
        }
        return openAuthContext;
    }

    // 新老师 -- 选择mentor
    @RequestMapping(value = "chosementor.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext choseMentor(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
        Long mentorId = ConversionUtils.toLong(openAuthContext.getParams().get("mentorId"));
        try {
            Teacher mentee = teacherLoaderClient.loadTeacher(teacherId);
            if (mentee == null) {
                openAuthContext.setCode("400");
                openAuthContext.setError("invalid parameters");
                return openAuthContext;
            }
            MapMessage mesg = atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("MENTOR_SYSTEM")
                    .keys(teacherId)
                    .proxy()
                    .setUpMMRelationship(mentorId, teacherId, MentorCategory.MENTOR_AUTHENTICATION, MentorType.MENTEE_INITIATIVE);
            if (mesg.isSuccess()) {
                Map<Long, UserAuthentication> uas = userLoaderClient.loadUserAuthentications(Arrays.asList(mentorId, teacherId));
                UserAuthentication mentor_ua = uas.get(mentorId);
                UserAuthentication mentee_ua = uas.get(teacherId);
                if (mentor_ua != null && mentor_ua.isMobileAuthenticated()) {
                    String amMentee = mentee_ua == null ? null : sensitiveUserDataServiceClient.showUserMobile(mentee_ua.getId(), "/open/wechat/teacher/chosementor", SafeConverter.toString(mentee_ua.getId()));
                    String payload = "同校新老师" + mentee.fetchRealname() + "使用一起作业遇到困难向你求帮助！！登录网站“有奖互助”页";
                    if (mentee_ua != null && mentee_ua.isMobileAuthenticated()) {
                        payload += "帮Ta（" + amMentee + "）";
                    } else {
                        payload += "帮Ta";
                    }
                    payload += "还有园丁豆奖励！";
                    userSmsServiceClient.buildSms().to(mentor_ua)
                            .content(payload).type(SmsType.MENTOR_MENTEE).send();
                }
                openAuthContext.setCode("200");
                return openAuthContext;
            } else {
                openAuthContext.setCode("400");
                openAuthContext.setError(mesg.getInfo());
                return openAuthContext;
            }
        } catch (DuplicatedOperationException ex) {
            openAuthContext.setCode("400");
            openAuthContext.setError("正在处理，请不要重复提交");
            return openAuthContext;
        }
    }

    private List<School> searchSchool(Collection<Integer> regionCode, String query, Integer limit) {
        List<School> regionSchools = raikouSystem.querySchoolLocations(regionCode)
                .enabled()
                .waitingSuccess()
                .filter(e -> e.getLevel() == SchoolLevel.JUNIOR.getLevel())
                .transform()
                .asList();
        if (CollectionUtils.isEmpty(regionSchools)) {
            return Collections.emptyList();
        }
        if (StringUtils.isEmpty(query)) {
            return regionSchools;
        }
        Map<School, Integer> hit = new HashMap<>();
        char[] chars = query.toCharArray();
        for (School school : regionSchools) {
            for (char c : chars) {
                if (StringUtils.isNotBlank(school.getShortName()) && school.getShortName().indexOf(c) != -1) {
                    if (hit.get(school) == null) {
                        hit.put(school, 1);
                    } else {
                        hit.put(school, hit.get(school) + 1);
                    }
                }
            }
        }
        List<Map.Entry<School, Integer>> list = new ArrayList<>(hit.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<School, Integer>>() {
            @Override
            public int compare(Map.Entry<School, Integer> o1, Map.Entry<School, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        List<School> schools = new LinkedList<>();
        if (limit == null || limit > list.size()) {
            limit = list.size();
        }
        for (int i = 0; i < limit; i++) {
            schools.add(list.get(i).getKey());
        }
        return schools;
    }

    // 新老师福利  是否余姚老师接口
    @RequestMapping(value = "isyuyaot.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext isYuYaoTeacher(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);

        openAuthContext.setCode("200");
        openAuthContext.add("yuyaoFlag", false);
        return openAuthContext;
    }

    // 积分历史
    @RequestMapping(value = "integralhistory.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext integralHistory(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        int pageNumber = SafeConverter.toInt(openAuthContext.getParams().get("pn"), 1);
        boolean ge0 = SafeConverter.toBoolean(openAuthContext.getParams().get("ge0"), true);
        String subjectStr = SafeConverter.toString(openAuthContext.getParams().get("subject"));
        teacherLoaderClient.loadRelTeacherIdBySubject(teacherId, Subject.of(subjectStr));

        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }

        User teacher = raikouSystem.loadUser(teacherId);
        if (teacher == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }

        if (pageNumber < 1) {
            pageNumber = 1;
        }
        UserIntegralHistoryPagination pagination = userLoaderClient
                .loadUserIntegralHistories(teacher, 3, pageNumber - 1, 5, ge0);
        openAuthContext.add("pagination", pagination);
        openAuthContext.add("currentPage", pageNumber);
        openAuthContext.add("integral", pagination.getUsableIntegral());
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    // 生成需要在微信老师端使用的cookie
    @RequestMapping(value = "genvoxauthcookie.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext genVoxAuthCookie(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("参数不正确");
            return openAuthContext;
        }
        User teacher = raikouSystem.loadUser(teacherId);
        if (teacher == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("老师不存在");
            return openAuthContext;
        }
        // 主站cookieValue=version|userId|roleIds|password，这里伪造一个简单的cookieValue=version|userId|1|password
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add(teacherId.toString());
//        teacher
        list.add("1");
        // 会有用户密码为空吗
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
        String password = ua.getPassword();
        if (StringUtils.isEmpty(password)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("密码不能为空");
            return openAuthContext;
        }
        list.add(ua.getPassword());
        String cookieValue = StringUtils.join(list, "|");
        String cek = ConfigManager.instance().getCommonConfig().getConfigs().get("cookie_encryption_key");
        if (cek == null) {
            throw new ConfigurationException("No 'cookie_encryption_key' defined");
        }
        String encryptedCookieValue = AesUtils.encryptBase64String(cek, CookieManager.ENCRYPTION_TAG + cookieValue);
        encryptedCookieValue = StringUtils.stripEnd(encryptedCookieValue, "=");
        openAuthContext.add("cookie", encryptedCookieValue);
        return openAuthContext;
    }

    // 验证微信端穿过来的cookie
    @RequestMapping(value = "validatevoxauthcookie.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext validateVoxAuthCookie(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String value = SafeConverter.toString(openAuthContext.getParams().get("cookie"));
        try {
            if (value != null) {
                int paddedLength = (value.length() + 3) / 4 * 4;
                value = StringUtils.rightPad(value, paddedLength, '=');
                String cek = ConfigManager.instance().getCommonConfig().getConfigs().get("cookie_encryption_key");
                if (cek == null) {
                    throw new ConfigurationException("No 'cookie_encryption_key' configured");
                }
                value = AesUtils.decryptBase64String(cek, value);
                if (StringUtils.startsWith(value, CookieManager.ENCRYPTION_TAG)) {
                    value = StringUtils.substring(value, CookieManager.ENCRYPTION_TAG.length());
                    // 得到saltedPassword
                    String[] segments = StringUtils.split(value, "|");
                    Long teacherId = SafeConverter.toLong(segments[1]);
                    String saltedPassword = segments[3];
                    Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                    UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
                    if (null != teacher && ua.getPassword().equals(saltedPassword)) {
                        openAuthContext.setCode("200");
                        openAuthContext.add("isValid", true);
                        openAuthContext.add("teacherId", teacherId);
                        Subject subject = teacher.getSubject();
                        if (subject == null) {
                            subject = Subject.UNKNOWN;
                        }
                        openAuthContext.add("subject", subject.getKey());
                        return openAuthContext;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("validate wechat teacher cookie failed,cookie value is {},error is {}", value, ex.getMessage());
        }
        openAuthContext.setCode("200");
        openAuthContext.add("isValid", false);
        return openAuthContext;
    }

    @RequestMapping(value = "loadteacherauthentication.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadTeacherAuthentication(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail != null && teacherDetail.fetchCertificationState() == AuthenticationState.SUCCESS) {
            openAuthContext.add("isAuth", true);
        } else {
            openAuthContext.add("isAuth", false);
        }
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    @RequestMapping(value = "getsubjects.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getSubjects(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        teacherIds.add(teacherId);
        Map<Long, Teacher> teacherMaps = teacherLoaderClient.loadTeachers(teacherIds);
        List<Map<String, Object>> subjects = new ArrayList<>();
        for (Teacher t : teacherMaps.values()) {
            Subject subject = t.getSubject();
            Map<String, Object> map = new HashMap<>();
            map.put("name", subject.name());
            map.put("value", subject.getValue());
            subjects.add(map);
        }
        openAuthContext.add("subjects", subjects);
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    @RequestMapping(value = "getrelteachers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getRelTeacherIds(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        teacherIds.add(teacherId);
        openAuthContext.add("relTeachers", teacherLoaderClient.loadTeachers(teacherIds));
        openAuthContext.setCode("200");
        return openAuthContext;
    }


    // 获取一个老师在这个班所教的所有学科
    @RequestMapping(value = "getsubjectsforclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getSubjectsForClazz(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        long clazzId = SafeConverter.toLong(openAuthContext.getParams().get("clazzId"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE || clazzId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        List<Map<String, Object>> teachingSubjects = new ArrayList<>();
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        teacherIds.add(teacherId);
        teacherIds.stream().filter(t -> teacherLoaderClient.isTeachingClazz(t, clazzId)).forEach(t -> {
            Subject subject = teacherLoaderClient.loadTeacher(t).getSubject();
            Map<String, Object> map = new HashMap<>();
            map.put("name", subject.name());
            map.put("value", subject.getValue());
            teachingSubjects.add(map);
        });
        openAuthContext.add("teachingSubjects", teachingSubjects);
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    @RequestMapping(value = "getrelteacheridbysubject.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getRelTeacherIdBySubject(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        String subject = SafeConverter.toString(openAuthContext.getParams().get("subject"));
        if (teacherId < 0 || StringUtils.isBlank(subject)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        Long relTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacherId, Subject.of(subject));
        openAuthContext.add("relTeacherId", relTeacherId);
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    // 记录老师首页点击了升级提示
    @RequestMapping(value = "teacheragent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getTeacherAgent(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("uid"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        openAuthContext.setCode("200");

        // 根据灰度地区控制
        openAuthContext.add("agentList", teacherAgentLoaderClient.getSchoolManager(teacherDetail.getTeacherSchoolId()));

        return openAuthContext;

    }
}
