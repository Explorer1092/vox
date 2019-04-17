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

package com.voxlearning.utopia.service.campaign.impl.lottery.wrapper;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.AwardType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryBigHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Summer Yang
 * @since 2015/7/28
 */
@Named
public class TeacherTermBeginLotteryWrapper extends AbstractCampaignWrapper {
    private static final String lotteryChanceKey = "JUNIOR_ARRANGE_HOMEWORK_LOTTERY_CHANCE";
    private final static String bigLotteryWeekCountKey = "BIG_LOTTERY_WEEK_COUNT_KEY_";

    @Override
    public int getLotteryFreeChance(Long userId) {
        String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{userId});
        CacheObject<String> cacheObject = campaignCacheSystem.CBS.persistence.get(key);
        if (cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue())) {
            return NumberUtils.toInt(StringUtils.trim(cacheObject.getValue()));
        }
        return 0;
    }

    public MapMessage hadArrangedHomework(Long userId) {
        String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{userId});
        CacheObject<String> cacheObject = campaignCacheSystem.CBS.persistence.get(key);
        if (cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue())) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

    @Override
    public MapMessage doLottery(CampaignType campaignType, final User user, LotteryClientType clientType) {
        Map<String, Object> result = new HashMap<>();

        if (getLotteryFreeChance(user.getId()) <= 0) {
            return MapMessage.errorMessage("对不起，您没有抽奖次数了");
        }

        try {
            // 抽奖处理
            CampaignLottery lotteryResult = drawLottery(campaignType.getId());
            if (lotteryResult == null) {
                return MapMessage.errorMessage("抽奖失败,请稍候再试");
            }

            List<Map> jsonArray = JsonUtils.fromJsonToList(lotteryResult.getAwardContent(), Map.class);
            List<CampaignLotteryBigHistory> bigHistories = campaignLoader
                    .findCampaignLotteryBigHistories(CampaignType.JUNIOR_ARRANGE_HOMEWORK_LOTTERY.getId());
            for (Map<String, Object> jsonObject : jsonArray) {
                //判断当前用户是否中过大奖
                bigHistories = bigHistories.stream()
                        .filter(h -> Objects.equals(user.getId(), h.getUserId()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(bigHistories)) {
                    lotteryResult.setAwardId(7);
                    result.put("awardId", lotteryResult.getAwardId());
                    result.put("awardName", "谢谢参与");
                    result.put("isVirtual", true);
                    // 免费机会减1
                    decrChance(user.getId());
                    return MapMessage.successMessage().add("result", result);
                }

                //抽中实物发邮件
                if (AwardType.INTEGRAL.name().equals(jsonObject.get("type"))) {//中学豆
                    sendBeans(user.getId(), ConversionUtils.toInt(jsonObject.get("num")));
                    result.put("awardId", lotteryResult.getAwardId());
                    result.put("awardName", lotteryResult.getAwardName());
                    result.put("isVirtual", true);
                } else if (AwardType.RC_PRODUCT.name().equals(jsonObject.get("type"))) {
                    if (!isRemainder(lotteryResult)) {
                        lotteryResult.setAwardId(7);
                        result.put("awardId", lotteryResult.getAwardId());
                        result.put("awardName", "谢谢参与");
                        result.put("isVirtual", true);
                    } else {
                        //发邮件
                        Map<String, Object> content = new HashMap<>();
                        content.put("info", "大奖产生，得奖人ID：" + user.getId() + "，中了" + lotteryResult.getAwardName());
                        emailServiceClient.createTemplateEmail(EmailTemplate.office)
                                .to("jiamin.lin@17zuoye.com;sha.zeng@17zuoye.com;xu.yan@17zuoye.com")
                                .cc("zhilong.hu@17zuoye.com;jiechun.xiao@17zuoye.com")
                                .subject("中奖老师资料（布置作业抽奖）环境：" + RuntimeMode.getCurrentStage())
                                .content(content)
                                .send();

                        addRecentCampaignLotteryResultBig(user, lotteryResult, campaignType.getId(), DateUtils.getCurrentToWeekEndSecond());
                        // 记录大奖记录
                        CampaignLotteryBigHistory bigHistory = new CampaignLotteryBigHistory();
                        bigHistory.setAwardId(lotteryResult.getAwardId());
                        bigHistory.setUserId(user.getId());
                        bigHistory.setCampaignId(lotteryResult.getCampaignId());
                        campaignService.$insertCampaignLotteryBigHistory(bigHistory);
                        result.put("awardId", lotteryResult.getAwardId());
                        result.put("awardName", lotteryResult.getAwardName());
                        result.put("isVirtual", false);
                    }
                } else {//谢谢参与
                    result.put("awardId", lotteryResult.getAwardId());
                    result.put("awardName", lotteryResult.getAwardName());
                    result.put("isVirtual", true);
                }
            }

            // 记录抽奖结果
            CampaignLotteryHistory userLotteryHistory = new CampaignLotteryHistory();
            userLotteryHistory.setUserId(user.getId());
            userLotteryHistory.setCampaignId(lotteryResult.getCampaignId());
            userLotteryHistory.setAwardId(lotteryResult.getAwardId());
            campaignService.$insertCampaignLotteryHistory(userLotteryHistory);

            // 免费机会减1
            decrChance(user.getId());
            return MapMessage.successMessage().add("result", result);
        } catch (Exception ex) {
            logger.error("TEACHER DRAW TERM BEGIN LOTTERY ERROR, {}", ex.getMessage());
            return MapMessage.errorMessage("抽奖失败");
        }
    }

    private void decrChance(Long userId) {
        String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{userId});
        campaignCacheSystem.CBS.persistence.decr(
                key,
                1,
                1,
                DateUtils.getCurrentToDayEndSecond());
    }

    public Long addLotteryChance(Long userId, int delta) {
        if (delta == 0) {
            return 0L;
        }
        String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{userId});
        Long ret = campaignCacheSystem.CBS.persistence.incr(key, delta, delta, DateUtils.getCurrentToDayEndSecond());
        if (ret == null) {
            logger.error("Failed increase {} teacher term begin lottery free chance with delta {}", userId, delta);
            return 0L;
        }
        return ret;
    }

    private void sendBeans(Long userId, int num) {
        IntegralHistory integralHistory = new IntegralHistory(userId, IntegralType.JUNIOR_ARRANGE_HOMEWORK_LOTTERY, +num);
        integralHistory.setComment("初中英语认证老师布置作业抽奖");
        if (!userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory).isSuccess()) {
            logger.error("teacher term begin lottery add integral error, tid {}, amount {}", userId, num);
        }
    }

    private boolean isRemainder(CampaignLottery lotteryResult) {
        String cacheKey = bigLotteryWeekCountKey + lotteryResult.getAwardId();
        String cacheValue = campaignCacheSystem.CBS.persistence.load(cacheKey);
        Boolean flag = false;
        if (lotteryResult.getAwardId() == 1) {
            if (StringUtils.isBlank(cacheValue)) {
                flag = true;
            }
        } else if (lotteryResult.getAwardId() == 2) {
            if (StringUtils.isBlank(cacheValue) || ConversionUtils.toInt(StringUtils.trim(cacheValue)) < 2) {
                flag = true;
            }
        } else if (lotteryResult.getAwardId() == 3) {
            if (StringUtils.isBlank(cacheValue) || ConversionUtils.toInt(StringUtils.trim(cacheValue)) < 3) {
                flag = true;
            }
        } else if (lotteryResult.getAwardId() == 4) {
            if (StringUtils.isBlank(cacheValue) || ConversionUtils.toInt(StringUtils.trim(cacheValue)) < 4) {
                flag = true;
            }
        }

        if (flag) {
            campaignCacheSystem.CBS.persistence.incr(cacheKey, 1, 1, DateUtils.getCurrentToWeekEndSecond());
        }

        return flag;
    }


}
