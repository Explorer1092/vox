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

package com.voxlearning.utopia.service.campaign.impl.lottery.wrapper;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryBigHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserActivityType;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserActivity;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author XiaoPeng.Yang
 * @since 14-11-4
 */
@Named
public class TeacherLotteryWrapper extends AbstractCampaignWrapper {

    private final static String cacheKey = "TEACHER_LOTTERY_FREE_CHANCE_NEW";
    private final static String pcLotteryCountCacheKey = "TEACHER_DAY_LOTTERY_COUNT";
    private final static String teacherBigLotteryDayCountKey = "TEACHER_BIG_LOTTERY_DAY_COUNT";

    @Inject private UserActivityServiceClient userActivityServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;

    @Override
    public int getLotteryFreeChance(Long userId) {
        String key = CacheKeyGenerator.generateCacheKey(cacheKey, null, new Object[]{userId});
        CacheObject<String> cacheObject = campaignCacheSystem.CBS.persistence.get(key);
        if (cacheObject == null) {
            return 0;
        }
        String response = StringUtils.trim(cacheObject.getValue());
        if (StringUtils.isNotBlank(response)) {
            return NumberUtils.toInt(response);
        }
        int initialCount = calculateInitialCount(userId);
        int expiration = DateUtils.getCurrentToDayEndSecond();
        Boolean ret = campaignCacheSystem.CBS.persistence.add(key, expiration, Integer.toString(initialCount));
        if (Boolean.TRUE.equals(ret)) {
            return initialCount;
        } else {
            logger.warn("Add '{}' failed, maybe someone has already add this value", key);
            return NumberUtils.toInt(StringUtils.trim(campaignCacheSystem.CBS.persistence.load(key)));
        }
    }

    private int calculateInitialCount(Long userId) {
        int chance = 0;
        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(userId);
        if (extAttribute != null && extAttribute.fetchTeacherLevel().getLevel() >= 13) {
            chance = 1;
        }
        return chance;
    }

    @Override
    public MapMessage doLottery(CampaignType campaignType, final User user, LotteryClientType clientType) {
        //判断资格
        if (user.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage("认证老师才能参与本次活动");
        }
        //最近7天检查过作业
        List<UserActivity> activityList = userActivityServiceClient.getUserActivityService()
                .findUserActivities(user.getId())
                .getUninterruptibly();
        UserActivity activity = activityList.stream().filter(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                .filter(a -> a.getActivityTime().after(DateUtils.calculateDateDay(new Date(), -7))).findFirst().orElse(null);
        if (activity == null) {
            // 判断一下是否有副账号， 如果有， 校验副账号
            List<Long> subTeacherIds = teacherLoaderClient.loadSubTeacherIds(user.getId());
            if (CollectionUtils.isNotEmpty(subTeacherIds)) {
                for (Long teacherId : subTeacherIds) {
                    activityList = userActivityServiceClient.getUserActivityService()
                            .findUserActivities(teacherId)
                            .getUninterruptibly();
                    activity = activityList.stream().filter(a -> a.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                            .filter(a -> a.getActivityTime().after(DateUtils.calculateDateDay(new Date(), -7))).findFirst().orElse(null);
                    if (activity != null) {
                        break;
                    }
                }
            }
            if (activity == null) {
                return MapMessage.errorMessage("对不起，您还未检查作业，检查作业后才能参与本次活动");
            }
        }
        //当天平台抽奖总次数限制  每天5次
        String totalKey = CacheKeyGenerator.generateCacheKey(pcLotteryCountCacheKey, null, new Object[]{user.getId()});
        if (clientType == LotteryClientType.PC) {
            String response = campaignCacheSystem.CBS.persistence.load(totalKey);
            if (StringUtils.isNotBlank(response) && NumberUtils.toInt(response) >= 5) {
                return MapMessage.errorMessage("今日平台抽奖次数已用完");
            }
        }

        //没有免费机会 扣除金币
        if (getLotteryFreeChance(user.getId()) <= 0) {
            //没有机会 扣除1金币
            // 判断余额
            UserIntegral integral = teacherLoaderClient.loadTeacherDetail(user.getId()).getUserIntegral();
            if (integral == null || integral.getUsable() < 1) {
                return MapMessage.errorMessage("园丁豆不足");
            }
            //扣钱
            try {
                IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.老师转盘抽奖, -10);
                integralHistory.setComment("抽奖消耗");
                if (!userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
                    throw new RuntimeException();
                }
            } catch (Exception ex) {
                return MapMessage.errorMessage("抽奖失败");
            }
        }
        try {
            // 抽奖处理
            CampaignLottery lotteryResult = drawLottery(campaignType.getId());
            if (lotteryResult == null) {
                return MapMessage.successMessage().add("win", false);
            }
            //每周大奖限制 手机两个 平板一个
            if (lotteryResult.getAwardId() <= 2) {
                boolean emptyFlag = false;
                List<CampaignLotteryBigHistory> histories = campaignLoader.findCampaignLotteryBigHistories(campaignType.getId(), lotteryResult.getAwardId());
                if (CollectionUtils.isNotEmpty(histories)) {
                    histories = histories.stream().filter(h -> h.getCreateDatetime().after(WeekRange.current().getStartDate())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(histories) && histories.size() >= lotteryResult.getAwardId()) {
                        emptyFlag = true;
                    }
                }
                //判断当前用户是否中过大奖
                List<CampaignLotteryBigHistory> bigHis = campaignLoader.findCampaignLotteryBigHistories(campaignType.getId())
                        .stream().filter(c -> Objects.equals(c.getUserId(), user.getId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(bigHis)) {
                    emptyFlag = true;
                }
                // 大奖每天限制出一个
                String response = campaignCacheSystem.CBS.persistence.load(teacherBigLotteryDayCountKey);
                if (StringUtils.isNotBlank(response)) {
                    emptyFlag = true;
                }
                if (emptyFlag) {
                    lotteryResult = getLastLottery(campaignType.getId());
                }
            }
            MapMessage result = MapMessage.successMessage();
            result.add("win", true);
            Map<String, Object> winLottery = new HashMap<>();
            winLottery.put("campaignId", lotteryResult.getCampaignId());
            winLottery.put("awardId", lotteryResult.getAwardId());
            winLottery.put("awardLevelName", lotteryResult.getAwardLevelName());
            winLottery.put("awardName", lotteryResult.getAwardName());
            result.add("lottery", winLottery);
            // 记录抽奖结果
            CampaignLotteryHistory userLotteryHistory = new CampaignLotteryHistory();
            userLotteryHistory.setUserId(user.getId());
            userLotteryHistory.setCampaignId(campaignType.getId());
            userLotteryHistory.setAwardId(ConversionUtils.toInt(winLottery.get("awardId")));
            campaignService.$insertCampaignLotteryHistory(userLotteryHistory);
            if (ConversionUtils.toInt(winLottery.get("awardId")) != 8) {
                // 这里分两种处理 一种是非假期 一种是假期
                dealNotInHoliday(winLottery, user, lotteryResult, campaignType);
//                dealInHoliday(winLottery, user, lotteryResult, campaignType);
            }
            //免费机会减1
            String key = CacheKeyGenerator.generateCacheKey(cacheKey, null, new Object[]{user.getId()});
            campaignCacheSystem.CBS.persistence.decr(
                    key,
                    1,
                    1,
                    DateUtils.getCurrentToDayEndSecond());
            //统计次数
            if (clientType == LotteryClientType.PC) {
                campaignCacheSystem.CBS.persistence.incr(totalKey, 1, 1, DateUtils.getCurrentToDayEndSecond());
            }
            //点亮老师抽奖图标
            ambassadorServiceClient.getAmbassadorService().recordAmbassadorMentor(user.getId(), MiscUtils.map(UserTagType.AMBASSADOR_MENTOR_DO_LOTTERY, UserTagEventType.AMBASSADOR_MENTOR_DO_LOTTERY));
            return result;
        } catch (Exception ex) {
            logger.error("TEACHER DRAW LOTTERY ERROR, {}", ex.getMessage());
            return MapMessage.errorMessage("抽奖失败");
        }
    }

    private void dealInHoliday(Map<String, Object> winLottery, User user, CampaignLottery lotteryResult, CampaignType campaignType) {
        // 假期处理方法 奖项全部为金币
        addRecentCampaignLotteryResult(user, lotteryResult, campaignType.getId());
        //发奖  金币
        int value = 0;
        switch (ConversionUtils.toInt(winLottery.get("awardId"))) {
            case 1:
                value = 10000;
                break;
            case 2:
                value = 5000;
                break;
            case 3:
                value = 1000;
                break;
            case 4:
                value = 500;
                break;
            case 5:
                value = 100;
                break;
            case 6:
                value = 50;
                break;
            case 7:
                value = 10;
                break;
            default:
        }
        IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.老师转盘抽奖奖励, value);
        integralHistory.setComment("抽奖奖励");
        if (!userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
            throw new RuntimeException();
        }
    }

    private void dealNotInHoliday(Map<String, Object> winLottery, User user, CampaignLottery lotteryResult, CampaignType campaignType) {
        if (ConversionUtils.toInt(winLottery.get("awardId")) <= 2) {
            addRecentCampaignLotteryResultBig(user, lotteryResult, campaignType.getId());
            // 大奖每天限制一个 记录缓存
            campaignCacheSystem.CBS.persistence.incr(teacherBigLotteryDayCountKey, 1, 1, DateUtils.getCurrentToDayEndSecond());
            String comment = "尊敬的" + user.fetchRealname() + "老师，" +
                    "恭喜您获得" + winLottery.get("awardName") + "，中奖信息将提交系统审核，如审核通过，奖品将在7个工作日内发货，请耐心等待。";
            //系统消息
            teacherLoaderClient.sendTeacherMessage(user.getId(), comment);
            //右下角弹窗
            userPopupServiceClient.createPopup(user.getId())
                    .content(comment)
                    .type(PopupType.WIN_LOTTERY_NOTICE)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
            //发邮件
            if (RuntimeMode.ge(Mode.STAGING)) {
                Map<String, Object> content = new HashMap<>();
                content.put("info", "大奖产生，得奖人ID：" + user.getId() + "，中了" + winLottery.get("awardName"));
                emailServiceClient.createTemplateEmail(EmailTemplate.office)
                        .to("xiaohan.yu@17zuoye.com")
                        .cc("xiaopeng.yang@17zuoye.com")
                        .subject("中奖老师资料（平台抽奖）")
                        .content(content)
                        .send();
            }
            // 记录大奖记录
            CampaignLotteryBigHistory bigHistory = new CampaignLotteryBigHistory();
            bigHistory.setAwardId(ConversionUtils.toInt(winLottery.get("awardId")));
            bigHistory.setUserId(user.getId());
            bigHistory.setCampaignId(campaignType.getId());
            campaignService.$insertCampaignLotteryBigHistory(bigHistory);
        }
        addRecentCampaignLotteryResult(user, lotteryResult, campaignType.getId());
        //发奖  金币
        int value = 0;
        switch (ConversionUtils.toInt(winLottery.get("awardId"))) {
            case 3:
                value = 1000;
                break;
            case 4:
                value = 500;
                break;
            case 5:
                value = 100;
                break;
            case 6:
                value = 50;
                break;
            case 7:
                value = 10;
                break;
            default:
        }
        if (value > 0) {
            IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.老师转盘抽奖奖励, value);
            integralHistory.setComment("抽奖奖励");
            if (!userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
                throw new RuntimeException();
            }
        }
    }

    public Long addLotteryChance(Long userId, int delta) {
        if (delta == 0) {
            return 0L;
        }
        String key = CacheKeyGenerator.generateCacheKey(cacheKey, null, new Object[]{userId});
        int expirtation = DateUtils.getCurrentToDayEndSecond();
        int initialValue = calculateInitialCount(userId) + delta;
        Long ret = campaignCacheSystem.CBS.persistence.incr(key, delta, initialValue, expirtation);
        if (ret == null) {
            logger.error("Failed increase {} teacher lottery free chance with delta {}", userId, delta);
            return 0L;
        }
        return ret;
    }
}
