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

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.business.api.constant.AppUseNumCalculateType;
import com.voxlearning.utopia.service.business.api.DPBusinessService;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.CampaignServiceClient;
import com.voxlearning.utopia.service.finance.client.WirelessChargingServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@Service(interfaceClass = DPBusinessService.class)
@ExposeService(interfaceClass = DPBusinessService.class)
public class DPBusinessServiceImpl implements DPBusinessService {

    @Inject private WirelessChargingServiceClient wirelessChargingServiceClient;

    @Inject private BusinessTeacherServiceImpl businessTeacherService;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private CampaignServiceClient campaignServiceClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private UserIntegralServiceClient userIntegralServiceClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private BusinessVendorServiceImpl businessVendorServiceImpl;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public MapMessage teacherInviteTeacherBySms(User user, String mobile, String realname, InvitationType type, String subject) {
        return businessTeacherService.teacherInviteTeacherBySms(user, mobile, realname, type, subject);
    }

    @Override
    public MapMessage wakeUpInvitedTeacherBySms(User inviter, User invitee, InvitationType type) {
        return businessTeacherService.wakeUpInvitedTeacherBySms(inviter, invitee, type);
    }

    @Override
    public MapMessage saveWirelessCharging_junior(Long userId, ChargeType chargeType, Integer amount, String smsMessage, String extraDesc) {
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(userId);
        if (userAuthentication == null) {
            return MapMessage.errorMessage("Unknown user id:" + userId);
        }
        boolean ret = wirelessChargingServiceClient.getWirelessChargingService()
                .saveWirelessCharging_junior(userId, userAuthentication.getSensitiveMobile(), chargeType, amount, smsMessage, extraDesc)
                .getUninterruptibly();
        if (ret) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("Unknown user id:" + userId);
        }
    }

    @Override
    public MapMessage addLotteryFreeChance(CampaignType campaignType, Long userId, int delta) {
        return campaignServiceClient.getCampaignService().addLotteryFreeChance(campaignType, userId, delta);
    }

    // 小学老师内容报错后的奖励
    @Override
    public MapMessage contentFeedBackReward(Long teacherId, Integer rewardIntegral, String message, String questionId, String linkUrl) {
        if (teacherId == null || teacherId == 0 || StringUtils.isBlank(message) || StringUtils.isBlank(questionId)) {
            return MapMessage.errorMessage("参数错误");
        }
        if (rewardIntegral != null && rewardIntegral > 0) {
            // 发奖
            IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.TEACHER_CONTENT_ERROR_NOTIFY_REWARD, rewardIntegral * 10);
            integralHistory.setComment("内容报错奖励园丁豆");
            integralHistory.setUniqueKey(teacherId + "-" + questionId);
            if (!userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
                return MapMessage.errorMessage("发放园丁豆失败！");
            }
        }
        // 系统消息
        String pcMessage = message;
        if (StringUtils.isNoneBlank(linkUrl)) {
            pcMessage = pcMessage + "<a href=\"" + linkUrl + "\">查看题目</a>";
        }
        messageCommandServiceClient.getMessageCommandService().sendUserMessage(teacherId, pcMessage);
        // app小铃铛消息
        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(teacherId);
        appMessage.setMessageType(TeacherMessageType.ACTIVIY.getType());
        appMessage.setTitle("系统通知");
        appMessage.setContent(message);
        appMessage.setImageUrl("");
        appMessage.setLinkUrl(linkUrl); // 这里写相对地址
        appMessage.setLinkType(1);
        appMessage.setIsTop(false);
        appMessage.setTopEndTime(0L);
        appMessage.setExtInfo(new HashMap<>());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);
        // jpush 消息
        Map<String, Object> extroInfo = MiscUtils.m("s", TeacherMessageType.ACTIVIY.getType(), "key", "m", "t", "msg_list");
        if (StringUtils.isNoneBlank(linkUrl)) extroInfo.put("link", linkUrl);
        appMessageServiceClient.sendAppJpushMessageByIds(message, AppMessageSource.PRIMARY_TEACHER, Collections.singletonList(teacherId), extroInfo);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage fetchSameAppActiveNumByStudentId(Long studentId, List<String> appkeys) {
        if (studentId == null || CollectionUtils.isEmpty(appkeys)) {
            return MapMessage.errorMessage("参数错误");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生未注册");
        }
        Map<String, Integer> resMap = businessVendorServiceImpl.loadUseNum(AppUseNumCalculateType.SCHOOL, appkeys, studentDetail);
        MapMessage message = MapMessage.successMessage();
        message.putAll(resMap);
        return message;
    }

}
