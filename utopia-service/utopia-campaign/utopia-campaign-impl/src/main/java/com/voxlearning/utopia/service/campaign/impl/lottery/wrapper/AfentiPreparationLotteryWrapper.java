package com.voxlearning.utopia.service.campaign.impl.lottery.wrapper;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.LotteryClientType;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryBigHistory;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import com.voxlearning.utopia.service.campaign.impl.lottery.AbstractCampaignWrapper;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer on 2017/7/14.
 */
@Named
public class AfentiPreparationLotteryWrapper extends AbstractCampaignWrapper {
    private static final String lotteryChanceKey = "AFENTI_PREPARATION_LOTTERY_FREE_CHANCE:";
    private static final int activityExpiration = (int) (DateUtils.stringToDate("2017-09-15 23:59:59").getTime() / 1000);

    @Inject private CouponServiceClient couponServiceClient;

    @Override
    public int getLotteryFreeChance(Long userId) {
        String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{userId});
        CacheObject<String> cacheObject = campaignCacheSystem.CBS.persistence.get(key);
        if (cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue())) {
            return NumberUtils.toInt(StringUtils.trim(cacheObject.getValue()));
        }
        return 0;
    }

    @Override
    public MapMessage doLottery(CampaignType campaignType, final User user, LotteryClientType clientType) {
        if (getLotteryFreeChance(user.getId()) <= 0) {
            return MapMessage.errorMessage("对不起，您没有抽奖次数了");
        }
        try {
            // 抽奖处理
            CampaignLottery lotteryResult = drawLottery(campaignType.getId());
            if (lotteryResult == null) {
                return MapMessage.successMessage().add("win", false);
            }
            // 实物奖品数量限制
            if (lotteryResult.getAwardId() <= 5) {
                boolean emptyFlag = false;
                // 所有大奖记录
                List<CampaignLotteryBigHistory> histories = campaignLoader.findCampaignLotteryBigHistories(campaignType.getId(), lotteryResult.getAwardId());
                if (histories != null && histories.size() >= getMaxCount(lotteryResult.getAwardId())) {
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
            String awardName = lotteryResult.getAwardName();
            winLottery.put("awardName", awardName);
            result.add("lottery", winLottery);
            // 记录抽奖结果
            CampaignLotteryHistory userLotteryHistory = new CampaignLotteryHistory();
            userLotteryHistory.setUserId(user.getId());
            userLotteryHistory.setCampaignId(campaignType.getId());
            userLotteryHistory.setAwardId(SafeConverter.toInt(winLottery.get("awardId")));
            campaignService.$insertCampaignLotteryHistory(userLotteryHistory);
            // 记录最近的用户中奖信息
            if (SafeConverter.toInt(winLottery.get("awardId")) > 3) {
                addRecentCampaignLotteryForWeek(user, lotteryResult, campaignType.getId());
            }
            if (SafeConverter.toInt(winLottery.get("awardId")) <= 5) {
                // 添加缓存大奖纪录
                if (SafeConverter.toInt(winLottery.get("awardId")) <= 3) {
                    addRecentCampaignLotteryResultBig(user, lotteryResult, campaignType.getId(), activityExpiration);
                }
                // 记录大奖记录
                CampaignLotteryBigHistory bigHistory = new CampaignLotteryBigHistory();
                bigHistory.setAwardId(ConversionUtils.toInt(winLottery.get("awardId")));
                bigHistory.setUserId(user.getId());
                bigHistory.setCampaignId(campaignType.getId());
                campaignService.$insertCampaignLotteryBigHistory(bigHistory);
            }
            //发奖
            String couponId = "";
            switch (SafeConverter.toInt(winLottery.get("awardId"))) {
                case 6:
                    couponId = "";
                    if (RuntimeMode.current().ge(Mode.STAGING)) {
                        couponId = "";
                    }
                    break;
                case 7:
                    couponId = "";
                    if (RuntimeMode.current().ge(Mode.STAGING)) {
                        couponId = "";
                    }
                    break;
                case 8:
                    couponId = "";
                    if (RuntimeMode.current().ge(Mode.STAGING)) {
                        couponId = "";
                    }
                    break;
                default:
            }
            if (StringUtils.isNotBlank(couponId)) {
                couponServiceClient.sendCoupon(couponId, user.getId());
            }
            // 免费机会减1
            String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{user.getId()});
            campaignCacheSystem.CBS.persistence.decr(
                    key,
                    1,
                    1,
                    activityExpiration);
            return result;
        } catch (Exception ex) {
            logger.error("AFENTI PREPARATION LOTTERY ERROR, {}", ex.getMessage());
            return MapMessage.errorMessage("抽奖失败");
        }
    }

    private int getMaxCount(Integer awardId) {
        if (awardId == 1) {
            return 1;
        }
        if (awardId == 2) {
            return 1;
        }
        if (awardId == 3) {
            return 1;
        }
        if (awardId == 4) {
            return 30;
        }
        if (awardId == 5) {
            return 30;
        }
        return 0;
    }

    public Long addLotteryChance(Long userId, int delta) {
        if (delta == 0) {
            return 0L;
        }
        String key = CacheKeyGenerator.generateCacheKey(lotteryChanceKey, null, new Object[]{userId});
        Long ret = campaignCacheSystem.CBS.persistence.incr(key, delta, delta, activityExpiration);
        if (ret == null) {
            logger.error("Failed increase {} afenti preparation lottery free chance with delta {}", userId, delta);
            return 0L;
        }
        return ret;
    }
}
