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

package com.voxlearning.washington.controller.teacher;


import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.entity.task.TeacherRookieTask;
import com.voxlearning.utopia.service.business.api.TeacherRookieTaskService;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskLoaderClient;
import com.voxlearning.utopia.service.invitation.api.InviteRewardHistoryService;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.invitation.entity.InviteRewardHistory;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Controller
@RequestMapping("/teacherinvite/2018/")
public class Teacher2018InviteController extends AbstractController {

    @Inject
    private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @ImportService(interfaceClass = TeacherRookieTaskService.class)
    private TeacherRookieTaskService teacherRookieTaskService;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @ImportService(interfaceClass = InviteRewardHistoryService.class)
    public InviteRewardHistoryService inviteRewardHistoryService;
    @Inject
    private TeacherTaskLoaderClient ttLoader;

    @ResponseBody
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public MapMessage getInviteList() {
        try {
            TeacherDetail teacherDetail = currentTeacherDetail();
            if (teacherDetail == null) {
                return MapMessage.errorMessage("只有老师才可以参与该活动");
            }

            List<Subject> subjects = teacherDetail.getSubjects();
            boolean noPrimaryTeacher = !teacherDetail.isPrimarySchool();
            boolean notEnglishTeacher = subjects == null || (!subjects.contains(Subject.ENGLISH));
            if (noPrimaryTeacher && notEnglishTeacher) {
                return MapMessage.errorMessage("不在中学邀请活动范围");
            }

            List<InviteHistory> historyList = asyncInvitationServiceClient.getAsyncInvitationService().queryByUser2019First(teacherDetail.getId());
            List<Long> teacherIds = historyList.stream().map(InviteHistory::getInviteeUserId).collect(toList());
            Map<Long, TeacherDetail> teacherDetailMap = teacherLoaderClient.loadTeacherDetails(teacherIds);

            return inviteDetail(teacherDetail, historyList, teacherDetailMap);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    private MapMessage inviteDetail(TeacherDetail teacherDetail, List<InviteHistory> historyList, Map<Long, TeacherDetail> teacherDetailMap) {
        List<Map<String, Object>> primaryData = new ArrayList<>();
        List<Map<String, Object>> juniorData = new ArrayList<>();

        boolean userAuthStatus = Objects.equals(teacherDetail.getAuthenticationState(), AuthenticationState.SUCCESS.getState());
        Map<Long, InviteRewardHistory> rewardHistoryMap = inviteRewardHistoryService.loadByUserId(teacherDetail.getId())
                .stream().collect(toMap(InviteRewardHistory::getInviteId, Function.identity()));

        long primaryRewardCount = 0;
        long juniorRewardCount = 0;

        for (InviteHistory inviteHistory : historyList) {
            TeacherDetail teacher = teacherDetailMap.get(inviteHistory.getInviteeUserId());
            if (teacher == null) continue;
            InviteRewardHistory inviteRewardHistory = rewardHistoryMap.get(teacher.getId());

            Map<String, Object> item = MapUtils.map(
                    "id", teacher.getId(),
                    "name", getTeacherName(teacher),
                    "img", getUserAvatarImgUrl(teacher.fetchImageUrl()),
                    "school", teacher.getTeacherSchoolName()
            );

            // 写起来比较多，但是容易理解
            boolean oneMonth = DateUtils.dayDiff(new Date(), teacher.getCreateTime()) <= 30;

            if (teacher.getKtwelve() == null || teacher.isPrimarySchool()) {
                TeacherRookieTask teacherRookieTask = teacherRookieTaskService.loadRookieTask(teacher.getId());

                if (oneMonth) {
                    if (teacherRookieTask == null) {
                        item.put("setup", "未领取任务");
                        item.put("reward", "0");
                    } else if (teacherRookieTask.fetchFinished()) {
                        item.put("setup", "完成任务");
                        item.put("reward", "奖励已完成");
                    } else {
                        TeacherRookieTask.SubTask currSubTask = null;
                        for (TeacherRookieTask.SubTask subTask : teacherRookieTask.getSubTask()) {
                            if (subTask.fetchOnGoing()) {
                                currSubTask = subTask;
                                break;
                            }
                        }
                        assert currSubTask != null;

                        Integer index = currSubTask.getIndex();
                        if (index == 1) {
                            item.put("setup", "布置作业");
                            item.put("reward", "100");
                        } else if (index == 2) {
                            item.put("setup", "检查作业");
                            item.put("reward", "100");
                        } else if (index == 3) {
                            item.put("setup", "点评奖励");
                            item.put("reward", "200");
                        } else if (index == 4) {
                            item.put("setup", "分享报告");
                            item.put("reward", "600");
                        }
                    }
                } else {
                    if ((teacherRookieTask != null && teacherRookieTask.getFinishedDate() != null)
                            && (DateUtils.dayDiff(teacherRookieTask.getFinishedDate(), teacher.getCreateTime()) <= 30)) {
                        item.put("setup", "完成任务");
                        item.put("reward", "奖励已完成");
                    } else {
                        item.put("setup", "已逾期");
                        item.put("reward", 0);
                    }
                }
                // 如果没认证,统一设置为0
                if (!userAuthStatus) {
                    item.put("reward", 0);
                }
                // 这里展示的都是园丁豆, 不是小学的话需要 *10 转换成学豆
                if (!teacherDetail.isPrimarySchool()) {
                    Long reward = MapUtils.getLong(item, "reward", 0L);
                    item.put("reward", reward * 10);
                }
                primaryData.add(item);

                if (inviteRewardHistory != null) {
                    primaryRewardCount += inviteRewardHistory.getReward();
                }
            } else {
                if (oneMonth) {
                    if (Objects.equals(teacher.getAuthenticationState(), AuthenticationState.SUCCESS.getState())) {
                        item.put("setup", "已认证");
                        item.put("reward", 1000);
                    } else {
                        item.put("setup", "未认证");
                        item.put("reward", 0);
                    }
                } else {
                    Date lastAuthDate = teacher.getLastAuthDate();
                    if (
                            (lastAuthDate != null)
                                    && (Objects.equals(teacher.getAuthenticationState(), AuthenticationState.SUCCESS.getState()))
                                    && (DateUtils.dayDiff(lastAuthDate, teacher.getCreateTime()) <= 30)
                    ) {
                        item.put("setup", "已认证");
                        item.put("reward", 1000);
                    } else {
                        item.put("setup", "已逾期");
                        item.put("reward", 0);
                    }
                }
                // 如果没认证,统一设置为0
                if (!userAuthStatus) {
                    item.put("reward", 0);
                }

                // 这里展示的都是园丁豆, 不是小学的话需要 *10 转换成学豆
                if (!teacherDetail.isPrimarySchool()) {
                    Long reward = MapUtils.getLong(item, "reward", 0L);
                    item.put("reward", reward * 10);
                }
                juniorData.add(item);

                if (inviteRewardHistory != null) {
                    juniorRewardCount += inviteRewardHistory.getReward();
                }
            }
        }


        Long inviteTeacherCount = ttLoader.getInviteTeacherCount();

        return MapMessage.successMessage()
                .add("primaryData", primaryData)
                .add("juniorData", juniorData)
                .add("primaryDataSize", primaryData.size())
                .add("juniorDataSize", juniorData.size())
                .add("primaryRewardCount", teacherDetail.isPrimarySchool() ? primaryRewardCount / 10 : primaryRewardCount)
                .add("juniorRewardCount", teacherDetail.isPrimarySchool() ? juniorRewardCount / 10 : juniorRewardCount)
                .add("teacherId", teacherDetail.getId())
                .add("teacherName", teacherDetail.getProfile().getRealname())
                .add("avatar", getUserAvatarImgUrl(teacherDetail.fetchImageUrl()))
                .add("auth", userAuthStatus)
                .add("primarySchool", teacherDetail.isPrimarySchool())
                .add("teacherSize", historyList.size())
                .add("integralType", teacherDetail.isPrimarySchool() ? "园丁豆" : "学豆")
                .add("clickCount", inviteTeacherCount);
    }

    @ResponseBody
    @RequestMapping(value = "teacher_captcha_token.vpage", method = RequestMethod.GET)
    public MapMessage getTeacherCaptchaToken() {
        try {
            MapMessage mapMessage = MapMessage.successMessage().add("captchaToken", RandomUtils.randomString(24));

            long teacherId = getRequestLong("teacherId", 0);

            if (teacherId != 0) {
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
                mapMessage.add("teacherName", teacherDetail.getProfile().getRealname());
                mapMessage.add("primarySchool", teacherDetail.isPrimarySchool());
                /*TeacherInvitationConfig teacherInvation = getTeacherInvation(teacherDetail);
                mapMessage.add("money", teacherInvation);
                mapMessage.add("money_count", getMaxMonty(teacherInvation));
                */
            }
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping(value = "invite_send_sms_code.vpage", method = RequestMethod.POST)
    public MapMessage sendInviteSmsCode() {
        String mobile = getRequestString("mobile");
        String captchaToken = getRequestString("captchaToken");
        String captchaCode = getRequestString("captchaCode");

        if (StringUtils.isEmpty(mobile)) {
            return MapMessage.errorMessage("请输入手机号");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号格式不正确");
        }
        if (StringUtils.isEmpty(captchaToken)) {
            return MapMessage.errorMessage("captchaToken 不可为空");
        }
        if (StringUtils.isEmpty(captchaCode)) {
            return MapMessage.errorMessage("请输入验证码");
        }
        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return MapMessage.errorMessage("验证码错误");
        }

        if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
            return MapMessage.errorMessage("该手机号码已经注册，请直接登录").add("registered", true);
        }
        MapMessage verifyResponse = smsServiceClient.getSmsService().verifyMobileRisk(mobile);
        if (!verifyResponse.isSuccess()) {
            return verifyResponse;
        }

        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE.name(), false);
    }

    @ResponseBody
    @RequestMapping(value = "invite_register.vpage", method = RequestMethod.POST)
    public MapMessage inviteRegister() {
        try {
            String userIdString = getRequestString("userId");
            String mobile = getRequestString("mobile");
            String smsCode = getRequestString("smsCode");
            String ktwelve = getRequestString("ktwelve");
            String subject = getRequestString("subject");

            if (StringUtils.isEmpty(mobile)) {
                return MapMessage.errorMessage("请输入手机号");
            }
            if (!MobileRule.isMobile(mobile)) {
                return MapMessage.errorMessage("手机号格式不正确");
            }
            MapMessage verifyResponse = smsServiceClient.getSmsService().verifyMobileRisk(mobile);
            if (!verifyResponse.isSuccess()) {
                return verifyResponse;
            }
            MapMessage smsMessage = smsServiceClient.getSmsService().verifyValidateCode(mobile, smsCode, SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE.name());
            if (!smsMessage.isSuccess()) {
                return smsMessage;
            }

            String password = RandomGenerator.generatePlainPassword();
            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
            neonatalUser.setUserType(UserType.TEACHER);
            neonatalUser.setWebSource(UserWebSource.mobile.getSource());
            neonatalUser.setMobile(mobile);
            neonatalUser.setRealname("");
            neonatalUser.setNickName("");
            neonatalUser.setPassword(password);

            if (StringUtils.isNotBlank(ktwelve)) {
                Ktwelve ktwelveEnum = Ktwelve.of(ktwelve);
                if (ktwelveEnum != Ktwelve.UNKNOWN) {
                    neonatalUser.setKtwelve(ktwelveEnum);
                }
            }
            if (StringUtils.isNotBlank(subject)) {
                Subject subjectEnum = Subject.of(subject);
                neonatalUser.setSubject(subjectEnum.name());
            }

            String realname = "";
            // 先看一下邀请人是否正确, 如果不对注册时就不设置邀请人了, 以免后续流程出问题
            if (StringUtils.isNotEmpty(userIdString)) {
                Long userId = Long.parseLong(userIdString);
                User user = userLoaderClient.loadUser(userId, UserType.TEACHER);
                if (user != null) {
                    realname = user.fetchRealname();
                    neonatalUser.setInviter(userIdString);
                    neonatalUser.setInvitationType(InvitationType.TEACHER_INVITE_TEACHER_LINK);
                }
            }

            MapMessage mapMessage = userServiceClient.registerUser(neonatalUser);
            if (mapMessage.isSuccess()) {
                String content = "老师您好，欢迎您使用一起教育科技辅助教学（17zuoye.com），账号" + mobile + "，密码" + password;
                if (StringUtils.isNotEmpty(userIdString)) {
                    content = realname + "老师邀您使用一起教育科技辅助教学（17zuoye.com），账号" + mobile + "，密码" + password;
                }
                smsServiceClient.createSmsMessage(mobile)
                        .content(content)
                        .type(SmsType.SEND_ACCOUNT_PWD_AFTER_REG.name())
                        .send();

                // 发送邀请的 kafka 的消息
                if (StringUtils.isNotEmpty(userIdString)) {
                    User user = (User) mapMessage.get("user");
                    asyncInvitationServiceClient.getAsyncInvitationService().sendInvitationKafkaMessage(SafeConverter.toLong(userIdString), user.getId());
                }
            }
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    private String getTeacherName(TeacherDetail teacherDetail) {
        if (StringUtils.isNotBlank(teacherDetail.fetchRealname())) {
            return teacherDetail.fetchRealname();
        } else {
            String tel = "";
            try {
                tel = sensitiveUserDataServiceClient.loadUserMobile(teacherDetail.getId());
                if (StringUtils.isNotBlank(tel)) {
                    tel = tel.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                }
            } catch (Exception ignored) {
            }
            return tel;
        }
    }
}
