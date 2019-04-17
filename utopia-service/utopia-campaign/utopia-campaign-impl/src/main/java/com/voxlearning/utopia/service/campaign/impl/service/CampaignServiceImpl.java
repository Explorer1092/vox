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

package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.service.campaign.api.CampaignService;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.*;
import com.voxlearning.utopia.service.campaign.impl.dao.*;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.campaign.impl.lottery.CampaignWrapperFactory;
import com.voxlearning.utopia.service.campaign.impl.lottery.wrapper.TeacherTermBeginLotteryWrapper;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Default {@link CampaignService} implementation.
 *
 * @author Xiaohai Zhang
 * @since Aug 5, 2016
 */
@Named("com.voxlearning.utopia.service.campaign.impl.service.CampaignServiceImpl")
@ExposeServices({
        @ExposeService(interfaceClass = CampaignService.class, version = @ServiceVersion(version = "2016.08.05")),
        @ExposeService(interfaceClass = CampaignService.class, version = @ServiceVersion(version = "2018.05.24"))
})
public class CampaignServiceImpl implements CampaignService {

    @Inject private UserPopupServiceClient userPopupServiceClient;

    @Inject
    private CampaignAwardDao campaignAwardDao;
    @Inject
    private CampaignLotteryDao campaignLotteryDao;
    @Inject
    private CampaignLotteryHistoryDao campaignLotteryHistoryDao;
    @Inject
    private CampaignLotteryBigHistoryDao campaignLotteryBigHistoryDao;
    @Inject
    private CampaignLotteryFragmentHistoryDao campaignLotteryFragmentHistoryDao;
    @Inject
    private CampaignLotterySendHistoryDao campaignLotterySendHistoryDao;
    @Inject
    private CampaignWrapperFactory campaignWrapperFactory;
    @Inject
    private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    @Override
    public boolean $updateCampaignLotteryRate(Long id, Integer rate) {
        return id != null && rate != null && campaignLotteryDao.updateRate(id, rate) > 0;
    }

    @Override
    public CampaignAward $insertCampaignAward(CampaignAward document) {
        if (document == null) return null;
        campaignAwardDao.insert(document);
        return document;
    }

    @Override
    public CampaignLotteryHistory $insertCampaignLotteryHistory(CampaignLotteryHistory document) {
        if (document == null) return null;
        campaignLotteryHistoryDao.insert(document);
        return document;
    }

    @Override
    public CampaignLotteryBigHistory $insertCampaignLotteryBigHistory(CampaignLotteryBigHistory document) {
        if (document == null) return null;
        campaignLotteryBigHistoryDao.insert(document);
        return document;
    }

    @Override
    public CampaignLotterySendHistory $insertCampaignLotterySendHistory(CampaignLotterySendHistory document) {
        if (document == null) return null;
        campaignLotterySendHistoryDao.insert(document);
        return document;
    }

    @Override
    public CampaignLotteryFragmentHistory $insertCampaignLotteryFragmentHistory(CampaignLotteryFragmentHistory document) {
        if (document == null) return null;
        campaignLotteryFragmentHistoryDao.insert(document);
        return document;
    }

    @Override
    public MapMessage saveFirstGradeCampaignAward(Long userId) {
        if (userId == null) return MapMessage.errorMessage();

        // 判断用户是否已经参加过此次活动
        CampaignType campaignType = CampaignType.FIRST_GRADE_ANSWER_APPLE;
        List<CampaignAward> userAwards = campaignAwardDao.findCampaignAwards(campaignType.getId(), userId);
        if (userAwards != null && userAwards.size() > 0) {
            return MapMessage.errorMessage("你已经参加过此次活动!");
        }

        // 给用户发奖励
        IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(userId, IntegralType.一年级答题分享得学豆)
                .withIntegral(1).withComment(IntegralType.一年级答题分享得学豆.name() + "获得奖励").build();
        MapMessage message = userIntegralService.changeIntegral(history);
        if (!message.isSuccess()) {
            return MapMessage.errorMessage("系统错误，请稍后再试!");
        }

        CampaignAward award = new CampaignAward();
        award.setUserId(userId);
        award.setCampaignId(campaignType.getId());
        award.setAward(campaignType.getAward());
        award.setAwardStatus(1);
        campaignAwardDao.insert(award);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage drawLottery(CampaignType campaignType, User user, LotteryClientType clientType) {
        AbstractCampaignWrapper wrapper = campaignWrapperFactory.get(campaignType);
        if (wrapper == null) {
            return MapMessage.errorMessage("对不起，活动不存在");
        }
        return wrapper.doLottery(campaignType, user, clientType);
    }

    @Override
    public MapMessage addLotteryFreeChance(CampaignType campaignType, Long userId, int delta) {
        if (campaignType == null || userId == null) {
            return MapMessage.errorMessage();
        }
        AbstractCampaignWrapper wrapper = campaignWrapperFactory.get(campaignType);
        if (wrapper == null) {
            return MapMessage.errorMessage();
        }

        Long result = wrapper.addLotteryChance(userId, delta);

        LogCollector.info("lottery_chance_history", MiscUtils.map(
                "userId", SafeConverter.toString(userId),
                "delta", SafeConverter.toString(delta),
                "campaignType", SafeConverter.toString(campaignType),
                "result", SafeConverter.toString(result)
        ));

        return MapMessage.successMessage();
    }

    @Override
    public int getTeacherLotteryFreeChance(CampaignType campaignType, Long userId) {
        AbstractCampaignWrapper wrapper = campaignWrapperFactory.get(campaignType);
        if (wrapper == null) {
            return 0;
        }
        return wrapper.getLotteryFreeChance(userId);
    }

    @Override
    public MapMessage studentSendLotteryChance(CampaignType type, User user, Long studentId) {
        AbstractCampaignWrapper wrapper = campaignWrapperFactory.get(type);
        if (wrapper == null) {
            return MapMessage.errorMessage("对不起，活动不存在");
        }
        long freeCount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCount(UserBehaviorType.STUDENT_FREE_SEND_LOTTERY_CHANCE, user.getId())
                .getUninterruptibly();
        if (freeCount < 2) {
            return MapMessage.errorMessage("对不起，你已经没有抽奖次数可以赠送了。");
        }
        // 减去缓存里的两次
        asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_decrUserBehaviorCount(UserBehaviorType.STUDENT_FREE_SEND_LOTTERY_CHANCE, user.getId(), 2L, DateUtils.getCurrentToDayEndSecond())
                .awaitUninterruptibly();
        // 给同学送两次机会
        wrapper.addLotteryChance(studentId, 2);
        // 记录赠送历史
        CampaignLotterySendHistory history = new CampaignLotterySendHistory();
        history.setCampaignId(type.getId());
        history.setCount(2);
        history.setReceiverId(studentId);
        history.setSenderId(user.getId());
        history.setSenderName(user.fetchRealname());
        $insertCampaignLotterySendHistory(history);
        String comment = "有同学送你免费抽奖机会，当天有效。";
        comment = StringUtils.formatMessage(
                comment + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【立即去抽奖】</a>",
                "/campaign/studentlottery.vpage"
        );
        long pcount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                .unflushable_getUserBehaviorCount(UserBehaviorType.STUDENT_RECEIVE_FREE_LOTTERY_CHANCE_NOTICE_DAY_COUNT, studentId)
                .getUninterruptibly();
        if (pcount == 0) {
            //右下角弹窗 只弹一次
            userPopupServiceClient.createPopup(studentId)
                    .content(comment)
                    .type(PopupType.STUDENT_LOTTERY_SEND_CHANCE_NOTICE)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .unflushable_incUserBehaviorCount(UserBehaviorType.STUDENT_RECEIVE_FREE_LOTTERY_CHANCE_NOTICE_DAY_COUNT, studentId, 1L, DateUtils.getCurrentToDayEndSecond())
                    .awaitUninterruptibly();
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage hadArrangedHomework(CampaignType campaignType, Long userId) {
        TeacherTermBeginLotteryWrapper wrapper = (TeacherTermBeginLotteryWrapper)campaignWrapperFactory.get(campaignType);
        if (wrapper == null) {
            return MapMessage.errorMessage();
        }

        return wrapper.hadArrangedHomework(userId);
    }

    @Override
    public CampaignLottery $insertCampaignLottery(CampaignLottery campaignLottery) {
        return campaignLotteryDao.upsert(campaignLottery);
    }

    @Override
    public CampaignLottery $updateCampaignLottery(CampaignLottery campaignLottery) {
        return campaignLotteryDao.upsert(campaignLottery);
    }

    public MapMessage validateCampaignLottery(CampaignType campaignType, CampaignLottery campaignLottery) {
        AbstractCampaignWrapper wrapper = campaignWrapperFactory.get(campaignType);
        if (wrapper == null) {
            return MapMessage.errorMessage("活动不存在");
        }
        return wrapper.validateCampaignLottery(campaignLottery);
    }

}
