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

package com.voxlearning.utopia.service.business.impl.service.teacher;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.random.RandomGenerator;
import com.voxlearning.alps.lang.util.EmailRule;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.constants.UserActivityType;
import com.voxlearning.utopia.service.user.api.constants.UserExtensionAttributeKeyType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;

@Named
@Slf4j
public class TeacherInvitationServiceImpl extends BusinessServiceSpringBean {

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private UserActivityServiceClient userActivityServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;

    /**
     * 教研员邀请老师
     */
    public MapMessage rstaffInviteTeacherBySms(User user, String[] mobiles) {
        List<String> successMobiles = new ArrayList<>();
        List<String> errorMobiles = new ArrayList<>();
        String rstaffName = user.getProfile().getRealname();

        //邀请人ID
        String inviter = conversionService.convert(user.getId(), String.class);

        // FIXME: 可以继续优化，无必要加载InviteHistory，Location足够
        List<InviteHistory> inviteHistories = asyncInvitationServiceClient.loadByInviter(user.getId())
                .filter(t -> t.getType() == InvitationType.RSTAFF_INVITE_TEACHER_SMS)
                .toList();
        //被邀请人手机号
        Map<String, User> inviteeSensitiveMobileMap = new LinkedHashMap<>();
        for (InviteHistory inviteHistory : inviteHistories) {
            String inviteMobile = inviteHistory.getInviteSensitiveMobile();
            //获得手机号对应的邀请用户
            if (inviteeSensitiveMobileMap.get(inviteMobile) == null) {
                inviteeSensitiveMobileMap.put(inviteMobile, userLoaderClient.loadUser(inviteHistory.getInviteeUserId()));
            }
        }
        //发送的手机号不能重复，防止同一邀请人邀请同一手机号生成多个登录账号
        Set<String> sentSensitiveMobileSet = new LinkedHashSet<>();
        //生成账号并保存邀请记录
        for (String mobile : mobiles) {
            try {
                String sensitiveMobile = sensitiveUserDataServiceClient.encodeMobile(mobile);
                if (sentSensitiveMobileSet.contains(sensitiveMobile))
                    continue;

                sentSensitiveMobileSet.add(sensitiveMobile);

                MapMessage mesg = mobileValidator(mobile);
                if (!mesg.isSuccess()) {
                    errorMobiles.add(mobile + ":" + mesg.getInfo());

                } else {
                    //教研员邀请老师的短信中，在教研员名称后面加“老师”,如果教研员名称中本身已经有“老师”字段 ，自动过滤
                    rstaffName = rstaffName.replace("老师", "") + "老师";
                    //短信内容
                    String payload = rstaffName + "邀您参与外专委十二五课题，请登录17zuoye.com，";

                    //邀请过，则调取已生成的学号密码并再次发送
                    User inviteUser = inviteeSensitiveMobileMap.get(sensitiveMobile);
                    if (inviteUser != null) {
                        // FIXME 这里已经不用了，要用的时候再改
//                        payload = payload + "用户名" + inviteUser.getId() + "，密码" + inviteUser.getRealCode();
                        payload = payload + "用户名" + inviteUser.getId();
                    } else {
                        NeonatalUser neonatalUser = new NeonatalUser();
                        neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
                        neonatalUser.setUserType(UserType.TEACHER);
                        neonatalUser.setRealname("");  //no name now
                        neonatalUser.setMobile(mobile);
                        neonatalUser.setPassword(RandomGenerator.generatePlainPassword());
                        neonatalUser.setInvitationType(InvitationType.RSTAFF_INVITE_TEACHER_SMS);
                        neonatalUser.setInviter(inviter);
                        neonatalUser.setSubject(Subject.ENGLISH.name());
                        MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
                        if (!message.isSuccess()) {
                            errorMobiles.add(mobile);
                            return MapMessage.errorMessage();
                        }
                        User teacher = (User) message.get("user");
                        payload = payload + "用户名" + teacher.getId() + "，密码" + neonatalUser.getPassword();
                    }
                    smsServiceClient.createSmsMessage(mobile)
                            .content(payload)
                            .type(SmsType.RSTAFF_SMS_INVITE_TEACHER.name())
                            .send();
                    successMobiles.add(mobile);
                }

            } catch (Exception e) {
                log.error("rstaffInviteTeacherBySms :" + e.getMessage(), e);
                errorMobiles.add(mobile);
            }
        }
        return MapMessage.successMessage("邀请成功").add("successMobiles", successMobiles).add("errorMobiles", errorMobiles);
    }

    public MapMessage teacherInviteTeacherBySms(User user, final String mobile, String realname, InvitationType type, String subject) {
        Validate.notNull(user, "Inviter should not be null");

        // 检查被邀请老师姓名
        // FIXME 他们说微信端短信邀请过来的用户，先不填姓名，
        // FIXME 我觉得可能有坑（我们的注册引导里没有对于空姓名的引导），他们非得先上，先这么地吧
        MapMessage mesg = nameValidator(realname);
        if (type != InvitationType.TEACHER_INVITE_TEACHER_SMS_BY_WECHAT && !mesg.isSuccess()) {
            return mesg;
        }
        // 验证手机
        mesg = mobileValidator(mobile);
        if (!mesg.isSuccess()) {
            return mesg;
        }
        // 检查是否邀请过这个手机号，如果邀请过，跳过注册账户，直接发短信
        Set<InvitationType> types = new HashSet<>();
        types.add(InvitationType.TEACHER_INVITE_TEACHER_SMS);
        types.add(InvitationType.TEACHER_INVITE_TEACHER_SMS_BY_WECHAT);

        Long inviteeId = asyncInvitationServiceClient.loadByInviter(user.getId())
                .originalLocationsAsList()
                .stream()
                .filter(t -> types.contains(t.getType()))
                .filter(t -> sensitiveUserDataServiceClient.mobileEquals(t.getSensitiveMobile(), mobile))
                .filter(t -> t.getInviteeId() != 0)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .map(InviteHistory.Location::getInviteeId)
                .findFirst()
                .orElse(null);

        String account;
        String password;
        boolean isNewInvitee = true;
        if (inviteeId != null) {
            User invitee = userLoaderClient.loadUser(inviteeId);
            if (invitee == null) {
                return MapMessage.errorMessage("邀请失败");
            }
            account = invitee.getId().toString();
            // 没有realcode了，所以只能重置一下密码
            password = RandomGenerator.generatePlainPassword();
            if (!userServiceClient.setPassword(invitee, password).isSuccess()) {
                return MapMessage.errorMessage("邀请失败");
            }
            isNewInvitee = false;
        } else {
            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
            neonatalUser.setUserType(UserType.TEACHER);
            neonatalUser.setRealname(realname);
            neonatalUser.setMobile(mobile);
            neonatalUser.setPassword(RandomGenerator.generatePlainPassword());
            neonatalUser.setInvitationType(type);
            neonatalUser.setInviter(user.getId().toString());

            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
            if (!message.isSuccess()) {
                return MapMessage.errorMessage("创建教师失败");
            }
            User teacher = (User) message.get("user");
            account = teacher.getId().toString();
            password = neonatalUser.getPassword();
        }
        String key = MemcachedKeyConstants.INVITE_TEACHER_SMS_SEND_COUNT + "_" + user.getId() + "_" + account;
        Long ret = businessCacheSystem.CBS.unflushable.incr(key, 1, 1, DateUtils.getCurrentToDayEndSecond());

        //同一天，同一个人邀请同一个手机号超过3次，就不发短信了
        if (ret < 4) {
            // 发短信
            String moneyStr = "，请在24小时内登录领取话费奖励";
            int month = MonthRange.current().getMonth();
            if (month == 1 || month == 2 || month == 7 || month == 8) {
                moneyStr = "，系统已为您注册";
            }
            String payload = user.getProfile().getRealname().replace("老师", "") +
                    "老师邀您使用一起作业网辅助教学（17zuoye.com）" + moneyStr + "，账号" + account + "，密码" + password;
            if (StringUtils.equals(subject, "CHINESE")) {
                payload = user.getProfile().getRealname().replace("老师", "") +
                        "老师邀您使用一起作业网辅助教学（17zuoye.com），账号" + account + "，密码" + password;
            }

            smsServiceClient.createSmsMessage(mobile)
                    .content(payload)
                    .type(SmsType.TEACHER_SMS_INVITE_TEACHER.name())
                    .send();
        }
        return MapMessage.successMessage().add("userId", account).add("isNewInvitee", isNewInvitee);
    }

    public MapMessage wakeUpInvitedTeacherBySms(User inviter, User invitee, InvitationType type) {

        Validate.notNull(inviter, "Inviter should not be null");
        Validate.notNull(invitee, "Invitee should not be null");

        Long originInviterId = null;
        InviteHistory inviteHistory = asyncInvitationServiceClient.loadByInvitee(invitee.getId())
                .enabled()
                .findFirst();
        if (inviteHistory != null) {
            originInviterId = inviteHistory.getUserId();
            //重置邀请关系
            asyncInvitationServiceClient.getAsyncInvitationService()
                    .deleteInviteHistories(invitee.getId())
                    .awaitUninterruptibly();
        }

        inviteHistory = InviteHistory.newInstance();
        inviteHistory.setUserId(inviter.getId());
        inviteHistory.setInviteeUserId(invitee.getId());
        UserAuthentication inviteeUa = userLoaderClient.loadUserAuthentication(invitee.getId());
        inviteHistory.setInviteSensitiveEmail(inviteeUa.getSensitiveEmail());
        inviteHistory.setInviteSensitiveMobile(inviteeUa.getSensitiveMobile());
        inviteHistory.setInvitationType(type);
        asyncInvitationServiceClient.getAsyncInvitationService()
                .createInviteHistory(inviteHistory)
                .awaitUninterruptibly();

        // invitee原来可能不是一个被邀请的教师，现在需要更新isInvite字段
        if (Boolean.FALSE.equals(invitee.getIsInvite())) {
            EventBus.execute(() -> userServiceClient.getRemoteReference().updateIsInvite(invitee.getId(), true));
        }

        //重置密码
        String password = RandomGenerator.generatePlainPassword();
        MapMessage mapMessage = userServiceClient.setPassword(invitee, password);
        if (mapMessage.isSuccess()) {
            String moneyStr = "，请在24小时内登录领取话费奖励";
            int month = MonthRange.current().getMonth();
            if (month == 1 || month == 2 || month == 7 || month == 8) {
                moneyStr = "，系统已为您注册";
            }
            String payload = inviter.getProfile().getRealname().replace("老师", "") +
                    "老师邀您使用一起作业网辅助教学（17zuoye.com）" + moneyStr + "，账号" + invitee.getId() + "，密码" + password;
            userSmsServiceClient.buildSms().to(invitee)
                    .content(payload)
                    .type(SmsType.TEACHER_SMS_INVITE_TEACHER)
                    .send();

            if (Objects.equals(originInviterId, inviter.getId())) {
                return MapMessage.successMessage().add("originInviterId", originInviterId);
            }

            return MapMessage.successMessage().add("originInviterId", originInviterId);
        } else {
            return MapMessage.errorMessage();
        }
    }

    public String encryptCodeGenerator(Long userId, String email, String subject) {
        Map<String, Object> validationInfo = new LinkedHashMap<>();
        validationInfo.put("userId", userId);
        validationInfo.put("timestamp", System.currentTimeMillis());
        if (StringUtils.isNotBlank(email)) {
            validationInfo.put("email", email);
        }
        if (StringUtils.isNotBlank(subject)) {
            validationInfo.put("subject", subject);
        }
        String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
        if (defaultDesKey == null) {
            throw new ConfigurationException("No 'default_des_key' configured");
        }
        return DesUtils.encryptHexString(defaultDesKey, JsonUtils.toJson(validationInfo));
    }

    public MapMessage nameValidator(String name) {
        MapMessage mesg = new MapMessage();
        // 检查名字是否为空
        if (!StringUtils.isNotBlank(name)) {
            mesg.setInfo("被邀请老师的姓名不能为空");
            mesg.setSuccess(false);
            return mesg;
        }
        // 检查名字长度
        if (name.length() > 5) {
            mesg.setInfo("被邀请老师的姓名过长");
            mesg.setSuccess(false);
            return mesg;
        }
        // 验证通过
        mesg.setSuccess(true);
        return mesg;
    }

    public MapMessage emailValidator(String email) {
        MapMessage mesg = new MapMessage();
        //  检查邮箱是否为空
        if (!StringUtils.isNotBlank(email)) {
            mesg.setInfo("邮箱不能为空");
            mesg.setSuccess(false);
            return mesg;
        }
        // 检查邮箱的合法性
        if (!EmailRule.isEmail(email)) {
            mesg.setInfo("邮箱格式不正确");
            mesg.setSuccess(false);
            return mesg;
        }
        // 检查邮箱唯一性
        if (userLoaderClient.loadEmailAuthentication(email) != null) {
            mesg.setInfo("此邮箱已经注册了，请与您想邀请的老师联系.");
            mesg.setSuccess(false);
            return mesg;
        }
        // 验证通过
        mesg.setSuccess(true);
        return mesg;
    }

    public MapMessage mobileValidator(String mobile) {
        MapMessage mesg = new MapMessage();
        // 检查手机号码是否为空
        if (!StringUtils.isNotBlank(mobile)) {
            mesg.setInfo("手机号码不能为空.");
            mesg.setSuccess(false);
            return mesg;
        }
        // 检查手机号码的号段
        if (!MobileRule.isMobile(mobile)) {
            mesg.setInfo("手机号码不正确.");
            mesg.setSuccess(false);
            return mesg;
        }
        // 检查手机号码唯一性
        if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
            mesg.setInfo("此手机号已经注册.");
            mesg.setSuccess(false);
            return mesg;
        }
        // 验证通过
        mesg.setSuccess(true);
        return mesg;
    }

    public MapMessage sendAuthenticateNotify(User sender, Long receiverId) {
        UserExtensionAttribute attribute = userAttributeLoaderClient.loadUserExtensionAttributes(receiverId)
                .type(UserExtensionAttributeKeyType.TEACHER_INVITE_TEACHER_NOTIFY_AUTHENTICATE)
                .findFirst();
        if (null == attribute) {
            User receiver = userLoaderClient.loadUser(receiverId);
            MapMessage message;
            try {
                message = userAttributeServiceClient.setExtensionAttribute(receiverId,
                        UserExtensionAttributeKeyType.TEACHER_INVITE_TEACHER_NOTIFY_AUTHENTICATE);
            } catch (Exception ex) {
                message = MapMessage.errorMessage();
            }
            if (message.isSuccess()) {
                String payload = sender.fetchRealname() + "老师邀请您登录17zuoye.com，申请教师认证，成功可得100园丁豆哦！已有4876位老师拿到话费！快去申请！";
                userSmsServiceClient.buildSms().to(receiver)
                        .content(payload)
                        .type(SmsType.TEACHER_NOTIFY_TEACHER_AUTH)
                        .send();
                return MapMessage.successMessage();
            }
            if (message.hasDuplicatedException()) {
                // DuplicateKeyException, ignore
                return MapMessage.successMessage();
            }
            return MapMessage.errorMessage("发送失败");
        }
        return MapMessage.errorMessage("只能发送一次提醒");
    }

    public MapMessage rstaffNotifyTeacherBySms(User sender, Long receiverId, String flag) {
        String keyType = "";
        if (flag != null && "arrange".equals(flag)) {
            keyType = UserExtensionAttributeKeyType.RESEARCHSTAFF_INVITE_TEACHER_NOTIFY_ARRANGE.name();
        } else if (flag != null && "authentication".equals(flag)) {
            keyType = UserExtensionAttributeKeyType.RESEARCHSTAFF_INVITE_TEACHER_NOTIFY_AUTHENTICATE.name();
        }
        UserExtensionAttribute attribute = userAttributeLoaderClient.loadUserExtensionAttributes(receiverId)
                .key(keyType).findFirst();
        if (null == attribute) {
            User receiver = userLoaderClient.loadUser(receiverId);
            MapMessage message;
            try {
                message = userAttributeServiceClient.setExtensionAttribute(receiverId, keyType, null);
            } catch (Exception ex) {
                message = MapMessage.errorMessage();
            }
            if (message.isSuccess()) {
                if ("arrange".equals(flag)) {
                    // FIXME 这里已经不用了，要用的话再改
//                    String sms = sender.fetchRealname() + "教研员邀请您参与“十二五”课题，登录17zuoye.com，布置网络作业，用户名"
//                            + receiver.getId() + "，密码" + receiver.getRealCode();
                    String sms = sender.fetchRealname() + "教研员邀请您参与“十二五”课题，登录17zuoye.com，布置网络作业，用户名"
                            + receiver.getId();
                    userSmsServiceClient.buildSms().to(receiver)
                            .content(sms)
                            .type(SmsType.RSTAFF_NOTIFY_TEACHER_ARRANGE_HW)
                            .send();
                } else if ("authentication".equals(flag)) {
                    String sms = sender.fetchRealname() + "教研员提醒您登录17zuoye.com，申请教师认证！完成“十二五”课题研究。";
                    userSmsServiceClient.buildSms().to(receiver)
                            .content(sms)
                            .type(SmsType.RSTAFF_NOTIFY_TEACHER_AUTH)
                            .send();
                }
                return MapMessage.successMessage();
            }
            if (message.hasDuplicatedException()) {
                // duplicatedKeyException, ignore
                return MapMessage.successMessage();
            }
            return MapMessage.errorMessage("发送失败");
        }
        return MapMessage.errorMessage("只能发送一次提醒");
    }

    /**
     * 教研员邀请老师 提醒所有老师 发短信
     */
    public MapMessage rstaffNotifyAllBySms(Long userId) {
        if (null == userId) {
            return MapMessage.errorMessage("教研员不存在");
        }
        Date date = DateUtils.nextDay(new Date(), -180);
        List<InviteHistory> inviteHistoryPage = asyncInvitationServiceClient.loadByInviter(userId)
                .filter(t -> t.getCreateTime() > date.getTime())
                .toList();
        for (InviteHistory inviteHistory : inviteHistoryPage) {
            //短信方式
            if (inviteHistory.getInvitationType() != null && InvitationType.RSTAFF_INVITE_TEACHER_SMS == inviteHistory.getInvitationType()) {
                Long teacherId = inviteHistory.getInviteeUserId();
                User teacher = userLoaderClient.loadUser(teacherId);
                if (teacher == null) {
                    continue;
                }
                //登录过
//                Date lastLoginTime = userLoaderClient.findUserLastLoginTime(teacher);
                Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(teacher.getId());
                if (lastLoginTime != null) {
                    //是否布置作业

                    UserActivity ua = userActivityServiceClient.getUserActivityService()
                            .findUserActivities(teacher.getId())
                            .getUninterruptibly()
                            .stream()
                            .filter(t -> t.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                            .findFirst()
                            .orElse(null);
                    Date teacherLatestCheckHomeworkTime = (ua == null ? null : ua.getActivityTime());

                    //是否认证
                    boolean authenticated = teacher.fetchCertificationState() == SUCCESS;
                    if (teacherLatestCheckHomeworkTime == null) {
                        //提醒过
                        long count = userAttributeLoaderClient.loadUserExtensionAttributes(teacher.getId())
                                .type(UserExtensionAttributeKeyType.RESEARCHSTAFF_INVITE_TEACHER_NOTIFY_ARRANGE)
                                .count();
                        if (count > 0) {
                            continue;
                        } else {
                            //提醒使用
                            User sender = userLoaderClient.loadUser(userId);
                            rstaffNotifyTeacherBySms(sender, teacherId, "arrange");
                        }

                    } else {
                        if (!authenticated) {
                            long count = userAttributeLoaderClient.loadUserExtensionAttributes(teacher.getId())
                                    .type(UserExtensionAttributeKeyType.RESEARCHSTAFF_INVITE_TEACHER_NOTIFY_AUTHENTICATE)
                                    .count();
                            if (count > 0) {
                                continue;
                            }
                            //提醒认证
                            User sender = userLoaderClient.loadUser(userId);
                            rstaffNotifyTeacherBySms(sender, teacherId, "authentication");
                        }
                    }
                }

            }
        }
        return MapMessage.successMessage();
    }

    public MapMessage teacherRegisterByMobile(String mobile, String realname, String province) {

        // 检查名字是否为空
        MapMessage mesg = new MapMessage();
        if (!StringUtils.isNotBlank(realname)) {
            mesg.setInfo("姓名不能为空");
            mesg.setSuccess(false);
            return mesg;
        }
        // 检查名字长度
        if (realname.length() > 5) {
            mesg.setInfo("姓名过长");
            mesg.setSuccess(false);
            return mesg;
        }

        // 验证手机
        mesg = mobileValidator(mobile);
        if (!mesg.isSuccess()) {
            return mesg;
        }

        String account;
        String password = RandomGenerator.generatePlainPassword();
        String postUrl;

        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
        neonatalUser.setUserType(UserType.TEACHER);
        neonatalUser.setRealname(realname);
        neonatalUser.setMobile(mobile);
        neonatalUser.setPassword(password);
        if (StringUtils.isNotBlank(province)) {
            neonatalUser.setWebSource(StringUtils.join("QRCODE_", province));
        }
        try {
            MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
            if (!message.isSuccess()) {
                return MapMessage.errorMessage("创建教师失败");
            }
            User teacher = (User) message.get("user");
            account = teacher.getId().toString();

            postUrl = UrlUtils.buildUrlQuery(ProductConfig.getMainSiteBaseUrl() + "/j_spring_security_check",
                    MiscUtils.map().add("j_username", account).add("j_password", password));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage("创建教师失败");
        }

//        String payload = realname + "，欢迎您使用一起作业网辅助教学，网址17zuoye.com，系统已为您注册账号" + account + "，密码" + password;
        String payload = realname + "，恭喜您加入一起作业！请用此手机号作为账号登录http://www.17zuoye.com 使用！";

        smsServiceClient.createSmsMessage(mobile)
                .content(payload)
                .type(SmsType.TEACHER_REG_BY_MOBILE.name())
                .send();
        return MapMessage.successMessage().add("postUrl", postUrl);
    }
}
