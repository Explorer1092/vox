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

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryFragmentHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserBehaviorType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.AsyncUserBehaviorServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Summer Yang
 * @since 2016/1/6
 */
@Named
public class StudentLotteryWrapper extends AbstractCampaignWrapper {

    private final static String cacheKey = "STUDENT_LOTTERY_FREE_CHANCE";
    private final static int expiration = 86400; // 有效期24小时

    @Inject private AsyncUserBehaviorServiceClient asyncUserBehaviorServiceClient;
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
        int initialCount = 0;
        Boolean ret = campaignCacheSystem.CBS.persistence.add(key, expiration, Integer.toString(initialCount));
        if (Boolean.TRUE.equals(ret)) {
            return initialCount;
        } else {
            logger.warn("Add '{}' failed, maybe someone has already add this value", key);
            return NumberUtils.toInt(StringUtils.trim(campaignCacheSystem.CBS.persistence.load(key)));
        }
    }

    private int getLotteryCount(Integer awardId) {
        switch (awardId) {
            case 1:
                return 500;
            case 2:
                return 200;
            case 3:
                return 100;
            case 4:
                return 50;
            case 6:
                return 10;
            default:
        }
        return 0;
    }


    @Override
    public MapMessage doLottery(CampaignType campaignType, final User user, LotteryClientType clientType) {
        if (user == null || user.fetchUserType() != UserType.STUDENT) {
            return MapMessage.errorMessage("对不起，您不能参与该活动");
        }
        if (getLotteryFreeChance(user.getId()) <= 0) {
            //扣除2学豆
            UserIntegral integral = studentLoaderClient.loadStudentDetail(user.getId()).getUserIntegral();
            if (integral == null || integral.getUsable() < 2) {
                return MapMessage.errorMessage("学豆不足");
            }
            //扣钱
            try {
                IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.学生抽奖_产品平台, -2);
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
            //发奖
            int awardId = ConversionUtils.toInt(winLottery.get("awardId"));
            if (awardId < 7) {
                if (awardId == 5) {
                    // 中了碎片的处理
                    // 首先查看最近有没有凑齐过碎片
                    Date startDate = DateUtils.calculateDateDay(new Date(), -10);
                    List<CampaignLotteryFragmentHistory> histories = campaignLoader.findCampaignLotteryFragmentHistories(campaignType.getId(), user.getId());
                    histories = histories.stream().filter(h -> h.getAwardId() == awardId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(histories)) {
                        CampaignLotteryFragmentHistory recentHis = MiscUtils.firstElement(histories);
                        if (recentHis.getCreateDatetime().after(startDate)) {
                            startDate = recentHis.getCreateDatetime();
                        }
                    }
                    // 历史做兼容 查询41 42 43的历史碎片
                    List<CampaignLotteryHistory> lotteryHistories = new ArrayList<>();
                    if (new Date().before(DateUtils.stringToDate("2016-09-26 23:59:59"))) {
                        List<CampaignLotteryHistory> lotteryHistories43 = campaignLoader.findCampaignLotteryHistories(43, user.getId());

                        List<CampaignLotteryHistory> lotteryHistories41 = campaignLoader.findCampaignLotteryHistories(41, user.getId());

                        List<CampaignLotteryHistory> lotteryHistories42 = campaignLoader.findCampaignLotteryHistories(42, user.getId());
                        if (CollectionUtils.isNotEmpty(lotteryHistories41)) {
                            lotteryHistories.addAll(lotteryHistories41);
                        }
                        if (CollectionUtils.isNotEmpty(lotteryHistories42)) {
                            lotteryHistories.addAll(lotteryHistories42);
                        }
                        if (CollectionUtils.isNotEmpty(lotteryHistories43)) {
                            lotteryHistories.addAll(lotteryHistories43);
                        }
                    } else {
                        lotteryHistories = campaignLoader.findCampaignLotteryHistories(campaignType.getId(), user.getId());
                    }

                    final Date finalStartDate = startDate;
                    long count = lotteryHistories.stream().filter(h -> h.getCreateDatetime().after(finalStartDate))
                            .filter(h -> h.getAwardId() == awardId).count();
                    if (count == 5) {
                        // 正好凑齐5个了， 可以换一次奖品了 奖励50学豆
                        IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.学生抽奖_产品平台, 50);
                        integralHistory.setComment("集齐碎片获得奖励");
                        if (!userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
                            throw new RuntimeException();
                        }
                        // 记录获奖历史
                        CampaignLotteryFragmentHistory history = new CampaignLotteryFragmentHistory();
                        history.setCampaignId(campaignType.getId());
                        history.setAwardId(awardId);
                        history.setAwardName(lotteryResult.getAwardName());
                        history.setUserId(user.getId());
                        campaignService.$insertCampaignLotteryFragmentHistory(history);
                        result.add("exchanged", true);
                    }
                    result.add("fragment", true);
                } else {
                    // 滚动信息
                    addRecentCampaignLotteryResultForStudent(user, lotteryResult, campaignType.getId(), studentLoaderClient.loadStudentDetail(user.getId()).getClazzId());
                    int value = getLotteryCount(awardId);
                    if (value > 0) {
                        IntegralHistory integralHistory = new IntegralHistory(user.getId(), IntegralType.学生抽奖_产品平台, value);
                        integralHistory.setComment("抽奖奖励");
                        if (!userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
                            throw new RuntimeException();
                        }
                    }
                }
                // 蛋疼的弹窗
                if (awardId == 1) {
                    // 给全班同学弹窗 一天只弹一次
                    Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
                    if (clazz != null) {
                        AlpsThreadPool.getInstance().submit(() -> {
                            sendPopup(user, clazz.getId());
                        });
                    }
                }
            }
            // 是否添加免费机会
            if (awardId == 7) {
                // 没中奖
                long count = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                        .persistence_getUserBehaviorCount(UserBehaviorType.STUDENT_LOTTERY_GET_FREE_COUNT, user.getId())
                        .getUninterruptibly();
                if (count == 0) {
                    List<CampaignLotteryHistory> histories = campaignLoader.findCampaignLotteryHistories(campaignType.getId(), user.getId());
                    List<CampaignLotteryHistory> lotteryHistories = histories.stream().filter(h -> h.getAwardId() < 7).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(lotteryHistories)) {
                        if (histories.size() == 5) {
                            // 自己加三次  要多加一次 因为下面减了一次
                            addLotteryChance(user.getId(), 4);
                            // 缓存两次
                            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                                    .persistence_incUserBehaviorCount(UserBehaviorType.STUDENT_FREE_SEND_LOTTERY_CHANCE, user.getId(), 2L, DateUtils.getCurrentToDayEndSecond())
                                    .awaitUninterruptibly();
                            // 记录本次机会已经触发
                            asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                                    .persistence_incUserBehaviorCount(UserBehaviorType.STUDENT_LOTTERY_GET_FREE_COUNT, user.getId(), 1L, DateUtils.getCurrentToDayEndSecond())
                                    .awaitUninterruptibly();
                            result.add("free", true);
                        }
                    }
                }
            }
            //免费机会减1
            String key = CacheKeyGenerator.generateCacheKey(cacheKey, null, new Object[]{user.getId()});
            campaignCacheSystem.CBS.persistence.decr(
                    key,
                    1,
                    0,
                    expiration);
            return result;
        } catch (Exception ex) {
            logger.error("STUDENT DRAW LOTTERY ERROR, {}", ex.getMessage());
            return MapMessage.errorMessage("抽奖失败");
        }
    }

    private void sendPopup(User user, Long clazzId) {
        List<User> userList = userAggregationLoaderClient.loadLinkedStudentsBySystemClazzId(clazzId, user.getId());
        if (CollectionUtils.isNotEmpty(userList)) {
            String comment = user.fetchRealname() + "同学在『幸运大抽奖』中抽中500学豆。";
            comment = StringUtils.formatMessage(
                    comment + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【我也去抽奖】</a>",
                    "/campaign/studentlottery.vpage"
            );
            long pcount = asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                    .persistence_getUserBehaviorCount(UserBehaviorType.CLAZZ_WIN_BIG_LOTTERY_NOTICE_COUNT, clazzId)
                    .getUninterruptibly();
            if (pcount == 0) {
                //右下角弹窗 只弹一次
                for (User u : userList) {
                    userPopupServiceClient.createPopup(u.getId())
                            .content(comment)
                            .type(PopupType.STUDENT_LOTTERY_BIG_NOTICE)
                            .category(PopupCategory.LOWER_RIGHT)
                            .create();
                }
                asyncUserBehaviorServiceClient.getAsyncUserBehaviorService()
                        .persistence_incUserBehaviorCount(UserBehaviorType.CLAZZ_WIN_BIG_LOTTERY_NOTICE_COUNT, clazzId, 1L, DateUtils.getCurrentToDayEndSecond())
                        .awaitUninterruptibly();
            }
        }
    }

    @Override
    public Long addLotteryChance(Long userId, int delta) {
        if (delta == 0) {
            return 0L;
        }
        String key = CacheKeyGenerator.generateCacheKey(cacheKey, null, new Object[]{userId});
        Long ret = campaignCacheSystem.CBS.persistence.incr(key, delta, delta, expiration);
        if (ret == null) {
            logger.error("Failed increase {} student lottery free chance with delta {}", userId, delta);
            return 0L;
        }
        return ret;
    }
}

